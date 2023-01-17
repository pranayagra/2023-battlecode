package actuallaunchermicro.robots;

import actuallaunchermicro.communications.Communicator;
import actuallaunchermicro.communications.HqMetaInfo;
import actuallaunchermicro.robots.micro.AttackMicro;
import actuallaunchermicro.robots.micro.AttackerFightingMicro;
import actuallaunchermicro.robots.micro.MicroConstants;
import actuallaunchermicro.utils.Cache;
import actuallaunchermicro.utils.Utils;
import battlecode.common.*;

public class Launcher extends MobileRobot {
  private static final int MIN_TURN_TO_MOVE = 9;

  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    rc.setIndicatorString("Ooga booga im a launcher");

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
