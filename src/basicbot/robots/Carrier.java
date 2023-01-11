package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.containers.HashMap;
import basicbot.containers.LinkedList;
import basicbot.robots.micro.CarrierWellPathing;
import basicbot.utils.Cache;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

import java.util.Arrays;

public class Carrier extends Robot {
  private static final int SET_WELL_PATH_DISTANCE = RobotType.CARRIER.actionRadiusSquared;

  private MapLocation targetHQ;

  private MapLocation targetWell;
  private boolean targetWellUpgraded;

  HashMap<MapLocation, Direction> wellApproachDirection;
  MapLocation[] wellPathToFollow;
  int wellPathTargetIndex;
  private int lastEmptyRobotID;
  private int lastEmptyRobotDistance;
  private int emptyRobotsSeen;

  private CarrierRole role;
  private int turnsSinceRoleChange;

  private int maxResourceToCarry;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    this.turnsSinceRoleChange = Integer.MAX_VALUE;
    wellApproachDirection = new HashMap<>(3);
    wellPathToFollow = null;
    resetRole();
  }

  @Override
  protected void runTurn() throws GameActionException {
//    if (Cache.PerTurn.ROUND_NUM == 400) {
//      rc.resign();
//    }
    switch (this.role) {
      case ADAMANTIUM_COLLECTION:
      case MANA_COLLECTION:
        if (this.targetWell == null) searchForTargetWell();
        if (this.targetWell != null) runCollection(role.getResourceCollectionType());
        break;
      default:
        resetRole();
    }
    if (turnCount % 10 == 9) {
      resetRole();
    }
    turnsSinceRoleChange++;
  }

  private void resetRole() throws GameActionException {
    CarrierRole newRole = determineRole();
    if (newRole != this.role) {
      this.role = newRole;
      turnsSinceRoleChange = 0;
      switch (this.role) {
        case ADAMANTIUM_COLLECTION:
          initAdamantiumCollection();
          break;
        case MANA_COLLECTION:
          initManaCollection();
          break;
      }
    }
  }

  private CarrierRole determineRole() throws GameActionException {
    if (this.role == null) return CarrierRole.DEFAULT_ROLE;
//    this.role = CarrierRole.ADAMANTIUM_COLLECTION;
    int numLocalCarriers = 0;
    for (RobotInfo friend : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (friend.type == RobotType.CARRIER) {
        numLocalCarriers++;
      }
    }
    if (numLocalCarriers > 10 && turnsSinceRoleChange >= 100) {
      return this.role.toggleCollectionType();
    }
    return this.role;
  }

  private boolean searchForTargetWell() throws GameActionException {
    while (pathing.moveRandomly()) {
      WellInfo well = getClosestWell(role.getResourceCollectionType());
      if (well != null) {
        rc.setIndicatorString("found well: " + well);
        setTargetWell(well);
        return true;
      }
    }
    return false;
  }

  private void initAdamantiumCollection() throws GameActionException {
    determineTargetWell(ResourceType.ADAMANTIUM);
    maxResourceToCarry = GameConstants.CARRIER_CAPACITY;
  }

  private void initManaCollection() throws GameActionException {
    determineTargetWell(ResourceType.MANA);
    maxResourceToCarry = GameConstants.CARRIER_CAPACITY;
  }

  private void runCollection(ResourceType resourceType) throws GameActionException {
    if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
      rc.setIndicatorString("RET " + resourceType + ": " + targetWell + "->" + targetHQ);
      if (rc.canTransferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType))) {
        rc.transferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType));
      }
      MapLocation targetPosition = targetHQ;
      if (targetWell != null) {
        targetPosition = targetWell;
        while (!targetPosition.isAdjacentTo(targetHQ)) {
          Direction toHQ = targetPosition.directionTo(targetHQ);
          targetPosition = targetPosition.add(Utils.randomSimilarDirectionPrefer(toHQ));
        }
      }
      while (pathing.moveTowards(targetPosition)) {}
