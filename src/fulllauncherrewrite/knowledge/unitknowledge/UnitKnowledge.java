package fulllauncherrewrite.knowledge.unitknowledge;

import fulllauncherrewrite.knowledge.Cache;
import fulllauncherrewrite.knowledge.Memory;
import fulllauncherrewrite.robots.pathfinding.unitpathing.*;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public abstract class UnitKnowledge {
  public static MapLocation[] newUnseenMapLocations(MapLocation prev, MapLocation curr) {
    return Memory.uk.newUnseenMapLocationsImpl(prev, curr);
  }


  protected abstract MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr);
}
