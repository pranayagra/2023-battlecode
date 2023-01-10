package basicbot.pathfinding;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class Pathing {
    public static Pathing create(RobotController rc) {
        return new GenericPathing();
    }

    public abstract void moveTowards(MapLocation target);

    public abstract void moveAwayFrom(MapLocation target);
}
