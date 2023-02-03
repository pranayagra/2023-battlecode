package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.containers.CharSet;
import basicbot.knowledge.Cache;
import basicbot.knowledge.RunningMemory;
import basicbot.utils.Constants;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

/*WORKFLOW_ONLY*///import basicbot.utils.Printer;
public class HeadQuarters extends Robot {
  /*WORKFLOW_ONLY*///private int totalSpawns = 0;
  private static int NUM_FORCED_LATE_GAME_ANCHORS = 3;
  private static final int INCOME_MOVING_AVERAGE_WINDOW_SIZE = 100;

  private int hqID;
//  public final WellInfo closestAdamantium;
//  public final WellInfo closestMana;

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
  private final SpawnType[] carrierSpawnOrder = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};

  private static final SpawnType[] spawnOrderEnemyHQHere = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};
  private static final SpawnType[] spawnOrder20x20 = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_ADAMANTIUM};
  private static final SpawnType[] spawnOrder40x40 = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};
  private static final SpawnType[] spawnOrder60x60 = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};
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

  // budgeting for anchor forcing
  private static int adamantiumToSave;
  private static int manaToSave;

  private int spawnIdx = 0;
  private SpawnType[] spawnOrder;

  MapLocation[] spawnLocations;

  private boolean foundEndangeredWells;
  private int checkedEndangeredWellsCounter;


  public HeadQuarters(RobotController rc) throws GameActionException {
    super(rc);
//    this.closestAdamantium = getClosestWell(ResourceType.ADAMANTIUM);
//    this.closestMana = getClosestWell(ResourceType.MANA);
//    this.closestWell = this.closestAdamantium;
//    if (this.closestAdamantium == null || (this.closestMana != null && this.closestMana.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < this.closestAdamantium.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION))) {
//      this.closestWell = this.closestMana;
//    }
//    this.hqID = Communicator.MetaInfo.registerHQ(this.closestAdamantium, this.closestMana);
    this.hqID = Communicator.MetaInfo.registerHQ();

    if (Cache.Permanent.MAP_AREA <= 20*20) spawnOrder = spawnOrder20x20;
    else if (Cache.Permanent.MAP_AREA <= 40*40) spawnOrder = spawnOrder40x40;
    else spawnOrder = spawnOrder60x60;

    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0) { // turn 0: only possible enemies is the enemy HQ
      spawnOrder = spawnOrderEnemyHQHere;
    }

    spawnLocations = rc.getAllLocationsWithinRadiusSquared(Cache.PerTurn.CURRENT_LOCATION, RobotType.LAUNCHER.actionRadiusSquared);

    checkedEndangeredWellsCounter = 2;
