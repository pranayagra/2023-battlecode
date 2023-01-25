package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.knowledge.Cache;
import basicbot.knowledge.RunningMemory;
import basicbot.utils.Constants;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

import java.util.HashMap;

public class HeadQuarters extends Robot {
  /*WORKFLOW_ONLY*///private int totalSpawns = 0;
  private static final int NUM_FORCED_LATE_GAME_ANCHORS = 3;
  private static final int INCOME_MOVING_AVERAGE_WINDOW_SIZE = 100;

  private int hqID;
  public final WellInfo closestAdamantium;
  public final WellInfo closestMana;

  private WellInfo closestWell;
  private MapLocation targetWell;
  private boolean targetWellUpgraded;

  private int numAnchorsMade;


  private HQRole role;

  private boolean spawnAmplifier;
  private int spawnAmplifierSafe;
  private int spawnAmplifierCooldown;

  private MapLocation lastSpawnLoc = null;

  private int carrierSpawnOrderIdx = 0;
  private final SpawnType[] carrierSpawnOrder = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA};//new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};

  private static final SpawnType[] spawnOrderEnemyHQHere = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};
  private static final SpawnType[] spawnOrder20x20 = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.CARRIER_ADAMANTIUM};
  private static final SpawnType[] spawnOrder40x40 = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER};
  private static final SpawnType[] spawnOrder60x60 = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER};
  private static final SpawnType[] spawnOrderEndangeredWells = spawnOrder20x20;

  private final int[] adamantiumIncomeHistory = new int[INCOME_MOVING_AVERAGE_WINDOW_SIZE];
  private final int[] manaIncomeHistory = new int[INCOME_MOVING_AVERAGE_WINDOW_SIZE];
  private final int[] elixirIncomeHistory = new int[INCOME_MOVING_AVERAGE_WINDOW_SIZE];
  // these are all over the last 3 rounds
  private static int adamantiumIncome = 0;
  private static int manaIncome = 0;
  private static int elixirIncome = 0;
  // the resources on the previous round to keep track of income
  private static int prevAdamantium = 0;
  private static int prevMana = 0;
  private static int prevElixir = 0;

  private int spawnIdx = 0;
  private SpawnType[] spawnOrder;

  MapLocation[] spawnLocations;

  private int numAdamantiumCarriers;
  private int numManaCarriers;



  private boolean foundEndangeredWells;
  private int checkedEndangeredWellsCounter;

  private int idxToWriteAt;


  public HeadQuarters(RobotController rc) throws GameActionException {
    super(rc);
    this.closestAdamantium = getClosestWell(ResourceType.ADAMANTIUM);
    this.closestMana = getClosestWell(ResourceType.MANA);
    this.closestWell = this.closestAdamantium;
    if (this.closestAdamantium == null || (this.closestMana != null && this.closestMana.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < this.closestAdamantium.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION))) {
      this.closestWell = this.closestMana;
    }
    this.hqID = Communicator.MetaInfo.registerHQ(this.closestAdamantium, this.closestMana);
    if (Cache.Permanent.MAP_AREA <= 20*20) spawnOrder = spawnOrder20x20;
    else if (Cache.Permanent.MAP_AREA <= 40*40) spawnOrder = spawnOrder40x40;
    else spawnOrder = spawnOrder60x60;

    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0) { // turn 0: only possible enemies is the enemy HQ
      spawnOrder = spawnOrderEnemyHQHere;
    }

    spawnLocations = rc.getAllLocationsWithinRadiusSquared(Cache.PerTurn.CURRENT_LOCATION, RobotType.LAUNCHER.actionRadiusSquared);;

    checkedEndangeredWellsCounter = 2;
//    if (Cache.Permanent.MAP_AREA <= 20*20) {
//      foundEndangeredWells = true;
//    }
    foundEndangeredWells = true;

    determineRole();
  }

  private void printWellInformation() {
    for (WellData wellData : wellMap.values()) {
      Printer.print(wellData.toString());
    }
  }

  @Override
  protected void runTurn() throws GameActionException {
    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM >= 1000) rc.resign();
//    if (Cache.PerTurn.ROUND_NUM >= 50) rc.resign();
    idxToWriteAt = 0;
    readAllSpawnLocations(); // read all locations that HQ spawned at (cleared by carrier on first turn)

    if (Cache.PerTurn.ROUND_NUM % 10 == 1) {
//      printWellInformation();
    }

