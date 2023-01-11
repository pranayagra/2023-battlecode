package basicbot.robots.micro;

import basicbot.utils.Utils;
import battlecode.common.*;


public class CarrierWellPathing {

  // CONSTS

  /**
   * Returns a 9-length array of maplocations specifying the path. The 0th index is the last position (leaving position).
   */
  public static MapLocation[] getPathForWell(MapLocation wellCenter, Direction directionToHQ) {
    Direction[] offset = getOffset(directionToHQ);
    return new MapLocation[] {
        wellCenter.add(offset[0]),
        wellCenter.add(offset[1]),
        wellCenter.add(offset[2]),
        wellCenter.add(offset[3]),
        wellCenter.add(offset[4]),
        wellCenter.add(offset[5]),
        wellCenter.add(offset[6]),
        wellCenter.add(offset[7]),
        wellCenter.add(offset[8]),
    };
  }

  private static Direction[] getOffset(Direction directionToHQ) {
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