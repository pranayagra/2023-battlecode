package basicbot.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HeadQuarters extends Robot {
  public HeadQuarters(RobotController rc) throws GameActionException {
      super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {

  }

  /**
   * build the specified robot type in the specified direction
   * @param type the robot type to build
   * @param location where to build it (if null, choose random direction)
   * @return method success
   * @throws GameActionException when building fails
   */
  protected boolean buildRobot(RobotType type, MapLocation location) throws GameActionException {
    if (rc.canBuildRobot(type, location)) {
      rc.buildRobot(type, location);
      return true;
    }
    return false;
  }
}
