package multispawnhqavoidance.robots.pathfinding;

import multispawnhqavoidance.containers.CharSet;
import multispawnhqavoidance.knowledge.Cache;
import battlecode.common.*;

public class BugNav{
  static public CharSet blockedLocations = new CharSet(); // currently, only used for avoiding HQ's damage range

  static RobotController rc;
  static Pathing pathing;
  static private MapLocation currTarget;

  public static void init(RobotController rc, Pathing pathing) {
    BugNav.rc = rc;
    BugNav.pathing = pathing;
  }

  static final int INF = 1000000;
//  static final int MAX_MAP_SIZE = GameConstants.MAP_MAX_HEIGHT;

  static boolean rotateRight = true; //if I should rotate right or left
  static MapLocation lastObstacleFound = null; //latest obstacle I've found in my way
  static int minDistToTarget = INF; //minimum distance I've been to the enemy while going around an obstacle
  static CharSet visited = new CharSet();

  static void setTarget(MapLocation newTarget) {
    //different target? ==> previous data does not help!
    if (currTarget == null || !currTarget.equals(newTarget)) resetPathfinding();
    currTarget = newTarget;
//    Printer.print("BUGNAV: Setting target to " + newTarget + " -- coming from current loc: " + Cache.PerTurn.CURRENT_LOCATION);
  }

  /**
   * checks if we are done bug naving towards the target
   * @return true if done bugging and can go straight there
   */
  static boolean checkDoneBugging() throws GameActionException {
//    Printer.print("BUGNAV: Checking if done bugging - target: " + currTarget + " - current: " + Cache.PerTurn.CURRENT_LOCATION);
    MapLocation myLoc = Cache.PerTurn.CURRENT_LOCATION;
//    int d = myLoc.distanceSquaredTo(currTarget);
//    if (d <= minDistToTarget) {
//      resetPathfinding();
////      return true;
//    }
    Direction dir = myLoc.directionTo(currTarget);
    if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);
    if (canMoveInDirection(dir)) {
//      resetPathfinding();
      return true;
    }
    return false;
  }

  static boolean canMoveInDirection(Direction dir) throws GameActionException {
//    Printer.print("BUGNAV: Checking if can move in direction " + dir);
    if (!rc.canMove(dir)) return false;
    MapLocation nextLoc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
    if (blockedLocations.contains(nextLoc)) return false;
    if (!rc.canSenseLocation(nextLoc)) return true; // don't know what's there, assume it's fine
    MapInfo newLocInfo = rc.senseMapInfo(nextLoc);
    Direction windCurrentDir = newLocInfo.getCurrentDirection();
    if (!windCurrentDir.equals(dir.opposite())) return true; // wind is not blowing towards me
    // check if it's the last movement before turn end
    int moveCD = Cache.Permanent.ROBOT_TYPE == RobotType.CARRIER ? ((int) (GameConstants.CARRIER_MOVEMENT_INTERCEPT + rc.getWeight() * GameConstants.CARRIER_MOVEMENT_SLOPE)) : Cache.Permanent.ROBOT_TYPE.movementCooldown;
    int newMoveCD = rc.getMovementCooldownTurns() + ((int) (moveCD * newLocInfo.getCooldownMultiplier(Cache.Permanent.OUR_TEAM)));
    if (newMoveCD < GameConstants.COOLDOWN_LIMIT) return true; // can move again
    return false; // wind is blowing towards me and I can't move again
  }

  /**
   * does bug nav towards the current target
   * @return true if we moved
   */
  static boolean tryBugging() throws GameActionException {
//    Printer.print("BUGNAV: Trying to bug");
    //If I'm at a minimum distance to the target, I'm free!
    MapLocation myLoc = rc.getLocation();
    int d = myLoc.distanceSquaredTo(currTarget);
    if (d <= minDistToTarget) {
      resetPathfinding();
//      Printer.print("ERROR: should check done bugging first");
    }

    char code = getCode();

    if (visited.contains(code)) resetPathfinding();
    visited.add(code);

    //Update data
    minDistToTarget = Math.min(d, minDistToTarget);

    //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
    Direction dir = myLoc.directionTo(currTarget);
    if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);
    if (rc.canMove(dir)){
      resetPathfinding();
    }

    //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
    //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
//    Direction startingDir = dir;
    for (int i = 16; --i >= 0;) {
      if (canMoveInDirection(dir)) {
        return pathing.move(dir);
      }
      MapLocation newLoc = myLoc.add(dir);
      if (!rc.onTheMap(newLoc)) { // switch directions once map edge reached
        rotateRight = !rotateRight;
//        dir = startingDir;
      } else { //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
        lastObstacleFound = myLoc.add(dir);
//        rc.setIndicatorDot(lastObstacleFound, 255, 255, 0);
      }
      if (rotateRight) dir = dir.rotateRight();
      else dir = dir.rotateLeft();
    }

//    if (pathing.move(dir)) rc.move(dir);
    return canMoveInDirection(dir) && pathing.move(dir);
  }

  //clear some of the previous data
  static void resetPathfinding(){
    lastObstacleFound = null;
    minDistToTarget = INF;
    visited.clear();
  }

  static char getCode(){
//    int x = rc.getLocation().x % MAX_MAP_SIZE;
//    int y = rc.getLocation().y % MAX_MAP_SIZE;
    Direction obstacleDir = Cache.PerTurn.CURRENT_LOCATION.directionTo(currTarget);
    if (lastObstacleFound != null) obstacleDir = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastObstacleFound);
    int bit = rotateRight ? 1 : 0;
    return (char) ((((((Cache.PerTurn.CURRENT_LOCATION.x << 6) | Cache.PerTurn.CURRENT_LOCATION.y) << 3) | obstacleDir.ordinal()) << 1) | bit);
  }
}
