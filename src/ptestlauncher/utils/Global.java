package ptestlauncher.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import ptestlauncher.communications.Communicator;
import ptestlauncher.robots.Robot;

public class Global {
  public static RobotController rc;
  public static Robot robot;

  public static void setupGlobals(RobotController rc, Robot robot) {
    Global.rc = rc;
    Global.robot = robot;
  }
}
