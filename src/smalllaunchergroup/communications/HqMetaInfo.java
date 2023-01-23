package smalllaunchergroup.communications;

import smalllaunchergroup.knowledge.Cache;
import smalllaunchergroup.knowledge.RunningMemory;
import smalllaunchergroup.utils.Utils;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class HqMetaInfo {
  private static final int ENEMY_TERRITORY_HQ_DANGER_RADIUS_SQ = 145;
  public static int hqCount;
  public static MapLocation[] hqLocations;
  public static MapLocation[] enemyHqLocations;

  public static void init() throws GameActionException {
    HqMetaInfo.hqCount = CommsHandler.readHqCount();
    HqMetaInfo.hqLocations = new MapLocation[HqMetaInfo.hqCount];
    HqMetaInfo.enemyHqLocations = new MapLocation[HqMetaInfo.hqCount];
    switch (HqMetaInfo.hqCount) {
      case 4:
        HqMetaInfo.hqLocations[3] = CommsHandler.readOurHqLocation(3);
        HqMetaInfo.enemyHqLocations[3] = Utils.applySymmetry(HqMetaInfo.hqLocations[3], RunningMemory.guessedSymmetry);
      case 3:
        HqMetaInfo.hqLocations[2] = CommsHandler.readOurHqLocation(2);
        HqMetaInfo.enemyHqLocations[2] = Utils.applySymmetry(HqMetaInfo.hqLocations[2], RunningMemory.guessedSymmetry);
      case 2:
        HqMetaInfo.hqLocations[1] = CommsHandler.readOurHqLocation(1);
        HqMetaInfo.enemyHqLocations[1] = Utils.applySymmetry(HqMetaInfo.hqLocations[1], RunningMemory.guessedSymmetry);
      case 1:
        HqMetaInfo.hqLocations[0] = CommsHandler.readOurHqLocation(0);
        HqMetaInfo.enemyHqLocations[0] = Utils.applySymmetry(HqMetaInfo.hqLocations[0], RunningMemory.guessedSymmetry);
        break;
      default:
//          if (Cache.Permanent.ROBOT_TYPE == RobotType.HEADQUARTERS) {
//            Global.rc.setIndicatorString("HQ failing -- spawned" + Cache.Permanent.ROUND_SPAWNED + " -- round " + Cache.PerTurn.ROUND_NUM + " -- alive " + Cache.PerTurn.ROUNDS_ALIVE);
//            Printer.print("HQ failing -- spawned" + Cache.Permanent.ROUND_SPAWNED + " -- round " + Cache.PerTurn.ROUND_NUM + " -- alive " + Cache.PerTurn.ROUNDS_ALIVE);
//          }
        if (Cache.Permanent.ROBOT_TYPE != RobotType.HEADQUARTERS || Cache.PerTurn.ROUNDS_ALIVE > 1) {
          throw new RuntimeException("Invalid HQ count: " + HqMetaInfo.hqCount);
        }
    }
  }

  /**
   * gets the closeset HQ to the specified location based on comms info
   *
   * @param location where to center the search for closest HQ
   * @return the hqID of the closest HQ
   */
  public static int getClosestHQ(MapLocation location) {
    int closest = 0;
    // optimized switch statement to use as little jvm/java bytecode as possible
    switch (hqCount) {
      case 4:
        if (location.distanceSquaredTo(hqLocations[3]) <
            location.distanceSquaredTo(hqLocations[closest])) {
          closest = 3;
        }
      case 3:
        if (location.distanceSquaredTo(hqLocations[2]) <
            location.distanceSquaredTo(hqLocations[closest])) {
          closest = 2;
        }
      case 2:
        if (location.distanceSquaredTo(hqLocations[1]) <
            location.distanceSquaredTo(hqLocations[closest])) {
          closest = 1;
        }
        return closest;
      case 1:
        return 0;
    }
    throw new RuntimeException("hqCount is not 1, 2, 3, or 4. Got=" + hqCount);
  }

  public static MapLocation getClosestHqLocation(MapLocation location) {
    int closest = 0;
    // optimized switch statement to use as little jvm/java bytecode as possible
    switch (hqCount) {
      case 4:
        if (location.distanceSquaredTo(hqLocations[3]) <
            location.distanceSquaredTo(hqLocations[closest])) {
          closest = 3;
        }
      case 3:
        if (location.distanceSquaredTo(hqLocations[2]) <
            location.distanceSquaredTo(hqLocations[closest])) {
          closest = 2;
        }
      case 2:
        if (location.distanceSquaredTo(hqLocations[1]) <
            location.distanceSquaredTo(hqLocations[closest])) {
          closest = 1;
        }
        return hqLocations[closest];
      case 1:
        return hqLocations[0];
    }
    throw new RuntimeException("hqCount is not 1, 2, 3, or 4. Got=" + hqCount);
  }
  public static MapLocation getFurthestHqLocation(MapLocation fromHere) {
    int furthest = 0;
    // optimized switch statement to use as little jvm/java bytecode as possible
    switch (hqCount) {
      case 4:
        if (fromHere.distanceSquaredTo(hqLocations[3]) >
            fromHere.distanceSquaredTo(hqLocations[furthest])) {
          furthest = 3;
        }
      case 3:
        if (fromHere.distanceSquaredTo(hqLocations[2]) >
            fromHere.distanceSquaredTo(hqLocations[furthest])) {
          furthest = 2;
        }
      case 2:
        if (fromHere.distanceSquaredTo(hqLocations[1]) >
            fromHere.distanceSquaredTo(hqLocations[furthest])) {
          furthest = 1;
        }
        return hqLocations[furthest];
      case 1:
        return hqLocations[0];
    }
    throw new RuntimeException("hqCount is not 1, 2, 3, or 4. Got=" + hqCount);
  }

  public static MapLocation getClosestEnemyHqLocation(MapLocation location) {
    int closest = 0;
    // optimized switch statement to use as little jvm/java bytecode as possible
    switch (hqCount) {
      case 4:
        if (location.distanceSquaredTo(enemyHqLocations[3]) <
            location.distanceSquaredTo(enemyHqLocations[closest])) {
          closest = 3;
        }
      case 3:
        if (location.distanceSquaredTo(enemyHqLocations[2]) <
            location.distanceSquaredTo(enemyHqLocations[closest])) {
          closest = 2;
        }
      case 2:
        if (location.distanceSquaredTo(enemyHqLocations[1]) <
            location.distanceSquaredTo(enemyHqLocations[closest])) {
          closest = 1;
        }
        return enemyHqLocations[closest];
      case 1:
        return enemyHqLocations[0];
    }
    throw new RuntimeException("hqCount is not 1, 2, 3, or 4. Got=" + hqCount);
  }

  public static void recomputeEnemyHqLocations() {
    Utils.MapSymmetry symmetry = RunningMemory.guessedSymmetry;
//    if (Cache.Permanent.ROBOT_TYPE == RobotType.HEADQUARTERS && hqCount != hqLocations.length) {
//      Printer.print("recompute enemy locations: hqCount=" + hqCount + " - numHq=" + hqLocations.length + " - numEnemy=" + enemyHqLocations.length);
//    }
    switch (hqCount) {
      case 4:
        enemyHqLocations[3] = Utils.applySymmetry(hqLocations[3], symmetry);
      case 3:
        enemyHqLocations[2] = Utils.applySymmetry(hqLocations[2], symmetry);
      case 2:
        enemyHqLocations[1] = Utils.applySymmetry(hqLocations[1], symmetry);
      case 1:
        enemyHqLocations[0] = Utils.applySymmetry(hqLocations[0], symmetry);
        break;
      default:
//          if (Cache.Permanent.ROBOT_TYPE == RobotType.HEADQUARTERS) {
//            Global.rc.setIndicatorString("HQ failing -- spawned" + Cache.Permanent.ROUND_SPAWNED + " -- round " + Cache.PerTurn.ROUND_NUM + " -- alive " + Cache.PerTurn.ROUNDS_ALIVE);
//            Printer.print("HQ failing -- spawned" + Cache.Permanent.ROUND_SPAWNED + " -- round " + Cache.PerTurn.ROUND_NUM + " -- alive " + Cache.PerTurn.ROUNDS_ALIVE);
//          }
        if (Cache.Permanent.ROBOT_TYPE != RobotType.HEADQUARTERS || Cache.PerTurn.ROUNDS_ALIVE > 1) {
          throw new RuntimeException("Invalid HQ count: " + hqCount);
        }
    }
  }

  public static boolean isEnemyTerritory(MapLocation location) {
    MapLocation closestEnemyHQ = getClosestEnemyHqLocation(location);
    int enemyDist = location.distanceSquaredTo(closestEnemyHQ);
    if (enemyDist <= ENEMY_TERRITORY_HQ_DANGER_RADIUS_SQ) return true;
    MapLocation closestHQ = getClosestHqLocation(location);
    int friendlyDist = location.distanceSquaredTo(closestHQ);
    return enemyDist < friendlyDist;
  }
}