//    if (Cache.PerTurn.ROUND_NUM >= 15) rc.resign();


    if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
      WellInfo[] wellInfos = rc.senseNearbyWells();
      for (WellInfo wellInfo : wellInfos) {
        WellData wellData = new WellData(wellInfo.getMapLocation(), wellInfo.getResourceType().resourceID, 0);
        wellMap.put(wellInfo.getMapLocation(), wellData);
        Printer.print("Found well at " + wellInfo.getMapLocation() + " with " + wellInfo.getResourceType());
      }
    }

    if (Cache.PerTurn.ROUNDS_ALIVE == 1) {
      Communicator.MetaInfo.reinitForHQ();

      updateWellExploration();
      unknown_symmetry: if (RunningMemory.knownSymmetry == null) {
//      do {
//        prev = RunningMemory.knownSymmetry;
        for (MapLocation myHQ : HqMetaInfo.hqLocations) {
          MapLocation enemyHQ;
          if (!RunningMemory.notRotationalSymmetry) {
            enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.ROTATIONAL);
            if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
              RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
              if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
                RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.ROTATIONAL);
              }
            }
          }
          if (!RunningMemory.notHorizontalSymmetry) {
            enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.HORIZONTAL);
            if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
              RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
              if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
                RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.HORIZONTAL);
              }
            }
          }
          if (!RunningMemory.notVerticalSymmetry) {
            enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.VERTICAL);
            if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
              RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
              if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
                RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.VERTICAL);
              }
            }
          }
        }
//      } while (prev != RunningMemory.knownSymmetry);
        if (RunningMemory.knownSymmetry == null) {
          updateSymmetryComms();
        }
      }
    }
//    if (Cache.PerTurn.ROUND_NUM >= 10) rc.resign();
    Communicator.clearEnemyComms();
    handleIncome();
    readWellData();

