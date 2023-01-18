package launcherpatrolmicro.robots;

import launcherpatrolmicro.communications.CommsHandler;
import launcherpatrolmicro.communications.Communicator;
import launcherpatrolmicro.communications.HqMetaInfo;
import launcherpatrolmicro.communications.MapMetaInfo;
import launcherpatrolmicro.containers.HashSet;
import launcherpatrolmicro.robots.micro.AttackMicro;
import launcherpatrolmicro.robots.micro.AttackerFightingMicro;
import launcherpatrolmicro.utils.Cache;
import launcherpatrolmicro.utils.Constants;
import launcherpatrolmicro.utils.Printer;
import launcherpatrolmicro.utils.Utils;
import battlecode.common.*;

public class Launcher extends MobileRobot {
  private static final int MIN_TURN_TO_MOVE = 0;
  private static final int MIN_GROUP_SIZE_TO_MOVE = 3; // min group size to move out
  private static final int TURNS_TO_WAIT = 5; // turns to wait (without friends) until going back to nearest HQ
  private static final int TURNS_AT_TARGET = 10; // how long to delay at each patrol target
  private static final int MIN_HOT_SPOT_GROUP_SIZE = 5; // min group size to move to hot spot
  private static final int TURNS_AT_HOT_SPOT = 10;

  private int numTurnsWaiting = 0;
  private int numTurnsNearTarget = 0;
  private int numTurnsAtHotSpot = 0;
  private HashSet<MapLocation> visitedLocations;
  private PatrolTargetType patrolTargetType;
  private MapLocation patrolTarget;
  private PatrolTargetType savedLastTargetType;
  private MapLocation savedLastTarget;

  private PatrolTargetType preHotSpotSavedTargetType;
  private MapLocation preHotSpotSavedLastTarget;

  private boolean launcherInVision;
  private boolean carrierInVision;
  private boolean carrierInAttackRange;

  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
    patrolTargetType = PatrolTargetType.DEFAULT_FIRST_TARGET;
    resetVisited();
    computeInitialPatrolTarget();
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

    updatePatrolTarget();

    // do micro -- returns true if we did micro -> should not do exploration/patrolling behavior
    boolean didAnyMicro = false;
    while (AttackerFightingMicro.doMicro()) {
      didAnyMicro = true;
      rc.setIndicatorString("did micro");
      tryAttack(false);
    }
    if (didAnyMicro) {
      numTurnsNearTarget = 0;
    } else {
      boolean canMove = Cache.PerTurn.ROUND_NUM >= MIN_TURN_TO_MOVE;
      int numFriendlyLaunchers = 0;
      for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
        if (robot.type == RobotType.LAUNCHER) {
          numFriendlyLaunchers++;
        }
      }
      if (numFriendlyLaunchers >= MIN_GROUP_SIZE_TO_MOVE) canMove = true;
//      if (numFriendlyLaunchers == 0) {
//        numTurnsWaiting++;
//      } else {
//        numTurnsWaiting = 0;
//      }
      if (canMove) {
        MapLocation target;
        do {
          target = getDestination();
//          rc.setIndicatorString("patrol target: " + target);
        } while (target != null && attemptMoveTowards(target)); // moves every other turn
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

  private MapLocation bestCarrierInAction() {
    MapLocation bestCarrierLocationToAttack = null;
    int bestScore = Integer.MIN_VALUE;
    int myDamage = rc.getType().damage;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (robot.type == RobotType.CARRIER) {
        if (robot.location.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED)) {
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
      }
    }
    return bestCarrierLocationToAttack;
  }

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
    // closest enemy to our closest HQ -- friendlies in danger -> will come back to protect HQ
    destination = Communicator.getClosestEnemy(HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION));
    if (destination != null) {
      rc.setIndicatorString("closest commed enemy: " + destination);
      return destination;
    }

    // early game -- just explore on our own side of the map
//    if (Cache.PerTurn.ROUND_NUM < MicroConstants.ATTACK_TURN && HqMetaInfo.isEnemyTerritory(Cache.PerTurn.CURRENT_LOCATION)) {
//      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(explorationTarget, EXPLORATION_REACHED_RADIUS)) {
//        randomizeExplorationTarget(true);
//      }
//      return explorationTarget;
//    }

    // do actual patrolling -> cycle between different enemy hotspots (wells / HQs)
    destination = getPatrolTarget();
    if (destination != null) return destination;

    destination = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    if (destination.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Utils.DSQ_2by2)) {
      destination = HqMetaInfo.enemyHqLocations[Utils.rng.nextInt(HqMetaInfo.hqCount)];
    }
    return destination;
