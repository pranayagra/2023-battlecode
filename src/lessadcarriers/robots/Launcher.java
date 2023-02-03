package lessadcarriers.robots;

import lessadcarriers.containers.LinkedList;
import lessadcarriers.robots.micro.MicroConstants;
import lessadcarriers.communications.CommsHandler;
import lessadcarriers.communications.Communicator;
import lessadcarriers.communications.HqMetaInfo;
import lessadcarriers.containers.HashSet;
import lessadcarriers.knowledge.Cache;
import lessadcarriers.knowledge.RunningMemory;
import lessadcarriers.robots.micro.AttackMicro;
import lessadcarriers.robots.micro.AttackerFightingMicro;
import lessadcarriers.robots.pathfinding.SmitePathing;
import lessadcarriers.utils.Constants;
import lessadcarriers.utils.Printer;
import lessadcarriers.utils.Utils;
import battlecode.common.*;

public class Launcher extends MobileRobot {
  private static final int TURNS_TO_WAIT_SINCE_ENEMY_SEEN = 6;
  private static int MIN_GROUP_SIZE_TO_MOVE = 3; // min group size to move out TODO: done hacky
  private static final int TURNS_TO_WAIT = 15; // turns to wait (without friends) until going back to nearest HQ
  private static final int TURNS_AT_TARGET = 8; // how long to delay at each patrol target
  private static final int TURNS_AT_WELL = 3; // how long to delay at each patrol target
  private static final int MIN_HOT_SPOT_GROUP_SIZE = 5; // min group size to move to hot spot
  private static final int TURNS_AT_HOT_SPOT = 7;
  private static final int TURNS_AT_HOT_WELL = TURNS_AT_WELL * 3 / 2;
  private static final int TURNS_AT_FIGHT = 3;
  private static final int MAX_LAUNCHER_TASKS = 10;

  private int numTurnsWaitingForFriends = 0;

  private final LauncherTask[] launcherTaskStack;
  private int launcherTaskStackPointer;
  private LauncherTask currentTask;
  private HashSet<MapLocation> visitedLocations;

  // enemy state information
  private boolean launcherInVision;
  private boolean carrierInVision;
  private boolean carrierInAttackRange;
  private MapLocation lastAttackedLocation;
  private MapLocation lastEnemyLocation;
  private int roundLastEnemySeen;