//    if (Cache.PerTurn.ROUND_NUM == ) rc.resign();

    // spawn order
    // if map size <20x20, do CM CM CM CAD L L L (technically should do L CM L CM L CM AD)
    // if map size <40x40, do CAD CM CM CM L L L
    // if map size >=40x40, do CAD CAD CM CM L L L
    // then control ratio AD:M = 1:4

    // give each HQ just spawned location + 4 bits, and then look at all and check 4 bits

    // TODO: switch to more launchers if we detect endangered wells
    if (!foundEndangeredWells) {
      if (--checkedEndangeredWellsCounter <= 0) {
//        Printer.print("HQ Checking for endangered wells");
        MapLocation[] endangeredWellPair = Communicator.closestAllyEnemyWellPair();
        MapLocation mostEndangeredWell = endangeredWellPair[0];
        MapLocation mostEndangeredEnemyWell = endangeredWellPair[1];
        if (mostEndangeredWell == null) {
          return;
        }
        int mostEndangeredDist = mostEndangeredWell.distanceSquaredTo(mostEndangeredEnemyWell);
        if (mostEndangeredDist <= Constants.ENDANGERED_WELL_DIST) {
//          Printer.print("HQ found endangered wells! " + mostEndangeredWell + " - " + mostEndangeredEnemyWell + " dist:" + mostEndangeredDist);
//          Printer.print("HQ found endangered wells -- switching to more launchers");
          foundEndangeredWells = true;
//          spawnOrder = spawnOrderEndangeredWells;
//          spawnIdx = 0;
        } else {
          checkedEndangeredWellsCounter = 1;
        }
      }
    }

    boolean spawned;
    int tries = 0;
    do {
      if (spawnIdx < spawnOrder.length) {
        spawned = forceSpawnOrder();
      } else {
        spawned = normalSpawnOrder();
      }
    } while (spawned && rc.isActionReady() && tries++ < 10);
    // store the resources at the end of the turn
    prevAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
    prevMana = rc.getResourceAmount(ResourceType.MANA);
    prevElixir = rc.getResourceAmount(ResourceType.ELIXIR);

    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM % 250 == 249) {
    /*WORKFLOW_ONLY*///  Printer.print("HQ" + Cache.PerTurn.ROUND_NUM + Cache.Permanent.OUR_TEAM + hqID + " (" + totalSpawns + ")");
    /*WORKFLOW_ONLY*///}
  }

  // very beginning, read from current round comms
  private void readAllSpawnLocations() throws GameActionException {
    if (Cache.PerTurn.ROUND_NUM % 2 == 0) {
      for (int i = 0; i < 8; ++i) {
        if (CommsHandler.readPranayOurHqEvenTargetExists(i)) {
            MapLocation loc = CommsHandler.readPranayOurHqEvenTargetLocation(i);
            int instruction = CommsHandler.readPranayOurHqEvenSpawnInstruction(i);
            if (!wellMap.containsKey(loc)) {
              wellMap.put(loc, new WellData(loc, instruction, 0));
            }
            wellMap.get(loc).numMinersExpected++;
        }
      }
    } else {
        for (int i = 0; i < 8; ++i) {
            if (CommsHandler.readPranayOurHqOddTargetExists(i)) {
                MapLocation loc = CommsHandler.readPranayOurHqOddTargetLocation(i);
                int instruction = CommsHandler.readPranayOurHqEvenSpawnInstruction(i);
                if (!wellMap.containsKey(loc)) {
                  wellMap.put(loc, new WellData(loc, instruction, 0));
                }
                wellMap.get(loc).numMinersExpected++;
            }
        }
    }
  }

  /**
   * Handles the resource income information. Does the following actions:
   * - Calculates income based on prevAdamantium, prevElixir, prevMana
   * - Updates the income histories
   * - Publishes the new income to comms
   * @throws GameActionException
   */
  private void handleIncome() throws GameActionException {

    int newAd = rc.getResourceAmount(ResourceType.ADAMANTIUM) - prevAdamantium;
    int newMana = rc.getResourceAmount(ResourceType.MANA) - prevMana;
    int newElixir = rc.getResourceAmount(ResourceType.ELIXIR) - prevElixir;

    if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
      newAd = 0;
      newMana = 0;
      newElixir = 0;
    }
    // add current income & subtract off the income from WINDOW_SIZE rounds ago
    int ind = Cache.PerTurn.ROUND_NUM % INCOME_MOVING_AVERAGE_WINDOW_SIZE;
    adamantiumIncome += newAd - adamantiumIncomeHistory[ind];
    manaIncome += newMana - manaIncomeHistory[ind];
    elixirIncome += newElixir - elixirIncomeHistory[ind];
    rc.setIndicatorString("Income: A:"+adamantiumIncome+" M:" + manaIncome + " E:" + elixirIncome);
    // update the history
    adamantiumIncomeHistory[ind] = newAd;
    manaIncomeHistory[ind] = newMana;
    elixirIncomeHistory[ind] = newElixir;

    // comm the results / 40
    int max = 31; // based on 5 bits for comms
    CommsHandler.writeOurHqAdamantiumIncome(this.hqID, Math.min((adamantiumIncome / 40), max));
    CommsHandler.writeOurHqManaIncome(this.hqID, Math.min((manaIncome / 40), max));
    CommsHandler.writeOurHqElixirIncome(this.hqID, Math.min((elixirIncome / 40), max));
    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM % 250 == 249) {
    /*WORKFLOW_ONLY*///  Printer.print("HQ" + Cache.PerTurn.ROUND_NUM + Cache.Permanent.OUR_TEAM + hqID + " (" + totalSpawns + ")");
    /*WORKFLOW_ONLY*///}
  }

  private void determineRole() throws GameActionException {
    this.role = HQRole.MAKE_CARRIERS;
    if (this.closestWell == null) {
      this.role = HQRole.MAKE_LAUNCHERS;
    }
  }

  private boolean spawnAndCommCarrier(MapLocation preferredSpawnLocation, SpawnType nextSpawn, MapLocation wellLocation) throws GameActionException {
    if (preferredSpawnLocation == null) preferredSpawnLocation = Utils.randomMapLocation();
    MapLocation targetLocation = CommsHandler.NONEXISTENT_MAP_LOC;
    if (wellLocation != null) targetLocation = wellLocation;
//    Printer.print("Spawning " + nextSpawn + " at " + preferredSpawnLocation + " towards " + wellLocation + "");
    if (idxToWriteAt >= 2) return false;
    if (spawnCarrierTowardsWell(preferredSpawnLocation)) {
      int commBits = nextSpawn.getCommBits();
      MapLocation spawnLocation = lastSpawnLoc;
//      Printer.print("Spawned " + nextSpawn + " at " + spawnLocation);
      int index = this.hqID * 2 + ++idxToWriteAt;
//      Printer.print("spawned... comm at " + idxToWriteAt);
      if (nextSpawn == SpawnType.CARRIER_MANA) ++numManaCarriers;
      if (nextSpawn == SpawnType.CARRIER_ADAMANTIUM) ++numAdamantiumCarriers;
      if (Cache.PerTurn.ROUND_NUM % 2 == 0) {
          assert idxToWriteAt < 4;
//          Printer.print("O Writing carrier spawn at " + index + " at " + spawnLocation + " with comm bits " + commBits + " and target " + targetLocation);
          CommsHandler.writePranayOurHqOddSpawnLocation(index, spawnLocation);
          CommsHandler.writePranayOurHqOddSpawnInstruction(index, commBits);
          CommsHandler.writePranayOurHqOddTargetLocation(index, targetLocation);
      } else {
        assert idxToWriteAt < 4;
//        Printer.print("E Writing carrier spawn at " + index + " at " + spawnLocation + " with comm bits " + commBits + " and target " + targetLocation);
        CommsHandler.writePranayOurHqEvenSpawnLocation(index, spawnLocation);
        CommsHandler.writePranayOurHqEvenSpawnInstruction(index, commBits);
        CommsHandler.writePranayOurHqEvenTargetLocation(index, targetLocation);
      }
      return true;
    }
    return false;
  }

  class WellData {
    public MapLocation location;
    public ResourceType type;
    public int numMinersObserved; // observed is lagged by ~50 turns and will generally be less than expected
    public int numMinersExpected;

    public WellData(MapLocation location, int type, int numMiners) {
      this.location = location;
      switch(type) {
        case 1:
          this.type = ResourceType.ADAMANTIUM;
          break;
        case 2:
          this.type = ResourceType.MANA;
          break;
      }
      this.numMinersObserved = numMiners;
    }

    @Override
    public String toString() {
      return "WellData{" +
              "location=" + location +
              ", type=" + type +
              ", numMinersObserved=" + numMinersObserved +
              ", numMinersExpected=" + numMinersExpected +
              '}';
    }
  }

