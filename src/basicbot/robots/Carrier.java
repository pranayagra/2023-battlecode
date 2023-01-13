package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.containers.HashMap;
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
  private int emptierRobotsSeen;
  private int fullerRobotsSeen;

  private CarrierRole role;
  private int turnsSinceRoleChange;

  private int maxResourceToCarry;

  int fleeingCounter;
  MapLocation lastEnemyLocation;

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
//    if (Cache.PerTurn.ROUND_NUM >= 200) rc.resign();
    if (enemyExists()) {
        RobotInfo enemyToAttack = enemyToAttackIfWorth();
        if (enemyToAttack == null) enemyToAttack = attackEnemyIfCannotRun();
//        Printer.print("enemyToAttack - " + enemyToAttack);
        if (enemyToAttack != null && rc.isActionReady()) {
          // todo: attack it!
          if (rc.canAttack(enemyToAttack.location)) {
//            Printer.print("it can attack w/o moving");
            rc.attack(enemyToAttack.location);
          } else {
            // move and then attack
            pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(enemyToAttack.location));
//            Printer.print("moved to attack... " + rc.canAttack(enemyToAttack.location), "loc=" + enemyToAttack.location, "canAct=" + rc.canActLocation(enemyToAttack.location), "robInfo=" + rc.senseRobotAtLocation(enemyToAttack.location));
            if (rc.canAttack(enemyToAttack.location)) {
              rc.attack(enemyToAttack.location);
            }
          }
        }
        updateLastEnemy();
    }

    if (fleeingCounter > 0) {
      // run from lastEnemyLocation
      Direction away = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation).opposite();
      MapLocation fleeDirection = Cache.PerTurn.CURRENT_LOCATION.add(away).add(away).add(away).add(away).add(away);
      // todo: move towards fleeDirection
      pathing.moveInDirLoose(away);
      fleeingCounter--;
    }

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
    resetRole(null);
  }
  private void resetRole(CarrierRole newRole) throws GameActionException {
    if (newRole == null) newRole = determineRole();
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
      rc.setIndicatorString("Role: " + this.role + " (" + targetWell + "->" + targetHQ + ")");
    }
  }

  private CarrierRole determineRole() throws GameActionException {
    if (this.role == null) return CarrierRole.DEFAULT_ROLE;
//    this.role = CarrierRole.ADAMANTIUM_COLLECTION;
    int numLocalCarriers = 0;
    for (RobotInfo friend : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (friend.type == RobotType.CARRIER && friend.getResourceAmount(role.getResourceCollectionType()) > 0) {
        numLocalCarriers++;
      }
    }
    if (numLocalCarriers > 10 && turnsSinceRoleChange >= 100 && rc.getResourceAmount(role.getResourceCollectionType()) == 0) {
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
      if (!doTransfer(targetHQ, resourceType)) {
        rc.setIndicatorString("transfer failed - " + targetHQ + "..ActCD=" + rc.getActionCooldownTurns() + "..canAct=" + rc.canActLocation(targetHQ) + "..robInfo=" + (rc.canSenseLocation(targetHQ) ? rc.senseRobotAtLocation(targetHQ) : "unknown"));
      }
      if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(targetWell)) {
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
      } else { // currently adjacent to well, need to gtfo
        // move away from the well and towards the HQ
        Direction awayFromWell = targetWell.directionTo(Cache.PerTurn.CURRENT_LOCATION);
        Direction awayLeft = awayFromWell.rotateLeft();
        Direction awayRight = awayFromWell.rotateRight();
        Direction towardsHQ = Cache.PerTurn.CURRENT_LOCATION.directionTo(targetHQ);
        Direction hqLeft = towardsHQ.rotateLeft();
        Direction hqRight = towardsHQ.rotateRight();
        if      (awayFromWell == Direction.CENTER && (pathing.moveInDirLoose(towardsHQ) || pathing.moveRandomly() || true)) {}
        else if (awayFromWell == towardsHQ && pathing.move(awayFromWell)) {}
        else if (awayLeft  == towardsHQ && pathing.move(awayLeft)) {}
        else if (awayRight == towardsHQ && pathing.move(awayRight)) {}
        else if (awayLeft  == hqLeft && pathing.move(awayLeft)) {}
        else if (awayLeft  == hqRight && pathing.move(awayLeft)) {}
        else if (awayRight == hqLeft && pathing.move(awayRight)) {}
        else if (awayRight == hqRight && pathing.move(awayRight)) {}
        else if (pathing.moveTowards(targetHQ)) {}
        else if (pathing.moveAwayFrom(targetWell)) {}
      }
      doTransfer(targetHQ, resourceType);
    } else if (this.targetWell != null) {
//      rc.setIndicatorString("COL " + resourceType + ": " + targetWell + "->" + targetHQ);
      doWellCollection(resourceType);

      if (wellPathToFollow == null) {
        rc.setIndicatorString("no well path -- approaching=" + targetWell + " dist=" + Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(targetWell));
        while (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(targetWell, SET_WELL_PATH_DISTANCE)
            && pathing.moveTowards(targetWell)) {
//          wellApproachDirection.setAlreadyContainedValue(targetWell, targetWell.directionTo(Cache.PerTurn.CURRENT_LOCATION));
//          Printer.print("moved towards well: " + targetWell + " now=" + Cache.PerTurn.CURRENT_LOCATION);//, "new dir back: " + wellApproachDirection.get(targetWell));
          rc.setIndicatorString("did pathing towards well:" + Cache.PerTurn.CURRENT_LOCATION + "->" + targetWell);
        }
        if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(targetWell, SET_WELL_PATH_DISTANCE)) {
          wellApproachDirection.setAlreadyContainedValue(targetWell, targetWell.directionTo(Cache.PerTurn.CURRENT_LOCATION));
          wellPathToFollow = CarrierWellPathing.getPathForWell(targetWell, wellApproachDirection.get(targetWell));
//          Printer.print("well path: " + Arrays.toString(wellPathToFollow), "from direction: " + wellApproachDirection.get(targetWell));
          rc.setIndicatorString("set well path:" + Cache.PerTurn.CURRENT_LOCATION + "->" + targetWell);
        }
      }
      if (wellPathToFollow != null) {
        rc.setIndicatorString("follow well path: " + targetWell);
        followWellPath();
//      } else {
//        Printer.print("Cannot get to well! -- canMove=" + rc.isMovementReady());
      }

