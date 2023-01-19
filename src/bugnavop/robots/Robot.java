package bugnavop.robots;

import bugnavop.communications.Communicator;
import bugnavop.communications.MapMetaInfo;
import bugnavop.communications.RunningMemory;
import bugnavop.robots.micro.AttackMicro;
import bugnavop.robots.micro.AttackerFightingMicro;
import bugnavop.robots.pathfinding.Pathing;
import bugnavop.utils.Cache;
import bugnavop.utils.Global;
import bugnavop.utils.Printer;
import bugnavop.utils.Utils;
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
    Cache.setup();
    Printer.cleanPrint();
    Communicator.init(rc);

    pathing = Pathing.create(rc);
    AttackerFightingMicro.init(rc, pathing);
    AttackMicro.init(rc);

    rc.setIndicatorString("Just spawned!");
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
//                Printer.cleanPrint();
        Printer.submitPrint();
      } catch (GameActionException e) {
        // something illegal in the Battlecode world
        System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + " GameActionException");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION) die();
      } catch (Exception e) {
        // something bad
        System.out.println(rc.getType() + "@" + rc.getLocation() + ".BC=" + Clock.getBytecodeNum() + " Exception");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION || RESIGN_ON_RUNTIME_EXCEPTION) die();
      } finally {
        // end turn - make code wait until next turn
        if (!dontYield) Clock.yield();
        else {
          if (Clock.getBytecodesLeft() < 0.9 * Cache.Permanent.ROBOT_TYPE.bytecodeLimit) { // if don't have 90% of limit, still yield
            dontYield = false;
            Clock.yield();
//                    } else {
//                      System.out.println("Skipping turn yeild!!");
          }
        }
      }
      Printer.cleanPrint();
    }
  }

  private void die() {
    Clock.yield();
    Clock.yield();
    rc.resign();
  }

  /**
   * wrap intern run turn method with generic actions for all robots
   */
  private void runTurnWrapper() throws GameActionException {
//        System.out.println("Age: " + turnCount + "; Location: " + Cache.PerTurn.CURRENT_LOCATION);

    Cache.updateOnTurn();
    if (!dontYield) {
      rc.setIndicatorString("ac: " + rc.getActionCooldownTurns() + " mc: " + rc.getMovementCooldownTurns());
    }
    dontYield = false;

    // PRE MESSAGE READING -----
    closestCommedEnemy = null;
    distToClosestCommedEnemy = 999999;

//        pathing.initTurn();

//    Utils.startByteCodeCounting("updating-comm-metainfo");
    Communicator.MetaInfo.updateOnTurnStart();
//    Utils.finishByteCodeCounting("updating-comm-metainfo");

    MapLocation initial = Cache.PerTurn.CURRENT_LOCATION;

    if (Cache.PerTurn.ROUND_NUM < 20 && Cache.PerTurn.ROUNDS_ALIVE == 0) {
      initialWellExploration();
    }

    runTurnTypeWrapper();

    // if the bot moved on its turn
    if (!initial.equals(Cache.PerTurn.CURRENT_LOCATION)) {
      afterTurnWhenMoved();
    }

    commNearbyEnemies();

    if (++turnCount != rc.getRoundNum() - Cache.Permanent.ROUND_SPAWNED) { // took too much bytecode
      rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,0,255); // MAGENTA IF RAN OUT OF BYTECODE
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
  }

  /**
   * perform any universal code that robots should run to figure out map symmetry
   *    Currently - broadcast my location + the rubble there
   * @throws GameActionException if sensing fails
   */
  protected void updateSymmetryComms() throws GameActionException {
    if (MapMetaInfo.knownSymmetry != null) return;
    if (!rc.canWriteSharedArray(0,0)) return;
    if (Cache.PerTurn.ROUND_NUM > MAX_TURNS_FIGURE_SYMMETRY) return;

    // TODO: actually do the computation
    int visionRadiusSq = Cache.Permanent.VISION_RADIUS_SQUARED;
    int midlineThreshold = Cache.Permanent.VISION_RADIUS_FLOOR / 2;
    MapLocation myLoc = Cache.PerTurn.CURRENT_LOCATION;
    int myX = myLoc.x;
    int myY = myLoc.y;
    int mapWidth = Cache.Permanent.MAP_WIDTH;
    int mapHeight = Cache.Permanent.MAP_HEIGHT;
    // if Vertical not ruled out (flipY, horizontal midline)
    if (!MapMetaInfo.notVertical) { // could be vertical
      // check if y is near the middle
      nearHorizMidline:
      if (myY * 2 <= mapHeight + midlineThreshold
          && myY * 2 >= mapHeight - midlineThreshold) {
        MapLocation test1 = new MapLocation(myX, mapHeight / 2 - 1);
        MapLocation test2 = new MapLocation(myX, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.VERTICAL); // eliminate Vertical symmetry
          break nearHorizMidline;
        }
        test1 = new MapLocation(myX - 2, mapHeight / 2 - 1);
        test2 = new MapLocation(myX - 2, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.VERTICAL); // eliminate Vertical symmetry
          break nearHorizMidline;
        }
        test1 = new MapLocation(myX + 2, mapHeight / 2 - 1);
        test2 = new MapLocation(myX + 2, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.VERTICAL); // eliminate Vertical symmetry
          break nearHorizMidline;
        }
      }
    }
    // if Horizontal not ruled out (flipX, vertical midline)
    if (!MapMetaInfo.notHorizontal) { // could be horizontal
      // check if x is near the middle
      nearVertMidline:
      if (myX * 2 <= mapWidth + midlineThreshold
          && myX * 2 >= mapWidth - midlineThreshold) {
        MapLocation test1 = new MapLocation(mapWidth / 2 - 1, myY);
        MapLocation test2 = new MapLocation(mapWidth - mapWidth / 2, myY);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.HORIZONTAL); // eliminate Horizontal symmetry
          break nearVertMidline;
        }
        test1 = new MapLocation(mapWidth / 2 - 1, myY - 2);
        test2 = new MapLocation(mapWidth - mapWidth / 2, myY - 2);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.HORIZONTAL); // eliminate Horizontal symmetry
          break nearVertMidline;
        }
        test1 = new MapLocation(mapWidth / 2 - 1, myY + 2);
        test2 = new MapLocation(mapWidth - mapWidth / 2, myY + 2);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.HORIZONTAL); // eliminate Horizontal symmetry
          break nearVertMidline;
        }
      }
    }
    // if Rotational not ruled out (rotXY, near center)
    if (!MapMetaInfo.notRotational) { // could be rotational
      // check if near the center
      nearCenter:
      if (myX * 2 <= mapWidth + midlineThreshold
          && myX * 2 >= mapWidth - midlineThreshold
          && myY * 2 <= mapHeight + midlineThreshold
          && myY * 2 >= mapHeight - midlineThreshold) {
        MapLocation test1 = new MapLocation(mapWidth / 2 - 1, mapHeight / 2 - 1);
        MapLocation test2 = new MapLocation(mapWidth - mapWidth / 2, mapHeight - mapHeight / 2);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.ROTATIONAL); // eliminate Rotational symmetry
          break nearCenter;
        }
        test1 = new MapLocation(mapWidth / 2 - 1, mapHeight - mapHeight / 2);
        test2 = new MapLocation(mapWidth - mapWidth / 2, mapHeight / 2 - 1);
        if (checkFailsSymmetry(test1, test2)) {
          MapMetaInfo.writeNot(Utils.MapSymmetry.ROTATIONAL); // eliminate Rotational symmetry
          break nearCenter;
        }
      }
    }
  }
  private boolean checkFailsSymmetry(MapLocation test1, MapLocation test2) throws GameActionException {
//     rc.setIndicatorDot(test1, 211, 211, 211);
//     rc.setIndicatorDot(test2, 211, 211, 211);
    if (rc.onTheMap(test1)
        && rc.onTheMap(test2)
        && test1.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.VISION_RADIUS_SQUARED)
        && test2.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.VISION_RADIUS_SQUARED)) {
      if (rc.senseCloud(test1) != rc.senseCloud(test2)) {
        return true;
      }
      if (rc.canSenseLocation(test1)) {
        if (rc.sensePassability(test1) != rc.sensePassability(test2) || rc.senseMapInfo(test1).getCurrentDirection() != rc.senseMapInfo(test2).getCurrentDirection()) {
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
    MapLocation knownWellLoc = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, ResourceType.ADAMANTIUM);
    boolean needAd = knownWellLoc == null;
    knownWellLoc = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, ResourceType.MANA);
    boolean needMana = knownWellLoc == null;
    knownWellLoc = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, ResourceType.ELIXIR);
    boolean needElixir = knownWellLoc == null;
    if (!needAd && !needMana && !needElixir) return;
