package basicbot.communications;

import basicbot.utils.Global;
import basicbot.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int ADAMANTIUM_WELL_SLOTS = 4;
  public static final int MANA_WELL_SLOTS = 4;
  public static final int ELIXIR_WELL_SLOTS = 4;
  public static final int ATTACK_POD_SLOTS = 10;

  RobotController rc;

  public CommsHandler(RobotController rc) throws GameActionException {
    this.rc = rc;
  }


  public int readHqCount() throws GameActionException {
    return (rc.readSharedArray(0) & 49152) >>> 14;
  }

  public void writeHqCount(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 16383) | (value << 14));
  }

  public int readMapSymmetry() throws GameActionException {
    return (rc.readSharedArray(0) & 14336) >>> 11;
  }

  public void writeMapSymmetry(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 51199) | (value << 11));
  }

  private int readOurHqX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(0) & 2016) >>> 5;
      case 1:
          return (rc.readSharedArray(1) & 32256) >>> 9;
      case 2:
          return ((rc.readSharedArray(1) & 7) << 3) + ((rc.readSharedArray(2) & 57344) >>> 13);
      case 3:
          return (rc.readSharedArray(2) & 126) >>> 1;
      default:
          return -1;
    }
  }

  private void writeOurHqX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 63519) | (value << 5));
        break;
      case 1:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 33279) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 8191) | ((value & 7) << 13));
        break;
      case 3:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65409) | (value << 1));
        break;
    }
  }

  private int readOurHqY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(0) & 31) << 1) + ((rc.readSharedArray(1) & 32768) >>> 15);
      case 1:
          return (rc.readSharedArray(1) & 504) >>> 3;
      case 2:
          return (rc.readSharedArray(2) & 8064) >>> 7;
      case 3:
          return ((rc.readSharedArray(2) & 1) << 5) + ((rc.readSharedArray(3) & 63488) >>> 11);
      default:
          return -1;
    }
  }

  private void writeOurHqY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 32767) | ((value & 1) << 15));
        break;
      case 1:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65031) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 57471) | (value << 7));
        break;
      case 3:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 2047) | ((value & 31) << 11));
        break;
    }
  }

  public MapLocation readOurHqLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqX(idx)-1,readOurHqY(idx)-1);
  }
  public boolean readOurHqExists(int idx) throws GameActionException {
    return !((readOurHqLocation(idx)).equals(new MapLocation(0,0)));
  }
  public void writeOurHqLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqX(idx, (value).x+1);
    writeOurHqY(idx, (value).y+1);
  }
  private int readAdamantiumWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 2016) >>> 5;
      case 1:
          return (rc.readSharedArray(4) & 16128) >>> 8;
      case 2:
          return ((rc.readSharedArray(4) & 1) << 5) + ((rc.readSharedArray(5) & 63488) >>> 11);
      case 3:
          return ((rc.readSharedArray(5) & 15) << 2) + ((rc.readSharedArray(6) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  private void writeAdamantiumWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 63519) | (value << 5));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 49407) | (value << 8));
        break;
      case 2:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 2047) | ((value & 31) << 11));
        break;
      case 3:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 16383) | ((value & 3) << 14));
        break;
    }
  }

  private int readAdamantiumWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(3) & 31) << 1) + ((rc.readSharedArray(4) & 32768) >>> 15);
      case 1:
          return (rc.readSharedArray(4) & 252) >>> 2;
      case 2:
          return (rc.readSharedArray(5) & 2016) >>> 5;
      case 3:
          return (rc.readSharedArray(6) & 16128) >>> 8;
      default:
          return -1;
    }
  }

  private void writeAdamantiumWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 32767) | ((value & 1) << 15));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65283) | (value << 2));
        break;
      case 2:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 63519) | (value << 5));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 49407) | (value << 8));
        break;
    }
  }

  private int readAdamantiumWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(4) & 16384) >>> 14;
      case 1:
          return (rc.readSharedArray(4) & 2) >>> 1;
      case 2:
          return (rc.readSharedArray(5) & 16) >>> 4;
      case 3:
          return (rc.readSharedArray(6) & 128) >>> 7;
      default:
          return -1;
    }
  }

  private void writeAdamantiumWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 49151) | (value << 14));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65533) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65519) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65407) | (value << 7));
        break;
    }
  }

  public MapLocation readAdamantiumWellLocation(int idx) throws GameActionException {
    return new MapLocation(readAdamantiumWellX(idx)-1,readAdamantiumWellY(idx)-1);
  }
  public boolean readAdamantiumWellExists(int idx) throws GameActionException {
    return !((readAdamantiumWellLocation(idx)).equals(new MapLocation(0,0)));
  }
  public void writeAdamantiumWellLocation(int idx, MapLocation value) throws GameActionException {
    writeAdamantiumWellX(idx, (value).x+1);
    writeAdamantiumWellY(idx, (value).y+1);
  }
  public boolean readAdamantiumWellUpgraded(int idx) throws GameActionException {
    return ((readAdamantiumWellUpgradedBit(idx)) == 1);
  }
  public void writeAdamantiumWellUpgraded(int idx, boolean value) throws GameActionException {
    writeAdamantiumWellUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private int readManaWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(6) & 126) >>> 1;
      case 1:
          return (rc.readSharedArray(7) & 1008) >>> 4;
      case 2:
          return (rc.readSharedArray(8) & 8064) >>> 7;
      case 3:
          return (rc.readSharedArray(9) & 64512) >>> 10;
      default:
          return -1;
    }
  }

  private void writeManaWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65409) | (value << 1));
        break;
      case 1:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 57471) | (value << 7));
        break;
      case 3:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 1023) | (value << 10));
        break;
    }
  }

  private int readManaWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(6) & 1) << 5) + ((rc.readSharedArray(7) & 63488) >>> 11);
      case 1:
          return ((rc.readSharedArray(7) & 15) << 2) + ((rc.readSharedArray(8) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(8) & 126) >>> 1;
      case 3:
          return (rc.readSharedArray(9) & 1008) >>> 4;
      default:
          return -1;
    }
  }

  private void writeManaWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 2047) | ((value & 31) << 11));
        break;
      case 1:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65409) | (value << 1));
        break;
      case 3:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 64527) | (value << 4));
        break;
    }
  }

  private int readManaWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(7) & 1024) >>> 10;
      case 1:
          return (rc.readSharedArray(8) & 8192) >>> 13;
      case 2:
          return (rc.readSharedArray(8) & 1);
      case 3:
          return (rc.readSharedArray(9) & 8) >>> 3;
      default:
          return -1;
    }
  }

  private void writeManaWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 64511) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 57343) | (value << 13));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65534) | (value));
        break;
      case 3:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65527) | (value << 3));
        break;
    }
  }

  public MapLocation readManaWellLocation(int idx) throws GameActionException {
    return new MapLocation(readManaWellX(idx)-1,readManaWellY(idx)-1);
  }
  public boolean readManaWellExists(int idx) throws GameActionException {
    return !((readManaWellLocation(idx)).equals(new MapLocation(0,0)));
  }
  public void writeManaWellLocation(int idx, MapLocation value) throws GameActionException {
    writeManaWellX(idx, (value).x+1);
    writeManaWellY(idx, (value).y+1);
  }
  public boolean readManaWellUpgraded(int idx) throws GameActionException {
    return ((readManaWellUpgradedBit(idx)) == 1);
  }
  public void writeManaWellUpgraded(int idx, boolean value) throws GameActionException {
    writeManaWellUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private int readElixirWellX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(9) & 7) << 3) + ((rc.readSharedArray(10) & 57344) >>> 13);
      case 1:
          return (rc.readSharedArray(10) & 63);
      case 2:
          return (rc.readSharedArray(11) & 504) >>> 3;
      case 3:
          return (rc.readSharedArray(12) & 4032) >>> 6;
      default:
          return -1;
    }
  }

  private void writeElixirWellX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 8191) | ((value & 7) << 13));
        break;
      case 1:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65472) | (value));
        break;
      case 2:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65031) | (value << 3));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 61503) | (value << 6));
        break;
    }
  }

  private int readElixirWellY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(10) & 8064) >>> 7;
      case 1:
          return (rc.readSharedArray(11) & 64512) >>> 10;
      case 2:
          return ((rc.readSharedArray(11) & 7) << 3) + ((rc.readSharedArray(12) & 57344) >>> 13);
      case 3:
          return (rc.readSharedArray(12) & 63);
      default:
          return -1;
    }
  }

  private void writeElixirWellY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 57471) | (value << 7));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 1023) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 8191) | ((value & 7) << 13));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65472) | (value));
        break;
    }
  }

  private int readElixirWellUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(10) & 64) >>> 6;
      case 1:
          return (rc.readSharedArray(11) & 512) >>> 9;
      case 2:
          return (rc.readSharedArray(12) & 4096) >>> 12;
      case 3:
          return (rc.readSharedArray(13) & 32768) >>> 15;
      default:
          return -1;
    }
  }

  private void writeElixirWellUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65471) | (value << 6));
        break;
      case 1:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65023) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 61439) | (value << 12));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 32767) | (value << 15));
        break;
    }
  }

  public MapLocation readElixirWellLocation(int idx) throws GameActionException {
    return new MapLocation(readElixirWellX(idx)-1,readElixirWellY(idx)-1);
  }
  public boolean readElixirWellExists(int idx) throws GameActionException {
    return !((readElixirWellLocation(idx)).equals(new MapLocation(0,0)));
  }
  public void writeElixirWellLocation(int idx, MapLocation value) throws GameActionException {
    writeElixirWellX(idx, (value).x+1);
    writeElixirWellY(idx, (value).y+1);
  }
  public boolean readElixirWellUpgraded(int idx) throws GameActionException {
    return ((readElixirWellUpgradedBit(idx)) == 1);
  }
  public void writeElixirWellUpgraded(int idx, boolean value) throws GameActionException {
    writeElixirWellUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private int readAttackPodAmpAliveBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 16384) >>> 14;
      case 1:
          return (rc.readSharedArray(14) & 2048) >>> 11;
      case 2:
          return (rc.readSharedArray(15) & 256) >>> 8;
      case 3:
          return (rc.readSharedArray(16) & 32) >>> 5;
      case 4:
          return (rc.readSharedArray(17) & 4) >>> 2;
      case 5:
          return (rc.readSharedArray(19) & 32768) >>> 15;
      case 6:
          return (rc.readSharedArray(20) & 4096) >>> 12;
      case 7:
          return (rc.readSharedArray(21) & 512) >>> 9;
      case 8:
          return (rc.readSharedArray(22) & 64) >>> 6;
      case 9:
          return (rc.readSharedArray(23) & 8) >>> 3;
      default:
          return -1;
    }
  }

  private void writeAttackPodAmpAliveBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 49151) | (value << 14));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 63487) | (value << 11));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65279) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65503) | (value << 5));
        break;
      case 4:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65531) | (value << 2));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 32767) | (value << 15));
        break;
      case 6:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 61439) | (value << 12));
        break;
      case 7:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65023) | (value << 9));
        break;
      case 8:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65471) | (value << 6));
        break;
      case 9:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65527) | (value << 3));
        break;
    }
  }

  private int readAttackPodAmpX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(14) & 2016) >>> 5;
      case 2:
          return (rc.readSharedArray(15) & 252) >>> 2;
      case 3:
          return ((rc.readSharedArray(16) & 31) << 1) + ((rc.readSharedArray(17) & 32768) >>> 15);
      case 4:
          return ((rc.readSharedArray(17) & 3) << 4) + ((rc.readSharedArray(18) & 61440) >>> 12);
      case 5:
          return (rc.readSharedArray(19) & 32256) >>> 9;
      case 6:
          return (rc.readSharedArray(20) & 4032) >>> 6;
      case 7:
          return (rc.readSharedArray(21) & 504) >>> 3;
      case 8:
          return (rc.readSharedArray(22) & 63);
      case 9:
          return ((rc.readSharedArray(23) & 7) << 3) + ((rc.readSharedArray(24) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  private void writeAttackPodAmpX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 63519) | (value << 5));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65283) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 32767) | ((value & 1) << 15));
        break;
      case 4:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 4095) | ((value & 15) << 12));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 33279) | (value << 9));
        break;
      case 6:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 61503) | (value << 6));
        break;
      case 7:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65031) | (value << 3));
        break;
      case 8:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65472) | (value));
        break;
      case 9:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  private int readAttackPodAmpY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 252) >>> 2;
      case 1:
          return ((rc.readSharedArray(14) & 31) << 1) + ((rc.readSharedArray(15) & 32768) >>> 15);
      case 2:
          return ((rc.readSharedArray(15) & 3) << 4) + ((rc.readSharedArray(16) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(17) & 32256) >>> 9;
      case 4:
          return (rc.readSharedArray(18) & 4032) >>> 6;
      case 5:
          return (rc.readSharedArray(19) & 504) >>> 3;
      case 6:
          return (rc.readSharedArray(20) & 63);
      case 7:
          return ((rc.readSharedArray(21) & 7) << 3) + ((rc.readSharedArray(22) & 57344) >>> 13);
      case 8:
          return (rc.readSharedArray(23) & 64512) >>> 10;
      case 9:
          return (rc.readSharedArray(24) & 8064) >>> 7;
      default:
          return -1;
    }
  }

  private void writeAttackPodAmpY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 32767) | ((value & 1) << 15));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 33279) | (value << 9));
        break;
      case 4:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 61503) | (value << 6));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65031) | (value << 3));
        break;
      case 6:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65472) | (value));
        break;
      case 7:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 8191) | ((value & 7) << 13));
        break;
      case 8:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 1023) | (value << 10));
        break;
      case 9:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 57471) | (value << 7));
        break;
    }
  }

  public int readAttackPodLauncherRegistry(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(13) & 3) << 4) + ((rc.readSharedArray(14) & 61440) >>> 12);
      case 1:
          return (rc.readSharedArray(15) & 32256) >>> 9;
      case 2:
          return (rc.readSharedArray(16) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(17) & 504) >>> 3;
      case 4:
          return (rc.readSharedArray(18) & 63);
      case 5:
          return ((rc.readSharedArray(19) & 7) << 3) + ((rc.readSharedArray(20) & 57344) >>> 13);
      case 6:
          return (rc.readSharedArray(21) & 64512) >>> 10;
      case 7:
          return (rc.readSharedArray(22) & 8064) >>> 7;
      case 8:
          return (rc.readSharedArray(23) & 1008) >>> 4;
      case 9:
          return (rc.readSharedArray(24) & 126) >>> 1;
      default:
          return -1;
    }
  }

  public void writeAttackPodLauncherRegistry(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 4095) | ((value & 15) << 12));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 33279) | (value << 9));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65031) | (value << 3));
        break;
      case 4:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65472) | (value));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 8191) | ((value & 7) << 13));
        break;
      case 6:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 1023) | (value << 10));
        break;
      case 7:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 57471) | (value << 7));
        break;
      case 8:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 64527) | (value << 4));
        break;
      case 9:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65409) | (value << 1));
        break;
    }
  }

  public boolean readAttackPodAmpAlive(int idx) throws GameActionException {
    return ((readAttackPodAmpAliveBit(idx)) == 1);
  }
  public void writeAttackPodAmpAlive(int idx, boolean value) throws GameActionException {
    writeAttackPodAmpAliveBit(idx, ((value) ? 1 : 0));
  }
  public MapLocation readAttackPodAmpLocation(int idx) throws GameActionException {
    return new MapLocation(readAttackPodAmpX(idx)-1,readAttackPodAmpY(idx)-1);
  }
  public boolean readAttackPodAmpExists(int idx) throws GameActionException {
    return !((readAttackPodAmpLocation(idx)).equals(new MapLocation(0,0)));
  }
  public void writeAttackPodAmpLocation(int idx, MapLocation value) throws GameActionException {
    writeAttackPodAmpX(idx, (value).x+1);
    writeAttackPodAmpY(idx, (value).y+1);
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
          return Global.communicator.commsHandler.readAdamantiumWellLocation(idx);
        case MANA:
          return Global.communicator.commsHandler.readManaWellLocation(idx);
        case ELIXIR:
          return Global.communicator.commsHandler.readElixirWellLocation(idx);
        default:
          throw new RuntimeException("readWellLocation not defined for " + this);
      }
    }

    public boolean readWellExists(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return Global.communicator.commsHandler.readAdamantiumWellExists(idx);
        case MANA:
          return Global.communicator.commsHandler.readManaWellExists(idx);
        case ELIXIR:
          return Global.communicator.commsHandler.readElixirWellExists(idx);
        default:
          throw new RuntimeException("readWellExists not defined for " + this);
      }
    }

    public boolean readWellUpgraded(int idx) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          return Global.communicator.commsHandler.readAdamantiumWellUpgraded(idx);
        case MANA:
          return Global.communicator.commsHandler.readManaWellUpgraded(idx);
        case ELIXIR:
          return Global.communicator.commsHandler.readElixirWellUpgraded(idx);
        default:
          throw new RuntimeException("readWellUpgraded not defined for " + this);
      }
    }

  }
}