//      while (pathing.moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell))) {}
//      while (pathing.moveRandomly()) {}

      doWellCollection(resourceType);
    } else {
//      while (pathing.moveRandomly()) {}
      // periodically check comms for new target
    }
  }

  private boolean doTransfer(MapLocation target, ResourceType resourceType) throws GameActionException {
    if (rc.canTransferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType))) {
      rc.transferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType));
      return true;
    }
//    rc.setIndicatorString("FAILED TO TRANSFER" + targetHQ + " " + resourceType + " " + rc.getResourceAmount(resourceType));
    return false;
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
//      if (wellPathTargetIndex != -1) {
//        Printer.print("set target: " + wellPathTargetIndex + ":" + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]]);
//      }
    }
    updateRobotsSeenInQueue(role.getResourceCollectionType());
    updateWellPathTarget();
//    if (wellPathTargetIndex != -1) {
//      Printer.print("  emptier=" + emptierRobotsSeen + "--fuller=" + fullerRobotsSeen, "new target: " + wellPathTargetIndex + ":" + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]]);
//    }

    if (wellPathTargetIndex != -1) {
//      MapLocation target = wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]];
      rc.setIndicatorString("well path target: " + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]]);
      while (rc.isMovementReady() && !Cache.PerTurn.CURRENT_LOCATION.equals(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]])) {
        // not yet in the path, just go to the entry point
        if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(targetWell)) {
          if (pathing.moveTowards(wellPathToFollow[0])) {
            continue;
          }
        }

        // in the path, so try to get towards the correct point by navigating through the path
        int currentPathIndex = -1;
        for (int i = 0; i < wellPathToFollow.length; i++) {
          if (Cache.PerTurn.CURRENT_LOCATION.equals(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[i]])) {
            currentPathIndex = i;
            break;
          }
        }

        int moveTrial = wellPathTargetIndex;
        if (currentPathIndex < wellPathTargetIndex) {
          while (moveTrial >= 0 && !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]) && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]])))) {
            // we can't move (adjacent + can move)
            moveTrial--;
          }
          if (moveTrial < 0) {
            break;
          }
        } else {
          while (moveTrial < 9 && !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]) && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]])))) {
            // we can't move (adjacent + can move)
            moveTrial++;
          }
          if (moveTrial >= 9) {
            break;
          }
        }
