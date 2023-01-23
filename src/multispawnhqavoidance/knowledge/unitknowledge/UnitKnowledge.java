package multispawnhqavoidance.knowledge.unitknowledge;

import multispawnhqavoidance.knowledge.Cache;
import multispawnhqavoidance.knowledge.Memory;
import multispawnhqavoidance.robots.pathfinding.unitpathing.*;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public abstract class UnitKnowledge {
  public static MapLocation[] newUnseenMapLocations(MapLocation prev, MapLocation curr) {
    return Memory.uk.newUnseenMapLocationsImpl(prev, curr);
  }


  protected abstract MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr);
}
