package actualcarrierecon.communications;

import actualcarrierecon.robots.HeadQuarters;
import actualcarrierecon.utils.Cache;
import actualcarrierecon.utils.Global;
import actualcarrierecon.utils.Printer;
import actualcarrierecon.utils.Utils;
import battlecode.common.*;


public class Communicator {
  public class HQInfo {
    public int hqCount;
    public MapLocation[] hqLocations;

    /**
     * gets the closeset HQ to the specified location based on comms info
     * @param location where to center the search for closest HQ
     * @return the hqID of the closest HQ
     */
    public int getClosestHQ(MapLocation location) {
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
  }

  public class MapInfo {
    public final Utils.MapSymmetry[] symmetryKnownMap = {
        null,
        null,
        null,
        Utils.MapSymmetry.HORIZONTAL,
        null,
        Utils.MapSymmetry.VERTICAL,
        Utils.MapSymmetry.ROTATIONAL,
//      null,
    };
    public final Utils.MapSymmetry[] symmetryGuessMap = {
        Utils.MapSymmetry.ROTATIONAL, // 000
        Utils.MapSymmetry.HORIZONTAL, // 001
        Utils.MapSymmetry.HORIZONTAL, // 010 - not vert (rot harder to rule out)
        Utils.MapSymmetry.HORIZONTAL, // 011
        Utils.MapSymmetry.VERTICAL,   // 100 - not horiz (rot harder to rule out)
        Utils.MapSymmetry.VERTICAL,   // 101
        Utils.MapSymmetry.ROTATIONAL, // 110
//        Utils.MapSymmetry.ROTATIONAL, // 111
    };

    private int symmetryInfo;
    public Utils.MapSymmetry knownSymmetry; // determined by next three bools
    public Utils.MapSymmetry guessedSymmetry; // determined by next three bools
    private static final int NOT_HORIZ_MASK = 0b100;
    public boolean notHorizontal;     // 0-1               -- 1 bit  [3]
    private static final int NOT_VERT_MASK = 0b10;
    public boolean notVertical;       // 0-1               -- 1 bit  [2]
    private static final int NOT_ROT_MASK = 0b1;
    public boolean notRotational;     // 0-1               -- 1 bit  [1]

    public void updateSymmetry() throws GameActionException {
      if (knownSymmetry != null) return;
      this.symmetryInfo = commsHandler.readMapSymmetry();
      knownSymmetry = symmetryKnownMap[symmetryInfo];
      notHorizontal = (symmetryInfo & NOT_HORIZ_MASK) > 0;
      notVertical = (symmetryInfo & NOT_VERT_MASK) > 0;
      notRotational = (symmetryInfo & NOT_ROT_MASK) > 0;
      guessedSymmetry = knownSymmetry != null ? knownSymmetry : symmetryGuessMap[symmetryInfo];
    }

    public void writeNot(Utils.MapSymmetry symmetryToEliminate) throws GameActionException {
      switch (symmetryToEliminate) {
        case ROTATIONAL:
          this.symmetryInfo |= NOT_ROT_MASK;
          break;
        case HORIZONTAL:
          this.symmetryInfo |= NOT_HORIZ_MASK;
          break;
        case VERTICAL:
          this.symmetryInfo |= NOT_VERT_MASK;
      }
      commsHandler.writeMapSymmetry(this.symmetryInfo);
      knownSymmetry = symmetryKnownMap[symmetryInfo];
      notHorizontal = (symmetryInfo & NOT_HORIZ_MASK) > 0;
      notVertical = (symmetryInfo & NOT_VERT_MASK) > 0;
      notRotational = (symmetryInfo & NOT_ROT_MASK) > 0;
      guessedSymmetry = knownSymmetry != null ? knownSymmetry : symmetryGuessMap[symmetryInfo];
      Printer.print("AYO I updated the symmetry!");
    }
  }

  public class MetaInfo {
    public HQInfo hqInfo;
    public MapInfo mapInfo;

    public MetaInfo() throws GameActionException {
      this.hqInfo = new HQInfo();
      this.mapInfo = new MapInfo();
      init();
    }