//        Printer.print("following path: " + Cache.PerTurn.CURRENT_LOCATION + "|" + rc.getLocation() + " --> " + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]);
//        Printer.print("following path: ->" + wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]);
        pathing.forceMoveTo(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[moveTrial]]);
      }
//      while (pathing.moveTowards(wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[wellPathTargetIndex]])) {}
    } else {
      // we aren't there yet, so just get out of the way      || we're already full so get out
      if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(targetWell) || rc.getResourceAmount(role.getResourceCollectionType()) >= maxResourceToCarry) {
//        pathing.moveTowards(targetWell);
        rc.setIndicatorString("there's people in the way so ima dip");
//        while (pathing.moveAwayFrom(targetWell)) {}
        resetRole(role.toggleCollectionType());
      } else {
        pathing.moveToOrAdjacent(targetWell);
      }
    }
  }

  /**
   * fins the first coordinate in the well path that can be moved to
   * @throws GameActionException any issues during computation
   */
  private void updateWellPathTarget() throws GameActionException {
    if (wellPathToFollow == null) return;
    wellPathTargetIndex = -1;
    int minFillSpot = emptierRobotsSeen;
    int maxFillSpot = 8 - fullerRobotsSeen;
    int testSpot;
    while (minFillSpot <= maxFillSpot) {
      if (minFillSpot < (8-maxFillSpot)) {
        testSpot = minFillSpot;
        minFillSpot++;
      } else {
        testSpot = maxFillSpot;
        maxFillSpot--;
      }
      MapLocation pathTarget = wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[testSpot]];
      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || (rc.canSenseLocation(pathTarget) && !rc.canSenseRobotAtLocation(pathTarget))) {
        // we can move to the target (or already there)
        wellPathTargetIndex = testSpot;
        return;
//      } else {
//        Printer.print("can't move from=" + Cache.PerTurn.CURRENT_LOCATION + ".to=" + pathTarget + " sense=" + rc.canSenseLocation(pathTarget) + ".bot=" + rc.canSenseRobotAtLocation(pathTarget));
      }
    }
    while (maxFillSpot >= 0) {
      MapLocation pathTarget = wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[maxFillSpot]];
      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || (rc.canSenseLocation(pathTarget) && !rc.canSenseRobotAtLocation(pathTarget))) {
        // we can move to the target (or already there)
        wellPathTargetIndex = maxFillSpot;
        return;
//      } else {
//        Printer.print("can't move from=" + Cache.PerTurn.CURRENT_LOCATION + ".to=" + pathTarget + " sense=" + rc.canSenseLocation(pathTarget) + ".bot=" + rc.canSenseRobotAtLocation(pathTarget));
      }
      maxFillSpot--;
    }
