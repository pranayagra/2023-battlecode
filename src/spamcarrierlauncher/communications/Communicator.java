package spamcarrierlauncher.communications;

import spamcarrierlauncher.robots.HeadQuarters;
import spamcarrierlauncher.utils.Cache;
import spamcarrierlauncher.utils.Global;
import spamcarrierlauncher.utils.Printer;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.WellInfo;

public class Communicator {
  public static final int NUM_HQS = 4;
  public static final int CLOSEST_ADAMANTIUM_WELL_INFO_START = 5;
  public static final int CLOSEST_MANA_WELL_INFO_START = 9;


  public void registerHQ(HeadQuarters hq, WellInfo closestAdamantium, WellInfo closestMana) throws GameActionException {
    if (hq.hqID == 0) {
      for (int i = 0; i < 4; i++) {
        Global.rc.writeSharedArray(i, 0b111111111111111);
        Global.rc.writeSharedArray(i+CLOSEST_ADAMANTIUM_WELL_INFO_START, 0b111111111111111);
        Global.rc.writeSharedArray(i+CLOSEST_MANA_WELL_INFO_START, 0b111111111111111);
      }
//      return;
    }
    Global.rc.writeSharedArray(NUM_HQS, Global.rc.readSharedArray(NUM_HQS) + 1);
    Printer.print("Total HQs: " + Global.rc.readSharedArray(NUM_HQS));
    writeLocationTopBits(hq.hqID,
        Cache.Permanent.START_LOCATION);
    if (closestAdamantium != null) {
      int dist = (int) Math.sqrt(closestAdamantium.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION));
      int wellUpgraded = closestAdamantium.isUpgraded() ? 1 : 0;
      writeLocationTopBitsWithExtraData(hq.hqID + CLOSEST_ADAMANTIUM_WELL_INFO_START,
          closestAdamantium.getMapLocation(),
          wellUpgraded << 3 | dist);
    }
    if (closestMana != null) {
      int dist = (int) Math.sqrt(closestMana.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION));
      int wellUpgraded = closestMana.isUpgraded() ? 1 : 0;
      writeLocationTopBitsWithExtraData(hq.hqID + CLOSEST_MANA_WELL_INFO_START,
          closestMana.getMapLocation(),
          wellUpgraded << 3 | dist);
    }
  }

  public void writeLocationTopBits(int index, MapLocation location) throws GameActionException {
    Printer.print("WRITE (" + index + "): " + location);
    Global.rc.writeSharedArray(index, location.x << 10 | location.y << 4);
  }

  public void writeLocationTopBitsWithExtraData(int index, MapLocation location, int otherData) throws GameActionException {
    Printer.print("WRITE (" + index + "): " + location + "," + otherData);
    Global.rc.writeSharedArray(index, location.x << 10 | location.y << 4 | otherData);
  }

  public MapLocation readLocationTopBits(int index) throws GameActionException {
    int data = Global.rc.readSharedArray(index);
    return new MapLocation(data >> 10, (data >> 4) & 0b111111);
  }
}
