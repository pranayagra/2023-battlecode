package nogotofriend;

import nogotofriend.knowledge.Cache;
import nogotofriend.robots.Robot;
import nogotofriend.utils.Printer;
import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {
    public static final boolean RESIGN_ON_FATAL_ERROR = false;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // While loop is completely unnecessary but used to ensure that the method is never exit
        while (true) {
            try {
                Robot robot = Robot.fromRC(rc);
                robot.runLoop();
            } catch (Exception e) {
                System.out.println("FATAL ERROR - " + rc.getLocation());
                System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + ".TLE?=" + (rc.getRoundNum() != Cache.PerTurn.ROUND_NUM) + " FATAL ERROR");
                Printer.submitPrint();
                e.printStackTrace();
                if (RESIGN_ON_FATAL_ERROR) rc.resign();
            }
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
}
