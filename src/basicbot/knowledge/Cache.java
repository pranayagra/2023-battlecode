package basicbot.knowledge;

import basicbot.utils.Global;
import basicbot.utils.Utils;
import battlecode.common.*;

public class Cache {
  public static class Permanent {
    public static int ROUND_SPAWNED;
    public static Team OUR_TEAM;
    public static Team OPPONENT_TEAM;
    public static RobotType ROBOT_TYPE;
    public static int ID;
    public static int VISION_RADIUS_SQUARED;
    public static int ACTION_RADIUS_SQUARED;
    public static int VISION_RADIUS_FLOOR;
    public static int ACTION_RADIUS_FLOOR;
    public static MapLocation START_LOCATION;
    public static int MAP_WIDTH;
    public static int MAP_HEIGHT;
    public static int MAP_AREA;

    public static int MAX_HEALTH;
  }

  static void setup() {
    Permanent.ROUND_SPAWNED = Global.rc.getRoundNum();
    Permanent.OUR_TEAM = Global.rc.getTeam();
    Permanent.OPPONENT_TEAM = Permanent.OUR_TEAM.opponent();
    Permanent.ROBOT_TYPE = Global.rc.getType();
    Permanent.START_LOCATION = Global.rc.getLocation();
    Permanent.ID = Global.rc.getID();
    Permanent.VISION_RADIUS_SQUARED = Permanent.ROBOT_TYPE.visionRadiusSquared;
    Permanent.ACTION_RADIUS_SQUARED = Permanent.ROBOT_TYPE.actionRadiusSquared;
    Permanent.VISION_RADIUS_FLOOR = (int) Math.sqrt(Permanent.VISION_RADIUS_SQUARED);
    Permanent.ACTION_RADIUS_FLOOR = (int) Math.sqrt(Permanent.ACTION_RADIUS_SQUARED);
    Permanent.MAP_WIDTH = Global.rc.getMapWidth();
    Permanent.MAP_HEIGHT = Global.rc.getMapHeight();
    Permanent.MAP_AREA = Permanent.MAP_WIDTH * Permanent.MAP_HEIGHT;
    Permanent.MAX_HEALTH = Global.rc.getType().health;
  }

  public static class PerTurn {
    public static int ROUND_NUM;
    public static int ROUNDS_ALIVE; // round num - round spawned (=0 on first turn)
    public static RobotInfo[] ALL_NEARBY_ROBOTS;
    public static RobotInfo[] ALL_NEARBY_FRIENDLY_ROBOTS;
    public static RobotInfo[] ALL_NEARBY_ENEMY_ROBOTS;

    public static MapLocation CURRENT_LOCATION;
    public static MapLocation PREVIOUS_LOCATION;
    public static int ROUND_LAST_MOVED;

    public static final int[] GLOBAL_VISITED_LOCS = new int[113];
    //        public static int LEVEL;
    public static int HEALTH;
    //        public static MapLocation[] NEARBY_LEAD_MIN_2;
//        public static MapLocation[] NEARBY_LEAD_2;

    public static int cacheState;

//    static void whenMoved() throws GameActionException {
//      // don't need to update
//      if (PerTurn.CURRENT_LOCATION != null && Global.rc.getLocation().equals(PerTurn.CURRENT_LOCATION)) {
//        return;
//      }
//      // do the update
//      updateForMovement();
//    }

    static void updateForMovement() throws GameActionException {
      PerTurn.cacheState++;
      PerTurn.PREVIOUS_LOCATION = PerTurn.CURRENT_LOCATION;
      if (PREVIOUS_LOCATION != null) {
        int visitedBit = PREVIOUS_LOCATION.x + 60 * PREVIOUS_LOCATION.y;
        GLOBAL_VISITED_LOCS[visitedBit >>> 5] |= 1 << (31 - visitedBit & 31);
      }
      PerTurn.CURRENT_LOCATION = Global.rc.getLocation();
      PerTurn.ROUND_LAST_MOVED = PerTurn.ROUND_NUM;
      PerTurn.ALL_NEARBY_ROBOTS = Global.rc.senseNearbyRobots();
      PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS = Global.rc.senseNearbyRobots(-1, Permanent.OUR_TEAM);
      PerTurn.ALL_NEARBY_ENEMY_ROBOTS = Global.rc.senseNearbyRobots(-1, Permanent.OPPONENT_TEAM);
//            PerTurn.NEARBY_LEAD_MIN_2 = Global.rc.senseNearbyLocationsWithLead(-1, 2);
    }

    public static boolean hasPreviouslyVisited(MapLocation loc) {
      int visitedBit = loc.x + 60 * loc.y;
      return (GLOBAL_VISITED_LOCS[visitedBit >>> 5] & (1 << (31 - visitedBit & 31))) != 0;
    }
    public static boolean hasPreviouslyVisitedOwnLoc() {
      int visitedBit = CURRENT_LOCATION.x + 60 * CURRENT_LOCATION.y;
      return (GLOBAL_VISITED_LOCS[visitedBit >>> 5] & (1 << (31 - visitedBit & 31))) != 0;
    }
  }

  static void updateOnTurn() throws GameActionException {
    PerTurn.ROUND_NUM = Global.rc.getRoundNum();
    PerTurn.ROUNDS_ALIVE = PerTurn.ROUND_NUM - Permanent.ROUND_SPAWNED;
//        PerTurn.LEVEL = Global.rc.getLevel();
    PerTurn.HEALTH = Global.rc.getHealth();
//    PerTurn.updateForMovement();
  }
}
