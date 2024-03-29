package fastpathing.robots.pathfinding;

import fastpathing.robots.Robot;
import fastpathing.utils.Cache;
import fastpathing.utils.Global;
import fastpathing.utils.Printer;
import fastpathing.utils.Utils;
import battlecode.common.*;

public abstract class Pathing {

    public static Pathing globalPathing;

    protected final RobotController rc;

    public Pathing(RobotController rc) {
        this.rc = rc;
    }

    public static Pathing create(RobotController rc) {
        globalPathing = new SmartPathing(rc);
        return globalPathing;
    }

    public abstract boolean moveTowards(MapLocation target) throws GameActionException;

    public abstract boolean moveAwayFrom(MapLocation target) throws GameActionException;


    // =========== BASIC MOVEMENT =============

    /**
     * Wrapper for move() of RobotController that ensures enough bytecodes
     * @param dir where to move
     * @return if the robot moved
     * @throws GameActionException if movement failed
     */
    public boolean move(Direction dir) throws GameActionException {
        if (Clock.getBytecodesLeft() < 25) Clock.yield(); // todo: this should be larger? whenMoved takes a bit longer...
        if (rc.canMove(dir)) {
            rc.move(dir);
            Cache.PerTurn.whenMoved();
            return true;
        }
        return false;
    }

    public void forceMoveTo(MapLocation mapLocation) throws GameActionException {
//        if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(mapLocation)) {
//            Printer.print("Can only force move to adjacent locations" + Cache.PerTurn.CURRENT_LOCATION + "->" + mapLocation);
//        }
        assert Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(mapLocation);
        rc.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(mapLocation));
        Cache.PerTurn.whenMoved();
    }

    /**
     * requires adjacency -- same logic as above
     * @param center
     * @return
     * @throws GameActionException
     */
    public boolean moveToOrAdjacent(MapLocation center) throws GameActionException {
        assert Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(center);
        return move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.NORTH)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.SOUTH)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.EAST)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.WEST)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.NORTHEAST)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.NORTHWEST)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.SOUTHEAST)))
            || move(Cache.PerTurn.CURRENT_LOCATION.directionTo(center.add(Direction.SOUTHWEST)));
    }

    /**
     * if the robot can move, choose a random direction and move
     * will try 16 times in case some directions are blocked
     * @return if moved
     * @throws GameActionException if movement fails
     */
    public boolean moveRandomly() throws GameActionException {
        return move(Utils.randomDirection()) || move(Utils.randomDirection()); // try twice in case many blocked locs
    }

    /**
     * move in this direction or an adjacent direction if can't move
     * @param dir the direction to move in
     * @return if moved
     * @throws GameActionException if movement fails
     */
    public boolean moveInDirLoose(Direction dir) throws GameActionException {
        return move(dir) || move(dir.rotateLeft()) || move(dir.rotateRight());
    }

    /**
     * move randomly in this general direction
     * @param dir the direction to move in
     * @return if moved
     * @throws GameActionException if movement fails
     */
    public boolean moveInDirRandom(Direction dir) throws GameActionException {
        return move(Utils.randomSimilarDirectionPrefer(dir)) || move(Utils.randomSimilarDirection(dir));
    }
}
