package testing230122.robots.pathfinding;

import testing230122.knowledge.Cache;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class BasicPathing extends Pathing {

  public BasicPathing(RobotController rc) {
      super(rc);
  }


  @Override
  public boolean moveTowards(MapLocation target) throws GameActionException {
    return moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(target));
  }

  @Override
  public boolean moveAwayFrom(MapLocation target) throws GameActionException {
    return moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(target).opposite());
  }
}
