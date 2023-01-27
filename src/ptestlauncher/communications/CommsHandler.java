package ptestlauncher.communications;

import ptestlauncher.utils.Global;
import ptestlauncher.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int ADAMANTIUM_WELL_SLOTS = 4;
  public static final int MANA_WELL_SLOTS = 4;
  public static final int ELIXIR_WELL_SLOTS = 4;
  public static final int ENEMY_SLOTS = 26;
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
          return ((rc.readSharedArray(3) & 7) << 3) + ((rc.readSharedArray(4) & 57344) >>> 13);
      case 2:
          return (rc.readSharedArray(7) & 4032) >>> 6;
      case 3:
          return ((rc.readSharedArray(10) & 31) << 1) + ((rc.readSharedArray(11) & 32768) >>> 15);
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
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 8191) | ((value & 7) << 13));
        break;
      case 2:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  private static int readOurHqY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(0) & 15) << 2) + ((rc.readSharedArray(1) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(4) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(7) & 63);
      case 3:
          return (rc.readSharedArray(11) & 32256) >>> 9;
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
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 33279) | (value << 9));
        break;
    }
  }

  private static int readOurHqOddSpawnX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(4) & 126) >>> 1;
      case 2:
          return (rc.readSharedArray(8) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(11) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private static void writeOurHqOddSpawnX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65031) | (value << 3));
        break;
    }
  }

  private static int readOurHqOddSpawnY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 252) >>> 2;
      case 1:
          return ((rc.readSharedArray(4) & 1) << 5) + ((rc.readSharedArray(5) & 63488) >>> 11);
      case 2:
          return (rc.readSharedArray(8) & 1008) >>> 4;
      case 3:
          return ((rc.readSharedArray(11) & 7) << 3) + ((rc.readSharedArray(12) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private static void writeOurHqOddSpawnY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 2047) | ((value & 31) << 11));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  private static int readOurHqEvenSpawnX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(1) & 3) << 4) + ((rc.readSharedArray(2) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(5) & 2016) >>> 5;
      case 2:
          return ((rc.readSharedArray(8) & 15) << 2) + ((rc.readSharedArray(9) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(12) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private static void writeOurHqEvenSpawnX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 63519) | (value << 5));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 57471) | (value << 7));
        break;
    }
  }

  private static int readOurHqEvenSpawnY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 4032) >>> 6;
      case 1:
          return ((rc.readSharedArray(5) & 31) << 1) + ((rc.readSharedArray(6) & 32768) >>> 15);
      case 2:
          return (rc.readSharedArray(9) & 16128) >>> 8;
      case 3:
          return (rc.readSharedArray(12) & 126) >>> 1;
      default:
          return -1;
    }
  }

  private static void writeOurHqEvenSpawnY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 32767) | ((value & 1) << 15));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65409) | (value << 1));
        break;
    }
  }

  public static int readOurHqOddSpawnInstruction(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 48) >>> 4;
      case 1:
          return (rc.readSharedArray(6) & 24576) >>> 13;
      case 2:
          return (rc.readSharedArray(9) & 192) >>> 6;
      case 3:
          return ((rc.readSharedArray(12) & 1) << 1) + ((rc.readSharedArray(13) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  public static void writeOurHqOddSpawnInstruction(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65487) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 40959) | (value << 13));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65343) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65534) | ((value & 2) >>> 1));
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  public static int readOurHqEvenSpawnInstruction(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 12) >>> 2;
      case 1:
          return (rc.readSharedArray(6) & 6144) >>> 11;
      case 2:
          return (rc.readSharedArray(9) & 48) >>> 4;
      case 3:
          return (rc.readSharedArray(13) & 24576) >>> 13;
      default:
          return -1;
    }
  }

  public static void writeOurHqEvenSpawnInstruction(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65523) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 59391) | (value << 11));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65487) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 40959) | (value << 13));
        break;
    }
  }

  public static int readOurHqAdamantiumIncome(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(2) & 3) << 3) + ((rc.readSharedArray(3) & 57344) >>> 13);
      case 1:
          return (rc.readSharedArray(6) & 1984) >>> 6;
      case 2:
          return ((rc.readSharedArray(9) & 15) << 1) + ((rc.readSharedArray(10) & 32768) >>> 15);
      case 3:
          return (rc.readSharedArray(13) & 7936) >>> 8;
      default:
          return -1;
    }
  }

  public static void writeOurHqAdamantiumIncome(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65532) | ((value & 24) >>> 3));
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 8191) | ((value & 7) << 13));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 63551) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65520) | ((value & 30) >>> 1));
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 32767) | ((value & 1) << 15));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 57599) | (value << 8));
        break;
    }
  }

  public static int readOurHqManaIncome(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 7936) >>> 8;
      case 1:
          return (rc.readSharedArray(6) & 62) >>> 1;
      case 2:
          return (rc.readSharedArray(10) & 31744) >>> 10;
      case 3:
          return (rc.readSharedArray(13) & 248) >>> 3;
      default:
          return -1;
    }
  }

  public static void writeOurHqManaIncome(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 57599) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65473) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 33791) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65287) | (value << 3));
        break;
    }
  }

  public static int readOurHqElixirIncome(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 248) >>> 3;
      case 1:
          return ((rc.readSharedArray(6) & 1) << 4) + ((rc.readSharedArray(7) & 61440) >>> 12);
      case 2:
          return (rc.readSharedArray(10) & 992) >>> 5;
      case 3:
          return ((rc.readSharedArray(13) & 7) << 2) + ((rc.readSharedArray(14) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  public static void writeOurHqElixirIncome(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65287) | (value << 3));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65534) | ((value & 16) >>> 4));
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 4095) | ((value & 15) << 12));
        break;
      case 2:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 64543) | (value << 5));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65528) | ((value & 28) >>> 2));
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 16383) | ((value & 3) << 14));
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
  public static MapLocation readOurHqOddSpawnLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqOddSpawnX(idx)-1,readOurHqOddSpawnY(idx)-1);
  }
  public static boolean readOurHqOddSpawnExists(int idx) throws GameActionException {
    return !((readOurHqOddSpawnLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeOurHqOddSpawnLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqOddSpawnX(idx, (value).x+1);
    writeOurHqOddSpawnY(idx, (value).y+1);
  }
  public static MapLocation readOurHqEvenSpawnLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqEvenSpawnX(idx)-1,readOurHqEvenSpawnY(idx)-1);
  }
  public static boolean readOurHqEvenSpawnExists(int idx) throws GameActionException {
    return !((readOurHqEvenSpawnLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeOurHqEvenSpawnLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqEvenSpawnX(idx, (value).x+1);
    writeOurHqEvenSpawnY(idx, (value).y+1);
  }
  private static int readAdamantiumWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(14) & 16128) >>> 8;
      case 1:
          return ((rc.readSharedArray(14) & 1) << 5) + ((rc.readSharedArray(15) & 63488) >>> 11);
      case 2:
          return ((rc.readSharedArray(15) & 15) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(16) & 126) >>> 1;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 2047) | ((value & 31) << 11));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65409) | (value << 1));
        break;
    }
  }

  private static int readAdamantiumWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(14) & 252) >>> 2;
      case 1:
          return (rc.readSharedArray(15) & 2016) >>> 5;
      case 2:
          return (rc.readSharedArray(16) & 16128) >>> 8;
      case 3:
          return ((rc.readSharedArray(16) & 1) << 5) + ((rc.readSharedArray(17) & 63488) >>> 11);
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 63519) | (value << 5));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 2047) | ((value & 31) << 11));
        break;
    }
  }

  private static int readAdamantiumWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(14) & 2) >>> 1;
      case 1:
          return (rc.readSharedArray(15) & 16) >>> 4;
      case 2:
          return (rc.readSharedArray(16) & 128) >>> 7;
      case 3:
          return (rc.readSharedArray(17) & 1024) >>> 10;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65533) | (value << 1));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65519) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65407) | (value << 7));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 64511) | (value << 10));
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
          return (rc.readSharedArray(17) & 1008) >>> 4;
      case 1:
          return (rc.readSharedArray(18) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(19) & 64512) >>> 10;
      case 3:
          return ((rc.readSharedArray(19) & 7) << 3) + ((rc.readSharedArray(20) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private static void writeManaWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  private static int readManaWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(17) & 15) << 2) + ((rc.readSharedArray(18) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(18) & 126) >>> 1;
      case 2:
          return (rc.readSharedArray(19) & 1008) >>> 4;
      case 3:
          return (rc.readSharedArray(20) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private static void writeManaWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 57471) | (value << 7));
        break;
    }
  }

  private static int readManaWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(18) & 8192) >>> 13;
      case 1:
          return (rc.readSharedArray(18) & 1);
      case 2:
          return (rc.readSharedArray(19) & 8) >>> 3;
      case 3:
          return (rc.readSharedArray(20) & 64) >>> 6;
      default:
          return -1;
    }
  }

  private static void writeManaWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 57343) | (value << 13));
        break;
      case 1:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65534) | (value));
        break;
      case 2:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65527) | (value << 3));
        break;
      case 3:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65471) | (value << 6));
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
          return (rc.readSharedArray(20) & 63);
      case 1:
          return (rc.readSharedArray(21) & 504) >>> 3;
      case 2:
          return (rc.readSharedArray(22) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(23) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private static void writeElixirWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65472) | (value));
        break;
      case 1:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 33279) | (value << 9));
        break;
    }
  }

  private static int readElixirWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(21) & 64512) >>> 10;
      case 1:
          return ((rc.readSharedArray(21) & 7) << 3) + ((rc.readSharedArray(22) & 57344) >>> 13);
      case 2:
          return (rc.readSharedArray(22) & 63);
      case 3:
          return (rc.readSharedArray(23) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private static void writeElixirWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 8191) | ((value & 7) << 13));
        break;
      case 2:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65031) | (value << 3));
        break;
    }
  }

  private static int readElixirWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(21) & 512) >>> 9;
      case 1:
          return (rc.readSharedArray(22) & 4096) >>> 12;
      case 2:
          return (rc.readSharedArray(23) & 32768) >>> 15;
      case 3:
          return (rc.readSharedArray(23) & 4) >>> 2;
      default:
          return -1;
    }
  }

  private static void writeElixirWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65023) | (value << 9));
        break;
      case 1:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 61439) | (value << 12));
        break;
      case 2:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 32767) | (value << 15));
        break;
      case 3:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65531) | (value << 2));
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
          return ((rc.readSharedArray(23) & 3) << 4) + ((rc.readSharedArray(24) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(25) & 1008) >>> 4;
      case 2:
          return ((rc.readSharedArray(26) & 3) << 4) + ((rc.readSharedArray(27) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(28) & 1008) >>> 4;
      case 4:
          return ((rc.readSharedArray(29) & 3) << 4) + ((rc.readSharedArray(30) & 61440) >>> 12);
      case 5:
          return (rc.readSharedArray(31) & 1008) >>> 4;
      case 6:
          return ((rc.readSharedArray(32) & 3) << 4) + ((rc.readSharedArray(33) & 61440) >>> 12);
      case 7:
          return (rc.readSharedArray(34) & 1008) >>> 4;
      case 8:
          return ((rc.readSharedArray(35) & 3) << 4) + ((rc.readSharedArray(36) & 61440) >>> 12);
      case 9:
          return (rc.readSharedArray(37) & 1008) >>> 4;
      case 10:
          return ((rc.readSharedArray(38) & 3) << 4) + ((rc.readSharedArray(39) & 61440) >>> 12);
      case 11:
          return (rc.readSharedArray(40) & 1008) >>> 4;
      case 12:
          return ((rc.readSharedArray(41) & 3) << 4) + ((rc.readSharedArray(42) & 61440) >>> 12);
      case 13:
          return (rc.readSharedArray(43) & 1008) >>> 4;
      case 14:
          return ((rc.readSharedArray(44) & 3) << 4) + ((rc.readSharedArray(45) & 61440) >>> 12);
      case 15:
          return (rc.readSharedArray(46) & 1008) >>> 4;
      case 16:
          return ((rc.readSharedArray(47) & 3) << 4) + ((rc.readSharedArray(48) & 61440) >>> 12);
      case 17:
          return (rc.readSharedArray(49) & 1008) >>> 4;
      case 18:
          return ((rc.readSharedArray(50) & 3) << 4) + ((rc.readSharedArray(51) & 61440) >>> 12);
      case 19:
          return (rc.readSharedArray(52) & 1008) >>> 4;
      case 20:
          return ((rc.readSharedArray(53) & 3) << 4) + ((rc.readSharedArray(54) & 61440) >>> 12);
      case 21:
          return (rc.readSharedArray(55) & 1008) >>> 4;
      case 22:
          return ((rc.readSharedArray(56) & 3) << 4) + ((rc.readSharedArray(57) & 61440) >>> 12);
      case 23:
          return (rc.readSharedArray(58) & 1008) >>> 4;
      case 24:
          return ((rc.readSharedArray(59) & 3) << 4) + ((rc.readSharedArray(60) & 61440) >>> 12);
      case 25:
          return (rc.readSharedArray(61) & 1008) >>> 4;
      default:
          return -1;
    }
  }

  private static void writeEnemyOddX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 64527) | (value << 4));
        break;
      case 4:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 4095) | ((value & 15) << 12));
        break;
      case 5:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 64527) | (value << 4));
        break;
      case 6:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 4095) | ((value & 15) << 12));
        break;
      case 7:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 64527) | (value << 4));
        break;
      case 8:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 4095) | ((value & 15) << 12));
        break;
      case 9:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 64527) | (value << 4));
        break;
      case 10:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 4095) | ((value & 15) << 12));
        break;
      case 11:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 64527) | (value << 4));
        break;
      case 12:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 4095) | ((value & 15) << 12));
        break;
      case 13:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 64527) | (value << 4));
        break;
      case 14:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 4095) | ((value & 15) << 12));
        break;
      case 15:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 64527) | (value << 4));
        break;
      case 16:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 4095) | ((value & 15) << 12));
        break;
      case 17:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 64527) | (value << 4));
        break;
      case 18:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 4095) | ((value & 15) << 12));
        break;
      case 19:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 64527) | (value << 4));
        break;
      case 20:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 4095) | ((value & 15) << 12));
        break;
      case 21:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 64527) | (value << 4));
        break;
      case 22:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 4095) | ((value & 15) << 12));
        break;
      case 23:
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 64527) | (value << 4));
        break;
      case 24:
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 4095) | ((value & 15) << 12));
        break;
      case 25:
        rc.writeSharedArray(61, (rc.readSharedArray(61) & 64527) | (value << 4));
        break;
    }
  }

  private static int readEnemyOddY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(24) & 4032) >>> 6;
      case 1:
          return ((rc.readSharedArray(25) & 15) << 2) + ((rc.readSharedArray(26) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(27) & 4032) >>> 6;
      case 3:
          return ((rc.readSharedArray(28) & 15) << 2) + ((rc.readSharedArray(29) & 49152) >>> 14);
      case 4:
          return (rc.readSharedArray(30) & 4032) >>> 6;
      case 5:
          return ((rc.readSharedArray(31) & 15) << 2) + ((rc.readSharedArray(32) & 49152) >>> 14);
      case 6:
          return (rc.readSharedArray(33) & 4032) >>> 6;
      case 7:
          return ((rc.readSharedArray(34) & 15) << 2) + ((rc.readSharedArray(35) & 49152) >>> 14);
      case 8:
          return (rc.readSharedArray(36) & 4032) >>> 6;
      case 9:
          return ((rc.readSharedArray(37) & 15) << 2) + ((rc.readSharedArray(38) & 49152) >>> 14);
      case 10:
          return (rc.readSharedArray(39) & 4032) >>> 6;
      case 11:
          return ((rc.readSharedArray(40) & 15) << 2) + ((rc.readSharedArray(41) & 49152) >>> 14);
      case 12:
          return (rc.readSharedArray(42) & 4032) >>> 6;
      case 13:
          return ((rc.readSharedArray(43) & 15) << 2) + ((rc.readSharedArray(44) & 49152) >>> 14);
      case 14:
          return (rc.readSharedArray(45) & 4032) >>> 6;
      case 15:
          return ((rc.readSharedArray(46) & 15) << 2) + ((rc.readSharedArray(47) & 49152) >>> 14);
      case 16:
          return (rc.readSharedArray(48) & 4032) >>> 6;
      case 17:
          return ((rc.readSharedArray(49) & 15) << 2) + ((rc.readSharedArray(50) & 49152) >>> 14);
      case 18:
          return (rc.readSharedArray(51) & 4032) >>> 6;
      case 19:
          return ((rc.readSharedArray(52) & 15) << 2) + ((rc.readSharedArray(53) & 49152) >>> 14);
      case 20:
          return (rc.readSharedArray(54) & 4032) >>> 6;
      case 21:
          return ((rc.readSharedArray(55) & 15) << 2) + ((rc.readSharedArray(56) & 49152) >>> 14);
      case 22:
          return (rc.readSharedArray(57) & 4032) >>> 6;
      case 23:
          return ((rc.readSharedArray(58) & 15) << 2) + ((rc.readSharedArray(59) & 49152) >>> 14);
      case 24:
          return (rc.readSharedArray(60) & 4032) >>> 6;
      case 25:
          return ((rc.readSharedArray(61) & 15) << 2) + ((rc.readSharedArray(62) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  private static void writeEnemyOddY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 16383) | ((value & 3) << 14));
        break;
      case 4:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 61503) | (value << 6));
        break;
      case 5:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 16383) | ((value & 3) << 14));
        break;
      case 6:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 61503) | (value << 6));
        break;
      case 7:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 16383) | ((value & 3) << 14));
        break;
      case 8:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 61503) | (value << 6));
        break;
      case 9:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 16383) | ((value & 3) << 14));
        break;
      case 10:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 61503) | (value << 6));
        break;
      case 11:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 16383) | ((value & 3) << 14));
        break;
      case 12:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 61503) | (value << 6));
        break;
      case 13:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 16383) | ((value & 3) << 14));
        break;
      case 14:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 61503) | (value << 6));
        break;
      case 15:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 16383) | ((value & 3) << 14));
        break;
      case 16:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 61503) | (value << 6));
        break;
      case 17:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 16383) | ((value & 3) << 14));
        break;
      case 18:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 61503) | (value << 6));
        break;
      case 19:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 16383) | ((value & 3) << 14));
        break;
      case 20:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 61503) | (value << 6));
        break;
      case 21:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 16383) | ((value & 3) << 14));
        break;
      case 22:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 61503) | (value << 6));
        break;
      case 23:
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 16383) | ((value & 3) << 14));
        break;
      case 24:
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 61503) | (value << 6));
        break;
      case 25:
        rc.writeSharedArray(61, (rc.readSharedArray(61) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(62, (rc.readSharedArray(62) & 16383) | ((value & 3) << 14));
        break;
    }
  }

  private static int readEnemyEvenX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(24) & 63);
      case 1:
          return (rc.readSharedArray(26) & 16128) >>> 8;
      case 2:
          return (rc.readSharedArray(27) & 63);
      case 3:
          return (rc.readSharedArray(29) & 16128) >>> 8;
      case 4:
          return (rc.readSharedArray(30) & 63);
      case 5:
          return (rc.readSharedArray(32) & 16128) >>> 8;
      case 6:
          return (rc.readSharedArray(33) & 63);
      case 7:
          return (rc.readSharedArray(35) & 16128) >>> 8;
      case 8:
          return (rc.readSharedArray(36) & 63);
      case 9:
          return (rc.readSharedArray(38) & 16128) >>> 8;
      case 10:
          return (rc.readSharedArray(39) & 63);
      case 11:
          return (rc.readSharedArray(41) & 16128) >>> 8;
      case 12:
          return (rc.readSharedArray(42) & 63);
      case 13:
          return (rc.readSharedArray(44) & 16128) >>> 8;
      case 14:
          return (rc.readSharedArray(45) & 63);
      case 15:
          return (rc.readSharedArray(47) & 16128) >>> 8;
      case 16:
          return (rc.readSharedArray(48) & 63);
      case 17:
          return (rc.readSharedArray(50) & 16128) >>> 8;
      case 18:
          return (rc.readSharedArray(51) & 63);
      case 19:
          return (rc.readSharedArray(53) & 16128) >>> 8;
      case 20:
          return (rc.readSharedArray(54) & 63);
      case 21:
          return (rc.readSharedArray(56) & 16128) >>> 8;
      case 22:
          return (rc.readSharedArray(57) & 63);
      case 23:
          return (rc.readSharedArray(59) & 16128) >>> 8;
      case 24:
          return (rc.readSharedArray(60) & 63);
      case 25:
          return (rc.readSharedArray(62) & 16128) >>> 8;
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65472) | (value));
        break;
      case 1:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 49407) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 49407) | (value << 8));
        break;
      case 4:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 65472) | (value));
        break;
      case 5:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 49407) | (value << 8));
        break;
      case 6:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 65472) | (value));
        break;
      case 7:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 49407) | (value << 8));
        break;
      case 8:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 65472) | (value));
        break;
      case 9:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 49407) | (value << 8));
        break;
      case 10:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 65472) | (value));
        break;
      case 11:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 49407) | (value << 8));
        break;
      case 12:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 65472) | (value));
        break;
      case 13:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 49407) | (value << 8));
        break;
      case 14:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65472) | (value));
        break;
      case 15:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 49407) | (value << 8));
        break;
      case 16:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 65472) | (value));
        break;
      case 17:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 49407) | (value << 8));
        break;
      case 18:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 65472) | (value));
        break;
      case 19:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 49407) | (value << 8));
        break;
      case 20:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 65472) | (value));
        break;
      case 21:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 49407) | (value << 8));
        break;
      case 22:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 65472) | (value));
        break;
      case 23:
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 49407) | (value << 8));
        break;
      case 24:
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 65472) | (value));
        break;
      case 25:
        rc.writeSharedArray(62, (rc.readSharedArray(62) & 49407) | (value << 8));
        break;
    }
  }

  private static int readEnemyEvenY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(25) & 64512) >>> 10;
      case 1:
          return (rc.readSharedArray(26) & 252) >>> 2;
      case 2:
          return (rc.readSharedArray(28) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(29) & 252) >>> 2;
      case 4:
          return (rc.readSharedArray(31) & 64512) >>> 10;
      case 5:
          return (rc.readSharedArray(32) & 252) >>> 2;
      case 6:
          return (rc.readSharedArray(34) & 64512) >>> 10;
      case 7:
          return (rc.readSharedArray(35) & 252) >>> 2;
      case 8:
          return (rc.readSharedArray(37) & 64512) >>> 10;
      case 9:
          return (rc.readSharedArray(38) & 252) >>> 2;
      case 10:
          return (rc.readSharedArray(40) & 64512) >>> 10;
      case 11:
          return (rc.readSharedArray(41) & 252) >>> 2;
      case 12:
          return (rc.readSharedArray(43) & 64512) >>> 10;
      case 13:
          return (rc.readSharedArray(44) & 252) >>> 2;
      case 14:
          return (rc.readSharedArray(46) & 64512) >>> 10;
      case 15:
          return (rc.readSharedArray(47) & 252) >>> 2;
      case 16:
          return (rc.readSharedArray(49) & 64512) >>> 10;
      case 17:
          return (rc.readSharedArray(50) & 252) >>> 2;
      case 18:
          return (rc.readSharedArray(52) & 64512) >>> 10;
      case 19:
          return (rc.readSharedArray(53) & 252) >>> 2;
      case 20:
          return (rc.readSharedArray(55) & 64512) >>> 10;
      case 21:
          return (rc.readSharedArray(56) & 252) >>> 2;
      case 22:
          return (rc.readSharedArray(58) & 64512) >>> 10;
      case 23:
          return (rc.readSharedArray(59) & 252) >>> 2;
      case 24:
          return (rc.readSharedArray(61) & 64512) >>> 10;
      case 25:
          return (rc.readSharedArray(62) & 252) >>> 2;
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 65283) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65283) | (value << 2));
        break;
      case 4:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 1023) | (value << 10));
        break;
      case 5:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 65283) | (value << 2));
        break;
      case 6:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 1023) | (value << 10));
        break;
      case 7:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 65283) | (value << 2));
        break;
      case 8:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 1023) | (value << 10));
        break;
      case 9:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 65283) | (value << 2));
        break;
      case 10:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 1023) | (value << 10));
        break;
      case 11:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65283) | (value << 2));
        break;
      case 12:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 1023) | (value << 10));
        break;
      case 13:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 65283) | (value << 2));
        break;
      case 14:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 1023) | (value << 10));
        break;
      case 15:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 65283) | (value << 2));
        break;
      case 16:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 1023) | (value << 10));
        break;
      case 17:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 65283) | (value << 2));
        break;
      case 18:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 1023) | (value << 10));
        break;
      case 19:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 65283) | (value << 2));
        break;
      case 20:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 1023) | (value << 10));
        break;
      case 21:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 65283) | (value << 2));
        break;
      case 22:
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 1023) | (value << 10));
        break;
      case 23:
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 65283) | (value << 2));
        break;
      case 24:
        rc.writeSharedArray(61, (rc.readSharedArray(61) & 1023) | (value << 10));
        break;
      case 25:
        rc.writeSharedArray(62, (rc.readSharedArray(62) & 65283) | (value << 2));
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