package basicbot.communications;

import basicbot.robots.HeadQuarters;
import basicbot.utils.Cache;
import basicbot.utils.Utils;
import battlecode.common.*;


public class Communicator {
  public class HQInfo {
    public static final int HQ_DOESNT_EXIST = 0;
    public static final int HQ_EXISTS = 1;

    public int hqCount;
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
        Utils.MapSymmetry.ROTATIONAL, // 010
        Utils.MapSymmetry.HORIZONTAL, // 011
        Utils.MapSymmetry.ROTATIONAL, // 100
        Utils.MapSymmetry.VERTICAL,   // 101
        Utils.MapSymmetry.ROTATIONAL, // 110
//        Utils.MapSymmetry.ROTATIONAL, // 111
    };

    public Utils.MapSymmetry knownSymmetry; // determined by next three bools
    public Utils.MapSymmetry guessedSymmetry; // determined by next three bools
    private static final int NOT_HORIZ_MASK = 0b100;
    public boolean notHorizontal;     // 0-1               -- 1 bit  [3]
    private static final int NOT_VERT_MASK = 0b10;
    public boolean notVertical;       // 0-1               -- 1 bit  [2]
    private static final int NOT_ROT_MASK = 0b1;
    public boolean notRotational;     // 0-1               -- 1 bit  [1]
    private static final int ALL_SYM_INFO_MASK = NOT_HORIZ_MASK | NOT_VERT_MASK | NOT_ROT_MASK;
    private static final int SYM_INFO_INVERTED_MASK = ~ALL_SYM_INFO_MASK;

    public void updateSymmetry(int symmetryInfo) {
      if (knownSymmetry != null) return;
      knownSymmetry = symmetryKnownMap[(symmetryInfo & ALL_SYM_INFO_MASK) >> 1];
      notHorizontal = (symmetryInfo & NOT_HORIZ_MASK) > 0;
      notVertical = (symmetryInfo & NOT_VERT_MASK) > 0;
      notRotational = (symmetryInfo & NOT_ROT_MASK) > 0;
      guessedSymmetry = knownSymmetry != null ? knownSymmetry : symmetryGuessMap[(symmetryInfo & ALL_SYM_INFO_MASK) >> 1];
    }
  }

  public class MetaInfo {
    public HQInfo hqInfo;
    public MapInfo mapInfo;

    public MetaInfo() {
      this.hqInfo = new HQInfo();
      this.mapInfo = new MapInfo();
    }

    public void init() throws GameActionException {
      hqInfo.hqCount = commsHandler.readHqCount();
    }

    public int registerHQ(HeadQuarters hq, WellInfo closestAdamantium, WellInfo closestMana) throws GameActionException {
      int hqID = hqInfo.hqCount;
      hqInfo.hqCount++;
      commsHandler.writeHqCount(hqID);
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
      init();
    }

    public void updateOnTurnStart() throws GameActionException {
      mapInfo.updateSymmetry(commsHandler.readMapSymmetry());
    }
  }

  public CommsHandler commsHandler;

  public MetaInfo metaInfo;

  public Communicator(RobotController rc) throws GameActionException {
    this.commsHandler = new CommsHandler(rc);
    this.metaInfo = new MetaInfo();
  }
}