//    for (int i = 0; i < maxFillSpot; i++) {
//      MapLocation pathTarget = wellPathToFollow[CarrierWellPathing.WELL_PATH_FILL_ORDER[i]];
//      // already there or can move there
//      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || (rc.canSenseLocation(pathTarget) && !rc.canSenseRobotAtLocation(pathTarget))) {
//        wellPathTargetIndex = i;
//        if (i >= emptierRobotsSeen) {
//          return;
//        }
//      }
//    }
  }

  /**
   * senses around and updates the number of emptier/fuller robots seen near this well
   * if we aren't close to the well, leave the info as is
   * @param resourceType the resource type to check for
   * @return whether an update was made or not
   */
  private boolean updateRobotsSeenInQueue(ResourceType resourceType) {
    if (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(targetWell, 9)) {
      if (emptierRobotsSeen > 0 || fullerRobotsSeen > 0) {
//        Printer.print("not close to well, so not updating emptier/fuller robots seen");
        emptierRobotsSeen = 0;
        fullerRobotsSeen = 0;
        return true;
      }
      return false;
    }
    int newEmptierSeen = 0;
    int newFullerSeen = 0;
    int myAmount = rc.getResourceAmount(resourceType);
    for (RobotInfo friendly : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (friendly.type == RobotType.CARRIER) {
        int friendlyAmount = friendly.getResourceAmount(resourceType);
        if (friendlyAmount == myAmount) {
          if (friendly.ID < Cache.Permanent.ID) {
            newEmptierSeen++;
          } else {
            newFullerSeen++;
          }
        } else if (friendlyAmount < myAmount) {
          newEmptierSeen++;
        } else if (friendlyAmount < maxResourceToCarry) {
          newFullerSeen++;
        }
      }
    }
    boolean changed = newEmptierSeen != emptierRobotsSeen || newFullerSeen != fullerRobotsSeen;
    emptierRobotsSeen = newEmptierSeen;
    fullerRobotsSeen = newFullerSeen;
    return changed;
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
      closestWellLocation = tmpLocation;
      closestUpgraded = resourceTypeReaderWriter.readWellUpgraded(i);
    }

    int hqWithClosestWell = closestWellLocation != null ? communicator.metaInfo.hqInfo.getClosestHQ(closestWellLocation) : communicator.metaInfo.hqInfo.getClosestHQ(Cache.PerTurn.CURRENT_LOCATION);
    targetHQ = communicator.commsHandler.readOurHqLocation(hqWithClosestWell);
//    if (resourceType == ResourceType.MANA) {
////      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
//      hqWithClosestWell++;
//      hqWithClosestWell %= communicator.metaInfo.hqInfo.hqCount;
//      targetHQ = communicator.commsHandler.readOurHqLocation(hqWithClosestWell);
////      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
//    }

    setTargetWell(closestWellLocation, closestUpgraded);
  }


  private void setTargetWell(MapLocation targetWell, boolean targetWellUpgraded) {
    if (this.targetWell != targetWell) {
      this.wellPathToFollow = null;
      this.wellPathTargetIndex = -1;
      this.emptierRobotsSeen = 0;
      this.fullerRobotsSeen = 0;
    }
    this.targetWell = targetWell;
    this.targetWellUpgraded = targetWellUpgraded;
    Direction dirBackFromWell = targetWell.directionTo(Cache.PerTurn.CURRENT_LOCATION);
    if (targetHQ != null && targetHQ.isWithinDistanceSquared(targetWell, RobotType.HEADQUARTERS.visionRadiusSquared)) {
      dirBackFromWell = targetWell.directionTo(targetHQ);
    }
    this.wellApproachDirection.put(targetWell, dirBackFromWell);
//    Printer.print("put new well (" + targetWell + ") dir: " + dirBackFromWell, "return rss to " + targetHQ);
  }
  private void setTargetWell(WellInfo targetWell) {
    setTargetWell(targetWell.getMapLocation(), targetWell.isUpgraded());
  }

  /*
  CARRIER BEHAVIOR AGAINST OPPONENT
  1) if we can kill it effectively, kill it
  2) if we cannot kill it
    2.1) attempt to run away. if we are going die (or maybe easy health is <= 7)
  * */

  private boolean enemyExists() throws GameActionException {
    return Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0;
  }

  private int getInvWeight(RobotInfo ri) {
    return (ri.getResourceAmount(ResourceType.ADAMANTIUM) + ri.getResourceAmount(ResourceType.MANA) + ri.getResourceAmount(ResourceType.ELIXIR) + (ri.getTotalAnchors() == 0 ? 0 : 40));
  }

  private RobotInfo enemyToAttackIfWorth() throws GameActionException {
//    Printer.print("enemyToAttackIfWorth()");
    int myInvSize = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) + (rc.getAnchor() != null ? 40 : 0));
    if (myInvSize <= 4) {
//      Printer.print("null bc invSize <= 4");
      return null;
    }

    // if enemy launcher, consider attacking 1) closest
    RobotInfo bestEnemyToAttack = null;
    int bestValue = 0;

    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      RobotType type = enemyRobot.getType();
      int costToBuild = type.buildCostAdamantium + type.buildCostMana + type.buildCostElixir;
      int carryingResourceValue = getInvWeight(enemyRobot);
      int enemyValue = costToBuild + carryingResourceValue;
      //todo: maybe make if-statement always true for launchers depending on if we have launchers or not
      if (enemyRobot.health / GameConstants.CARRIER_DAMAGE_FACTOR <= enemyValue || type == RobotType.LAUNCHER) { // it is worth attacking this enemy;
        // determine if we have enough to attack it...
        int totalDmg = 0;
        totalDmg += myInvSize * GameConstants.CARRIER_DAMAGE_FACTOR;
        RobotInfo[] robotInfos = rc.senseNearbyRobots(enemyRobot.location, -1, Cache.Permanent.OUR_TEAM); //assume this returns this robot as well
        for (RobotInfo friendlyRobot : robotInfos) {
          //todo: maybe dont consider launchers in dmg calculation here
          totalDmg += (getInvWeight(friendlyRobot) * GameConstants.CARRIER_DAMAGE_FACTOR) + friendlyRobot.type.damage;
        }

//        Printer.print("enemy location: " + enemyRobot.location + " we deal: " + totalDmg);
        // todo: consider allowing only launcher to attack or smth?
        if (totalDmg > enemyRobot.health) { // we can kill it
          if (bestEnemyToAttack == null || enemyValue > bestValue || (enemyValue == bestValue && bestEnemyToAttack.health < enemyRobot.health)) {
            bestEnemyToAttack = enemyRobot;
            bestValue = enemyValue;
          }
        }
      }
    }
