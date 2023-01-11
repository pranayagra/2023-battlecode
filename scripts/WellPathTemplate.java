package basicbot.robots.micro;

import basicbot.utils.Utils;
import battlecode.common.*;


public class CarrierWellPathing {

  // CONSTS

  /**
   * Returns a 9-length array of maplocations specifying the path. The 0th index is the last position (leaving position).
   */
  public static MapLocation[] getPathForWell(MapLocation wellCenter, Direction directionToHQ) {
    MapLocation[] offset = getOffset(directionToHQ);
    MapLocation[] path = new MapLocation[9];
    for(int i = 0; i < offset.length; i++) {
      // TODO: change to directions
      MapLocation o = offset[i]; // maybe remove for bytecode optimization?
      path[i] = wellCenter.translate(o.x, o.y);
    }
    return path;
  }

  private static MapLocation[] getOffset(Direction directionToHQ) {
    switch(directionToHQ) {
      case NORTH:
        return NORTH_OFFSET;
      case NORTHEAST:
        return NORTHEAST_OFFSET;
      case EAST:
        return EAST_OFFSET;
      case SOUTHEAST:
        return SOUTHEAST_OFFSET;
      case SOUTH:
        return SOUTH_OFFSET;
      case SOUTHWEST:
        return SOUTHWEST_OFFSET;
      case WEST:
        return WEST_OFFSET;
      case NORTHWEST:
        return NORTHWEST_OFFSET;
      case CENTER: // assume north if center
      default:
        return NORTH_OFFSET;
    }
  }
  
  // MAIN READ AND WRITE METHODS

}