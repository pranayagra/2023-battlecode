package basicbot.knowledge;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.utils.Global;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.WellInfo;

public class RunningMemory {
  public static final Utils.MapSymmetry[] SYMMETRY_KNOWN_MAP = {
      null,
      null,
      null,
      Utils.MapSymmetry.HORIZONTAL,
      null,
      Utils.MapSymmetry.VERTICAL,
      Utils.MapSymmetry.ROTATIONAL,
//      null,
  };
  public static final Utils.MapSymmetry[] SYMMETRY_GUESS_MAP = {
      Utils.MapSymmetry.ROTATIONAL, // 000
      Utils.MapSymmetry.HORIZONTAL, // 001
      Utils.MapSymmetry.HORIZONTAL, // 010 - not vert (rot harder to rule out)
      Utils.MapSymmetry.HORIZONTAL, // 011
      Utils.MapSymmetry.VERTICAL,   // 100 - not horiz (rot harder to rule out)
      Utils.MapSymmetry.VERTICAL,   // 101
      Utils.MapSymmetry.ROTATIONAL, // 110
//        Utils.MapSymmetry.ROTATIONAL, // 111
  };

  public static int symmetryInfo = 0;
  public static boolean symmetryInfoDirty = false;
  public static Utils.MapSymmetry knownSymmetry = SYMMETRY_KNOWN_MAP[0]; // determined by next three bools
  public static Utils.MapSymmetry guessedSymmetry = SYMMETRY_GUESS_MAP[0]; // determined by next three bools
  private static final int NOT_HORIZ_MASK = 0b100;
  public static boolean notHorizontalSymmetry;     // 0-1               -- 1 bit  [3]
  private static final int NOT_VERT_MASK = 0b10;
  public static boolean notVerticalSymmetry;       // 0-1               -- 1 bit  [2]
  private static final int NOT_ROT_MASK = 0b1;
  public static boolean notRotationalSymmetry;     // 0-1               -- 1 bit  [1]


  public static final int WELL_MEMORY_LIMIT = 32;
  public static WellInfo[] wells = new WellInfo[WELL_MEMORY_LIMIT];
  public static int wellCount = 0;
  private static final int[] wellsSeenTracker = new int[113];

  public static void markInvalidSymmetry(Utils.MapSymmetry symmetryToEliminate) throws GameActionException {
//    RunningMemory.symmetryInfo = symmetryInfo;
//    RunningMemory.symmetryInfoDirty = true;
    if (knownSymmetry != null) {
      Printer.print("ERROR: trying to mark invalid symmetry (" + symmetryToEliminate + ") when already known!" + knownSymmetry);
      return;
    }
    switch (symmetryToEliminate) {
      case ROTATIONAL:
        symmetryInfo |= NOT_ROT_MASK;
        notRotationalSymmetry = true;
        break;
      case HORIZONTAL:
        symmetryInfo |= NOT_HORIZ_MASK;
        notHorizontalSymmetry = true;
        break;
      case VERTICAL:
        symmetryInfo |= NOT_VERT_MASK;
        notVerticalSymmetry = true;
    }
//    Printer.print("AYO symmetry updated! (not " + symmetryToEliminate + ") -- known=" + knownSymmetry + " guessed=" + guessedSymmetry);
    knownSymmetry = SYMMETRY_KNOWN_MAP[symmetryInfo];
    guessedSymmetry = knownSymmetry != null ? knownSymmetry : SYMMETRY_GUESS_MAP[symmetryInfo];
    HqMetaInfo.recomputeEnemyHqLocations();
    symmetryInfoDirty = true;
//    if (knownSymmetry != null) {
//       Printer.print("AYO symmetry updated! (not " + symmetryToEliminate + ") -- known=" + knownSymmetry + " guessed=" + guessedSymmetry);
//    }
  }
  /**
   * read from shared array and update symmetry info
   * @return true if updated
   * @throws GameActionException
   */
  public static boolean updateSymmetry() throws GameActionException {
    int newSymmetryInfo = CommsHandler.readMapSymmetry();
    if (newSymmetryInfo == symmetryInfo) return false;
    Utils.MapSymmetry newKnownSymmetry = SYMMETRY_KNOWN_MAP[newSymmetryInfo];
//    if (knownSymmetry != null && knownSymmetry == newKnownSymmetry) return false;
    symmetryInfo = newSymmetryInfo;
    knownSymmetry = newKnownSymmetry;
    notHorizontalSymmetry = (newSymmetryInfo & NOT_HORIZ_MASK) != 0;
    notVerticalSymmetry = (newSymmetryInfo & NOT_VERT_MASK) != 0;
    notRotationalSymmetry = (newSymmetryInfo & NOT_ROT_MASK) != 0;
    guessedSymmetry = newKnownSymmetry != null ? newKnownSymmetry : SYMMETRY_GUESS_MAP[newSymmetryInfo];
    return true;
  }

  /**
   * write to shared array if in range
   * @throws GameActionException if cannot write
   */
  public static void broadcastSymmetry() throws GameActionException {
    if (!symmetryInfoDirty) return;
    if (!Global.rc.canWriteSharedArray(0,0)) return;
    CommsHandler.writeMapSymmetry(symmetryInfo);
    symmetryInfoDirty = false;
  }

  /**
   * writes the given well info into running memory or sends it to the comms array if in range
   * @param well the well info to write
   * @return false if already been visited, true otherwise
   */
  public static boolean publishWell(WellInfo well) {
    MapLocation loc = well.getMapLocation();
    int wellVisitedBit = loc.x + 60*loc.y;
    int wellVisitedBitVec = 1 << (31 - wellVisitedBit & 31);
    if ((wellsSeenTracker[wellVisitedBit >>> 5] & wellVisitedBitVec) != 0) return false;
    wellsSeenTracker[wellVisitedBit >>> 5] |= wellVisitedBitVec;
    if (wellCount >= WELL_MEMORY_LIMIT) {
//      Printer.print("TOO MANY MEMORIZED WELLS!! " + wellCount);
      return false;
    }
    wells[wellCount] = well;
    wellCount++;
    return true;
  }

  /**
   * flushes well memory into the comms array
   * @return the number of wells flushed
   */
  public static int broadcastMemorizedWells() throws GameActionException {
    if (wellCount == 0) return 0;
    if (!Global.rc.canWriteSharedArray(0,0)) return 0;
    if (Clock.getBytecodesLeft() < wellCount * 250) return 0;
    int oldCount = wellCount;
//    Printer.print("flushing " + wellCount + " wells - bc=" + Clock.getBytecodesLeft());
    while (wellCount > 0 && Communicator.writeNextWell(wells[wellCount-1])) {
//      Printer.print("flushed well " + wells[wellCount-1].getMapLocation());
      wellCount--;
    }
    return oldCount - wellCount;
  }
}
