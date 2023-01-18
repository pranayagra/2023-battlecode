package basicbot.robots;

import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.robots.micro.AttackMicro;
import basicbot.robots.micro.AttackerFightingMicro;
import basicbot.robots.micro.MicroConstants;
import basicbot.utils.Cache;
import basicbot.utils.Utils;
import battlecode.common.*;

public class Launcher extends MobileRobot {
  private static final int MIN_TURN_TO_MOVE = 9;
  private boolean launcherInVision;
  private boolean carrierInVision;
  private boolean carrierInAttackRange;

  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
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

    // do micro -- returns true if we did micro -> should exit early
    boolean didAnyMicro = false;
    while (AttackerFightingMicro.doMicro()) {
      didAnyMicro = true;
      rc.setIndicatorString("did micro");
    }
    if (!didAnyMicro) {
      boolean canMove = Cache.PerTurn.ROUND_NUM >= MIN_TURN_TO_MOVE;
      if (Cache.PerTurn.ROUND_NUM < MIN_TURN_TO_MOVE) {
        int numFriendlyLaunchers = 0;
        for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
          if (robot.type == RobotType.LAUNCHER) {
            numFriendlyLaunchers++;
          }
        }
        if (numFriendlyLaunchers >= 2) canMove = true;
      }
      if (canMove) {
        MapLocation target;
        do {
          target = getPatrolTarget();
          rc.setIndicatorString("patrol target: " + target);
        } while (target != null && pathing.moveTowards(target));
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

  private MapLocation getPatrolTarget() throws GameActionException {
    // immediately adjacent location to consider -> will chase a valid enemy if necessary
    MapLocation patrolTarget = AttackMicro.getBestMovementPosition();
    if (patrolTarget != null) return patrolTarget;
    // closest enemy to our closest HQ -- friendlies in danger -> will come back to protect HQ
    patrolTarget = Communicator.getClosestEnemy(HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION));
    if (patrolTarget != null) return patrolTarget;

    // early game -- just explore on our own side of the map
    if (Cache.PerTurn.ROUND_NUM < MicroConstants.ATTACK_TURN && HqMetaInfo.isEnemyTerritory(Cache.PerTurn.CURRENT_LOCATION)) {
      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(explorationTarget, EXPLORATION_REACHED_RADIUS)) {
        randomizeExplorationTarget(true);
      }
      return explorationTarget;
    }

    // do actual patrolling -> cycle between different enemy hotspots (wells / HQs)
    // TODO: add alex code here
    patrolTarget = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    if (patrolTarget.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Utils.DSQ_2by2)) {
      patrolTarget = HqMetaInfo.enemyHqLocations[Utils.rng.nextInt(HqMetaInfo.hqCount)];
    }
    return patrolTarget;
//    return explorationTarget;
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
//    MapLocation[] allLocationsWithinAction = rc.getAllLocationsWithinRadiusSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED);
//    for (MapLocation loc : allLocationsWithinAction) {
//      if (Cache.PerTurn.ROUND_NUM == 131 && rc.getID() == 10216) {
//        Printer.print("Checking " + loc + ", has? " + rc.canActLocation(loc) + " and " + rc.canAttack(loc));
//      }
//      if (rc.canAttack(loc)) {
//        rc.attack(loc);
//        return;
//      }
//    }
  }
}
