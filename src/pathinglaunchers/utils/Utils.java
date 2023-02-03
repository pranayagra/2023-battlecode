package pathinglaunchers.utils;

import pathinglaunchers.knowledge.Cache;
import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {
  public enum MapSymmetry {
    ROTATIONAL,
    HORIZONTAL,
    VERTICAL,
  }

  /** Seeded RNG for use throughout the bot classes */
  public static Random rng;

  /** Array of the 8 possible directions. */
  public static final Direction[] directions = {
      Direction.NORTH,
      Direction.NORTHEAST,
      Direction.EAST,
      Direction.SOUTHEAST,
      Direction.SOUTH,
      Direction.SOUTHWEST,
      Direction.WEST,
      Direction.NORTHWEST,
  };

  /** Array of the 4 possible angled directions. */
  public static final Direction[] ordinal_directions = {
          Direction.NORTHEAST,
          Direction.SOUTHEAST,
          Direction.SOUTHWEST,
          Direction.NORTHWEST,
  };

  /** Array of the 3x3 possible directions. */
  public static final Direction[] directionsNine = {
          Direction.CENTER,
          Direction.NORTH,
          Direction.NORTHEAST,
          Direction.EAST,
          Direction.SOUTHEAST,
          Direction.SOUTH,
          Direction.SOUTHWEST,
          Direction.WEST,
          Direction.NORTHWEST,
  };

  public static final int DSQ_1by1 = 2; // technically 3x3 but a 1tile boundary around center
  public static final int DSQ_2by2 = 8;
  public static final int DSQ_3by3plus = 18; // contains some extra tiles
//  public static final int DSQ_3by3 = 32;
//  public static final int DSQ_3by3 = 32;

  /**
   * mapping from comms integer value to MapSymmetry
   * 0 is invalid -> null
   * NOT_HORIZ NOT_VERT NOT_ROT
   * horiz = 011 = 3
   * vert = 101 = 5
   * rot = 110 = 6
   */


  /** The number of chunks that the map should be split into */
  public static final int MAX_MAP_CHUNKS = 100;

  /** related to above */
  public static final int CHUNK_INFOS_PER_INT = 4;

  /*
   * the amount of lead that a single miner can claim from others (based on lead regen)
   * regenAmt*regenTime * FACTOR (~0.75) = 3*40*X = 100
   */
  public static final int LEAD_PER_MINER_CLAIM = 75;

  public static void setUpStatics() {
    rng = new Random(Global.rc.getID());
  }

  public static Direction randomDirection() {
    return directions[rng.nextInt(directions.length)];
  }

  /**
   * generate a direction either same as dir or random rotation to left/right
   *    uniform chance
   * @param dir base direction
   * @return the randomized direction
   */
  public static Direction randomSimilarDirection(Direction dir) {
    switch (Utils.rng.nextInt(3)) {
      default:
      case 0:
        return dir;
      case 1:
        return dir.rotateLeft();
      case 2:
        return dir.rotateRight();
    }
  }

  /**
   * generate a direction either same as dir or random rotation to left/right
   *    prefers passed in direction with 50% (25% each rotation)
   * @param dir base direction
   * @return the randomized direction
   */
  public static Direction randomSimilarDirectionPrefer(Direction dir) {
    switch (Utils.rng.nextInt(4)) {
      default:
      case 1:
        return dir;
      case 2:
        return dir.rotateLeft();
      case 3:
        return dir.rotateRight();
    }
  }


  public static MapLocation randomMapLocation() { return new MapLocation(randomMapLocationX(), randomMapLocationY());}

  public static int randomMapLocationX() { return rng.nextInt(Cache.Permanent.MAP_WIDTH);}

  public static int randomMapLocationY() { return rng.nextInt(Cache.Permanent.MAP_HEIGHT);}

  public static int minDistanceToEdge(MapLocation loc) {
    int minDistance = Math.min(loc.x, loc.y);
    minDistance = Math.min(minDistance, Cache.Permanent.MAP_WIDTH - loc.x);
    minDistance = Math.min(minDistance, Cache.Permanent.MAP_HEIGHT - loc.y);
    return minDistance;
  }

  public static int maxDistanceToCorner(MapLocation loc) {
    return Math.max(Math.min(loc.x, Cache.Permanent.MAP_WIDTH - loc.x), Math.min(loc.y, Cache.Permanent.MAP_HEIGHT - loc.y));
  }

  /**
   * encode the location into an integer where
   *  bits 15-10 : x
   *  bits  9-4  : y
   *  bits  3-0  : free
   * @param location the location to encode
   * @return the encoded location as int
   */
  public static int encodeLocation(MapLocation location) {
    return (location.x << 10) | (location.y << 4);
  }
  public static int encodeLocationLower(MapLocation location) {
    return (location.x << 6) | (location.y);
  }

  /**
   * decode a location from the provided integer (encoding described above)
   * @param encoded the integer to extract location from
   * @return the decoded location
   */
  public static MapLocation decodeLocation(int encoded) {
    return new MapLocation((encoded >> 10) & 0x3f, (encoded >> 4) & 0x3f);
  }
  public static MapLocation decodeLocationLower(int encoded) {
    return new MapLocation((encoded >> 6) & 0x3f, (encoded) & 0x3f);
  }

  /**
   * flip the x component of a direction
   * @param toFlip the direction to slip x
   * @return the x-flipped direction
   */
  public static Direction flipDirX(Direction toFlip) {
    switch (toFlip) {
      case NORTH: return toFlip;
      case NORTHEAST: return Direction.NORTHWEST;
      case EAST: return Direction.WEST;
      case SOUTHEAST: return Direction.SOUTHWEST;
      case SOUTH: return toFlip;
      case SOUTHWEST: return Direction.SOUTHEAST;
      case WEST: return Direction.EAST;
      case NORTHWEST: return Direction.NORTHEAST;
      case CENTER: return Direction.CENTER;
    }
    throw new RuntimeException("Cannot flip x of invalid Direction! " + toFlip);
  }

  /**
   * flip the y component of a direction
   * @param toFlip the direction to slip y
   * @return the y-flipped direction
   */
  public static Direction flipDirY(Direction toFlip) {
    switch (toFlip) {
      case NORTH: return Direction.SOUTH;
      case NORTHEAST: return Direction.SOUTHEAST;
      case EAST: return Direction.EAST;
      case SOUTHEAST: return Direction.NORTHEAST;
      case SOUTH: return Direction.NORTH;
      case SOUTHWEST: return Direction.NORTHWEST;
      case WEST: return Direction.WEST;
      case NORTHWEST: return Direction.SOUTHWEST;
      case CENTER: return Direction.CENTER;
    }
    throw new RuntimeException("Cannot flip y of invalid Direction! " + toFlip);
  }

  /**
   * calculate the distance between two locations based on the largest difference on a single axis
   * @param a first location
   * @param b second location
   * @return the distance metric
   */
  public static int maxSingleAxisDist(MapLocation a, MapLocation b) {
    return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
  }

  /**
   * do a lerp between the locations
   * @param from the location to start from
   * @param to the location to end at
   * @param amount the lerping distance
   * @return the lerped location
   */
  public static MapLocation lerpLocations(MapLocation from, MapLocation to, double amount) {
    return new MapLocation((int) ((to.x - from.x) * amount) + from.x, (int) ((to.y - from.y) * amount) + from.y);
  }

  /**
   * flip x-coord of location (mimic horizontal flip)
   * @param toFlip the coord to flip horiz
   * @return the flipped location
   */
  public static MapLocation flipLocationX(MapLocation toFlip) {
    return new MapLocation(Cache.Permanent.MAP_WIDTH - 1 - toFlip.x, toFlip.y);
  }

  /**
   * flip y-coord of location (mimic vertical flip)
   * @param toFlip the coord to flip vert
   * @return the flipped location
   */
  public static MapLocation flipLocationY(MapLocation toFlip) {
    return new MapLocation(toFlip.x, Cache.Permanent.MAP_HEIGHT - 1 - toFlip.y);
  }

  /**
   * flip x- and y-coords of location (mimic rotation)
   * @param toRot the coord to rotate
   * @return the rotated location
   */
  public static MapLocation rotateLocation180(MapLocation toRot) {
    return new MapLocation(Cache.Permanent.MAP_WIDTH - 1 - toRot.x, Cache.Permanent.MAP_HEIGHT - 1 - toRot.y);
  }

  /**
   * invert the given maploc based on the provided symmetry
   * @param location the coordinates to invert
   * @param symmetry how to invert the coordinates
   * @return the inverted location
   */
  public static MapLocation applySymmetry(MapLocation location, MapSymmetry symmetry) {
    switch (symmetry) {
      case HORIZONTAL:
        return flipLocationX(location);
      case VERTICAL:
        return flipLocationY(location);
      case ROTATIONAL:
        return rotateLocation180(location);
      default:
        throw new RuntimeException("Cannot apply unknown symmetry to map location: " + symmetry);
    }
  }

  public static Direction applySymmetry(Direction direction, MapSymmetry symmetry) {
    switch (symmetry) {
      case HORIZONTAL:
        return flipDirX(direction);
      case VERTICAL:
        return flipDirY(direction);
      case ROTATIONAL:
        return direction.opposite();
      default:
        throw new RuntimeException("Cannot apply unknown symmetry to direction: " + symmetry);
    }
  }

  public static double turnsTillNextCooldown(int c, int r) {
    int cooldownAfterMove = (int) Math.floor((1 + r/10.0) * c); //35
    //20 => 2
    return cooldownAfterMove / 10.0;
  }

  /**
   * converts a map location to a chunk index based on the chunk size calculated by cache
   * @param location the location to get its chunk
   * @return the chunk index
   */
  public static int locationToChunkIndex(MapLocation location) {
    int x = location.x / Cache.Permanent.CHUNK_WIDTH;
    int y = location.y / Cache.Permanent.CHUNK_HEIGHT;
    return x + y * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
  }

  /**
   * gets the center coord of the given chunk
   * @param chunkIndex the index of the chunk in our chunk info
   * @return the center location of the chunk
   */
  public static MapLocation chunkIndexToLocation(int chunkIndex) {
    int x = chunkIndex % Cache.Permanent.NUM_HORIZONTAL_CHUNKS * Cache.Permanent.CHUNK_WIDTH;
    int y = chunkIndex / Cache.Permanent.NUM_HORIZONTAL_CHUNKS * Cache.Permanent.CHUNK_HEIGHT;
    return new MapLocation(x + Cache.Permanent.CHUNK_WIDTH / 2, y + Cache.Permanent.CHUNK_HEIGHT / 2);
  }

  /**
   * convert the provided location to the center of its chunk
   * @param location the location to find the chunk center
   * @return the chunk center for the given location
   */
  public static MapLocation locationToChunkCenter(MapLocation location) {
    int x = location.x - (location.x % Cache.Permanent.CHUNK_WIDTH);
    int y = location.y - (location.y % Cache.Permanent.CHUNK_HEIGHT);
    return new MapLocation(x + Cache.Permanent.CHUNK_WIDTH / 2, y + Cache.Permanent.CHUNK_HEIGHT / 2);
  }

  public static MapLocation clampToMap(MapLocation location) {
    // make sure location is on the map by checking 0<x<mapWidth and 0<y<mapHeight
    if (location.x < 0) location = new MapLocation(0, location.y);
    if (location.y < 0) location = new MapLocation(location.x, 0);
    if (location.x >= Cache.Permanent.MAP_WIDTH) location = new MapLocation(Cache.Permanent.MAP_WIDTH - 1, location.y);
    if (location.y >= Cache.Permanent.MAP_HEIGHT) location = new MapLocation(location.x, Cache.Permanent.MAP_HEIGHT - 1);
    return location;
  }


  public static int getInvWeight(RobotInfo ri) {
    return (ri.getResourceAmount(ResourceType.ADAMANTIUM) + ri.getResourceAmount(ResourceType.MANA) + ri.getResourceAmount(ResourceType.ELIXIR) + (ri.getTotalAnchors() * GameConstants.ANCHOR_WEIGHT));
  }


  /**
   * Returns the actual saturation of a well depending on capacity and singleAxisMaxDist and well upgraded status
   * @param capacity
   * @param singleAxisMaxDist
   * @return number of carriers per well
   */
  public static int maxCarriersPerWell(int capacity, int singleAxisMaxDist, boolean isUpgraded) {
    int collectionTime = isUpgraded ? 14 : 40;
    // carriers spend 40 / 3 turns @ well
    double multiplier = ((singleAxisMaxDist * 2.5 + 1) / collectionTime) + 1;
    return (int) (capacity * multiplier);
  }
  public static int maxCarriersPerWell(int capacity, int singleAxisMaxDist) {
    return maxCarriersPerWell(capacity, singleAxisMaxDist, false);
  }


  /*

  */ // ================================== TOGGLE THIS OFF/ON

  private static Map<String, Integer> byteCodeMap = new HashMap<>();
  public static void startByteCodeCounting(String reason) {
    if (byteCodeMap.putIfAbsent(reason, Clock.getBytecodeNum()) != null) { // we're already counting!
      System.out.printf("Already counting for %s!!\n", reason);
    }
  }

  public static void finishByteCodeCounting(String reason) {
    int end = Clock.getBytecodeNum();
    Integer start = byteCodeMap.getOrDefault(reason, -1);
    if (start == -1) {
      System.out.printf("Not counting bytecodes for %s!!!\n", reason);
      return;
    }
    int diff = end - start;
    if (diff < 0) diff = end + (Cache.Permanent.ROBOT_TYPE.bytecodeLimit - start);
    System.out.printf("%s bytecode=%4d\n", reason, diff);
    byteCodeMap.remove(reason);
  }
  /*

  * / // ------------------------------ TOGGLE THIS ON/OFF

  public static void startByteCodeCounting(String reason) {}
  public static void finishByteCodeCounting(String reason) {}

  /*
   */

}