  private int turnsInCloud;


  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
    LauncherTask.parentLauncher = this;
    LauncherTask.rc = rc;
    resetVisited();
    launcherTaskStack = new LauncherTask[MAX_LAUNCHER_TASKS];
    launcherTaskStackPointer = -1;
    setupInitialLauncherTask();
    turnsInCloud = 0;
  }

  private void resetVisited() {
    visitedLocations = new HashSet<>(HqMetaInfo.hqCount + CommsHandler.ADAMANTIUM_WELL_SLOTS + CommsHandler.MANA_WELL_SLOTS + CommsHandler.ELIXIR_WELL_SLOTS);
  }

  @Override
  protected void runTurn() throws GameActionException {
    Printer.appendToIndicator("launcher");
    if (rc.canWriteSharedArray(0, 0)) {
      CommsHandler.writeNumLaunchersIncrement();
    }
//    int manaIncome = CommsHandler.readOurHqManaIncome(HqMetaInfo.getClosestHQ(Cache.PerTurn.CURRENT_LOCATION));
    int manaIncome = Communicator.getTotalCarriersMiningType(ResourceType.MANA);
    if (manaIncome > 8) {
      MIN_GROUP_SIZE_TO_MOVE = (manaIncome / 8) + 3;
    } else {
      MIN_GROUP_SIZE_TO_MOVE = 3;
    }
    if (rc.senseCloud(Cache.PerTurn.CURRENT_LOCATION)) {
      switch (++turnsInCloud) {
//        case 1: case 2: case 3:
//          MIN_GROUP_SIZE_TO_MOVE = 2;
//          break;
        default:
          MIN_GROUP_SIZE_TO_MOVE = 1;
          break;
      }
    } else {
      turnsInCloud = 0;
    }
//    if (Cache.PerTurn.ROUND_NUM < 1000) {
//      MIN_GROUP_SIZE_TO_MOVE = 1;
//    }

    //TODO: refactor this out
    updateEnemyStateInformation();

    if (!launcherInVision) {
      if (shouldHeal) {
        tryAttack(true);
      }
      if (healingProtocol()) {
        tryAttack(true);
        tryAttack(false);
        return;
      }
    }

    if (!launcherInVision) {
      if (carrierInAttackRange) {
        // attack carrier in action radius and disable moving
        // TODO: let's consider moving forwards?
        MapLocation locationToAttack = bestCarrierInAction();
        if (attack(locationToAttack)) {
          return;
        }
      } else if (carrierInVision) {
        // move towards
        Direction direction = bestCarrierInVision();
        if (direction != null) {
          if (pathing.move(direction)) {
            updateEnemyStateInformation();
            if (!launcherInVision && carrierInAttackRange) {
              MapLocation locationToAttack = bestCarrierInAction();
              if (attack(locationToAttack)) {
                return;
              }
            }
          }
        }
      }
    }

    tryAttack(true);

    int maxTaskChanges = 5;
    while (currentTask.update() && maxTaskChanges-- > 0) {
      popLauncherTask();
    }
    if (maxTaskChanges <= 0) {
      Printer.print("Launcher task stack looping too much");
    }

    // do micro -- returns true if we did micro -> should not do exploration/patrolling behavior
    boolean didAnyMicro = false;
    while (AttackerFightingMicro.doMicro()) {
      didAnyMicro = true;
      Printer.appendToIndicator("micro'd.");
      tryAttack(false);
    }
    if (didAnyMicro) {
      currentTask.numTurnsNearTarget = 0;
      addFightTask(lastAttackedLocation != null ? lastAttackedLocation : Cache.PerTurn.CURRENT_LOCATION);
    } else {
      MapLocation target = getDestination();
      if (target != null) {
        /*BASICBOT_ONLY*///rc.setIndicatorDot(target, 0, 0, 255);
        attemptMoveTowards(target);
      }
    }

    tryAttack(false);

    if (Cache.PerTurn.ROUND_NUM >= 1800 && Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length >= 40 && Utils.rng.nextInt(10) <= 3) {
      rc.disintegrate();
    }
  }

  private MapLocation closestNonAdjIslandFriend() throws GameActionException {
    int distance = Integer.MAX_VALUE;
    MapLocation closestFriend = null;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (robot.type == RobotType.LAUNCHER) {
        int dist = robot.location.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
        int islandId = rc.senseIsland(robot.location);
        if (islandId == -1 || rc.senseTeamOccupyingIsland(islandId) != Cache.Permanent.OUR_TEAM) continue;
        if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(robot.location) && dist < distance) {
          distance = dist;
          closestFriend = robot.location;
        }
      }
    }
    return closestFriend;
  }

  // ISLAND STUFF
  private boolean ownIsland(MapLocation location) throws GameActionException {
    int islandID = rc.senseIsland(location);
    return (islandID >= 0 && rc.senseTeamOccupyingIsland(islandID) == Cache.Permanent.OUR_TEAM);
  }

  private void microIsland() throws GameActionException {
    if (!rc.isMovementReady()) return;

    MapLocation closestFriend = closestNonAdjIslandFriend();

    LinkedList<Direction> allDirections = new LinkedList<>();
//    List<Direction> allDirections = new ArrayList<>();
    if (closestFriend == null) {
      for (Direction dir : Utils.directions) {
        if (!rc.canMove(dir)) continue;
        if (ownIsland(Cache.PerTurn.CURRENT_LOCATION.add(dir))) {
          allDirections.add(dir);
        }
      }
    } else {
      int bestDistance = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(closestFriend);
      for (Direction dir : Utils.directions) {
        if (!rc.canMove(dir)) continue;
        MapLocation candidateLocation = Cache.PerTurn.CURRENT_LOCATION.add(dir);
        if (!ownIsland(candidateLocation)) continue;
        int candidateDistance = candidateLocation.distanceSquaredTo(closestFriend);
        if (candidateDistance < bestDistance) {
          allDirections.clear();
          allDirections.add(dir);
          bestDistance = candidateDistance;
        } else if (candidateDistance == bestDistance) {
          allDirections.add(dir);
        }
      }
    }
    if (allDirections.size > 0) {
      Direction dir = allDirections.get(Utils.rng.nextInt(allDirections.size));
      pathing.move(dir);
    }
  }
  private boolean shouldHeal;

  /**
   * does healing logic -- going to closest island when low health
   * @return true if we are healing
   * @throws GameActionException
   */
  private boolean healingProtocol() throws GameActionException {
    //todo: not complete
    if (Cache.PerTurn.HEALTH < RobotType.LAUNCHER.health * 0.5) {
      shouldHeal = true;
    } else if (Cache.PerTurn.HEALTH == RobotType.LAUNCHER.health) {
      shouldHeal = false;
    }

    if (!shouldHeal) return false;

    // island micro (I am on my island)
    if (ownIsland(Cache.PerTurn.CURRENT_LOCATION)) {
      microIsland();
      return true;
    }

    // find closest island and go towards it
    IslandInfo closestFriendlyIsland = getClosestFriendlyIsland();
    if (closestFriendlyIsland == null) {
//      Printer.appendToIndicator("no healing :(");
      return false;
    }
//    Printer.appendToIndicator("healing=" + closestFriendlyIsland.islandLocation);
    MapLocation islandTargetLocation = closestFriendlyIsland.updateLocationToClosestOpenLocation(Cache.PerTurn.CURRENT_LOCATION);
    pathing.moveTowards(islandTargetLocation);
    return true;
  }

  private Direction bestCarrierInVision() {
    MapLocation bestCarrierLocationToAttack = null;
    Direction bestDirection = null;
    int bestScore = Integer.MIN_VALUE;
    int myDamage = Cache.Permanent.ROBOT_TYPE.damage;
    for (Direction dir : Utils.directions) {
      if (!rc.canMove(dir)) continue;
      MapLocation newLoc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
      for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
        if (robot.type != RobotType.CARRIER) continue;
        if (!robot.location.isWithinDistanceSquared(newLoc, Cache.Permanent.ACTION_RADIUS_SQUARED)) continue;
        int score = 0;
        if (robot.health <= myDamage) {
          score += 100;
          score += robot.health;
        } else {
          score -= robot.health;
        }
        if (score > bestScore) {
          bestScore = score;
          bestCarrierLocationToAttack = robot.location;
          bestDirection = dir;
        } else if (score == bestScore && dir.getDirectionOrderNum() % 2 == 1) {
          bestCarrierLocationToAttack = robot.location;
          bestDirection = dir;
        }
      }
    }
    return bestDirection;
  }

  /**
   * Should pre-check that there exists a carrier to attack in action range.
   * @return the best carrier in action range to attack
   */
  private MapLocation bestCarrierInAction() {
    MapLocation bestCarrierLocationToAttack = null;
    int bestScore = Integer.MIN_VALUE;
    int myDamage = rc.getType().damage;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (robot.type != RobotType.CARRIER) continue;
      if (!robot.location.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED)) continue;

      int score = 0;
      if (robot.health <= myDamage) {
        score += 100;
        score += robot.health;
      } else {
        score -= robot.health;
      }
      if (score > bestScore) {
        bestScore = score;
        bestCarrierLocationToAttack = robot.location;
      }
    }
    return bestCarrierLocationToAttack;
  }

  /**
   * Updates relevant state variables about the enemies. Currently this is:
   * - launcherInVision
   * - carrierInVision
   * - carrierInAttackRange
   */
  private void updateEnemyStateInformation() {
    launcherInVision = false;
    carrierInVision = false;
    carrierInAttackRange = false;
    MapLocation closestEnemy = null;
    int closestEnemyDistance = Integer.MAX_VALUE;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      switch (robot.type) {
        case HEADQUARTERS:
          continue;
        case LAUNCHER:
          launcherInVision = true;
        case DESTABILIZER:
          int dist = robot.location.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
          if (dist < closestEnemyDistance) {
            closestEnemyDistance = dist;
            closestEnemy = robot.location;
          }
          break;
        case CARRIER:
          carrierInVision = true;
          if (robot.location.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED)) {
            carrierInAttackRange = true;
          }
          break;
      }
    }
    if (closestEnemy != null) {
      lastEnemyLocation = closestEnemy;
      roundLastEnemySeen = Cache.PerTurn.ROUND_NUM;
    }
  }

  /**
   * This gets the closest enemyHQLocation based on first vision, then comms.
   * @return the closest enemyHQLocation. NULL if there are enemies nearby!
   * @throws GameActionException
   */
  private MapLocation getClosestEnemyHQIfNoEnemies() throws GameActionException {
    MapLocation closestEnemyHQ = null;
    int distToClosestEnemyHQ = Integer.MAX_VALUE;
    for (RobotInfo e : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (e.type != RobotType.HEADQUARTERS) {
        return null;
      }
      int dist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(e.location);
      if (dist < distToClosestEnemyHQ) {
        closestEnemyHQ = e.location;
        distToClosestEnemyHQ = dist;
      }
    }
    if (closestEnemyHQ == null) return HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    return closestEnemyHQ;
  }

  /**
   * computes the destination for this robot to move towards:
   * run away from enemyHq if it is the only enemy
   * chase nearby enemies if needed
   * approach enemy closest to our HQ
   * do exploration in early game -- TODO: probably remove
   * do patrolling (visit hot spots in order)
   * @return the destination to move towards. Null if the action was handled inside :)
   * @throws GameActionException any exception while calculating destination
   */
  private MapLocation getDestination() throws GameActionException {
    if (lastEnemyLocation != null && (Cache.PerTurn.ROUND_NUM - roundLastEnemySeen) < TURNS_TO_WAIT_SINCE_ENEMY_SEEN) {
      int avgX = Cache.PerTurn.CURRENT_LOCATION.x;
      int avgY = Cache.PerTurn.CURRENT_LOCATION.y;
      int num = 1;
      for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
        switch (robot.type) {
          case LAUNCHER:
          case DESTABILIZER:
            avgX += robot.location.x;
            avgY += robot.location.y;
            num++;
            break;
        }
      }
      return new MapLocation(avgX / num, avgY / num);
    }

    MapLocation destination = null;
    // If enemyHQ only enemy near us and we are in range, walk away from it.
    MapLocation closestEnemyHQ = getClosestEnemyHQIfNoEnemies();
    if (closestEnemyHQ != null && Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(closestEnemyHQ, RobotType.HEADQUARTERS.actionRadiusSquared)) {
      Printer.appendToIndicator("moving away from enemy HQ:" + closestEnemyHQ);
      Direction awayFromEnemyHQ = closestEnemyHQ.directionTo(Cache.PerTurn.CURRENT_LOCATION);
      return Cache.PerTurn.CURRENT_LOCATION.translate(awayFromEnemyHQ.dx*5, awayFromEnemyHQ.dy*5);
    }

    // if one of our friends got hurt, go to him
