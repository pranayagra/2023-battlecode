package basicbot.robots.micro;

import basicbot.communications.HqMetaInfo;
import basicbot.knowledge.Cache;
import basicbot.robots.Carrier;
import basicbot.robots.pathfinding.BugNav;
import basicbot.robots.pathfinding.Pathing;
import basicbot.utils.Global;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class CarrierWellMicro {

  private static RobotController rc;
  private static Carrier carrier;
  private static Pathing pathing;

  private static int fleeingCounter;


  public static MapLocation lastEnemyLocation;
  public static int lastEnemyLocationRound;
  public static RobotInfo cachedLastEnemyForBroadcast;

  public static void init() {
    rc = Global.rc;
    pathing = Pathing.globalPathing;
  }


  /**
   * checks if a certain position is a valid queueing spot for the given well location
   * should be passable and not a robot?
   * ASSUMES adjacency
   * @param wellLocation the well location
   * @param queuePosition the position to check
   * @return true if the position is valid
   * @throws GameActionException
   */
  public static boolean isValidQueuePosition(MapLocation wellLocation, MapLocation queuePosition) throws GameActionException {
//    if (queuePosition.x == 16 && queuePosition.y == 6) {
//      Printer.print("checking " + queuePosition + " for well " + wellLocation);
//      Printer.print("\tmap:" + rc.onTheMap(queuePosition) + ",sense:" + rc.canSenseLocation(queuePosition) + ",pass:" +  (rc.canSenseLocation(queuePosition) ? rc.senseMapInfo(queuePosition).isPassable() : "unknown"));
//      Printer.print("\tWadj:" + (rc.canSenseLocation(queuePosition) ? queuePosition.add(rc.senseMapInfo(queuePosition).getCurrentDirection()).isAdjacentTo(wellLocation) : "unknown")+ ",blk:" + BugNav.blockedLocations.contains(queuePosition) + ",Wblk:" + (rc.canSenseLocation(queuePosition) && BugNav.blockedLocations.contains(queuePosition.add(rc.senseMapInfo(queuePosition).getCurrentDirection()))));
//    }
    local_checks: {
      if (!rc.onTheMap(queuePosition)) return false;
      if (!rc.canSenseLocation(queuePosition)) break local_checks; // assume it is valid if can't sense
      if (rc.canSenseRobotAtLocation(queuePosition)) return false; // blocked by a robot
      MapInfo mapInfo = rc.senseMapInfo(queuePosition);
      if (!mapInfo.isPassable()) return false; // isn't passable
      if (!queuePosition.add(mapInfo.getCurrentDirection()).isAdjacentTo(wellLocation)) return false; // gets blown away
    }
    if (BugNav.blockedLocations.contains(queuePosition)) return false; // blocked by a bugnav
    if (rc.canSenseLocation(queuePosition)
        && BugNav.blockedLocations.contains(
        queuePosition.add(rc.senseMapInfo(queuePosition).getCurrentDirection()
        ))) return false; // pushed into a blocked location
    return true;
  }

  /**
   * checks if a certain position is a valid queueing spot for the given well location only considering PERMANENT obstructions
   * ASSUMES adjacency
   * queuePosition should be in vision range of the robot (this does not check this).
   * @param wellLocation the well location
   * @param queuePosition the position to check
   * @return true if the position is valid
   * @throws GameActionException
   */
  public static boolean isValidStaticQueuePosition(MapLocation wellLocation, MapLocation queuePosition) throws GameActionException {
//    if (wellLocation.x == 8 && wellLocation.y == 5) {
//      Printer.print("checking " + queuePosition + " for well " + wellLocation);
//      Printer.print("\tmap:" + rc.onTheMap(queuePosition) + ",sense:" + rc.canSenseLocation(queuePosition) + ",pass:" + rc.senseMapInfo(queuePosition).isPassable());
//      Printer.print("\tWadj:" + queuePosition.add(rc.senseMapInfo(queuePosition).getCurrentDirection()).isAdjacentTo(wellLocation) + ",blk:" + BugNav.blockedLocations.contains(queuePosition) + ",Wblk:" + (rc.canSenseLocation(queuePosition) && BugNav.blockedLocations.contains(queuePosition.add(rc.senseMapInfo(queuePosition).getCurrentDirection()))));
//    }
    if (!rc.onTheMap(queuePosition)) return false;
    MapInfo mapInfo = rc.canSenseLocation(queuePosition) ? rc.senseMapInfo(queuePosition) : null;
    if (mapInfo == null) { // assume it is valid if can't sense
      return !BugNav.blockedLocations.contains(queuePosition);
    }
    if (BugNav.blockedLocations.contains(queuePosition)) return false; // blocked by a bugnav
    if (!mapInfo.isPassable()) return false; // isn't passable
    MapLocation nextPos = queuePosition.add(mapInfo.getCurrentDirection());
    if (!nextPos.isAdjacentTo(wellLocation)) return false; // gets blown away
    return !BugNav.blockedLocations.contains(nextPos); // pushed into a blocked location
  }
}
