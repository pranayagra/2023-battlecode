package supportivelaunchers.knowledge.unitknowledge;

import supportivelaunchers.knowledge.Cache;
import supportivelaunchers.knowledge.Memory;
import supportivelaunchers.robots.pathfinding.unitpathing.*;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public abstract class UnitKnowledge {
  public static MapLocation[] newUnseenMapLocations(MapLocation prev, MapLocation curr) {
    return Memory.uk.newUnseenMapLocationsImpl(prev, curr);
  }


  protected abstract MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr);
}