//    if (Cache.Permanent.MAP_AREA <= 20*20) {
//      foundEndangeredWells = true;
//    }
    foundEndangeredWells = true;

    determineRole();
  }

  private void islandHqProtocol() throws GameActionException {
    updateIslandInfoMemoryFromComms();
    clearIslandInfo();
    rotateMyOwnedIslands();
  }

  private void clearIslandInfo() throws GameActionException {
    if (this.hqID + 1 == HqMetaInfo.hqCount && Cache.PerTurn.ROUND_NUM % 2 == 1) {
      CommsHandler.writeIslandInfoLocation(CommsHandler.NONEXISTENT_MAP_LOC);
    }
  }

  protected int globalIslandInfoIterator = 0;
  private void rotateMyOwnedIslands() throws GameActionException {
    if (this.hqID + 1 == HqMetaInfo.hqCount) {
      int tries = IslandInfo.MAX_ISLAND_COUNT;
      while (tries-- > 0) {
        IslandInfo islandInfo = globalIslandInfo[globalIslandInfoIterator];
        globalIslandInfoIterator = (globalIslandInfoIterator + 1) % IslandInfo.MAX_ISLAND_COUNT;
        if (islandInfo != null) {
          if (islandInfo.islandTeam == Cache.Permanent.OUR_TEAM) {
            CommsHandler.writeMyIslandsLocation(islandInfo.islandLocation);
            CommsHandler.writeMyIslandsRoundNum(islandInfo.roundNum);
            CommsHandler.writeMyIslandsIslandId(islandInfo.islandId);
            //Printer.appendToIndicator("writing island " + islandInfo.islandLocation);
//            Printer.print("rotating island " + islandInfo);
//            CommsHandler.writeMyIslandsTeam(teamToInt(islandInfo.islandTeam)); // not needed since we only rotate owned team islands
            return;
          }
        }
      }
    }
  }

  @Override
  protected void runTurn() throws GameActionException {
    // printDebugInfo();
//    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM >= 1000) rc.resign();
//     if (Cache.PerTurn.ROUND_NUM >= 300) rc.resign();
    if (Cache.PerTurn.ROUNDS_ALIVE == 1) {
      Communicator.MetaInfo.reinitForHQ();
      afterTurnWhenMoved();
    }

    if (this.hqID + 1 == HqMetaInfo.hqCount) {
//      printDebugInfo();
      CommsHandler.writeNumLaunchersReset();
      CommsHandler.writeNumAmpsReset();
    }

    Communicator.clearEnemyComms();
    handleIncome();

    setDefaultIndicatorString();

    islandHqProtocol();


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

    boolean baseOverWhelmed = baseOverWhelmed();
    if (!baseOverWhelmed) {
      int neededLauncherBombSize = neededLauncherBombSize();
      if (neededLauncherBombSize > 0) {
        //Printer.appendToIndicator("need bomb=" + neededLauncherBombSize + "-mana:" + rc.getResourceAmount(ResourceType.MANA) + "-cost:" + RobotType.LAUNCHER.buildCostMana * neededLauncherBombSize);
        boolean canLauncherBomb = rc.getResourceAmount(ResourceType.MANA) >= RobotType.LAUNCHER.buildCostMana * neededLauncherBombSize;
        if (canLauncherBomb) {
          spawnLauncherBomb(Math.min(neededLauncherBombSize, 5));
        }
      } else {
        boolean spawned;
        do {
          if (spawnIdx < spawnOrder.length) {
            spawned = forceSpawnOrder();
          } else {
            spawned = normalSpawnOrder();
          }
        } while (spawned && rc.isActionReady());
      }
//    } else {
//      Printer.appendToIndicator("base overwhelmed");
    }
    // store the resources at the end of the turn
    prevAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
    prevMana = rc.getResourceAmount(ResourceType.MANA);
    prevElixir = rc.getResourceAmount(ResourceType.ELIXIR);

    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM % 250 == 249) {
    /*WORKFLOW_ONLY*///  Printer.print("HQ" + Cache.PerTurn.ROUND_NUM + Cache.Permanent.OUR_TEAM + hqID + " (" + totalSpawns + ")");
    /*WORKFLOW_ONLY*///}
  }

  private void spawnLauncherBomb(int bombSize) throws GameActionException {
    int enemyX = 0;
    int enemyY = 0;
    int enemyCount = 0;
    for (RobotInfo enemy : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      switch (enemy.type) {
        case LAUNCHER:
        case DESTABILIZER:
        case BOOSTER:
          enemyX += enemy.location.x;
          enemyY += enemy.location.y;
          enemyCount++;
      }
    }
    MapLocation enemyCentroid;
    if (enemyCount > 0) {
      enemyX /= enemyCount;
      enemyY /= enemyCount;
      enemyCentroid = new MapLocation(enemyX, enemyY);
      Direction toSelf = enemyCentroid.directionTo(Cache.PerTurn.CURRENT_LOCATION);
      enemyCentroid = enemyCentroid.translate(4 * toSelf.dx, 4 * toSelf.dy);
    } else {
      enemyCentroid = Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
      if (enemyCentroid == null) {
        enemyCentroid = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        if (enemyCentroid.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, 70)) {
          Direction toSelf = enemyCentroid.directionTo(Cache.PerTurn.CURRENT_LOCATION);
          enemyCentroid = enemyCentroid.translate(4 * toSelf.dx, 4 * toSelf.dy);
        }
      } else {
        Direction toSelf = enemyCentroid.directionTo(Cache.PerTurn.CURRENT_LOCATION);
        enemyCentroid = enemyCentroid.translate(4 * toSelf.dx, 4 * toSelf.dy);
      }
    }
    while (bombSize > 0 && Clock.getBytecodesLeft() > spawnLocations.length * 20) {
      if (spawnLauncherTowardsLocation(enemyCentroid)) {
        bombSize--;
      }
    }
    if (bombSize > 0) {
      for (int i = spawnLocations.length; --i >= 0;) {
        MapLocation spawnLocation = spawnLocations[i];
        if (buildRobot(RobotType.LAUNCHER, spawnLocation)) {
          if (--bombSize == 0) {
            return;
          }
        }
      }
    }
  }

  /**
   * determines if we need a launcher bomb
   * @return the size of the bomb needed
   */
  private int neededLauncherBombSize() {
    int enemyOffensiveCount = 0;
    for (RobotInfo enemy : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      switch (enemy.type) {
        case LAUNCHER:
        case DESTABILIZER:
        case BOOSTER:
          enemyOffensiveCount++;
          break;
      }
    }
    int friendlyOffensiveCount = 0;
    for (RobotInfo friendly : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      switch (friendly.type) {
        case LAUNCHER:
        case DESTABILIZER:
        case BOOSTER:
          friendlyOffensiveCount++;
          break;
      }
    }
    int deficit = enemyOffensiveCount - friendlyOffensiveCount;
    if (deficit < 5) {
      return 0;
    }
    return (int) (deficit * 0.75);
  }

  private boolean baseOverWhelmed() {
    return (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length - Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length) > 30;
  }

  private void setDefaultIndicatorString() throws GameActionException {
//    String indString = "Inc-A:"+CommsHandler.readOurHqAdamantiumIncome(this.hqID)+" M:" + CommsHandler.readOurHqManaIncome(this.hqID) + " E:" + CommsHandler.readOurHqElixirIncome(this.hqID);
//    indString += ";kSym:" + RunningMemory.knownSymmetry + ";gSym:" + RunningMemory.guessedSymmetry;
//    Printer.appendToIndicator(indString);
  }

  private void printDebugInfo() throws GameActionException {
    if (Cache.PerTurn.ROUND_NUM % 10 != 0) return;
    ResourceType[] resourceTypes = new ResourceType[]{ResourceType.MANA, ResourceType.ADAMANTIUM};
    for (ResourceType type : resourceTypes) {
      CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(type);
      for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
        if (!writer.readWellExists(i)) continue;
        MapLocation loc = writer.readWellLocation(i);
        int capacity = writer.readWellCapacity(i);
        int extraCapacity = Utils.maxCarriersPerWell(capacity, Utils.maxSingleAxisDist(HqMetaInfo.getClosestHqLocation(loc), loc));
        int currentWorkers = writer.readWellCurrentWorkers(i);
        //Printer.print("Well:" + loc + " " + currentWorkers +"/" + capacity + "/" + extraCapacity);
      }
    }
    //Printer.print("Num Amps:" + CommsHandler.readNumAmps());
    //Printer.print("Num launchers:" + CommsHandler.readNumLaunchers());
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
//    Printer.appendToIndicator("Income: A:"+adamantiumIncome+" M:" + manaIncome + " E:" + elixirIncome);
    // update the history
    adamantiumIncomeHistory[ind] = newAd;
    manaIncomeHistory[ind] = newMana;
    elixirIncomeHistory[ind] = newElixir;



    // comm the results / 40
    int max = 31; // based on 5 bits for comms
    // disabled currently - allow carriers to write and subtract. Altho no way to handle dead carriers...