//    destination = AttackMicro.updateAndGetInjuredAllyTarget();
    if (destination != null) {
      Printer.appendToIndicator("going to injured ally: " + destination);
      return destination;
    }

    // immediately adjacent location to consider -> will chase a valid enemy if necessary
    destination = AttackMicro.getBestMovementPosition();
    if (destination != null) {
      Printer.appendToIndicator("chasing enemy: " + destination);
      return destination;
    }

    if (currentTask.type != PatrolTargetType.HOT_SPOT_FIGHT) { // not patrolling a hot spot - consider defending home
      // closest enemy to our closest HQ -- friendlies in danger -> will come back to protect HQ
      destination = Communicator.getClosestEnemy(HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION));
      if (destination != null) {
        MapLocation closestHq = HqMetaInfo.getClosestHqLocation(destination);
        if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(destination,
            Math.max(
                destination.distanceSquaredTo(closestHq)*2,
                200
            ))) {
//          addFightTask(destination);
          Printer.appendToIndicator("defending HQ " + closestHq + " from closest commed enemy: " + destination);
          /*BASICBOT_ONLY*///rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, destination, 255, 100, 100);
          return destination;
        }
//        return destination;
      }
    }

    // do actual patrolling -> cycle between different enemy hotspots (wells / HQs)
    if (currentTask.turnsSinceClosest >= 20) {
      SmitePathing.forceOneBug = true;
    }
    return getPatrolTarget(); //shouldn't return null
  }

  /**
   * does some checks and determines where to patrol to
   * based on the current patrol type and ally/enemy counts
   * @return the location to patrol to
   * @throws GameActionException any issues with
   */
  private MapLocation getPatrolTarget() throws GameActionException {
    MapLocation patrolTarget = currentTask.patrolLocation;
    final MapLocation myLocation = Cache.PerTurn.CURRENT_LOCATION;
    final int myDistToTarget = myLocation.distanceSquaredTo(patrolTarget);

//    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    RobotInfo[] alliedRobots = Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS;
    MapLocation closestFriendToTargetLoc = myLocation;
    int closestFriendDistToTargetDist = myDistToTarget;
    int closestFriendID = Cache.Permanent.ID;

    // nearbyAllyLaunchers
    int adjacentAllyLaunchers = 0;
    int nearbyAllyLaunchers = 0;
    int totalAllyLaunchers = 0;
    int numCloserAllies = 0;
    for (RobotInfo ally : alliedRobots) {
      if (ally.type == RobotType.LAUNCHER) {
        totalAllyLaunchers++;
        int friendToTargetDist = ally.location.distanceSquaredTo(patrolTarget);
        if (friendToTargetDist < closestFriendDistToTargetDist) {
          closestFriendToTargetLoc = ally.location;
          closestFriendDistToTargetDist = friendToTargetDist;
          closestFriendID = ally.ID;
//        } else if (friendToTargetDist == closestFriendDistToTargetDist && ally.ID < closestFriendID) {
//          closestFriendToTargetLoc = ally.location;
//          closestFriendID = ally.ID;
        }
        if (ally.location.isWithinDistanceSquared(myLocation, Utils.DSQ_1by1)) {
          adjacentAllyLaunchers++;
          nearbyAllyLaunchers++;
        } else if (ally.location.isWithinDistanceSquared(myLocation, Utils.DSQ_2by2)) {
          nearbyAllyLaunchers++;
        }
        if (friendToTargetDist < myDistToTarget || (friendToTargetDist == myDistToTarget && ally.ID < Cache.Permanent.ID)) {
          numCloserAllies++;
        }
      }
    }


    switch (myDistToTarget) {
      case 25: case 24: case 23: case 22: case 21: case 20: case 19: case 18: // 3by3 plus
      case 17:
        if (nearbyAllyLaunchers < 3) {
          break;
        }
      case 16: // launcher action
      case 15: case 14: case 13: case 12: case 11: case 10: case 9: case 8: // 2by2
      case 7: case 6: case 5: case 4: case 3: case 2: // 1by1 (adjacent)
      case 1: case 0:
        currentTask.numTurnsNearTarget++;
      default:
        break;
    }

    if (currentTask.type.isOurSide) {
      return patrolTarget;
    }

    // make sure we have friends
    if (nearbyAllyLaunchers < MIN_GROUP_SIZE_TO_MOVE - 1) { // 1 for self
      if (totalAllyLaunchers > 0) {
        if (!closestFriendToTargetLoc.isAdjacentTo(myLocation)) {
          // move towards friend closest to current target
          Printer.appendToIndicator("moving towards friend (" + nearbyAllyLaunchers + "," + totalAllyLaunchers + "," + (MIN_GROUP_SIZE_TO_MOVE-1) + ") at " + closestFriendToTargetLoc + "-target: " + patrolTarget + " -type=" + currentTask.type.name);
          return closestFriendToTargetLoc;
//        attemptMoveTowards(closestFriendToTargetLoc);
        } else if (closestFriendToTargetLoc.equals(myLocation)) {
          Printer.appendToIndicator("I'm the closest (" + nearbyAllyLaunchers + "," + totalAllyLaunchers + "," + (MIN_GROUP_SIZE_TO_MOVE-1) + "), staying still" + " -target: " + patrolTarget + " -type=" + currentTask.type.name);
          return closestFriendToTargetLoc;
        }
      }
      // stay still, not enough friends
      if (++numTurnsWaitingForFriends > turnsToWaitUntilRetreat()) {
        // go back to nearest HQ
        if (currentTask.numTurnsNearTarget > 0) {
          currentTask.numTurnsNearTarget -= (MIN_GROUP_SIZE_TO_MOVE - totalAllyLaunchers);
          if (currentTask.numTurnsNearTarget < 0) currentTask.numTurnsNearTarget = 0;
        }
        MapLocation closestHq = HqMetaInfo.getClosestHqLocation(myLocation);
        Printer.appendToIndicator("retreating towards HQ: " + closestHq);
        return closestHq;
//        Printer.appendToIndicator("no friends - just go to target");
//        return patrolTarget;
      } else {
//        return explorationTarget; // explore? still while waiting
        Printer.appendToIndicator("waiting for friends");
        return closestFriendToTargetLoc;//Cache.PerTurn.CURRENT_LOCATION; // stay still while waiting
      }
    } else {
      // ayy we got friends, now we can go!
      numTurnsWaitingForFriends = 0;
      // TODO if we're closest to the target, don't move
      Direction toTarget = myLocation.directionTo(patrolTarget);
      if (closestFriendDistToTargetDist < myDistToTarget) { // someone else is closer
//        Printer.appendToIndicator("clump -" + currentTask.type.name + "@" + currentTask.targetLocation + "via-" + patrolTarget + "-turns@=" + currentTask.numTurnsNearTarget + "-turns close=" + currentTask.turnsSinceClosest);
        Printer.appendToIndicator("follow: " + closestFriendToTargetLoc + " -target: " + patrolTarget + "-turns close=" + currentTask.turnsSinceClosest);
////        return closestFriendToTargetLoc.add(closestFriendToTargetLoc.directionTo(patrolTarget));
//        MapLocation lineFormationCenter = closestFriendToTargetLoc;
//        MapLocation lineFormationPointLeft = lineFormationCenter;//.add(toTarget.rotateLeft());
//        MapLocation lineFormationPointRight = lineFormationCenter;//.add(toTarget.rotateRight());
////        Direction toTargetLeft = toTarget.rotateLeft();
////        Direction toTargetRight = toTarget.rotateRight();
//        MapLocation targetLocation = currentTask.targetLocation;
//        MapLocation closestLinePoint = null;
//        int closestLinePointDist = Integer.MAX_VALUE;
//        for (int i = 0; i < 10; i++) {
//          lineFormationPointLeft = lineFormationPointLeft.add(lineFormationPointLeft.directionTo(patrolTarget).rotateLeft().rotateLeft());
//          left_point: {
//            int dist = lineFormationPointLeft.distanceSquaredTo(myLocation);
//            if (dist > 0 && rc.canSenseLocation(lineFormationPointLeft) && (rc.isLocationOccupied(lineFormationPointLeft) || !rc.sensePassability(lineFormationPointLeft))) {
//              break left_point;
//            }
//            if (dist < closestLinePointDist) {
//              closestLinePoint = lineFormationPointLeft;
//              closestLinePointDist = dist;
//            }
//          }
//          lineFormationPointRight = lineFormationPointRight.add(lineFormationPointRight.directionTo(patrolTarget).rotateRight().rotateRight());
//          right_point: {
//            int dist = lineFormationPointRight.distanceSquaredTo(myLocation);
//            if (dist > 0 && rc.canSenseLocation(lineFormationPointRight) && (rc.isLocationOccupied(lineFormationPointRight) || !rc.sensePassability(lineFormationPointRight))) {
//              break right_point;
//            }
//            if (dist < closestLinePointDist) {
//              closestLinePoint = lineFormationPointRight;
//              closestLinePointDist = dist;
//            }
//          }
//          if (i % 2 == 0) {
//            if (closestLinePoint != null) {
//              break;
//            }
//            lineFormationPointLeft = lineFormationPointRight = lineFormationCenter = lineFormationCenter.subtract(toTarget);
//          }
//        }
//        if (closestLinePoint != null) {
//          MapLocation shiftedPatrol = patrolTarget.translate(closestLinePoint.x - closestFriendToTargetLoc.x, closestLinePoint.y - closestFriendToTargetLoc.y);
//          return shiftedPatrol; //closestLinePoint;
//        }
//        MapLocation shiftedPatrol = patrolTarget.translate(myLocation.x - closestFriendToTargetLoc.x, myLocation.y - closestFriendToTargetLoc.y);
//        return shiftedPatrol;
        return patrolTarget;
      } else { // i'm the closest
//        Printer.appendToIndicator("advance clump -> " + currentTask.type.name + "@" + currentTask.targetLocation + "via-" + patrolTarget + " - turns@target:" + currentTask.numTurnsNearTarget);
        int friendsInLine = 0;
        MapLocation lineFormationCenter = closestFriendToTargetLoc;
        MapLocation lineFormationPointLeft = lineFormationCenter;//.add(toTarget.rotateLeft());
        MapLocation lineFormationPointRight = lineFormationCenter;//.add(toTarget.rotateRight());
//        Direction toTargetLeft = toTarget.rotateLeft();
//        Direction toTargetRight = toTarget.rotateRight();
        MapLocation targetLocation = currentTask.targetLocation;
        MapLocation closestLinePoint = null;
        int closestLinePointDist = Integer.MAX_VALUE;
        for (int i = 0; i < 10; i++) {
          any_friends: {
            lineFormationPointLeft = lineFormationPointLeft.add(lineFormationPointLeft.directionTo(patrolTarget).rotateLeft().rotateLeft());
            left_point: {
              if (rc.canSenseLocation(lineFormationPointLeft) && rc.isLocationOccupied(lineFormationPointLeft)) {
                RobotInfo robotInfo = rc.senseRobotAtLocation(lineFormationPointLeft);
                if (robotInfo.team == Cache.Permanent.OUR_TEAM && AttackMicro.isAttacker(robotInfo.type)) {
                  friendsInLine++;
                  break left_point;
                }
              }
              break any_friends;
            }
            lineFormationPointRight = lineFormationPointRight.add(lineFormationPointRight.directionTo(patrolTarget).rotateRight().rotateRight());
            right_point: {
              if (rc.canSenseLocation(lineFormationPointRight) && rc.isLocationOccupied(lineFormationPointRight)) {
                RobotInfo robotInfo = rc.senseRobotAtLocation(lineFormationPointRight);
                if (robotInfo.team == Cache.Permanent.OUR_TEAM && AttackMicro.isAttacker(robotInfo.type)) {
                  friendsInLine++;
                  break right_point;
                }
              }
              break any_friends;
            }
          }
          if (i % 2 == 0) {
            lineFormationPointLeft = lineFormationPointRight = lineFormationCenter = lineFormationCenter.subtract(toTarget);
          }
        }

        Printer.appendToIndicator("advance -> " + currentTask.type.name + "@" + currentTask.targetLocation + " - turns=" + currentTask.numTurnsNearTarget + " |line|=" + friendsInLine);
        return MIN_GROUP_SIZE_TO_MOVE == 1 ? patrolTarget : myLocation; //adjacentAllyLaunchers >= (MIN_GROUP_SIZE_TO_MOVE - 1)*0.75 ? patrolTarget : myLocation;
//        return friendsInLine >= (MIN_GROUP_SIZE_TO_MOVE - 1) ? patrolTarget : myLocation;
      }
//      return patrolTarget;
    }
  }

  /**
   * Idea: the lower health we are, go back earlier to regroup
   * @return the turns to wait based on current health.
   */
  private int turnsToWaitUntilRetreat() {
    return TURNS_TO_WAIT;
//    double healthPercentage = (Cache.PerTurn.HEALTH / (double) Cache.Permanent.MAX_HEALTH);
//    if (healthPercentage < 0.5 /* immediately go home */) return 0;
//    return (int) (healthPercentage * TURNS_TO_WAIT);
  }

  /**
   * determines if there is a hot spot we need to visit when spawned
   * @throws GameActionException any issues with figuring out a hot spot
   */
  private void setupInitialLauncherTask() throws GameActionException {
    addLauncherTask(new LauncherTask(PatrolTargetType.DEFAULT_FIRST_TARGET, null, null));

    MapLocation[] endangeredWellPair = Communicator.closestAllyEnemyWellPair();
    MapLocation mostEndangeredWell = endangeredWellPair[0];
    MapLocation mostEndangeredEnemyWell = endangeredWellPair[1];
    if (mostEndangeredWell != null) {
      int mostEndangeredDist = mostEndangeredWell.distanceSquaredTo(mostEndangeredEnemyWell);
//    Printer.print("endangered wells found! " + mostEndangeredWell + " - " + mostEndangeredEnemyWell + " dist:" + mostEndangeredDist);
      if (mostEndangeredDist <= Constants.ENDANGERED_WELL_DIST) {
        //      Printer.print("endangered wells found! " + mostEndangeredWell + " - " + mostEndangeredEnemyWell + " dist:" + mostEndangeredDist);
        Direction oursToTheirs = mostEndangeredWell.directionTo(mostEndangeredEnemyWell);
        MapLocation toGuard = mostEndangeredWell.add(oursToTheirs).add(oursToTheirs);
        //      Printer.print("need to guard hotspot - save last target -- " + patrolTargetType + ": " + patrolTarget);
        addLauncherTask(new LauncherTask(PatrolTargetType.HOT_SPOT_WELL_DEFENSE, mostEndangeredWell, toGuard));
      }
    }

//    MapLocation launcherMidpoint = CommsHandler.readLauncherMidpointLocation();
//    if (!launcherMidpoint.equals(CommsHandler.NONEXISTENT_MAP_LOC)) {
//      addLauncherTask(new LauncherTask(PatrolTargetType.HOT_SPOT_FIGHT, launcherMidpoint, launcherMidpoint));
//    }
  }

  private void addLauncherTask(LauncherTask launcherTask) {
    if (launcherTask == null) {
      return;
    }
    if (launcherTaskStackPointer < MAX_LAUNCHER_TASKS-1) {
      launcherTaskStack[++launcherTaskStackPointer] = launcherTask;
    } else {
      Printer.print("launcher task stack overflow");
      for (int i = launcherTaskStackPointer; --i >= 0;) {
        Printer.print(i  + ": " + launcherTaskStack[i].type.name + " - " + launcherTaskStack[i].targetLocation);
      }
//      die();
      launcherTaskStack[launcherTaskStackPointer] = launcherTask;
    }
    currentTask = launcherTask;
  }
  private void popLauncherTask() throws GameActionException {
    launcherTaskStack[launcherTaskStackPointer--] = null;
    while (launcherTaskStackPointer >= 0 && (launcherTaskStack[launcherTaskStackPointer] == null)) {// || launcherTaskStack[launcherTaskStackPointer].type == PatrolTargetType.HOT_SPOT_FIGHT)) {
      launcherTaskStack[launcherTaskStackPointer--] = null;
    }
    if (launcherTaskStackPointer <= -1) {
      addLauncherTask(new LauncherTask(PatrolTargetType.DEFAULT_FIRST_TARGET, null, null));
    }
    currentTask = launcherTaskStack[launcherTaskStackPointer];
    currentTask.numTurnsNearTarget = 0;
  }
  private void addFightTask(MapLocation fightLocation) throws GameActionException {
    if (currentTask != null && currentTask.type == PatrolTargetType.HOT_SPOT_FIGHT && currentTask.targetLocation.isWithinDistanceSquared(fightLocation, Cache.Permanent.VISION_RADIUS_SQUARED)) return;

//    Printer.print("adding fight task");
//    for (int i = launcherTaskStackPointer; --i >= 0;) {
//      Printer.print(i  + ": " + launcherTaskStack[i].type.name + " - " + launcherTaskStack[i].targetLocation);
//    }
//      MapLocation enemyLocation = lastAttackedLocation != null ? lastAttackedLocation : Cache.PerTurn.CURRENT_LOCATION;
//      // average of self and enemy
    Direction toSelf = fightLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION);
    int scalar = 3;
    if (fightLocation.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < Cache.Permanent.VISION_RADIUS_SQUARED*2) {
      scalar = 2;
    }
    MapLocation toPatrolFrom = fightLocation.translate(toSelf.dx * scalar, toSelf.dy * scalar);
    if (currentTask != null && currentTask.type == PatrolTargetType.HOT_SPOT_FIGHT && currentTask.targetLocation.isWithinDistanceSquared(toPatrolFrom, Cache.Permanent.ACTION_RADIUS_SQUARED)) return;
