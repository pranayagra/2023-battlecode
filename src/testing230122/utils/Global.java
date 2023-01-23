package testing230122.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import testing230122.communications.Communicator;
import testing230122.robots.Robot;

public class Global {
  public static RobotController rc;
  public static Robot robot;

  public static void setupGlobals(RobotController rc, Robot robot) {
    Global.rc = rc;
    Global.robot = robot;
  }
}
