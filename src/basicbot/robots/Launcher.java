package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.containers.HashSet;
import basicbot.knowledge.Cache;
import basicbot.knowledge.RunningMemory;
import basicbot.robots.micro.AttackMicro;
import basicbot.robots.micro.AttackerFightingMicro;
import basicbot.utils.Constants;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

public class Launcher extends MobileRobot {
  private static final int MIN_TURN_TO_MOVE = 0;
  private static final int MIN_GROUP_SIZE_TO_MOVE = 3; // min group size to move out
  private static final int TURNS_TO_WAIT = 15; // turns to wait (without friends) until going back to nearest HQ
  private static final int TURNS_AT_TARGET = 10; // how long to delay at each patrol target
  private static final int MIN_HOT_SPOT_GROUP_SIZE = 5; // min group size to move to hot spot
  private static final int TURNS_AT_HOT_SPOT = 10;
  private static final int TURNS_AT_FIGHT = 3;
  private static final int MAX_LAUNCHER_TASKS = 10;

  private int numTurnsWaitingForFriends = 0;

  private final LauncherTask[] launcherTaskStack;
  private int launcherTaskStackPointer;
  private LauncherTask currentTask;
  private HashSet<MapLocation> visitedLocations;

  private boolean launcherInVision;
  private boolean carrierInVision;
  private boolean carrierInAttackRange;
  private MapLocation lastAttackedLocation;

  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
    LauncherTask.parentLauncher = this;
    LauncherTask.rc = rc;
    resetVisited();
    launcherTaskStack = new LauncherTask[MAX_LAUNCHER_TASKS];
    launcherTaskStackPointer = -1;
    addLauncherTask(setupInitialLauncherTask());
  }

  private void resetVisited() {
    visitedLocations = new HashSet<>(HqMetaInfo.hqCount + CommsHandler.ADAMANTIUM_WELL_SLOTS + CommsHandler.MANA_WELL_SLOTS + CommsHandler.ELIXIR_WELL_SLOTS);
  }

  @Override
  protected void runTurn() throws GameActionException {
    rc.setIndicatorString("Ooga booga im a launcher");

    updateEnemyStateInformation();

    if (!launcherInVision) {
      if (carrierInAttackRange) {
        // attack carrier in action radius and disable moving
        // TODO: let's consider moving forwards?
        MapLocation locationToAttack = bestCarrierInAction();
        if (rc.canAttack(locationToAttack)) {
          rc.attack(locationToAttack);
        }
        return;
      } else if (carrierInVision) {
        // move towards
        Direction direction = bestCarrierInVision();
        if (direction != null) {
          if (pathing.move(direction)) {
            updateEnemyStateInformation();
            if (!launcherInVision && carrierInAttackRange) {
              MapLocation locationToAttack = bestCarrierInAction();
              if (rc.canAttack(locationToAttack)) {
                rc.attack(locationToAttack);
              }
              return;
            }
          }
        }
      }
    }

    tryAttack(true);

    int maxTaskChanges = 5;
    while (currentTask.update() && maxTaskChanges-- > 0) {
      completeLauncherTask();
    }
    if (maxTaskChanges <= 0) {
      Printer.print("Launcher task stack looping too much");
    }

    // do micro -- returns true if we did micro -> should not do exploration/patrolling behavior
    boolean didAnyMicro = false;
    while (AttackerFightingMicro.doMicro()) {
      didAnyMicro = true;
      rc.setIndicatorString("did micro");
      tryAttack(false);
    }
    if (didAnyMicro) {
      currentTask.numTurnsNearTarget = 0;
      addFightTask(lastAttackedLocation != null ? lastAttackedLocation : Cache.PerTurn.CURRENT_LOCATION);
    } else {
      MapLocation target = getDestination();
      if (target != null) {
        rc.setIndicatorDot(target, 0, 0, 255);
        attemptMoveTowards(target);
      }
    }

    tryAttack(false);
  }

  private Direction bestCarrierInVision() {
    MapLocation bestCarrierLocationToAttack = null;
    Direction bestDirection = null;
    int bestScore = Integer.MIN_VALUE;
    int myDamage = rc.getType().damage;
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
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (robot.type == RobotType.LAUNCHER) {
        launcherInVision = true;
      } else if (robot.type == RobotType.CARRIER) {
        carrierInVision = true;
        if (robot.location.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED)) {
          carrierInAttackRange = true;
        }
      }
    }
  }

  /**
   * computes the destination for this robot to move towards
   * chase nearby enemies if needed
   * approach enemy closest to our HQ
   * do exploration in early game -- TODO: probably remove
   * do patrolling (visit hot spots in order)
   * @return the destination to move towards
   * @throws GameActionException any exception while calculating destination
   */
  private MapLocation getDestination() throws GameActionException {
    // immediately adjacent location to consider -> will chase a valid enemy if necessary
    MapLocation destination = AttackMicro.getBestMovementPosition();
    if (destination != null) {
      rc.setIndicatorString("chasing enemy: " + destination);
      return destination;
    }

    if (currentTask.type != PatrolTargetType.HOT_SPOT_FIGHT) { // not patrolling a hot spot - consider defending home
      // closest enemy to our closest HQ -- friendlies in danger -> will come back to protect HQ
      destination = Communicator.getClosestEnemy(HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION));
      if (destination != null) {
        MapLocation closestHq = HqMetaInfo.getClosestHqLocation(destination);
        if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(destination, destination.distanceSquaredTo(closestHq)*4)) {
//          addFightTask(destination);
          rc.setIndicatorString("defending HQ " + closestHq + " from closest commed enemy: " + destination);
          return destination;
        }
//        return destination;
      }
    }

    // do actual patrolling -> cycle between different enemy hotspots (wells / HQs)
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
          rc.setIndicatorString("moving towards friend at " + closestFriendToTargetLoc + "-target: " + patrolTarget + " -type=" + currentTask.type.name);
          return closestFriendToTargetLoc;
