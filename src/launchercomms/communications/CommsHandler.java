package launchercomms.communications;

import launchercomms.utils.Global;
import launchercomms.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int ADAMANTIUM_WELL_SLOTS = 4;
  public static final int MANA_WELL_SLOTS = 4;
  public static final int ELIXIR_WELL_SLOTS = 4;
  public static final int ENEMY_SLOTS = 30;
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
          return (rc.readSharedArray(1) & 16128) >>> 8;
      case 2:
          return ((rc.readSharedArray(1) & 3) << 4) + ((rc.readSharedArray(2) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(2) & 63);
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
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 49407) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65472) | (value));
        break;
    }
  }

  private static int readOurHqY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(0) & 15) << 2) + ((rc.readSharedArray(1) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(1) & 252) >>> 2;
      case 2:
          return (rc.readSharedArray(2) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(3) & 64512) >>> 10;
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
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65283) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 1023) | (value << 10));
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
  private static int readAdamantiumWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 1008) >>> 4;
      case 1:
          return (rc.readSharedArray(4) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(5) & 64512) >>> 10;
      case 3:
          return ((rc.readSharedArray(5) & 7) << 3) + ((rc.readSharedArray(6) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  private static int readAdamantiumWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(3) & 15) << 2) + ((rc.readSharedArray(4) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(4) & 126) >>> 1;
      case 2:
          return (rc.readSharedArray(5) & 1008) >>> 4;
      case 3:
          return (rc.readSharedArray(6) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 57471) | (value << 7));
        break;
    }
  }

  private static int readAdamantiumWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(4) & 8192) >>> 13;
      case 1:
          return (rc.readSharedArray(4) & 1);
      case 2:
          return (rc.readSharedArray(5) & 8) >>> 3;
      case 3:
          return (rc.readSharedArray(6) & 64) >>> 6;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 57343) | (value << 13));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65534) | (value));
        break;
      case 2:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65527) | (value << 3));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65471) | (value << 6));
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
          return (rc.readSharedArray(6) & 63);
      case 1:
          return (rc.readSharedArray(7) & 504) >>> 3;
      case 2:
          return (rc.readSharedArray(8) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(9) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private static void writeManaWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65472) | (value));
        break;
      case 1:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 33279) | (value << 9));
        break;
    }
  }

  private static int readManaWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(7) & 64512) >>> 10;
      case 1:
          return ((rc.readSharedArray(7) & 7) << 3) + ((rc.readSharedArray(8) & 57344) >>> 13);
      case 2:
          return (rc.readSharedArray(8) & 63);
      case 3:
          return (rc.readSharedArray(9) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private static void writeManaWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 8191) | ((value & 7) << 13));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65031) | (value << 3));
        break;
    }
  }

  private static int readManaWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(7) & 512) >>> 9;
      case 1:
          return (rc.readSharedArray(8) & 4096) >>> 12;
      case 2:
          return (rc.readSharedArray(9) & 32768) >>> 15;
      case 3:
          return (rc.readSharedArray(9) & 4) >>> 2;
      default:
          return -1;
    }
  }

  private static void writeManaWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65023) | (value << 9));
        break;
      case 1:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 61439) | (value << 12));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 32767) | (value << 15));
        break;
      case 3:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65531) | (value << 2));
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
          return ((rc.readSharedArray(9) & 3) << 4) + ((rc.readSharedArray(10) & 61440) >>> 12);
      case 1:
          return ((rc.readSharedArray(10) & 31) << 1) + ((rc.readSharedArray(11) & 32768) >>> 15);
      case 2:
          return (rc.readSharedArray(11) & 252) >>> 2;
      case 3:
          return (rc.readSharedArray(12) & 2016) >>> 5;
      default:
          return -1;
    }
  }

  private static void writeElixirWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 32767) | ((value & 1) << 15));
        break;
      case 2:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65283) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 63519) | (value << 5));
        break;
    }
  }

  private static int readElixirWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(10) & 4032) >>> 6;
      case 1:
          return (rc.readSharedArray(11) & 32256) >>> 9;
      case 2:
          return ((rc.readSharedArray(11) & 3) << 4) + ((rc.readSharedArray(12) & 61440) >>> 12);
      case 3:
          return ((rc.readSharedArray(12) & 31) << 1) + ((rc.readSharedArray(13) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  private static void writeElixirWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 33279) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  private static int readElixirWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(10) & 32) >>> 5;
      case 1:
          return (rc.readSharedArray(11) & 256) >>> 8;
      case 2:
          return (rc.readSharedArray(12) & 2048) >>> 11;
      case 3:
          return (rc.readSharedArray(13) & 16384) >>> 14;
      default:
          return -1;
    }
  }

  private static void writeElixirWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65503) | (value << 5));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65279) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 63487) | (value << 11));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 49151) | (value << 14));
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
          return (rc.readSharedArray(13) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(14) & 63);
      case 2:
          return (rc.readSharedArray(16) & 16128) >>> 8;
      case 3:
          return (rc.readSharedArray(17) & 63);
      case 4:
          return (rc.readSharedArray(19) & 16128) >>> 8;
      case 5:
          return (rc.readSharedArray(20) & 63);
      case 6:
          return (rc.readSharedArray(22) & 16128) >>> 8;
      case 7:
          return (rc.readSharedArray(23) & 63);
      case 8:
          return (rc.readSharedArray(25) & 16128) >>> 8;
      case 9:
          return (rc.readSharedArray(26) & 63);
      case 10:
          return (rc.readSharedArray(28) & 16128) >>> 8;
      case 11:
          return (rc.readSharedArray(29) & 63);
      case 12:
          return (rc.readSharedArray(31) & 16128) >>> 8;
      case 13:
          return (rc.readSharedArray(32) & 63);
      case 14:
          return (rc.readSharedArray(34) & 16128) >>> 8;
      case 15:
          return (rc.readSharedArray(35) & 63);
      case 16:
          return (rc.readSharedArray(37) & 16128) >>> 8;
      case 17:
          return (rc.readSharedArray(38) & 63);
      case 18:
          return (rc.readSharedArray(40) & 16128) >>> 8;
      case 19:
          return (rc.readSharedArray(41) & 63);
      case 20:
          return (rc.readSharedArray(43) & 16128) >>> 8;
      case 21:
          return (rc.readSharedArray(44) & 63);
      case 22:
          return (rc.readSharedArray(46) & 16128) >>> 8;
      case 23:
          return (rc.readSharedArray(47) & 63);
      case 24:
          return (rc.readSharedArray(49) & 16128) >>> 8;
      case 25:
          return (rc.readSharedArray(50) & 63);
      case 26:
          return (rc.readSharedArray(52) & 16128) >>> 8;
      case 27:
          return (rc.readSharedArray(53) & 63);
      case 28:
          return (rc.readSharedArray(55) & 16128) >>> 8;
      case 29:
          return (rc.readSharedArray(56) & 63);
      default:
          return -1;
    }
  }

  private static void writeEnemyOddX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65472) | (value));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65472) | (value));
        break;
      case 4:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 49407) | (value << 8));
        break;
      case 5:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65472) | (value));
        break;
      case 6:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 49407) | (value << 8));
        break;
      case 7:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65472) | (value));
        break;
      case 8:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 49407) | (value << 8));
        break;
      case 9:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 65472) | (value));
        break;
      case 10:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 49407) | (value << 8));
        break;
      case 11:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65472) | (value));
        break;
      case 12:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 49407) | (value << 8));
        break;
      case 13:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 65472) | (value));
        break;
      case 14:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 49407) | (value << 8));
        break;
      case 15:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 65472) | (value));
        break;
      case 16:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 49407) | (value << 8));
        break;
      case 17:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 65472) | (value));
        break;
      case 18:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 49407) | (value << 8));
        break;
      case 19:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65472) | (value));
        break;
      case 20:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 49407) | (value << 8));
        break;
      case 21:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 65472) | (value));
        break;
      case 22:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 49407) | (value << 8));
        break;
      case 23:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 65472) | (value));
        break;
      case 24:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 49407) | (value << 8));
        break;
      case 25:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 65472) | (value));
        break;
      case 26:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 49407) | (value << 8));
        break;
      case 27:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 65472) | (value));
        break;
      case 28:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 49407) | (value << 8));
        break;
      case 29:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 65472) | (value));
        break;
    }
  }

  private static int readEnemyOddY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 252) >>> 2;
      case 1:
          return (rc.readSharedArray(15) & 64512) >>> 10;
      case 2:
          return (rc.readSharedArray(16) & 252) >>> 2;
      case 3:
          return (rc.readSharedArray(18) & 64512) >>> 10;
      case 4:
          return (rc.readSharedArray(19) & 252) >>> 2;
      case 5:
          return (rc.readSharedArray(21) & 64512) >>> 10;
      case 6:
          return (rc.readSharedArray(22) & 252) >>> 2;
      case 7:
          return (rc.readSharedArray(24) & 64512) >>> 10;
      case 8:
          return (rc.readSharedArray(25) & 252) >>> 2;
      case 9:
          return (rc.readSharedArray(27) & 64512) >>> 10;
      case 10:
          return (rc.readSharedArray(28) & 252) >>> 2;
      case 11:
          return (rc.readSharedArray(30) & 64512) >>> 10;
      case 12:
          return (rc.readSharedArray(31) & 252) >>> 2;
      case 13:
          return (rc.readSharedArray(33) & 64512) >>> 10;
      case 14:
          return (rc.readSharedArray(34) & 252) >>> 2;
      case 15:
          return (rc.readSharedArray(36) & 64512) >>> 10;
      case 16:
          return (rc.readSharedArray(37) & 252) >>> 2;
      case 17:
          return (rc.readSharedArray(39) & 64512) >>> 10;
      case 18:
          return (rc.readSharedArray(40) & 252) >>> 2;
      case 19:
          return (rc.readSharedArray(42) & 64512) >>> 10;
      case 20:
          return (rc.readSharedArray(43) & 252) >>> 2;
      case 21:
          return (rc.readSharedArray(45) & 64512) >>> 10;
      case 22:
          return (rc.readSharedArray(46) & 252) >>> 2;
      case 23:
          return (rc.readSharedArray(48) & 64512) >>> 10;
      case 24:
          return (rc.readSharedArray(49) & 252) >>> 2;
      case 25:
          return (rc.readSharedArray(51) & 64512) >>> 10;
      case 26:
          return (rc.readSharedArray(52) & 252) >>> 2;
      case 27:
          return (rc.readSharedArray(54) & 64512) >>> 10;
      case 28:
          return (rc.readSharedArray(55) & 252) >>> 2;
      case 29:
          return (rc.readSharedArray(57) & 64512) >>> 10;
      default:
          return -1;
    }
  }

  private static void writeEnemyOddY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 1023) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65283) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 1023) | (value << 10));
        break;
      case 4:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65283) | (value << 2));
        break;
      case 5:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 1023) | (value << 10));
        break;
      case 6:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65283) | (value << 2));
        break;
      case 7:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 1023) | (value << 10));
        break;
      case 8:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 65283) | (value << 2));
        break;
      case 9:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 1023) | (value << 10));
        break;
      case 10:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65283) | (value << 2));
        break;
      case 11:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 1023) | (value << 10));
        break;
      case 12:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 65283) | (value << 2));
        break;
      case 13:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 1023) | (value << 10));
        break;
      case 14:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65283) | (value << 2));
        break;
      case 15:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 1023) | (value << 10));
        break;
      case 16:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 65283) | (value << 2));
        break;
      case 17:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 1023) | (value << 10));
        break;
      case 18:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 65283) | (value << 2));
        break;
      case 19:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 1023) | (value << 10));
        break;
      case 20:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65283) | (value << 2));
        break;
      case 21:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 1023) | (value << 10));
        break;
      case 22:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 65283) | (value << 2));
        break;
      case 23:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 1023) | (value << 10));
        break;
      case 24:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 65283) | (value << 2));
        break;
      case 25:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 1023) | (value << 10));
        break;
      case 26:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 65283) | (value << 2));
        break;
      case 27:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 1023) | (value << 10));
        break;
      case 28:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 65283) | (value << 2));
        break;
      case 29:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 1023) | (value << 10));
        break;
    }
  }

  private static int readEnemyEvenX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(13) & 3) << 4) + ((rc.readSharedArray(14) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(15) & 1008) >>> 4;
      case 2:
          return ((rc.readSharedArray(16) & 3) << 4) + ((rc.readSharedArray(17) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(18) & 1008) >>> 4;
      case 4:
          return ((rc.readSharedArray(19) & 3) << 4) + ((rc.readSharedArray(20) & 61440) >>> 12);
      case 5:
          return (rc.readSharedArray(21) & 1008) >>> 4;
      case 6:
          return ((rc.readSharedArray(22) & 3) << 4) + ((rc.readSharedArray(23) & 61440) >>> 12);
      case 7:
          return (rc.readSharedArray(24) & 1008) >>> 4;
      case 8:
          return ((rc.readSharedArray(25) & 3) << 4) + ((rc.readSharedArray(26) & 61440) >>> 12);
      case 9:
          return (rc.readSharedArray(27) & 1008) >>> 4;
      case 10:
          return ((rc.readSharedArray(28) & 3) << 4) + ((rc.readSharedArray(29) & 61440) >>> 12);
      case 11:
          return (rc.readSharedArray(30) & 1008) >>> 4;
      case 12:
          return ((rc.readSharedArray(31) & 3) << 4) + ((rc.readSharedArray(32) & 61440) >>> 12);
      case 13:
          return (rc.readSharedArray(33) & 1008) >>> 4;
      case 14:
          return ((rc.readSharedArray(34) & 3) << 4) + ((rc.readSharedArray(35) & 61440) >>> 12);
      case 15:
          return (rc.readSharedArray(36) & 1008) >>> 4;
      case 16:
          return ((rc.readSharedArray(37) & 3) << 4) + ((rc.readSharedArray(38) & 61440) >>> 12);
      case 17:
          return (rc.readSharedArray(39) & 1008) >>> 4;
      case 18:
          return ((rc.readSharedArray(40) & 3) << 4) + ((rc.readSharedArray(41) & 61440) >>> 12);
      case 19:
          return (rc.readSharedArray(42) & 1008) >>> 4;
      case 20:
          return ((rc.readSharedArray(43) & 3) << 4) + ((rc.readSharedArray(44) & 61440) >>> 12);
      case 21:
          return (rc.readSharedArray(45) & 1008) >>> 4;
      case 22:
          return ((rc.readSharedArray(46) & 3) << 4) + ((rc.readSharedArray(47) & 61440) >>> 12);
      case 23:
          return (rc.readSharedArray(48) & 1008) >>> 4;
      case 24:
          return ((rc.readSharedArray(49) & 3) << 4) + ((rc.readSharedArray(50) & 61440) >>> 12);
      case 25:
          return (rc.readSharedArray(51) & 1008) >>> 4;
      case 26:
          return ((rc.readSharedArray(52) & 3) << 4) + ((rc.readSharedArray(53) & 61440) >>> 12);
      case 27:
          return (rc.readSharedArray(54) & 1008) >>> 4;
      case 28:
          return ((rc.readSharedArray(55) & 3) << 4) + ((rc.readSharedArray(56) & 61440) >>> 12);
      case 29:
          return (rc.readSharedArray(57) & 1008) >>> 4;
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 64527) | (value << 4));
        break;
      case 4:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 4095) | ((value & 15) << 12));
        break;
      case 5:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 64527) | (value << 4));
        break;
      case 6:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 4095) | ((value & 15) << 12));
        break;
      case 7:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 64527) | (value << 4));
        break;
      case 8:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 4095) | ((value & 15) << 12));
        break;
      case 9:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 64527) | (value << 4));
        break;
      case 10:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 4095) | ((value & 15) << 12));
        break;
      case 11:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 64527) | (value << 4));
        break;
      case 12:
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 4095) | ((value & 15) << 12));
        break;
      case 13:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 64527) | (value << 4));
        break;
      case 14:
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 4095) | ((value & 15) << 12));
        break;
      case 15:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 64527) | (value << 4));
        break;
      case 16:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 4095) | ((value & 15) << 12));
        break;
      case 17:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 64527) | (value << 4));
        break;
      case 18:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 4095) | ((value & 15) << 12));
        break;
      case 19:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 64527) | (value << 4));
        break;
      case 20:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 4095) | ((value & 15) << 12));
        break;
      case 21:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 64527) | (value << 4));
        break;
      case 22:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 4095) | ((value & 15) << 12));
        break;
      case 23:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 64527) | (value << 4));
        break;
      case 24:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 4095) | ((value & 15) << 12));
        break;
      case 25:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 64527) | (value << 4));
        break;
      case 26:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 4095) | ((value & 15) << 12));
        break;
      case 27:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 64527) | (value << 4));
        break;
      case 28:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 4095) | ((value & 15) << 12));
        break;
      case 29:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 64527) | (value << 4));
        break;
    }
  }

  private static int readEnemyEvenY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(14) & 4032) >>> 6;
      case 1:
          return ((rc.readSharedArray(15) & 15) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(17) & 4032) >>> 6;
      case 3:
          return ((rc.readSharedArray(18) & 15) << 2) + ((rc.readSharedArray(19) & 49152) >>> 14);
      case 4:
          return (rc.readSharedArray(20) & 4032) >>> 6;
      case 5:
          return ((rc.readSharedArray(21) & 15) << 2) + ((rc.readSharedArray(22) & 49152) >>> 14);
      case 6:
          return (rc.readSharedArray(23) & 4032) >>> 6;
      case 7:
          return ((rc.readSharedArray(24) & 15) << 2) + ((rc.readSharedArray(25) & 49152) >>> 14);
      case 8:
          return (rc.readSharedArray(26) & 4032) >>> 6;
      case 9:
          return ((rc.readSharedArray(27) & 15) << 2) + ((rc.readSharedArray(28) & 49152) >>> 14);
      case 10:
          return (rc.readSharedArray(29) & 4032) >>> 6;
      case 11:
          return ((rc.readSharedArray(30) & 15) << 2) + ((rc.readSharedArray(31) & 49152) >>> 14);
      case 12:
          return (rc.readSharedArray(32) & 4032) >>> 6;
      case 13:
          return ((rc.readSharedArray(33) & 15) << 2) + ((rc.readSharedArray(34) & 49152) >>> 14);
      case 14:
          return (rc.readSharedArray(35) & 4032) >>> 6;
      case 15:
          return ((rc.readSharedArray(36) & 15) << 2) + ((rc.readSharedArray(37) & 49152) >>> 14);
      case 16:
          return (rc.readSharedArray(38) & 4032) >>> 6;
      case 17:
          return ((rc.readSharedArray(39) & 15) << 2) + ((rc.readSharedArray(40) & 49152) >>> 14);
      case 18:
          return (rc.readSharedArray(41) & 4032) >>> 6;
      case 19:
          return ((rc.readSharedArray(42) & 15) << 2) + ((rc.readSharedArray(43) & 49152) >>> 14);
      case 20:
          return (rc.readSharedArray(44) & 4032) >>> 6;
      case 21:
          return ((rc.readSharedArray(45) & 15) << 2) + ((rc.readSharedArray(46) & 49152) >>> 14);
      case 22:
          return (rc.readSharedArray(47) & 4032) >>> 6;
      case 23:
          return ((rc.readSharedArray(48) & 15) << 2) + ((rc.readSharedArray(49) & 49152) >>> 14);
      case 24:
          return (rc.readSharedArray(50) & 4032) >>> 6;
      case 25:
          return ((rc.readSharedArray(51) & 15) << 2) + ((rc.readSharedArray(52) & 49152) >>> 14);
      case 26:
          return (rc.readSharedArray(53) & 4032) >>> 6;
      case 27:
          return ((rc.readSharedArray(54) & 15) << 2) + ((rc.readSharedArray(55) & 49152) >>> 14);
      case 28:
          return (rc.readSharedArray(56) & 4032) >>> 6;
      case 29:
          return ((rc.readSharedArray(57) & 15) << 2) + ((rc.readSharedArray(58) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 16383) | ((value & 3) << 14));
        break;
      case 4:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 61503) | (value << 6));
        break;
      case 5:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 16383) | ((value & 3) << 14));
        break;
      case 6:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 61503) | (value << 6));
        break;
      case 7:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 16383) | ((value & 3) << 14));
        break;
      case 8:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 61503) | (value << 6));
        break;
      case 9:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 16383) | ((value & 3) << 14));
        break;
      case 10:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 61503) | (value << 6));
        break;
      case 11:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(31, (rc.readSharedArray(31) & 16383) | ((value & 3) << 14));
        break;
      case 12:
        rc.writeSharedArray(32, (rc.readSharedArray(32) & 61503) | (value << 6));
        break;
      case 13:
        rc.writeSharedArray(33, (rc.readSharedArray(33) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 16383) | ((value & 3) << 14));
        break;
      case 14:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 61503) | (value << 6));
        break;
      case 15:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 16383) | ((value & 3) << 14));
        break;
      case 16:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 61503) | (value << 6));
        break;
      case 17:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 16383) | ((value & 3) << 14));
        break;
      case 18:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 61503) | (value << 6));
        break;
      case 19:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 16383) | ((value & 3) << 14));
        break;
      case 20:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 61503) | (value << 6));
        break;
      case 21:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 16383) | ((value & 3) << 14));
        break;
      case 22:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 61503) | (value << 6));
        break;
      case 23:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 16383) | ((value & 3) << 14));
        break;
      case 24:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 61503) | (value << 6));
        break;
      case 25:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 16383) | ((value & 3) << 14));
        break;
      case 26:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 61503) | (value << 6));
        break;
      case 27:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 16383) | ((value & 3) << 14));
        break;
      case 28:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 61503) | (value << 6));
        break;
      case 29:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 16383) | ((value & 3) << 14));
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
