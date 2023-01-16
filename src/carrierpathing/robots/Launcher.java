package carrierpathing.robots;

import carrierpathing.utils.Cache;
import carrierpathing.utils.Printer;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Launcher extends MobileRobot {
  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    for (int i = allNearbyEnemyRobots.length; --i >= 0 && rc.isActionReady();) {
      RobotInfo enemy = allNearbyEnemyRobots[i];
      attack(enemy.location);
    }

    // if I could not attack anyone in my vision range above, then consider cloud attacks (and if success, we should move backwards?)
    // need to be careful with cd multiplier causing isAction to be false after a couple successful attacks
    if (rc.isActionReady()) {
      if (attemptCloudAttack()) {
        return;
      }
    }

    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0) {
      randomizeExplorationTarget(true);
    }
    doExploration();

    rc.setIndicatorString("Enemies near me: " + Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length + " -- canAct=" + rc.isActionReady());

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
