package bugfixessprint.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import bugfixessprint.communications.Communicator;
import bugfixessprint.robots.Robot;

public class Global {
  public static RobotController rc;
  public static Robot robot;

  public static void setupGlobals(RobotController rc, Robot robot) {
    Global.rc = rc;
    Global.robot = robot;
  }
}
