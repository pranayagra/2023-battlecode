package basicbot.communications;

import basicbot.utils.Global;
import basicbot.utils.Utils;
import battlecode.common.*;


public class CommsHandler {

  // CONSTS

  RobotController rc;

  public CommsHandler(RobotController rc) throws GameActionException {
    this.rc = rc;
  }

  // MAIN READ AND WRITE METHODS


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

    // RESOURCE READERS AND WRITERS

  }
}