package soldiermacro.communications;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import soldiermacro.communications.messages.Message;
import soldiermacro.containers.FastQueue;
import soldiermacro.utils.Cache;
import soldiermacro.utils.Global;
import soldiermacro.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will have a variety of methods related to communications between robots
 * There should be a generic interface for sending messages and reading them
 * Every robot should have a communicator instantiated with its own RobotController
 *    (used to read/write to the shared array)
 */
public class Communicator {

  /**
   * information about a subchunk of the map
   *    lead presence, danger, etc
   */
  public static class ChunkInfo {
    public static final int NUM_CHUNK_INTS = 0;//Utils.MAX_MAP_CHUNKS / Utils.CHUNK_INFOS_PER_INT;
    public static final int CHUNK_INTS_START = GameConstants.SHARED_ARRAY_LENGTH - NUM_CHUNK_INTS;
    public static final int SHIFT_PER_CHUNK_MOD_INTS = 16 / Utils.CHUNK_INFOS_PER_INT;

    public static final int CHUNK_INFO_MASK = 0b1111;

    /**
     * next three encoded into 2 bits
     * 00 - unexplored              0
     * 01 - explored, no rss        1
     * 10 - explored, rss exist     2
     * 11 - explored, rss depleted  3
     */
//    boolean explored;
//    boolean hasResources;
//    boolean resourcesDepleted;
    public static final int EXPLORATION_AND_LEAD_MASK = 0b0011;
    public static final int EXPLORED_NO_RSS_MASK = 0b0001;
    public static final int EXPLORED_W_RSS_MASK = 0b0010;
    public static final int EXPLORED_RSS_DEPLETED_MASK = 0b0011;

    /**
     * indicates presence of dangerous enemies
     *    soldier, sage,
     *    watch tower,
     *    archon
     */
//    boolean hasDangerousUnits;
    public static final int DANGEROUS_UNIT_MASK = 0b1000;

    /**
     * indicates presence of non-dangerous enemies
     *    miner, builder
     */
//    boolean hasNonOffensiveUnits;
    public static final int PASSIVE_UNIT_MASK = 0b0100;

    public static final int BAD_FOR_MINERS = DANGEROUS_UNIT_MASK | 0b1; // danger or no rss

