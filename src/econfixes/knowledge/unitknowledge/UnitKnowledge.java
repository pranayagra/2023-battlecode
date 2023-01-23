package econfixes.knowledge.unitknowledge;

import econfixes.knowledge.Cache;
import econfixes.knowledge.Memory;
import econfixes.robots.pathfinding.unitpathing.*;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public abstract class UnitKnowledge {
  public static MapLocation[] newUnseenMapLocations(MapLocation prev, MapLocation curr) {
    return Memory.uk.newUnseenMapLocationsImpl(prev, curr);
  }


  protected abstract MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr);
}
