package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.utils.Cache;
import basicbot.utils.Utils;
import battlecode.common.*;

public class HeadQuarters extends Robot {
  private int hqID;
  public final WellInfo closestAdamantium;
  public final WellInfo closestMana;

  private WellInfo closestWell;
  private MapLocation targetWell;
  private boolean targetWellUpgraded;

  private HQRole role;


  public HeadQuarters(RobotController rc) throws GameActionException {
    super(rc);
    this.closestAdamantium = getClosestWell(ResourceType.ADAMANTIUM);
    this.closestMana = getClosestWell(ResourceType.MANA);
    this.closestWell = this.closestAdamantium;
    if (this.closestAdamantium == null || (this.closestMana != null && this.closestMana.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < this.closestAdamantium.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION))) {
      this.closestWell = this.closestMana;
    }
    this.hqID = Communicator.MetaInfo.registerHQ(this.closestAdamantium, this.closestMana);
    determineRole();
  }

  @Override
  protected void runTurn() throws GameActionException {
    if (Cache.PerTurn.ROUNDS_ALIVE == 1) Communicator.MetaInfo.reinitForHQ();
//    if (Cache.PerTurn.ROUND_NUM == 50) rc.resign();
    if (Cache.PerTurn.ROUND_NUM >= 100 && Cache.PerTurn.ROUND_NUM % 200 <= 20) {
      this.role = HQRole.BUILD_ANCHORS;
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
      createAnchors();
      determineRole();
    }
  }

  private void determineRole() throws GameActionException {
    this.role = HQRole.MAKE_CARRIERS;
    if (this.closestWell == null) {
      this.role = HQRole.MAKE_LAUNCHERS;
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
    while (!buildRobot(RobotType.LAUNCHER, toSpawn) && toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 2) {
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
}
