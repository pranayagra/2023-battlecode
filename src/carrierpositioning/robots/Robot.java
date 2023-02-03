package carrierpositioning.robots;

import carrierpositioning.communications.CommsHandler;
import carrierpositioning.communications.Communicator;
import carrierpositioning.communications.HqMetaInfo;
import carrierpositioning.knowledge.Memory;
import carrierpositioning.knowledge.RunningMemory;
import carrierpositioning.knowledge.WellData;
import carrierpositioning.robots.micro.AttackMicro;
import carrierpositioning.robots.micro.AttackerFightingMicro;
import carrierpositioning.robots.micro.CarrierWellMicro;
import carrierpositioning.robots.pathfinding.BugNav;
import carrierpositioning.robots.pathfinding.Pathing;
import carrierpositioning.knowledge.Cache;
import carrierpositioning.utils.Global;
import carrierpositioning.utils.Printer;
import carrierpositioning.utils.Utils;
import battlecode.common.*;

public abstract class Robot {
  private static final boolean RESIGN_ON_GAME_EXCEPTION = false;
  private static final boolean RESIGN_ON_RUNTIME_EXCEPTION = false;

  private static final int MAX_TURNS_FIGURE_SYMMETRY = 500;

  protected final RobotController rc;
  

  protected int turnCount;
  protected boolean dontYield;

  protected MapLocation closestCommedEnemy;
  protected int distToClosestCommedEnemy;

  protected final Pathing pathing;


  public int testCount;

  /**
   * Create a Robot with the given controller
   * Perform various setup tasks generic to ny robot (building/droid)
   * @param rc the controller
   */
  public Robot(RobotController rc) throws GameActionException {
    this.rc = rc;

    Global.setupGlobals(rc, this);
    Utils.setUpStatics();
    Memory.init();
    Printer.cleanPrint();
    Communicator.init(rc);

    pathing = Pathing.create(rc);
    AttackerFightingMicro.init(rc, pathing);
    AttackMicro.init(rc);
    CarrierWellMicro.init();

    Printer.appendToIndicator("Just spawned!");
    turnCount = -1;
  }

  /**
   * Create a Robot-subclass instance from the provided controller
   * @param rc the controller object
   * @return a custom Robot instance
   */
  public static Robot fromRC(RobotController rc) throws GameActionException {
    switch (rc.getType()) {
      case HEADQUARTERS: return new HeadQuarters(rc);
      case CARRIER: return new Carrier(rc);
      case LAUNCHER: return new Launcher(rc);
      case DESTABILIZER: return new Destabilizer(rc);
      case BOOSTER: return new Booster(rc);
      case AMPLIFIER: return new Amplifier(rc);
      default: throw new RuntimeException("Cannot create Robot-subclass for invalid RobotType: " + rc.getType());
    }
  }

