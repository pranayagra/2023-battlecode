package smalllaunchergroup.knowledge.unitknowledge;

import smalllaunchergroup.knowledge.Cache;
import smalllaunchergroup.knowledge.Memory;
import smalllaunchergroup.robots.pathfinding.unitpathing.*;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public abstract class UnitKnowledge {
  public static MapLocation[] newUnseenMapLocations(MapLocation prev, MapLocation curr) {
    return Memory.uk.newUnseenMapLocationsImpl(prev, curr);
  }


  protected abstract MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr);
}
