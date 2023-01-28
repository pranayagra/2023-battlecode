// Inspired by https://github.com/IvanGeffner/battlecode2021/blob/master/thirtyone/BFSMuckraker.java.
package maybebigeconfix.robots.pathfinding.unitpathing;

import battlecode.common.*;
import maybebigeconfix.knowledge.Cache;
import maybebigeconfix.utils.Utils;

public class AmplifierPathing implements UnitPathing {
    
    RobotController rc;

    static MapLocation l$x_4$y_2; // location representing relative coordinate (-4, -2)
    static int d$x_4$y_2; // shortest distance to location from current location
    // static Direction dir$x_4$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x_4$y_1; // location representing relative coordinate (-4, -1)
    static int d$x_4$y_1; // shortest distance to location from current location
    // static Direction dir$x_4$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x_4$y0; // location representing relative coordinate (-4, 0)
    static int d$x_4$y0; // shortest distance to location from current location
    // static Direction dir$x_4$y0; // best direction to take now to optimally get to location

    static MapLocation l$x_4$y1; // location representing relative coordinate (-4, 1)
    static int d$x_4$y1; // shortest distance to location from current location
    // static Direction dir$x_4$y1; // best direction to take now to optimally get to location

    static MapLocation l$x_4$y2; // location representing relative coordinate (-4, 2)
    static int d$x_4$y2; // shortest distance to location from current location
    // static Direction dir$x_4$y2; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y_3; // location representing relative coordinate (-3, -3)
    static int d$x_3$y_3; // shortest distance to location from current location
    // static Direction dir$x_3$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y_2; // location representing relative coordinate (-3, -2)
    static int d$x_3$y_2; // shortest distance to location from current location
    // static Direction dir$x_3$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y_1; // location representing relative coordinate (-3, -1)
    static int d$x_3$y_1; // shortest distance to location from current location
    // static Direction dir$x_3$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y0; // location representing relative coordinate (-3, 0)
    static int d$x_3$y0; // shortest distance to location from current location
    // static Direction dir$x_3$y0; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y1; // location representing relative coordinate (-3, 1)
    static int d$x_3$y1; // shortest distance to location from current location
    // static Direction dir$x_3$y1; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y2; // location representing relative coordinate (-3, 2)
    static int d$x_3$y2; // shortest distance to location from current location
    // static Direction dir$x_3$y2; // best direction to take now to optimally get to location

    static MapLocation l$x_3$y3; // location representing relative coordinate (-3, 3)
    static int d$x_3$y3; // shortest distance to location from current location
    // static Direction dir$x_3$y3; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y_4; // location representing relative coordinate (-2, -4)
    static int d$x_2$y_4; // shortest distance to location from current location
    // static Direction dir$x_2$y_4; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y_3; // location representing relative coordinate (-2, -3)
    static int d$x_2$y_3; // shortest distance to location from current location
    // static Direction dir$x_2$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y_2; // location representing relative coordinate (-2, -2)
    static int d$x_2$y_2; // shortest distance to location from current location
    // static Direction dir$x_2$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y_1; // location representing relative coordinate (-2, -1)
    static int d$x_2$y_1; // shortest distance to location from current location
    // static Direction dir$x_2$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y0; // location representing relative coordinate (-2, 0)
    static int d$x_2$y0; // shortest distance to location from current location
    // static Direction dir$x_2$y0; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y1; // location representing relative coordinate (-2, 1)
    static int d$x_2$y1; // shortest distance to location from current location
    // static Direction dir$x_2$y1; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y2; // location representing relative coordinate (-2, 2)
    static int d$x_2$y2; // shortest distance to location from current location
    // static Direction dir$x_2$y2; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y3; // location representing relative coordinate (-2, 3)
    static int d$x_2$y3; // shortest distance to location from current location
    // static Direction dir$x_2$y3; // best direction to take now to optimally get to location

    static MapLocation l$x_2$y4; // location representing relative coordinate (-2, 4)
    static int d$x_2$y4; // shortest distance to location from current location
    // static Direction dir$x_2$y4; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y_4; // location representing relative coordinate (-1, -4)
    static int d$x_1$y_4; // shortest distance to location from current location
    // static Direction dir$x_1$y_4; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y_3; // location representing relative coordinate (-1, -3)
    static int d$x_1$y_3; // shortest distance to location from current location
    // static Direction dir$x_1$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y_2; // location representing relative coordinate (-1, -2)
    static int d$x_1$y_2; // shortest distance to location from current location
    // static Direction dir$x_1$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y_1; // location representing relative coordinate (-1, -1)
    static int d$x_1$y_1; // shortest distance to location from current location
    // static Direction dir$x_1$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y0; // location representing relative coordinate (-1, 0)
    static int d$x_1$y0; // shortest distance to location from current location
    // static Direction dir$x_1$y0; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y1; // location representing relative coordinate (-1, 1)
    static int d$x_1$y1; // shortest distance to location from current location
    // static Direction dir$x_1$y1; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y2; // location representing relative coordinate (-1, 2)
    static int d$x_1$y2; // shortest distance to location from current location
    // static Direction dir$x_1$y2; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y3; // location representing relative coordinate (-1, 3)
    static int d$x_1$y3; // shortest distance to location from current location
    // static Direction dir$x_1$y3; // best direction to take now to optimally get to location

    static MapLocation l$x_1$y4; // location representing relative coordinate (-1, 4)
    static int d$x_1$y4; // shortest distance to location from current location
    // static Direction dir$x_1$y4; // best direction to take now to optimally get to location

    static MapLocation l$x0$y_4; // location representing relative coordinate (0, -4)
    static int d$x0$y_4; // shortest distance to location from current location
    // static Direction dir$x0$y_4; // best direction to take now to optimally get to location

    static MapLocation l$x0$y_3; // location representing relative coordinate (0, -3)
    static int d$x0$y_3; // shortest distance to location from current location
    // static Direction dir$x0$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x0$y_2; // location representing relative coordinate (0, -2)
    static int d$x0$y_2; // shortest distance to location from current location
    // static Direction dir$x0$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x0$y_1; // location representing relative coordinate (0, -1)
    static int d$x0$y_1; // shortest distance to location from current location
    // static Direction dir$x0$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x0$y0; // location representing relative coordinate (0, 0)
    static int d$x0$y0; // shortest distance to location from current location
    // static Direction dir$x0$y0; // best direction to take now to optimally get to location

    static MapLocation l$x0$y1; // location representing relative coordinate (0, 1)
    static int d$x0$y1; // shortest distance to location from current location
    // static Direction dir$x0$y1; // best direction to take now to optimally get to location

    static MapLocation l$x0$y2; // location representing relative coordinate (0, 2)
    static int d$x0$y2; // shortest distance to location from current location
    // static Direction dir$x0$y2; // best direction to take now to optimally get to location

    static MapLocation l$x0$y3; // location representing relative coordinate (0, 3)
    static int d$x0$y3; // shortest distance to location from current location
    // static Direction dir$x0$y3; // best direction to take now to optimally get to location

    static MapLocation l$x0$y4; // location representing relative coordinate (0, 4)
    static int d$x0$y4; // shortest distance to location from current location
    // static Direction dir$x0$y4; // best direction to take now to optimally get to location

    static MapLocation l$x1$y_4; // location representing relative coordinate (1, -4)
    static int d$x1$y_4; // shortest distance to location from current location
    // static Direction dir$x1$y_4; // best direction to take now to optimally get to location

    static MapLocation l$x1$y_3; // location representing relative coordinate (1, -3)
    static int d$x1$y_3; // shortest distance to location from current location
    // static Direction dir$x1$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x1$y_2; // location representing relative coordinate (1, -2)
    static int d$x1$y_2; // shortest distance to location from current location
    // static Direction dir$x1$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x1$y_1; // location representing relative coordinate (1, -1)
    static int d$x1$y_1; // shortest distance to location from current location
    // static Direction dir$x1$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x1$y0; // location representing relative coordinate (1, 0)
    static int d$x1$y0; // shortest distance to location from current location
    // static Direction dir$x1$y0; // best direction to take now to optimally get to location

    static MapLocation l$x1$y1; // location representing relative coordinate (1, 1)
    static int d$x1$y1; // shortest distance to location from current location
    // static Direction dir$x1$y1; // best direction to take now to optimally get to location

    static MapLocation l$x1$y2; // location representing relative coordinate (1, 2)
    static int d$x1$y2; // shortest distance to location from current location
    // static Direction dir$x1$y2; // best direction to take now to optimally get to location

    static MapLocation l$x1$y3; // location representing relative coordinate (1, 3)
    static int d$x1$y3; // shortest distance to location from current location
    // static Direction dir$x1$y3; // best direction to take now to optimally get to location

    static MapLocation l$x1$y4; // location representing relative coordinate (1, 4)
    static int d$x1$y4; // shortest distance to location from current location
    // static Direction dir$x1$y4; // best direction to take now to optimally get to location

    static MapLocation l$x2$y_4; // location representing relative coordinate (2, -4)
    static int d$x2$y_4; // shortest distance to location from current location
    // static Direction dir$x2$y_4; // best direction to take now to optimally get to location

    static MapLocation l$x2$y_3; // location representing relative coordinate (2, -3)
    static int d$x2$y_3; // shortest distance to location from current location
    // static Direction dir$x2$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x2$y_2; // location representing relative coordinate (2, -2)
    static int d$x2$y_2; // shortest distance to location from current location
    // static Direction dir$x2$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x2$y_1; // location representing relative coordinate (2, -1)
    static int d$x2$y_1; // shortest distance to location from current location
    // static Direction dir$x2$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x2$y0; // location representing relative coordinate (2, 0)
    static int d$x2$y0; // shortest distance to location from current location
    // static Direction dir$x2$y0; // best direction to take now to optimally get to location

    static MapLocation l$x2$y1; // location representing relative coordinate (2, 1)
    static int d$x2$y1; // shortest distance to location from current location
    // static Direction dir$x2$y1; // best direction to take now to optimally get to location

    static MapLocation l$x2$y2; // location representing relative coordinate (2, 2)
    static int d$x2$y2; // shortest distance to location from current location
    // static Direction dir$x2$y2; // best direction to take now to optimally get to location

    static MapLocation l$x2$y3; // location representing relative coordinate (2, 3)
    static int d$x2$y3; // shortest distance to location from current location
    // static Direction dir$x2$y3; // best direction to take now to optimally get to location

    static MapLocation l$x2$y4; // location representing relative coordinate (2, 4)
    static int d$x2$y4; // shortest distance to location from current location
    // static Direction dir$x2$y4; // best direction to take now to optimally get to location

    static MapLocation l$x3$y_3; // location representing relative coordinate (3, -3)
    static int d$x3$y_3; // shortest distance to location from current location
    // static Direction dir$x3$y_3; // best direction to take now to optimally get to location

    static MapLocation l$x3$y_2; // location representing relative coordinate (3, -2)
    static int d$x3$y_2; // shortest distance to location from current location
    // static Direction dir$x3$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x3$y_1; // location representing relative coordinate (3, -1)
    static int d$x3$y_1; // shortest distance to location from current location
    // static Direction dir$x3$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x3$y0; // location representing relative coordinate (3, 0)
    static int d$x3$y0; // shortest distance to location from current location
    // static Direction dir$x3$y0; // best direction to take now to optimally get to location

    static MapLocation l$x3$y1; // location representing relative coordinate (3, 1)
    static int d$x3$y1; // shortest distance to location from current location
    // static Direction dir$x3$y1; // best direction to take now to optimally get to location

    static MapLocation l$x3$y2; // location representing relative coordinate (3, 2)
    static int d$x3$y2; // shortest distance to location from current location
    // static Direction dir$x3$y2; // best direction to take now to optimally get to location

    static MapLocation l$x3$y3; // location representing relative coordinate (3, 3)
    static int d$x3$y3; // shortest distance to location from current location
    // static Direction dir$x3$y3; // best direction to take now to optimally get to location

    static MapLocation l$x4$y_2; // location representing relative coordinate (4, -2)
    static int d$x4$y_2; // shortest distance to location from current location
    // static Direction dir$x4$y_2; // best direction to take now to optimally get to location

    static MapLocation l$x4$y_1; // location representing relative coordinate (4, -1)
    static int d$x4$y_1; // shortest distance to location from current location
    // static Direction dir$x4$y_1; // best direction to take now to optimally get to location

    static MapLocation l$x4$y0; // location representing relative coordinate (4, 0)
    static int d$x4$y0; // shortest distance to location from current location
    // static Direction dir$x4$y0; // best direction to take now to optimally get to location

    static MapLocation l$x4$y1; // location representing relative coordinate (4, 1)
    static int d$x4$y1; // shortest distance to location from current location
    // static Direction dir$x4$y1; // best direction to take now to optimally get to location

    static MapLocation l$x4$y2; // location representing relative coordinate (4, 2)
    static int d$x4$y2; // shortest distance to location from current location
    // static Direction dir$x4$y2; // best direction to take now to optimally get to location


    public AmplifierPathing(RobotController rc) {
        this.rc = rc;
    }