//    if (needAd || needMana) {
//      Printer.print("Early game checking for wells: needAd=" + needAd + ", needMana=" + needMana);
//    }
    for (WellInfo wellInfo : rc.senseNearbyWells()) {
      switch (wellInfo.getResourceType()) {
        case ADAMANTIUM:
          if (needAd) {
            needAd = false;
            RunningMemory.publishWell(wellInfo);
//            Printer.print("Early game well publishing: Ad @ " + wellInfo.getMapLocation());
          }
          break;
        case MANA:
          if (needMana) {
            needMana = false;
            RunningMemory.publishWell(wellInfo);
//            Printer.print("Early game well publishing: Mana @ " + wellInfo.getMapLocation());
          }
          break;
        case ELIXIR:
          if (needElixir) {
            needElixir = false;
            RunningMemory.publishWell(wellInfo);
//            Printer.print("Early game well publishing: Elixir @ " + wellInfo.getMapLocation());
          }
          break;
      }
    }
    RunningMemory.broadcastMemorizedWells();
  }

  /**
   * If a nearby well is seen, put it in comms
   * @throws GameActionException any issues during reading/writing
   */
  protected void updateWellExploration() throws GameActionException {
//    if (!rc.canWriteSharedArray(0,0)) return;
    if (Cache.PerTurn.hasPreviouslyVisitedOwnLoc()) return;
    if (Clock.getBytecodesLeft() < 200) return;
    int wellsPublished = 0;
    for (WellInfo well : rc.senseNearbyWells()) {
      if (RunningMemory.publishWell(well)) {
        wellsPublished++;
      }
    }
//    if (wellsPublished > 0) {
//      Printer.print("Found " + wellsPublished + " new wells");
//    }
//    if (Clock.getBytecodesLeft() < 200) return;
//    int count = RunningMemory.wellCount;
//    Utils.startByteCodeCounting("broadcast-" + count + "-wells");
    int wellsBroadcast = RunningMemory.broadcastMemorizedWells();
//    Utils.finishByteCodeCounting("broadcast-" + count + "-wells");
//    if (wellsBroadcast > 0) {
//      Printer.print("Broadcasted " + wellsBroadcast + " wells");
//    }
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
    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0) {

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


  protected static int getInvWeight(RobotInfo ri) {
    return (ri.getResourceAmount(ResourceType.ADAMANTIUM) + ri.getResourceAmount(ResourceType.MANA) + ri.getResourceAmount(ResourceType.ELIXIR) + (ri.getTotalAnchors() * GameConstants.ANCHOR_WEIGHT));
  }

  protected void becomeDoNothingBot() {
    while (true) Clock.yield();
  }
}
