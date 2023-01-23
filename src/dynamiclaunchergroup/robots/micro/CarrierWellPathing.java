package dynamiclaunchergroup.robots.micro;

import dynamiclaunchergroup.utils.Utils;
import battlecode.common.*;


public class CarrierWellPathing {

  public static final int WELL_PATH_FILL_ORDER[] = {0,1,2,3,4,5,6,7,8};
//  public static final int WELL_PATH_FILL_ORDER[] = {0,8,7,6,5,4,3,2,1};
//  public static final int WELL_PATH_FILL_ORDER[] = {8,1,7,2,6,3,5,4,0};

  public static Direction[] NORTHEAST_OFFSET = {
      Direction.NORTHEAST, Direction.NORTH, Direction.CENTER, Direction.NORTHWEST, Direction.WEST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST
    };
  public static Direction[] SOUTHEAST_OFFSET = {
      Direction.SOUTHEAST, Direction.EAST, Direction.CENTER, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST, Direction.SOUTHWEST, Direction.SOUTH
    };
  public static Direction[] SOUTHWEST_OFFSET = {
      Direction.SOUTHWEST, Direction.SOUTH, Direction.CENTER, Direction.SOUTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST
    };
  public static Direction[] NORTHWEST_OFFSET = {
      Direction.NORTHWEST, Direction.WEST, Direction.CENTER, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.NORTH
    };
  public static Direction[] NORTH_OFFSET = {
      Direction.NORTH, Direction.NORTHEAST, Direction.CENTER, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST
    };
  public static Direction[] EAST_OFFSET = {
      Direction.EAST, Direction.SOUTHEAST, Direction.CENTER, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST
    };
  public static Direction[] SOUTH_OFFSET = {
      Direction.SOUTH, Direction.SOUTHWEST, Direction.CENTER, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST
    };
  public static Direction[] WEST_OFFSET = {
      Direction.WEST, Direction.NORTHWEST, Direction.CENTER, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST
    };

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
  


}
