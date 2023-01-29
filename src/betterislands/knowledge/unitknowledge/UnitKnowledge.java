package betterislands.knowledge.unitknowledge;

import betterislands.knowledge.Cache;
import betterislands.knowledge.Memory;
import betterislands.robots.pathfinding.unitpathing.*;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public abstract class UnitKnowledge {
  public static MapLocation[] newUnseenMapLocations(MapLocation prev, MapLocation curr) {
    return Memory.uk.newUnseenMapLocationsImpl(prev, curr);
  }


  protected abstract MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr);
}
