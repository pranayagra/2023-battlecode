package finalbotfinaltwo.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import finalbotfinaltwo.communications.Communicator;
import finalbotfinaltwo.robots.Robot;

public class Global {
  public static RobotController rc;
  public static Robot robot;

  public static void setupGlobals(RobotController rc, Robot robot) {
    Global.rc = rc;
    Global.robot = robot;
  }
}