  /**
   * Run the wrapper for the main loop function of the robot
   * This is generic to all robots
   */
  public void runLoop() throws GameActionException {
        /*
          should never exit - Robot will die otherwise (Clock.yield() to end turn)
         */
    while (true) {
      // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
      try {
        this.runTurnWrapper();
        Printer.submitPrint();
      } catch (GameActionException e) {
        // something illegal in the Battlecode world
        System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + ".TLE?=" + (rc.getRoundNum() != Cache.PerTurn.ROUND_NUM) + " GameActionException");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION) die();
      } catch (Exception e) {
        // something bad
        System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + ".TLE?=" + (rc.getRoundNum() != Cache.PerTurn.ROUND_NUM) + " Exception");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION || RESIGN_ON_RUNTIME_EXCEPTION) die();
      } catch (AssertionError e) {
        // some assertion failed
        System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + ".TLE?=" + (rc.getRoundNum() != Cache.PerTurn.ROUND_NUM) + " AssertionError");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION || RESIGN_ON_RUNTIME_EXCEPTION) die();
      } finally {
        // end turn - make code wait until next turn
        if (!dontYield) Clock.yield();
        else {
          if (Clock.getBytecodesLeft() < 0.8 * Cache.Permanent.ROBOT_TYPE.bytecodeLimit) { // if don't have 80% of limit, still yield
            dontYield = false;
            Clock.yield();
//          } else {
//            System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + "Skipping turn yeild!!");
          }
        }
      }
      Printer.cleanPrint();
    }
  }

  protected void die() {
    Printer.submitPrint();
    Clock.yield();
    Clock.yield();
    rc.resign();
  }

  /**
   * wrap intern run turn method with generic actions for all robots
   */
  private void runTurnWrapper() throws GameActionException {
//        System.out.println("Age: " + turnCount + "; Location: " + Cache.PerTurn.CURRENT_LOCATION);
    if (!dontYield) {
      Printer.appendToIndicator("a" + rc.getActionCooldownTurns() + "m" + rc.getMovementCooldownTurns());
    }
    dontYield = false;

    Memory.updateOnTurn();

    // PRE MESSAGE READING -----
    closestCommedEnemy = null;
    distToClosestCommedEnemy = 999999;

//        pathing.initTurn();

//    Utils.startByteCodeCounting("updating-comm-metainfo");
    Communicator.MetaInfo.updateOnTurnStart();
//    Utils.finishByteCodeCounting("updating-comm-metainfo");

    MapLocation initial = Cache.PerTurn.CURRENT_LOCATION;

//    Printer.print("" + Cache.PerTurn.ROUNDS_ALIVE);
    if (Cache.PerTurn.ROUND_NUM < 20 && Cache.PerTurn.ROUNDS_ALIVE == 0) {
//      Printer.print("hi");
      initialWellExploration();
    }

    runTurnTypeWrapper();

    // if the bot moved on its turn
    if (!initial.equals(Cache.PerTurn.CURRENT_LOCATION)) {
      afterTurnWhenMoved();
    }

    commNearbyEnemies();

    // if mobile unit,
    if (Cache.Permanent.ROBOT_TYPE != RobotType.HEADQUARTERS) {
      islandMobileBotsProtocol();
    }

    if (++turnCount != rc.getRoundNum() - Cache.Permanent.ROUND_SPAWNED) { // took too much bytecode
      rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,0,255); // MAGENTA IF RAN OUT OF BYTECODE
//      Printer.print(Cache.Permanent.ROBOT_TYPE + " ran out of bytecode! Overused: " + Clock.getBytecodeNum() + " bytecode");
      turnCount = rc.getRoundNum() - Cache.Permanent.ROUND_SPAWNED;
      dontYield = true;
    } else { // still on our turn logic -- finish turn with last few potentially expensive things
//    if (Clock.getBytecodesLeft() >= MIN_BYTECODES_TO_SEND) {
//    }
    }
