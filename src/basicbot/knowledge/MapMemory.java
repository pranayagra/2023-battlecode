package basicbot.knowledge;

import basicbot.knowledge.unitknowledge.UnitKnowledge;
import basicbot.utils.Global;
import basicbot.utils.Utils;
import battlecode.common.*;

public class MapMemory {

  public static RobotController rc;
  public static MemorizedMapInfo[] mapInfo;

  public static void setup() throws GameActionException {
    MapMemory.rc = Global.rc;
    MapMemory.mapInfo = new MemorizedMapInfo[Cache.Permanent.MAP_WIDTH * Cache.Permanent.MAP_HEIGHT];
    int currVision = Cache.Permanent.VISION_RADIUS_SQUARED;
    if (rc.senseCloud(rc.getLocation())) {
      currVision = GameConstants.CLOUD_VISION_RADIUS_SQUARED;
    }
//    updateLocations(rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), currVision));
  }

  public static void update(MapLocation previousLocation, MapLocation currentLocation) throws GameActionException {
//    if (previousLocation == null) return;
//    if (rc.senseCloud(currentLocation)) {
//      updateLocations(rc.getAllLocationsWithinRadiusSquared(currentLocation, GameConstants.CLOUD_VISION_RADIUS_SQUARED));
//      return;
//    }
//    updateLocations(UnitKnowledge.newUnseenMapLocations(previousLocation, currentLocation));
  }

  private static void updateLocations(MapLocation[] locations) throws GameActionException {
    if (Cache.Permanent.ID != 10995) return;
    for (int i = locations.length; --i >= 0;) {
//      Utils.startByteCodeCounting("MapMemory.updateLocations" + i);
      MapLocation loc = locations[i];
      if (!rc.onTheMap(loc)) continue;
//      if (mapInfo[loc.x] == null) {
//        mapInfo[loc.x] = new MemorizedMapInfo[Cache.Permanent.MAP_HEIGHT];
//      }
      int index = loc.x * Cache.Permanent.MAP_HEIGHT + loc.y;
      if (mapInfo[index] != null) continue;
//      if (!rc.canSenseLocation(loc)) continue;
      mapInfo[index] = new MemorizedMapInfo(loc);
//      Utils.finishByteCodeCounting("MapMemory.updateLocations" + i);
    }
  }

  public static class MemorizedMapInfo {

    public boolean isPassable;
    public Direction currentDir;

    public MemorizedMapInfo(MapLocation loc) throws GameActionException {
      MapInfo locInfo = rc.senseMapInfo(loc);
      isPassable = locInfo.isPassable();
      currentDir = locInfo.getCurrentDirection();
    }
  }
}
