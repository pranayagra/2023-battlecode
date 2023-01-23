package dynamiclaunchergroup.robots.pathfinding.unitpathing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface UnitPathing {
  public Direction bestDir(MapLocation target) throws GameActionException;
}
