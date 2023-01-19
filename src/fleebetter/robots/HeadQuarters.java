package fleebetter.robots;

import fleebetter.communications.CommsHandler;
import fleebetter.communications.Communicator;
import fleebetter.communications.HqMetaInfo;
import fleebetter.communications.MapMetaInfo;
import fleebetter.utils.Cache;
import fleebetter.utils.Constants;
import fleebetter.utils.Printer;
import fleebetter.utils.Utils;
import battlecode.common.*;

public class HeadQuarters extends Robot {
  private static final int NUM_FORCED_LATE_GAME_ANCHORS = 3;
  private static final boolean printNumUnitsSpawned = true;
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
  private final SpawnType[] carrierSpawnOrder = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};

  private static final SpawnType[] spawnOrderEnemyHQHere = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA};
  private static final SpawnType[] spawnOrder20x20 = new SpawnType[] {SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.CARRIER_MANA, SpawnType.CARRIER_ADAMANTIUM};
  private static final SpawnType[] spawnOrder40x40 = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER};
  private static final SpawnType[] spawnOrder60x60 = new SpawnType[] {SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_ADAMANTIUM, SpawnType.CARRIER_MANA, SpawnType.CARRIER_MANA, SpawnType.LAUNCHER, SpawnType.LAUNCHER, SpawnType.LAUNCHER};
  private static final SpawnType[] spawnOrderEndangeredWells = spawnOrder20x20;

  private int spawnIdx = 0;
  private SpawnType[] spawnOrder;

  MapLocation[] spawnLocations;

  private int totalSpawns = 0;

  private boolean foundEndangeredWells;
  private int checkedEndangeredWellsCounter;


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

  @Override
  protected void runTurn() throws GameActionException {
//    if (Cache.PerTurn.ROUND_NUM >= 500) rc.resign();
    if (Cache.PerTurn.ROUNDS_ALIVE == 1) {
      Communicator.MetaInfo.reinitForHQ();
      updateWellExploration();
      unknown_symmetry: if (MapMetaInfo.knownSymmetry == null) {
//      do {
//        prev = MapMetaInfo.knownSymmetry;
        for (MapLocation myHQ : HqMetaInfo.hqLocations) {
          MapLocation enemyHQ;
          if (!MapMetaInfo.notRotational) {
            enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.ROTATIONAL);
            if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
              RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
              if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
                MapMetaInfo.writeNot(Utils.MapSymmetry.ROTATIONAL);
              }
            }
          }
          if (!MapMetaInfo.notHorizontal) {
            enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.HORIZONTAL);
            if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
              RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
              if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
                MapMetaInfo.writeNot(Utils.MapSymmetry.HORIZONTAL);
              }
            }
          }
          if (!MapMetaInfo.notVertical) {
            enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.VERTICAL);
            if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
              RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
              if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
                MapMetaInfo.writeNot(Utils.MapSymmetry.VERTICAL);
              }
            }
          }
        }
//      } while (prev != MapMetaInfo.knownSymmetry);
        if (MapMetaInfo.knownSymmetry == null) {
          updateSymmetryComms();
        }
      }
    }
//    if (Cache.PerTurn.ROUND_NUM >= 10) rc.resign();
    Communicator.clearEnemyComms();

    if (printNumUnitsSpawned && Cache.PerTurn.ROUND_NUM % 250 == 249) {
      Printer.print("HQ" + Cache.PerTurn.ROUND_NUM + Cache.Permanent.OUR_TEAM + hqID + " (" + totalSpawns + ")");
    }

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

    if (spawnIdx < spawnOrder.length) {
      forceSpawnOrder();
    } else {
      normalSpawnOrder();
    }
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

  private MapLocation getPreferredCarrierSpawnLocation(SpawnType nextSpawn) throws GameActionException {
    MapLocation closestWellLocation = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, nextSpawn.getResourceType());
    if (closestWellLocation == null) {
      closestWellLocation = Utils.randomMapLocation();
    }
    return closestWellLocation;
  }

  private void normalSpawnOrder() throws GameActionException {
    if (spawnAmplifierCooldown > 0) --spawnAmplifierCooldown;

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
        }
      }
      return;
    }

    if (Cache.PerTurn.ROUND_NUM >= 500 && numAnchorsMade <= NUM_FORCED_LATE_GAME_ANCHORS) {
      // consider anchor spawn
      rc.setIndicatorString("Build anchor");
      if (createAnchors()) {
        numAnchorsMade++;
      }
      return;
    }

    if (canAfford(RobotType.LAUNCHER)) {
      if (spawnLauncherTowardsEnemyHQ()) spawnAmplifierCooldown -= 2;
    } else if (canAfford(RobotType.CARRIER)) {
      SpawnType nextSpawn = carrierSpawnOrder[carrierSpawnOrderIdx];
      MapLocation preferredSpawnLocation = getPreferredCarrierSpawnLocation(nextSpawn);
      if (spawnAndCommCarrier(preferredSpawnLocation, nextSpawn)) {
        carrierSpawnOrderIdx = (carrierSpawnOrderIdx + 1) % carrierSpawnOrder.length;
        if (spawnAmplifierCooldown > 0) spawnAmplifierCooldown -= 2;
      }
    }

  }

  private void forceSpawnOrder() throws GameActionException {
    SpawnType nextSpawn = spawnOrder[spawnIdx];
    switch (nextSpawn) {
      case CARRIER_MANA:
      case CARRIER_ADAMANTIUM:
        MapLocation preferredSpawnLocation = getPreferredCarrierSpawnLocation(nextSpawn);
        if (spawnAndCommCarrier(preferredSpawnLocation, nextSpawn)) {
          spawnIdx++;
        }
        break;
      case LAUNCHER:
        if (canAfford(RobotType.LAUNCHER)) {
          if (spawnLauncherTowardsEnemyHQ()) {
            int commBits = nextSpawn.getCommBits();
            MapLocation spawnLocation = lastSpawnLoc;
            //todo: do comms
            ++spawnIdx;
          }
        }
        break;
    }
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

    MapLocation goal = targetWell;
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
      totalSpawns++;
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
