package launchercomms.robots;

import launchercomms.communications.Communicator;
import launchercomms.communications.HqMetaInfo;
import launchercomms.utils.Cache;
import launchercomms.utils.Printer;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Launcher extends MobileRobot {
  private static final int MIN_TURN_TO_MOVE = 9;

  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    rc.setIndicatorString("Ooga booga im a launcher");
    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    for (int i = allNearbyEnemyRobots.length; --i >= 0 && rc.isActionReady();) {
      RobotInfo enemy = allNearbyEnemyRobots[i];
      attack(enemy.location);
    }

    // if I could not attack anyone in my vision range above, then consider cloud attacks (and if success, we should move backwards?)
    // need to be careful with cd multiplier causing isAction to be false after a couple successful attacks
    if (rc.isActionReady()) {
      if (attemptCloudAttack()) {
        rc.setIndicatorString("successful cloud attack -> exit early");
        return;
      }
    }

    if (Cache.PerTurn.ROUND_NUM >= MIN_TURN_TO_MOVE) {
      MapLocation closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
      MapLocation closestEnemy = Communicator.getClosestEnemy(closestHQ);
      int closestDist = closestEnemy == null ? Integer.MAX_VALUE : closestEnemy.distanceSquaredTo(closestHQ);
      for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
        switch (robot.type) {
          case CARRIER:
          case HEADQUARTERS:
            break;
          default:
            int dist = robot.location.distanceSquaredTo(closestHQ);
            if (dist < closestDist) {
              closestDist = dist;
              closestEnemy = robot.location;
            }
        }
      }
      no_enemy:
      if (closestEnemy == null) {
//      if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0) {
//        randomizeExplorationTarget(true);
////        break no_enemy;
//      }
        rc.setIndicatorString("no enemies -> do exploration: " + explorationTarget);
        doExploration();
      }
      if (closestEnemy != null) { // only go to enemies if we aren't already dealing with some enemy
        rc.setIndicatorString("Approach commed enemy: " + closestEnemy);
        pathing.moveTowards(closestEnemy);
      }
    }

//    rc.setIndicatorString("Enemies near me: " + Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length + " -- canAct=" + rc.isActionReady());

    allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    for (int i = allNearbyEnemyRobots.length; --i >= 0 && rc.isActionReady();) {
      RobotInfo enemy = allNearbyEnemyRobots[i];
      attack(enemy.location);
    }

    if (rc.isActionReady()) {
      attemptCloudAttack();
    }
  }

  private boolean attack(MapLocation loc) throws GameActionException {
//    if (rc.canAttack(loc)) return false; //todo : Remove after!
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
