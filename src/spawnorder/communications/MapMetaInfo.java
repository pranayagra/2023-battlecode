package spawnorder.communications;

import spawnorder.utils.Printer;
import spawnorder.utils.Utils;
import battlecode.common.GameActionException;

public class MapMetaInfo {
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

  private static int symmetryInfo;
  public static Utils.MapSymmetry knownSymmetry = SYMMETRY_KNOWN_MAP[0]; // determined by next three bools
  public static Utils.MapSymmetry guessedSymmetry = SYMMETRY_GUESS_MAP[0]; // determined by next three bools
  private static final int NOT_HORIZ_MASK = 0b100;
  public static boolean notHorizontal;     // 0-1               -- 1 bit  [3]
  private static final int NOT_VERT_MASK = 0b10;
  public static boolean notVertical;       // 0-1               -- 1 bit  [2]
  private static final int NOT_ROT_MASK = 0b1;
  public static boolean notRotational;     // 0-1               -- 1 bit  [1]

  /**
   * read from shared array and update symmetry info
   * @return true if updated
   * @throws GameActionException
   */
  public static boolean updateSymmetry() throws GameActionException {
    if (knownSymmetry != null) return false;
    int newSymmetryInfo = CommsHandler.readMapSymmetry();
    if (newSymmetryInfo == symmetryInfo) return false;
    symmetryInfo = newSymmetryInfo;
    knownSymmetry = SYMMETRY_KNOWN_MAP[symmetryInfo];
    notHorizontal = (symmetryInfo & NOT_HORIZ_MASK) > 0;
    notVertical = (symmetryInfo & NOT_VERT_MASK) > 0;
    notRotational = (symmetryInfo & NOT_ROT_MASK) > 0;
    guessedSymmetry = knownSymmetry != null ? knownSymmetry : SYMMETRY_GUESS_MAP[symmetryInfo];
    return true;
  }

  public static void writeNot(Utils.MapSymmetry symmetryToEliminate) throws GameActionException {
    switch (symmetryToEliminate) {
      case ROTATIONAL:
        symmetryInfo |= NOT_ROT_MASK;
        break;
      case HORIZONTAL:
        symmetryInfo |= NOT_HORIZ_MASK;
        break;
      case VERTICAL:
        symmetryInfo |= NOT_VERT_MASK;
    }
    CommsHandler.writeMapSymmetry(symmetryInfo);
    knownSymmetry = SYMMETRY_KNOWN_MAP[symmetryInfo];
    notHorizontal = (symmetryInfo & NOT_HORIZ_MASK) > 0;
    notVertical = (symmetryInfo & NOT_VERT_MASK) > 0;
    notRotational = (symmetryInfo & NOT_ROT_MASK) > 0;
    guessedSymmetry = knownSymmetry != null ? knownSymmetry : SYMMETRY_GUESS_MAP[symmetryInfo];
    HqMetaInfo.recomputeEnemyHqLocations();
    // Printer.print("AYO I updated the symmetry!");
  }
}
