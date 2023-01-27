package ptestcarrier.knowledge;

import ptestcarrier.knowledge.unitknowledge.*;
import ptestcarrier.utils.Global;
import battlecode.common.GameActionException;

public class Memory {
  public static UnitKnowledge uk;

  public static void init() throws GameActionException {
    Cache.setup();
    switch (Cache.Permanent.ROBOT_TYPE) {
      case HEADQUARTERS:
        uk = new HeadquartersKnowledge();
        break;
      case CARRIER:
        uk = new CarrierKnowledge();
        break;
      case LAUNCHER:
        uk = new LauncherKnowledge();
        break;
      case AMPLIFIER:
        uk = new AmplifierKnowledge();
        break;
      case DESTABILIZER:
        uk = new DestabilizerKnowledge();
        break;
      case BOOSTER:
        uk = new BoosterKnowledge();
        break;
      default:
        throw new RuntimeException("Invalid robot type for unit knowledge");
    }
    MapMemory.setup();
    updateOnTurn();
  }

  public static void updateOnTurn() throws GameActionException {
    Cache.updateOnTurn();
    updateForMovement();
  }

  private static void updateForMovement() throws GameActionException {
    Cache.PerTurn.updateForMovement();
    MapMemory.update(Cache.PerTurn.PREVIOUS_LOCATION, Cache.PerTurn.CURRENT_LOCATION);
  }

  public static void whenMoved() throws GameActionException {
    if (Cache.PerTurn.CURRENT_LOCATION != null && Global.rc.getLocation().equals(Cache.PerTurn.CURRENT_LOCATION)) {
      return;
    }
    updateForMovement();
  }
}