//      while (pathing.moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetPosition))) {}
      while (pathing.moveRandomly()) {}
      if (rc.canTransferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType))) {
        rc.transferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType));
      } else {
//        rc.setIndicatorString("FAILED TO TRANSFER" + targetHQ + " " + resourceType + " " + rc.getResourceAmount(resourceType));
      }
    } else if (this.targetWell != null) {
      rc.setIndicatorString("COL " + resourceType + ": " + targetWell + "->" + targetHQ);
      doWellCollection(resourceType);

      if (wellPathToFollow == null) {
        while (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(targetWell, SET_WELL_PATH_DISTANCE)
            && pathing.moveTowards(targetWell)) {
          wellApproachDirection.setAlreadyContainedValue(targetWell, targetWell.directionTo(Cache.PerTurn.CURRENT_LOCATION));
        }
        if (Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(targetWell) <= SET_WELL_PATH_DISTANCE) {
          wellPathToFollow = CarrierWellPathing.getPathForWell(targetWell, wellApproachDirection.get(targetWell));
          Printer.print("well path: " + Arrays.toString(wellPathToFollow), "from direction: " + wellApproachDirection.get(targetWell));
        }
      }
      if (wellPathToFollow != null) {
        followWellPath();
      }

//      while (pathing.moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell))) {}
//      while (pathing.moveRandomly()) {}

      doWellCollection(resourceType);
    } else {
//      while (pathing.moveRandomly()) {}
      // periodically check comms for new target
    }
  }

  /**
   * will collect as much as possible from the target well based on upgrade status
   * @param resourceType the resource to collect
   * @throws GameActionException any issues during collection
   */
  private void doWellCollection(ResourceType resourceType) throws GameActionException {
    while (collectResource(targetWell, getWellRate())) {
      if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
        return;
      }
    }
  }

  /**
   * will follow the collection path of the well (circle around the well)
   * will move along path depending on robots seen
   */
  private void followWellPath() throws GameActionException {
    if (wellPathTargetIndex == -1) {
      updateWellPathTarget();
      if (wellPathTargetIndex != -1) {
        Printer.print("set target: " + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]]);
      }
    }
    if (updateEmptyRobotsSeen(role.getResourceCollectionType())) {
      updateWellPathTarget();
      if (wellPathTargetIndex != -1) {
        Printer.print("empty robots seen: " + emptyRobotsSeen, "new target: " + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]]);
      }
    }

    if (wellPathTargetIndex != -1) {
//      MapLocation target = wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]];
      rc.setIndicatorString("well path target: " + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]]);
      while (rc.isMovementReady()) {
        // not yet in the path, just go to the entry point
        if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(targetWell)) {
          if (pathing.moveTowards(wellPathToFollow[0])) {
            continue;
          }
        }

        // in the path, so try to get towards the correct point by navigating through the path
        int moveTrial = wellPathTargetIndex;
        while (moveTrial >= 0 && !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]) && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]])))) {
          // we can't move (adjacent + can move)
          moveTrial--;
        }
        if (moveTrial < 0) {
          break;
        }
//        Printer.print("following path: " + Cache.PerTurn.CURRENT_LOCATION + "|" + rc.getLocation() + " --> " + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]);
        Printer.print("following path: ->" + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]);
        pathing.forceMoveTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]);
      }
