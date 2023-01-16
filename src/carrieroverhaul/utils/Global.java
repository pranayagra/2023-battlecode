package carrieroverhaul.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import carrieroverhaul.communications.Communicator;
import carrieroverhaul.robots.Robot;

public class Global {
  public static RobotController rc;
  public static Robot robot;

  public static void setupGlobals(RobotController rc, Robot robot) {
    Global.rc = rc;
    Global.robot = robot;
  }

  public static void configureCommunicator() throws GameActionException {
    Communicator.init(rc);
  }
}
