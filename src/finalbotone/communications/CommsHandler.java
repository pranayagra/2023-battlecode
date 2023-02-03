package finalbotone.communications;

import finalbotone.utils.Global;
import finalbotone.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int ADAMANTIUM_WELL_SLOTS = 4;
  public static final int MANA_WELL_SLOTS = 4;
  public static final int ELIXIR_WELL_SLOTS = 4;
  public static final int ISLAND_INFO_SLOTS = 1;
  public static final int NEXT_ISLAND_TO_CLAIM_SLOTS = 1;
  public static final int MY_ISLANDS_SLOTS = 1;
  public static final int ENEMY_SLOTS = 19;
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

  private static int readNumLaunchersBits() throws GameActionException {
    return (rc.readSharedArray(0) & 992) >>> 5;
  }

  private static void writeNumLaunchersBits(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 64543) | (value << 5));
  }

  private static int readNumAmpsBits() throws GameActionException {
    return (rc.readSharedArray(0) & 31);
  }

  private static void writeNumAmpsBits(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 65504) | (value));
  }

  public static int readNumLaunchers() throws GameActionException {
    return (readNumLaunchersBits());
  }
  public static void writeNumLaunchersReset() throws GameActionException {
    writeNumLaunchersBits(0);
  }
  public static void writeNumLaunchersSet(int value) throws GameActionException {
    writeNumLaunchersBits(Math.min(Math.max(value, 0), 31));
  }
  public static void writeNumLaunchersIncrement() throws GameActionException {
    writeNumLaunchersBits(Math.min((readNumLaunchers())+1, 31));
  }
  public static void writeNumLaunchersDecrement() throws GameActionException {
    writeNumLaunchersBits(Math.max((readNumLaunchers())-1, 0));
  }
  public static int readNumAmps() throws GameActionException {
    return (readNumAmpsBits());
  }
  public static void writeNumAmpsReset() throws GameActionException {
    writeNumAmpsBits(0);
  }
  public static void writeNumAmpsSet(int value) throws GameActionException {
    writeNumAmpsBits(Math.min(Math.max(value, 0), 31));
  }
  public static void writeNumAmpsIncrement() throws GameActionException {
    writeNumAmpsBits(Math.min((readNumAmps())+1, 31));
  }
  public static void writeNumAmpsDecrement() throws GameActionException {
    writeNumAmpsBits(Math.max((readNumAmps())-1, 0));
  }
  private static int readOurHqX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 64512) >>> 10;
      case 1:
          return (rc.readSharedArray(4) & 504) >>> 3;
      case 2:
          return ((rc.readSharedArray(7) & 3) << 4) + ((rc.readSharedArray(8) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(11) & 2016) >>> 5;
      default:
          return -1;
    }
  }

  private static void writeOurHqX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 63519) | (value << 5));
        break;
    }
  }

  private static int readOurHqY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 1008) >>> 4;
      case 1:
          return ((rc.readSharedArray(4) & 7) << 3) + ((rc.readSharedArray(5) & 57344) >>> 13);
      case 2:
          return (rc.readSharedArray(8) & 4032) >>> 6;
      case 3:
          return ((rc.readSharedArray(11) & 31) << 1) + ((rc.readSharedArray(12) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  private static void writeOurHqY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 8191) | ((value & 7) << 13));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  private static int readOurHqOddSpawnX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(1) & 15) << 2) + ((rc.readSharedArray(2) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(5) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(8) & 63);
      case 3:
          return (rc.readSharedArray(12) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private static void writeOurHqOddSpawnX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 33279) | (value << 9));
        break;
    }
  }

  private static int readOurHqOddSpawnY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(5) & 126) >>> 1;
      case 2:
          return (rc.readSharedArray(9) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(12) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private static void writeOurHqOddSpawnY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65031) | (value << 3));
        break;
    }
  }

  private static int readOurHqEvenSpawnX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 252) >>> 2;
      case 1:
          return ((rc.readSharedArray(5) & 1) << 5) + ((rc.readSharedArray(6) & 63488) >>> 11);
      case 2:
          return (rc.readSharedArray(9) & 1008) >>> 4;
      case 3:
          return ((rc.readSharedArray(12) & 7) << 3) + ((rc.readSharedArray(13) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private static void writeOurHqEvenSpawnX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 2047) | ((value & 31) << 11));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  private static int readOurHqEvenSpawnY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(2) & 3) << 4) + ((rc.readSharedArray(3) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(6) & 2016) >>> 5;
      case 2:
          return ((rc.readSharedArray(9) & 15) << 2) + ((rc.readSharedArray(10) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(13) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private static void writeOurHqEvenSpawnY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 63519) | (value << 5));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 57471) | (value << 7));
        break;
    }
  }

  public static int readOurHqOddSpawnInstruction(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 3072) >>> 10;
      case 1:
          return (rc.readSharedArray(6) & 24) >>> 3;
      case 2:
          return (rc.readSharedArray(10) & 12288) >>> 12;
      case 3:
          return (rc.readSharedArray(13) & 96) >>> 5;
      default:
          return -1;
    }
  }

  public static void writeOurHqOddSpawnInstruction(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 62463) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65511) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 53247) | (value << 12));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65439) | (value << 5));
        break;
    }
  }

  public static int readOurHqEvenSpawnInstruction(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 768) >>> 8;
      case 1:
          return (rc.readSharedArray(6) & 6) >>> 1;
      case 2:
          return (rc.readSharedArray(10) & 3072) >>> 10;
      case 3:
          return (rc.readSharedArray(13) & 24) >>> 3;
      default:
          return -1;
    }
  }

  public static void writeOurHqEvenSpawnInstruction(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 64767) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65529) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 62463) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65511) | (value << 3));
        break;
    }
  }

  private static int readOurHqAdamantiumIncomeBits(int idx) throws GameActionException {
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

  private static void writeOurHqAdamantiumIncomeBits(int idx, int value) throws GameActionException {
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

  private static int readOurHqManaIncomeBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(3) & 7) << 2) + ((rc.readSharedArray(4) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(7) & 3968) >>> 7;
      case 2:
          return (rc.readSharedArray(10) & 31);
      case 3:
          return (rc.readSharedArray(14) & 15872) >>> 9;
      default:
          return -1;
    }
  }

  private static void writeOurHqManaIncomeBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65528) | ((value & 28) >>> 2));
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 61567) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65504) | (value));
        break;
      case 3:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 49663) | (value << 9));
        break;
    }
  }

  private static int readOurHqElixirIncomeBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(4) & 15872) >>> 9;
      case 1:
          return (rc.readSharedArray(7) & 124) >>> 2;
      case 2:
          return (rc.readSharedArray(11) & 63488) >>> 11;
      case 3:
          return (rc.readSharedArray(14) & 496) >>> 4;
      default:
          return -1;
    }
  }

  private static void writeOurHqElixirIncomeBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 49663) | (value << 9));
        break;
      case 1:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65411) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 2047) | (value << 11));
        break;
      case 3:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65039) | (value << 4));
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
  public static int readOurHqAdamantiumIncome(int idx) throws GameActionException {
    return (readOurHqAdamantiumIncomeBits(idx));
  }
  public static void writeOurHqAdamantiumIncomeReset(int idx) throws GameActionException {
    writeOurHqAdamantiumIncomeBits(idx, 0);
  }
  public static void writeOurHqAdamantiumIncomeSet(int idx, int value) throws GameActionException {
    writeOurHqAdamantiumIncomeBits(idx, Math.min(Math.max(value, 0), 31));
  }
  public static void writeOurHqAdamantiumIncomeIncrement(int idx) throws GameActionException {
    writeOurHqAdamantiumIncomeBits(idx, Math.min((readOurHqAdamantiumIncome(idx))+1, 31));
  }
  public static void writeOurHqAdamantiumIncomeDecrement(int idx) throws GameActionException {
    writeOurHqAdamantiumIncomeBits(idx, Math.max((readOurHqAdamantiumIncome(idx))-1, 0));
  }
  public static int readOurHqManaIncome(int idx) throws GameActionException {
    return (readOurHqManaIncomeBits(idx));
  }
  public static void writeOurHqManaIncomeReset(int idx) throws GameActionException {
    writeOurHqManaIncomeBits(idx, 0);
  }
  public static void writeOurHqManaIncomeSet(int idx, int value) throws GameActionException {
    writeOurHqManaIncomeBits(idx, Math.min(Math.max(value, 0), 31));
  }
  public static void writeOurHqManaIncomeIncrement(int idx) throws GameActionException {
    writeOurHqManaIncomeBits(idx, Math.min((readOurHqManaIncome(idx))+1, 31));
  }
  public static void writeOurHqManaIncomeDecrement(int idx) throws GameActionException {
    writeOurHqManaIncomeBits(idx, Math.max((readOurHqManaIncome(idx))-1, 0));
  }
  public static int readOurHqElixirIncome(int idx) throws GameActionException {
    return (readOurHqElixirIncomeBits(idx));
  }
  public static void writeOurHqElixirIncomeReset(int idx) throws GameActionException {
    writeOurHqElixirIncomeBits(idx, 0);
  }
  public static void writeOurHqElixirIncomeSet(int idx, int value) throws GameActionException {
    writeOurHqElixirIncomeBits(idx, Math.min(Math.max(value, 0), 31));
  }
  public static void writeOurHqElixirIncomeIncrement(int idx) throws GameActionException {
    writeOurHqElixirIncomeBits(idx, Math.min((readOurHqElixirIncome(idx))+1, 31));
  }
  public static void writeOurHqElixirIncomeDecrement(int idx) throws GameActionException {
    writeOurHqElixirIncomeBits(idx, Math.max((readOurHqElixirIncome(idx))-1, 0));
  }
  private static int readAdamantiumWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(14) & 15) << 2) + ((rc.readSharedArray(15) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(16) & 32256) >>> 9;
      case 2:
          return (rc.readSharedArray(17) & 1008) >>> 4;
      case 3:
          return ((rc.readSharedArray(18) & 31) << 1) + ((rc.readSharedArray(19) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 33279) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  private static int readAdamantiumWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(15) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(16) & 504) >>> 3;
      case 2:
          return ((rc.readSharedArray(17) & 15) << 2) + ((rc.readSharedArray(18) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(19) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 33279) | (value << 9));
        break;
    }
  }

  private static int readAdamantiumWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(15) & 128) >>> 7;
      case 1:
          return (rc.readSharedArray(16) & 4) >>> 2;
      case 2:
          return (rc.readSharedArray(18) & 8192) >>> 13;
      case 3:
          return (rc.readSharedArray(19) & 256) >>> 8;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65407) | (value << 7));
        break;
      case 1:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65531) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 57343) | (value << 13));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65279) | (value << 8));
        break;
    }
  }

  private static int readAdamantiumWellCapacityBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(15) & 120) >>> 3;
      case 1:
          return ((rc.readSharedArray(16) & 3) << 2) + ((rc.readSharedArray(17) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(18) & 7680) >>> 9;
      case 3:
          return (rc.readSharedArray(19) & 240) >>> 4;
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellCapacityBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65415) | (value << 3));
        break;
      case 1:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65532) | ((value & 12) >>> 2));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 57855) | (value << 9));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65295) | (value << 4));
        break;
    }
  }

  private static int readAdamantiumWellCurrentWorkersBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(15) & 7) << 1) + ((rc.readSharedArray(16) & 32768) >>> 15);
      case 1:
          return (rc.readSharedArray(17) & 15360) >>> 10;
      case 2:
          return (rc.readSharedArray(18) & 480) >>> 5;
      case 3:
          return (rc.readSharedArray(19) & 15);
      default:
          return -1;
    }
  }

  private static void writeAdamantiumWellCurrentWorkersBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65528) | ((value & 14) >>> 1));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 32767) | ((value & 1) << 15));
        break;
      case 1:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 50175) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65055) | (value << 5));
        break;
      case 3:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65520) | (value));
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
  public static int readAdamantiumWellCapacity(int idx) throws GameActionException {
    return (readAdamantiumWellCapacityBits(idx));
  }
  public static void writeAdamantiumWellCapacityReset(int idx) throws GameActionException {
    writeAdamantiumWellCapacityBits(idx, 0);
  }
  public static void writeAdamantiumWellCapacitySet(int idx, int value) throws GameActionException {
    writeAdamantiumWellCapacityBits(idx, Math.min(Math.max(value, 0), 15));
  }
  public static void writeAdamantiumWellCapacityIncrement(int idx) throws GameActionException {
    writeAdamantiumWellCapacityBits(idx, Math.min((readAdamantiumWellCapacity(idx))+1, 15));
  }
  public static void writeAdamantiumWellCapacityDecrement(int idx) throws GameActionException {
    writeAdamantiumWellCapacityBits(idx, Math.max((readAdamantiumWellCapacity(idx))-1, 0));
  }
  public static int readAdamantiumWellCurrentWorkers(int idx) throws GameActionException {
    return (readAdamantiumWellCurrentWorkersBits(idx));
  }
  public static void writeAdamantiumWellCurrentWorkersReset(int idx) throws GameActionException {
    writeAdamantiumWellCurrentWorkersBits(idx, 0);
  }
  public static void writeAdamantiumWellCurrentWorkersSet(int idx, int value) throws GameActionException {
    writeAdamantiumWellCurrentWorkersBits(idx, Math.min(Math.max(value, 0), 15));
  }
  public static void writeAdamantiumWellCurrentWorkersIncrement(int idx) throws GameActionException {
    writeAdamantiumWellCurrentWorkersBits(idx, Math.min((readAdamantiumWellCurrentWorkers(idx))+1, 15));
  }
  public static void writeAdamantiumWellCurrentWorkersDecrement(int idx) throws GameActionException {
    writeAdamantiumWellCurrentWorkersBits(idx, Math.max((readAdamantiumWellCurrentWorkers(idx))-1, 0));
  }
  private static int readManaWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(20) & 64512) >>> 10;
      case 1:
          return (rc.readSharedArray(21) & 2016) >>> 5;
      case 2:
          return (rc.readSharedArray(22) & 63);
      case 3:
          return ((rc.readSharedArray(23) & 1) << 5) + ((rc.readSharedArray(24) & 63488) >>> 11);
      default:
          return -1;
    }
  }

  private static void writeManaWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 63519) | (value << 5));
        break;
      case 2:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 2047) | ((value & 31) << 11));
        break;
    }
  }

  private static int readManaWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(20) & 1008) >>> 4;
      case 1:
          return ((rc.readSharedArray(21) & 31) << 1) + ((rc.readSharedArray(22) & 32768) >>> 15);
      case 2:
          return (rc.readSharedArray(23) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(24) & 2016) >>> 5;
      default:
          return -1;
    }
  }

  private static void writeManaWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 32767) | ((value & 1) << 15));
        break;
      case 2:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 63519) | (value << 5));
        break;
    }
  }

  private static int readManaWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(20) & 8) >>> 3;
      case 1:
          return (rc.readSharedArray(22) & 16384) >>> 14;
      case 2:
          return (rc.readSharedArray(23) & 512) >>> 9;
      case 3:
          return (rc.readSharedArray(24) & 16) >>> 4;
      default:
          return -1;
    }
  }

  private static void writeManaWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65527) | (value << 3));
        break;
      case 1:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 49151) | (value << 14));
        break;
      case 2:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65023) | (value << 9));
        break;
      case 3:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65519) | (value << 4));
        break;
    }
  }

  private static int readManaWellCapacityBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(20) & 7) << 1) + ((rc.readSharedArray(21) & 32768) >>> 15);
      case 1:
          return (rc.readSharedArray(22) & 15360) >>> 10;
      case 2:
          return (rc.readSharedArray(23) & 480) >>> 5;
      case 3:
          return (rc.readSharedArray(24) & 15);
      default:
          return -1;
    }
  }

  private static void writeManaWellCapacityBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65528) | ((value & 14) >>> 1));
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 32767) | ((value & 1) << 15));
        break;
      case 1:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 50175) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65055) | (value << 5));
        break;
      case 3:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65520) | (value));
        break;
    }
  }

  private static int readManaWellCurrentWorkersBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(21) & 30720) >>> 11;
      case 1:
          return (rc.readSharedArray(22) & 960) >>> 6;
      case 2:
          return (rc.readSharedArray(23) & 30) >>> 1;
      case 3:
          return (rc.readSharedArray(25) & 61440) >>> 12;
      default:
          return -1;
    }
  }

  private static void writeManaWellCurrentWorkersBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 34815) | (value << 11));
        break;
      case 1:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 64575) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65505) | (value << 1));
        break;
      case 3:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 4095) | (value << 12));
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
  public static int readManaWellCapacity(int idx) throws GameActionException {
    return (readManaWellCapacityBits(idx));
  }
  public static void writeManaWellCapacityReset(int idx) throws GameActionException {
    writeManaWellCapacityBits(idx, 0);
  }
  public static void writeManaWellCapacitySet(int idx, int value) throws GameActionException {
    writeManaWellCapacityBits(idx, Math.min(Math.max(value, 0), 15));
  }
  public static void writeManaWellCapacityIncrement(int idx) throws GameActionException {
    writeManaWellCapacityBits(idx, Math.min((readManaWellCapacity(idx))+1, 15));
  }
  public static void writeManaWellCapacityDecrement(int idx) throws GameActionException {
    writeManaWellCapacityBits(idx, Math.max((readManaWellCapacity(idx))-1, 0));
  }
  public static int readManaWellCurrentWorkers(int idx) throws GameActionException {
    return (readManaWellCurrentWorkersBits(idx));
  }
  public static void writeManaWellCurrentWorkersReset(int idx) throws GameActionException {
    writeManaWellCurrentWorkersBits(idx, 0);
  }
  public static void writeManaWellCurrentWorkersSet(int idx, int value) throws GameActionException {
    writeManaWellCurrentWorkersBits(idx, Math.min(Math.max(value, 0), 15));
  }
  public static void writeManaWellCurrentWorkersIncrement(int idx) throws GameActionException {
    writeManaWellCurrentWorkersBits(idx, Math.min((readManaWellCurrentWorkers(idx))+1, 15));
  }
  public static void writeManaWellCurrentWorkersDecrement(int idx) throws GameActionException {
    writeManaWellCurrentWorkersBits(idx, Math.max((readManaWellCurrentWorkers(idx))-1, 0));
  }
  private static int readElixirWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(25) & 4032) >>> 6;
      case 1:
          return (rc.readSharedArray(26) & 126) >>> 1;
      case 2:
          return ((rc.readSharedArray(27) & 3) << 4) + ((rc.readSharedArray(28) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(29) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private static void writeElixirWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 61503) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 57471) | (value << 7));
        break;
    }
  }

  private static int readElixirWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(25) & 63);
      case 1:
          return ((rc.readSharedArray(26) & 1) << 5) + ((rc.readSharedArray(27) & 63488) >>> 11);
      case 2:
          return (rc.readSharedArray(28) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(29) & 126) >>> 1;
      default:
          return -1;
    }
  }

  private static void writeElixirWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 65472) | (value));
        break;
      case 1:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 2047) | ((value & 31) << 11));
        break;
      case 2:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65409) | (value << 1));
        break;
    }
  }

  private static int readElixirWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(26) & 32768) >>> 15;
      case 1:
          return (rc.readSharedArray(27) & 1024) >>> 10;
      case 2:
          return (rc.readSharedArray(28) & 32) >>> 5;
      case 3:
          return (rc.readSharedArray(29) & 1);
      default:
          return -1;
    }
  }

  private static void writeElixirWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 32767) | (value << 15));
        break;
      case 1:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 64511) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65503) | (value << 5));
        break;
      case 3:
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 65534) | (value));
        break;
    }
  }

  private static int readElixirWellCapacityBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(26) & 30720) >>> 11;
      case 1:
          return (rc.readSharedArray(27) & 960) >>> 6;
      case 2:
          return (rc.readSharedArray(28) & 30) >>> 1;
      case 3:
          return (rc.readSharedArray(30) & 61440) >>> 12;
      default:
          return -1;
    }
  }

  private static void writeElixirWellCapacityBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 34815) | (value << 11));
        break;
      case 1:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 64575) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65505) | (value << 1));
        break;
      case 3:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 4095) | (value << 12));
        break;
    }
  }

  private static int readElixirWellCurrentWorkersBits(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(26) & 1920) >>> 7;
      case 1:
          return (rc.readSharedArray(27) & 60) >>> 2;
      case 2:
          return ((rc.readSharedArray(28) & 1) << 3) + ((rc.readSharedArray(29) & 57344) >>> 13);
      case 3:
          return (rc.readSharedArray(30) & 3840) >>> 8;
      default:
          return -1;
    }
  }

  private static void writeElixirWellCurrentWorkersBits(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(26, (rc.readSharedArray(26) & 63615) | (value << 7));
        break;
      case 1:
        rc.writeSharedArray(27, (rc.readSharedArray(27) & 65475) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(28, (rc.readSharedArray(28) & 65534) | ((value & 8) >>> 3));
        rc.writeSharedArray(29, (rc.readSharedArray(29) & 8191) | ((value & 7) << 13));
        break;
      case 3:
        rc.writeSharedArray(30, (rc.readSharedArray(30) & 61695) | (value << 8));
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
  public static int readElixirWellCapacity(int idx) throws GameActionException {
    return (readElixirWellCapacityBits(idx));
  }
  public static void writeElixirWellCapacityReset(int idx) throws GameActionException {
    writeElixirWellCapacityBits(idx, 0);
  }
  public static void writeElixirWellCapacitySet(int idx, int value) throws GameActionException {
    writeElixirWellCapacityBits(idx, Math.min(Math.max(value, 0), 15));
  }
  public static void writeElixirWellCapacityIncrement(int idx) throws GameActionException {
    writeElixirWellCapacityBits(idx, Math.min((readElixirWellCapacity(idx))+1, 15));
  }
  public static void writeElixirWellCapacityDecrement(int idx) throws GameActionException {
    writeElixirWellCapacityBits(idx, Math.max((readElixirWellCapacity(idx))-1, 0));
  }
  public static int readElixirWellCurrentWorkers(int idx) throws GameActionException {
    return (readElixirWellCurrentWorkersBits(idx));
  }
  public static void writeElixirWellCurrentWorkersReset(int idx) throws GameActionException {
    writeElixirWellCurrentWorkersBits(idx, 0);
  }
  public static void writeElixirWellCurrentWorkersSet(int idx, int value) throws GameActionException {
    writeElixirWellCurrentWorkersBits(idx, Math.min(Math.max(value, 0), 15));
  }
  public static void writeElixirWellCurrentWorkersIncrement(int idx) throws GameActionException {
    writeElixirWellCurrentWorkersBits(idx, Math.min((readElixirWellCurrentWorkers(idx))+1, 15));
  }
  public static void writeElixirWellCurrentWorkersDecrement(int idx) throws GameActionException {
    writeElixirWellCurrentWorkersBits(idx, Math.max((readElixirWellCurrentWorkers(idx))-1, 0));
  }
  private static int readIslandInfoX() throws GameActionException {
    return (rc.readSharedArray(30) & 252) >>> 2;
  }

  private static void writeIslandInfoX(int value) throws GameActionException {
    rc.writeSharedArray(30, (rc.readSharedArray(30) & 65283) | (value << 2));
  }

  private static int readIslandInfoY() throws GameActionException {
    return ((rc.readSharedArray(30) & 3) << 4) + ((rc.readSharedArray(31) & 61440) >>> 12);
  }

  private static void writeIslandInfoY(int value) throws GameActionException {
    rc.writeSharedArray(30, (rc.readSharedArray(30) & 65532) | ((value & 48) >>> 4));
    rc.writeSharedArray(31, (rc.readSharedArray(31) & 4095) | ((value & 15) << 12));
  }

  public static int readIslandInfoOwner() throws GameActionException {
    return (rc.readSharedArray(31) & 3072) >>> 10;
  }

  public static void writeIslandInfoOwner(int value) throws GameActionException {
    rc.writeSharedArray(31, (rc.readSharedArray(31) & 62463) | (value << 10));
  }

  public static int readIslandInfoRoundNum() throws GameActionException {
    return ((rc.readSharedArray(31) & 1023) << 1) + ((rc.readSharedArray(32) & 32768) >>> 15);
  }

  public static void writeIslandInfoRoundNum(int value) throws GameActionException {
    rc.writeSharedArray(31, (rc.readSharedArray(31) & 64512) | ((value & 2046) >>> 1));
    rc.writeSharedArray(32, (rc.readSharedArray(32) & 32767) | ((value & 1) << 15));
  }

  public static int readIslandInfoIslandId() throws GameActionException {
    return (rc.readSharedArray(32) & 32256) >>> 9;
  }

  public static void writeIslandInfoIslandId(int value) throws GameActionException {
    rc.writeSharedArray(32, (rc.readSharedArray(32) & 33279) | (value << 9));
  }

  public static MapLocation readIslandInfoLocation() throws GameActionException {
    return new MapLocation(readIslandInfoX()-1,readIslandInfoY()-1);
  }
  public static boolean readIslandInfoExists() throws GameActionException {
    return !((readIslandInfoLocation()).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeIslandInfoLocation(MapLocation value) throws GameActionException {
    writeIslandInfoX((value).x+1);
    writeIslandInfoY((value).y+1);
  }
  private static int readNextIslandToClaimX() throws GameActionException {
    return (rc.readSharedArray(32) & 504) >>> 3;
  }

  private static void writeNextIslandToClaimX(int value) throws GameActionException {
    rc.writeSharedArray(32, (rc.readSharedArray(32) & 65031) | (value << 3));
  }

  private static int readNextIslandToClaimY() throws GameActionException {
    return ((rc.readSharedArray(32) & 7) << 3) + ((rc.readSharedArray(33) & 57344) >>> 13);
  }

  private static void writeNextIslandToClaimY(int value) throws GameActionException {
    rc.writeSharedArray(32, (rc.readSharedArray(32) & 65528) | ((value & 56) >>> 3));
    rc.writeSharedArray(33, (rc.readSharedArray(33) & 8191) | ((value & 7) << 13));
  }

  public static MapLocation readNextIslandToClaimLocation() throws GameActionException {
    return new MapLocation(readNextIslandToClaimX()-1,readNextIslandToClaimY()-1);
  }
  public static boolean readNextIslandToClaimExists() throws GameActionException {
    return !((readNextIslandToClaimLocation()).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeNextIslandToClaimLocation(MapLocation value) throws GameActionException {
    writeNextIslandToClaimX((value).x+1);
    writeNextIslandToClaimY((value).y+1);
  }
  private static int readMyIslandsX() throws GameActionException {
    return (rc.readSharedArray(33) & 8064) >>> 7;
  }

  private static void writeMyIslandsX(int value) throws GameActionException {
    rc.writeSharedArray(33, (rc.readSharedArray(33) & 57471) | (value << 7));
  }

  private static int readMyIslandsY() throws GameActionException {
    return (rc.readSharedArray(33) & 126) >>> 1;
  }

  private static void writeMyIslandsY(int value) throws GameActionException {
    rc.writeSharedArray(33, (rc.readSharedArray(33) & 65409) | (value << 1));
  }

  public static int readMyIslandsRoundNum() throws GameActionException {
    return ((rc.readSharedArray(33) & 1) << 10) + ((rc.readSharedArray(34) & 65472) >>> 6);
  }

  public static void writeMyIslandsRoundNum(int value) throws GameActionException {
    rc.writeSharedArray(33, (rc.readSharedArray(33) & 65534) | ((value & 1024) >>> 10));
    rc.writeSharedArray(34, (rc.readSharedArray(34) & 63) | ((value & 1023) << 6));
  }

  public static int readMyIslandsIslandId() throws GameActionException {
    return (rc.readSharedArray(34) & 63);
  }

  public static void writeMyIslandsIslandId(int value) throws GameActionException {
    rc.writeSharedArray(34, (rc.readSharedArray(34) & 65472) | (value));
  }

  public static MapLocation readMyIslandsLocation() throws GameActionException {
    return new MapLocation(readMyIslandsX()-1,readMyIslandsY()-1);
  }
  public static boolean readMyIslandsExists() throws GameActionException {
    return !((readMyIslandsLocation()).equals(NONEXISTENT_MAP_LOC));
  }
  public static void writeMyIslandsLocation(MapLocation value) throws GameActionException {
    writeMyIslandsX((value).x+1);
    writeMyIslandsY((value).y+1);
  }
  private static int readEnemyOddX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(35) & 64512) >>> 10;
      case 1:
          return (rc.readSharedArray(36) & 252) >>> 2;
      case 2:
          return (rc.readSharedArray(38) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(39) & 252) >>> 2;
      case 4:
          return (rc.readSharedArray(41) & 64512) >>> 10;
      case 5:
          return (rc.readSharedArray(42) & 252) >>> 2;
      case 6:
          return (rc.readSharedArray(44) & 64512) >>> 10;
      case 7:
          return (rc.readSharedArray(45) & 252) >>> 2;
      case 8:
          return (rc.readSharedArray(47) & 64512) >>> 10;
      case 9:
          return (rc.readSharedArray(48) & 252) >>> 2;
      case 10:
          return (rc.readSharedArray(50) & 64512) >>> 10;
      case 11:
          return (rc.readSharedArray(51) & 252) >>> 2;
      case 12:
          return (rc.readSharedArray(53) & 64512) >>> 10;
      case 13:
          return (rc.readSharedArray(54) & 252) >>> 2;
      case 14:
          return (rc.readSharedArray(56) & 64512) >>> 10;
      case 15:
          return (rc.readSharedArray(57) & 252) >>> 2;
      case 16:
          return (rc.readSharedArray(59) & 64512) >>> 10;
      case 17:
          return (rc.readSharedArray(60) & 252) >>> 2;
      case 18:
          return (rc.readSharedArray(62) & 64512) >>> 10;
      default:
          return -1;
    }
  }

  private static void writeEnemyOddX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 1023) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 65283) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 65283) | (value << 2));
        break;
      case 4:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 1023) | (value << 10));
        break;
      case 5:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 65283) | (value << 2));
        break;
      case 6:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 1023) | (value << 10));
        break;
      case 7:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65283) | (value << 2));
        break;
      case 8:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 1023) | (value << 10));
        break;
      case 9:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 65283) | (value << 2));
        break;
      case 10:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 1023) | (value << 10));
        break;
      case 11:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 65283) | (value << 2));
        break;
      case 12:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 1023) | (value << 10));
        break;
      case 13:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 65283) | (value << 2));
        break;
      case 14:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 1023) | (value << 10));
        break;
      case 15:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 65283) | (value << 2));
        break;
      case 16:
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 1023) | (value << 10));
        break;
      case 17:
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 65283) | (value << 2));
        break;
      case 18:
        rc.writeSharedArray(62, (rc.readSharedArray(62) & 1023) | (value << 10));
        break;
    }
  }

  private static int readEnemyOddY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(35) & 1008) >>> 4;
      case 1:
          return ((rc.readSharedArray(36) & 3) << 4) + ((rc.readSharedArray(37) & 61440) >>> 12);
      case 2:
          return (rc.readSharedArray(38) & 1008) >>> 4;
      case 3:
          return ((rc.readSharedArray(39) & 3) << 4) + ((rc.readSharedArray(40) & 61440) >>> 12);
      case 4:
          return (rc.readSharedArray(41) & 1008) >>> 4;
      case 5:
          return ((rc.readSharedArray(42) & 3) << 4) + ((rc.readSharedArray(43) & 61440) >>> 12);
      case 6:
          return (rc.readSharedArray(44) & 1008) >>> 4;
      case 7:
          return ((rc.readSharedArray(45) & 3) << 4) + ((rc.readSharedArray(46) & 61440) >>> 12);
      case 8:
          return (rc.readSharedArray(47) & 1008) >>> 4;
      case 9:
          return ((rc.readSharedArray(48) & 3) << 4) + ((rc.readSharedArray(49) & 61440) >>> 12);
      case 10:
          return (rc.readSharedArray(50) & 1008) >>> 4;
      case 11:
          return ((rc.readSharedArray(51) & 3) << 4) + ((rc.readSharedArray(52) & 61440) >>> 12);
      case 12:
          return (rc.readSharedArray(53) & 1008) >>> 4;
      case 13:
          return ((rc.readSharedArray(54) & 3) << 4) + ((rc.readSharedArray(55) & 61440) >>> 12);
      case 14:
          return (rc.readSharedArray(56) & 1008) >>> 4;
      case 15:
          return ((rc.readSharedArray(57) & 3) << 4) + ((rc.readSharedArray(58) & 61440) >>> 12);
      case 16:
          return (rc.readSharedArray(59) & 1008) >>> 4;
      case 17:
          return ((rc.readSharedArray(60) & 3) << 4) + ((rc.readSharedArray(61) & 61440) >>> 12);
      case 18:
          return (rc.readSharedArray(62) & 1008) >>> 4;
      default:
          return -1;
    }
  }

  private static void writeEnemyOddY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 4095) | ((value & 15) << 12));
        break;
      case 2:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 64527) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 4095) | ((value & 15) << 12));
        break;
      case 4:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 64527) | (value << 4));
        break;
      case 5:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 4095) | ((value & 15) << 12));
        break;
      case 6:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 64527) | (value << 4));
        break;
      case 7:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 4095) | ((value & 15) << 12));
        break;
      case 8:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 64527) | (value << 4));
        break;
      case 9:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 4095) | ((value & 15) << 12));
        break;
      case 10:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 64527) | (value << 4));
        break;
      case 11:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 4095) | ((value & 15) << 12));
        break;
      case 12:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 64527) | (value << 4));
        break;
      case 13:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 4095) | ((value & 15) << 12));
        break;
      case 14:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 64527) | (value << 4));
        break;
      case 15:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 4095) | ((value & 15) << 12));
        break;
      case 16:
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 64527) | (value << 4));
        break;
      case 17:
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(61, (rc.readSharedArray(61) & 4095) | ((value & 15) << 12));
        break;
      case 18:
        rc.writeSharedArray(62, (rc.readSharedArray(62) & 64527) | (value << 4));
        break;
    }
  }

  private static int readEnemyEvenX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(35) & 15) << 2) + ((rc.readSharedArray(36) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(37) & 4032) >>> 6;
      case 2:
          return ((rc.readSharedArray(38) & 15) << 2) + ((rc.readSharedArray(39) & 49152) >>> 14);
      case 3:
          return (rc.readSharedArray(40) & 4032) >>> 6;
      case 4:
          return ((rc.readSharedArray(41) & 15) << 2) + ((rc.readSharedArray(42) & 49152) >>> 14);
      case 5:
          return (rc.readSharedArray(43) & 4032) >>> 6;
      case 6:
          return ((rc.readSharedArray(44) & 15) << 2) + ((rc.readSharedArray(45) & 49152) >>> 14);
      case 7:
          return (rc.readSharedArray(46) & 4032) >>> 6;
      case 8:
          return ((rc.readSharedArray(47) & 15) << 2) + ((rc.readSharedArray(48) & 49152) >>> 14);
      case 9:
          return (rc.readSharedArray(49) & 4032) >>> 6;
      case 10:
          return ((rc.readSharedArray(50) & 15) << 2) + ((rc.readSharedArray(51) & 49152) >>> 14);
      case 11:
          return (rc.readSharedArray(52) & 4032) >>> 6;
      case 12:
          return ((rc.readSharedArray(53) & 15) << 2) + ((rc.readSharedArray(54) & 49152) >>> 14);
      case 13:
          return (rc.readSharedArray(55) & 4032) >>> 6;
      case 14:
          return ((rc.readSharedArray(56) & 15) << 2) + ((rc.readSharedArray(57) & 49152) >>> 14);
      case 15:
          return (rc.readSharedArray(58) & 4032) >>> 6;
      case 16:
          return ((rc.readSharedArray(59) & 15) << 2) + ((rc.readSharedArray(60) & 49152) >>> 14);
      case 17:
          return (rc.readSharedArray(61) & 4032) >>> 6;
      case 18:
          return ((rc.readSharedArray(62) & 15) << 2) + ((rc.readSharedArray(63) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 61503) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(38, (rc.readSharedArray(38) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 61503) | (value << 6));
        break;
      case 4:
        rc.writeSharedArray(41, (rc.readSharedArray(41) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 16383) | ((value & 3) << 14));
        break;
      case 5:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 61503) | (value << 6));
        break;
      case 6:
        rc.writeSharedArray(44, (rc.readSharedArray(44) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 16383) | ((value & 3) << 14));
        break;
      case 7:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 61503) | (value << 6));
        break;
      case 8:
        rc.writeSharedArray(47, (rc.readSharedArray(47) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 16383) | ((value & 3) << 14));
        break;
      case 9:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 61503) | (value << 6));
        break;
      case 10:
        rc.writeSharedArray(50, (rc.readSharedArray(50) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 16383) | ((value & 3) << 14));
        break;
      case 11:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 61503) | (value << 6));
        break;
      case 12:
        rc.writeSharedArray(53, (rc.readSharedArray(53) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 16383) | ((value & 3) << 14));
        break;
      case 13:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 61503) | (value << 6));
        break;
      case 14:
        rc.writeSharedArray(56, (rc.readSharedArray(56) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 16383) | ((value & 3) << 14));
        break;
      case 15:
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 61503) | (value << 6));
        break;
      case 16:
        rc.writeSharedArray(59, (rc.readSharedArray(59) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 16383) | ((value & 3) << 14));
        break;
      case 17:
        rc.writeSharedArray(61, (rc.readSharedArray(61) & 61503) | (value << 6));
        break;
      case 18:
        rc.writeSharedArray(62, (rc.readSharedArray(62) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(63, (rc.readSharedArray(63) & 16383) | ((value & 3) << 14));
        break;
    }
  }

  private static int readEnemyEvenY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(36) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(37) & 63);
      case 2:
          return (rc.readSharedArray(39) & 16128) >>> 8;
      case 3:
          return (rc.readSharedArray(40) & 63);
      case 4:
          return (rc.readSharedArray(42) & 16128) >>> 8;
      case 5:
          return (rc.readSharedArray(43) & 63);
      case 6:
          return (rc.readSharedArray(45) & 16128) >>> 8;
      case 7:
          return (rc.readSharedArray(46) & 63);
      case 8:
          return (rc.readSharedArray(48) & 16128) >>> 8;
      case 9:
          return (rc.readSharedArray(49) & 63);
      case 10:
          return (rc.readSharedArray(51) & 16128) >>> 8;
      case 11:
          return (rc.readSharedArray(52) & 63);
      case 12:
          return (rc.readSharedArray(54) & 16128) >>> 8;
      case 13:
          return (rc.readSharedArray(55) & 63);
      case 14:
          return (rc.readSharedArray(57) & 16128) >>> 8;
      case 15:
          return (rc.readSharedArray(58) & 63);
      case 16:
          return (rc.readSharedArray(60) & 16128) >>> 8;
      case 17:
          return (rc.readSharedArray(61) & 63);
      case 18:
          return (rc.readSharedArray(63) & 16128) >>> 8;
      default:
          return -1;
    }
  }

  private static void writeEnemyEvenY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(36, (rc.readSharedArray(36) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(37, (rc.readSharedArray(37) & 65472) | (value));
        break;
      case 2:
        rc.writeSharedArray(39, (rc.readSharedArray(39) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(40, (rc.readSharedArray(40) & 65472) | (value));
        break;
      case 4:
        rc.writeSharedArray(42, (rc.readSharedArray(42) & 49407) | (value << 8));
        break;
      case 5:
        rc.writeSharedArray(43, (rc.readSharedArray(43) & 65472) | (value));
        break;
      case 6:
        rc.writeSharedArray(45, (rc.readSharedArray(45) & 49407) | (value << 8));
        break;
      case 7:
        rc.writeSharedArray(46, (rc.readSharedArray(46) & 65472) | (value));
        break;
      case 8:
        rc.writeSharedArray(48, (rc.readSharedArray(48) & 49407) | (value << 8));
        break;
      case 9:
        rc.writeSharedArray(49, (rc.readSharedArray(49) & 65472) | (value));
        break;
      case 10:
        rc.writeSharedArray(51, (rc.readSharedArray(51) & 49407) | (value << 8));
        break;
      case 11:
        rc.writeSharedArray(52, (rc.readSharedArray(52) & 65472) | (value));
        break;
      case 12:
        rc.writeSharedArray(54, (rc.readSharedArray(54) & 49407) | (value << 8));
        break;
      case 13:
        rc.writeSharedArray(55, (rc.readSharedArray(55) & 65472) | (value));
        break;
      case 14:
        rc.writeSharedArray(57, (rc.readSharedArray(57) & 49407) | (value << 8));
        break;
      case 15:
        rc.writeSharedArray(58, (rc.readSharedArray(58) & 65472) | (value));
        break;
      case 16:
        rc.writeSharedArray(60, (rc.readSharedArray(60) & 49407) | (value << 8));
        break;
      case 17:
        rc.writeSharedArray(61, (rc.readSharedArray(61) & 65472) | (value));
        break;
      case 18:
        rc.writeSharedArray(63, (rc.readSharedArray(63) & 49407) | (value << 8));
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

    public int readWellCapacity(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return CommsHandler.readAdamantiumWellCapacity(idx);
        case MANA:
          return CommsHandler.readManaWellCapacity(idx);
        case ELIXIR:
          return CommsHandler.readElixirWellCapacity(idx);
        default:
          throw new RuntimeException("readWellCapacity not defined for " + this);
      }
    }

    public void writeWellCapacityReset(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCapacityReset(idx);
          break;
        case MANA:
          CommsHandler.writeManaWellCapacityReset(idx);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCapacityReset(idx);
          break;
        default:
          throw new RuntimeException("writeWellCapacityReset not defined for " + this);
      }
    }

    public void writeWellCapacitySet(int idx, int value) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCapacitySet(idx, value);
          break;
        case MANA:
          CommsHandler.writeManaWellCapacitySet(idx, value);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCapacitySet(idx, value);
          break;
        default:
          throw new RuntimeException("writeWellCapacitySet not defined for " + this);
      }
    }

    public void writeWellCapacityIncrement(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCapacityIncrement(idx);
          break;
        case MANA:
          CommsHandler.writeManaWellCapacityIncrement(idx);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCapacityIncrement(idx);
          break;
        default:
          throw new RuntimeException("writeWellCapacityIncrement not defined for " + this);
      }
    }

    public void writeWellCapacityDecrement(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCapacityDecrement(idx);
          break;
        case MANA:
          CommsHandler.writeManaWellCapacityDecrement(idx);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCapacityDecrement(idx);
          break;
        default:
          throw new RuntimeException("writeWellCapacityDecrement not defined for " + this);
      }
    }

    public int readWellCurrentWorkers(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return CommsHandler.readAdamantiumWellCurrentWorkers(idx);
        case MANA:
          return CommsHandler.readManaWellCurrentWorkers(idx);
        case ELIXIR:
          return CommsHandler.readElixirWellCurrentWorkers(idx);
        default:
          throw new RuntimeException("readWellCurrentWorkers not defined for " + this);
      }
    }

    public void writeWellCurrentWorkersReset(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCurrentWorkersReset(idx);
          break;
        case MANA:
          CommsHandler.writeManaWellCurrentWorkersReset(idx);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCurrentWorkersReset(idx);
          break;
        default:
          throw new RuntimeException("writeWellCurrentWorkersReset not defined for " + this);
      }
    }

    public void writeWellCurrentWorkersSet(int idx, int value) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCurrentWorkersSet(idx, value);
          break;
        case MANA:
          CommsHandler.writeManaWellCurrentWorkersSet(idx, value);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCurrentWorkersSet(idx, value);
          break;
        default:
          throw new RuntimeException("writeWellCurrentWorkersSet not defined for " + this);
      }
    }

    public void writeWellCurrentWorkersIncrement(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCurrentWorkersIncrement(idx);
          break;
        case MANA:
          CommsHandler.writeManaWellCurrentWorkersIncrement(idx);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCurrentWorkersIncrement(idx);
          break;
        default:
          throw new RuntimeException("writeWellCurrentWorkersIncrement not defined for " + this);
      }
    }

    public void writeWellCurrentWorkersDecrement(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          CommsHandler.writeAdamantiumWellCurrentWorkersDecrement(idx);
          break;
        case MANA:
          CommsHandler.writeManaWellCurrentWorkersDecrement(idx);
          break;
        case ELIXIR:
          CommsHandler.writeElixirWellCurrentWorkersDecrement(idx);
          break;
        default:
          throw new RuntimeException("writeWellCurrentWorkersDecrement not defined for " + this);
      }
    }

  }
}
