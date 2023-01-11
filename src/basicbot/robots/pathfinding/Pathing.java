package basicbot.robots.pathfinding;

import basicbot.robots.Robot;
import basicbot.utils.Cache;
import basicbot.utils.Global;
import basicbot.utils.Utils;
import battlecode.common.*;

public abstract class Pathing {

    private final RobotController rc;

    public Pathing(RobotController rc) {
        this.rc = rc;
    }

    public static Pathing create(RobotController rc) {
        return new BasicPathing(rc);
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