//    CommsHandler.writeOurHqAdamantiumIncome(this.hqID, Math.min((adamantiumIncome / 40), max));
//    CommsHandler.writeOurHqManaIncome(this.hqID, Math.min((manaIncome / 40), max));
//    CommsHandler.writeOurHqElixirIncome(this.hqID, Math.min((elixirIncome / 40), max));
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

  private boolean spawnAndCommCarrier(MapLocation preferredSpawnLocation, SpawnType nextSpawn) throws GameActionException {
    if (spawnCarrierTowardsWell(preferredSpawnLocation)) {
      int commBits = nextSpawn.getCommBits();
      MapLocation spawnLocation = lastSpawnLoc;
//      Printer.print("Spawned " + nextSpawn + " at " + spawnLocation);
      if (Cache.PerTurn.ROUND_NUM % 2 == 0) {
        CommsHandler.writeOurHqOddSpawnLocation(this.hqID, spawnLocation);
        CommsHandler.writeOurHqOddSpawnInstruction(this.hqID, commBits);
      } else {
        CommsHandler.writeOurHqEvenSpawnLocation(this.hqID, spawnLocation);
        CommsHandler.writeOurHqEvenSpawnInstruction(this.hqID, commBits);
      }
      return true;
    }
    return false;
  }

  private int randomCarrierCount = 0;
  private MapLocation getPreferredCarrierSpawnLocation(SpawnType nextSpawn) throws GameActionException {
    MapLocation closestWellLocation = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, nextSpawn.getResourceType());
    if (closestWellLocation != null) {
      return closestWellLocation;
    }
    Direction dir = Utils.directions[randomCarrierCount++*3 % 8];
    return Cache.PerTurn.CURRENT_LOCATION.translate(dir.dx * 8, dir.dy * 8);
  }

  /**
   * spawns a unit based on resources we have
   * @return true if spawned
   * @throws GameActionException any issues
   */
  private boolean normalSpawnOrder() throws GameActionException {
    boolean spawned = false;


//    if (spawnAmplifierCooldown > 0) --spawnAmplifierCooldown;

    adamantiumToSave = 0;
    manaToSave = 0;
    if (Cache.PerTurn.ROUND_NUM > 1500 && Cache.PerTurn.ROUND_NUM % 50 == 0) NUM_FORCED_LATE_GAME_ANCHORS++;
    if (Cache.PerTurn.ROUND_NUM >= 400 && numAnchorsMade <= NUM_FORCED_LATE_GAME_ANCHORS) {
      // consider anchor spawn
      if (createAnchors()) {
        numAnchorsMade++;
        spawned = true;
      }
      // can't afford, just reserve some resources
//      System.out.println(Anchor.STANDARD.adamantiumCost);
      adamantiumToSave = Anchor.STANDARD.adamantiumCost;
      manaToSave = Anchor.STANDARD.manaCost;
    }

//    spawnAmplifier = false;

    if (Cache.PerTurn.ROUND_NUM >= (hqID + 1) * 30 && spawnAmplifierCooldown <= 0) {
      spawnAmplifier = true;
      if (true || Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length >= 4) {
        int numAttackingEnemies = 0;
        for (RobotInfo ri : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
          switch (ri.type) {
            case LAUNCHER:
            case DESTABILIZER:
              numAttackingEnemies++;
              break;
            default:
              continue;
          }
        }
        if (numAttackingEnemies > 0) {
          spawnAmplifierSafe = 0;
        } else {
          spawnAmplifierSafe++;
        }
      }
    }

    if (spawnAmplifier && spawnAmplifierSafe >= 5) {
      if (canAfford(RobotType.AMPLIFIER)) {
        if (spawnAmplifierTowardsEnemyHQ()) {
          spawnAmplifier = false;
          spawnAmplifierCooldown = 40;
          spawnAmplifierSafe = 0;
          spawned = true;
        }
      }
    }



    if (canAfford(RobotType.LAUNCHER)) {
      if (spawnLauncherTowardsEnemyHQ()) {
        spawnAmplifierCooldown -= 2;
        spawned = true;
      }
    }

    if (canAfford(RobotType.CARRIER)) {
      SpawnType nextSpawn = carrierSpawnOrder[carrierSpawnOrderIdx];
      MapLocation preferredSpawnLocation = getPreferredCarrierSpawnLocation(nextSpawn);
      if (spawnAndCommCarrier(preferredSpawnLocation, nextSpawn)) {
        carrierSpawnOrderIdx = (carrierSpawnOrderIdx + 1) % carrierSpawnOrder.length;
        spawnAmplifierCooldown -= 2;
        spawned = true;
      }
    }

    if (canAfford(Anchor.ACCELERATING) || canAfford(Anchor.STANDARD)) {
      if (createAnchors()) {
        numAnchorsMade++;
        spawned = true;
      }
    }
    return spawned;
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
        MapLocation preferredSpawnLocation = getPreferredCarrierSpawnLocation(nextSpawn);
        if (spawnAndCommCarrier(preferredSpawnLocation, nextSpawn)) {
          spawnIdx++;
          return true;
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
    //Printer.appendToIndicator("closest well: " + targetWell);
    if (targetWell == null) return false;
    if (!rc.isActionReady()) return false;
    if (!canAfford(RobotType.CARRIER)) return false;

    return spawnCarrierTowardsWell(targetWell.getMapLocation());
  }

  private boolean spawnCarrierTowardsWell(MapLocation targetWell) throws GameActionException {
//    if (rc.senseNearbyRobots(targetWell, RobotType.CARRIER.actionRadiusSquared, Cache.Permanent.OUR_TEAM).length > 12) return false;
//
//    Direction dirToWell = Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell);
//    MapLocation goal = Cache.PerTurn.CURRENT_LOCATION.translate(dirToWell.dx * 4, dirToWell.dy * 4);
//    MapLocation toSpawn = goal;
//    do {
//      if (buildRobotAtOrAround(RobotType.CARRIER, toSpawn)) {
//        return true;
//      }
//      Direction toSelf = toSpawn.directionTo(Cache.PerTurn.CURRENT_LOCATION);
//      toSpawn = toSpawn.add(Utils.randomSimilarDirectionPrefer(toSelf));
//      if (toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) <= 2) {
//        Direction dir = Utils.randomDirection();
//        goal = goal.add(dir).add(dir);
//        toSpawn = goal;
//      }
////      Printer.appendToIndicator("Attempted spawn at " + toSpawn);
//    } while (toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 2);
//    return buildRobotAtOrAround(RobotType.CARRIER, Cache.PerTurn.CURRENT_LOCATION);

    MapLocation bestSpawnLocation = null;
    int bestMoveDistance = Integer.MAX_VALUE;
    int bestEucDistance = Integer.MAX_VALUE;
    for (int i = spawnLocations.length; --i >= 0;) {
      MapLocation spawnLocation = spawnLocations[i];
      if (rc.canBuildRobot(RobotType.CARRIER, spawnLocation)) {
        int moveDistance = Utils.maxSingleAxisDist(spawnLocation, targetWell);
        int euclideanDistance = spawnLocation.distanceSquaredTo(targetWell);
        if (moveDistance < bestMoveDistance || (moveDistance == bestMoveDistance && euclideanDistance < bestEucDistance)) {
          bestSpawnLocation = spawnLocation;
          bestMoveDistance = moveDistance;
          bestEucDistance = euclideanDistance;
        }
      }
    }

    if (bestSpawnLocation != null) {
      return buildRobot(RobotType.CARRIER, bestSpawnLocation);
    }
    return false;
  }

  private boolean spawnLauncherTowardsEnemyHQ() throws GameActionException {
    MapLocation goal = Utils.applySymmetry(Cache.PerTurn.CURRENT_LOCATION, Utils.MapSymmetry.ROTATIONAL);
//    Printer.print("Spawning launcher towards enemy HQ - " + goal);

    return spawnLauncherTowardsLocation(goal);
  }

  /**
   * Spawns a launcher towards the given location
   * @param goal
   * @return
   * @throws GameActionException
   */
  private boolean spawnLauncherTowardsLocation(MapLocation goal) throws GameActionException {
    MapLocation bestSpawnLocation = null;
    int bestMoveDistance = Integer.MAX_VALUE;
    int bestEucDistance = Integer.MAX_VALUE;
    for (int i = spawnLocations.length; --i >= 0;) {
      MapLocation spawnLocation = spawnLocations[i];
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
    boolean skipAnchor = false;
    /*WORKFLOW_ONLY*///skipAnchor = true;
    return skipAnchor || (buildAnchor(Anchor.ACCELERATING) || buildAnchor(Anchor.STANDARD));
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

  /**
   * Respects state variables adamantiumToSave and manaToSave (only for anchors)
   * @param type
   * @return
   */
  private boolean canAfford(RobotType type) {
    for (ResourceType rType : ResourceType.values()) {
      if (rType == ResourceType.NO_RESOURCE) continue;
      if (type.getBuildCost(rType) == 0) continue;
      int budget = rc.getResourceAmount(rType);
      switch (rType) {
        case ADAMANTIUM:
          budget -= adamantiumToSave;
          break;
        case MANA:
          budget -= manaToSave;
          break;
        default:
          break;
      }
      if (budget < type.getBuildCost(rType)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Respects state variables adamantiumToSave and manaToSave
   * @param anchorType
   * @return
   */
  private boolean canAfford(Anchor anchorType) {
    for (ResourceType rType : ResourceType.values()) {
      if (rType == ResourceType.NO_RESOURCE) continue;
      if (anchorType.getBuildCost(rType) == 0) continue;

      int budget = rc.getResourceAmount(rType);
      switch (rType) {
        case ADAMANTIUM:
          budget -= adamantiumToSave;
          break;
        case MANA:
          budget -= manaToSave;
          break;
        default:
          break;
      }
      if (budget < anchorType.getBuildCost(rType)) {
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
            case CARRIER_MANA: return 1;
            case CARRIER_ADAMANTIUM: return 2;
            case LAUNCHER: return 3;
        }
        throw new RuntimeException("unknown spawn type: " + this);
    }
  }
}