//  class WellDataSpawned {
//    public MapLocation location;
//    public ResourceType type;
//    public int numMinersSpawned;
//  }

  HashMap<MapLocation, WellData> wellMap = new HashMap<>();
  private void readWellData() throws GameActionException {
    for (int i = 0; i < 4; ++i) {
      if (CommsHandler.readPranayWellInfoExists(i)) {
        MapLocation wellLocation = CommsHandler.readPranayWellInfoLocation(i);
        int wellType = CommsHandler.readPranayWellInfoType(i);
        int wellMiners = CommsHandler.readPranayWellInfoNumMiners(i);
        if (wellMap.containsKey(wellLocation)) {
          WellData wellData = wellMap.get(wellLocation);
          if (wellMiners != 0) {
            wellData.numMinersObserved = wellMiners;
            if (wellData.numMinersObserved > wellData.numMinersExpected) {
              wellData.numMinersExpected = wellData.numMinersObserved;
            }
            if (wellData.numMinersExpected >= 6 && wellData.numMinersObserved * 2 < wellData.numMinersExpected) {
              wellData.numMinersExpected--;
            }
          }
//          Printer.print("seen Well at " + wellLocation + " has " + wellData.numMinersObserved + " observed miners and " + wellData.numMinersExpected + " expected miners");
        } else {
          wellMap.put(wellLocation, new WellData(wellLocation, wellType, wellMiners));
          WellData wellData = wellMap.get(wellLocation);
//          Printer.print("new Well at " + wellLocation + " has " + wellData.numMinersObserved + " observed miners and " + wellData.numMinersExpected + " expected miners");
        }

        if (this.hqID + 1 == HqMetaInfo.hqCount) {
          CommsHandler.writePranayWellInfoLocation(i, CommsHandler.NONEXISTENT_MAP_LOC);
          CommsHandler.writePranayWellInfoNumMiners(i, 0);
        }
      }
    }
  }

  class BestSpawn {
    MapLocation bestADSpawn;
    MapLocation bestManaSpawn;
  }

  private BestSpawn getBestWellLocation() throws GameActionException {
    MapLocation bestSpawnForAD = null;
    int bestDistForAD = Integer.MAX_VALUE;
    MapLocation bestSpawnForMana = null;
    int bestDistForMana = Integer.MAX_VALUE;
    if (Cache.PerTurn.ROUND_NUM <= 50) {
      for (MapLocation wellLocation : wellMap.keySet()) {
        WellData wellData = wellMap.get(wellLocation);
        if (wellData.numMinersObserved >= 7 || wellData.numMinersExpected >= 7) continue; // too full
        int candidateDist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation);
        if (wellData.type == ResourceType.ADAMANTIUM) {
          if (candidateDist < bestDistForAD && candidateDist <= 100) {
            bestDistForAD = candidateDist;
            bestSpawnForAD = wellLocation;
          }
        } else if (wellData.type == ResourceType.MANA) {
          if (candidateDist < bestDistForMana && candidateDist <= 250) {
            bestDistForMana = candidateDist;
            bestSpawnForMana = wellLocation;
          }
        }
      }
    } else {
      for (MapLocation wellLocation : wellMap.keySet()) {
        WellData wellData = wellMap.get(wellLocation);
        if ((wellData.numMinersObserved + wellData.numMinersExpected)/2 >= 7) continue; // too full
        int candidateDist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation);
        if (wellData.type == ResourceType.ADAMANTIUM) {
          if (candidateDist < bestDistForAD) {
            bestDistForAD = candidateDist;
            bestSpawnForAD = wellLocation;
          }
        } else if (wellData.type == ResourceType.MANA) {
          if (candidateDist < bestDistForMana) {
            bestDistForMana = candidateDist;
            bestSpawnForMana = wellLocation;
          }
        }
      }
    }
    BestSpawn bestSpawn = new BestSpawn();
    bestSpawn.bestADSpawn = bestSpawnForAD;
    bestSpawn.bestManaSpawn = bestSpawnForMana;