//      while (pathing.moveTowards(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]])) {}
    } else {
      while (pathing.moveAwayFrom(targetWell)) {}
    }
  }

  private void updateWellPathTarget() throws GameActionException {
    if (wellPathToFollow == null) return;
    wellPathTargetIndex = -1;
    for (int i = 0; i < 9; i++) {
      MapLocation pathTarget = wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[i]];
      if (rc.canSenseLocation(pathTarget) && !rc.canSenseRobotAtLocation(pathTarget)) {
        wellPathTargetIndex = i;
        if (i >= emptyRobotsSeen) {
          return;
        }
      }
    }
  }

  /**
   * senses around and updates the number of empty robots seen near this well
   * @param resourceType the resource type to check for
   * @return whether an update was made or not
   */
  private boolean updateEmptyRobotsSeen(ResourceType resourceType) {
    boolean closeToWell = Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(targetWell, 9);
    int counter = 0;
    for (RobotInfo friendly : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (friendly.type == RobotType.CARRIER && friendly.getResourceAmount(resourceType) < maxResourceToCarry) {
        if (closeToWell) {
          counter++;
        } else if (friendly.ID != lastEmptyRobotID && friendly.getResourceAmount(resourceType) <= getWellRate()) {
          int dist = friendly.location.distanceSquaredTo(targetWell);
          if (dist >= lastEmptyRobotDistance) {
            counter++;
            lastEmptyRobotID = friendly.ID;
            lastEmptyRobotDistance = dist;
          }
        }
      }
    }
    if (closeToWell) {
      emptyRobotsSeen = 0;
    }
    emptyRobotsSeen += counter;
    return counter != 0;
  }

  /**
   * @return the rate at which the well is producing resources
   */
  private int getWellRate() {
    return targetWellUpgraded ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE;
  }
  private boolean collectResource(MapLocation well, int amount) throws GameActionException {
    if (rc.canCollectResource(well, amount)) {
      rc.collectResource(well, amount);
      return true;
    }
    return false;
  }

  private void determineTargetWell(ResourceType resourceType) throws GameActionException {
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
    targetHQ = communicator.commsHandler.readOurHqLocation(hqWithClosestWell);
    if (closestWellLocation == null) {
      setTargetWell(null, false);
      if (resourceType == ResourceType.MANA) {
//      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
        hqWithClosestWell++;
        hqWithClosestWell %= rc.readSharedArray(communicator.metaInfo.hqInfo.hqCount);
        targetHQ = communicator.commsHandler.readOurHqLocation(hqWithClosestWell);
//      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
      }
    } else {
      setTargetWell(closestWellLocation, closestUpgraded);
    }
  }

  private void setTargetWell(MapLocation targetWell, boolean targetWellUpgraded) {
    if (this.targetWell != targetWell) {
      this.wellPathToFollow = null;
      this.wellPathTargetIndex = -1;
      this.lastEmptyRobotID = -1;
      this.lastEmptyRobotDistance = Integer.MAX_VALUE;
      this.emptyRobotsSeen = 0;
    }
    this.targetWell = targetWell;
    this.targetWellUpgraded = targetWellUpgraded;
    this.wellApproachDirection.put(targetWell, Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell));
  }
  private void setTargetWell(WellInfo targetWell) {
    setTargetWell(targetWell.getMapLocation(), targetWell.isUpgraded());
  }

//  private RobotInfo shouldAttackEnemy() throws GameActionException {
//    // if enemy launcher, consider attacking 1) closest
//    RobotInfo bestEnemyToAttack = null;
//    int bestValue = 0;
//
//    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
//      RobotType type = enemyRobot.getType();
//      int costToBuild = type.buildCostAdamantium + type.buildCostMana + type.buildCostElixir;
//      int carryingResourceValue = enemyRobot.inventory.getWeight();
//      int enemyValue = costToBuild + carryingResourceValue;
//      //todo: maybe make if-statement always true for launchers depending on if we have launchers or not
//      if (enemyRobot.health / GameConstants.CARRIER_DAMAGE_FACTOR <= enemyValue || type == RobotType.LAUNCHER) { // it is worth attacking this enemy;
//        // determine if we have enough to attack it...
//        int totalDmg = 0;
//        RobotInfo[] robotInfos = rc.senseNearbyRobots(enemyRobot.location, -1, Cache.Permanent.OUR_TEAM); //assume this returns this robot as well
//        for (RobotInfo friendlyRobot : robotInfos) {
//          //todo: maybe dont consider launchers in dmg calculation here
//          totalDmg += (friendlyRobot.inventory.getWeight() * GameConstants.CARRIER_DAMAGE_FACTOR) + friendlyRobot.type.damage;
//        }
//        Printer.print("enemy location: " + enemyRobot.location + " can deal: " + totalDmg);
//        // todo: consider allowing only launcher to attack or smth?
//        if (totalDmg > enemyRobot.health) { // we can kill it
//          if (bestEnemyToAttack == null || enemyValue > bestValue || (enemyValue == bestValue && bestEnemyToAttack.health < enemyRobot.health)) {
//            bestEnemyToAttack = enemyRobot;
//            bestValue = enemyValue;
//          }
//        }
//      }
//    }
//    return bestEnemyToAttack;
//  }

  private enum CarrierRole {
    ADAMANTIUM_COLLECTION,
    MANA_COLLECTION;

    private static final CarrierRole DEFAULT_ROLE = CarrierRole.ADAMANTIUM_COLLECTION;

    CarrierRole toggleCollectionType() {
      switch (this) {
        case ADAMANTIUM_COLLECTION: return MANA_COLLECTION;
        case MANA_COLLECTION: return ADAMANTIUM_COLLECTION;
      }
      return null;
    }

    public ResourceType getResourceCollectionType() {
      switch (this) {
        case ADAMANTIUM_COLLECTION: return ResourceType.ADAMANTIUM;
        case MANA_COLLECTION: return ResourceType.MANA;
      }
      return null;
    }
  }
}
