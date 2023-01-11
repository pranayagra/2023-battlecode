package wellcircling.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import wellcircling.communications.Communicator;
import wellcircling.robots.Robot;

public class Global {
  public static RobotController rc;
  public static Robot robot;
  public static Communicator communicator;

  public static void setupGlobals(RobotController rc, Robot robot) {
    Global.rc = rc;
    Global.robot = robot;
  }

  public static void configureCommunicator() throws GameActionException {
    Global.communicator = new Communicator(rc);
  }
}
