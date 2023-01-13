package actualcarrierecon.communications;

import actualcarrierecon.utils.Global;
import actualcarrierecon.utils.Utils;
import battlecode.common.*;


public class CommsHandler {


  public static final int META_SLOTS = 1;
  public static final int OUR_HQ_SLOTS = 4;
  public static final int ADAMANTIUM_WELL_SLOTS = 4;
  public static final int MANA_WELL_SLOTS = 4;
  public static final int ELIXIR_WELL_SLOTS = 4;
  public static final int ATTACK_TARGET_SLOTS = 8;
  private static final MapLocation NONEXISTENT_MAP_LOC = new MapLocation(-1,-1);


  RobotController rc;

  public CommsHandler(RobotController rc) throws GameActionException {
    this.rc = rc;
  }


  public int readHqCount() throws GameActionException {
    return (rc.readSharedArray(0) & 57344) >>> 13;
  }

  public void writeHqCount(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 8191) | (value << 13));
  }

  public int readMapSymmetry() throws GameActionException {
    return (rc.readSharedArray(0) & 7168) >>> 10;
  }

  public void writeMapSymmetry(int value) throws GameActionException {
    rc.writeSharedArray(0, (rc.readSharedArray(0) & 58367) | (value << 10));
  }

  private int readOurHqX(int idx) throws GameActionException {
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

  private void writeOurHqX(int idx, int value) throws GameActionException {
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

  private int readOurHqY(int idx) throws GameActionException {
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

  private void writeOurHqY(int idx, int value) throws GameActionException {
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

  public MapLocation readOurHqLocation(int idx) throws GameActionException {
    return new MapLocation(readOurHqX(idx)-1,readOurHqY(idx)-1);
  }
  public boolean readOurHqExists(int idx) throws GameActionException {
    return !((readOurHqLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public void writeOurHqLocation(int idx, MapLocation value) throws GameActionException {
    writeOurHqX(idx, (value).x+1);
    writeOurHqY(idx, (value).y+1);
  }
  private int readAdamantiumWellX(int idx) throws GameActionException {
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

  private void writeAdamantiumWellX(int idx, int value) throws GameActionException {
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

  private int readAdamantiumWellY(int idx) throws GameActionException {
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

  private void writeAdamantiumWellY(int idx, int value) throws GameActionException {
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

  private int readAdamantiumWellUpgradedBit(int idx) throws GameActionException {
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

  private void writeAdamantiumWellUpgradedBit(int idx, int value) throws GameActionException {
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

  public MapLocation readAdamantiumWellLocation(int idx) throws GameActionException {
    return new MapLocation(readAdamantiumWellX(idx)-1,readAdamantiumWellY(idx)-1);
  }
  public boolean readAdamantiumWellExists(int idx) throws GameActionException {
    return !((readAdamantiumWellLocation(idx)).equals(NONEXISTENT_MAP_LOC));
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

  private void writeManaWellX(int idx, int value) throws GameActionException {
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

  private int readManaWellY(int idx) throws GameActionException {
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

  private void writeManaWellY(int idx, int value) throws GameActionException {
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

  private int readManaWellUpgradedBit(int idx) throws GameActionException {
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

  private void writeManaWellUpgradedBit(int idx, int value) throws GameActionException {
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

  public MapLocation readManaWellLocation(int idx) throws GameActionException {
    return new MapLocation(readManaWellX(idx)-1,readManaWellY(idx)-1);
  }
  public boolean readManaWellExists(int idx) throws GameActionException {
    return !((readManaWellLocation(idx)).equals(NONEXISTENT_MAP_LOC));
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

  private void writeElixirWellX(int idx, int value) throws GameActionException {
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

  private int readElixirWellY(int idx) throws GameActionException {
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

  private void writeElixirWellY(int idx, int value) throws GameActionException {
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

  private int readElixirWellUpgradedBit(int idx) throws GameActionException {
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

  private void writeElixirWellUpgradedBit(int idx, int value) throws GameActionException {
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

  public MapLocation readElixirWellLocation(int idx) throws GameActionException {
    return new MapLocation(readElixirWellX(idx)-1,readElixirWellY(idx)-1);
  }
  public boolean readElixirWellExists(int idx) throws GameActionException {
    return !((readElixirWellLocation(idx)).equals(NONEXISTENT_MAP_LOC));
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
  private int readAttackTargetX(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 16128) >>> 8;
      case 1:
          return ((rc.readSharedArray(13) & 3) << 4) + ((rc.readSharedArray(14) & 61440) >>> 12);
      case 2:
          return (rc.readSharedArray(14) & 63);
      case 3:
          return (rc.readSharedArray(15) & 1008) >>> 4;
      case 4:
          return (rc.readSharedArray(16) & 16128) >>> 8;
      case 5:
          return ((rc.readSharedArray(16) & 3) << 4) + ((rc.readSharedArray(17) & 61440) >>> 12);
      case 6:
          return (rc.readSharedArray(17) & 63);
      case 7:
          return (rc.readSharedArray(18) & 1008) >>> 4;
      default:
          return -1;
    }
  }

  private void writeAttackTargetX(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 49407) | (value << 8));
        break;
      case 1:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 4095) | ((value & 15) << 12));
        break;
      case 2:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 65472) | (value));
        break;
      case 3:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 64527) | (value << 4));
        break;
      case 4:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 49407) | (value << 8));
        break;
      case 5:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65532) | ((value & 48) >>> 4));
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 4095) | ((value & 15) << 12));
        break;
      case 6:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 65472) | (value));
        break;
      case 7:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 64527) | (value << 4));
        break;
    }
  }

  private int readAttackTargetY(int idx) throws GameActionException {
    switch (idx) {
      case 0:
          return (rc.readSharedArray(13) & 252) >>> 2;
      case 1:
          return (rc.readSharedArray(14) & 4032) >>> 6;
      case 2:
          return (rc.readSharedArray(15) & 64512) >>> 10;
      case 3:
          return ((rc.readSharedArray(15) & 15) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
      case 4:
          return (rc.readSharedArray(16) & 252) >>> 2;
      case 5:
          return (rc.readSharedArray(17) & 4032) >>> 6;
      case 6:
          return (rc.readSharedArray(18) & 64512) >>> 10;
      case 7:
          return ((rc.readSharedArray(18) & 15) << 2) + ((rc.readSharedArray(19) & 49152) >>> 14);
      default:
          return -1;
    }
  }

  private void writeAttackTargetY(int idx, int value) throws GameActionException {
    switch (idx) {
      case 0:
        rc.writeSharedArray(13, (rc.readSharedArray(13) & 65283) | (value << 2));
        break;
      case 1:
        rc.writeSharedArray(14, (rc.readSharedArray(14) & 61503) | (value << 6));
        break;
      case 2:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 1023) | (value << 10));
        break;
      case 3:
        rc.writeSharedArray(15, (rc.readSharedArray(15) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
        break;
      case 4:
        rc.writeSharedArray(16, (rc.readSharedArray(16) & 65283) | (value << 2));
        break;
      case 5:
        rc.writeSharedArray(17, (rc.readSharedArray(17) & 61503) | (value << 6));
        break;
      case 6:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 1023) | (value << 10));
        break;
      case 7:
        rc.writeSharedArray(18, (rc.readSharedArray(18) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(19, (rc.readSharedArray(19) & 16383) | ((value & 3) << 14));
        break;
    }
  }

  public MapLocation readAttackTargetLocation(int idx) throws GameActionException {
    return new MapLocation(readAttackTargetX(idx)-1,readAttackTargetY(idx)-1);
  }
  public boolean readAttackTargetExists(int idx) throws GameActionException {
    return !((readAttackTargetLocation(idx)).equals(NONEXISTENT_MAP_LOC));
  }
  public void writeAttackTargetLocation(int idx, MapLocation value) throws GameActionException {
    writeAttackTargetX(idx, (value).x+1);
    writeAttackTargetY(idx, (value).y+1);
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

    public void writeWellLocation(int idx, MapLocation value) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          Global.communicator.commsHandler.writeAdamantiumWellLocation(idx, value);
          break;
        case MANA:
          Global.communicator.commsHandler.writeManaWellLocation(idx, value);
          break;
        case ELIXIR:
          Global.communicator.commsHandler.writeElixirWellLocation(idx, value);
          break;
        default:
          throw new RuntimeException("writeWellLocation not defined for " + this);
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

    public void writeWellUpgraded(int idx, boolean value) throws GameActionException {
      switch (this) {
        case ADAMANTIUM:
          Global.communicator.commsHandler.writeAdamantiumWellUpgraded(idx, value);
          break;
        case MANA:
          Global.communicator.commsHandler.writeManaWellUpgraded(idx, value);
          break;
        case ELIXIR:
          Global.communicator.commsHandler.writeElixirWellUpgraded(idx, value);
          break;
        default:
          throw new RuntimeException("writeWellUpgraded not defined for " + this);
      }
    }

  }
}
