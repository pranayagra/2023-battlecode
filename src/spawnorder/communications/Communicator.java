package spawnorder.communications;

import spawnorder.utils.Cache;
import spawnorder.utils.Printer;
import spawnorder.utils.Utils;
import battlecode.common.*;


public class Communicator {

  public static class MetaInfo {


    public static void init() throws GameActionException {
      MapMetaInfo.updateSymmetry();
      HqMetaInfo.init();
    }

    public static int registerHQ(WellInfo closestAdamantium, WellInfo closestMana) throws GameActionException {
      int hqID = HqMetaInfo.hqCount;
//      Printer.print("Registering HQ " + hqID);
      HqMetaInfo.hqCount++;
      CommsHandler.writeHqCount(HqMetaInfo.hqCount);
      CommsHandler.writeOurHqLocation(hqID, Cache.PerTurn.CURRENT_LOCATION);
      if (closestAdamantium != null) {
        commsHandler.writeAdamantiumWellLocation(hqID, closestAdamantium.getMapLocation());
//        commsHandler.writeOurHqClosestAdamantiumLocation(hqID, closestAdamantium.getMapLocation());
      }
      if (closestMana != null) {
        commsHandler.writeManaWellLocation(hqID, closestMana.getMapLocation());
//        commsHandler.writeOurHqClosestManaLocation(hqID, closestMana.getMapLocation());
      }
      return hqID;
    }

    public static void reinitForHQ() throws GameActionException {
//      Global.rc.setIndicatorString("HQ reinit!");
      init();
    }

    public static void updateOnTurnStart() throws GameActionException {
      if (MapMetaInfo.updateSymmetry()) {
        HqMetaInfo.recomputeEnemyHqLocations();
      }
    }
  }

  public static CommsHandler commsHandler;

  private Communicator() throws GameActionException {}
  public static void init(RobotController rc) throws GameActionException {
    CommsHandler.init(rc);
    MetaInfo.init();
  }

  /**
   * puts a well into the next free slot within the comms buffer for wells of that type
   * @param well the well info to broadcast
   * @return true if the information was successfully broadcast (or already in comms)
   * @throws GameActionException if any issues with reading/writing to comms
   */
  public static boolean writeNextWell(WellInfo well) throws GameActionException {
    CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(well.getResourceType());
    for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
      if (!writer.readWellExists(i)) {
        writer.writeWellLocation(i, well.getMapLocation());
        writer.writeWellUpgraded(i, well.isUpgraded());
//        Printer.print("Published new well! " + well.getMapLocation());
        return true;
      } else if (writer.readWellLocation(i).equals(well.getMapLocation())) {
        if (writer.readWellUpgraded(i) != well.isUpgraded()) {
          writer.writeWellUpgraded(i, well.isUpgraded());
          return true;
        }
        return true;
//      } else {
//        Printer.print("Well already exists in comms: " + writer.readWellLocation(i));
      }
    }
//    Printer.print("Failed to write well " + well);
    return false;
  }

  public static MapLocation getClosestWellLocation(MapLocation fromHere, ResourceType resourceType) throws GameActionException {
    CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(resourceType);
    MapLocation closest = null;
    int closestDist = Integer.MAX_VALUE;
    for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
      if (writer.readWellExists(i)) {
        MapLocation wellLocation = writer.readWellLocation(i);
        int dist = fromHere.distanceSquaredTo(wellLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closest = wellLocation;
        }
      }
    }
    return closest;
  }

  /**
   * writes the given enemy into the shared array of enemies
   * @param enemy the enemy to put into comms
   * @return true if the enemy was successfully written (or already in comms)
   * @throws GameActionException if any issues with reading/writing to comms
   */
  public static boolean writeEnemy(RobotInfo enemy) throws GameActionException {
    if (Cache.PerTurn.ROUND_NUM % 2 == 0) { // write to even array
      for (int i = CommsHandler.ENEMY_SLOTS; --i >= 0;) {
        if (CommsHandler.readEnemyEvenExists(i)) {
          if (CommsHandler.readEnemyEvenLocation(i).equals(enemy.location)) {
            return true;
          }
        } else {
          CommsHandler.writeEnemyEvenLocation(i, enemy.location);
//          CommsHandler.writeEnemyEvenType(i, enemy.type);
//          CommsHandler.writeEnemyEvenExists(i, true);
          return true;
        }
      }
    } else { // write to odd array
      for (int i = CommsHandler.ENEMY_SLOTS; --i >= 0;) {
        if (CommsHandler.readEnemyOddExists(i)) {
          if (CommsHandler.readEnemyOddLocation(i).equals(enemy.location)) {
            return true;
          }
        } else {
          CommsHandler.writeEnemyOddLocation(i, enemy.location);
//          CommsHandler.writeEnemyOddType(i, enemy.type);
//          CommsHandler.writeEnemyOddExists(i, true);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * gets the closest enemy to the given location
   * @param toHere the location from which to find the closest enemy
   * @return the location of the closest enemy (or null if no enemies are known)
   * @throws GameActionException if any issues with reading from comms
   */
  public static MapLocation getClosestEnemy(MapLocation toHere) throws GameActionException {
    MapLocation closest = null;
    int closestDist = Integer.MAX_VALUE;
    for (int i = CommsHandler.ENEMY_SLOTS; --i >= 0;) {
      if (Cache.PerTurn.ROUND_NUM % 2 == 0) { // even round - read from the odd writes
        if (!CommsHandler.readEnemyOddExists(i)) {
          break;
        } else {
          MapLocation enemyLocation = CommsHandler.readEnemyOddLocation(i);
          int dist = toHere.distanceSquaredTo(enemyLocation);
          if (dist < closestDist) {
            closestDist = dist;
            closest = enemyLocation;
          }
        }
      } else { // odd round - read from the even writes
        if (!CommsHandler.readEnemyEvenExists(i)) {
          break;
        } else {
          MapLocation enemyLocation = CommsHandler.readEnemyEvenLocation(i);
          int dist = toHere.distanceSquaredTo(enemyLocation);
          if (dist < closestDist) {
            closestDist = dist;
            closest = enemyLocation;
          }
        }
      }
    }
    return closest;
  }

  /**
   * empties out the commed enemies for fresh writes
   * @return the number of cleared enemies from comms
   * @throws GameActionException any issues with reading/writing to comms
   */
  public static int clearEnemyComms() throws GameActionException {
    int cleared = 0;
    for (int i = CommsHandler.ENEMY_SLOTS; --i >= 0;) {
      if (Cache.PerTurn.ROUND_NUM % 2 == 0) { // even round - empty the even spaces for writing
        if (!CommsHandler.readEnemyEvenExists(i)) {
          break;
        } else {
          CommsHandler.writeEnemyEvenLocation(i, CommsHandler.NONEXISTENT_MAP_LOC);
          cleared++;
        }
      } else { // odd round - empty the odd spaces for writing
        if (!CommsHandler.readEnemyOddExists(i)) {
          break;
        } else {
          CommsHandler.writeEnemyOddLocation(i, CommsHandler.NONEXISTENT_MAP_LOC);
          cleared++;
        }
      }
    }
    return cleared;
  }
}
