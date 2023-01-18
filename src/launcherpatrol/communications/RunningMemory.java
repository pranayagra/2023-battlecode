package launcherpatrol.communications;

import launcherpatrol.utils.Global;
import launcherpatrol.utils.Printer;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.WellInfo;

public class RunningMemory {
  public static int symmetryInfo = 0;
  public static boolean symmetryInfoDirty = false;

  public static final int WELL_MEMORY_LIMIT = 32;
  public static WellInfo[] wells = new WellInfo[WELL_MEMORY_LIMIT];
  public static int wellCount = 0;
  private static final int[] wellsSeenTracker = new int[113];

//  public static void updateSymmetryNot(int symmetryInfo) {
//    RunningMemory.symmetryInfo = symmetryInfo;
//    RunningMemory.symmetryInfoDirty = true;
//  }

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