    /**
     * checks if the chunk at a given index has dangerous units
     * @param chunkIndex the chunk to check
     * @return whether the chunk has dangerous units
     * @throws GameActionException if reading fails
     */
    public boolean chunkHasDanger(int chunkIndex) throws GameActionException {
//      int chunkInfoInt = (Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
      return ((Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & DANGEROUS_UNIT_MASK) > 0;
    }
    public boolean chunkHasPassiveUnits(int chunkIndex) throws GameActionException {
//      int chunkInfoInt = (Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
      return ((Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & PASSIVE_UNIT_MASK) > 0;
    }
    public int chunkInfoBits(int chunkIndex) throws GameActionException {
//      int sharedArrIndex = chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START;
//      int chunkInfoInt = (Global.rc.readSharedArray(sharedArrIndex) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
//      return chunkInfoInt & ChunkInfo.EXPLORATION_AND_LEAD_MASK;
      return (Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & ChunkInfo.CHUNK_INFO_MASK;
    }
    public boolean chunkIsUnexplored(int chunkIndex) throws GameActionException {
//      int sharedArrIndex = chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START;
//      int chunkInfoInt = (Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
      return ((Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & EXPLORATION_AND_LEAD_MASK) <= 0;
    }
    public boolean chunkIsGoodForMinerExploration(int chunkIndex) throws GameActionException {
//      int sharedArrIndex = chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START;
      int chunkInfoInt = (Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
      // danger / lead depleted -- OR -- unexplored
      return (chunkInfoInt & BAD_FOR_MINERS) == 0 || (chunkInfoInt & EXPLORATION_AND_LEAD_MASK) == 0;
    }
//    public boolean chunkIsGoodForMinerExploration(MapLocation mapLoc) throws GameActionException {
//      int chunkIndex = Utils.locationToChunkIndex(mapLoc);
//      int sharedArrIndex = chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START;
//      int chunkInfoInt = (Global.rc.readSharedArray(sharedArrIndex) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
//      // danger / lead depleted -- OR -- unexplored
////      ////System.out.println("");
//      return (chunkInfoInt & BAD_FOR_MINERS) == 0 || (chunkInfoInt & EXPLORATION_AND_LEAD_MASK) == 0;
//    }
    public boolean chunkIsGoodForOffensiveUnits(int chunkIndex) throws GameActionException {
//      int sharedArrIndex = chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START;
      int chunkInfoInt = (Global.rc.readSharedArray(chunkIndex / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START) >> ((chunkIndex % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS)) & 0b1111;
      // danger / lead depleted -- OR -- unexplored
//      //Utils.print("chunkIndex: " + chunkIndex, "dangerous: " + ((chunkInfoInt & DANGEROUS_UNIT_MASK) > 0), "not explored and RSS: " + ((chunkInfoInt & EXPLORATION_AND_LEAD_MASK) == 0));
//      if ( ((chunkInfoInt & DANGEROUS_UNIT_MASK) > 0)) Global.//rc.setIndicatorDot(Utils.chunkIndexToLocation(chunkIndex), 255, 0, 0);
//      if ( (chunkInfoInt & EXPLORATION_AND_LEAD_MASK) == 0) Global.//rc.setIndicatorDot(Utils.chunkIndexToLocation(chunkIndex).add(Direction.SOUTH), 0, 255, 0);
      return (chunkInfoInt & DANGEROUS_UNIT_MASK) > 0 || (chunkInfoInt & PASSIVE_UNIT_MASK) > 0;
    }


    /**
     * gets the current chunk and returns the center of the closest optimal chunk for miner to go to
     *    checks for explored+rss or unexplored
     * @return the center of the optimal mining chunk
     * @throws GameActionException if reading fails
     */
    public MapLocation centerOfClosestOptimalChunkForMiners(MapLocation source, boolean forceNotSource) throws GameActionException {
      int myChunk = Utils.locationToChunkIndex(source);
      if (!forceNotSource && chunkIsGoodForMinerExploration(myChunk)) {
        ////System.out.println("closest optimal is self! " + source);
        return Utils.chunkIndexToLocation(myChunk);
      }
//      int leadFullChunk = -1;
      int unexplored = -1;
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS == 0 && dir.dx < 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS == Cache.Permanent.NUM_HORIZONTAL_CHUNKS - 1 && dir.dx > 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS == 0 && dir.dy < 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS == Cache.Permanent.NUM_VERTICAL_CHUNKS - 1 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx + dir.dy * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b10: // explored+rss
          case 0b1010:
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
          case 0b1000:
//            ////System.out.println(Utils.chunkIndexToLocation(chunkToTest) + " - unexplored!");
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
      if (unexplored != -1) return Utils.chunkIndexToLocation(unexplored);
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (dir.dx == 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS <= 1 && dir.dx < 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS >= Cache.Permanent.NUM_HORIZONTAL_CHUNKS - 2 && dir.dx > 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS == 0 && dir.dy < 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS == Cache.Permanent.NUM_VERTICAL_CHUNKS - 1 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx*2 + dir.dy * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b10: // explored+rss
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
//      if (unexplored != -1) return Utils.decodeChunkIndexToLocation(unexplored);
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (dir.dy == 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS == 0 && dir.dx < 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS == Cache.Permanent.NUM_HORIZONTAL_CHUNKS - 1 && dir.dx > 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS <= 1 && dir.dy < 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS >= Cache.Permanent.NUM_VERTICAL_CHUNKS - 2 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx + dir.dy*2 * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b10: // explored+rss
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
//      if (unexplored != -1) return Utils.decodeChunkIndexToLocation(unexplored);
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (dir.dx == 0) continue;
        if (dir.dy == 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS <= 1 && dir.dx < 0) continue;
        if (myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS >= Cache.Permanent.NUM_HORIZONTAL_CHUNKS - 2 && dir.dx > 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS <= 1 && dir.dy < 0) continue;
        if (myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS >= Cache.Permanent.NUM_VERTICAL_CHUNKS - 2 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx*2 + dir.dy*2 * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b10: // explored+rss
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
      if (unexplored != -1) return Utils.chunkIndexToLocation(unexplored);
      return null;
    }
    public MapLocation centerOfClosestOptimalChunkForOffensiveUnits(MapLocation source, boolean forceNotSource) throws GameActionException {
      int myChunk = Utils.locationToChunkIndex(source);
      if (!forceNotSource && chunkIsGoodForMinerExploration(myChunk)) {
        ////System.out.println("closest optimal is self! " + source);
        return Utils.chunkIndexToLocation(myChunk);
      }
//      int leadFullChunk = -1;
      int unexplored = -1;
      boolean chunkLowX0 = myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS == 0;
      boolean chunkHighX0 = myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS == Cache.Permanent.NUM_HORIZONTAL_CHUNKS - 1;
      boolean chunkLowY0 = myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS == 0;
      boolean chunkHighY0 = myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS == Cache.Permanent.NUM_VERTICAL_CHUNKS - 1;
      boolean chunkLowX1 = myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS <= 1;
      boolean chunkHighX1 = myChunk % Cache.Permanent.NUM_HORIZONTAL_CHUNKS >= Cache.Permanent.NUM_HORIZONTAL_CHUNKS - 2;
      boolean chunkLowY1 = myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS <= 1;
      boolean chunkHighY1 = myChunk / Cache.Permanent.NUM_HORIZONTAL_CHUNKS >= Cache.Permanent.NUM_VERTICAL_CHUNKS - 2;
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (chunkLowX0 && dir.dx < 0) continue;
        if (chunkHighX0 && dir.dx > 0) continue;
        if (chunkLowY0 && dir.dy < 0) continue;
        if (chunkHighY0 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx + dir.dy * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b1000: // danger
          case 0b1001:
          case 0b1010:
          case 0b1011:
          case 0b1100:
          case 0b1101:
          case 0b1110:
          case 0b1111:
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
//            ////System.out.println(Utils.chunkIndexToLocation(chunkToTest) + " - unexplored!");
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
      if (unexplored != -1) return Utils.chunkIndexToLocation(unexplored);
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (dir.dx == 0) continue;
        if (chunkLowX1 && dir.dx < 0) continue;
        if (chunkHighX1 && dir.dx > 0) continue;
        if (chunkLowY0 && dir.dy < 0) continue;
        if (chunkHighY0 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx*2 + dir.dy * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b1000: // danger
          case 0b1001:
          case 0b1010:
          case 0b1011:
          case 0b1100:
          case 0b1101:
          case 0b1110:
          case 0b1111:
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
//      if (unexplored != -1) return Utils.decodeChunkIndexToLocation(unexplored);
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (dir.dy == 0) continue;
        if (chunkLowX0 && dir.dx < 0) continue;
        if (chunkHighX0 && dir.dx > 0) continue;
        if (chunkLowY1 && dir.dy < 0) continue;
        if (chunkHighY1 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx + dir.dy*2 * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b1000: // danger
          case 0b1001:
          case 0b1010:
          case 0b1011:
          case 0b1100:
          case 0b1101:
          case 0b1110:
          case 0b1111:
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
//      if (unexplored != -1) return Utils.decodeChunkIndexToLocation(unexplored);
      for (Direction dir : Utils.directions) {
//        if (Utils.rng.nextInt(3) == 0) continue;
        if (dir.dx == 0) continue;
        if (dir.dy == 0) continue;
        if (chunkLowX1 && dir.dx < 0) continue;
        if (chunkHighX1 && dir.dx > 0) continue;
        if (chunkLowY1 && dir.dy < 0) continue;
        if (chunkHighY1 && dir.dy > 0) continue;
        int chunkToTest = myChunk + dir.dx*2 + dir.dy*2 * Cache.Permanent.NUM_HORIZONTAL_CHUNKS;
        switch (chunkInfoBits(chunkToTest)) {
          case 0b1000: // danger
          case 0b1001:
          case 0b1010:
          case 0b1011:
          case 0b1100:
          case 0b1101:
          case 0b1110:
          case 0b1111:
            return Utils.chunkIndexToLocation(chunkToTest);
          case 0b00: // unexplored
            if (unexplored == -1 || Utils.rng.nextInt(3) == 0) unexplored = chunkToTest;
        }
      }
      if (unexplored != -1) return Utils.chunkIndexToLocation(unexplored);
      return null;
    }

    /**
     * gets the current chunk and returns the center of the closest unexplored chunk
     * @return the center of an unexplored chunk
     * @throws GameActionException if reading fails
     */
    public MapLocation centerOfClosestUnexploredChunk(MapLocation source) throws GameActionException {
      int myChunk = Utils.locationToChunkIndex(source);
      if (chunkIsUnexplored(myChunk)) return Utils.chunkIndexToLocation(myChunk);

      MapLocation closestUnexploredChunk = null;
      int closestUnexploredChunkDist = Integer.MAX_VALUE;

      for (int chunkToTest = 0; chunkToTest < 100; ++chunkToTest) {
        if (chunkIsUnexplored(chunkToTest)) {
          MapLocation chunkCenter = Utils.chunkIndexToLocation(chunkToTest);
          if (closestUnexploredChunk == null || source.isWithinDistanceSquared(chunkCenter, closestUnexploredChunkDist)) {
            closestUnexploredChunk = chunkCenter;
            closestUnexploredChunkDist = source.distanceSquaredTo(chunkCenter);
          }
        }
      }
      return closestUnexploredChunk;
    }

    public void markExplored(MapLocation location, boolean dangerous, boolean passiveEnemies, int explorationBits) throws GameActionException {
      if (dangerous) {
        ////System.out.printf("DANGEROUS CHUNK!\n\tbot        :%s\n\tchunk      :%s\n\tdanger     :%s\n\texploration:%d\n",Cache.PerTurn.CURRENT_LOCATION,location,dangerous,explorationBits);
      }
      int chunkToMark = Utils.locationToChunkIndex(location);
      int sharedArrIndex = chunkToMark / Utils.CHUNK_INFOS_PER_INT + CHUNK_INTS_START;
      int existingChunkSetInfo = Global.rc.readSharedArray(sharedArrIndex);
      int shiftAmt = (chunkToMark % Utils.CHUNK_INFOS_PER_INT) * SHIFT_PER_CHUNK_MOD_INTS;
//      ////System.out.println("chunk idx: " + chunkToMark);
//      ////System.out.println("shared indices: [" + (sharedArrIndex-CHUNK_INTS_START) + "," + shiftAmt + "]");
//      ////System.out.println("Current bits: " + Integer.toBinaryString(existingChunkSetInfo));
//      int currentChunkInfo = (existingChunkSetInfo >> shiftAmt) & 0b1111;
//      ////System.out.println("Chunk before: " + Integer.toBinaryString(currentChunkInfo));
//      int data = (dangerous ? DANGEROUS_UNIT_MASK : 0) | explorationBits;
//      ////System.out.println("data: " + Integer.toBinaryString(data));
//      ////System.out.println("new value: " + Integer.toBinaryString(existingChunkSetInfo | (data << shiftAmt)));
//      Global.rc.writeSharedArray(sharedArrIndex, existingChunkSetInfo | (data << shiftAmt));
      int newChunkSetInfo = ((dangerous ? DANGEROUS_UNIT_MASK : 0) | (passiveEnemies ? PASSIVE_UNIT_MASK : 0) | explorationBits) << shiftAmt;
      newChunkSetInfo = newChunkSetInfo | (existingChunkSetInfo & ~(0b1111 << shiftAmt));
      if (existingChunkSetInfo != newChunkSetInfo) {
        Global.rc.writeSharedArray(sharedArrIndex, newChunkSetInfo);
      }
    }
  }

  public static class ArchonInfo {
    public static final int NUM_ARCHON_INTS = 4;
    public static final int ARCHON_INTS_START = ChunkInfo.CHUNK_INTS_START - NUM_ARCHON_INTS;
    public static final int OUR_ARCHONS_12 = ARCHON_INTS_START;
    public static final int OUR_ARCHONS_34 = ARCHON_INTS_START+1;
    public static final int ENEMY_ARCHONS_12 = ARCHON_INTS_START+2;
    public static final int ENEMY_ARCHONS_34 = ARCHON_INTS_START+3;
    public static final int LEFT_ARCHON_LOC_MASK = 0b1111111 << 8;
    public static final int LEFT_ARCHON_LOC_INVERTED_MASK = ~(LEFT_ARCHON_LOC_MASK);
    public static final int LEFT_ARCHON_MOVING_MASK = 0b10000000 << 8;
    public static final int LEFT_ARCHON_NOT_MOVING_MASK = ~(LEFT_ARCHON_MOVING_MASK);
    public static final int RIGHT_ARCHON_LOC_MASK = 0b1111111;
    public static final int RIGHT_ARCHON_LOC_INVERTED_MASK = ~(RIGHT_ARCHON_LOC_MASK);
    public static final int RIGHT_ARCHON_MOVING_MASK = 0b10000000;
    public static final int RIGHT_ARCHON_NOT_MOVING_MASK = ~(RIGHT_ARCHON_MOVING_MASK);
//    public static final int SHIFT_PER_CHUNK_MOD_INTS = 16 / Utils.CHUNK_INFOS_PER_INT;

    //    public static final int ARCHON_INFO_SIZE = 2;
//    public static final int ARCHON_INTS_START = SYMMETRY_INFO_IND - ARCHON_INFO_SIZE;
    public MapLocation ourArchon1;
    public MapLocation ourArchon2;
    public MapLocation ourArchon3;
    public MapLocation ourArchon4;

    public void readArchonLocs() throws GameActionException {
      ourArchon1 = Utils.chunkIndexToLocation(Global.rc.readSharedArray(OUR_ARCHONS_12) & RIGHT_ARCHON_LOC_MASK);
      ourArchon2 = Utils.chunkIndexToLocation((Global.rc.readSharedArray(OUR_ARCHONS_12) >>> 8) & RIGHT_ARCHON_LOC_MASK);
      ourArchon3 = Utils.chunkIndexToLocation(Global.rc.readSharedArray(OUR_ARCHONS_34) & RIGHT_ARCHON_LOC_MASK);
      ourArchon4 = Utils.chunkIndexToLocation((Global.rc.readSharedArray(OUR_ARCHONS_34) >>> 8) & RIGHT_ARCHON_LOC_MASK);
    }

    public void setOurArchonLoc(int whichArchon, MapLocation archonLoc) throws GameActionException {
//      Utils.cleanPrint();
      switch (whichArchon) {
        case 1:
          ourArchon1 = archonLoc;
          Global.rc.writeSharedArray(OUR_ARCHONS_12, (Global.rc.readSharedArray(OUR_ARCHONS_12) & LEFT_ARCHON_LOC_INVERTED_MASK) | (Utils.locationToChunkIndex(archonLoc) << 8));
//          //Utils.print("OUR_ARCHONS_12: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_12)));
          break;
        case 2:
          ourArchon2 = archonLoc;
          Global.rc.writeSharedArray(OUR_ARCHONS_12, (Global.rc.readSharedArray(OUR_ARCHONS_12) & RIGHT_ARCHON_LOC_INVERTED_MASK) | Utils.locationToChunkIndex(archonLoc));
//          //Utils.print("OUR_ARCHONS_12: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_12)));
          break;
        case 3:
          ourArchon3 = archonLoc;
          Global.rc.writeSharedArray(OUR_ARCHONS_34, (Global.rc.readSharedArray(OUR_ARCHONS_34) & LEFT_ARCHON_LOC_INVERTED_MASK) | (Utils.locationToChunkIndex(archonLoc) << 8));
//          //Utils.print("OUR_ARCHONS_34: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_34)));
          break;
        case 4:
          ourArchon4 = archonLoc;
          Global.rc.writeSharedArray(OUR_ARCHONS_34, (Global.rc.readSharedArray(OUR_ARCHONS_34) & RIGHT_ARCHON_LOC_INVERTED_MASK) | Utils.locationToChunkIndex(archonLoc));
          break;
      }
//      Utils.submitPrint();
    }

    public void setMoving(int whichArchon) throws GameActionException {
//      Utils.cleanPrint();
      switch (whichArchon) {
        case 1:
          Global.rc.writeSharedArray(OUR_ARCHONS_12, Global.rc.readSharedArray(OUR_ARCHONS_12) | LEFT_ARCHON_MOVING_MASK);
//          //Utils.print("OUR_ARCHONS_12: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_12)));
          break;
        case 2:
          Global.rc.writeSharedArray(OUR_ARCHONS_12, Global.rc.readSharedArray(OUR_ARCHONS_12) | RIGHT_ARCHON_MOVING_MASK);
//          //Utils.print("OUR_ARCHONS_12: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_12)));
          break;
        case 3:
          Global.rc.writeSharedArray(OUR_ARCHONS_34, Global.rc.readSharedArray(OUR_ARCHONS_34) | LEFT_ARCHON_MOVING_MASK);
//          //Utils.print("OUR_ARCHONS_34: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_34)));
          break;
        case 4:
          Global.rc.writeSharedArray(OUR_ARCHONS_34, Global.rc.readSharedArray(OUR_ARCHONS_34) | RIGHT_ARCHON_MOVING_MASK);
//          //Utils.print("OUR_ARCHONS_34: " + Integer.toBinaryString(Global.rc.readSharedArray(OUR_ARCHONS_34)));
          break;
      }
//      Utils.submitPrint();
    }

    public void setNotMoving(int whichArchon) throws GameActionException {
      switch (whichArchon) {
        case 1:
          Global.rc.writeSharedArray(OUR_ARCHONS_12, Global.rc.readSharedArray(OUR_ARCHONS_12) & LEFT_ARCHON_NOT_MOVING_MASK);
          break;
        case 2:
          Global.rc.writeSharedArray(OUR_ARCHONS_12, Global.rc.readSharedArray(OUR_ARCHONS_12) & RIGHT_ARCHON_NOT_MOVING_MASK);
          break;
        case 3:
          Global.rc.writeSharedArray(OUR_ARCHONS_34, Global.rc.readSharedArray(OUR_ARCHONS_34) & LEFT_ARCHON_NOT_MOVING_MASK);
          break;
        case 4:
          Global.rc.writeSharedArray(OUR_ARCHONS_34, Global.rc.readSharedArray(OUR_ARCHONS_34) & RIGHT_ARCHON_NOT_MOVING_MASK);
          break;
      }
    }

    public boolean isMoving(int whichArchon) throws GameActionException {
      switch (whichArchon) {
        case 1:
          if ((Global.rc.readSharedArray(OUR_ARCHONS_12) & LEFT_ARCHON_MOVING_MASK) > 0) {
            return true;
          }
          break;
        case 2:
          if ((Global.rc.readSharedArray(OUR_ARCHONS_12) & RIGHT_ARCHON_MOVING_MASK) > 0) {
            return true;
          }
          break;
        case 3:
          if ((Global.rc.readSharedArray(OUR_ARCHONS_34) & LEFT_ARCHON_MOVING_MASK) > 0) {
            return true;
          }
          break;
        case 4:
          if ((Global.rc.readSharedArray(OUR_ARCHONS_34) & RIGHT_ARCHON_MOVING_MASK) > 0) {
            return true;
          }
          break;
      }
      return false;
    }
  }

  public static class MetaInfo {
    public static final int NUM_META_INTS = 1;
    public static final int META_INT_START = ArchonInfo.ARCHON_INTS_START - NUM_META_INTS;

    public static final int VALID_REGION_IND = META_INT_START;
    private int validRegionStart; // 0-62    -- 6 bits [15,10]
    private int validRegionEnd;   // 0-62    -- 6 bits [9,4]
    public static final int EMPTY_REGION_INDICATOR = 61;

    public static final int SYMMETRY_INFO_SIZE = 0;
    public static final int SYMMETRY_INFO_IND = VALID_REGION_IND - SYMMETRY_INFO_SIZE;
    public Utils.MapSymmetry knownSymmetry; // determined by next three bools
    public Utils.MapSymmetry guessedSymmetry; // determined by next three bools
    private static final int NOT_HORIZ_MASK = 0b1000;
    public boolean notHorizontal;     // 0-1               -- 1 bit  [3]
    private static final int NOT_VERT_MASK = 0b100;
    public boolean notVertical;       // 0-1               -- 1 bit  [2]
    private static final int NOT_ROT_MASK = 0b10;
    public boolean notRotational;     // 0-1               -- 1 bit  [1]
    private static final int ALL_SYM_INFO_MASK = NOT_HORIZ_MASK|NOT_VERT_MASK|NOT_ROT_MASK;

    public boolean dirty;


    public MetaInfo() {
//      knownSymmetry = null;
//      guessedSymmetry = null;
//      notHorizontal = false;
//      notVertical = false;
//      notRotational = false;
//
//      dirty = false;
    }

    /**
     * update the communicators meta information based on the meta ints from the shared array
     * @throws GameActionException if reading fails
     */
    public void updateFromShared() throws GameActionException {
      int validRegion = Global.rc.readSharedArray(VALID_REGION_IND);
      validRegionStart = (validRegion >>> 10) & 63;
      validRegionEnd = (validRegion >>> 4) & 63;

      int symmetryInfo = validRegion; //Global.rc.readSharedArray(SYMMETRY_INFO_IND);
      knownSymmetry = Utils.commsSymmetryMap[(symmetryInfo & ALL_SYM_INFO_MASK) >> 1];
      notHorizontal = (symmetryInfo & NOT_HORIZ_MASK) > 0;
      notVertical = (symmetryInfo & NOT_VERT_MASK) > 0;
      notRotational = (symmetryInfo & NOT_ROT_MASK) > 0;
      guessedSymmetry = knownSymmetry != null ? knownSymmetry : Utils.commsSymmetryGuessMap[(symmetryInfo & ALL_SYM_INFO_MASK) >> 1];

      dirty = false;
    }

    public void initializeValidRegion() {
      validRegionStart = validRegionEnd = EMPTY_REGION_INDICATOR;
      dirty = true;
    }

    /**
     * update validstart/validend from shared array
     * @throws GameActionException if reading fails
     */
    public void reloadValidRegion() throws GameActionException {
      int validRegion = Global.rc.readSharedArray(VALID_REGION_IND);
      validRegionStart = (validRegion >>> 10) & 63;
      validRegionEnd = (validRegion >>> 4) & 63;
    }

    /**
     * convert the meta information about the communication buffer into a set of ints
     *    also write the ints to the shared array
     * @return true if updated
     * @throws GameActionException if writing fails
     */
    public boolean encodeAndWrite() throws GameActionException {
      if (!dirty) return false;
//      //System.out.printf("%s\n", this);
      Global.rc.writeSharedArray(VALID_REGION_IND,
            validRegionStart << 10
          | validRegionEnd << 4
          | ((notHorizontal ? NOT_HORIZ_MASK : 0) | (notVertical ? NOT_VERT_MASK : 0) | (notRotational ? NOT_ROT_MASK : 0)));
      dirty = false;
      return true;
    }

    @Override
    public String toString() {
      return String.format("ValidComms[%d:%d)", validRegionStart, validRegionEnd);
    }

    /**
     * update the meta info on map symmetry to disallow the gievn type of symmetry
     *    also updates knownSymmetry if possible
     *    sets dirty flag as well
     * @param blockedSymmetry the symmetry that is no longer allowed
     * @throws GameActionException if writing fails
     */
    public void setSymmetryCantBe(Utils.MapSymmetry blockedSymmetry) throws GameActionException {
      switch (blockedSymmetry) {
        case HORIZONTAL:
          notHorizontal = true;
          break;
        case VERTICAL:
          notVertical = true;
          break;
        case ROTATIONAL:
          notRotational = true;
          break;
      }
//      ////System.out.println("Bot at " + Cache.PerTurn.CURRENT_LOCATION + " realized sym can't be " + blockedSymmetry);
      int index = ((notHorizontal ? NOT_HORIZ_MASK : 0) | (notVertical ? NOT_VERT_MASK : 0) | (notRotational ? NOT_ROT_MASK : 0)) >> 1;
      knownSymmetry = Utils.commsSymmetryMap[index];
      guessedSymmetry = Utils.commsSymmetryGuessMap[index];
//      ////System.out.println("symIndex: " + index + " known: " + knownSymmetry + " -- guess: " + guessedSymmetry);
      ////System.out.printf("NEW SYMMETRY KNOWLEDGE\n\tnot:%s\n\tknown:%s\n\tguess:%s\n", blockedSymmetry, knownSymmetry, guessedSymmetry);
      dirty = true;
      encodeAndWrite();
    }
  }

  private static final int MIN_BYTECODES_TO_SEND_MESSAGE = 1000;

  private final RobotController rc;
//  private final int[] sharedBuffer;

  public final MetaInfo metaInfo;
  public final ChunkInfo chunkInfo;
  public final ArchonInfo archonInfo;

  private static final int NUM_MESSAGING_INTS = MetaInfo.META_INT_START;
  private final FastQueue<Message> messageQueue;
  private final List<Message> sentMessages;
//  private final List<Message> received;

  public Communicator() {
    this.rc = Global.rc;
//    sharedBuffer = new int[NUM_MESSAGING_INTS];
    metaInfo = new MetaInfo();
    chunkInfo = new ChunkInfo();
    archonInfo = new ArchonInfo();

    messageQueue = new FastQueue<>(10);
    sentMessages = new ArrayList<>(5);
//    received = new ArrayList<>();
  }

  /**
   * reset the sharedBuffer with the contents of the entire shared array
   */
  private void reloadBuffer() throws GameActionException {
    Utils.startByteCodeCounting("readShared");
    metaInfo.updateFromShared();
//    int toUpdate = (metaInfo.validRegionEnd - metaInfo.validRegionStart + 1 + NUM_MESSAGING_INTS) % NUM_MESSAGING_INTS;
//    int ind;
//    for (int i = 0; i < toUpdate; i++) {
//      ind = (metaInfo.validRegionStart+i) % NUM_MESSAGING_INTS;
//      sharedBuffer[ind] = rc.readSharedArray(ind);
//    }
    Utils.finishByteCodeCounting("readShared");
  }

  /**
   * clean out own stale messages if they are at the start of the valid region
   * @return if cleaned
   * @throws GameActionException if writing fails
   */
  public boolean cleanStaleMessages() throws GameActionException {
    if (!sentMessages.isEmpty()) {
//      if (rc.getRoundNum() == 1471) {
//        ////System.out.println("bounds before cleaning: " + metaInfo);
//      }
//      Message message = sentMessages.get(0);
//      int origin = metaInfo.validRegionStart;
//      int ending = metaInfo.validRegionEnd;
//      if (ending < origin) {
//        ending += NUM_MESSAGING_INTS;
//      }
//      if (ending == origin) { // no messages to read
//        return 0;
//      }
//      for (; origin < ending; origin += Message.Header.fromReadInt(rc.readSharedArray(origin%NUM_MESSAGING_INTS)).type.standardSize+1) {
//        if (message.writeInfo.startIndex == origin) {
//          //System.out.println("CLEAN " + message.header.type + ": " + origin);
          Message last = sentMessages.get(sentMessages.size() - 1);
          metaInfo.validRegionStart = (last.writeInfo.startIndex + last.size()) % NUM_MESSAGING_INTS;
          if (metaInfo.validRegionEnd == metaInfo.validRegionStart) {
            metaInfo.initializeValidRegion();
          }
          metaInfo.dirty = true;
          metaInfo.encodeAndWrite();
//          if (rc.getRoundNum() == 1471) {
//            //System.out.println("Cleaning " + (sentMessages.size() - sentMessages.indexOf(message)) + " messages!");
//            //System.out.println("Clearing messages! - starting from " + message.header.type + " on " + message.header.cyclicRoundNum + " at " + message.writeInfo.startIndex);
//            //System.out.println("Last message cleaned: " + last.header.type + " on " + message.header.cyclicRoundNum + " at " + last.writeInfo.startIndex);
//            //System.out.println("new bounds from cleaning: " + metaInfo);
//          }
//          //System.out.println("\ncleaned  - " + metaInfo);
          return true;
//        }
//      }
//      //System.out.printf("FAILED TO CLEAN MESSAGES\n%s\n1st: %s @ %d\n", metaInfo, message.header, message.writeInfo.startIndex);
//      sentMessages.clear();
    }
    return false;
  }

  /**
   * read all the messages in the sharedArray
   *    Also forward these to the robot!
   * @return the number of messages that were read
   * @throws GameActionException thrown if readMessageAt fails
   */
  public int readAndAckAllMessages() throws GameActionException {
    Utils.startByteCodeCounting("reloadBuffer");
    reloadBuffer();
//    //System.out.println("update meta - " + Clock.getBytecodeNum());
//    if (rc.getRoundNum() == 1471) {
//      //System.out.println("Reading on round 582 -- " + metaInfo);
//      //System.out.println(Arrays.toString(readInts(metaInfo.validRegionStart, metaInfo.validRegionEnd- metaInfo.validRegionStart+1)));
//    }
    Utils.finishByteCodeCounting("reloadBuffer");
//    //System.out.println("\nstarting - " + metaInfo);

    cleanStaleMessages(); // clean out stale bois
    sentMessages.clear();
//    //System.out.println("clean stale - " + Clock.getBytecodeNum());
    int origin = metaInfo.validRegionStart;
    int ending = metaInfo.validRegionEnd;
    if (ending < origin) {
      ending += NUM_MESSAGING_INTS;
    }
    if (ending == MetaInfo.EMPTY_REGION_INDICATOR) { // no messages to read
      return 0;
    }
//    //System.out.println("Reading messages: " + metaInfo);
    int messages = 0;
//    int lastAckdRound = received.isEmpty() ? 0 : getNthLastReceivedMessage(1).header.cyclicRoundNum;
//    if (!received.isEmpty()) {
//      Message last = getNthLastReceivedMessage(1);
//      //System.out.println("last message: " + last.header.type + "\t -- ");
//    }
//    int maxRoundNum = Message.Header.toCyclicRound(rc.getRoundNum());
//    if (maxRoundNum < lastAckdRound) maxRoundNum += Message.Header.ROUND_NUM_CYCLE_SIZE;
//    //System.out.println("ack messages within: (" + lastAckdRound + ", " + maxRoundNum + "]");
//    int thisRound = rc.getRoundNum();
    while (origin < ending) {
//      //System.out.println("\nBefore  read/ack message: " + Clock.getBytecodeNum());
      Message message = readMessageAt(origin % NUM_MESSAGING_INTS);
//      if (message == null) {
//        int tries = 1;
//        do {
//          message = readMessageAt(++origin % NUM_MESSAGING_INTS);
//        } while (--tries > 0 && message == null);
//      }
//      if (message != null) {
        Global.robot.ackMessage(message);
        messages++;
        origin += message.size();
////      //System.out.println("\nCost to read/ack message: " + Clock.getBytecodeNum());
//      } else {
//        metaInfo.initializeValidRegion();
//        metaInfo.encodeAndWrite();
//        break;
//      }
    }
    return messages;
  }

  /**
   * read a message from the shared array
   *    ASSUMES - messageOrigin is the start of a VALID message
   * @param messageOrigin where the message starts
   * @return the read message
   */
  private Message readMessageAt(final int messageOrigin) throws GameActionException {
//     assert messageOrigin < NUM_MESSAGING_INTS; // ensure that the message is within the messaging ints
    int headerInt = Global.rc.readSharedArray(messageOrigin);//sharedBuffer[messageOrigin];
    Message.Header header = null;
    try {
//      int beforeReadHeader = Clock.getBytecodeNum();
      header = Message.Header.fromReadInt(headerInt);
//      ////System.out.println("Cost to read header: " + (Clock.getBytecodeNum() - beforeReadHeader));
//      header.validate();
    } catch (Exception e) {
      //System.out.println("Failed to parse header! at: " + messageOrigin);
      //System.out.println("Reading bounds: " + metaInfo);
      //System.out.println("ints: " + Arrays.toString(readInts(metaInfo.validRegionStart, (metaInfo.validRegionEnd-metaInfo.validRegionStart + NUM_MESSAGING_INTS) % NUM_MESSAGING_INTS)));
      //System.out.println("Header int: " + headerInt);
      //System.out.println("Header: " + header);
//      e.printStackTrace();
//      metaInfo.validRegionStart = metaInfo.validRegionEnd = 0;
//      return null;
//      if (messageOrigin < metaInfo.validRegionEnd || (metaInfo.validRegionStart < metaInfo.validRegionEnd && messageOrigin < NUM_MESSAGING_INTS)) {
//        return readMessageAt((messageOrigin+1) % NUM_MESSAGING_INTS);
//      }
      throw e;
    }

    switch (header.type.standardSize) {
      case 0:
        return Message.fromHeaderAndInfo0(header,
          headerInt
          ).setWriteInfo(new Message.WriteInfo(messageOrigin));
      case 1:
        return Message.fromHeaderAndInfo1(header,
            Global.rc.readSharedArray((messageOrigin + 1) % NUM_MESSAGING_INTS)
          ).setWriteInfo(new Message.WriteInfo(messageOrigin));
      case 2:
        return Message.fromHeaderAndInfo2(header,
            Global.rc.readSharedArray((messageOrigin + 1) % NUM_MESSAGING_INTS),
            Global.rc.readSharedArray((messageOrigin + 2) % NUM_MESSAGING_INTS)
          ).setWriteInfo(new Message.WriteInfo(messageOrigin));
      default:
        throw new RuntimeException("Not enough cases for big message! - " + header);
    }

//    int beforeMakeInfo = Clock.getBytecodeNum();
//    int[] information = new int[header.numInformationInts];
//    for (int i = 0; i < header.numInformationInts; i++) {
//      information[i] = Global.rc.readSharedArray((messageOrigin + i + 1) % NUM_MESSAGING_INTS);// sharedBuffer[(messageOrigin + i + 1) % NUM_MESSAGING_INTS];
//    }
//    //System.out.println("Cost to make info arr: " + (Clock.getBytecodeNum() - beforeMakeInfo));
//    try {
//      return Message.fromHeaderAndInfo(header, information).setWriteInfo(new Message.WriteInfo(messageOrigin));
//    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
//      //System.out.println("Message instantiation failed!");
//      //System.out.println("Reading bounds: " + metaInfo);
//      //System.out.println("ints: " + Arrays.toString(readInts(metaInfo.validRegionStart, metaInfo.validRegionEnd-metaInfo.validRegionStart + 1)));
//      //System.out.printf("Read at %d\n", messageOrigin);
//      //System.out.println("Header int: " + headerInt);
//      throw new RuntimeException("Failed to initialize message", e);
//    }
  }

  /**
   * add a message to the internal communicator queue
   * @param message the message to send
   */
  public void enqueueMessage(Message message) {
    messageQueue.push(message);
//    if (rc.getID() == 10618) {
//      Utils.cleanPrint();
//      //Utils.print("Enqueued message: " + messageQueue.size(), "header: " + message.header);
//      Utils.submitPrint();
//    }
  }

  /**
   * reschedule a message to be sent in some number of turns
   *    should NOT happen often
   * @param message the message to reschedule
   */
  public void rescheduleMessage(Message message) {
    enqueueMessage(message);
  }

  /**
   * send all messages that should be sent by now
   * @throws GameActionException thrown if sendMessage fails
   */
  public void sendQueuedMessages() throws GameActionException {
    while (!messageQueue.isEmpty() && sendMessage(messageQueue.popFront()));
  }

  /**
   * write a certain message to the shared array
   * starts message after validRegionEnd and bumps validRegionStart as needed if ints are overwritten
   * @param message the message to write
   * @returns true if sent, false if rescheduled
   * @throws GameActionException thrown if writing to array fails
   */
  private boolean sendMessage(Message message) throws GameActionException {
    if (Clock.getBytecodesLeft() < MIN_BYTECODES_TO_SEND_MESSAGE) {
      rescheduleMessage(message);
//      //System.out.println("reschedule for bc - " + message.header);
//      //System.out.printf("---\nRESCHEDULE  %s:\n%d - %s\n", message.header.type, Clock.getBytecodesLeft(), Arrays.toString(message.toEncodedInts()));
      return false;
    }
//    //System.out.println("pre-reload -- bc: " + Clock.getBytecodesLeft());
    metaInfo.reloadValidRegion();
//    //System.out.println("pre-sending -- bc: " + Clock.getBytecodesLeft());
//    //System.out.println("metaInfo: " + metaInfo);
//    //System.out.println("Current ints: " + ((metaInfo.validRegionEnd-metaInfo.validRegionStart+NUM_MESSAGING_INTS) % NUM_MESSAGING_INTS) + "\nnew ints: " + message.size() + "\nlimit: " + NUM_MESSAGING_INTS);
    if (metaInfo.validRegionEnd != MetaInfo.EMPTY_REGION_INDICATOR
        && ((metaInfo.validRegionEnd-metaInfo.validRegionStart+NUM_MESSAGING_INTS) % NUM_MESSAGING_INTS) + message.size() >= NUM_MESSAGING_INTS) { // will try to write more ints than available
      rescheduleMessage(message);
//      //System.out.println("reschedule for out of space - " + message.header);
//      //System.out.printf("---\nRESCHEDULE  %s:\n%d - %s\n", message.header.type, Clock.getBytecodesLeft(), Arrays.toString(message.toEncodedInts()));
      return false;
    }
    boolean updateStart = metaInfo.validRegionStart == MetaInfo.EMPTY_REGION_INDICATOR; // no valid messages currently
    int[] messageBits = message.toEncodedInts();
    int origin = metaInfo.validRegionEnd % NUM_MESSAGING_INTS;
    int messageOrigin = origin;
//    //System.out.printf("---\nSEND  %s:\n%d - %s\n%s\nbc: %d\n", message.header.type, messageOrigin, Arrays.toString(messageBits),metaInfo,Clock.getBytecodesLeft());
//    //Utils.print(String.format("SEND  %s:\n%d - %s\n", message.header.type, messageOrigin, Arrays.toString(messageBits)));
    //rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 0,0,0);
//    //System.out.println(message.header);
    for (int messageChunk : messageBits) {
//      if (origin == metaInfo.validRegionStart) { // about to overwrite the start!
//        Message messageAt = readMessageAt(origin);
//        metaInfo.validRegionStart += messageAt != null ? messageAt.size() : 1;
//        metaInfo.validRegionStart %= NUM_MESSAGING_INTS;
//        //System.out.println("OVERWROTE STALE MESSAGE");
//        //System.out.println(messageAt.header);
//        throw new RuntimeException("Shouldn't overwrite message buffer");
//      }
//      //System.out.println("Write to shared " + origin + ": " + messageChunk);
      rc.writeSharedArray(origin, messageChunk);
      origin = (origin + 1) % NUM_MESSAGING_INTS;
    }
    sentMessages.add(message);
    //rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 0,255,0);
    metaInfo.validRegionEnd = origin;//(origin+1) % NUM_MESSAGING_INTS;
    if (updateStart) { // first message!
      metaInfo.validRegionStart = messageOrigin;
//      if (metaInfo.validRegionStart < 0) metaInfo.validRegionStart += NUM_MESSAGING_INTS;
//      //System.out.println("Move start: " + metaInfo);
    }
    message.setWriteInfo(new Message.WriteInfo(messageOrigin));
    metaInfo.dirty = true;
    metaInfo.encodeAndWrite();
    return true;
  }

  /**
   * updates the meta ints in the shared memory if needed
   *    comunicator is dirty
   *      any messages were written
   * @return true if updated
   * @throws GameActionException if updating fails
   */
  public boolean updateMetaIntsIfNeeded() throws GameActionException {
//    //System.out.println("\nend turn - " + metaInfo);
    return metaInfo.encodeAndWrite();
  }

  /**
   * returns true if the integer at the specified index matches the provided message header
   *    ASSUMES data has already been read into sharedBuffer
   * @param headerIndex the index to check
   * @param header the message metadata to verify
   * @return the sameness
   * @throws GameActionException if reading fails
   */
  public boolean headerMatches(int headerIndex, Message.Header header) throws GameActionException {
//    //System.out.println("Checking header at " + headerIndex + ": " + sharedBuffer[headerIndex] + " -- " + header.toInt());
//    return sharedBuffer[headerIndex] == header.toInt();
    return Global.rc.readSharedArray(headerIndex) == header.toInt();
  }

  /**
   * reads numInts integers starting at startIndex into an integer array and sends them back for use
   *    reads from ALREADY processed sharedBuffer
   *    loops around based on NUM_MESSAGING_BITS
   * @param startIndex where to start
   * @param numInts the number of ints to read
   * @return the array of read ints
   * @throws GameActionException if reading fails
   */
  public int[] readInts(int startIndex, int numInts) throws GameActionException {
//    //System.out.println("Read ints at " + startIndex + ": " + numInts);
    int[] ints = new int[numInts];
    for (int i = 0; i < numInts; i++) {
      ints[i] = Global.rc.readSharedArray((startIndex+i) % NUM_MESSAGING_INTS);//sharedBuffer[(startIndex+i) % NUM_MESSAGING_INTS];
    }
    return ints;
  }

  /**
   * write a set of ints into the message buffer starting at the given index
   *    cycles indices based on NUM_MESSAGING_INTS
   * @param startIndex where to start writing
   * @param information the ints to write
   * @throws GameActionException if writing fails
   */
  public void writeInts(int startIndex, int[] information) throws GameActionException {
//    //System.out.println("Write ints at " + startIndex + ": " + Arrays.toString(information));
    for (int i = 0; i < information.length; i++) {
      rc.writeSharedArray((startIndex + i) % NUM_MESSAGING_INTS, information[i]);
    }
  }
}