//      addLauncherTask(new LauncherTask(PatrolTargetType.HOT_SPOT_FIGHT, enemyLocation, new MapLocation(toPatrolFrom.x / 2, toPatrolFrom.y / 2)));
    addLauncherTask(new LauncherTask(PatrolTargetType.HOT_SPOT_FIGHT, fightLocation, toPatrolFrom));
  }

  public enum PatrolTargetType {
    OUR_HQ("OUR_HQ", true, false, Launcher.TURNS_AT_TARGET, false),
    OUR_WELL("OUR_WELL", true, false, Launcher.TURNS_AT_WELL, true),
    ENEMY_ISLAND("ENEMY_ISLAND", false, false, Launcher.TURNS_AT_TARGET, false),
    ENEMY_WELL("ENEMY_WELL", false, false, Launcher.TURNS_AT_WELL, true),
    ENEMY_HQ("ENEMY_HQ", false, false, Launcher.TURNS_AT_TARGET, true),
    HOT_SPOT_WELL_DEFENSE("HOT_WELL", false, true, Launcher.TURNS_AT_HOT_WELL, false),
    HOT_SPOT_FIGHT("HOT_FIGHT", false, true, Launcher.TURNS_AT_FIGHT, false);

    public static final PatrolTargetType DEFAULT_FIRST_TARGET = ENEMY_WELL;
    public static final PatrolTargetType TARGET_ON_CYCLE = ENEMY_WELL;

    public final boolean isOurSide;
    public final boolean isHotSpot;
    public final int numTurnsToStayAtTarget;
    public final boolean updateOnSymmetryChange;
    public final String name;

    PatrolTargetType(String name, boolean isOurSide, boolean isHotSpot, int numTurnsToStayAtTarget, boolean updateOnSymmetryChange) {
      this.name = name;
      this.isOurSide = isOurSide;
      this.isHotSpot = isHotSpot;
      this.numTurnsToStayAtTarget = numTurnsToStayAtTarget;
      this.updateOnSymmetryChange = updateOnSymmetryChange;
    }
  }

  private static class LauncherTask {
    private static Launcher parentLauncher;
    private static RobotController rc;

    PatrolTargetType type;
    MapLocation patrolLocation;
    MapLocation targetLocation;
    Utils.MapSymmetry symmetryOnSet;

    int numTurnsNearTarget;

    int closestDistanceToPatrolLocation;
    int turnsSinceClosest;

    private LauncherTask(PatrolTargetType type, MapLocation targetLocation, MapLocation patrolLocation) throws GameActionException {
      this.type = type;
      this.numTurnsNearTarget = 0;
      closestDistanceToPatrolLocation = Integer.MAX_VALUE;
      turnsSinceClosest = 0;
      if (patrolLocation != null) {
        this.targetLocation = targetLocation;
        setNewPatrolLocation(patrolLocation);
      } else {
        update();
      }
    }

    /**
     * updates the patrol target to go to if needed
     * @return true if this task is complete
     * @throws GameActionException any issues with sensing and updating
     */
    private boolean update() throws GameActionException {
      update_symmetry: if (targetLocation != null && rc.canSenseLocation(targetLocation)) {
        switch (type) {
          case ENEMY_HQ:
            if (!HqMetaInfo.getClosestEnemyHqLocation(targetLocation).equals(targetLocation)) {
              break; // we no longer think there should be an enemy HQ here
            }
            RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
            if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
//            Printer.print("ERROR: expected enemy HQ is not an HQ " + patrolTarget, "symmetry guess must be wrong, eliminating symmetry (" + RunningMemory.guessedSymmetry + ") and retrying...");
//            Printer.print("Closest enemy HQ to patrol: " + HqMetaInfo.getClosestEnemyHqLocation(targetLocation));
              RunningMemory.markInvalidSymmetry(RunningMemory.guessedSymmetry);
              break;
            }
            break update_symmetry;
          case ENEMY_WELL:
            MapLocation closestEnemyWellLocation = Communicator.getClosestEnemyWellLocation(targetLocation, ResourceType.ADAMANTIUM);
            if (closestEnemyWellLocation == null || !closestEnemyWellLocation.equals(targetLocation)) {
              closestEnemyWellLocation = Communicator.getClosestEnemyWellLocation(targetLocation, ResourceType.MANA);
              if (closestEnemyWellLocation == null || !closestEnemyWellLocation.equals(targetLocation)) {
                closestEnemyWellLocation = Communicator.getClosestEnemyWellLocation(targetLocation, ResourceType.ELIXIR);
                if (closestEnemyWellLocation == null || !closestEnemyWellLocation.equals(targetLocation)) {
                  break; // we no longer think there should be an enemy well here
                }
              }
            }
            WellInfo well = rc.senseWell(targetLocation);
            if (well == null) {
//              Printer.print("ERROR: expected enemy well is not a well " + targetLocation, "symmetry guess must be wrong, eliminating symmetry (" + RunningMemory.guessedSymmetry + ") and retrying...");
              RunningMemory.markInvalidSymmetry(RunningMemory.guessedSymmetry);
              break;
            }
            break update_symmetry;
          default:
            break update_symmetry;
        }
        // if we're here, the target is messed up
        targetLocation = null;
        patrolLocation = null;
      }

      if (patrolLocation != null) {
        int distanceToPatrolLocation = Utils.maxSingleAxisDist(patrolLocation, Cache.PerTurn.CURRENT_LOCATION);
        if (distanceToPatrolLocation < closestDistanceToPatrolLocation) {
          closestDistanceToPatrolLocation = distanceToPatrolLocation;
          turnsSinceClosest = 0;
        } else {
          turnsSinceClosest++;
        }
      }

      at_target: if (numTurnsNearTarget > 0 && patrolLocation != null && numTurnsNearTarget < type.numTurnsToStayAtTarget) {
        Printer.appendToIndicator("Completing patrol " + type.name + "@" + targetLocation + " (via: " + patrolLocation + ")" + " --turns=" + numTurnsNearTarget);
        return false; // exit early if we're near the patrol target -- finish patrolling
      }

      boolean isComplete = false;
      patrol_complete: if (patrolLocation != null && numTurnsNearTarget >= type.numTurnsToStayAtTarget * 0.7) {
        if (type.isHotSpot && type != PatrolTargetType.HOT_SPOT_FIGHT) {
          // need to be more careful with hotspots, wait for more friends
          if (Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length < MIN_HOT_SPOT_GROUP_SIZE) {
            numTurnsNearTarget /= 2;
            break patrol_complete;
          }
        }
        if (numTurnsNearTarget < type.numTurnsToStayAtTarget) {
          break patrol_complete;
        }
        isComplete = true;
      }

      patrol_giveup: if (turnsSinceClosest > Math.max(50, closestDistanceToPatrolLocation * MicroConstants.TURNS_SCALAR_TO_GIVE_UP_ON_TARGET_APPROACH)) {
        // we've been stuck for a while, give up
        isComplete = true;
      }

      if (isComplete) {
        numTurnsNearTarget = 0;
        // mark the current target as visited
        if (targetLocation != null) {
          parentLauncher.visitedLocations.add(targetLocation);
//          if (Cache.Permanent.ID == 10819) {
//            Printer.print("Done patrolling " + type.name + "@" + targetLocation + " (via: " + patrolLocation + ")");
//            Printer.print("Visited locations: " + parentLauncher.visitedLocations);
//          }
          return true;
        }
      }

      // update if we haven't gotten near the target
      if ((numTurnsNearTarget == 0 && type.updateOnSymmetryChange && symmetryOnSet != RunningMemory.guessedSymmetry) || patrolLocation == null) { // we are done patrolling the current location
        // update the target if needed
        switch (type) {
          case OUR_HQ:
//            patrolTargetType = PatrolTargetType.OUR_HQ;
            targetLocation = parentLauncher.determinePatrollingTargetLocationOurHq();
            if (targetLocation != null) {
              Direction randomDir = Utils.randomDirection();
              setNewPatrolLocation(targetLocation.add(randomDir).add(randomDir));
              Printer.appendToIndicator("patrolling our hq " + targetLocation + " - from " + patrolLocation);
              break;
            }
          case OUR_WELL: our_well: {
            if (true) break our_well;
            type = PatrolTargetType.OUR_WELL;
            targetLocation = parentLauncher.determinePatrollingTargetLocationOurWell();
            if (targetLocation != null) {
              Direction awayFromBase = HqMetaInfo.getClosestHqLocation(targetLocation).directionTo(targetLocation);
              setNewPatrolLocation(targetLocation.add(awayFromBase).add(awayFromBase));
              Printer.appendToIndicator("patrolling our well " + targetLocation + " - from " + patrolLocation);
              break; // switch to enemy well if all our wells are visited
            }
          }
          case ENEMY_WELL:
            type = PatrolTargetType.ENEMY_WELL;
//          Printer.print("trying to find enemy well for patrolling");
            targetLocation = parentLauncher.determinePatrollingTargetLocationEnemyWell();
            if (targetLocation != null) {
              Direction towardsEnemyBase = targetLocation.directionTo(HqMetaInfo.getClosestEnemyHqLocation(targetLocation));
              setNewPatrolLocation(targetLocation.add(towardsEnemyBase).add(towardsEnemyBase));
              Printer.appendToIndicator("patrolling enemy well " + targetLocation + " - from " + patrolLocation);
              break; // fall through to enemy HQ if no enemy well known
            }
          case ENEMY_HQ:
            type = PatrolTargetType.ENEMY_HQ;
            targetLocation = parentLauncher.determinePatrollingTargetLocationEnemyHq();
            if (targetLocation != null) {
              MapLocation newPatrolLocation = targetLocation;
              int tries = 10;
              while ((newPatrolLocation.isWithinDistanceSquared(targetLocation, RobotType.HEADQUARTERS.actionRadiusSquared) || !rc.onTheMap(newPatrolLocation)) && --tries >= 0) {
                newPatrolLocation = newPatrolLocation.add(Utils.randomDirection());
              }
              if (tries < 0) {
                Direction toSelf = newPatrolLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION);
                newPatrolLocation = targetLocation.translate(toSelf.dx*4, toSelf.dy*4);
              }
              setNewPatrolLocation(newPatrolLocation);
              Printer.appendToIndicator("patrolling enemy HQ " + targetLocation + " - from " + patrolLocation);
              break;
            }
//          Printer.print("ERROR: no closest enemy HQ found for patrol -- visited: " + visitedLocations);
          default:
            if (type.isHotSpot) {
              if (patrolLocation != null) {
                Printer.appendToIndicator("patrolling hot spot: " + patrolLocation);
                break;
              }
            } else {
              type = PatrolTargetType.TARGET_ON_CYCLE;
              parentLauncher.resetVisited();
              return update();
//            if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(explorationTarget, EXPLORATION_REACHED_RADIUS)) {
//              randomizeExplorationTarget(true);
//            }
//            parentLauncher.resetVisited();
//            Printer.appendToIndicator("patrolling default: " + explorationTarget);
//            patrolLocation = explorationTarget;
//            targetLocation = explorationTarget;
            }
            break;
        }
        if (patrolLocation == null) {
          Printer.print("Failed to select patrol target for type: " + type);
        }
      }
      /*BASICBOT_ONLY*///rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, patrolLocation, 200,200,200);
      return false;
    }

    private void setNewPatrolLocation(MapLocation newPatrolLocation) {
      if (newPatrolLocation != patrolLocation) {
        patrolLocation = newPatrolLocation;
        closestDistanceToPatrolLocation = Integer.MAX_VALUE;
        turnsSinceClosest = 0;
        numTurnsNearTarget = 0;
        symmetryOnSet = RunningMemory.guessedSymmetry;
//        Printer.print("New patrol location: " + patrolLocation,"reset turnsClosest");
      }
    }
  }

  private MapLocation determinePatrollingTargetLocationEnemyHq() {
//          patrolTarget = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    MapLocation[] enemyHQs = HqMetaInfo.enemyHqLocations;
    MapLocation closestHQ = null;
    int bestDist = 1000000;
    for (MapLocation enemyHQ : enemyHQs) {
      if (visitedLocations.contains(enemyHQ)) continue;
//            int dist = enemyHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
      int dist = Utils.applySymmetry(enemyHQ, RunningMemory.guessedSymmetry).distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
      if (dist < bestDist) {
        bestDist = dist;
        closestHQ = enemyHQ;
      }
    }
    return closestHQ;
  }

  private MapLocation determinePatrollingTargetLocationEnemyWell() throws GameActionException {
    MapLocation myLoc = Cache.PerTurn.CURRENT_LOCATION;
    int closestDist = Integer.MAX_VALUE;
    MapLocation closestEnemyWell = null;
    for (int i = 0; i < CommsHandler.MANA_WELL_SLOTS; i++) {
      if (CommsHandler.readManaWellExists(i)) {
        MapLocation wellLocation = CommsHandler.readManaWellLocation(i);
        if (!HqMetaInfo.isEnemyTerritory(wellLocation)) {
          wellLocation = Utils.applySymmetry(wellLocation, RunningMemory.guessedSymmetry);
        }
        if (visitedLocations.contains(wellLocation)) continue;
        int dist = myLoc.distanceSquaredTo(wellLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closestEnemyWell = wellLocation;
        }
      }
    }
    for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
      if (CommsHandler.readAdamantiumWellExists(i)) {
        MapLocation wellLocation = CommsHandler.readAdamantiumWellLocation(i);
        if (!HqMetaInfo.isEnemyTerritory(wellLocation)) {
          wellLocation = Utils.applySymmetry(wellLocation, RunningMemory.guessedSymmetry);
        }
        if (visitedLocations.contains(wellLocation)) continue;
        int dist = myLoc.distanceSquaredTo(wellLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closestEnemyWell = wellLocation;
        }
      }
    }
    for (int i = 0; i < CommsHandler.ELIXIR_WELL_SLOTS; i++) {
      if (CommsHandler.readElixirWellExists(i)) {
        MapLocation wellLocation = CommsHandler.readElixirWellLocation(i);
        if (!HqMetaInfo.isEnemyTerritory(wellLocation)) {
          wellLocation = Utils.applySymmetry(wellLocation, RunningMemory.guessedSymmetry);
        }
        if (visitedLocations.contains(wellLocation)) continue;
        int dist = myLoc.distanceSquaredTo(wellLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closestEnemyWell = wellLocation;
        }
      }
    }
    return closestEnemyWell;
  }

  private MapLocation determinePatrollingTargetLocationOurWell() throws GameActionException {
    ResourceType rt = ResourceType.ADAMANTIUM;
    if (Utils.rng.nextBoolean()) rt = ResourceType.MANA;
    MapLocation closestWellLocation = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, rt); // TODO: make pick mana vs ad
    return closestWellLocation;
  }

  private MapLocation determinePatrollingTargetLocationOurHq() {
    return HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
  }


  /**
   * Attempts to move towards the given location -- only allows movement on even turns
   * @param target where to move towards
   * @return true if moved
   * @throws GameActionException any issues with moving
   */
  private boolean attemptMoveTowards(MapLocation target) throws GameActionException {
    return (Cache.PerTurn.ROUND_NUM % 2 == 0)
        && pathing.moveTowards(target);
  }

  /**
   * will attempt to attack nearby enemies
   * will use cloud attack exploit only if allowed to attack non-attackers (onlyAttackers==false)
   * @param onlyAttackers if true, will only attack other attackers
   * @return true if we attacked someone
   * @throws GameActionException
   */
  boolean tryAttack(boolean onlyAttackers) throws GameActionException {
    if (!rc.isActionReady()) return false;
    MapLocation bestAttackTarget = AttackMicro.getBestAttackTarget(onlyAttackers);
    if (bestAttackTarget != null) {
      lastAttackedLocation = bestAttackTarget;
      return attack(bestAttackTarget);
    }
//    return false;
    boolean attacked = bestAttackTarget != null && attack(bestAttackTarget);
    if (onlyAttackers) return attacked;
    return attemptCloudAttack();
  }


  private boolean attack(MapLocation loc) throws GameActionException {
    // TODO: consider adding bytecode check here (like movement)
    if (rc.canAttack(loc)) {
      rc.attack(loc);
      return true;
    }
    return false;
  }

  private boolean attemptCloudAttack() throws GameActionException {
    if (!rc.isActionReady()) return false;
    MapLocation commedEnemy = Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
    if (commedEnemy != null && (!rc.canSenseLocation(commedEnemy) || (rc.canSenseRobotAtLocation(commedEnemy) && rc.senseRobotAtLocation(commedEnemy).team == Cache.Permanent.OPPONENT_TEAM))) {
      if (rc.canAttack(commedEnemy)) {
        if (commedEnemy.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, 2*Cache.Permanent.VISION_RADIUS_SQUARED)) {
          lastEnemyLocation = commedEnemy;
          roundLastEnemySeen = Cache.PerTurn.ROUND_NUM;
        }
        lastAttackedLocation = commedEnemy;
        rc.attack(commedEnemy);
        return true;
      }
    }
    if (!rc.isActionReady()) return false;
    if (lastAttackedLocation != null && (!rc.canSenseLocation(lastAttackedLocation) || (rc.canSenseRobotAtLocation(lastAttackedLocation) && rc.senseRobotAtLocation(lastAttackedLocation).team == Cache.Permanent.OPPONENT_TEAM)) && attack(lastAttackedLocation)) return true;
    if (!rc.isActionReady()) return false;
    if (lastEnemyLocation != null && (!rc.canSenseLocation(lastEnemyLocation) || (rc.canSenseRobotAtLocation(lastEnemyLocation) && rc.senseRobotAtLocation(lastEnemyLocation).team == Cache.Permanent.OPPONENT_TEAM)) && attack(lastEnemyLocation)) return true;
    if (!rc.isActionReady()) return false;

    if (Cache.PerTurn.IS_IN_CLOUD) {
      MapLocation targetEnemy = Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
      if (targetEnemy == null) targetEnemy = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
      Direction toEnemy = Cache.PerTurn.CURRENT_LOCATION.directionTo(targetEnemy);
      MapLocation target = Cache.PerTurn.CURRENT_LOCATION.add(toEnemy);
      while (target.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED)) {
        target = target.add(toEnemy);
      }
      target = target.add(toEnemy.opposite());
      if (attack(target)) return true;
    }

    if (!rc.isActionReady()) return false;
    MapLocation closeTo = lastAttackedLocation;
    if (closeTo == null || !closeTo.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED  * 2)) {
      closeTo = lastEnemyLocation;
      if (closeTo == null || !closeTo.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED  * 2)) {
        closeTo = Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
        if (closeTo == null) {
          closeTo = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        }
      }
    }
    MapLocation[] clouds = rc.senseNearbyCloudLocations(Cache.Permanent.ACTION_RADIUS_SQUARED);
    if (Clock.getBytecodesLeft() < clouds.length * 100) return attack(clouds[0]);
    MapLocation bestCloudToAttack = null;
    int bestCloudDist = Integer.MAX_VALUE;
    for (int i = clouds.length; --i >= 0;) {
      MapLocation loc = clouds[i];
      if (rc.canSenseLocation(loc) && (!rc.canSenseRobotAtLocation(loc) || rc.senseRobotAtLocation(loc).team == Cache.Permanent.OUR_TEAM)) continue;
      if (rc.canAttack(loc)) {
        int dist = closeTo.distanceSquaredTo(loc);
        if (dist < bestCloudDist) {
          bestCloudDist = dist;
          bestCloudToAttack = loc;
        }
      }
    }
    if (bestCloudToAttack != null && attack(bestCloudToAttack)) {
      return true;
    }
    return false;
  }
}
