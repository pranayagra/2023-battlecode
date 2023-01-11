package basicbot.communications;

import basicbot.robots.HeadQuarters;
import basicbot.utils.Cache;
import basicbot.utils.Global;
import basicbot.utils.Printer;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.WellInfo;
import battlecode.world.Well;

public class Communicator {
  public static final int NUM_HQS = 4;
  public static final int CLOSEST_ADAMANTIUM_WELL_INFO_START = 5;
  public static final int CLOSEST_MANA_WELL_INFO_START = 9;

  /**
   * registers a new head quarters into the comms array
   * @param hq the headquarters
   * @param closestAdamantium the closest Ad well in vision
   * @param closestMana closest Ma well in vision
   * @return the ID of this HQ
   * @throws GameActionException any game error
   */
  public int registerHQ(HeadQuarters hq, WellInfo closestAdamantium, WellInfo closestMana) throws GameActionException {
    int hqID = getFirstEmptyHQID();
    if (hqID == 0) {
      for (int i = 0; i < 4; i++) {
//        Global.rc.writeSharedArray(i, 0b111111111111111);
        Global.rc.writeSharedArray(i+CLOSEST_ADAMANTIUM_WELL_INFO_START, 0b111111111111111);
        Global.rc.writeSharedArray(i+CLOSEST_MANA_WELL_INFO_START, 0b111111111111111);
      }
    }
    Global.rc.writeSharedArray(NUM_HQS, hqID+1);
    Printer.print("Total HQs: " + (hqID+1));
    writeLocationTopBitsWithExtraData(hqID,
        Cache.Permanent.START_LOCATION, 0b1);
    if (closestAdamantium != null) {
      int dist = (int) Math.sqrt(closestAdamantium.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION));
      int wellUpgraded = closestAdamantium.isUpgraded() ? 1 : 0;
      writeLocationTopBitsWithExtraData(hqID + CLOSEST_ADAMANTIUM_WELL_INFO_START,
          closestAdamantium.getMapLocation(),
          wellUpgraded << 3 | dist);
    }
    if (closestMana != null) {
      int dist = (int) Math.sqrt(closestMana.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION));
      int wellUpgraded = closestMana.isUpgraded() ? 1 : 0;
      writeLocationTopBitsWithExtraData(hqID + CLOSEST_MANA_WELL_INFO_START,
          closestMana.getMapLocation(),
          wellUpgraded << 3 | dist);
    }
    return hqID;
  }

  private int getFirstEmptyHQID() throws GameActionException {
    for (int i = 0; i < NUM_HQS; i++) {
      if (Global.rc.readSharedArray(i) == 0) {
        return i;
      }
    }
    return 0;
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
