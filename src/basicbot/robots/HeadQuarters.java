package basicbot.robots;

import basicbot.communications.Communicator;
import basicbot.utils.Cache;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;
import battlecode.world.Inventory;

import static battlecode.common.GameActionExceptionType.NOT_ENOUGH_RESOURCE;

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
    this.hqID = communicator.registerHQ(this, this.closestAdamantium, this.closestMana);
    determineRole();
  }

  @Override
  protected void runTurn() throws GameActionException {
    if (this.role == HQRole.MAKE_CARRIERS || canAfford(RobotType.CARRIER)) {
      if (this.closestWell != null) {
        rc.setIndicatorString("Spawn towards closest: " + this.closestWell.getMapLocation());
        spawnCarrierTowardsWell(this.closestWell);
      } else if (this.targetWell != null) {
        rc.setIndicatorString("Spawn towards target: " + this.targetWell);
        spawnCarrierTowardsWell(this.targetWell);
      } else {
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
    while (!buildRobot(RobotType.CARRIER, toSpawn) && toSpawn.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 2) {
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
    int sharedArrayOffset = Communicator.CLOSEST_ADAMANTIUM_WELL_INFO_START;
    switch (role) {
      case MAKE_CARRIERS:
        sharedArrayOffset = Communicator.CLOSEST_ADAMANTIUM_WELL_INFO_START;
        break;
      case MAKE_LAUNCHERS:
        sharedArrayOffset = Communicator.CLOSEST_MANA_WELL_INFO_START;
        break;
    }
    int hqWithClosestWell = 0;
    boolean closestUpgraded = false;
    int closestWellDistance = 0b111;
    for (int i = 0; i < 4; i++) {
      int data = rc.readSharedArray(i+sharedArrayOffset);
      int dist = data & 0b111;
//      Printer.print("READ(" + (i+sharedArrayOffset) + "): dist=" + dist);
      if (dist >= closestWellDistance) continue;
      closestWellDistance = dist;
      hqWithClosestWell = i;
      closestUpgraded = (data & 0b1000) != 0;
    }
    if (closestWellDistance == 0b111) {
      targetWell = Utils.applySymmetry(Cache.PerTurn.CURRENT_LOCATION, Utils.MapSymmetry.ROTATIONAL);
    } else {
      targetWell = communicator.readLocationTopBits(hqWithClosestWell + sharedArrayOffset);
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
  }
}
