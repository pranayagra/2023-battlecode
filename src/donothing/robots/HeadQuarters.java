package donothing.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**///import basicbot.utils.Printer;
public class HeadQuarters extends Robot {
  /*WORKFLOW_ONLY*///private int totalSpawns = 0;
  public HeadQuarters(RobotController rc) throws GameActionException {
      super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM >= 1000) rc.resign();

    /*WORKFLOW_ONLY*///if (Cache.PerTurn.ROUND_NUM % 250 == 249) {
    /*WORKFLOW_ONLY*///  Printer.print("HQ" + Cache.PerTurn.ROUND_NUM + Cache.Permanent.OUR_TEAM + hqID + " (" + totalSpawns + ")");
    /*WORKFLOW_ONLY*///}
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
      /*WORKFLOW_ONLY*///totalSpawns++;
      return true;
    }
    return false;
  }
}