    public void init() throws GameActionException {
      hqInfo.hqCount = commsHandler.readHqCount();
      hqInfo.hqLocations = new MapLocation[hqInfo.hqCount];
      switch (hqInfo.hqCount) {
        case 4:
          hqInfo.hqLocations[3] = commsHandler.readOurHqLocation(3);
        case 3:
          hqInfo.hqLocations[2] = commsHandler.readOurHqLocation(2);
        case 2:
          hqInfo.hqLocations[1] = commsHandler.readOurHqLocation(1);
        case 1:
          hqInfo.hqLocations[0] = commsHandler.readOurHqLocation(0);
          break;
        default:
//          if (Cache.Permanent.ROBOT_TYPE == RobotType.HEADQUARTERS) {
//            Global.rc.setIndicatorString("HQ failing -- spawned" + Cache.Permanent.ROUND_SPAWNED + " -- round " + Cache.PerTurn.ROUND_NUM + " -- alive " + Cache.PerTurn.ROUNDS_ALIVE);
//            Printer.print("HQ failing -- spawned" + Cache.Permanent.ROUND_SPAWNED + " -- round " + Cache.PerTurn.ROUND_NUM + " -- alive " + Cache.PerTurn.ROUNDS_ALIVE);
//          }
          if (Cache.Permanent.ROBOT_TYPE != RobotType.HEADQUARTERS || Cache.PerTurn.ROUNDS_ALIVE > 1) {
            throw new RuntimeException("Invalid HQ count: " + hqInfo.hqCount);
          }
      }
    }

    public int registerHQ(WellInfo closestAdamantium, WellInfo closestMana) throws GameActionException {
      int hqID = hqInfo.hqCount;
//      Printer.print("Registering HQ " + hqID);
      hqInfo.hqCount++;
      commsHandler.writeHqCount(hqInfo.hqCount);
      commsHandler.writeOurHqLocation(hqID, Cache.PerTurn.CURRENT_LOCATION);
      if (closestAdamantium != null) {
        commsHandler.writeAdamantiumWellLocation(hqID, closestAdamantium.getMapLocation());
//        commsHandler.writeOurHqClosestAdamantiumLocation(hqID, closestAdamantium.getMapLocation());
      }
      if (closestMana != null) {
        commsHandler.writeManaWellLocation(hqID, closestMana.getMapLocation());
//        commsHandler.writeOurHqClosestManaLocation(hqID, closestMana.getMapLocation());
      }
      return hqID;
    }

    public void reinitForHQ() throws GameActionException {
//      Global.rc.setIndicatorString("HQ reinit!");
      init();
    }

    public void updateOnTurnStart() throws GameActionException {
      mapInfo.updateSymmetry();
    }
  }

  public CommsHandler commsHandler;

  public MetaInfo metaInfo;

  public Communicator(RobotController rc) throws GameActionException {
    this.commsHandler = new CommsHandler(rc);
    this.metaInfo = new MetaInfo();
  }

  /**
   * puts a well into the next free slot within the comms buffer for wells of that type
   * @param well the well info to broadcast
   * @return true if the information was successfully broadcast (or already in comms)
   * @throws GameActionException if any issues with reading/writing to comms
   */
  public boolean writeNextWell(WellInfo well) throws GameActionException {
    CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(well.getResourceType());
    for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
      if (!writer.readWellExists(i)) {
        writer.writeWellLocation(i, well.getMapLocation());
        writer.writeWellUpgraded(i, well.isUpgraded());
//        Printer.print("Published new well! " + well.getMapLocation());
        return true;
      } else if (writer.readWellLocation(i).equals(well.getMapLocation())) {
        if (writer.readWellUpgraded(i) != well.isUpgraded()) {
          writer.writeWellUpgraded(i, well.isUpgraded());
          return true;
        }
        return true;
//      } else {
//        Printer.print("Well already exists in comms: " + writer.readWellLocation(i));
      }
    }
//    Printer.print("Failed to write well " + well);
    return false;
  }
}