//    System.out.println("\nvery end - " + rc.readSharedArray(Communicator.MetaInfo.META_INT_START));
  }

  /**
   * Run a single turn for the robot
   * unique to buildings/droids
   */
  protected void runTurnTypeWrapper() throws GameActionException {
    runTurn();
  }

  /**
   * Run a single turn for the robot
   * unique to each robot type
   */
  protected abstract void runTurn() throws GameActionException;

  /**
   * run this code after the robot completes its turn
   *    should ONLY be run if the robot moved on this turn
   *  updates symmetry (broadcast rubble to check for symmetry failure)
   *  update chunks in the chunk info buffer of the shared array
   * @throws GameActionException if updating symmetry or visible chunks fails
   */
  protected void afterTurnWhenMoved() throws GameActionException {
    updateSymmetryComms();
    updateWellExploration();
    updateEnemyHQAvoidance();
    // write to comms
    broadcastMemoryToComms();

  }

  protected void broadcastMemoryToComms() throws GameActionException {
    int wellsBroadcast = RunningMemory.broadcastMemorizedWells();
  }


  protected IslandInfo getClosestFriendlyIsland() {
    int closestDist = Integer.MAX_VALUE;
    int closestEuclideanDist = Integer.MAX_VALUE;
    IslandInfo closest = null;
    for (int i = 0; i < IslandInfo.MAX_ISLAND_COUNT; ++i) {
      IslandInfo islandInfo = getIslandInformation(i);
      if (islandInfo != null && islandInfo.islandTeam == Cache.Permanent.OUR_TEAM) {
        int dist = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, islandInfo.islandLocation);
        int euclideanDist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(islandInfo.islandLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closest = islandInfo;
          closestEuclideanDist = euclideanDist;
        } else if (dist == closestDist && euclideanDist < closestEuclideanDist) {
          closestEuclideanDist = euclideanDist;
          closest = islandInfo;
        }
      }
    }

    return closest;
  }

  protected IslandInfo getClosestUnclaimedIsland() {
    int closestDist = Integer.MAX_VALUE;
    int closestEuclideanDist = Integer.MAX_VALUE;
    IslandInfo closest = null;
    for (int i = 0; i < IslandInfo.MAX_ISLAND_COUNT; ++i) {
      IslandInfo islandInfo = getIslandInformation(i);
      if (islandInfo != null && islandInfo.islandTeam == Team.NEUTRAL) {
        int dist = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, islandInfo.islandLocation);
        int euclideanDist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(islandInfo.islandLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closest = islandInfo;
          closestEuclideanDist = euclideanDist;
        } else if (dist == closestDist && euclideanDist < closestEuclideanDist) {
          closestEuclideanDist = euclideanDist;
          closest = islandInfo;
        }
      }
    }

    return closest;
  }

  protected IslandInfo getClosestEnemyIsland() {
    int closestDist = Integer.MAX_VALUE;
    int closestEuclideanDist = Integer.MAX_VALUE;
    IslandInfo closest = null;
    for (int i = 0; i < IslandInfo.MAX_ISLAND_COUNT; ++i) {
      IslandInfo islandInfo = getIslandInformation(i);
      if (islandInfo != null && islandInfo.islandTeam == Cache.Permanent.OPPONENT_TEAM) {
        int dist = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, islandInfo.islandLocation);
        int euclideanDist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(islandInfo.islandLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closest = islandInfo;
          closestEuclideanDist = euclideanDist;
        } else if (dist == closestDist && euclideanDist < closestEuclideanDist) {
          closestEuclideanDist = euclideanDist;
          closest = islandInfo;
        }
      }
    }

    return closest;
  }

  // ID => [loc, roundNum, owner]
  class IslandInfo {
    public static final int MAX_ISLAND_COUNT = GameConstants.MAX_NUMBER_ISLANDS + 1;
    
    public MapLocation islandLocation;
    public int islandId;
    public int roundNum;
    public Team islandTeam;

    public IslandInfo(MapLocation islandLocation, int islandId, int roundNum, Team islandTeam) {
      this.islandLocation = islandLocation;
      this.islandId = islandId;
      this.roundNum = roundNum;
      this.islandTeam = islandTeam;
    }

    public IslandInfo(IslandInfo other) {
      this.islandLocation = other.islandLocation;
      this.islandId = other.islandId;
      this.roundNum = other.roundNum;
      this.islandTeam = other.islandTeam;
    }

    public String toString() {
      return "IslandInfo{" +
          "islandLocation=" + islandLocation +
          ", islandId=" + islandId +
          ", roundNum=" + roundNum +
          ", islandTeam=" + islandTeam +
          '}';
    }

    /**
     * Updates the location of the island to the closest location we can see
     * @param fromHere the location to check closest to
     * @return the new location of the island (may not change if no closer location is found)
     */
    public MapLocation updateLocationToClosestOpenLocation(MapLocation fromHere) throws GameActionException {
      MapLocation[] mapLocations = rc.senseNearbyIslandLocations(islandId);
      if (mapLocations.length == 0) {
        return islandLocation;
      } else {
        // I can see the island in vision, let's find a good spot to heal from
        int closestDist = Integer.MAX_VALUE;
        int closestEuclideanDist = Integer.MAX_VALUE;
        MapLocation closest = null;
        for (int i = mapLocations.length; --i >= 0;) {
          MapLocation mapLocation = mapLocations[i];
          if (!rc.isLocationOccupied(mapLocation)) {
            int dist = Utils.maxSingleAxisDist(fromHere, mapLocation);
            int euclideanDist = fromHere.distanceSquaredTo(mapLocation);
            if (dist < closestDist) {
              closestDist = dist;
              closestEuclideanDist = euclideanDist;
              closest = mapLocation;
            } else if (dist == closestDist && euclideanDist < closestEuclideanDist) {
              closestEuclideanDist = euclideanDist;
              closest = mapLocation;
            }
          }
        }

//      Printer.appendToIndicator(" closest=" + closest);
        // I can see the island but I can't find a good spot to heal from, circle around the island?
        if (closest == null) {
          return islandLocation;
        } else {
          if (localIslandInfo[islandId] == null) {
            localIslandInfo[islandId] = new IslandInfo(closest, islandId, Cache.PerTurn.ROUND_NUM, islandTeam);
          } else {
            localIslandInfo[islandId].islandLocation = closest;
            localIslandInfo[islandId].roundNum = Cache.PerTurn.ROUND_NUM;
          }
          return closest;
        }
      }
    }

    public void updateTeam(Team newOwner) {
      islandTeam = newOwner;
      roundNum = Cache.PerTurn.ROUND_NUM;
    }
  }

  protected int teamToInt(Team t) {
    return t.ordinal();
  }

  protected Team intToTeam(int i) {
    return Team.values()[i];
  }

  protected IslandInfo[] globalIslandInfo = new IslandInfo[IslandInfo.MAX_ISLAND_COUNT];

  protected IslandInfo[] localIslandInfo = new IslandInfo[IslandInfo.MAX_ISLAND_COUNT];

  private void observeIslandsNearby() throws GameActionException {
    int[] islandIds = rc.senseNearbyIslands();
    for (int i = Math.min(islandIds.length, 3); --i >= 0;) {
      if (Clock.getBytecodesLeft() < 100) break;
      int islandId = islandIds[i];
      try { // TODO: remove once they patch this
        if (localIslandInfo[islandId] != null) {
          localIslandInfo[islandId].roundNum = Cache.PerTurn.ROUND_NUM;
          localIslandInfo[islandId].islandTeam = rc.senseTeamOccupyingIsland(islandId);
        } else {
          Team team = rc.senseTeamOccupyingIsland(islandId);
          MapLocation islandLocation = rc.senseNearbyIslandLocations(islandId)[0];
          localIslandInfo[islandId] = new IslandInfo(islandLocation, islandId, Cache.PerTurn.ROUND_NUM, team);
        }
      } catch (GameActionException e) {
        /*BASICBOT_ONLY*///Printer.print("Error sensing for island " + islandId);
      }
    }
  }

  private void debugIslandComms() {
    if (Cache.PerTurn.ROUND_NUM % 20 == 0) {
      for (int i = 0; i < IslandInfo.MAX_ISLAND_COUNT; ++i) {
        Printer.print("island " + i + " " + globalIslandInfo[i] + " " + localIslandInfo[i]);
      }
    }
  }

  protected void islandMobileBotsProtocol() throws GameActionException {
    updateIslandInfoMemoryFromComms();
    observeIslandsNearby();
    commIslandInformation();
//    debugIslandComms();
  }

  // lazy comming (even if roundNum is oudated, as long as team is the same, do not comm!)
  private void commIslandInformation() throws GameActionException {
    if (!rc.canWriteSharedArray(0, 0) || Cache.PerTurn.ROUND_NUM % 2 == 0) return;
    if (CommsHandler.readIslandInfoExists()) { // someone just wrote some new info to the array, can I replace it?
      // need to read and see if we have more updated info or not
      int islandID = CommsHandler.readIslandInfoIslandId();
      IslandInfo localInfo = localIslandInfo[islandID];
      if (localInfo == null) return;
      int roundNum = CommsHandler.readIslandInfoRoundNum();
      Team team = intToTeam(CommsHandler.readIslandInfoOwner());
      if (team != localInfo.islandTeam && roundNum < localInfo.roundNum) {
        // we have more updated info, so we need to overwrite
//        Printer.print("overwriting island " + islandID + ", global=" + globalIslandInfo[islandID] + " local=" + localInfo);
        globalIslandInfo[islandID] = new IslandInfo(localInfo);
        int newTeam = teamToInt(localInfo.islandTeam);
        CommsHandler.writeIslandInfoOwner(newTeam);
        CommsHandler.writeIslandInfoRoundNum(localInfo.roundNum);
        // do not need to write location or ID (both are the same)
      }
      return;
    }

    for (int i = 0; i < IslandInfo.MAX_ISLAND_COUNT; ++i) {
      IslandInfo globalInfo = globalIslandInfo[i];
      IslandInfo localInfo = localIslandInfo[i];
      if (localInfo != null) {
        if (globalInfo == null || (globalInfo.islandTeam != localInfo.islandTeam && globalInfo.roundNum < localInfo.roundNum)) {
          // we have more updated info, so we need to overwrite
//          Printer.print("overwriting island " + i + ", global=" + globalIslandInfo[i] + " local=" + localInfo);
          globalIslandInfo[i] = new IslandInfo(localInfo); //do I need to new here / reference issue?
          int newTeam = teamToInt(localInfo.islandTeam);
          CommsHandler.writeIslandInfoOwner(newTeam);
          CommsHandler.writeIslandInfoRoundNum(localInfo.roundNum);
          CommsHandler.writeIslandInfoLocation(localInfo.islandLocation);
          CommsHandler.writeIslandInfoIslandId(localInfo.islandId);
          return;
        }
      }
    }
  }

  protected void updateIslandInfoMemoryFromComms() throws GameActionException {
    if (CommsHandler.readIslandInfoExists()) {
      MapLocation location = CommsHandler.readIslandInfoLocation();
      int roundNum = CommsHandler.readIslandInfoRoundNum();
      Team team = intToTeam(CommsHandler.readIslandInfoOwner());
      int islandId = CommsHandler.readIslandInfoIslandId();
//      if (Cache.Permanent.ROBOT_TYPE == RobotType.HEADQUARTERS) Printer.print("read any island info " + location + " " + roundNum + " " + team + " " + islandId);
      if (globalIslandInfo[islandId] == null) {
        globalIslandInfo[islandId] = new IslandInfo(location, islandId, roundNum, team);
      } else if (globalIslandInfo[islandId].roundNum < roundNum) {
        globalIslandInfo[islandId].roundNum = roundNum;
        globalIslandInfo[islandId].islandTeam = team;
      }
    }
    if (CommsHandler.readMyIslandsExists()) {
      MapLocation location = CommsHandler.readMyIslandsLocation();
      int roundNum = CommsHandler.readMyIslandsRoundNum();
      Team team = Cache.Permanent.OUR_TEAM;
      int islandId = CommsHandler.readMyIslandsIslandId();
//      if (Cache.Permanent.ROBOT_TYPE == RobotType.HEADQUARTERS) Printer.print("read my island info " + location + " " + roundNum + " " + team + " " + islandId);

      if (globalIslandInfo[islandId] == null) {
        globalIslandInfo[islandId] = new IslandInfo(location, islandId, roundNum, team);
      } else if (globalIslandInfo[islandId].roundNum < roundNum) {
        globalIslandInfo[islandId].roundNum = roundNum;
        globalIslandInfo[islandId].islandTeam = team;
      }
    }
  }

  protected IslandInfo getIslandInformation(int id) {
    IslandInfo globalInfo = globalIslandInfo[id];
    IslandInfo localInfo = localIslandInfo[id];
//    if (rc.getID() == 11731 && Cache.PerTurn.ROUND_NUM >= 450 && Cache.PerTurn.ROUND_NUM <= 460) {
//      Printer.print(id + ", global " + globalInfo + ", local " + localInfo);
//    }
    if (globalInfo != null && localInfo != null) {
      if (globalInfo.roundNum > localInfo.roundNum) {
        return globalInfo;
      } else {
        return localInfo;
      }
    } else if (globalInfo != null) {
      return globalInfo;
    } else {
      return localInfo;
    }
  }

  /**
   * Updates the pathing to avoid enemy HQs (since they do damage to us).
   * @throws GameActionException
   */
  protected void updateEnemyHQAvoidance() throws GameActionException {
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (robot.type != RobotType.HEADQUARTERS) continue;
      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(robot.location, RobotType.HEADQUARTERS.actionRadiusSquared)) {
        BugNav.blockedLocations.clear();
      }
      int distToBlock = Math.min(RobotType.HEADQUARTERS.actionRadiusSquared, Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(robot.location) - 1);
      // TODO: optimize me lol that's alotta bytecode
      for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(robot.location, distToBlock)) {
        if (!BugNav.blockedLocations.contains(loc)) BugNav.blockedLocations.add(loc);
      }
    }
  }
  /**
   * perform any universal code that robots should run to figure out map symmetry
   *    Currently - broadcast my location + the rubble there
   * @throws GameActionException if sensing fails
   */
  protected void updateSymmetryComms() throws GameActionException {
    if (RunningMemory.knownSymmetry != null) {
      if (RunningMemory.symmetryInfoDirty) RunningMemory.broadcastSymmetry();
      return;
    }
//    if (Cache.PerTurn.ROUND_NUM > MAX_TURNS_FIGURE_SYMMETRY) return;

    int midlineThreshold = Cache.Permanent.VISION_RADIUS_FLOOR / 2;
    MapLocation myLoc = Cache.PerTurn.CURRENT_LOCATION;
    int myX = myLoc.x;
    int myY = myLoc.y;
    int mapWidth = Cache.Permanent.MAP_WIDTH;
    int mapHeight = Cache.Permanent.MAP_HEIGHT;
    // if Vertical not ruled out (flipY, horizontal midline)
    if (!RunningMemory.notVerticalSymmetry) { // could be vertical
      // check if y is near the middle
      nearHorizMidline:
      if (myY * 2 <= mapHeight + midlineThreshold
          && myY * 2 >= mapHeight - midlineThreshold) {
        MapLocation test1 = new MapLocation(myX, mapHeight / 2 - 1);
        MapLocation test2 = new MapLocation(myX, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.VERTICAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.VERTICAL); // eliminate Vertical symmetry
          break nearHorizMidline;
        }
        test1 = new MapLocation(myX - 2, mapHeight / 2 - 1);
        test2 = new MapLocation(myX - 2, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.VERTICAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.VERTICAL); // eliminate Vertical symmetry
          break nearHorizMidline;
        }
        test1 = new MapLocation(myX + 2, mapHeight / 2 - 1);
        test2 = new MapLocation(myX + 2, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.VERTICAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.VERTICAL); // eliminate Vertical symmetry
          break nearHorizMidline;
        }
      }
    }
    if (RunningMemory.knownSymmetry != null) {
      if (RunningMemory.symmetryInfoDirty) RunningMemory.broadcastSymmetry();
      return;
    }
    // if Horizontal not ruled out (flipX, vertical midline)
    if (!RunningMemory.notHorizontalSymmetry) { // could be horizontal
      // check if x is near the middle
      nearVertMidline:
      if (myX * 2 <= mapWidth + midlineThreshold
          && myX * 2 >= mapWidth - midlineThreshold) {
        MapLocation test1 = new MapLocation(mapWidth / 2 - 1, myY);
        MapLocation test2 = new MapLocation(mapWidth - mapWidth / 2, myY);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.HORIZONTAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.HORIZONTAL); // eliminate Horizontal symmetry
          break nearVertMidline;
        }
        test1 = new MapLocation(mapWidth / 2 - 1, myY - 2);
        test2 = new MapLocation(mapWidth - mapWidth / 2, myY - 2);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.HORIZONTAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.HORIZONTAL); // eliminate Horizontal symmetry
          break nearVertMidline;
        }
        test1 = new MapLocation(mapWidth / 2 - 1, myY + 2);
        test2 = new MapLocation(mapWidth - mapWidth / 2, myY + 2);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.HORIZONTAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.HORIZONTAL); // eliminate Horizontal symmetry
          break nearVertMidline;
        }
      }
    }
    if (RunningMemory.knownSymmetry != null) {
      if (RunningMemory.symmetryInfoDirty) RunningMemory.broadcastSymmetry();
      return;
    }
    // if Rotational not ruled out (rotXY, near center)
    if (!RunningMemory.notRotationalSymmetry) { // could be rotational
      // check if near the center
      nearCenter:
      if (myX * 2 <= mapWidth + midlineThreshold
          && myX * 2 >= mapWidth - midlineThreshold
          && myY * 2 <= mapHeight + midlineThreshold
          && myY * 2 >= mapHeight - midlineThreshold) {
        MapLocation test1 = new MapLocation(mapWidth / 2 - 1, mapHeight / 2 - 1);
        MapLocation test2 = new MapLocation(mapWidth - mapWidth / 2, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.ROTATIONAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.ROTATIONAL); // eliminate Rotational symmetry
          break nearCenter;
        }
        test1 = new MapLocation(mapWidth / 2 - 1, mapHeight - mapHeight / 2);
        test2 = new MapLocation(mapWidth - mapWidth / 2, mapHeight / 2 - 1);
        if (checkFailsSymmetry(test1, test2, Utils.MapSymmetry.ROTATIONAL)) {
          RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.ROTATIONAL); // eliminate Rotational symmetry
          break nearCenter;
        }
      }
    }
    if (RunningMemory.knownSymmetry != null) {
      if (RunningMemory.symmetryInfoDirty) RunningMemory.broadcastSymmetry();
      return;
    }
    // check for enemy HQ
    for (MapLocation myHQ : HqMetaInfo.hqLocations) {
      MapLocation enemyHQ;
      if (!RunningMemory.notRotationalSymmetry) {
        enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.ROTATIONAL);
        if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
          RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
          if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
            RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.ROTATIONAL);
          }
        }
      }
      if (!RunningMemory.notHorizontalSymmetry) {
        enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.HORIZONTAL);
        if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
          RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
          if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
            RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.HORIZONTAL);
          }
        }
      }
      if (!RunningMemory.notVerticalSymmetry) {
        enemyHQ = Utils.applySymmetry(myHQ, Utils.MapSymmetry.VERTICAL);
        if (rc.canSenseLocation(enemyHQ)) {
//            Printer.print("Checking for enemy HQ at " + enemyHQ);
          RobotInfo robot = rc.senseRobotAtLocation(enemyHQ);
          if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
            RunningMemory.markInvalidSymmetry(Utils.MapSymmetry.VERTICAL);
          }
        }
      }
    }

    RunningMemory.broadcastSymmetry();
  }
  private boolean checkFailsSymmetry(MapLocation test1, MapLocation test2, Utils.MapSymmetry symmetryToCheck) throws GameActionException {
    /*BASICBOT_ONLY*///rc.setIndicatorDot(test1, 211, 211, 211);
    /*BASICBOT_ONLY*///rc.setIndicatorDot(test2, 211, 211, 211);
    int visionRadiusSq = Cache.PerTurn.IS_IN_CLOUD ? GameConstants.CLOUD_VISION_RADIUS_SQUARED : Cache.Permanent.VISION_RADIUS_SQUARED;
    if (rc.onTheMap(test1)
        && rc.onTheMap(test2)
        && test1.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, visionRadiusSq)
        && test2.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, visionRadiusSq)) {
      if (rc.senseCloud(test1) != rc.senseCloud(test2)) {
        return true;
      }
      if (rc.canSenseLocation(test1) && rc.canSenseLocation(test2)) {
        if (rc.sensePassability(test1) != rc.sensePassability(test2) || rc.senseMapInfo(test1).getCurrentDirection() != Utils.applySymmetry(rc.senseMapInfo(test2).getCurrentDirection(), symmetryToCheck)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * does some early game well checking right when spawned
   * publish wells if none of that type exist so far
   * @throws GameActionException any exception with sensing or writing to shared array
   */
  private void initialWellExploration() throws GameActionException {
    // turn off optimization unless we need bytecode back
//    boolean needAd = !Communicator.anyWellExists(ResourceType.ADAMANTIUM);
//    boolean needMana = !Communicator.anyWellExists(ResourceType.MANA);
//    boolean needElixir = !Communicator.anyWellExists(ResourceType.ELIXIR);
//    if (!needAd && !needMana && !needElixir) return;
    boolean needAd = true;
    boolean needMana = true;
    boolean needElixir = true;
//    die();
//    if (needAd || needMana) {
//      Printer.print("Early game checking for wells: needAd=" + needAd + ", needMana=" + needMana);
//    }
    for (WellInfo wellInfo : rc.senseNearbyWells()) {
      // Printer.print("well:" + wellInfo);
      switch (wellInfo.getResourceType()) {
        case ADAMANTIUM:
          if (needAd) {
            needAd = false;
            publishWellData(wellInfo);
//            Printer.print("Early game well publishing: Ad @ " + wellInfo.getMapLocation());
          }
          break;
        case MANA:
          if (needMana) {
            needMana = false;
            publishWellData(wellInfo);
//            Printer.print("Early game well publishing: Mana @ " + wellInfo.getMapLocation());
          }
          break;
        case ELIXIR:
          if (needElixir) {
            needElixir = false;
            publishWellData(wellInfo);
//            Printer.print("Early game well publishing: Elixir @ " + wellInfo.getMapLocation());
          }
          break;
      }
    }
    RunningMemory.broadcastMemorizedWells();
  }

  /**
   * If a nearby well is seen, put it in local memory
   * @throws GameActionException any issues during reading/writing
   */
  protected void updateWellExploration() throws GameActionException {
//    if (!rc.canWriteSharedArray(0,0)) return;
    if (Cache.PerTurn.hasPreviouslyVisitedOwnLoc()) return;
    if (Clock.getBytecodesLeft() < 200) return;
    int wellsPublished = 0;
    for (WellInfo well : rc.senseNearbyWells()) {
      if (publishWellData(well)) {
        wellsPublished++;
      }
    }
  }

  /**
   * Converts wellInfo to wellData and writes to running memory
   * true if published, false if not
   */
  private boolean publishWellData(WellInfo wellInfo) throws GameActionException {
    // see all locations around well
    MapLocation wellLoc = wellInfo.getMapLocation();
    int capacity = 0;
    if (Clock.getBytecodesLeft() >= 1000 && Cache.PerTurn.ROUND_NUM == rc.getRoundNum()) { // not enough bytecode to measure this
      final int visionRadiusSq = Cache.PerTurn.IS_IN_CLOUD ? GameConstants.CLOUD_VISION_RADIUS_SQUARED : Cache.Permanent.VISION_RADIUS_SQUARED;
      for (Direction dir : Utils.directionsNine) {
        MapLocation loc = wellLoc.add(dir);
        if (!loc.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, visionRadiusSq)) continue;
        if (CarrierWellMicro.isValidStaticQueuePosition(wellLoc, loc)) {
          capacity++;
        }
      }
    }
    return RunningMemory.publishWell(new WellData(wellInfo, capacity));
  }

  protected WellInfo getClosestWell(ResourceType type) throws GameActionException {
    WellInfo[] wells = Global.rc.senseNearbyWells(type);
    if (wells.length == 0) {
      return null;
    }
    WellInfo closest = wells[0];
    int closestDist = closest.getMapLocation().distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
    for (int i = 1; i < wells.length; i++) {
      MapLocation well = wells[i].getMapLocation();
      int dist = well.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
      if (dist < closestDist) {
        closest = wells[i];
        closestDist = dist;
      }
    }
    return closest;
  }

  /**
   * check at the end of the turn if new enemies should be commed
   * @throws GameActionException if sending the message fails
   */
  protected void commNearbyEnemies() throws GameActionException {
    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length == 0) return;

    // no already seen enemy or closest seen is very far
//      Printer.cleanPrint();
//      Printer.print("closestCommedEnemy: " + closestCommedEnemy, "enemy: " + enemy);
//      if (closestCommedEnemy != null) {
//        Printer.print("dist: " + closestCommedEnemy.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION));
//      }
//      Printer.submitPrint();
//      if (closestCommedEnemy == null
//          || !closestCommedEnemy.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.VISION_RADIUS_SQUARED)) {
////        communicator.enqueueMessage(new EnemyFoundMessage(enemy));
//      }
    if (!rc.canWriteSharedArray(0,0)) return;
    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    int commedEnemies = 0;
    for (int i = allNearbyEnemyRobots.length; --i >= 0;) {
      if (Clock.getBytecodesLeft() < 200) return;
      RobotInfo enemy = allNearbyEnemyRobots[i];
      switch (enemy.type) {
        case CARRIER:
        case HEADQUARTERS:
          break;
        default:
          Communicator.writeEnemy(enemy);
          if (++commedEnemies >= 5) {
            return;
          }
      }
    }
  }



  /**
   * check if there are any enemy (soldiers) to run away from
   * @return the map location where there are offensive enemies (null if none)
   */
  private MapLocation cachedEnemyCentroid;
  private int cacheStateOnCalcOffensiveEnemyCentroid = -1;
  protected MapLocation offensiveEnemyCentroid() {
    if (cacheStateOnCalcOffensiveEnemyCentroid == Cache.PerTurn.cacheState) return cachedEnemyCentroid;
    cacheStateOnCalcOffensiveEnemyCentroid = Cache.PerTurn.cacheState;
    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length == 0) return (cachedEnemyCentroid = null);
    int avgX = 0;
    int avgY = 0;
    int count = 0;
    for (RobotInfo enemy : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemy.type.damage > 0) { // enemy can hurt me
        avgX += enemy.location.x * enemy.type.damage;
        avgY += enemy.location.y * enemy.type.damage;
        count += enemy.type.damage;
      }
    }
    return (cachedEnemyCentroid = (count == 0 ? null : new MapLocation(avgX / count, avgY / count)));
  }

  /**
   * calculate the average location of friendly soldiers
   * @return the map location where there are offensive enemies (null if none)
   */
  private MapLocation cachedFriendlyCentroid;
  private int cacheStateOnCalcFriendlySoldierCentroid = -1;
  public MapLocation friendlySoldierCentroid() {
    if (cacheStateOnCalcFriendlySoldierCentroid == Cache.PerTurn.cacheState) return cachedFriendlyCentroid;
    cacheStateOnCalcFriendlySoldierCentroid = Cache.PerTurn.cacheState;
    if (Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length == 0) return (cachedFriendlyCentroid = null);
    int avgX = 0;
    int avgY = 0;
    int count = 0;
    for (RobotInfo friend : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (friend.type.damage > 0) { // friend can hurt me
        avgX += friend.location.x;
        avgY += friend.location.y;
        count++;
      }
    }
    return (cachedFriendlyCentroid = (count == 0 ? null : new MapLocation(avgX / count, avgY / count)));
  }

  /**
   * checks if there are offensive enemies nearby
   * @return true if there are enemies in vision
   */
  protected boolean offensiveEnemiesNearby() {
    return offensiveEnemyCentroid() != null;
  }

  /**
   * looks through enemies in vision and finds the one with lowest health that matches the type criteria
   * @param enemyType the robottype to look for
   * @return the robot of specified type with lowest health
   */
  protected RobotInfo findLowestHealthEnemyOfType(RobotType enemyType) {
    RobotInfo weakestEnemy = null;
    int minHealth = Integer.MAX_VALUE;

    for (RobotInfo enemy : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemy.type == enemyType && enemy.health < minHealth) {
        minHealth = enemy.health;
        weakestEnemy = enemy;
      }
    }

    return weakestEnemy;
  }


  protected void becomeDoNothingBot() {
    while (true) Clock.yield();
  }
}
