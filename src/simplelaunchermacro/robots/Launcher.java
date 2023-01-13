package simplelaunchermacro.robots;

import simplelaunchermacro.utils.Cache;
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

    doExploration();

    rc.setIndicatorString("Enemies near me: " + Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length + " -- canAct=" + rc.isActionReady());

    allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    for (int i = allNearbyEnemyRobots.length; --i >= 0 && rc.isActionReady();) {
      RobotInfo enemy = allNearbyEnemyRobots[i];
      attack(enemy.location);
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
}
