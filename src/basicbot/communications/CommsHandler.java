package basicbot.communications;

import basicbot.utils.Global;
import basicbot.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int PRANAY_OUR_HQ_SLOTS = 8;
  public static final int PRANAY_WELL_INFO_SLOTS = 4;
  public static final int ADAMANTIUM_WELL_SLOTS = 4;
  public static final int MANA_WELL_SLOTS = 4;
  public static final int ELIXIR_WELL_SLOTS = 4;
  public static final int ENEMY_SLOTS = 3;
  public static final MapLocation NONEXISTENT_MAP_LOC = new MapLocation(-1,-1);


  static RobotController rc;

  public static void init(RobotController rc) throws GameActionException {
    CommsHandler.rc = rc;
  }


  public static int readHqCount() throws GameActionException {
    return (rc.readSharedArray(0) & 57344) >>> 13;
  }

  public static void writeHqCount(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 8191) | (value << 13));
  }

  public static int readMapSymmetry() throws GameActionException {
    return (rc.readSharedArray(0) & 7168) >>> 10;
  }

  public static void writeMapSymmetry(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 58367) | (value << 10));
  }

  private static int readOurHqX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(0) & 1008) >>> 4;
      case 1:
          return (rc.readSharedArray(2) & 32256) >>> 9;
      case 2:
          return ((rc.readSharedArray(3) & 15) << 2) + ((rc.readSharedArray(4) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(5) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private static void writeOurHqX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 33279) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65031) | (value << 3));
        break;
    }
  }

  private static int readOurHqY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(0) & 15) << 2) + ((rc.readSharedArray(1) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(2) & 504) >>> 3;
      case 2:
          return (rc.readSharedArray(4) & 16128) >>> 8;
      case 3:
          return ((rc.readSharedArray(5) & 7) << 3) + ((rc.readSharedArray(6) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private static void writeOurHqY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  public static int readOurHqAdamantiumIncome(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 15872) >>> 9;
      case 1:
          return ((rc.readSharedArray(2) & 7) << 2) + ((rc.readSharedArray(3) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(4) & 248) >>> 3;
      case 3:
          return (rc.readSharedArray(6) & 7936) >>> 8;
      default:
          return -1;
    }
  }

  public static void writeOurHqAdamantiumIncome(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 49663) | (value << 9));
        break;
      case 1:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65528) | ((value & 28) >>> 2));
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65287) | (value << 3));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 57599) | (value << 8));
        break;
    }
  }

  public static int readOurHqManaIncome(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 496) >>> 4;
      case 1:
          return (rc.readSharedArray(3) & 15872) >>> 9;
      case 2:
          return ((rc.readSharedArray(4) & 7) << 2) + ((rc.readSharedArray(5) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(6) & 248) >>> 3;
      default:
          return -1;
    }
  }

  public static void writeOurHqManaIncome(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65039) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 49663) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65528) | ((value & 28) >>> 2));
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65287) | (value << 3));
        break;
    }
  }

  public static int readOurHqElixirIncome(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(1) & 15) << 1) + ((rc.readSharedArray(2) & 32768) >>> 15);
      case 1:
          return (rc.readSharedArray(3) & 496) >>> 4;
      case 2:
          return (rc.readSharedArray(5) & 15872) >>> 9;
      case 3:
          return ((rc.readSharedArray(6) & 7) << 2) + ((rc.readSharedArray(7) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  public static void writeOurHqElixirIncome(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65520) | ((value & 30) >>> 1));
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 32767) | ((value & 1) << 15));
        break;
      case 1:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65039) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 49663) | (value << 9));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65528) | ((value & 28) >>> 2));
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 16383) | ((value & 3) << 14));
        break;
    }
  }

  public static MapLocation readOurHqLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqX(idx)-1,readOurHqY(idx)-1);
  }
  public static boolean readOurHqExists(int idx) throws GameActionException {
    return !((readOurHqLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeOurHqLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqX(idx, (value).x+1);
    writeOurHqY(idx, (value).y+1);
  }
  private static int readPranayOurHqOddSpawnX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(7) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(10) & 1008) >>> 4;
      case 2:
          return (rc.readSharedArray(13) & 63);
      case 3:
          return ((rc.readSharedArray(16) & 3) << 4) + ((rc.readSharedArray(17) & 61440) >>> 12);
      case 4:
          return (rc.readSharedArray(20) & 16128) >>> 8;
      case 5:
          return (rc.readSharedArray(23) & 1008) >>> 4;
      case 6:
          return (rc.readSharedArray(26) & 63);
      case 7:
          return ((rc.readSharedArray(29) & 3) << 4) + ((rc.readSharedArray(30) & 61440) >>> 12);
      default:
          return -1;
    }
  }

  private static void writePranayOurHqOddSpawnX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 4095) | ((value & 15) << 12));
        break;
      case 4:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 49407) | (value << 8));
        break;
      case 5:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 64527) | (value << 4));
        break;
      case 6:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 65472) | (value));
        break;
      case 7:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 4095) | ((value & 15) << 12));
        break;
    }
  }

  private static int readPranayOurHqOddSpawnY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(7) & 252) >>> 2;
      case 1:
          return ((rc.readSharedArray(10) & 15) << 2) + ((rc.readSharedArray(11) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(14) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(17) & 4032) >>> 6;
      case 4:
          return (rc.readSharedArray(20) & 252) >>> 2;
      case 5:
          return ((rc.readSharedArray(23) & 15) << 2) + ((rc.readSharedArray(24) & 49152) >>> 14);
      case 6:
          return (rc.readSharedArray(27) & 64512) >>> 10;
      case 7:
          return (rc.readSharedArray(30) & 4032) >>> 6;
      default:
          return -1;
    }
  }

  private static void writePranayOurHqOddSpawnY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 61503) | (value << 6));
        break;
      case 4:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65283) | (value << 2));
        break;
      case 5:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 16383) | ((value & 3) << 14));
        break;
      case 6:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 1023) | (value << 10));
        break;
      case 7:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 61503) | (value << 6));
        break;
    }
  }

  private static int readPranayOurHqEvenSpawnX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(7) & 3) << 4) + ((rc.readSharedArray(8) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(11) & 16128) >>> 8;
      case 2:
          return (rc.readSharedArray(14) & 1008) >>> 4;
      case 3:
          return (rc.readSharedArray(17) & 63);
      case 4:
          return ((rc.readSharedArray(20) & 3) << 4) + ((rc.readSharedArray(21) & 61440) >>> 12);
      case 5:
          return (rc.readSharedArray(24) & 16128) >>> 8;
      case 6:
          return (rc.readSharedArray(27) & 1008) >>> 4;
      case 7:
          return (rc.readSharedArray(30) & 63);
      default:
          return -1;
    }
  }

  private static void writePranayOurHqEvenSpawnX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 49407) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65472) | (value));
        break;
      case 4:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 4095) | ((value & 15) << 12));
        break;
      case 5:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 49407) | (value << 8));
        break;
      case 6:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 64527) | (value << 4));
        break;
      case 7:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 65472) | (value));
        break;
    }
  }

  private static int readPranayOurHqEvenSpawnY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(8) & 4032) >>> 6;
      case 1:
          return (rc.readSharedArray(11) & 252) >>> 2;
      case 2:
          return ((rc.readSharedArray(14) & 15) << 2) + ((rc.readSharedArray(15) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(18) & 64512) >>> 10;
      case 4:
          return (rc.readSharedArray(21) & 4032) >>> 6;
      case 5:
          return (rc.readSharedArray(24) & 252) >>> 2;
      case 6:
          return ((rc.readSharedArray(27) & 15) << 2) + ((rc.readSharedArray(28) & 49152) >>> 14);
      case 7:
          return (rc.readSharedArray(31) & 64512) >>> 10;
      default:
          return -1;
    }
  }

  private static void writePranayOurHqEvenSpawnY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65283) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 1023) | (value << 10));
        break;
      case 4:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 61503) | (value << 6));
        break;
      case 5:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65283) | (value << 2));
        break;
      case 6:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 16383) | ((value & 3) << 14));
        break;
      case 7:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 1023) | (value << 10));
        break;
    }
  }

  public static int readPranayOurHqOddSpawnInstruction(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(8) & 48) >>> 4;
      case 1:
          return (rc.readSharedArray(11) & 3);
      case 2:
          return (rc.readSharedArray(15) & 12288) >>> 12;
      case 3:
          return (rc.readSharedArray(18) & 768) >>> 8;
      case 4:
          return (rc.readSharedArray(21) & 48) >>> 4;
      case 5:
          return (rc.readSharedArray(24) & 3);
      case 6:
          return (rc.readSharedArray(28) & 12288) >>> 12;
      case 7:
          return (rc.readSharedArray(31) & 768) >>> 8;
      default:
          return -1;
    }
  }

  public static void writePranayOurHqOddSpawnInstruction(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65487) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65532) | (value));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 53247) | (value << 12));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 64767) | (value << 8));
        break;
      case 4:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65487) | (value << 4));
        break;
      case 5:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65532) | (value));
        break;
      case 6:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 53247) | (value << 12));
        break;
      case 7:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 64767) | (value << 8));
        break;
    }
  }

  public static int readPranayOurHqEvenSpawnInstruction(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(8) & 12) >>> 2;
      case 1:
          return (rc.readSharedArray(12) & 49152) >>> 14;
      case 2:
          return (rc.readSharedArray(15) & 3072) >>> 10;
      case 3:
          return (rc.readSharedArray(18) & 192) >>> 6;
      case 4:
          return (rc.readSharedArray(21) & 12) >>> 2;
      case 5:
          return (rc.readSharedArray(25) & 49152) >>> 14;
      case 6:
          return (rc.readSharedArray(28) & 3072) >>> 10;
      case 7:
          return (rc.readSharedArray(31) & 192) >>> 6;
      default:
          return -1;
    }
  }

  public static void writePranayOurHqEvenSpawnInstruction(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65523) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 16383) | (value << 14));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 62463) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65343) | (value << 6));
        break;
      case 4:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65523) | (value << 2));
        break;
      case 5:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 16383) | (value << 14));
        break;
      case 6:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 62463) | (value << 10));
        break;
      case 7:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 65343) | (value << 6));
        break;
    }
  }

  private static int readPranayOurHqOddTargetX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(8) & 3) << 4) + ((rc.readSharedArray(9) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(12) & 16128) >>> 8;
      case 2:
          return (rc.readSharedArray(15) & 1008) >>> 4;
      case 3:
          return (rc.readSharedArray(18) & 63);
      case 4:
          return ((rc.readSharedArray(21) & 3) << 4) + ((rc.readSharedArray(22) & 61440) >>> 12);
      case 5:
          return (rc.readSharedArray(25) & 16128) >>> 8;
      case 6:
          return (rc.readSharedArray(28) & 1008) >>> 4;
      case 7:
          return (rc.readSharedArray(31) & 63);
      default:
          return -1;
    }
  }

  private static void writePranayOurHqOddTargetX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 49407) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65472) | (value));
        break;
      case 4:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 4095) | ((value & 15) << 12));
        break;
      case 5:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 49407) | (value << 8));
        break;
      case 6:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 64527) | (value << 4));
        break;
      case 7:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 65472) | (value));
        break;
    }
  }

  private static int readPranayOurHqOddTargetY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(9) & 4032) >>> 6;
      case 1:
          return (rc.readSharedArray(12) & 252) >>> 2;
      case 2:
          return ((rc.readSharedArray(15) & 15) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(19) & 64512) >>> 10;
      case 4:
          return (rc.readSharedArray(22) & 4032) >>> 6;
      case 5:
          return (rc.readSharedArray(25) & 252) >>> 2;
      case 6:
          return ((rc.readSharedArray(28) & 15) << 2) + ((rc.readSharedArray(29) & 49152) >>> 14);
      case 7:
          return (rc.readSharedArray(32) & 64512) >>> 10;
      default:
          return -1;
    }
  }

  private static void writePranayOurHqOddTargetY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65283) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 1023) | (value << 10));
        break;
      case 4:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 61503) | (value << 6));
        break;
      case 5:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 65283) | (value << 2));
        break;
      case 6:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 16383) | ((value & 3) << 14));
        break;
      case 7:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 1023) | (value << 10));
        break;
    }
  }

  private static int readPranayOurHqEvenTargetX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(9) & 63);
      case 1:
          return ((rc.readSharedArray(12) & 3) << 4) + ((rc.readSharedArray(13) & 61440) >>> 12);
      case 2:
          return (rc.readSharedArray(16) & 16128) >>> 8;
      case 3:
          return (rc.readSharedArray(19) & 1008) >>> 4;
      case 4:
          return (rc.readSharedArray(22) & 63);
      case 5:
          return ((rc.readSharedArray(25) & 3) << 4) + ((rc.readSharedArray(26) & 61440) >>> 12);
      case 6:
          return (rc.readSharedArray(29) & 16128) >>> 8;
      case 7:
          return (rc.readSharedArray(32) & 1008) >>> 4;
      default:
          return -1;
    }
  }

  private static void writePranayOurHqEvenTargetX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65472) | (value));
        break;
      case 1:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 4095) | ((value & 15) << 12));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 64527) | (value << 4));
        break;
      case 4:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65472) | (value));
        break;
      case 5:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 4095) | ((value & 15) << 12));
        break;
      case 6:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 49407) | (value << 8));
        break;
      case 7:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 64527) | (value << 4));
        break;
    }
  }

  private static int readPranayOurHqEvenTargetY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(10) & 64512) >>> 10;
      case 1:
          return (rc.readSharedArray(13) & 4032) >>> 6;
      case 2:
          return (rc.readSharedArray(16) & 252) >>> 2;
      case 3:
          return ((rc.readSharedArray(19) & 15) << 2) + ((rc.readSharedArray(20) & 49152) >>> 14);
      case 4:
          return (rc.readSharedArray(23) & 64512) >>> 10;
      case 5:
          return (rc.readSharedArray(26) & 4032) >>> 6;
      case 6:
          return (rc.readSharedArray(29) & 252) >>> 2;
      case 7:
          return ((rc.readSharedArray(32) & 15) << 2) + ((rc.readSharedArray(33) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  private static void writePranayOurHqEvenTargetY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 61503) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65283) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 16383) | ((value & 3) << 14));
        break;
      case 4:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 1023) | (value << 10));
        break;
      case 5:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 61503) | (value << 6));
        break;
      case 6:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65283) | (value << 2));
        break;
      case 7:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 16383) | ((value & 3) << 14));
        break;
    }
  }

  public static MapLocation readPranayOurHqOddSpawnLocation(int idx) throws GameActionException {
    return new MapLocation(readPranayOurHqOddSpawnX(idx)-1,readPranayOurHqOddSpawnY(idx)-1);
  }
  public static boolean readPranayOurHqOddSpawnExists(int idx) throws GameActionException {
    return !((readPranayOurHqOddSpawnLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writePranayOurHqOddSpawnLocation(int idx, MapLocation value) throws GameActionException {
    writePranayOurHqOddSpawnX(idx, (value).x+1);
    writePranayOurHqOddSpawnY(idx, (value).y+1);
  }
  public static MapLocation readPranayOurHqEvenSpawnLocation(int idx) throws GameActionException {
    return new MapLocation(readPranayOurHqEvenSpawnX(idx)-1,readPranayOurHqEvenSpawnY(idx)-1);
  }
  public static boolean readPranayOurHqEvenSpawnExists(int idx) throws GameActionException {
    return !((readPranayOurHqEvenSpawnLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writePranayOurHqEvenSpawnLocation(int idx, MapLocation value) throws GameActionException {
    writePranayOurHqEvenSpawnX(idx, (value).x+1);
    writePranayOurHqEvenSpawnY(idx, (value).y+1);
  }
  public static MapLocation readPranayOurHqOddTargetLocation(int idx) throws GameActionException {
    return new MapLocation(readPranayOurHqOddTargetX(idx)-1,readPranayOurHqOddTargetY(idx)-1);
  }
  public static boolean readPranayOurHqOddTargetExists(int idx) throws GameActionException {
    return !((readPranayOurHqOddTargetLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writePranayOurHqOddTargetLocation(int idx, MapLocation value) throws GameActionException {
    writePranayOurHqOddTargetX(idx, (value).x+1);
    writePranayOurHqOddTargetY(idx, (value).y+1);
  }
  public static MapLocation readPranayOurHqEvenTargetLocation(int idx) throws GameActionException {
    return new MapLocation(readPranayOurHqEvenTargetX(idx)-1,readPranayOurHqEvenTargetY(idx)-1);
  }
  public static boolean readPranayOurHqEvenTargetExists(int idx) throws GameActionException {
    return !((readPranayOurHqEvenTargetLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writePranayOurHqEvenTargetLocation(int idx, MapLocation value) throws GameActionException {
    writePranayOurHqEvenTargetX(idx, (value).x+1);
    writePranayOurHqEvenTargetY(idx, (value).y+1);
  }
  private static int readPranayWellInfoX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(33) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(34) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(35) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(36) & 2016) >>> 5;
      default:
          return -1;
    }
  }

  private static void writePranayWellInfoX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 63519) | (value << 5));
        break;
    }
  }

  private static int readPranayWellInfoY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(33) & 252) >>> 2;
      case 1:
          return (rc.readSharedArray(34) & 126) >>> 1;
      case 2:
          return (rc.readSharedArray(35) & 63);
      case 3:
          return ((rc.readSharedArray(36) & 31) << 1) + ((rc.readSharedArray(37) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  private static void writePranayWellInfoY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  public static int readPranayWellInfoType(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(33) & 3);
      case 1:
          return ((rc.readSharedArray(34) & 1) << 1) + ((rc.readSharedArray(35) & 32768) >>> 15);
      case 2:
          return (rc.readSharedArray(36) & 49152) >>> 14;
      case 3:
          return (rc.readSharedArray(37) & 24576) >>> 13;
      default:
          return -1;
    }
  }

  public static void writePranayWellInfoType(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 65532) | (value));
        break;
      case 1:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65534) | ((value & 2) >>> 1));
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 32767) | ((value & 1) << 15));
        break;
      case 2:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 16383) | (value << 14));
        break;
      case 3:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 40959) | (value << 13));
        break;
    }
  }

  public static int readPranayWellInfoNumMiners(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(34) & 57344) >>> 13;
      case 1:
          return (rc.readSharedArray(35) & 28672) >>> 12;
      case 2:
          return (rc.readSharedArray(36) & 14336) >>> 11;
      case 3:
          return (rc.readSharedArray(37) & 7168) >>> 10;
      default:
          return -1;
    }
  }

  public static void writePranayWellInfoNumMiners(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 8191) | (value << 13));
        break;
      case 1:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 36863) | (value << 12));
        break;
      case 2:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 51199) | (value << 11));
        break;
      case 3:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 58367) | (value << 10));
        break;
    }
  }

  public static MapLocation readPranayWellInfoLocation(int idx) throws GameActionException {
    return new MapLocation(readPranayWellInfoX(idx)-1,readPranayWellInfoY(idx)-1);
  }
  public static boolean readPranayWellInfoExists(int idx) throws GameActionException {
    return !((readPranayWellInfoLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writePranayWellInfoLocation(int idx, MapLocation value) throws GameActionException {
    writePranayWellInfoX(idx, (value).x+1);
    writePranayWellInfoY(idx, (value).y+1);
  }
  private static int readAdamantiumWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(37) & 1008) >>> 4;
      case 1:
          return (rc.readSharedArray(38) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(39) & 64512) >>> 10;
      case 3:
          return ((rc.readSharedArray(39) & 7) << 3) + ((rc.readSharedArray(40) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  private static int readAdamantiumWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(37) & 15) << 2) + ((rc.readSharedArray(38) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(38) & 126) >>> 1;
      case 2:
          return (rc.readSharedArray(39) & 1008) >>> 4;
      case 3:
          return (rc.readSharedArray(40) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 57471) | (value << 7));
        break;
    }
  }

  private static int readAdamantiumWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(38) & 8192) >>> 13;
      case 1:
          return (rc.readSharedArray(38) & 1);
      case 2:
          return (rc.readSharedArray(39) & 8) >>> 3;
      case 3:
          return (rc.readSharedArray(40) & 64) >>> 6;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 57343) | (value << 13));
        break;
      case 1:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 65534) | (value));
        break;
      case 2:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 65527) | (value << 3));
        break;
      case 3:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 65471) | (value << 6));
        break;
    }
  }

  public static MapLocation readAdamantiumWellLocation(int idx) throws GameActionException {
    return new MapLocation(readAdamantiumWellX(idx)-1,readAdamantiumWellY(idx)-1);
  }
  public static boolean readAdamantiumWellExists(int idx) throws GameActionException {
    return !((readAdamantiumWellLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeAdamantiumWellLocation(int idx, MapLocation value) throws GameActionException {
    writeAdamantiumWellX(idx, (value).x+1);
    writeAdamantiumWellY(idx, (value).y+1);
  }
  public static boolean readAdamantiumWellUpgraded(int idx) throws GameActionException {
    return ((readAdamantiumWellUpgradedBit(idx)) == 1);
  }
  public static void writeAdamantiumWellUpgraded(int idx, boolean value) throws GameActionException {
    writeAdamantiumWellUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private static int readManaWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(40) & 63);
      case 1:
          return (rc.readSharedArray(41) & 504) >>> 3;
      case 2:
          return (rc.readSharedArray(42) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(43) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private static void writeManaWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 65472) | (value));
        break;
      case 1:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 33279) | (value << 9));
        break;
    }
  }

  private static int readManaWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(41) & 64512) >>> 10;
      case 1:
          return ((rc.readSharedArray(41) & 7) << 3) + ((rc.readSharedArray(42) & 57344) >>> 13);
      case 2:
          return (rc.readSharedArray(42) & 63);
      case 3:
          return (rc.readSharedArray(43) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private static void writeManaWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 8191) | ((value & 7) << 13));
        break;
      case 2:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65031) | (value << 3));
        break;
    }
  }

  private static int readManaWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(41) & 512) >>> 9;
      case 1:
          return (rc.readSharedArray(42) & 4096) >>> 12;
      case 2:
          return (rc.readSharedArray(43) & 32768) >>> 15;
      case 3:
          return (rc.readSharedArray(43) & 4) >>> 2;
      default:
          return -1;
    }
  }

  private static void writeManaWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65023) | (value << 9));
        break;
      case 1:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 61439) | (value << 12));
        break;
      case 2:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 32767) | (value << 15));
        break;
      case 3:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65531) | (value << 2));
        break;
    }
  }

  public static MapLocation readManaWellLocation(int idx) throws GameActionException {
    return new MapLocation(readManaWellX(idx)-1,readManaWellY(idx)-1);
  }
  public static boolean readManaWellExists(int idx) throws GameActionException {
    return !((readManaWellLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeManaWellLocation(int idx, MapLocation value) throws GameActionException {
    writeManaWellX(idx, (value).x+1);
    writeManaWellY(idx, (value).y+1);
  }
  public static boolean readManaWellUpgraded(int idx) throws GameActionException {
    return ((readManaWellUpgradedBit(idx)) == 1);
  }
  public static void writeManaWellUpgraded(int idx, boolean value) throws GameActionException {
    writeManaWellUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private static int readElixirWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(43) & 3) << 4) + ((rc.readSharedArray(44) & 61440) >>> 12);
      case 1:
          return ((rc.readSharedArray(44) & 31) << 1) + ((rc.readSharedArray(45) & 32768) >>> 15);
      case 2:
          return (rc.readSharedArray(45) & 252) >>> 2;
      case 3:
          return (rc.readSharedArray(46) & 2016) >>> 5;
      default:
          return -1;
    }
  }

  private static void writeElixirWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 32767) | ((value & 1) << 15));
        break;
      case 2:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65283) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 63519) | (value << 5));
        break;
    }
  }

  private static int readElixirWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(44) & 4032) >>> 6;
      case 1:
          return (rc.readSharedArray(45) & 32256) >>> 9;
      case 2:
          return ((rc.readSharedArray(45) & 3) << 4) + ((rc.readSharedArray(46) & 61440) >>> 12);
      case 3:
          return ((rc.readSharedArray(46) & 31) << 1) + ((rc.readSharedArray(47) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  private static void writeElixirWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 33279) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  private static int readElixirWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(44) & 32) >>> 5;
      case 1:
          return (rc.readSharedArray(45) & 256) >>> 8;
      case 2:
          return (rc.readSharedArray(46) & 2048) >>> 11;
      case 3:
          return (rc.readSharedArray(47) & 16384) >>> 14;
      default:
          return -1;
    }
  }

  private static void writeElixirWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 65503) | (value << 5));
        break;
      case 1:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65279) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 63487) | (value << 11));
        break;
      case 3:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 49151) | (value << 14));
        break;
    }
  }

  public static MapLocation readElixirWellLocation(int idx) throws GameActionException {
    return new MapLocation(readElixirWellX(idx)-1,readElixirWellY(idx)-1);
  }
  public static boolean readElixirWellExists(int idx) throws GameActionException {
    return !((readElixirWellLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeElixirWellLocation(int idx, MapLocation value) throws GameActionException {
    writeElixirWellX(idx, (value).x+1);
    writeElixirWellY(idx, (value).y+1);
  }
  public static boolean readElixirWellUpgraded(int idx) throws GameActionException {
    return ((readElixirWellUpgradedBit(idx)) == 1);
  }
  public static void writeElixirWellUpgraded(int idx, boolean value) throws GameActionException {
    writeElixirWellUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private static int readEnemyOddX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(47) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(48) & 63);
      case 2:
          return (rc.readSharedArray(50) & 16128) >>> 8;
      default:
          return -1;
    }
  }

  private static void writeEnemyOddX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 65472) | (value));
        break;
      case 2:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 49407) | (value << 8));
        break;
    }
  }

  private static int readEnemyOddY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(47) & 252) >>> 2;
      case 1:
          return (rc.readSharedArray(49) & 64512) >>> 10;
      case 2:
          return (rc.readSharedArray(50) & 252) >>> 2;
      default:
          return -1;
    }
  }

  private static void writeEnemyOddY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 1023) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 65283) | (value << 2));
        break;
    }
  }

  private static int readEnemyEvenX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(47) & 3) << 4) + ((rc.readSharedArray(48) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(49) & 1008) >>> 4;
      case 2:
          return ((rc.readSharedArray(50) & 3) << 4) + ((rc.readSharedArray(51) & 61440) >>> 12);
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 4095) | ((value & 15) << 12));
        break;
    }
  }

  private static int readEnemyEvenY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(48) & 4032) >>> 6;
      case 1:
          return ((rc.readSharedArray(49) & 15) << 2) + ((rc.readSharedArray(50) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(51) & 4032) >>> 6;
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 61503) | (value << 6));
        break;
    }
  }

  public static MapLocation readEnemyOddLocation(int idx) throws GameActionException {
    return new MapLocation(readEnemyOddX(idx)-1,readEnemyOddY(idx)-1);
  }
  public static boolean readEnemyOddExists(int idx) throws GameActionException {
    return !((readEnemyOddLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeEnemyOddLocation(int idx, MapLocation value) throws GameActionException {
    writeEnemyOddX(idx, (value).x+1);
    writeEnemyOddY(idx, (value).y+1);
  }
  public static MapLocation readEnemyEvenLocation(int idx) throws GameActionException {
    return new MapLocation(readEnemyEvenX(idx)-1,readEnemyEvenY(idx)-1);
  }
  public static boolean readEnemyEvenExists(int idx) throws GameActionException {
    return !((readEnemyEvenLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeEnemyEvenLocation(int idx, MapLocation value) throws GameActionException {
    writeEnemyEvenX(idx, (value).x+1);
    writeEnemyEvenY(idx, (value).y+1);
  }


  public enum ResourceTypeReaderWriter {
    INVALID(ResourceType.NO_RESOURCE),
    ADAMANTIUM(ResourceType.ADAMANTIUM),
    MANA(ResourceType.MANA),
    ELIXIR(ResourceType.ELIXIR);

    public final ResourceType type;

    static final ResourceTypeReaderWriter[] values = values();

    ResourceTypeReaderWriter(ResourceType type) {
      this.type = type;
    }

    public static ResourceTypeReaderWriter fromResourceType(ResourceType type) {
     return values[type.ordinal()];
    }


    public MapLocation readWellLocation(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return CommsHandler.readAdamantiumWellLocation(idx);
        case MANA:
          return CommsHandler.readManaWellLocation(idx);
        case ELIXIR:
          return CommsHandler.readElixirWellLocation(idx);
        default:
          throw new RuntimeException("readWellLocation not defined for " + this);
      }
    }

    public void writeWellLocation(int idx, MapLocation value) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellLocation(idx, value);
          break;
        case MANA:
          CommsHandler.writeManaWellLocation(idx, value);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellLocation(idx, value);
          break;
        default:
          throw new RuntimeException("writeWellLocation not defined for " + this);
      }
    }

    public boolean readWellExists(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return CommsHandler.readAdamantiumWellExists(idx);
        case MANA:
          return CommsHandler.readManaWellExists(idx);
        case ELIXIR:
          return CommsHandler.readElixirWellExists(idx);
        default:
          throw new RuntimeException("readWellExists not defined for " + this);
      }
    }

    public boolean readWellUpgraded(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return CommsHandler.readAdamantiumWellUpgraded(idx);
        case MANA:
          return CommsHandler.readManaWellUpgraded(idx);
        case ELIXIR:
          return CommsHandler.readElixirWellUpgraded(idx);
        default:
          throw new RuntimeException("readWellUpgraded not defined for " + this);
      }
    }

    public void writeWellUpgraded(int idx, boolean value) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellUpgraded(idx, value);
          break;
        case MANA:
          CommsHandler.writeManaWellUpgraded(idx, value);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellUpgraded(idx, value);
          break;
        default:
          throw new RuntimeException("writeWellUpgraded not defined for " + this);
      }
    }

  }
}