    public Direction bestDir(MapLocation target) throws GameActionException {

        l$x0$y0 = rc.getLocation();
        d$x0$y0 = 0;
        // dir$x0$y0 = Direction.CENTER;

        l$x_1$y0 = l$x0$y0.add(Direction.WEST); // (-1, 0) from (0, 0)
        d$x_1$y0 = 99999 << 4;
        // dir$x_1$y0 = null;

        l$x0$y_1 = l$x0$y0.add(Direction.SOUTH); // (0, -1) from (0, 0)
        d$x0$y_1 = 99999 << 4;
        // dir$x0$y_1 = null;

        l$x0$y1 = l$x0$y0.add(Direction.NORTH); // (0, 1) from (0, 0)
        d$x0$y1 = 99999 << 4;
        // dir$x0$y1 = null;

        l$x1$y0 = l$x0$y0.add(Direction.EAST); // (1, 0) from (0, 0)
        d$x1$y0 = 99999 << 4;
        // dir$x1$y0 = null;

        l$x_1$y_1 = l$x0$y0.add(Direction.SOUTHWEST); // (-1, -1) from (0, 0)
        d$x_1$y_1 = 99999 << 4;
        // dir$x_1$y_1 = null;

        l$x_1$y1 = l$x0$y0.add(Direction.NORTHWEST); // (-1, 1) from (0, 0)
        d$x_1$y1 = 99999 << 4;
        // dir$x_1$y1 = null;

        l$x1$y_1 = l$x0$y0.add(Direction.SOUTHEAST); // (1, -1) from (0, 0)
        d$x1$y_1 = 99999 << 4;
        // dir$x1$y_1 = null;

        l$x1$y1 = l$x0$y0.add(Direction.NORTHEAST); // (1, 1) from (0, 0)
        d$x1$y1 = 99999 << 4;
        // dir$x1$y1 = null;

        l$x_2$y0 = l$x_1$y0.add(Direction.WEST); // (-2, 0) from (-1, 0)
        d$x_2$y0 = 99999 << 4;
        // dir$x_2$y0 = null;

        l$x0$y_2 = l$x0$y_1.add(Direction.SOUTH); // (0, -2) from (0, -1)
        d$x0$y_2 = 99999 << 4;
        // dir$x0$y_2 = null;

        l$x0$y2 = l$x0$y1.add(Direction.NORTH); // (0, 2) from (0, 1)
        d$x0$y2 = 99999 << 4;
        // dir$x0$y2 = null;

        l$x2$y0 = l$x1$y0.add(Direction.EAST); // (2, 0) from (1, 0)
        d$x2$y0 = 99999 << 4;
        // dir$x2$y0 = null;

        l$x_2$y_1 = l$x_1$y0.add(Direction.SOUTHWEST); // (-2, -1) from (-1, 0)
        d$x_2$y_1 = 99999 << 4;
        // dir$x_2$y_1 = null;

        l$x_2$y1 = l$x_1$y0.add(Direction.NORTHWEST); // (-2, 1) from (-1, 0)
        d$x_2$y1 = 99999 << 4;
        // dir$x_2$y1 = null;

        l$x_1$y_2 = l$x0$y_1.add(Direction.SOUTHWEST); // (-1, -2) from (0, -1)
        d$x_1$y_2 = 99999 << 4;
        // dir$x_1$y_2 = null;

        l$x_1$y2 = l$x0$y1.add(Direction.NORTHWEST); // (-1, 2) from (0, 1)
        d$x_1$y2 = 99999 << 4;
        // dir$x_1$y2 = null;

        l$x1$y_2 = l$x0$y_1.add(Direction.SOUTHEAST); // (1, -2) from (0, -1)
        d$x1$y_2 = 99999 << 4;
        // dir$x1$y_2 = null;

        l$x1$y2 = l$x0$y1.add(Direction.NORTHEAST); // (1, 2) from (0, 1)
        d$x1$y2 = 99999 << 4;
        // dir$x1$y2 = null;

        l$x2$y_1 = l$x1$y0.add(Direction.SOUTHEAST); // (2, -1) from (1, 0)
        d$x2$y_1 = 99999 << 4;
        // dir$x2$y_1 = null;

        l$x2$y1 = l$x1$y0.add(Direction.NORTHEAST); // (2, 1) from (1, 0)
        d$x2$y1 = 99999 << 4;
        // dir$x2$y1 = null;

        l$x_2$y_2 = l$x_1$y_1.add(Direction.SOUTHWEST); // (-2, -2) from (-1, -1)
        d$x_2$y_2 = 99999 << 4;
        // dir$x_2$y_2 = null;

        l$x_2$y2 = l$x_1$y1.add(Direction.NORTHWEST); // (-2, 2) from (-1, 1)
        d$x_2$y2 = 99999 << 4;
        // dir$x_2$y2 = null;

        l$x2$y_2 = l$x1$y_1.add(Direction.SOUTHEAST); // (2, -2) from (1, -1)
        d$x2$y_2 = 99999 << 4;
        // dir$x2$y_2 = null;

        l$x2$y2 = l$x1$y1.add(Direction.NORTHEAST); // (2, 2) from (1, 1)
        d$x2$y2 = 99999 << 4;
        // dir$x2$y2 = null;

        l$x_3$y0 = l$x_2$y0.add(Direction.WEST); // (-3, 0) from (-2, 0)
        d$x_3$y0 = 99999 << 4;
        // dir$x_3$y0 = null;

        l$x0$y_3 = l$x0$y_2.add(Direction.SOUTH); // (0, -3) from (0, -2)
        d$x0$y_3 = 99999 << 4;
        // dir$x0$y_3 = null;

        l$x0$y3 = l$x0$y2.add(Direction.NORTH); // (0, 3) from (0, 2)
        d$x0$y3 = 99999 << 4;
        // dir$x0$y3 = null;

        l$x3$y0 = l$x2$y0.add(Direction.EAST); // (3, 0) from (2, 0)
        d$x3$y0 = 99999 << 4;
        // dir$x3$y0 = null;

        l$x_3$y_1 = l$x_2$y0.add(Direction.SOUTHWEST); // (-3, -1) from (-2, 0)
        d$x_3$y_1 = 99999 << 4;
        // dir$x_3$y_1 = null;

        l$x_3$y1 = l$x_2$y0.add(Direction.NORTHWEST); // (-3, 1) from (-2, 0)
        d$x_3$y1 = 99999 << 4;
        // dir$x_3$y1 = null;

        l$x_1$y_3 = l$x0$y_2.add(Direction.SOUTHWEST); // (-1, -3) from (0, -2)
        d$x_1$y_3 = 99999 << 4;
        // dir$x_1$y_3 = null;

        l$x_1$y3 = l$x0$y2.add(Direction.NORTHWEST); // (-1, 3) from (0, 2)
        d$x_1$y3 = 99999 << 4;
        // dir$x_1$y3 = null;

        l$x1$y_3 = l$x0$y_2.add(Direction.SOUTHEAST); // (1, -3) from (0, -2)
        d$x1$y_3 = 99999 << 4;
        // dir$x1$y_3 = null;

        l$x1$y3 = l$x0$y2.add(Direction.NORTHEAST); // (1, 3) from (0, 2)
        d$x1$y3 = 99999 << 4;
        // dir$x1$y3 = null;

        l$x3$y_1 = l$x2$y0.add(Direction.SOUTHEAST); // (3, -1) from (2, 0)
        d$x3$y_1 = 99999 << 4;
        // dir$x3$y_1 = null;

        l$x3$y1 = l$x2$y0.add(Direction.NORTHEAST); // (3, 1) from (2, 0)
        d$x3$y1 = 99999 << 4;
        // dir$x3$y1 = null;

        l$x_3$y_2 = l$x_2$y_1.add(Direction.SOUTHWEST); // (-3, -2) from (-2, -1)
        d$x_3$y_2 = 99999 << 4;
        // dir$x_3$y_2 = null;

        l$x_3$y2 = l$x_2$y1.add(Direction.NORTHWEST); // (-3, 2) from (-2, 1)
        d$x_3$y2 = 99999 << 4;
        // dir$x_3$y2 = null;

        l$x_2$y_3 = l$x_1$y_2.add(Direction.SOUTHWEST); // (-2, -3) from (-1, -2)
        d$x_2$y_3 = 99999 << 4;
        // dir$x_2$y_3 = null;

        l$x_2$y3 = l$x_1$y2.add(Direction.NORTHWEST); // (-2, 3) from (-1, 2)
        d$x_2$y3 = 99999 << 4;
        // dir$x_2$y3 = null;

        l$x2$y_3 = l$x1$y_2.add(Direction.SOUTHEAST); // (2, -3) from (1, -2)
        d$x2$y_3 = 99999 << 4;
        // dir$x2$y_3 = null;

        l$x2$y3 = l$x1$y2.add(Direction.NORTHEAST); // (2, 3) from (1, 2)
        d$x2$y3 = 99999 << 4;
        // dir$x2$y3 = null;

        l$x3$y_2 = l$x2$y_1.add(Direction.SOUTHEAST); // (3, -2) from (2, -1)
        d$x3$y_2 = 99999 << 4;
        // dir$x3$y_2 = null;

        l$x3$y2 = l$x2$y1.add(Direction.NORTHEAST); // (3, 2) from (2, 1)
        d$x3$y2 = 99999 << 4;
        // dir$x3$y2 = null;

        l$x_4$y0 = l$x_3$y0.add(Direction.WEST); // (-4, 0) from (-3, 0)
        d$x_4$y0 = 99999 << 4;
        // dir$x_4$y0 = null;

        l$x0$y_4 = l$x0$y_3.add(Direction.SOUTH); // (0, -4) from (0, -3)
        d$x0$y_4 = 99999 << 4;
        // dir$x0$y_4 = null;

        l$x0$y4 = l$x0$y3.add(Direction.NORTH); // (0, 4) from (0, 3)
        d$x0$y4 = 99999 << 4;
        // dir$x0$y4 = null;

        l$x4$y0 = l$x3$y0.add(Direction.EAST); // (4, 0) from (3, 0)
        d$x4$y0 = 99999 << 4;
        // dir$x4$y0 = null;

        l$x_4$y_1 = l$x_3$y0.add(Direction.SOUTHWEST); // (-4, -1) from (-3, 0)
        d$x_4$y_1 = 99999 << 4;
        // dir$x_4$y_1 = null;

        l$x_4$y1 = l$x_3$y0.add(Direction.NORTHWEST); // (-4, 1) from (-3, 0)
        d$x_4$y1 = 99999 << 4;
        // dir$x_4$y1 = null;

        l$x_1$y_4 = l$x0$y_3.add(Direction.SOUTHWEST); // (-1, -4) from (0, -3)
        d$x_1$y_4 = 99999 << 4;
        // dir$x_1$y_4 = null;

        l$x_1$y4 = l$x0$y3.add(Direction.NORTHWEST); // (-1, 4) from (0, 3)
        d$x_1$y4 = 99999 << 4;
        // dir$x_1$y4 = null;

        l$x1$y_4 = l$x0$y_3.add(Direction.SOUTHEAST); // (1, -4) from (0, -3)
        d$x1$y_4 = 99999 << 4;
        // dir$x1$y_4 = null;

        l$x1$y4 = l$x0$y3.add(Direction.NORTHEAST); // (1, 4) from (0, 3)
        d$x1$y4 = 99999 << 4;
        // dir$x1$y4 = null;

        l$x4$y_1 = l$x3$y0.add(Direction.SOUTHEAST); // (4, -1) from (3, 0)
        d$x4$y_1 = 99999 << 4;
        // dir$x4$y_1 = null;

        l$x4$y1 = l$x3$y0.add(Direction.NORTHEAST); // (4, 1) from (3, 0)
        d$x4$y1 = 99999 << 4;
        // dir$x4$y1 = null;

        l$x_3$y_3 = l$x_2$y_2.add(Direction.SOUTHWEST); // (-3, -3) from (-2, -2)
        d$x_3$y_3 = 99999 << 4;
        // dir$x_3$y_3 = null;

        l$x_3$y3 = l$x_2$y2.add(Direction.NORTHWEST); // (-3, 3) from (-2, 2)
        d$x_3$y3 = 99999 << 4;
        // dir$x_3$y3 = null;

        l$x3$y_3 = l$x2$y_2.add(Direction.SOUTHEAST); // (3, -3) from (2, -2)
        d$x3$y_3 = 99999 << 4;
        // dir$x3$y_3 = null;

        l$x3$y3 = l$x2$y2.add(Direction.NORTHEAST); // (3, 3) from (2, 2)
        d$x3$y3 = 99999 << 4;
        // dir$x3$y3 = null;

        l$x_4$y_2 = l$x_3$y_1.add(Direction.SOUTHWEST); // (-4, -2) from (-3, -1)
        d$x_4$y_2 = 99999 << 4;
        // dir$x_4$y_2 = null;

        l$x_4$y2 = l$x_3$y1.add(Direction.NORTHWEST); // (-4, 2) from (-3, 1)
        d$x_4$y2 = 99999 << 4;
        // dir$x_4$y2 = null;

        l$x_2$y_4 = l$x_1$y_3.add(Direction.SOUTHWEST); // (-2, -4) from (-1, -3)
        d$x_2$y_4 = 99999 << 4;
        // dir$x_2$y_4 = null;

        l$x_2$y4 = l$x_1$y3.add(Direction.NORTHWEST); // (-2, 4) from (-1, 3)
        d$x_2$y4 = 99999 << 4;
        // dir$x_2$y4 = null;

        l$x2$y_4 = l$x1$y_3.add(Direction.SOUTHEAST); // (2, -4) from (1, -3)
        d$x2$y_4 = 99999 << 4;
        // dir$x2$y_4 = null;

        l$x2$y4 = l$x1$y3.add(Direction.NORTHEAST); // (2, 4) from (1, 3)
        d$x2$y4 = 99999 << 4;
        // dir$x2$y4 = null;

        l$x4$y_2 = l$x3$y_1.add(Direction.SOUTHEAST); // (4, -2) from (3, -1)
        d$x4$y_2 = 99999 << 4;
        // dir$x4$y_2 = null;

        l$x4$y2 = l$x3$y1.add(Direction.NORTHEAST); // (4, 2) from (3, 1)
        d$x4$y2 = 99999 << 4;
        // dir$x4$y2 = null;



        int moveCooldown = RobotType.AMPLIFIER.movementCooldown;
        int shiftedMoveCD = moveCooldown << 4;
        boolean notNearEdge = !(l$x0$y0.x <= 4.47213595499958 || l$x0$y0.y <= 4.47213595499958 || rc.getMapWidth() - l$x0$y0.x <= 4.47213595499958 || rc.getMapHeight() - l$x0$y0.y <= 4.47213595499958);

        if (notNearEdge || rc.onTheMap(l$x_1$y0)) { // check (-1, 0)
          if (rc.sensePassability(l$x_1$y0) && !rc.isLocationOccupied(l$x_1$y0)) { 
            d$x_1$y0 = d$x0$y0 | 7; // from (0, 0)
            // dir$x_1$y0 = Direction.WEST;

            d$x_1$y0 += ((int) (rc.senseMapInfo(l$x_1$y0).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y_1)) { // check (0, -1)
          if (rc.sensePassability(l$x0$y_1) && !rc.isLocationOccupied(l$x0$y_1)) { 
            d$x0$y_1 = d$x0$y0 | 5; // from (0, 0)
            // dir$x0$y_1 = Direction.SOUTH;

            d$x0$y_1 += ((int) (rc.senseMapInfo(l$x0$y_1).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y1)) { // check (0, 1)
          if (rc.sensePassability(l$x0$y1) && !rc.isLocationOccupied(l$x0$y1)) { 
            d$x0$y1 = d$x0$y0 | 1; // from (0, 0)
            // dir$x0$y1 = Direction.NORTH;

            d$x0$y1 += ((int) (rc.senseMapInfo(l$x0$y1).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y0)) { // check (1, 0)
          if (rc.sensePassability(l$x1$y0) && !rc.isLocationOccupied(l$x1$y0)) { 
            d$x1$y0 = d$x0$y0 | 3; // from (0, 0)
            // dir$x1$y0 = Direction.EAST;

            d$x1$y0 += ((int) (rc.senseMapInfo(l$x1$y0).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y_1)) { // check (-1, -1)
          if (rc.sensePassability(l$x_1$y_1) && !rc.isLocationOccupied(l$x_1$y_1)) { 
            d$x_1$y_1 = d$x0$y0 | 6; // from (0, 0)
            // dir$x_1$y_1 = Direction.SOUTHWEST;

            d$x_1$y_1 += ((int) (rc.senseMapInfo(l$x_1$y_1).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y1)) { // check (-1, 1)
          if (rc.sensePassability(l$x_1$y1) && !rc.isLocationOccupied(l$x_1$y1)) { 
            d$x_1$y1 = d$x0$y0 | 8; // from (0, 0)
            // dir$x_1$y1 = Direction.NORTHWEST;

            d$x_1$y1 += ((int) (rc.senseMapInfo(l$x_1$y1).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y_1)) { // check (1, -1)
          if (rc.sensePassability(l$x1$y_1) && !rc.isLocationOccupied(l$x1$y_1)) { 
            d$x1$y_1 = d$x0$y0 | 4; // from (0, 0)
            // dir$x1$y_1 = Direction.SOUTHEAST;

            d$x1$y_1 += ((int) (rc.senseMapInfo(l$x1$y_1).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y1)) { // check (1, 1)
          if (rc.sensePassability(l$x1$y1) && !rc.isLocationOccupied(l$x1$y1)) { 
            d$x1$y1 = d$x0$y0 | 2; // from (0, 0)
            // dir$x1$y1 = Direction.NORTHEAST;

            d$x1$y1 += ((int) (rc.senseMapInfo(l$x1$y1).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << 4;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y0)) { // check (-2, 0)
          if ((!rc.canSenseLocation(l$x_2$y0) || rc.sensePassability(l$x_2$y0))) { 
            if (d$x_2$y0 > d$x_1$y0) { // from (-1, 0)
                d$x_2$y0 = d$x_1$y0;
                // dir$x_2$y0 = dir$x_1$y0;
            }
            if (d$x_2$y0 > d$x_1$y_1) { // from (-1, -1)
                d$x_2$y0 = d$x_1$y_1;
                // dir$x_2$y0 = dir$x_1$y_1;
            }
            if (d$x_2$y0 > d$x_1$y1) { // from (-1, 1)
                d$x_2$y0 = d$x_1$y1;
                // dir$x_2$y0 = dir$x_1$y1;
            }
            d$x_2$y0 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y_2)) { // check (0, -2)
          if ((!rc.canSenseLocation(l$x0$y_2) || rc.sensePassability(l$x0$y_2))) { 
            if (d$x0$y_2 > d$x0$y_1) { // from (0, -1)
                d$x0$y_2 = d$x0$y_1;
                // dir$x0$y_2 = dir$x0$y_1;
            }
            if (d$x0$y_2 > d$x_1$y_1) { // from (-1, -1)
                d$x0$y_2 = d$x_1$y_1;
                // dir$x0$y_2 = dir$x_1$y_1;
            }
            if (d$x0$y_2 > d$x1$y_1) { // from (1, -1)
                d$x0$y_2 = d$x1$y_1;
                // dir$x0$y_2 = dir$x1$y_1;
            }
            d$x0$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y2)) { // check (0, 2)
          if ((!rc.canSenseLocation(l$x0$y2) || rc.sensePassability(l$x0$y2))) { 
            if (d$x0$y2 > d$x0$y1) { // from (0, 1)
                d$x0$y2 = d$x0$y1;
                // dir$x0$y2 = dir$x0$y1;
            }
            if (d$x0$y2 > d$x_1$y1) { // from (-1, 1)
                d$x0$y2 = d$x_1$y1;
                // dir$x0$y2 = dir$x_1$y1;
            }
            if (d$x0$y2 > d$x1$y1) { // from (1, 1)
                d$x0$y2 = d$x1$y1;
                // dir$x0$y2 = dir$x1$y1;
            }
            d$x0$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y0)) { // check (2, 0)
          if ((!rc.canSenseLocation(l$x2$y0) || rc.sensePassability(l$x2$y0))) { 
            if (d$x2$y0 > d$x1$y0) { // from (1, 0)
                d$x2$y0 = d$x1$y0;
                // dir$x2$y0 = dir$x1$y0;
            }
            if (d$x2$y0 > d$x1$y_1) { // from (1, -1)
                d$x2$y0 = d$x1$y_1;
                // dir$x2$y0 = dir$x1$y_1;
            }
            if (d$x2$y0 > d$x1$y1) { // from (1, 1)
                d$x2$y0 = d$x1$y1;
                // dir$x2$y0 = dir$x1$y1;
            }
            d$x2$y0 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y_1)) { // check (-2, -1)
          if ((!rc.canSenseLocation(l$x_2$y_1) || rc.sensePassability(l$x_2$y_1))) { 
            if (d$x_2$y_1 > d$x_1$y0) { // from (-1, 0)
                d$x_2$y_1 = d$x_1$y0;
                // dir$x_2$y_1 = dir$x_1$y0;
            }
            if (d$x_2$y_1 > d$x_1$y_1) { // from (-1, -1)
                d$x_2$y_1 = d$x_1$y_1;
                // dir$x_2$y_1 = dir$x_1$y_1;
            }
            if (d$x_2$y_1 > d$x_2$y0) { // from (-2, 0)
                d$x_2$y_1 = d$x_2$y0;
                // dir$x_2$y_1 = dir$x_2$y0;
            }
            d$x_2$y_1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y1)) { // check (-2, 1)
          if ((!rc.canSenseLocation(l$x_2$y1) || rc.sensePassability(l$x_2$y1))) { 
            if (d$x_2$y1 > d$x_1$y0) { // from (-1, 0)
                d$x_2$y1 = d$x_1$y0;
                // dir$x_2$y1 = dir$x_1$y0;
            }
            if (d$x_2$y1 > d$x_1$y1) { // from (-1, 1)
                d$x_2$y1 = d$x_1$y1;
                // dir$x_2$y1 = dir$x_1$y1;
            }
            if (d$x_2$y1 > d$x_2$y0) { // from (-2, 0)
                d$x_2$y1 = d$x_2$y0;
                // dir$x_2$y1 = dir$x_2$y0;
            }
            d$x_2$y1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y_2)) { // check (-1, -2)
          if ((!rc.canSenseLocation(l$x_1$y_2) || rc.sensePassability(l$x_1$y_2))) { 
            if (d$x_1$y_2 > d$x0$y_1) { // from (0, -1)
                d$x_1$y_2 = d$x0$y_1;
                // dir$x_1$y_2 = dir$x0$y_1;
            }
            if (d$x_1$y_2 > d$x_1$y_1) { // from (-1, -1)
                d$x_1$y_2 = d$x_1$y_1;
                // dir$x_1$y_2 = dir$x_1$y_1;
            }
            if (d$x_1$y_2 > d$x0$y_2) { // from (0, -2)
                d$x_1$y_2 = d$x0$y_2;
                // dir$x_1$y_2 = dir$x0$y_2;
            }
            if (d$x_1$y_2 > d$x_2$y_1) { // from (-2, -1)
                d$x_1$y_2 = d$x_2$y_1;
                // dir$x_1$y_2 = dir$x_2$y_1;
            }
            d$x_1$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y2)) { // check (-1, 2)
          if ((!rc.canSenseLocation(l$x_1$y2) || rc.sensePassability(l$x_1$y2))) { 
            if (d$x_1$y2 > d$x0$y1) { // from (0, 1)
                d$x_1$y2 = d$x0$y1;
                // dir$x_1$y2 = dir$x0$y1;
            }
            if (d$x_1$y2 > d$x_1$y1) { // from (-1, 1)
                d$x_1$y2 = d$x_1$y1;
                // dir$x_1$y2 = dir$x_1$y1;
            }
            if (d$x_1$y2 > d$x0$y2) { // from (0, 2)
                d$x_1$y2 = d$x0$y2;
                // dir$x_1$y2 = dir$x0$y2;
            }
            if (d$x_1$y2 > d$x_2$y1) { // from (-2, 1)
                d$x_1$y2 = d$x_2$y1;
                // dir$x_1$y2 = dir$x_2$y1;
            }
            d$x_1$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y_2)) { // check (1, -2)
          if ((!rc.canSenseLocation(l$x1$y_2) || rc.sensePassability(l$x1$y_2))) { 
            if (d$x1$y_2 > d$x0$y_1) { // from (0, -1)
                d$x1$y_2 = d$x0$y_1;
                // dir$x1$y_2 = dir$x0$y_1;
            }
            if (d$x1$y_2 > d$x1$y_1) { // from (1, -1)
                d$x1$y_2 = d$x1$y_1;
                // dir$x1$y_2 = dir$x1$y_1;
            }
            if (d$x1$y_2 > d$x0$y_2) { // from (0, -2)
                d$x1$y_2 = d$x0$y_2;
                // dir$x1$y_2 = dir$x0$y_2;
            }
            d$x1$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y2)) { // check (1, 2)
          if ((!rc.canSenseLocation(l$x1$y2) || rc.sensePassability(l$x1$y2))) { 
            if (d$x1$y2 > d$x0$y1) { // from (0, 1)
                d$x1$y2 = d$x0$y1;
                // dir$x1$y2 = dir$x0$y1;
            }
            if (d$x1$y2 > d$x1$y1) { // from (1, 1)
                d$x1$y2 = d$x1$y1;
                // dir$x1$y2 = dir$x1$y1;
            }
            if (d$x1$y2 > d$x0$y2) { // from (0, 2)
                d$x1$y2 = d$x0$y2;
                // dir$x1$y2 = dir$x0$y2;
            }
            d$x1$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y_1)) { // check (2, -1)
          if ((!rc.canSenseLocation(l$x2$y_1) || rc.sensePassability(l$x2$y_1))) { 
            if (d$x2$y_1 > d$x1$y0) { // from (1, 0)
                d$x2$y_1 = d$x1$y0;
                // dir$x2$y_1 = dir$x1$y0;
            }
            if (d$x2$y_1 > d$x1$y_1) { // from (1, -1)
                d$x2$y_1 = d$x1$y_1;
                // dir$x2$y_1 = dir$x1$y_1;
            }
            if (d$x2$y_1 > d$x2$y0) { // from (2, 0)
                d$x2$y_1 = d$x2$y0;
                // dir$x2$y_1 = dir$x2$y0;
            }
            if (d$x2$y_1 > d$x1$y_2) { // from (1, -2)
                d$x2$y_1 = d$x1$y_2;
                // dir$x2$y_1 = dir$x1$y_2;
            }
            d$x2$y_1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y1)) { // check (2, 1)
          if ((!rc.canSenseLocation(l$x2$y1) || rc.sensePassability(l$x2$y1))) { 
            if (d$x2$y1 > d$x1$y0) { // from (1, 0)
                d$x2$y1 = d$x1$y0;
                // dir$x2$y1 = dir$x1$y0;
            }
            if (d$x2$y1 > d$x1$y1) { // from (1, 1)
                d$x2$y1 = d$x1$y1;
                // dir$x2$y1 = dir$x1$y1;
            }
            if (d$x2$y1 > d$x2$y0) { // from (2, 0)
                d$x2$y1 = d$x2$y0;
                // dir$x2$y1 = dir$x2$y0;
            }
            if (d$x2$y1 > d$x1$y2) { // from (1, 2)
                d$x2$y1 = d$x1$y2;
                // dir$x2$y1 = dir$x1$y2;
            }
            d$x2$y1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y_2)) { // check (-2, -2)
          if ((!rc.canSenseLocation(l$x_2$y_2) || rc.sensePassability(l$x_2$y_2))) { 
            if (d$x_2$y_2 > d$x_1$y_1) { // from (-1, -1)
                d$x_2$y_2 = d$x_1$y_1;
                // dir$x_2$y_2 = dir$x_1$y_1;
            }
            if (d$x_2$y_2 > d$x_2$y_1) { // from (-2, -1)
                d$x_2$y_2 = d$x_2$y_1;
                // dir$x_2$y_2 = dir$x_2$y_1;
            }
            if (d$x_2$y_2 > d$x_1$y_2) { // from (-1, -2)
                d$x_2$y_2 = d$x_1$y_2;
                // dir$x_2$y_2 = dir$x_1$y_2;
            }
            d$x_2$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y2)) { // check (-2, 2)
          if ((!rc.canSenseLocation(l$x_2$y2) || rc.sensePassability(l$x_2$y2))) { 
            if (d$x_2$y2 > d$x_1$y1) { // from (-1, 1)
                d$x_2$y2 = d$x_1$y1;
                // dir$x_2$y2 = dir$x_1$y1;
            }
            if (d$x_2$y2 > d$x_2$y1) { // from (-2, 1)
                d$x_2$y2 = d$x_2$y1;
                // dir$x_2$y2 = dir$x_2$y1;
            }
            if (d$x_2$y2 > d$x_1$y2) { // from (-1, 2)
                d$x_2$y2 = d$x_1$y2;
                // dir$x_2$y2 = dir$x_1$y2;
            }
            d$x_2$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y_2)) { // check (2, -2)
          if ((!rc.canSenseLocation(l$x2$y_2) || rc.sensePassability(l$x2$y_2))) { 
            if (d$x2$y_2 > d$x1$y_1) { // from (1, -1)
                d$x2$y_2 = d$x1$y_1;
                // dir$x2$y_2 = dir$x1$y_1;
            }
            if (d$x2$y_2 > d$x1$y_2) { // from (1, -2)
                d$x2$y_2 = d$x1$y_2;
                // dir$x2$y_2 = dir$x1$y_2;
            }
            if (d$x2$y_2 > d$x2$y_1) { // from (2, -1)
                d$x2$y_2 = d$x2$y_1;
                // dir$x2$y_2 = dir$x2$y_1;
            }
            d$x2$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y2)) { // check (2, 2)
          if ((!rc.canSenseLocation(l$x2$y2) || rc.sensePassability(l$x2$y2))) { 
            if (d$x2$y2 > d$x1$y1) { // from (1, 1)
                d$x2$y2 = d$x1$y1;
                // dir$x2$y2 = dir$x1$y1;
            }
            if (d$x2$y2 > d$x1$y2) { // from (1, 2)
                d$x2$y2 = d$x1$y2;
                // dir$x2$y2 = dir$x1$y2;
            }
            if (d$x2$y2 > d$x2$y1) { // from (2, 1)
                d$x2$y2 = d$x2$y1;
                // dir$x2$y2 = dir$x2$y1;
            }
            d$x2$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y0)) { // check (-3, 0)
          if ((!rc.canSenseLocation(l$x_3$y0) || rc.sensePassability(l$x_3$y0))) { 
            if (d$x_3$y0 > d$x_2$y0) { // from (-2, 0)
                d$x_3$y0 = d$x_2$y0;
                // dir$x_3$y0 = dir$x_2$y0;
            }
            if (d$x_3$y0 > d$x_2$y_1) { // from (-2, -1)
                d$x_3$y0 = d$x_2$y_1;
                // dir$x_3$y0 = dir$x_2$y_1;
            }
            if (d$x_3$y0 > d$x_2$y1) { // from (-2, 1)
                d$x_3$y0 = d$x_2$y1;
                // dir$x_3$y0 = dir$x_2$y1;
            }
            d$x_3$y0 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y_3)) { // check (0, -3)
          if ((!rc.canSenseLocation(l$x0$y_3) || rc.sensePassability(l$x0$y_3))) { 
            if (d$x0$y_3 > d$x0$y_2) { // from (0, -2)
                d$x0$y_3 = d$x0$y_2;
                // dir$x0$y_3 = dir$x0$y_2;
            }
            if (d$x0$y_3 > d$x_1$y_2) { // from (-1, -2)
                d$x0$y_3 = d$x_1$y_2;
                // dir$x0$y_3 = dir$x_1$y_2;
            }
            if (d$x0$y_3 > d$x1$y_2) { // from (1, -2)
                d$x0$y_3 = d$x1$y_2;
                // dir$x0$y_3 = dir$x1$y_2;
            }
            d$x0$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y3)) { // check (0, 3)
          if ((!rc.canSenseLocation(l$x0$y3) || rc.sensePassability(l$x0$y3))) { 
            if (d$x0$y3 > d$x0$y2) { // from (0, 2)
                d$x0$y3 = d$x0$y2;
                // dir$x0$y3 = dir$x0$y2;
            }
            if (d$x0$y3 > d$x_1$y2) { // from (-1, 2)
                d$x0$y3 = d$x_1$y2;
                // dir$x0$y3 = dir$x_1$y2;
            }
            if (d$x0$y3 > d$x1$y2) { // from (1, 2)
                d$x0$y3 = d$x1$y2;
                // dir$x0$y3 = dir$x1$y2;
            }
            d$x0$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y0)) { // check (3, 0)
          if ((!rc.canSenseLocation(l$x3$y0) || rc.sensePassability(l$x3$y0))) { 
            if (d$x3$y0 > d$x2$y0) { // from (2, 0)
                d$x3$y0 = d$x2$y0;
                // dir$x3$y0 = dir$x2$y0;
            }
            if (d$x3$y0 > d$x2$y_1) { // from (2, -1)
                d$x3$y0 = d$x2$y_1;
                // dir$x3$y0 = dir$x2$y_1;
            }
            if (d$x3$y0 > d$x2$y1) { // from (2, 1)
                d$x3$y0 = d$x2$y1;
                // dir$x3$y0 = dir$x2$y1;
            }
            d$x3$y0 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y_1)) { // check (-3, -1)
          if ((!rc.canSenseLocation(l$x_3$y_1) || rc.sensePassability(l$x_3$y_1))) { 
            if (d$x_3$y_1 > d$x_2$y0) { // from (-2, 0)
                d$x_3$y_1 = d$x_2$y0;
                // dir$x_3$y_1 = dir$x_2$y0;
            }
            if (d$x_3$y_1 > d$x_2$y_1) { // from (-2, -1)
                d$x_3$y_1 = d$x_2$y_1;
                // dir$x_3$y_1 = dir$x_2$y_1;
            }
            if (d$x_3$y_1 > d$x_2$y_2) { // from (-2, -2)
                d$x_3$y_1 = d$x_2$y_2;
                // dir$x_3$y_1 = dir$x_2$y_2;
            }
            if (d$x_3$y_1 > d$x_3$y0) { // from (-3, 0)
                d$x_3$y_1 = d$x_3$y0;
                // dir$x_3$y_1 = dir$x_3$y0;
            }
            d$x_3$y_1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y1)) { // check (-3, 1)
          if ((!rc.canSenseLocation(l$x_3$y1) || rc.sensePassability(l$x_3$y1))) { 
            if (d$x_3$y1 > d$x_2$y0) { // from (-2, 0)
                d$x_3$y1 = d$x_2$y0;
                // dir$x_3$y1 = dir$x_2$y0;
            }
            if (d$x_3$y1 > d$x_2$y1) { // from (-2, 1)
                d$x_3$y1 = d$x_2$y1;
                // dir$x_3$y1 = dir$x_2$y1;
            }
            if (d$x_3$y1 > d$x_2$y2) { // from (-2, 2)
                d$x_3$y1 = d$x_2$y2;
                // dir$x_3$y1 = dir$x_2$y2;
            }
            if (d$x_3$y1 > d$x_3$y0) { // from (-3, 0)
                d$x_3$y1 = d$x_3$y0;
                // dir$x_3$y1 = dir$x_3$y0;
            }
            d$x_3$y1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y_3)) { // check (-1, -3)
          if ((!rc.canSenseLocation(l$x_1$y_3) || rc.sensePassability(l$x_1$y_3))) { 
            if (d$x_1$y_3 > d$x0$y_2) { // from (0, -2)
                d$x_1$y_3 = d$x0$y_2;
                // dir$x_1$y_3 = dir$x0$y_2;
            }
            if (d$x_1$y_3 > d$x_1$y_2) { // from (-1, -2)
                d$x_1$y_3 = d$x_1$y_2;
                // dir$x_1$y_3 = dir$x_1$y_2;
            }
            if (d$x_1$y_3 > d$x_2$y_2) { // from (-2, -2)
                d$x_1$y_3 = d$x_2$y_2;
                // dir$x_1$y_3 = dir$x_2$y_2;
            }
            if (d$x_1$y_3 > d$x0$y_3) { // from (0, -3)
                d$x_1$y_3 = d$x0$y_3;
                // dir$x_1$y_3 = dir$x0$y_3;
            }
            d$x_1$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y3)) { // check (-1, 3)
          if ((!rc.canSenseLocation(l$x_1$y3) || rc.sensePassability(l$x_1$y3))) { 
            if (d$x_1$y3 > d$x0$y2) { // from (0, 2)
                d$x_1$y3 = d$x0$y2;
                // dir$x_1$y3 = dir$x0$y2;
            }
            if (d$x_1$y3 > d$x_1$y2) { // from (-1, 2)
                d$x_1$y3 = d$x_1$y2;
                // dir$x_1$y3 = dir$x_1$y2;
            }
            if (d$x_1$y3 > d$x_2$y2) { // from (-2, 2)
                d$x_1$y3 = d$x_2$y2;
                // dir$x_1$y3 = dir$x_2$y2;
            }
            if (d$x_1$y3 > d$x0$y3) { // from (0, 3)
                d$x_1$y3 = d$x0$y3;
                // dir$x_1$y3 = dir$x0$y3;
            }
            d$x_1$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y_3)) { // check (1, -3)
          if ((!rc.canSenseLocation(l$x1$y_3) || rc.sensePassability(l$x1$y_3))) { 
            if (d$x1$y_3 > d$x0$y_2) { // from (0, -2)
                d$x1$y_3 = d$x0$y_2;
                // dir$x1$y_3 = dir$x0$y_2;
            }
            if (d$x1$y_3 > d$x1$y_2) { // from (1, -2)
                d$x1$y_3 = d$x1$y_2;
                // dir$x1$y_3 = dir$x1$y_2;
            }
            if (d$x1$y_3 > d$x2$y_2) { // from (2, -2)
                d$x1$y_3 = d$x2$y_2;
                // dir$x1$y_3 = dir$x2$y_2;
            }
            if (d$x1$y_3 > d$x0$y_3) { // from (0, -3)
                d$x1$y_3 = d$x0$y_3;
                // dir$x1$y_3 = dir$x0$y_3;
            }
            d$x1$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y3)) { // check (1, 3)
          if ((!rc.canSenseLocation(l$x1$y3) || rc.sensePassability(l$x1$y3))) { 
            if (d$x1$y3 > d$x0$y2) { // from (0, 2)
                d$x1$y3 = d$x0$y2;
                // dir$x1$y3 = dir$x0$y2;
            }
            if (d$x1$y3 > d$x1$y2) { // from (1, 2)
                d$x1$y3 = d$x1$y2;
                // dir$x1$y3 = dir$x1$y2;
            }
            if (d$x1$y3 > d$x2$y2) { // from (2, 2)
                d$x1$y3 = d$x2$y2;
                // dir$x1$y3 = dir$x2$y2;
            }
            if (d$x1$y3 > d$x0$y3) { // from (0, 3)
                d$x1$y3 = d$x0$y3;
                // dir$x1$y3 = dir$x0$y3;
            }
            d$x1$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y_1)) { // check (3, -1)
          if ((!rc.canSenseLocation(l$x3$y_1) || rc.sensePassability(l$x3$y_1))) { 
            if (d$x3$y_1 > d$x2$y0) { // from (2, 0)
                d$x3$y_1 = d$x2$y0;
                // dir$x3$y_1 = dir$x2$y0;
            }
            if (d$x3$y_1 > d$x2$y_1) { // from (2, -1)
                d$x3$y_1 = d$x2$y_1;
                // dir$x3$y_1 = dir$x2$y_1;
            }
            if (d$x3$y_1 > d$x2$y_2) { // from (2, -2)
                d$x3$y_1 = d$x2$y_2;
                // dir$x3$y_1 = dir$x2$y_2;
            }
            if (d$x3$y_1 > d$x3$y0) { // from (3, 0)
                d$x3$y_1 = d$x3$y0;
                // dir$x3$y_1 = dir$x3$y0;
            }
            d$x3$y_1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y1)) { // check (3, 1)
          if ((!rc.canSenseLocation(l$x3$y1) || rc.sensePassability(l$x3$y1))) { 
            if (d$x3$y1 > d$x2$y0) { // from (2, 0)
                d$x3$y1 = d$x2$y0;
                // dir$x3$y1 = dir$x2$y0;
            }
            if (d$x3$y1 > d$x2$y1) { // from (2, 1)
                d$x3$y1 = d$x2$y1;
                // dir$x3$y1 = dir$x2$y1;
            }
            if (d$x3$y1 > d$x2$y2) { // from (2, 2)
                d$x3$y1 = d$x2$y2;
                // dir$x3$y1 = dir$x2$y2;
            }
            if (d$x3$y1 > d$x3$y0) { // from (3, 0)
                d$x3$y1 = d$x3$y0;
                // dir$x3$y1 = dir$x3$y0;
            }
            d$x3$y1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y_2)) { // check (-3, -2)
          if ((!rc.canSenseLocation(l$x_3$y_2) || rc.sensePassability(l$x_3$y_2))) { 
            if (d$x_3$y_2 > d$x_2$y_1) { // from (-2, -1)
                d$x_3$y_2 = d$x_2$y_1;
                // dir$x_3$y_2 = dir$x_2$y_1;
            }
            if (d$x_3$y_2 > d$x_2$y_2) { // from (-2, -2)
                d$x_3$y_2 = d$x_2$y_2;
                // dir$x_3$y_2 = dir$x_2$y_2;
            }
            if (d$x_3$y_2 > d$x_3$y_1) { // from (-3, -1)
                d$x_3$y_2 = d$x_3$y_1;
                // dir$x_3$y_2 = dir$x_3$y_1;
            }
            d$x_3$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y2)) { // check (-3, 2)
          if ((!rc.canSenseLocation(l$x_3$y2) || rc.sensePassability(l$x_3$y2))) { 
            if (d$x_3$y2 > d$x_2$y1) { // from (-2, 1)
                d$x_3$y2 = d$x_2$y1;
                // dir$x_3$y2 = dir$x_2$y1;
            }
            if (d$x_3$y2 > d$x_2$y2) { // from (-2, 2)
                d$x_3$y2 = d$x_2$y2;
                // dir$x_3$y2 = dir$x_2$y2;
            }
            if (d$x_3$y2 > d$x_3$y1) { // from (-3, 1)
                d$x_3$y2 = d$x_3$y1;
                // dir$x_3$y2 = dir$x_3$y1;
            }
            d$x_3$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y_3)) { // check (-2, -3)
          if ((!rc.canSenseLocation(l$x_2$y_3) || rc.sensePassability(l$x_2$y_3))) { 
            if (d$x_2$y_3 > d$x_1$y_2) { // from (-1, -2)
                d$x_2$y_3 = d$x_1$y_2;
                // dir$x_2$y_3 = dir$x_1$y_2;
            }
            if (d$x_2$y_3 > d$x_2$y_2) { // from (-2, -2)
                d$x_2$y_3 = d$x_2$y_2;
                // dir$x_2$y_3 = dir$x_2$y_2;
            }
            if (d$x_2$y_3 > d$x_1$y_3) { // from (-1, -3)
                d$x_2$y_3 = d$x_1$y_3;
                // dir$x_2$y_3 = dir$x_1$y_3;
            }
            if (d$x_2$y_3 > d$x_3$y_2) { // from (-3, -2)
                d$x_2$y_3 = d$x_3$y_2;
                // dir$x_2$y_3 = dir$x_3$y_2;
            }
            d$x_2$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y3)) { // check (-2, 3)
          if ((!rc.canSenseLocation(l$x_2$y3) || rc.sensePassability(l$x_2$y3))) { 
            if (d$x_2$y3 > d$x_1$y2) { // from (-1, 2)
                d$x_2$y3 = d$x_1$y2;
                // dir$x_2$y3 = dir$x_1$y2;
            }
            if (d$x_2$y3 > d$x_2$y2) { // from (-2, 2)
                d$x_2$y3 = d$x_2$y2;
                // dir$x_2$y3 = dir$x_2$y2;
            }
            if (d$x_2$y3 > d$x_1$y3) { // from (-1, 3)
                d$x_2$y3 = d$x_1$y3;
                // dir$x_2$y3 = dir$x_1$y3;
            }
            if (d$x_2$y3 > d$x_3$y2) { // from (-3, 2)
                d$x_2$y3 = d$x_3$y2;
                // dir$x_2$y3 = dir$x_3$y2;
            }
            d$x_2$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y_3)) { // check (2, -3)
          if ((!rc.canSenseLocation(l$x2$y_3) || rc.sensePassability(l$x2$y_3))) { 
            if (d$x2$y_3 > d$x1$y_2) { // from (1, -2)
                d$x2$y_3 = d$x1$y_2;
                // dir$x2$y_3 = dir$x1$y_2;
            }
            if (d$x2$y_3 > d$x2$y_2) { // from (2, -2)
                d$x2$y_3 = d$x2$y_2;
                // dir$x2$y_3 = dir$x2$y_2;
            }
            if (d$x2$y_3 > d$x1$y_3) { // from (1, -3)
                d$x2$y_3 = d$x1$y_3;
                // dir$x2$y_3 = dir$x1$y_3;
            }
            d$x2$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y3)) { // check (2, 3)
          if ((!rc.canSenseLocation(l$x2$y3) || rc.sensePassability(l$x2$y3))) { 
            if (d$x2$y3 > d$x1$y2) { // from (1, 2)
                d$x2$y3 = d$x1$y2;
                // dir$x2$y3 = dir$x1$y2;
            }
            if (d$x2$y3 > d$x2$y2) { // from (2, 2)
                d$x2$y3 = d$x2$y2;
                // dir$x2$y3 = dir$x2$y2;
            }
            if (d$x2$y3 > d$x1$y3) { // from (1, 3)
                d$x2$y3 = d$x1$y3;
                // dir$x2$y3 = dir$x1$y3;
            }
            d$x2$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y_2)) { // check (3, -2)
          if ((!rc.canSenseLocation(l$x3$y_2) || rc.sensePassability(l$x3$y_2))) { 
            if (d$x3$y_2 > d$x2$y_1) { // from (2, -1)
                d$x3$y_2 = d$x2$y_1;
                // dir$x3$y_2 = dir$x2$y_1;
            }
            if (d$x3$y_2 > d$x2$y_2) { // from (2, -2)
                d$x3$y_2 = d$x2$y_2;
                // dir$x3$y_2 = dir$x2$y_2;
            }
            if (d$x3$y_2 > d$x3$y_1) { // from (3, -1)
                d$x3$y_2 = d$x3$y_1;
                // dir$x3$y_2 = dir$x3$y_1;
            }
            if (d$x3$y_2 > d$x2$y_3) { // from (2, -3)
                d$x3$y_2 = d$x2$y_3;
                // dir$x3$y_2 = dir$x2$y_3;
            }
            d$x3$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y2)) { // check (3, 2)
          if ((!rc.canSenseLocation(l$x3$y2) || rc.sensePassability(l$x3$y2))) { 
            if (d$x3$y2 > d$x2$y1) { // from (2, 1)
                d$x3$y2 = d$x2$y1;
                // dir$x3$y2 = dir$x2$y1;
            }
            if (d$x3$y2 > d$x2$y2) { // from (2, 2)
                d$x3$y2 = d$x2$y2;
                // dir$x3$y2 = dir$x2$y2;
            }
            if (d$x3$y2 > d$x3$y1) { // from (3, 1)
                d$x3$y2 = d$x3$y1;
                // dir$x3$y2 = dir$x3$y1;
            }
            if (d$x3$y2 > d$x2$y3) { // from (2, 3)
                d$x3$y2 = d$x2$y3;
                // dir$x3$y2 = dir$x2$y3;
            }
            d$x3$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_4$y0)) { // check (-4, 0)
          if ((!rc.canSenseLocation(l$x_4$y0) || rc.sensePassability(l$x_4$y0))) { 
            if (d$x_4$y0 > d$x_3$y0) { // from (-3, 0)
                d$x_4$y0 = d$x_3$y0;
                // dir$x_4$y0 = dir$x_3$y0;
            }
            if (d$x_4$y0 > d$x_3$y_1) { // from (-3, -1)
                d$x_4$y0 = d$x_3$y_1;
                // dir$x_4$y0 = dir$x_3$y_1;
            }
            if (d$x_4$y0 > d$x_3$y1) { // from (-3, 1)
                d$x_4$y0 = d$x_3$y1;
                // dir$x_4$y0 = dir$x_3$y1;
            }
            d$x_4$y0 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y_4)) { // check (0, -4)
          if ((!rc.canSenseLocation(l$x0$y_4) || rc.sensePassability(l$x0$y_4))) { 
            if (d$x0$y_4 > d$x0$y_3) { // from (0, -3)
                d$x0$y_4 = d$x0$y_3;
                // dir$x0$y_4 = dir$x0$y_3;
            }
            if (d$x0$y_4 > d$x_1$y_3) { // from (-1, -3)
                d$x0$y_4 = d$x_1$y_3;
                // dir$x0$y_4 = dir$x_1$y_3;
            }
            if (d$x0$y_4 > d$x1$y_3) { // from (1, -3)
                d$x0$y_4 = d$x1$y_3;
                // dir$x0$y_4 = dir$x1$y_3;
            }
            d$x0$y_4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x0$y4)) { // check (0, 4)
          if ((!rc.canSenseLocation(l$x0$y4) || rc.sensePassability(l$x0$y4))) { 
            if (d$x0$y4 > d$x0$y3) { // from (0, 3)
                d$x0$y4 = d$x0$y3;
                // dir$x0$y4 = dir$x0$y3;
            }
            if (d$x0$y4 > d$x_1$y3) { // from (-1, 3)
                d$x0$y4 = d$x_1$y3;
                // dir$x0$y4 = dir$x_1$y3;
            }
            if (d$x0$y4 > d$x1$y3) { // from (1, 3)
                d$x0$y4 = d$x1$y3;
                // dir$x0$y4 = dir$x1$y3;
            }
            d$x0$y4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x4$y0)) { // check (4, 0)
          if ((!rc.canSenseLocation(l$x4$y0) || rc.sensePassability(l$x4$y0))) { 
            if (d$x4$y0 > d$x3$y0) { // from (3, 0)
                d$x4$y0 = d$x3$y0;
                // dir$x4$y0 = dir$x3$y0;
            }
            if (d$x4$y0 > d$x3$y_1) { // from (3, -1)
                d$x4$y0 = d$x3$y_1;
                // dir$x4$y0 = dir$x3$y_1;
            }
            if (d$x4$y0 > d$x3$y1) { // from (3, 1)
                d$x4$y0 = d$x3$y1;
                // dir$x4$y0 = dir$x3$y1;
            }
            d$x4$y0 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_4$y_1)) { // check (-4, -1)
          if ((!rc.canSenseLocation(l$x_4$y_1) || rc.sensePassability(l$x_4$y_1))) { 
            if (d$x_4$y_1 > d$x_3$y0) { // from (-3, 0)
                d$x_4$y_1 = d$x_3$y0;
                // dir$x_4$y_1 = dir$x_3$y0;
            }
            if (d$x_4$y_1 > d$x_3$y_1) { // from (-3, -1)
                d$x_4$y_1 = d$x_3$y_1;
                // dir$x_4$y_1 = dir$x_3$y_1;
            }
            if (d$x_4$y_1 > d$x_3$y_2) { // from (-3, -2)
                d$x_4$y_1 = d$x_3$y_2;
                // dir$x_4$y_1 = dir$x_3$y_2;
            }
            if (d$x_4$y_1 > d$x_4$y0) { // from (-4, 0)
                d$x_4$y_1 = d$x_4$y0;
                // dir$x_4$y_1 = dir$x_4$y0;
            }
            d$x_4$y_1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_4$y1)) { // check (-4, 1)
          if ((!rc.canSenseLocation(l$x_4$y1) || rc.sensePassability(l$x_4$y1))) { 
            if (d$x_4$y1 > d$x_3$y0) { // from (-3, 0)
                d$x_4$y1 = d$x_3$y0;
                // dir$x_4$y1 = dir$x_3$y0;
            }
            if (d$x_4$y1 > d$x_3$y1) { // from (-3, 1)
                d$x_4$y1 = d$x_3$y1;
                // dir$x_4$y1 = dir$x_3$y1;
            }
            if (d$x_4$y1 > d$x_3$y2) { // from (-3, 2)
                d$x_4$y1 = d$x_3$y2;
                // dir$x_4$y1 = dir$x_3$y2;
            }
            if (d$x_4$y1 > d$x_4$y0) { // from (-4, 0)
                d$x_4$y1 = d$x_4$y0;
                // dir$x_4$y1 = dir$x_4$y0;
            }
            d$x_4$y1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y_4)) { // check (-1, -4)
          if ((!rc.canSenseLocation(l$x_1$y_4) || rc.sensePassability(l$x_1$y_4))) { 
            if (d$x_1$y_4 > d$x0$y_3) { // from (0, -3)
                d$x_1$y_4 = d$x0$y_3;
                // dir$x_1$y_4 = dir$x0$y_3;
            }
            if (d$x_1$y_4 > d$x_1$y_3) { // from (-1, -3)
                d$x_1$y_4 = d$x_1$y_3;
                // dir$x_1$y_4 = dir$x_1$y_3;
            }
            if (d$x_1$y_4 > d$x_2$y_3) { // from (-2, -3)
                d$x_1$y_4 = d$x_2$y_3;
                // dir$x_1$y_4 = dir$x_2$y_3;
            }
            if (d$x_1$y_4 > d$x0$y_4) { // from (0, -4)
                d$x_1$y_4 = d$x0$y_4;
                // dir$x_1$y_4 = dir$x0$y_4;
            }
            d$x_1$y_4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_1$y4)) { // check (-1, 4)
          if ((!rc.canSenseLocation(l$x_1$y4) || rc.sensePassability(l$x_1$y4))) { 
            if (d$x_1$y4 > d$x0$y3) { // from (0, 3)
                d$x_1$y4 = d$x0$y3;
                // dir$x_1$y4 = dir$x0$y3;
            }
            if (d$x_1$y4 > d$x_1$y3) { // from (-1, 3)
                d$x_1$y4 = d$x_1$y3;
                // dir$x_1$y4 = dir$x_1$y3;
            }
            if (d$x_1$y4 > d$x_2$y3) { // from (-2, 3)
                d$x_1$y4 = d$x_2$y3;
                // dir$x_1$y4 = dir$x_2$y3;
            }
            if (d$x_1$y4 > d$x0$y4) { // from (0, 4)
                d$x_1$y4 = d$x0$y4;
                // dir$x_1$y4 = dir$x0$y4;
            }
            d$x_1$y4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y_4)) { // check (1, -4)
          if ((!rc.canSenseLocation(l$x1$y_4) || rc.sensePassability(l$x1$y_4))) { 
            if (d$x1$y_4 > d$x0$y_3) { // from (0, -3)
                d$x1$y_4 = d$x0$y_3;
                // dir$x1$y_4 = dir$x0$y_3;
            }
            if (d$x1$y_4 > d$x1$y_3) { // from (1, -3)
                d$x1$y_4 = d$x1$y_3;
                // dir$x1$y_4 = dir$x1$y_3;
            }
            if (d$x1$y_4 > d$x2$y_3) { // from (2, -3)
                d$x1$y_4 = d$x2$y_3;
                // dir$x1$y_4 = dir$x2$y_3;
            }
            if (d$x1$y_4 > d$x0$y_4) { // from (0, -4)
                d$x1$y_4 = d$x0$y_4;
                // dir$x1$y_4 = dir$x0$y_4;
            }
            d$x1$y_4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x1$y4)) { // check (1, 4)
          if ((!rc.canSenseLocation(l$x1$y4) || rc.sensePassability(l$x1$y4))) { 
            if (d$x1$y4 > d$x0$y3) { // from (0, 3)
                d$x1$y4 = d$x0$y3;
                // dir$x1$y4 = dir$x0$y3;
            }
            if (d$x1$y4 > d$x1$y3) { // from (1, 3)
                d$x1$y4 = d$x1$y3;
                // dir$x1$y4 = dir$x1$y3;
            }
            if (d$x1$y4 > d$x2$y3) { // from (2, 3)
                d$x1$y4 = d$x2$y3;
                // dir$x1$y4 = dir$x2$y3;
            }
            if (d$x1$y4 > d$x0$y4) { // from (0, 4)
                d$x1$y4 = d$x0$y4;
                // dir$x1$y4 = dir$x0$y4;
            }
            d$x1$y4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x4$y_1)) { // check (4, -1)
          if ((!rc.canSenseLocation(l$x4$y_1) || rc.sensePassability(l$x4$y_1))) { 
            if (d$x4$y_1 > d$x3$y0) { // from (3, 0)
                d$x4$y_1 = d$x3$y0;
                // dir$x4$y_1 = dir$x3$y0;
            }
            if (d$x4$y_1 > d$x3$y_1) { // from (3, -1)
                d$x4$y_1 = d$x3$y_1;
                // dir$x4$y_1 = dir$x3$y_1;
            }
            if (d$x4$y_1 > d$x3$y_2) { // from (3, -2)
                d$x4$y_1 = d$x3$y_2;
                // dir$x4$y_1 = dir$x3$y_2;
            }
            if (d$x4$y_1 > d$x4$y0) { // from (4, 0)
                d$x4$y_1 = d$x4$y0;
                // dir$x4$y_1 = dir$x4$y0;
            }
            d$x4$y_1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x4$y1)) { // check (4, 1)
          if ((!rc.canSenseLocation(l$x4$y1) || rc.sensePassability(l$x4$y1))) { 
            if (d$x4$y1 > d$x3$y0) { // from (3, 0)
                d$x4$y1 = d$x3$y0;
                // dir$x4$y1 = dir$x3$y0;
            }
            if (d$x4$y1 > d$x3$y1) { // from (3, 1)
                d$x4$y1 = d$x3$y1;
                // dir$x4$y1 = dir$x3$y1;
            }
            if (d$x4$y1 > d$x3$y2) { // from (3, 2)
                d$x4$y1 = d$x3$y2;
                // dir$x4$y1 = dir$x3$y2;
            }
            if (d$x4$y1 > d$x4$y0) { // from (4, 0)
                d$x4$y1 = d$x4$y0;
                // dir$x4$y1 = dir$x4$y0;
            }
            d$x4$y1 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y_3)) { // check (-3, -3)
          if ((!rc.canSenseLocation(l$x_3$y_3) || rc.sensePassability(l$x_3$y_3))) { 
            if (d$x_3$y_3 > d$x_2$y_2) { // from (-2, -2)
                d$x_3$y_3 = d$x_2$y_2;
                // dir$x_3$y_3 = dir$x_2$y_2;
            }
            if (d$x_3$y_3 > d$x_3$y_2) { // from (-3, -2)
                d$x_3$y_3 = d$x_3$y_2;
                // dir$x_3$y_3 = dir$x_3$y_2;
            }
            if (d$x_3$y_3 > d$x_2$y_3) { // from (-2, -3)
                d$x_3$y_3 = d$x_2$y_3;
                // dir$x_3$y_3 = dir$x_2$y_3;
            }
            d$x_3$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_3$y3)) { // check (-3, 3)
          if ((!rc.canSenseLocation(l$x_3$y3) || rc.sensePassability(l$x_3$y3))) { 
            if (d$x_3$y3 > d$x_2$y2) { // from (-2, 2)
                d$x_3$y3 = d$x_2$y2;
                // dir$x_3$y3 = dir$x_2$y2;
            }
            if (d$x_3$y3 > d$x_3$y2) { // from (-3, 2)
                d$x_3$y3 = d$x_3$y2;
                // dir$x_3$y3 = dir$x_3$y2;
            }
            if (d$x_3$y3 > d$x_2$y3) { // from (-2, 3)
                d$x_3$y3 = d$x_2$y3;
                // dir$x_3$y3 = dir$x_2$y3;
            }
            d$x_3$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y_3)) { // check (3, -3)
          if ((!rc.canSenseLocation(l$x3$y_3) || rc.sensePassability(l$x3$y_3))) { 
            if (d$x3$y_3 > d$x2$y_2) { // from (2, -2)
                d$x3$y_3 = d$x2$y_2;
                // dir$x3$y_3 = dir$x2$y_2;
            }
            if (d$x3$y_3 > d$x2$y_3) { // from (2, -3)
                d$x3$y_3 = d$x2$y_3;
                // dir$x3$y_3 = dir$x2$y_3;
            }
            if (d$x3$y_3 > d$x3$y_2) { // from (3, -2)
                d$x3$y_3 = d$x3$y_2;
                // dir$x3$y_3 = dir$x3$y_2;
            }
            d$x3$y_3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x3$y3)) { // check (3, 3)
          if ((!rc.canSenseLocation(l$x3$y3) || rc.sensePassability(l$x3$y3))) { 
            if (d$x3$y3 > d$x2$y2) { // from (2, 2)
                d$x3$y3 = d$x2$y2;
                // dir$x3$y3 = dir$x2$y2;
            }
            if (d$x3$y3 > d$x2$y3) { // from (2, 3)
                d$x3$y3 = d$x2$y3;
                // dir$x3$y3 = dir$x2$y3;
            }
            if (d$x3$y3 > d$x3$y2) { // from (3, 2)
                d$x3$y3 = d$x3$y2;
                // dir$x3$y3 = dir$x3$y2;
            }
            d$x3$y3 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_4$y_2)) { // check (-4, -2)
          if ((!rc.canSenseLocation(l$x_4$y_2) || rc.sensePassability(l$x_4$y_2))) { 
            if (d$x_4$y_2 > d$x_3$y_1) { // from (-3, -1)
                d$x_4$y_2 = d$x_3$y_1;
                // dir$x_4$y_2 = dir$x_3$y_1;
            }
            if (d$x_4$y_2 > d$x_3$y_2) { // from (-3, -2)
                d$x_4$y_2 = d$x_3$y_2;
                // dir$x_4$y_2 = dir$x_3$y_2;
            }
            if (d$x_4$y_2 > d$x_4$y_1) { // from (-4, -1)
                d$x_4$y_2 = d$x_4$y_1;
                // dir$x_4$y_2 = dir$x_4$y_1;
            }
            if (d$x_4$y_2 > d$x_3$y_3) { // from (-3, -3)
                d$x_4$y_2 = d$x_3$y_3;
                // dir$x_4$y_2 = dir$x_3$y_3;
            }
            d$x_4$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_4$y2)) { // check (-4, 2)
          if ((!rc.canSenseLocation(l$x_4$y2) || rc.sensePassability(l$x_4$y2))) { 
            if (d$x_4$y2 > d$x_3$y1) { // from (-3, 1)
                d$x_4$y2 = d$x_3$y1;
                // dir$x_4$y2 = dir$x_3$y1;
            }
            if (d$x_4$y2 > d$x_3$y2) { // from (-3, 2)
                d$x_4$y2 = d$x_3$y2;
                // dir$x_4$y2 = dir$x_3$y2;
            }
            if (d$x_4$y2 > d$x_4$y1) { // from (-4, 1)
                d$x_4$y2 = d$x_4$y1;
                // dir$x_4$y2 = dir$x_4$y1;
            }
            if (d$x_4$y2 > d$x_3$y3) { // from (-3, 3)
                d$x_4$y2 = d$x_3$y3;
                // dir$x_4$y2 = dir$x_3$y3;
            }
            d$x_4$y2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y_4)) { // check (-2, -4)
          if ((!rc.canSenseLocation(l$x_2$y_4) || rc.sensePassability(l$x_2$y_4))) { 
            if (d$x_2$y_4 > d$x_1$y_3) { // from (-1, -3)
                d$x_2$y_4 = d$x_1$y_3;
                // dir$x_2$y_4 = dir$x_1$y_3;
            }
            if (d$x_2$y_4 > d$x_2$y_3) { // from (-2, -3)
                d$x_2$y_4 = d$x_2$y_3;
                // dir$x_2$y_4 = dir$x_2$y_3;
            }
            if (d$x_2$y_4 > d$x_1$y_4) { // from (-1, -4)
                d$x_2$y_4 = d$x_1$y_4;
                // dir$x_2$y_4 = dir$x_1$y_4;
            }
            if (d$x_2$y_4 > d$x_3$y_3) { // from (-3, -3)
                d$x_2$y_4 = d$x_3$y_3;
                // dir$x_2$y_4 = dir$x_3$y_3;
            }
            d$x_2$y_4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x_2$y4)) { // check (-2, 4)
          if ((!rc.canSenseLocation(l$x_2$y4) || rc.sensePassability(l$x_2$y4))) { 
            if (d$x_2$y4 > d$x_1$y3) { // from (-1, 3)
                d$x_2$y4 = d$x_1$y3;
                // dir$x_2$y4 = dir$x_1$y3;
            }
            if (d$x_2$y4 > d$x_2$y3) { // from (-2, 3)
                d$x_2$y4 = d$x_2$y3;
                // dir$x_2$y4 = dir$x_2$y3;
            }
            if (d$x_2$y4 > d$x_1$y4) { // from (-1, 4)
                d$x_2$y4 = d$x_1$y4;
                // dir$x_2$y4 = dir$x_1$y4;
            }
            if (d$x_2$y4 > d$x_3$y3) { // from (-3, 3)
                d$x_2$y4 = d$x_3$y3;
                // dir$x_2$y4 = dir$x_3$y3;
            }
            d$x_2$y4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y_4)) { // check (2, -4)
          if ((!rc.canSenseLocation(l$x2$y_4) || rc.sensePassability(l$x2$y_4))) { 
            if (d$x2$y_4 > d$x1$y_3) { // from (1, -3)
                d$x2$y_4 = d$x1$y_3;
                // dir$x2$y_4 = dir$x1$y_3;
            }
            if (d$x2$y_4 > d$x2$y_3) { // from (2, -3)
                d$x2$y_4 = d$x2$y_3;
                // dir$x2$y_4 = dir$x2$y_3;
            }
            if (d$x2$y_4 > d$x1$y_4) { // from (1, -4)
                d$x2$y_4 = d$x1$y_4;
                // dir$x2$y_4 = dir$x1$y_4;
            }
            if (d$x2$y_4 > d$x3$y_3) { // from (3, -3)
                d$x2$y_4 = d$x3$y_3;
                // dir$x2$y_4 = dir$x3$y_3;
            }
            d$x2$y_4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x2$y4)) { // check (2, 4)
          if ((!rc.canSenseLocation(l$x2$y4) || rc.sensePassability(l$x2$y4))) { 
            if (d$x2$y4 > d$x1$y3) { // from (1, 3)
                d$x2$y4 = d$x1$y3;
                // dir$x2$y4 = dir$x1$y3;
            }
            if (d$x2$y4 > d$x2$y3) { // from (2, 3)
                d$x2$y4 = d$x2$y3;
                // dir$x2$y4 = dir$x2$y3;
            }
            if (d$x2$y4 > d$x1$y4) { // from (1, 4)
                d$x2$y4 = d$x1$y4;
                // dir$x2$y4 = dir$x1$y4;
            }
            if (d$x2$y4 > d$x3$y3) { // from (3, 3)
                d$x2$y4 = d$x3$y3;
                // dir$x2$y4 = dir$x3$y3;
            }
            d$x2$y4 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x4$y_2)) { // check (4, -2)
          if ((!rc.canSenseLocation(l$x4$y_2) || rc.sensePassability(l$x4$y_2))) { 
            if (d$x4$y_2 > d$x3$y_1) { // from (3, -1)
                d$x4$y_2 = d$x3$y_1;
                // dir$x4$y_2 = dir$x3$y_1;
            }
            if (d$x4$y_2 > d$x3$y_2) { // from (3, -2)
                d$x4$y_2 = d$x3$y_2;
                // dir$x4$y_2 = dir$x3$y_2;
            }
            if (d$x4$y_2 > d$x4$y_1) { // from (4, -1)
                d$x4$y_2 = d$x4$y_1;
                // dir$x4$y_2 = dir$x4$y_1;
            }
            if (d$x4$y_2 > d$x3$y_3) { // from (3, -3)
                d$x4$y_2 = d$x3$y_3;
                // dir$x4$y_2 = dir$x3$y_3;
            }
            d$x4$y_2 += shiftedMoveCD;
          }
        }

        if (notNearEdge || rc.onTheMap(l$x4$y2)) { // check (4, 2)
          if ((!rc.canSenseLocation(l$x4$y2) || rc.sensePassability(l$x4$y2))) { 
            if (d$x4$y2 > d$x3$y1) { // from (3, 1)
                d$x4$y2 = d$x3$y1;
                // dir$x4$y2 = dir$x3$y1;
            }
            if (d$x4$y2 > d$x3$y2) { // from (3, 2)
                d$x4$y2 = d$x3$y2;
                // dir$x4$y2 = dir$x3$y2;
            }
            if (d$x4$y2 > d$x4$y1) { // from (4, 1)
                d$x4$y2 = d$x4$y1;
                // dir$x4$y2 = dir$x4$y1;
            }
            if (d$x4$y2 > d$x3$y3) { // from (3, 3)
                d$x4$y2 = d$x3$y3;
                // dir$x4$y2 = dir$x3$y3;
            }
            d$x4$y2 += shiftedMoveCD;
          }
        }


        // System.out.println("LOCAL DISTANCES:");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + "\t" + d$x_2$y4 + "\t" + d$x_1$y4 + "\t" + d$x0$y4 + "\t" + d$x1$y4 + "\t" + d$x2$y4 + "\t" + "\t" + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + d$x_3$y3 + "\t" + d$x_2$y3 + "\t" + d$x_1$y3 + "\t" + d$x0$y3 + "\t" + d$x1$y3 + "\t" + d$x2$y3 + "\t" + d$x3$y3 + "\t" + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + d$x_4$y2 + "\t" + d$x_3$y2 + "\t" + d$x_2$y2 + "\t" + d$x_1$y2 + "\t" + d$x0$y2 + "\t" + d$x1$y2 + "\t" + d$x2$y2 + "\t" + d$x3$y2 + "\t" + d$x4$y2 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + d$x_4$y1 + "\t" + d$x_3$y1 + "\t" + d$x_2$y1 + "\t" + d$x_1$y1 + "\t" + d$x0$y1 + "\t" + d$x1$y1 + "\t" + d$x2$y1 + "\t" + d$x3$y1 + "\t" + d$x4$y1 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + d$x_4$y0 + "\t" + d$x_3$y0 + "\t" + d$x_2$y0 + "\t" + d$x_1$y0 + "\t" + d$x0$y0 + "\t" + d$x1$y0 + "\t" + d$x2$y0 + "\t" + d$x3$y0 + "\t" + d$x4$y0 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + d$x_4$y_1 + "\t" + d$x_3$y_1 + "\t" + d$x_2$y_1 + "\t" + d$x_1$y_1 + "\t" + d$x0$y_1 + "\t" + d$x1$y_1 + "\t" + d$x2$y_1 + "\t" + d$x3$y_1 + "\t" + d$x4$y_1 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + d$x_4$y_2 + "\t" + d$x_3$y_2 + "\t" + d$x_2$y_2 + "\t" + d$x_1$y_2 + "\t" + d$x0$y_2 + "\t" + d$x1$y_2 + "\t" + d$x2$y_2 + "\t" + d$x3$y_2 + "\t" + d$x4$y_2 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + d$x_3$y_3 + "\t" + d$x_2$y_3 + "\t" + d$x_1$y_3 + "\t" + d$x0$y_3 + "\t" + d$x1$y_3 + "\t" + d$x2$y_3 + "\t" + d$x3$y_3 + "\t" + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + "\t" + d$x_2$y_4 + "\t" + d$x_1$y_4 + "\t" + d$x0$y_4 + "\t" + d$x1$y_4 + "\t" + d$x2$y_4 + "\t" + "\t" + "\t" + "\t" + "\t");
        // System.out.println("DIRECTIONS:");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + "\t" + dir$x_2$y4 + "\t" + dir$x_1$y4 + "\t" + dir$x0$y4 + "\t" + dir$x1$y4 + "\t" + dir$x2$y4 + "\t" + "\t" + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + dir$x_3$y3 + "\t" + dir$x_2$y3 + "\t" + dir$x_1$y3 + "\t" + dir$x0$y3 + "\t" + dir$x1$y3 + "\t" + dir$x2$y3 + "\t" + dir$x3$y3 + "\t" + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + dir$x_4$y2 + "\t" + dir$x_3$y2 + "\t" + dir$x_2$y2 + "\t" + dir$x_1$y2 + "\t" + dir$x0$y2 + "\t" + dir$x1$y2 + "\t" + dir$x2$y2 + "\t" + dir$x3$y2 + "\t" + dir$x4$y2 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + dir$x_4$y1 + "\t" + dir$x_3$y1 + "\t" + dir$x_2$y1 + "\t" + dir$x_1$y1 + "\t" + dir$x0$y1 + "\t" + dir$x1$y1 + "\t" + dir$x2$y1 + "\t" + dir$x3$y1 + "\t" + dir$x4$y1 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + dir$x_4$y0 + "\t" + dir$x_3$y0 + "\t" + dir$x_2$y0 + "\t" + dir$x_1$y0 + "\t" + dir$x0$y0 + "\t" + dir$x1$y0 + "\t" + dir$x2$y0 + "\t" + dir$x3$y0 + "\t" + dir$x4$y0 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + dir$x_4$y_1 + "\t" + dir$x_3$y_1 + "\t" + dir$x_2$y_1 + "\t" + dir$x_1$y_1 + "\t" + dir$x0$y_1 + "\t" + dir$x1$y_1 + "\t" + dir$x2$y_1 + "\t" + dir$x3$y_1 + "\t" + dir$x4$y_1 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + dir$x_4$y_2 + "\t" + dir$x_3$y_2 + "\t" + dir$x_2$y_2 + "\t" + dir$x_1$y_2 + "\t" + dir$x0$y_2 + "\t" + dir$x1$y_2 + "\t" + dir$x2$y_2 + "\t" + dir$x3$y_2 + "\t" + dir$x4$y_2 + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + dir$x_3$y_3 + "\t" + dir$x_2$y_3 + "\t" + dir$x_1$y_3 + "\t" + dir$x0$y_3 + "\t" + dir$x1$y_3 + "\t" + dir$x2$y_3 + "\t" + dir$x3$y_3 + "\t" + "\t" + "\t" + "\t");
        // System.out.println("\t" + "\t" + "\t" + "\t" + "\t" + "\t" + dir$x_2$y_4 + "\t" + dir$x_1$y_4 + "\t" + dir$x0$y_4 + "\t" + dir$x1$y_4 + "\t" + dir$x2$y_4 + "\t" + "\t" + "\t" + "\t" + "\t");

        int target_dx = target.x - l$x0$y0.x;
        int target_dy = target.y - l$x0$y0.y;
        int dirEnc;
        switch (target_dx) {
                case -4:
                    switch (target_dy) {
                        case -2:
                            dirEnc = d$x_4$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_4$y_2; // destination is at relative location (-4, -2)
                        case -1:
                            dirEnc = d$x_4$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_4$y_1; // destination is at relative location (-4, -1)
                        case 0:
                            dirEnc = d$x_4$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_4$y0; // destination is at relative location (-4, 0)
                        case 1:
                            dirEnc = d$x_4$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_4$y1; // destination is at relative location (-4, 1)
                        case 2:
                            dirEnc = d$x_4$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_4$y2; // destination is at relative location (-4, 2)
                    }
                    break;
                case -3:
                    switch (target_dy) {
                        case -3:
                            dirEnc = d$x_3$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y_3; // destination is at relative location (-3, -3)
                        case -2:
                            dirEnc = d$x_3$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y_2; // destination is at relative location (-3, -2)
                        case -1:
                            dirEnc = d$x_3$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y_1; // destination is at relative location (-3, -1)
                        case 0:
                            dirEnc = d$x_3$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y0; // destination is at relative location (-3, 0)
                        case 1:
                            dirEnc = d$x_3$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y1; // destination is at relative location (-3, 1)
                        case 2:
                            dirEnc = d$x_3$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y2; // destination is at relative location (-3, 2)
                        case 3:
                            dirEnc = d$x_3$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_3$y3; // destination is at relative location (-3, 3)
                    }
                    break;
                case -2:
                    switch (target_dy) {
                        case -4:
                            dirEnc = d$x_2$y_4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y_4; // destination is at relative location (-2, -4)
                        case -3:
                            dirEnc = d$x_2$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y_3; // destination is at relative location (-2, -3)
                        case -2:
                            dirEnc = d$x_2$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y_2; // destination is at relative location (-2, -2)
                        case -1:
                            dirEnc = d$x_2$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y_1; // destination is at relative location (-2, -1)
                        case 0:
                            dirEnc = d$x_2$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y0; // destination is at relative location (-2, 0)
                        case 1:
                            dirEnc = d$x_2$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y1; // destination is at relative location (-2, 1)
                        case 2:
                            dirEnc = d$x_2$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y2; // destination is at relative location (-2, 2)
                        case 3:
                            dirEnc = d$x_2$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y3; // destination is at relative location (-2, 3)
                        case 4:
                            dirEnc = d$x_2$y4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_2$y4; // destination is at relative location (-2, 4)
                    }
                    break;
                case -1:
                    switch (target_dy) {
                        case -4:
                            dirEnc = d$x_1$y_4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y_4; // destination is at relative location (-1, -4)
                        case -3:
                            dirEnc = d$x_1$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y_3; // destination is at relative location (-1, -3)
                        case -2:
                            dirEnc = d$x_1$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y_2; // destination is at relative location (-1, -2)
                        case -1:
                            dirEnc = d$x_1$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y_1; // destination is at relative location (-1, -1)
                        case 0:
                            dirEnc = d$x_1$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y0; // destination is at relative location (-1, 0)
                        case 1:
                            dirEnc = d$x_1$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y1; // destination is at relative location (-1, 1)
                        case 2:
                            dirEnc = d$x_1$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y2; // destination is at relative location (-1, 2)
                        case 3:
                            dirEnc = d$x_1$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y3; // destination is at relative location (-1, 3)
                        case 4:
                            dirEnc = d$x_1$y4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x_1$y4; // destination is at relative location (-1, 4)
                    }
                    break;
                case 0:
                    switch (target_dy) {
                        case -4:
                            dirEnc = d$x0$y_4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y_4; // destination is at relative location (0, -4)
                        case -3:
                            dirEnc = d$x0$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y_3; // destination is at relative location (0, -3)
                        case -2:
                            dirEnc = d$x0$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y_2; // destination is at relative location (0, -2)
                        case -1:
                            dirEnc = d$x0$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y_1; // destination is at relative location (0, -1)
                        case 0:
                            dirEnc = d$x0$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y0; // destination is at relative location (0, 0)
                        case 1:
                            dirEnc = d$x0$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y1; // destination is at relative location (0, 1)
                        case 2:
                            dirEnc = d$x0$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y2; // destination is at relative location (0, 2)
                        case 3:
                            dirEnc = d$x0$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y3; // destination is at relative location (0, 3)
                        case 4:
                            dirEnc = d$x0$y4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x0$y4; // destination is at relative location (0, 4)
                    }
                    break;
                case 1:
                    switch (target_dy) {
                        case -4:
                            dirEnc = d$x1$y_4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y_4; // destination is at relative location (1, -4)
                        case -3:
                            dirEnc = d$x1$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y_3; // destination is at relative location (1, -3)
                        case -2:
                            dirEnc = d$x1$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y_2; // destination is at relative location (1, -2)
                        case -1:
                            dirEnc = d$x1$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y_1; // destination is at relative location (1, -1)
                        case 0:
                            dirEnc = d$x1$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y0; // destination is at relative location (1, 0)
                        case 1:
                            dirEnc = d$x1$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y1; // destination is at relative location (1, 1)
                        case 2:
                            dirEnc = d$x1$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y2; // destination is at relative location (1, 2)
                        case 3:
                            dirEnc = d$x1$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y3; // destination is at relative location (1, 3)
                        case 4:
                            dirEnc = d$x1$y4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x1$y4; // destination is at relative location (1, 4)
                    }
                    break;
                case 2:
                    switch (target_dy) {
                        case -4:
                            dirEnc = d$x2$y_4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y_4; // destination is at relative location (2, -4)
                        case -3:
                            dirEnc = d$x2$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y_3; // destination is at relative location (2, -3)
                        case -2:
                            dirEnc = d$x2$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y_2; // destination is at relative location (2, -2)
                        case -1:
                            dirEnc = d$x2$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y_1; // destination is at relative location (2, -1)
                        case 0:
                            dirEnc = d$x2$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y0; // destination is at relative location (2, 0)
                        case 1:
                            dirEnc = d$x2$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y1; // destination is at relative location (2, 1)
                        case 2:
                            dirEnc = d$x2$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y2; // destination is at relative location (2, 2)
                        case 3:
                            dirEnc = d$x2$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y3; // destination is at relative location (2, 3)
                        case 4:
                            dirEnc = d$x2$y4 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x2$y4; // destination is at relative location (2, 4)
                    }
                    break;
                case 3:
                    switch (target_dy) {
                        case -3:
                            dirEnc = d$x3$y_3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y_3; // destination is at relative location (3, -3)
                        case -2:
                            dirEnc = d$x3$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y_2; // destination is at relative location (3, -2)
                        case -1:
                            dirEnc = d$x3$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y_1; // destination is at relative location (3, -1)
                        case 0:
                            dirEnc = d$x3$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y0; // destination is at relative location (3, 0)
                        case 1:
                            dirEnc = d$x3$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y1; // destination is at relative location (3, 1)
                        case 2:
                            dirEnc = d$x3$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y2; // destination is at relative location (3, 2)
                        case 3:
                            dirEnc = d$x3$y3 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x3$y3; // destination is at relative location (3, 3)
                    }
                    break;
                case 4:
                    switch (target_dy) {
                        case -2:
                            dirEnc = d$x4$y_2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x4$y_2; // destination is at relative location (4, -2)
                        case -1:
                            dirEnc = d$x4$y_1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x4$y_1; // destination is at relative location (4, -1)
                        case 0:
                            dirEnc = d$x4$y0 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x4$y0; // destination is at relative location (4, 0)
                        case 1:
                            dirEnc = d$x4$y1 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x4$y1; // destination is at relative location (4, 1)
                        case 2:
                            dirEnc = d$x4$y2 & 0b1111;
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir$x4$y2; // destination is at relative location (4, 2)
                    }
                    break;
        }
        
        int ans = 0;
        double bestScore = 0;
        double currDist = Math.sqrt(l$x0$y0.distanceSquaredTo(target));
        
        double score$x_4$y_2 = (currDist - Math.sqrt(l$x_4$y_2.distanceSquaredTo(target))) / (d$x_4$y_2 >>> 4);
        if (score$x_4$y_2 > bestScore) {
            bestScore = score$x_4$y_2;
            ans = d$x_4$y_2; // dir$x_4$y_2;
        }

        double score$x_4$y_1 = (currDist - Math.sqrt(l$x_4$y_1.distanceSquaredTo(target))) / (d$x_4$y_1 >>> 4);
        if (score$x_4$y_1 > bestScore) {
            bestScore = score$x_4$y_1;
            ans = d$x_4$y_1; // dir$x_4$y_1;
        }

        double score$x_4$y0 = (currDist - Math.sqrt(l$x_4$y0.distanceSquaredTo(target))) / (d$x_4$y0 >>> 4);
        if (score$x_4$y0 > bestScore) {
            bestScore = score$x_4$y0;
            ans = d$x_4$y0; // dir$x_4$y0;
        }

        double score$x_4$y1 = (currDist - Math.sqrt(l$x_4$y1.distanceSquaredTo(target))) / (d$x_4$y1 >>> 4);
        if (score$x_4$y1 > bestScore) {
            bestScore = score$x_4$y1;
            ans = d$x_4$y1; // dir$x_4$y1;
        }

        double score$x_4$y2 = (currDist - Math.sqrt(l$x_4$y2.distanceSquaredTo(target))) / (d$x_4$y2 >>> 4);
        if (score$x_4$y2 > bestScore) {
            bestScore = score$x_4$y2;
            ans = d$x_4$y2; // dir$x_4$y2;
        }

        double score$x_3$y_3 = (currDist - Math.sqrt(l$x_3$y_3.distanceSquaredTo(target))) / (d$x_3$y_3 >>> 4);
        if (score$x_3$y_3 > bestScore) {
            bestScore = score$x_3$y_3;
            ans = d$x_3$y_3; // dir$x_3$y_3;
        }

        double score$x_3$y_2 = (currDist - Math.sqrt(l$x_3$y_2.distanceSquaredTo(target))) / (d$x_3$y_2 >>> 4);
        if (score$x_3$y_2 > bestScore) {
            bestScore = score$x_3$y_2;
            ans = d$x_3$y_2; // dir$x_3$y_2;
        }

        double score$x_3$y2 = (currDist - Math.sqrt(l$x_3$y2.distanceSquaredTo(target))) / (d$x_3$y2 >>> 4);
        if (score$x_3$y2 > bestScore) {
            bestScore = score$x_3$y2;
            ans = d$x_3$y2; // dir$x_3$y2;
        }

        double score$x_3$y3 = (currDist - Math.sqrt(l$x_3$y3.distanceSquaredTo(target))) / (d$x_3$y3 >>> 4);
        if (score$x_3$y3 > bestScore) {
            bestScore = score$x_3$y3;
            ans = d$x_3$y3; // dir$x_3$y3;
        }

        double score$x_2$y_4 = (currDist - Math.sqrt(l$x_2$y_4.distanceSquaredTo(target))) / (d$x_2$y_4 >>> 4);
        if (score$x_2$y_4 > bestScore) {
            bestScore = score$x_2$y_4;
            ans = d$x_2$y_4; // dir$x_2$y_4;
        }

        double score$x_2$y_3 = (currDist - Math.sqrt(l$x_2$y_3.distanceSquaredTo(target))) / (d$x_2$y_3 >>> 4);
        if (score$x_2$y_3 > bestScore) {
            bestScore = score$x_2$y_3;
            ans = d$x_2$y_3; // dir$x_2$y_3;
        }

        double score$x_2$y3 = (currDist - Math.sqrt(l$x_2$y3.distanceSquaredTo(target))) / (d$x_2$y3 >>> 4);
        if (score$x_2$y3 > bestScore) {
            bestScore = score$x_2$y3;
            ans = d$x_2$y3; // dir$x_2$y3;
        }

        double score$x_2$y4 = (currDist - Math.sqrt(l$x_2$y4.distanceSquaredTo(target))) / (d$x_2$y4 >>> 4);
        if (score$x_2$y4 > bestScore) {
            bestScore = score$x_2$y4;
            ans = d$x_2$y4; // dir$x_2$y4;
        }

        double score$x_1$y_4 = (currDist - Math.sqrt(l$x_1$y_4.distanceSquaredTo(target))) / (d$x_1$y_4 >>> 4);
        if (score$x_1$y_4 > bestScore) {
            bestScore = score$x_1$y_4;
            ans = d$x_1$y_4; // dir$x_1$y_4;
        }

        double score$x_1$y4 = (currDist - Math.sqrt(l$x_1$y4.distanceSquaredTo(target))) / (d$x_1$y4 >>> 4);
        if (score$x_1$y4 > bestScore) {
            bestScore = score$x_1$y4;
            ans = d$x_1$y4; // dir$x_1$y4;
        }

        double score$x0$y_4 = (currDist - Math.sqrt(l$x0$y_4.distanceSquaredTo(target))) / (d$x0$y_4 >>> 4);
        if (score$x0$y_4 > bestScore) {
            bestScore = score$x0$y_4;
            ans = d$x0$y_4; // dir$x0$y_4;
        }

        double score$x0$y4 = (currDist - Math.sqrt(l$x0$y4.distanceSquaredTo(target))) / (d$x0$y4 >>> 4);
        if (score$x0$y4 > bestScore) {
            bestScore = score$x0$y4;
            ans = d$x0$y4; // dir$x0$y4;
        }

        double score$x1$y_4 = (currDist - Math.sqrt(l$x1$y_4.distanceSquaredTo(target))) / (d$x1$y_4 >>> 4);
        if (score$x1$y_4 > bestScore) {
            bestScore = score$x1$y_4;
            ans = d$x1$y_4; // dir$x1$y_4;
        }

        double score$x1$y4 = (currDist - Math.sqrt(l$x1$y4.distanceSquaredTo(target))) / (d$x1$y4 >>> 4);
        if (score$x1$y4 > bestScore) {
            bestScore = score$x1$y4;
            ans = d$x1$y4; // dir$x1$y4;
        }

        double score$x2$y_4 = (currDist - Math.sqrt(l$x2$y_4.distanceSquaredTo(target))) / (d$x2$y_4 >>> 4);
        if (score$x2$y_4 > bestScore) {
            bestScore = score$x2$y_4;
            ans = d$x2$y_4; // dir$x2$y_4;
        }

        double score$x2$y_3 = (currDist - Math.sqrt(l$x2$y_3.distanceSquaredTo(target))) / (d$x2$y_3 >>> 4);
        if (score$x2$y_3 > bestScore) {
            bestScore = score$x2$y_3;
            ans = d$x2$y_3; // dir$x2$y_3;
        }

        double score$x2$y3 = (currDist - Math.sqrt(l$x2$y3.distanceSquaredTo(target))) / (d$x2$y3 >>> 4);
        if (score$x2$y3 > bestScore) {
            bestScore = score$x2$y3;
            ans = d$x2$y3; // dir$x2$y3;
        }

        double score$x2$y4 = (currDist - Math.sqrt(l$x2$y4.distanceSquaredTo(target))) / (d$x2$y4 >>> 4);
        if (score$x2$y4 > bestScore) {
            bestScore = score$x2$y4;
            ans = d$x2$y4; // dir$x2$y4;
        }

        double score$x3$y_3 = (currDist - Math.sqrt(l$x3$y_3.distanceSquaredTo(target))) / (d$x3$y_3 >>> 4);
        if (score$x3$y_3 > bestScore) {
            bestScore = score$x3$y_3;
            ans = d$x3$y_3; // dir$x3$y_3;
        }

        double score$x3$y_2 = (currDist - Math.sqrt(l$x3$y_2.distanceSquaredTo(target))) / (d$x3$y_2 >>> 4);
        if (score$x3$y_2 > bestScore) {
            bestScore = score$x3$y_2;
            ans = d$x3$y_2; // dir$x3$y_2;
        }

        double score$x3$y2 = (currDist - Math.sqrt(l$x3$y2.distanceSquaredTo(target))) / (d$x3$y2 >>> 4);
        if (score$x3$y2 > bestScore) {
            bestScore = score$x3$y2;
            ans = d$x3$y2; // dir$x3$y2;
        }

        double score$x3$y3 = (currDist - Math.sqrt(l$x3$y3.distanceSquaredTo(target))) / (d$x3$y3 >>> 4);
        if (score$x3$y3 > bestScore) {
            bestScore = score$x3$y3;
            ans = d$x3$y3; // dir$x3$y3;
        }

        double score$x4$y_2 = (currDist - Math.sqrt(l$x4$y_2.distanceSquaredTo(target))) / (d$x4$y_2 >>> 4);
        if (score$x4$y_2 > bestScore) {
            bestScore = score$x4$y_2;
            ans = d$x4$y_2; // dir$x4$y_2;
        }

        double score$x4$y_1 = (currDist - Math.sqrt(l$x4$y_1.distanceSquaredTo(target))) / (d$x4$y_1 >>> 4);
        if (score$x4$y_1 > bestScore) {
            bestScore = score$x4$y_1;
            ans = d$x4$y_1; // dir$x4$y_1;
        }

        double score$x4$y0 = (currDist - Math.sqrt(l$x4$y0.distanceSquaredTo(target))) / (d$x4$y0 >>> 4);
        if (score$x4$y0 > bestScore) {
            bestScore = score$x4$y0;
            ans = d$x4$y0; // dir$x4$y0;
        }

        double score$x4$y1 = (currDist - Math.sqrt(l$x4$y1.distanceSquaredTo(target))) / (d$x4$y1 >>> 4);
        if (score$x4$y1 > bestScore) {
            bestScore = score$x4$y1;
            ans = d$x4$y1; // dir$x4$y1;
        }

        double score$x4$y2 = (currDist - Math.sqrt(l$x4$y2.distanceSquaredTo(target))) / (d$x4$y2 >>> 4);
        if (score$x4$y2 > bestScore) {
            bestScore = score$x4$y2;
            ans = d$x4$y2; // dir$x4$y2;
        }

        
        dirEnc = ans & 0b1111;
        return dirEnc-- == 0 ? null : Utils.directions[dirEnc];
    }
}