//    Printer.print("Best AD spawn: " + bestSpawnForAD + " dist: " + bestDistForAD);
//    Printer.print("Best Mana spawn: " + bestSpawnForMana + " dist: " + bestDistForMana);
    return bestSpawn;
  }

  private MapLocation getPreferredCarrierSpawnLocation(SpawnType nextSpawn) throws GameActionException {
    BestSpawn bestSpawns = getBestWellLocation();
    MapLocation closestWellLocation = null;
    if (nextSpawn == SpawnType.CARRIER_ADAMANTIUM) {
      closestWellLocation = bestSpawns.bestADSpawn;
    } else if (nextSpawn == SpawnType.CARRIER_MANA) {
      closestWellLocation = bestSpawns.bestManaSpawn;
    }
    return closestWellLocation;
  }

  /**
   * spawns a unit based on resources we have
   * @return true if spawned
   * @throws GameActionException any issues
   */
  private boolean normalSpawnOrder() throws GameActionException {
    if (spawnAmplifierCooldown > 0) --spawnAmplifierCooldown;
//    spawnAmplifier = false;
    if (spawnAmplifier) {
      if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0 && Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS[0].type != RobotType.HEADQUARTERS) {
        spawnAmplifier = false;
        spawnAmplifierCooldown = 50;
      } else {
        spawnAmplifierSafe++;
      }
    }

    if (Cache.PerTurn.ROUND_NUM >= 60 && spawnAmplifierCooldown <= 0) {
      if (Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length >= 8) {
        if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length == 0 || (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length == 1 && Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS[0].type == RobotType.HEADQUARTERS)) {
          spawnAmplifier = true;
          spawnAmplifierSafe = 0;
        }
      }
    }

    if (spawnAmplifier && spawnAmplifierSafe >= 5) {
      if (canAfford(RobotType.AMPLIFIER)) {
        if (spawnAmplifierTowardsEnemyHQ()) {
          spawnAmplifier = false;
          spawnAmplifierCooldown = 200;
          spawnAmplifierSafe = 0;
          return true;
        }
      }
      return false;
    }

    if (Cache.PerTurn.ROUND_NUM >= 500 && numAnchorsMade <= NUM_FORCED_LATE_GAME_ANCHORS) {
      // consider anchor spawn
      rc.setIndicatorString("Build anchor");
      if (createAnchors()) {
        numAnchorsMade++;
        return true;
      }
      return false;
    }

    if (canAfford(RobotType.LAUNCHER)) {
      if (spawnLauncherTowardsEnemyHQ()) {
        spawnAmplifierCooldown -= 2;
        return true;
      }
    }
    if (canAfford(RobotType.CARRIER)) {
      SpawnType nextSpawn = carrierSpawnOrder[carrierSpawnOrderIdx];
      if (numManaCarriers < numAdamantiumCarriers + 2) {
        nextSpawn = SpawnType.CARRIER_MANA;
      } else {
        nextSpawn = SpawnType.CARRIER_ADAMANTIUM;
      }
      MapLocation wellLocation = getPreferredCarrierSpawnLocation(nextSpawn);
      MapLocation preferredSpawnLocation = getPreferredCarrierSpawnLocation(nextSpawn);
      if (spawnAndCommCarrier(preferredSpawnLocation, nextSpawn, wellLocation)) {
        carrierSpawnOrderIdx = (carrierSpawnOrderIdx + 1) % carrierSpawnOrder.length;
        if (spawnAmplifierCooldown > 0) spawnAmplifierCooldown -= 2;
        return true;
      }
    }
    return false;
  }


  private SpawnType assignRandomTask() {
    double pAD = Cache.Permanent.MAP_AREA / (60.0*60.0) * Math.exp(-0.01*Cache.PerTurn.ROUND_NUM);
    if (Cache.PerTurn.ROUND_NUM >= 10) {
      pAD = Math.max(0.75, pAD);
      pAD = Math.min(0.25, pAD);
    }

    if (Utils.rng.nextDouble() < pAD) {
      return SpawnType.CARRIER_ADAMANTIUM;
    } else {
      return SpawnType.CARRIER_MANA;
    }
  }


  /**
   * spawns a unit according to the initial spawn order
   * @return true if spawned
   * @throws GameActionException if something goes wrong
   */
  private boolean forceSpawnOrder() throws GameActionException {
    if (!rc.isActionReady()) return false;
    SpawnType nextSpawn = spawnOrder[spawnIdx];
    switch (nextSpawn) {
      case CARRIER_MANA:
      case CARRIER_ADAMANTIUM:
        BestSpawn bestSpawn = getBestWellLocation();
        SpawnType spawnType = assignRandomTask();
        if (bestSpawn.bestADSpawn != null && bestSpawn.bestManaSpawn == null) {
          spawnType = SpawnType.CARRIER_ADAMANTIUM;
        } else if (bestSpawn.bestADSpawn == null && bestSpawn.bestManaSpawn != null) {
          spawnType = SpawnType.CARRIER_MANA;
        } else {
          if (numManaCarriers < 2) {
            spawnType = SpawnType.CARRIER_MANA;
          } else if (numAdamantiumCarriers < 2) {
            spawnType = SpawnType.CARRIER_ADAMANTIUM;
          }
        }
        if (spawnType == SpawnType.CARRIER_ADAMANTIUM) {
          if (spawnAndCommCarrier(bestSpawn.bestADSpawn, SpawnType.CARRIER_ADAMANTIUM, bestSpawn.bestADSpawn)) {
            spawnIdx++;
            return true;
          }
        } else if (spawnType == SpawnType.CARRIER_MANA) {
          if (spawnAndCommCarrier(bestSpawn.bestManaSpawn, SpawnType.CARRIER_MANA, bestSpawn.bestManaSpawn)) {
            spawnIdx++;
            return true;
          }
        }
        break;
      case LAUNCHER:
        if (canAfford(RobotType.LAUNCHER)) {
          if (spawnLauncherTowardsEnemyHQ()) {
            int commBits = nextSpawn.getCommBits();
            MapLocation spawnLocation = lastSpawnLoc;
            //todo: do comms
            ++spawnIdx;
            return true;
          }
        }
        break;
    }
    return false;
  }

  private boolean spawnCarrierTowardsWell(WellInfo targetWell) throws GameActionException {
    rc.setIndicatorString("closest well: " + targetWell);
    if (targetWell == null) return false;
    if (!rc.isActionReady()) return false;
    if (!canAfford(RobotType.CARRIER)) return false;

    return spawnCarrierTowardsWell(targetWell.getMapLocation());
  }

  private boolean spawnCarrierTowardsWell(MapLocation targetWell) throws GameActionException {
    if (rc.senseNearbyRobots(targetWell, RobotType.CARRIER.actionRadiusSquared, Cache.Permanent.OUR_TEAM).length > 12) return false;

    Direction dirToWell = Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell);
    MapLocation goal = Cache.PerTurn.CURRENT_LOCATION.translate(dirToWell.dx * 4, dirToWell.dy * 4);
    MapLocation toSpawn = goal;
    while (!buildRobotAtOrAround(RobotType.CARRIER, toSpawn) && toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 2) {
//      rc.setIndicatorString("Attempted spawn at " + toSpawn);
      if (toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) <= 2) {
        Direction dir = Utils.randomDirection();
        goal = goal.add(dir).add(dir);
        toSpawn = goal;
      }
      Direction toSelf = toSpawn.directionTo(Cache.PerTurn.CURRENT_LOCATION);
      toSpawn = toSpawn.add(Utils.randomSimilarDirectionPrefer(toSelf));
    }
    return true;
  }

  private boolean spawnLauncherTowardsEnemyHQ() throws GameActionException {
    MapLocation goal = Utils.applySymmetry(Cache.PerTurn.CURRENT_LOCATION, Utils.MapSymmetry.ROTATIONAL);
    MapLocation toSpawn = goal;
//    Printer.print("Spawning launcher towards enemy HQ - " + goal);

    MapLocation bestSpawnLocation = null;
    int bestMoveDistance = Integer.MAX_VALUE;
    int bestEucDistance = Integer.MAX_VALUE;
    for (MapLocation spawnLocation : spawnLocations) {
      if (rc.canBuildRobot(RobotType.LAUNCHER, spawnLocation)) {
        int moveDistance = Utils.maxSingleAxisDist(spawnLocation, goal);
        int euclideanDistance = spawnLocation.distanceSquaredTo(goal);
        if (moveDistance < bestMoveDistance || (moveDistance == bestMoveDistance && euclideanDistance < bestEucDistance)) {
          bestSpawnLocation = spawnLocation;
          bestMoveDistance = moveDistance;
          bestEucDistance = euclideanDistance;
        }
      }
    }

    if (bestSpawnLocation != null) {
      if (buildRobot(RobotType.LAUNCHER, bestSpawnLocation)) {
        return true;
      }
    }
    return false;


//    while (!buildRobot(RobotType.LAUNCHER, toSpawn) && toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 2) {
//      if (toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) <= 2) {
//        Direction dir = Utils.randomDirection();
//        goal = goal.add(dir).add(dir);
//        toSpawn = goal;
//      }
//      Direction toSelf = toSpawn.directionTo(Cache.PerTurn.CURRENT_LOCATION);
//      toSpawn = toSpawn.add(Utils.randomSimilarDirectionPrefer(toSelf));
//    }
//    return true;
  }

  private boolean spawnAmplifierTowardsEnemyHQ() throws GameActionException {
    MapLocation goal = Utils.applySymmetry(Cache.PerTurn.CURRENT_LOCATION, Utils.MapSymmetry.ROTATIONAL);
    MapLocation toSpawn = goal;
    while (!buildRobot(RobotType.AMPLIFIER, toSpawn) && toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 2) {
      if (toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) <= 2) {
        Direction dir = Utils.randomDirection();
        goal = goal.add(dir).add(dir);
        toSpawn = goal;
      }
      Direction toSelf = toSpawn.directionTo(Cache.PerTurn.CURRENT_LOCATION);
      toSpawn = toSpawn.add(Utils.randomSimilarDirectionPrefer(toSelf));
    }
    return true;
  }

  private boolean createAnchors() throws GameActionException {
    return buildAnchor(Anchor.ACCELERATING) || buildAnchor(Anchor.STANDARD);
  }

  private void determineTargetWell() throws GameActionException {
    ResourceType resourceType = role.getResourceType();
    CommsHandler.ResourceTypeReaderWriter resourceTypeReaderWriter = CommsHandler.ResourceTypeReaderWriter.fromResourceType(resourceType);
    int hqWithClosestWell = 0;
    boolean closestUpgraded = false;
    int closestWellDistance = Integer.MAX_VALUE;
    MapLocation closestWellLocation = null;
    MapLocation tmpLocation;
    for (int i = 0; i < 4; i++) {
      if (!resourceTypeReaderWriter.readWellExists(i)) continue;

      tmpLocation = resourceTypeReaderWriter.readWellLocation(i);
      int dist = tmpLocation.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
      if (dist >= closestWellDistance) continue;

      closestWellDistance = dist;
      hqWithClosestWell = i;
      closestWellLocation = tmpLocation;
      closestUpgraded = resourceTypeReaderWriter.readWellUpgraded(i);
    }
    if (closestWellLocation == null) {
      targetWell = null;
    } else {
      targetWell = closestWellLocation;
      targetWellUpgraded = closestUpgraded;
    }
  }

  private boolean canAfford(RobotType type) {
    for (ResourceType rType : ResourceType.values()) {
      if (rType == ResourceType.NO_RESOURCE)
        continue;
      if (rc.getResourceAmount(rType) < type.getBuildCost(rType)) {
        return false;
      }
    }
    return true;
  }

  private boolean canAfford(Anchor anchorType) {
    for (ResourceType rType : ResourceType.values()) {
      if (rType == ResourceType.NO_RESOURCE)
        continue;
      if (rc.getResourceAmount(rType) < anchorType.getBuildCost(rType)) {
        return false;
      }
    }
    return true;
  }

  /**
   * build the specified robot type at the specified location
   * @param type the robot type to build
   * @param location where to build it
   * @return method success
   * @throws GameActionException when building fails
   */
  protected boolean buildRobot(RobotType type, MapLocation location) throws GameActionException {
    if (rc.canBuildRobot(type, location)) {
      rc.buildRobot(type, location);
      lastSpawnLoc = location;
      /*WORKFLOW_ONLY*///totalSpawns++;
      return true;
    }
    return false;
  }
  protected boolean buildRobotAtOrAround(RobotType type, MapLocation center) throws GameActionException {
    return buildRobot(type, center)
        || buildRobot(type, center.add(Direction.NORTH))
        || buildRobot(type, center.add(Direction.EAST))
        || buildRobot(type, center.add(Direction.SOUTH))
        || buildRobot(type, center.add(Direction.WEST))
        || buildRobot(type, center.add(Direction.NORTHEAST))
        || buildRobot(type, center.add(Direction.SOUTHEAST))
        || buildRobot(type, center.add(Direction.SOUTHWEST))
        || buildRobot(type, center.add(Direction.NORTHWEST));
  }

  protected boolean buildAnchor(Anchor type) throws GameActionException {
    if (rc.canBuildAnchor(type)) {
      rc.buildAnchor(type);
      return true;
    }
    return false;
  }

  private enum HQRole {
    MAKE_CARRIERS,
    MAKE_LAUNCHERS,
    BUILD_ANCHORS;

    public ResourceType getResourceType() {
      switch (this) {
        case MAKE_CARRIERS: return ResourceType.ADAMANTIUM;
        case MAKE_LAUNCHERS: return ResourceType.MANA;
        case BUILD_ANCHORS: return ResourceType.ELIXIR;
      }
      throw new RuntimeException("unknown role: " + this);
    }
  }

  private enum SpawnType {
    CARRIER_MANA,
    CARRIER_ADAMANTIUM,
    LAUNCHER;

    public ResourceType getResourceType() {
        switch (this) {
            case CARRIER_MANA: return ResourceType.MANA;
            case CARRIER_ADAMANTIUM: return ResourceType.ADAMANTIUM;
            case LAUNCHER: return ResourceType.NO_RESOURCE;
        }
        throw new RuntimeException("unknown spawn type: " + this);
    }

    public int getCommBits() {
        switch (this) {
            case CARRIER_ADAMANTIUM: return 1;
            case CARRIER_MANA: return 2;
            case LAUNCHER: return 3;
        }
        throw new RuntimeException("unknown spawn type: " + this);
    }
  }

  private void oldSpawnCode() throws GameActionException {
    if (Cache.PerTurn.ROUND_NUM >= 100 && Cache.PerTurn.ROUND_NUM % 200 <= 20) {
      this.role = HQRole.BUILD_ANCHORS;
    }
    if (Cache.PerTurn.ROUND_NUM >= 1000 && numAnchorsMade <= NUM_FORCED_LATE_GAME_ANCHORS) {
      rc.setIndicatorString("Build anchor");
      if (createAnchors()) {
        numAnchorsMade++;
      }
      return;
    }
    make_carriers: if (this.role == HQRole.MAKE_CARRIERS || canAfford(RobotType.CARRIER)) {
      if (Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length >= 10) {
        int emptyCarrierCount = 0;
        for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
          if (robot.type == RobotType.CARRIER && getInvWeight(robot) == 0) {
            emptyCarrierCount++;
          }
        }
        if (emptyCarrierCount >= 15) {
          break make_carriers;
        }
      }
      if (this.closestWell != null) {
        rc.setIndicatorString("Spawn towards closest: " + this.closestWell.getMapLocation());
        if (spawnCarrierTowardsWell(this.closestWell)) {
//          testCount++;
//          if (testCount >= 10) {
//            becomeDoNothingBot();
//          }
        }
      } else if (this.targetWell != null) {
//        rc.disintegrate();
        rc.setIndicatorString("Spawn towards target: " + this.targetWell);
        spawnCarrierTowardsWell(this.targetWell);
      } else {
//        rc.disintegrate();
        rc.setIndicatorString("Find target well");
        determineTargetWell();
      }
    }
    if (this.role == HQRole.MAKE_LAUNCHERS || canAfford(RobotType.LAUNCHER)) {
      rc.setIndicatorString("Spawn towards enemy");
      spawnLauncherTowardsEnemyHQ();
    }
    if (this.role == HQRole.BUILD_ANCHORS || canAfford(Anchor.ACCELERATING) || canAfford(Anchor.STANDARD)) {
      rc.setIndicatorString("Build anchor");
      if (createAnchors()) {
        numAnchorsMade++;
        determineRole();
      }
    }
  }
}
