package betterlaunchermacro.communications;

import betterlaunchermacro.utils.Cache;
import betterlaunchermacro.utils.Printer;
import betterlaunchermacro.utils.Utils;
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
      MapMetaInfo.updateSymmetry();
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
}
