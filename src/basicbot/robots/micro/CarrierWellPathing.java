package basicbot.robots.micro;

import basicbot.utils.Utils;
import battlecode.common.*;


public class CarrierWellPathing {

  public static Direction[] NORTHEAST_OFFSET = {
      Direction.NORTH, Direction.NORTHEAST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH
    };
  public static Direction[] SOUTHEAST_OFFSET = {
      Direction.EAST, Direction.SOUTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST
    };
  public static Direction[] SOUTHWEST_OFFSET = {
      Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH
    };
  public static Direction[] NORTHWEST_OFFSET = {
      Direction.WEST, Direction.NORTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST
    };
  public static Direction[] NORTH_OFFSET = {
      Direction.NORTHEAST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH
    };
  public static Direction[] EAST_OFFSET = {
      Direction.SOUTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST
    };
  public static Direction[] SOUTH_OFFSET = {
      Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH
    };
  public static Direction[] WEST_OFFSET = {
      Direction.NORTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST
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