//        attemptMoveTowards(closestFriendToTargetLoc);
        } else if (closestFriendToTargetLoc.equals(myLocation)) {
          rc.setIndicatorString("I'm the closest, staying still" + " -target: " + patrolTarget + " -type=" + currentTask.type.name);
          return closestFriendToTargetLoc;
        }
      }
      // stay still, not enough friends
      numTurnsWaitingForFriends++;
      if (numTurnsWaitingForFriends > TURNS_TO_WAIT) {
        // go back to nearest HQ
        MapLocation closestHq = HqMetaInfo.getClosestHqLocation(myLocation);
        if (currentTask.numTurnsNearTarget > 0) {
          currentTask.numTurnsNearTarget -= (MIN_GROUP_SIZE_TO_MOVE - totalAllyLaunchers);
          if (currentTask.numTurnsNearTarget < 0) currentTask.numTurnsNearTarget = 0;
        }
        rc.setIndicatorString("retreating towards HQ: " + closestHq);
        return closestHq;
      } else {
//        return explorationTarget; // explore? still while waiting
        rc.setIndicatorString("waiting for friends");
        return closestFriendToTargetLoc;//Cache.PerTurn.CURRENT_LOCATION; // stay still while waiting
      }
    } else {
      // ayy we got friends, now we can go!
      numTurnsWaitingForFriends = 0;
      // TODO if we're closest to the target, don't move
      Direction toTarget = myLocation.directionTo(patrolTarget);
      if (closestFriendDistToTargetDist < myDistToTarget) { // someone else is closer
        rc.setIndicatorString("clump friend -> " + currentTask.type.name + "@" + currentTask.targetLocation + "via-" + patrolTarget + " - turns@target:" + currentTask.numTurnsNearTarget);
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
//          if (i % 2 == 1) {
//            if (closestLinePoint != null) {
//              break;
//            }
//            lineFormationPointLeft = lineFormationPointRight = lineFormationCenter = lineFormationCenter.subtract(toTarget);
//          }
//        }
        return patrolTarget; //closestLinePoint != null ? closestLinePoint : patrolTarget;
      } else { // i'm the closest
        rc.setIndicatorString("advance clump -> " + currentTask.type.name + "@" + currentTask.targetLocation + "via-" + patrolTarget + " - turns@target:" + currentTask.numTurnsNearTarget);
        return myLocation; //adjacentAllyLaunchers >= (MIN_GROUP_SIZE_TO_MOVE - 1)*0.75 ? patrolTarget : myLocation;
      }
//      return patrolTarget;
    }
  }

  /**
   * determines if there is a hot spot we need to visit when spawned
   * @throws GameActionException any issues with figuring out a hot spot
   */
  private LauncherTask setupInitialLauncherTask() throws GameActionException {
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
        return new LauncherTask(PatrolTargetType.HOT_SPOT_WELL_DEFENSE, mostEndangeredWell, toGuard);
      }
    }

    return new LauncherTask(PatrolTargetType.DEFAULT_FIRST_TARGET, null, null);
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
  private void completeLauncherTask() throws GameActionException {
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
    OUR_HQ("OUR_HQ", true, false, Launcher.TURNS_AT_TARGET, true),
    OUR_WELL("OUR_WELL", true, false, Launcher.TURNS_AT_TARGET, true),
    ENEMY_WELL("ENEMY_WELL", false, false, Launcher.TURNS_AT_TARGET, true),
    ENEMY_HQ("ENEMY_HQ", false, false, Launcher.TURNS_AT_TARGET, false),
    HOT_SPOT_WELL_DEFENSE("HOT_WELL", false, true, Launcher.TURNS_AT_HOT_SPOT, false),
    HOT_SPOT_FIGHT("HOT_FIGHT", false, true, Launcher.TURNS_AT_FIGHT, false);

    public static final PatrolTargetType DEFAULT_FIRST_TARGET = OUR_WELL;
    public static final PatrolTargetType TARGET_ON_CYCLE = OUR_WELL;

    public final boolean isOurSide;
    public final boolean isHotSpot;
    public final int numTurnsToStayAtTarget;
    public final boolean shouldAlwaysUpdate;
    public final String name;

    PatrolTargetType(String name, boolean isOurSide, boolean isHotSpot, int numTurnsToStayAtTarget, boolean shouldAlwaysUpdate) {
      this.name = name;
      this.isOurSide = isOurSide;
      this.isHotSpot = isHotSpot;
      this.numTurnsToStayAtTarget = numTurnsToStayAtTarget;
      this.shouldAlwaysUpdate = shouldAlwaysUpdate;
    }
  }

  private static class LauncherTask {
    private static Launcher parentLauncher;
    private static RobotController rc;

    PatrolTargetType type;
    MapLocation patrolLocation;
    MapLocation targetLocation;

    int numTurnsNearTarget;

    private LauncherTask(PatrolTargetType type, MapLocation targetLocation, MapLocation patrolLocation) throws GameActionException {
      this.type = type;
      this.numTurnsNearTarget = 0;
      if (patrolLocation != null) {
        this.patrolLocation = patrolLocation;
        this.targetLocation = targetLocation;
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
        return update();
      }

      at_target: if (numTurnsNearTarget > 0 && patrolLocation != null && numTurnsNearTarget < type.numTurnsToStayAtTarget) {
        rc.setIndicatorString("Completing patrol " + type.name + "@" + targetLocation + " (via: " + patrolLocation + ")" + " --turns=" + numTurnsNearTarget);
        return false; // exit early if we're near the patrol target -- finish patrolling
      }
      patrol_complete: if (numTurnsNearTarget >= type.numTurnsToStayAtTarget * 0.7) {
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
      if ((numTurnsNearTarget == 0 && type.shouldAlwaysUpdate) || patrolLocation == null) { // we are done patrolling the current location
        // update the target if needed
        switch (type) {
          case OUR_HQ:
//            patrolTargetType = PatrolTargetType.OUR_HQ;
            targetLocation = parentLauncher.determinePatrollingTargetLocationOurHq();
            if (targetLocation != null) {
              Direction randomDir = Utils.randomDirection();
              patrolLocation = targetLocation.add(randomDir).add(randomDir);
              rc.setIndicatorString("patrolling our hq " + targetLocation + " - from " + patrolLocation);
              break;
            }
          case OUR_WELL: our_well: {
            if (true) break our_well;
            type = PatrolTargetType.OUR_WELL;
            targetLocation = parentLauncher.determinePatrollingTargetLocationOurWell();
            if (targetLocation != null) {
              Direction awayFromBase = HqMetaInfo.getClosestHqLocation(targetLocation).directionTo(targetLocation);
              patrolLocation = targetLocation.add(awayFromBase).add(awayFromBase);
              rc.setIndicatorString("patrolling our well " + targetLocation + " - from " + patrolLocation);
              break; // switch to enemy well if all our wells are visited
            }
          }
          case ENEMY_WELL:
            type = PatrolTargetType.ENEMY_WELL;
//          Printer.print("trying to find enemy well for patrolling");
            targetLocation = parentLauncher.determinePatrollingTargetLocationEnemyWell();
            if (targetLocation != null) {
              Direction towardsEnemyBase = targetLocation.directionTo(HqMetaInfo.getClosestEnemyHqLocation(targetLocation));
              patrolLocation = targetLocation.add(towardsEnemyBase).add(towardsEnemyBase);
              rc.setIndicatorString("patrolling enemy well " + targetLocation + " - from " + patrolLocation);
              break; // fall through to enemy HQ if no enemy well known
            }
          case ENEMY_HQ:
            type = PatrolTargetType.ENEMY_HQ;
            targetLocation = parentLauncher.determinePatrollingTargetLocationEnemyHq();
            if (targetLocation != null) {
              patrolLocation = targetLocation;
              int tries = 10;
              while (patrolLocation.isWithinDistanceSquared(targetLocation, RobotType.HEADQUARTERS.actionRadiusSquared) && --tries >= 0) {
                patrolLocation = patrolLocation.add(Utils.randomDirection());
              }
              if (tries < 0) {
                Direction toSelf = patrolLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION);
                patrolLocation = targetLocation.translate(toSelf.dx*4, toSelf.dy*4);
              }
              rc.setIndicatorString("patrolling enemy HQ " + targetLocation + " - from " + patrolLocation);
              break;
            }
//          Printer.print("ERROR: no closest enemy HQ found for patrol -- visited: " + visitedLocations);
          default:
            if (type.isHotSpot) {
              if (patrolLocation != null) {
                rc.setIndicatorString("patrolling hot spot: " + patrolLocation);
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
//            rc.setIndicatorString("patrolling default: " + explorationTarget);
//            patrolLocation = explorationTarget;
//            targetLocation = explorationTarget;
            }
            break;
        }
        if (patrolLocation == null) {
          Printer.print("Failed to select patrol target for type: " + type);
        }
      }
      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, patrolLocation, 200,200,200);
      return false;
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
    return (Cache.PerTurn.ROUND_NUM % 3 != 0)
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
    return false;
//    boolean attacked = bestAttackTarget != null && attack(bestAttackTarget);
//    if (onlyAttackers) return attacked;
//    return attemptCloudAttack();
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
    int cells = (int) Math.ceil(Math.sqrt(Cache.Permanent.ACTION_RADIUS_SQUARED));
    for (int i = -cells; i <= cells; ++i) {
      for (int j = -cells; j <= cells; ++j) {
        MapLocation loc = Cache.PerTurn.CURRENT_LOCATION.translate(i, j);
        if (rc.canAttack(loc)) {
          rc.attack(loc);
          return true;
        }
      }
    }
    return false;
  }
}
