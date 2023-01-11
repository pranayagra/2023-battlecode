package basicbot.communications;

import basicbot.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int ATTACK_PODS_SLOTS = 10;

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

  private int readOurHqExistsBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(0) & 1024) >>> 10;
      case 1:
          return (rc.readSharedArray(3) & 64) >>> 6;
      case 2:
          return (rc.readSharedArray(6) & 4) >>> 2;
      case 3:
          return (rc.readSharedArray(10) & 16384) >>> 14;
      default:
          return -1;
    }
  }

  private void writeOurHqExistsBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 64511) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65471) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65531) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 49151) | (value << 14));
        break;
    }
  }

  private int readOurHqX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(0) & 1008) >>> 4;
      case 1:
          return (rc.readSharedArray(3) & 63);
      case 2:
          return ((rc.readSharedArray(6) & 3) << 4) + ((rc.readSharedArray(7) & 61440) >>> 12);
      case 3:
          return (rc.readSharedArray(10) & 16128) >>> 8;
      default:
          return -1;
    }
  }

  private void writeOurHqX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65472) | (value));
        break;
      case 2:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 4095) | ((value & 15) << 12));
        break;
      case 3:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 49407) | (value << 8));
        break;
    }
  }

  private int readOurHqY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(0) & 15) << 2) + ((rc.readSharedArray(1) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(4) & 64512) >>> 10;
      case 2:
          return (rc.readSharedArray(7) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(10) & 252) >>> 2;
      default:
          return -1;
    }
  }

  private void writeOurHqY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(0, (rc.readSharedArray(0) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 1023) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65283) | (value << 2));
        break;
    }
  }

  private int readOurHqClosestAdamantiumX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(4) & 1008) >>> 4;
      case 2:
          return (rc.readSharedArray(7) & 63);
      case 3:
          return ((rc.readSharedArray(10) & 3) << 4) + ((rc.readSharedArray(11) & 61440) >>> 12);
      default:
          return -1;
    }
  }

  private void writeOurHqClosestAdamantiumX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 4095) | ((value & 15) << 12));
        break;
    }
  }

  private int readOurHqClosestAdamantiumY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 252) >>> 2;
      case 1:
          return ((rc.readSharedArray(4) & 15) << 2) + ((rc.readSharedArray(5) & 49152) >>> 14);
      case 2:
          return (rc.readSharedArray(8) & 64512) >>> 10;
      case 3:
          return (rc.readSharedArray(11) & 4032) >>> 6;
      default:
          return -1;
    }
  }

  private void writeOurHqClosestAdamantiumY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(4, (rc.readSharedArray(4) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 16383) | ((value & 3) << 14));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 61503) | (value << 6));
        break;
    }
  }

  private int readOurHqAdamantiumUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(1) & 2) >>> 1;
      case 1:
          return (rc.readSharedArray(5) & 8192) >>> 13;
      case 2:
          return (rc.readSharedArray(8) & 512) >>> 9;
      case 3:
          return (rc.readSharedArray(11) & 32) >>> 5;
      default:
          return -1;
    }
  }

  private void writeOurHqAdamantiumUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65533) | (value << 1));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 57343) | (value << 13));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65023) | (value << 9));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65503) | (value << 5));
        break;
    }
  }

  private int readOurHqClosestManaX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(1) & 1) << 5) + ((rc.readSharedArray(2) & 63488) >>> 11);
      case 1:
          return (rc.readSharedArray(5) & 8064) >>> 7;
      case 2:
          return (rc.readSharedArray(8) & 504) >>> 3;
      case 3:
          return ((rc.readSharedArray(11) & 31) << 1) + ((rc.readSharedArray(12) & 32768) >>> 15);
      default:
          return -1;
    }
  }

  private void writeOurHqClosestManaX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(1, (rc.readSharedArray(1) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 2047) | ((value & 31) << 11));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 57471) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65031) | (value << 3));
        break;
      case 3:
        rc.writeSharedArray(11, (rc.readSharedArray(11) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 32767) | ((value & 1) << 15));
        break;
    }
  }

  private int readOurHqClosestManaY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 2016) >>> 5;
      case 1:
          return (rc.readSharedArray(5) & 126) >>> 1;
      case 2:
          return ((rc.readSharedArray(8) & 7) << 3) + ((rc.readSharedArray(9) & 57344) >>> 13);
      case 3:
          return (rc.readSharedArray(12) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private void writeOurHqClosestManaY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 63519) | (value << 5));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(8, (rc.readSharedArray(8) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 8191) | ((value & 7) << 13));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 33279) | (value << 9));
        break;
    }
  }

  private int readOurHqManaUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(2) & 16) >>> 4;
      case 1:
          return (rc.readSharedArray(5) & 1);
      case 2:
          return (rc.readSharedArray(9) & 4096) >>> 12;
      case 3:
          return (rc.readSharedArray(12) & 256) >>> 8;
      default:
          return -1;
    }
  }

  private void writeOurHqManaUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65519) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(5, (rc.readSharedArray(5) & 65534) | (value));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 61439) | (value << 12));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65279) | (value << 8));
        break;
    }
  }

  private int readOurHqClosestElixirX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(2) & 15) << 2) + ((rc.readSharedArray(3) & 49152) >>> 14);
      case 1:
          return (rc.readSharedArray(6) & 64512) >>> 10;
      case 2:
          return (rc.readSharedArray(9) & 4032) >>> 6;
      case 3:
          return (rc.readSharedArray(12) & 252) >>> 2;
      default:
          return -1;
    }
  }

  private void writeOurHqClosestElixirX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(2, (rc.readSharedArray(2) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 1023) | (value << 10));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 61503) | (value << 6));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65283) | (value << 2));
        break;
    }
  }

  private int readOurHqClosestElixirY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(6) & 1008) >>> 4;
      case 2:
          return (rc.readSharedArray(9) & 63);
      case 3:
          return ((rc.readSharedArray(12) & 3) << 4) + ((rc.readSharedArray(13) & 61440) >>> 12);
      default:
          return -1;
    }
  }

  private void writeOurHqClosestElixirY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 64527) | (value << 4));
        break;
      case 2:
        rc.writeSharedArray(9, (rc.readSharedArray(9) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(12, (rc.readSharedArray(12) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 4095) | ((value & 15) << 12));
        break;
    }
  }

  private int readOurHqElixirUpgradedBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(3) & 128) >>> 7;
      case 1:
          return (rc.readSharedArray(6) & 8) >>> 3;
      case 2:
          return (rc.readSharedArray(10) & 32768) >>> 15;
      case 3:
          return (rc.readSharedArray(13) & 2048) >>> 11;
      default:
          return -1;
    }
  }

  private void writeOurHqElixirUpgradedBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(3, (rc.readSharedArray(3) & 65407) | (value << 7));
        break;
      case 1:
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65527) | (value << 3));
        break;
      case 2:
        rc.writeSharedArray(10, (rc.readSharedArray(10) & 32767) | (value << 15));
        break;
      case 3:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 63487) | (value << 11));
        break;
    }
  }

  public boolean readOurHqExists(int idx) throws GameActionException {
    return ((readOurHqExistsBit(idx)) == 1);
  }
  public void writeOurHqExists(int idx, boolean value) throws GameActionException {
    writeOurHqExistsBit(idx, ((value) ? 1 : 0));
  }
  public MapLocation readOurHqLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqX(idx), readOurHqY(idx));
  }
  public void writeOurHqLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqX(idx, (value).x);
    writeOurHqY(idx, (value).y);
  }
  public MapLocation readOurHqClosestAdamantiumLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqClosestAdamantiumX(idx), readOurHqClosestAdamantiumY(idx));
  }
  public void writeOurHqClosestAdamantiumLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqClosestAdamantiumX(idx, (value).x);
    writeOurHqClosestAdamantiumY(idx, (value).y);
  }
  public boolean readOurHqAdamantiumUpgraded(int idx) throws GameActionException {
    return ((readOurHqAdamantiumUpgradedBit(idx)) == 1);
  }
  public void writeOurHqAdamantiumUpgraded(int idx, boolean value) throws GameActionException {
    writeOurHqAdamantiumUpgradedBit(idx, ((value) ? 1 : 0));
  }
  public MapLocation readOurHqClosestManaLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqClosestManaX(idx), readOurHqClosestManaY(idx));
  }
  public void writeOurHqClosestManaLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqClosestManaX(idx, (value).x);
    writeOurHqClosestManaY(idx, (value).y);
  }
  public boolean readOurHqManaUpgraded(int idx) throws GameActionException {
    return ((readOurHqManaUpgradedBit(idx)) == 1);
  }
  public void writeOurHqManaUpgraded(int idx, boolean value) throws GameActionException {
    writeOurHqManaUpgradedBit(idx, ((value) ? 1 : 0));
  }
  public MapLocation readOurHqClosestElixirLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqClosestElixirX(idx), readOurHqClosestElixirY(idx));
  }
  public void writeOurHqClosestElixirLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqClosestElixirX(idx, (value).x);
    writeOurHqClosestElixirY(idx, (value).y);
  }
  public boolean readOurHqElixirUpgraded(int idx) throws GameActionException {
    return ((readOurHqElixirUpgradedBit(idx)) == 1);
  }
  public void writeOurHqElixirUpgraded(int idx, boolean value) throws GameActionException {
    writeOurHqElixirUpgradedBit(idx, ((value) ? 1 : 0));
  }
  private int readAttackPodsAmpAliveBit(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 1024) >>> 10;
      case 1:
          return (rc.readSharedArray(14) & 128) >>> 7;
      case 2:
          return (rc.readSharedArray(15) & 16) >>> 4;
      case 3:
          return (rc.readSharedArray(16) & 2) >>> 1;
      case 4:
          return (rc.readSharedArray(18) & 16384) >>> 14;
      case 5:
          return (rc.readSharedArray(19) & 2048) >>> 11;
      case 6:
          return (rc.readSharedArray(20) & 256) >>> 8;
      case 7:
          return (rc.readSharedArray(21) & 32) >>> 5;
      case 8:
          return (rc.readSharedArray(22) & 4) >>> 2;
      case 9:
          return (rc.readSharedArray(24) & 32768) >>> 15;
      default:
          return -1;
    }
  }

  private void writeAttackPodsAmpAliveBit(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 64511) | (value << 10));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65407) | (value << 7));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65519) | (value << 4));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65533) | (value << 1));
        break;
      case 4:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 49151) | (value << 14));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 63487) | (value << 11));
        break;
      case 6:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65279) | (value << 8));
        break;
      case 7:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65503) | (value << 5));
        break;
      case 8:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65531) | (value << 2));
        break;
      case 9:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 32767) | (value << 15));
        break;
    }
  }

  private int readAttackPodsAmpX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 1008) >>> 4;
      case 1:
          return (rc.readSharedArray(14) & 126) >>> 1;
      case 2:
          return ((rc.readSharedArray(15) & 15) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
      case 3:
          return ((rc.readSharedArray(16) & 1) << 5) + ((rc.readSharedArray(17) & 63488) >>> 11);
      case 4:
          return (rc.readSharedArray(18) & 16128) >>> 8;
      case 5:
          return (rc.readSharedArray(19) & 2016) >>> 5;
      case 6:
          return (rc.readSharedArray(20) & 252) >>> 2;
      case 7:
          return ((rc.readSharedArray(21) & 31) << 1) + ((rc.readSharedArray(22) & 32768) >>> 15);
      case 8:
          return ((rc.readSharedArray(22) & 3) << 4) + ((rc.readSharedArray(23) & 61440) >>> 12);
      case 9:
          return (rc.readSharedArray(24) & 32256) >>> 9;
      default:
          return -1;
    }
  }

  private void writeAttackPodsAmpX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 64527) | (value << 4));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65409) | (value << 1));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
        break;
      case 3:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 2047) | ((value & 31) << 11));
        break;
      case 4:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 49407) | (value << 8));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 63519) | (value << 5));
        break;
      case 6:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65283) | (value << 2));
        break;
      case 7:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 32767) | ((value & 1) << 15));
        break;
      case 8:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 4095) | ((value & 15) << 12));
        break;
      case 9:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 33279) | (value << 9));
        break;
    }
  }

  private int readAttackPodsAmpY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return ((rc.readSharedArray(13) & 15) << 2) + ((rc.readSharedArray(14) & 49152) >>> 14);
      case 1:
          return ((rc.readSharedArray(14) & 1) << 5) + ((rc.readSharedArray(15) & 63488) >>> 11);
      case 2:
          return (rc.readSharedArray(16) & 16128) >>> 8;
      case 3:
          return (rc.readSharedArray(17) & 2016) >>> 5;
      case 4:
          return (rc.readSharedArray(18) & 252) >>> 2;
      case 5:
          return ((rc.readSharedArray(19) & 31) << 1) + ((rc.readSharedArray(20) & 32768) >>> 15);
      case 6:
          return ((rc.readSharedArray(20) & 3) << 4) + ((rc.readSharedArray(21) & 61440) >>> 12);
      case 7:
          return (rc.readSharedArray(22) & 32256) >>> 9;
      case 8:
          return (rc.readSharedArray(23) & 4032) >>> 6;
      case 9:
          return (rc.readSharedArray(24) & 504) >>> 3;
      default:
          return -1;
    }
  }

  private void writeAttackPodsAmpY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 16383) | ((value & 3) << 14));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65534) | ((value & 32) >>> 5));
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 2047) | ((value & 31) << 11));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 49407) | (value << 8));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 63519) | (value << 5));
        break;
      case 4:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65283) | (value << 2));
        break;
      case 5:
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 32767) | ((value & 1) << 15));
        break;
      case 6:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 4095) | ((value & 15) << 12));
        break;
      case 7:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 33279) | (value << 9));
        break;
      case 8:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 61503) | (value << 6));
        break;
      case 9:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65031) | (value << 3));
        break;
    }
  }

  public int readAttackPodsLauncherRegistry(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(14) & 16128) >>> 8;
      case 1:
          return (rc.readSharedArray(15) & 2016) >>> 5;
      case 2:
          return (rc.readSharedArray(16) & 252) >>> 2;
      case 3:
          return ((rc.readSharedArray(17) & 31) << 1) + ((rc.readSharedArray(18) & 32768) >>> 15);
      case 4:
          return ((rc.readSharedArray(18) & 3) << 4) + ((rc.readSharedArray(19) & 61440) >>> 12);
      case 5:
          return (rc.readSharedArray(20) & 32256) >>> 9;
      case 6:
          return (rc.readSharedArray(21) & 4032) >>> 6;
      case 7:
          return (rc.readSharedArray(22) & 504) >>> 3;
      case 8:
          return (rc.readSharedArray(23) & 63);
      case 9:
          return ((rc.readSharedArray(24) & 7) << 3) + ((rc.readSharedArray(25) & 57344) >>> 13);
      default:
          return -1;
    }
  }

  public void writeAttackPodsLauncherRegistry(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 63519) | (value << 5));
        break;
      case 2:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65283) | (value << 2));
        break;
      case 3:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65504) | ((value & 62) >>> 1));
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 32767) | ((value & 1) << 15));
        break;
      case 4:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 4095) | ((value & 15) << 12));
        break;
      case 5:
        rc.writeSharedArray(20, (rc.readSharedArray(20) & 33279) | (value << 9));
        break;
      case 6:
        rc.writeSharedArray(21, (rc.readSharedArray(21) & 61503) | (value << 6));
        break;
      case 7:
        rc.writeSharedArray(22, (rc.readSharedArray(22) & 65031) | (value << 3));
        break;
      case 8:
        rc.writeSharedArray(23, (rc.readSharedArray(23) & 65472) | (value));
        break;
      case 9:
        rc.writeSharedArray(24, (rc.readSharedArray(24) & 65528) | ((value & 56) >>> 3));
        rc.writeSharedArray(25, (rc.readSharedArray(25) & 8191) | ((value & 7) << 13));
        break;
    }
  }

  public boolean readAttackPodsAmpAlive(int idx) throws GameActionException {
    return ((readAttackPodsAmpAliveBit(idx)) == 1);
  }
  public void writeAttackPodsAmpAlive(int idx, boolean value) throws GameActionException {
    writeAttackPodsAmpAliveBit(idx, ((value) ? 1 : 0));
  }
  public MapLocation readAttackPodsAmpLocation(int idx) throws GameActionException {
    return new MapLocation(readAttackPodsAmpX(idx), readAttackPodsAmpY(idx));
  }
  public void writeAttackPodsAmpLocation(int idx, MapLocation value) throws GameActionException {
    writeAttackPodsAmpX(idx, (value).x);
    writeAttackPodsAmpY(idx, (value).y);
  }
}