//    return explorationTarget;
  }

  /**
   * does some checks and determines where to patrol to
   * based on the current patrol type and ally/enemy counts
   * @return the location to patrol to
   * @throws GameActionException any issues with
   */
  private MapLocation getPatrolTarget() throws GameActionException {
    if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(patrolTarget, Utils.DSQ_2by2)) {
      numTurnsNearTarget++;
    }

    if (patrolTargetType == PatrolTargetType.OUR_HQ) {
      return patrolTarget;
    }

//    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    RobotInfo[] alliedRobots = Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS;
    MapLocation closestFriendToTargetLoc = Cache.PerTurn.CURRENT_LOCATION;
    int closestFriendDistToTargetDist = closestFriendToTargetLoc.distanceSquaredTo(patrolTarget);

    // nearbyAllyLaunchers
    int nearbyAllyLaunchers = 0;
    int totalAllyLaunchers = 0;
    for (RobotInfo ally : alliedRobots) {
      if (ally.type == RobotType.LAUNCHER) {
        totalAllyLaunchers++;
        int friendToTargetDist = ally.location.distanceSquaredTo(patrolTarget);
        if (friendToTargetDist < closestFriendDistToTargetDist) {
          closestFriendToTargetLoc = ally.location;
          closestFriendDistToTargetDist = friendToTargetDist;
        }
        if (ally.location.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Utils.DSQ_2by2)) {
          nearbyAllyLaunchers++;
        }
      }
    }

    // make sure we have friends
    if (nearbyAllyLaunchers < MIN_GROUP_SIZE_TO_MOVE - 1) { // 1 for self
      if (totalAllyLaunchers > 0 && !closestFriendToTargetLoc.isAdjacentTo(Cache.PerTurn.CURRENT_LOCATION)) {
        // move towards friend closest to current target
        rc.setIndicatorString("moving towards friend at " + closestFriendToTargetLoc + "target: " + patrolTarget);
        return closestFriendToTargetLoc;
//        attemptMoveTowards(closestFriendToTargetLoc);
      }
      // stay still, not enough friends
      numTurnsWaiting++;
      if (numTurnsWaiting > TURNS_TO_WAIT) {
        // go back to nearest HQ
//        Printer.print("waiting -- save last target -- " + patrolTargetType + ": " + patrolTarget);
        savedLastTargetType = patrolTargetType;
        savedLastTarget = patrolTarget;
        patrolTargetType = PatrolTargetType.OUR_HQ;
        numTurnsNearTarget = 0;
        numTurnsAtHotSpot = 0;
        patrolTarget = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        rc.setIndicatorString("retreating towards HQ: " + patrolTarget);
        return patrolTarget;
      } else {
        return explorationTarget;//Cache.PerTurn.CURRENT_LOCATION; // stay still while waiting
      }
    } else {
      // ayy we got friends, now we can go!
      numTurnsWaiting = 0;
//      rc.setIndicatorString("got friends! lessgo! to " + patrolTarget + " - turns@target:" + numTurnsNearTarget);
      // TODO if we're closest to the target, don't move
      if (closestFriendDistToTargetDist < Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(patrolTarget)) {
//        return closestFriendToTargetLoc.add(closestFriendToTargetLoc.directionTo(patrolTarget));
        return patrolTarget;
      } else {
        return Cache.PerTurn.CURRENT_LOCATION;
      }
//      return patrolTarget;
    }
  }

  /**
   * determines if there is a hot spot we need to visit when spawned
   * @throws GameActionException any issues with figuring out a hot spot
   */
  private void computeInitialPatrolTarget() throws GameActionException {
    MapLocation[] endangeredWellPair = Communicator.closestAllyEnemyWellPair();
    MapLocation mostEndangeredWell = endangeredWellPair[0];
    MapLocation mostEndangeredEnemyWell = endangeredWellPair[1];
    if (mostEndangeredWell == null) {
      return;
    }
    int mostEndangeredDist = mostEndangeredWell.distanceSquaredTo(mostEndangeredEnemyWell);
//    Printer.print("endangered wells found! " + mostEndangeredWell + " - " + mostEndangeredEnemyWell + " dist:" + mostEndangeredDist);
    if (mostEndangeredDist <= Constants.ENDANGERED_WELL_DIST) {
//      Printer.print("endangered wells found! " + mostEndangeredWell + " - " + mostEndangeredEnemyWell + " dist:" + mostEndangeredDist);
      Direction oursToTheirs = mostEndangeredWell.directionTo(mostEndangeredEnemyWell);
      MapLocation toGuard = mostEndangeredWell.add(oursToTheirs).add(oursToTheirs);
//      Printer.print("need to guard hotspot - save last target -- " + patrolTargetType + ": " + patrolTarget);
      preHotSpotSavedTargetType = patrolTargetType;
      preHotSpotSavedLastTarget = patrolTarget;
      patrolTargetType = PatrolTargetType.HOT_SPOT;
      patrolTarget = toGuard;
    }
  }

  /**
   * will update the patrol target to go to if needed
   * @throws GameActionException any issues during calculations
   */
  private void updatePatrolTarget() throws GameActionException {
//    MapLocation ans = AttackMicro.getBestTarget();
//    if (ans != null) {
//      // need to immediately respond
//      oldTarget = patrolTarget;
//      patrolTarget = ans;
//      return;
//    }
    if (patrolTargetType == PatrolTargetType.ENEMY_HQ && patrolTarget != null) {
      if (rc.canSenseLocation(patrolTarget) && HqMetaInfo.getClosestEnemyHqLocation(patrolTarget).equals(patrolTarget)) { // we still think there should be an enemy HQ here
        RobotInfo robot = rc.senseRobotAtLocation(patrolTarget);
        if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
//          if (rc.getRoundNum() == 350) {
//            Printer.print("ERROR: expected enemy HQ is not an HQ " + patrolTarget, "symmetry guess must be wrong, eliminating symmetry (" + MapMetaInfo.guessedSymmetry + ") and retrying...");
//            Printer.print("Closest enemy HQ to patrol: " + HqMetaInfo.getClosestEnemyHqLocation(patrolTarget));
//          }
          if (rc.canWriteSharedArray(0,0)) {
            MapMetaInfo.writeNot(MapMetaInfo.guessedSymmetry);
          }
          // TODO: eliminate symmetry and retry
//          RunningMemory.publishNotSymmetry(MapMetaInfo.guessedSymmetry);
//          explorationTarget = communicator.archonInfo.replaceEnemyArchon(explorationTarget);
        }
      }
    }
    if (numTurnsNearTarget > 0 && patrolTarget != null && numTurnsNearTarget < TURNS_AT_TARGET) {
      return; // exit early if we're near the patrol target -- finish patrolling
    }
    patrol_complete: if (numTurnsNearTarget >= TURNS_AT_TARGET) {
      if (patrolTargetType == PatrolTargetType.HOT_SPOT) {
        // need to be more careful with hotspots, wait for more friends
        if (Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length < MIN_HOT_SPOT_GROUP_SIZE) {
          numTurnsAtHotSpot = 0;
          break patrol_complete;
        }
        if (++numTurnsAtHotSpot < TURNS_AT_HOT_SPOT) {
          break patrol_complete;
        }
      }

      numTurnsNearTarget = 0;
      numTurnsAtHotSpot = 0;
      // mark the current target as visited
      if (patrolTarget != null) {
        visitedLocations.add(patrolTarget);
//        currentTargetType = currentTargetType.next();
        patrolTarget = null;
      }

      // restore old target
      if (patrolTargetType == PatrolTargetType.HOT_SPOT) {
//        Printer.print("pre-restore hotspot saved target: " + preHotSpotSavedTargetType + ": " + preHotSpotSavedLastTarget);
        if (preHotSpotSavedLastTarget != null || preHotSpotSavedTargetType != null) {
          patrolTargetType = preHotSpotSavedTargetType;
          patrolTarget = preHotSpotSavedLastTarget;
          preHotSpotSavedTargetType = null;
          preHotSpotSavedLastTarget = null;
        }
      } else {
        if (savedLastTarget != null || savedLastTargetType != null) {
          patrolTargetType = savedLastTargetType;
          patrolTarget = savedLastTarget;
          savedLastTargetType = null;
          savedLastTarget = null;
        }
      }
    }
    // update if we haven't gotten near the target
    if (numTurnsNearTarget == 0 || patrolTarget == null) { // we are done patrolling the current location
      // update the target if needed
      switch (patrolTargetType) {
        case OUR_HQ:
          patrolTarget = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
          break;
        case OUR_WELL: our_well: {
          if (true) break our_well;
          patrolTargetType = PatrolTargetType.OUR_WELL;
          ResourceType rt = ResourceType.ADAMANTIUM;
          if (Utils.rng.nextBoolean()) rt = ResourceType.MANA;
          patrolTarget = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, rt); // TODO: make pick mana vs ad
          if (!visitedLocations.contains(patrolTarget)) {
            rc.setIndicatorString("patrolling our well: " + patrolTarget);
            break; // switch to enemy well if all our wells are visited
          }
        }
        case ENEMY_WELL:
          patrolTargetType = PatrolTargetType.ENEMY_WELL;
//          Printer.print("trying to find enemy well for patrolling");
          MapLocation myLoc = Cache.PerTurn.CURRENT_LOCATION;
          int closestDist = Integer.MAX_VALUE;
          MapLocation closestEnemyWell = null;
          for (int i = 0; i < CommsHandler.MANA_WELL_SLOTS; i++) {
            if (CommsHandler.readManaWellExists(i)) {
              MapLocation wellLocation = CommsHandler.readManaWellLocation(i);
              if (!HqMetaInfo.isEnemyTerritory(wellLocation)) {
                wellLocation = Utils.applySymmetry(wellLocation, MapMetaInfo.guessedSymmetry);
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
                wellLocation = Utils.applySymmetry(wellLocation, MapMetaInfo.guessedSymmetry);
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
                wellLocation = Utils.applySymmetry(wellLocation, MapMetaInfo.guessedSymmetry);
              }
              if (visitedLocations.contains(wellLocation)) continue;
              int dist = myLoc.distanceSquaredTo(wellLocation);
              if (dist < closestDist) {
                closestDist = dist;
                closestEnemyWell = wellLocation;
              }
            }
          }

          patrolTarget = closestEnemyWell;
          if (patrolTarget != null) {
            rc.setIndicatorString("patrolling enemy well: " + patrolTarget);
            break; // fall through to enemy HQ if no enemy well known
          }
        case ENEMY_HQ:
          patrolTargetType = PatrolTargetType.ENEMY_HQ;
//          patrolTarget = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
          MapLocation[] enemyHQs = HqMetaInfo.enemyHqLocations;
          MapLocation closestHQ = null;
          int bestDist = 1000000;
          for (MapLocation enemyHQ : enemyHQs) {
            if (visitedLocations.contains(enemyHQ)) continue;
//            int dist = enemyHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
            int dist = Utils.applySymmetry(enemyHQ, MapMetaInfo.guessedSymmetry).distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
            if (dist < bestDist) {
              bestDist = dist;
              closestHQ = enemyHQ;
            }
          }
          patrolTarget = closestHQ;
          if (patrolTarget != null) {
            rc.setIndicatorString("patrolling enemy HQ: " + patrolTarget);
            break;
          }
//          Printer.print("ERROR: no closest enemy HQ found for patrol -- visited: " + visitedLocations);
        default:
          patrolTargetType = PatrolTargetType.TARGET_ON_CYCLE;
          if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(explorationTarget, EXPLORATION_REACHED_RADIUS)) {
            randomizeExplorationTarget(true);
          }
          rc.setIndicatorString("patrolling default: " + explorationTarget);
          patrolTarget = explorationTarget;
          break;
        case HOT_SPOT:
          if (patrolTarget == null) { // done patrolling the hot spot
            Printer.print("done patrolling -- revert to " + savedLastTargetType + ": " + savedLastTarget);
            patrolTargetType = savedLastTargetType;
            patrolTarget = savedLastTarget;
          }
          if (patrolTarget != null) {
            rc.setIndicatorString("patrolling hot spot: " + patrolTarget);
            break;
          }
      }
      if (patrolTarget == null) {
        Printer.print("Failed to select patrol target for type: " + patrolTargetType);
      }
      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, patrolTarget, 200,200,200);
    }
    rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, patrolTarget, 200,200,200);
  }

  public enum PatrolTargetType {
    OUR_HQ,
    OUR_WELL,
    ENEMY_WELL,
    ENEMY_HQ,
    HOT_SPOT;

    public static final PatrolTargetType DEFAULT_FIRST_TARGET = ENEMY_HQ;
    public static final PatrolTargetType TARGET_ON_CYCLE = ENEMY_WELL;

  }



  /**
   * Attempts to move towards the given location -- only allows movement on even turns
   * @param target where to move towards
   * @return true if moved
   * @throws GameActionException any issues with moving
   */
  private boolean attemptMoveTowards(MapLocation target) throws GameActionException {
    return Cache.PerTurn.ROUND_NUM % 2 == 0
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
    return bestAttackTarget != null && attack(bestAttackTarget);
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
