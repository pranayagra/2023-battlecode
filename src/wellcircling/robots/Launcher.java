package wellcircling.robots;

import wellcircling.utils.Cache;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Launcher extends Robot {
  public Launcher(RobotController rc) throws GameActionException {
    super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    int i = 0;
    while (i < Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length
        && attack(Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS[i++].location)) {}

    while (pathing.moveRandomly()) {};

    i = 0;
    while (i < Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length
        && attack(Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS[i++].location)) {}
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