//    Printer.print("bestEnemyToAttack=" + bestEnemyToAttack + ", value=" + bestValue);
    return bestEnemyToAttack;
    //todo: perform the attack here?
  }

  private int numMoves() {
    int moves = 0;
    int cd = rc.getMovementCooldownTurns();
    while (cd < 10 && moves < 5) {
      cd += rc.getType().movementCooldown;
      ++moves;
    }
    return moves;
  }

  private RobotInfo attackEnemyIfCannotRun() {
//    Printer.print("attackEnemyIfCannotRun()");
    int myInvSize = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) + (rc.getAnchor() != null ? 40 : 0));
    if (myInvSize <= 4) {
//      Printer.print("null bc invSize <= 4");
      return null;
    }

    // sum damage based on how many enemies can currently attack me (this bot must be within action radius of enemy's bot)
    int enemyDamage = 0;
    RobotInfo enemyToAttack = null;
//    int numMoves = numMoves();
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (rc.getLocation().isWithinDistanceSquared(enemyRobot.location, enemyRobot.type.actionRadiusSquared)) {
        enemyDamage += getInvWeight(enemyRobot) * GameConstants.CARRIER_DAMAGE_FACTOR + enemyRobot.type.damage;
      }
      if (rc.getLocation().isWithinDistanceSquared(enemyRobot.location, rc.getType().actionRadiusSquared)) { // todo: need to consider movement here
        if (enemyToAttack == null || (enemyToAttack.type != RobotType.LAUNCHER && enemyRobot.type == RobotType.LAUNCHER))
        enemyToAttack = enemyRobot;
      }
    }

    if (enemyToAttack == null) {
//      Printer.print("enemyToAttack=null" + ", enemies can deal: " + enemyDamage);
    } else {
//      Printer.print("enemyToAttack loc:" + enemyToAttack.location + ", enemies can deal: " + enemyDamage);
    }

    if (enemyDamage > rc.getHealth() - 1) {
//      Printer.print("attack bc no option!!");
      return enemyToAttack;
    }
//    Printer.print("I not die, return null");
    return null;
  }

  private void updateLastEnemy() {
    // get nearest enemy
    // set fleeing to 6
    // todo: consider whether or not to run away from enemy carriers
    MapLocation nearestCombatEnemy = null;
    int distanceToEnemy = Integer.MAX_VALUE;
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemyRobot.type == RobotType.LAUNCHER) {
        int dist = rc.getLocation().distanceSquaredTo(enemyRobot.location);
        if (dist < distanceToEnemy) {
          nearestCombatEnemy = enemyRobot.location;
          distanceToEnemy = dist;
        }
      }
    }
//    Printer.print("updateLastEnemy()");
    if (nearestCombatEnemy != null) {
//      Printer.print("lastEnemyLocation=" + lastEnemyLocation);
      lastEnemyLocation = nearestCombatEnemy;
      fleeingCounter = 6;
    } else {
      fleeingCounter = 0;
    }
  }

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
