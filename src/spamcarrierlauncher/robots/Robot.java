package spamcarrierlauncher.robots;

import spamcarrierlauncher.communications.Communicator;
import spamcarrierlauncher.communications.Message;
import spamcarrierlauncher.pathfinding.Pathing;
import spamcarrierlauncher.utils.Cache;
import spamcarrierlauncher.utils.Global;
import spamcarrierlauncher.utils.Printer;
import spamcarrierlauncher.utils.Utils;
import battlecode.common.*;

public abstract class Robot {
  private static final boolean RESIGN_ON_GAME_EXCEPTION = false;
  private static final boolean RESIGN_ON_RUNTIME_EXCEPTION = false;

  private static final int MAX_TURNS_FIGURE_SYMMETRY = 200;

  protected final RobotController rc;
  protected final Communicator communicator;
  protected int pendingMessages;

  protected int turnCount;
  protected boolean dontYield;

  protected MapLocation closestCommedEnemy;
  protected int distToClosestCommedEnemy;

  protected final Pathing pathing;

  /**
   * Create a Robot with the given controller
   * Perform various setup tasks generic to ny robot (building/droid)
   * @param rc the controller
   */
  public Robot(RobotController rc) throws GameActionException {
    Global.setupGlobals(rc, this);
    Utils.setUpStatics();
    Cache.setup();
    Printer.cleanPrint();
    this.rc = rc;
    this.communicator = Global.communicator;

    pathing = Pathing.create(rc);

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
        System.out.println(rc.getType() + " GameActionException");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION) rc.resign();
      } catch (Exception e) {
        // something bad
        System.out.println(rc.getType() + " Exception");
        Printer.submitPrint();
        e.printStackTrace();
        rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,255,255);
        if (RESIGN_ON_GAME_EXCEPTION || RESIGN_ON_RUNTIME_EXCEPTION) rc.resign();
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

  /**
   * wrap intern run turn method with generic actions for all robots
   */
  private void runTurnWrapper() throws GameActionException {
//        System.out.println("\nvery start - " + rc.readSharedArray(Communicator.MetaInfo.META_INT_START));
//        System.out.println("Age: " + turnCount + "; Location: " + Cache.PerTurn.CURRENT_LOCATION);
//        stolenbfs.initTurn();

    Cache.updateOnTurn();
    if (!dontYield) {
      rc.setIndicatorString("ac: " + rc.getActionCooldownTurns() + " mc: " + rc.getMovementCooldownTurns());
    }
    dontYield = false;

    // PRE MESSAGE READING -----
    closestCommedEnemy = null;
    distToClosestCommedEnemy = 999999;

//        pathing.initTurn();

//    System.out.println("Update cache -- " + Clock.getBytecodeNum());
//    communicator.cleanStaleMessages();
    Utils.startByteCodeCounting("reading");
//        pendingMessages = communicator.readAndAckAllMessages();
//    System.out.println("# messages: " + pendingMessages + " -- " + Clock.getBytecodeNum());
//    while (pendingMessages > 0) {
//      Message message = communicator.getNthLastReceivedMessage(pendingMessages);
//      ackMessage(message);
//      pendingMessages--;
//    }
    Utils.finishByteCodeCounting("reading");
//    if (pendingMessages > 0) System.out.println("Got " + pendingMessages + " messages!");

//    System.out.println("After acking: " + Clock.getBytecodeNum());
    MapLocation initial = Cache.PerTurn.CURRENT_LOCATION;
    runTurnTypeWrapper();

    // if the bot moved on its turn
    if (!initial.equals(Cache.PerTurn.CURRENT_LOCATION)) {
      afterTurnWhenMoved();
    }
    //      int b = Clock.getBytecodeNum();
    //      int updatedChunks =
//    updateVisibleChunks();
    //      System.out.println("updateVisibleChunks(" + updatedChunks + ") cost: " + (Clock.getBytecodeNum() - b));
    commNearbyEnemies();


    if (++turnCount != rc.getRoundNum() - Cache.Permanent.ROUND_SPAWNED) { // took too much bytecode
      rc.setIndicatorDot(Cache.PerTurn.CURRENT_LOCATION, 255,0,255); // MAGENTA IF RAN OUT OF BYTECODE
      turnCount = rc.getRoundNum() - Cache.Permanent.ROUND_SPAWNED;
      dontYield = true;
    } else { // still on our turn logic
//    if (Clock.getBytecodesLeft() >= MIN_BYTECODES_TO_SEND) {
      Utils.startByteCodeCounting("sending");
//      System.out.println("Bytecodes before send all messages: " + (Clock.getBytecodeNum()));
//      communicator.sendQueuedMessages();
//      communicator.updateMetaIntsIfNeeded();
//      System.out.println("Bytecodes after send all messages: " + (Clock.getBytecodeNum()));
      Utils.finishByteCodeCounting("sending");
//    }
    }
//    System.out.println("\nvery end - " + rc.readSharedArray(Communicator.MetaInfo.META_INT_START));
  }

  /**
   * acknowledge the provided message (happens at turn start)
   * @param message the message received
   */
  public void ackMessage(Message message) throws GameActionException {
//    switch (message.header.type) {
//      case RUBBLE_AT_LOCATION:
//        ackRubbleAtLocationMessage((RubbleAtLocationMessage) message);
//        break;
//      case ENEMY_FOUND:
//        ackEnemyFound((EnemyFoundMessage) message);
//    }
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
  }

  /**
   * perform any universal code that robots should run to figure out map symmetry
   *    Currently - broadcast my location + the rubble there
   * @throws GameActionException if sensing fails
   */
  protected void updateSymmetryComms() throws GameActionException {
    // TODO: do it based on how many robots we have spawned (or total friends alive) or something
//    if (Cache.PerTurn.HEALTH > 20 && communicator.metaInfo.knownSymmetry == null && Cache.PerTurn.ROUND_NUM < MAX_TURNS_FIGURE_SYMMETRY) {
//      RubbleAtLocationMessage rubbleAtLocationMessage = new RubbleAtLocationMessage(Cache.PerTurn.CURRENT_LOCATION, rc.senseRubble(Cache.PerTurn.CURRENT_LOCATION));
//      ackRubbleAtLocationMessage(rubbleAtLocationMessage);
//      if (communicator.metaInfo.knownSymmetry == null) communicator.enqueueMessage(rubbleAtLocationMessage);
////      if (communicator.metaInfo.knownSymmetry == null) communicator.enqueueMessage(rubbleAtLocationMessage);
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
      RobotInfo enemy = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS[0];
      for (RobotInfo enemyInfo : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) { // look for archons
        if (enemyInfo.type == RobotType.HEADQUARTERS) {
          enemy = enemyInfo;
//          int nearestEnemyIndex = communicator.archonInfo.getNearestEnemyArchonIndex(enemyInfo.location);
//          if (nearestEnemyIndex == -1) {
//            communicator.archonInfo.setEnemyArchonLoc(1, enemyInfo.location);
//          } else if (!communicator.archonInfo.getEnemyArchon(nearestEnemyIndex).equals(enemyInfo.location)) {
//            communicator.archonInfo.setEnemyArchonLoc(nearestEnemyIndex, enemyInfo.location);
//          }
//          System.out.println("Update enemy archon locs!");
//          communicator.archonInfo.readEnemyArchonLocs();
//          Printer.cleanPrint();
//          Printer.print("Set enemy mirror");
//          Printer.print("our 1: " + communicator.archonInfo.ourArchon1);
//          Printer.print("our 2: " + communicator.archonInfo.ourArchon2);
//          Printer.print("our 3: " + communicator.archonInfo.ourArchon3);
//          Printer.print("our 4: " + communicator.archonInfo.ourArchon4);
//          Printer.print("enemy 1: " + communicator.archonInfo.enemyArchon1);
//          Printer.print("enemy 2: " + communicator.archonInfo.enemyArchon2);
//          Printer.print("enemy 3: " + communicator.archonInfo.enemyArchon3);
//          Printer.print("enemy 4: " + communicator.archonInfo.enemyArchon4);
//          Printer.submitPrint();
          break;
        }
      }
      // no already seen enemy or closest seen is very far
//      Printer.cleanPrint();
//      Printer.print("closestCommedEnemy: " + closestCommedEnemy, "enemy: " + enemy);
//      if (closestCommedEnemy != null) {
//        Printer.print("dist: " + closestCommedEnemy.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION));
//      }
//      Printer.submitPrint();
      if (closestCommedEnemy == null
          || !closestCommedEnemy.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.VISION_RADIUS_SQUARED)) {
//        communicator.enqueueMessage(new EnemyFoundMessage(enemy));
      }
    }
  }

  /**
   * Wrapper for move() of RobotController that ensures enough bytecodes
   * @param dir where to move
   * @return if the robot moved
   * @throws GameActionException if movement failed
   */
  public boolean move(Direction dir) throws GameActionException {
    if (Clock.getBytecodesLeft() < 25) Clock.yield(); // todo: this should be larger? whenMoved takes a bit longer...
    if (rc.canMove(dir)) {
      rc.move(dir);
      Cache.PerTurn.whenMoved();
//      updateSymmetryComms();
      return true;
    }
    return false;
  }

  /**
   *
   * @param loc the location to test
   * @return if the robot is closer to the enemy archon than ours
   */
  protected boolean onEnemySide(MapLocation loc) {
    return true;
  }

  /**
   * if the robot can move, choose a random direction and move
   * will try 16 times in case some directions are blocked
   * @return if moved
   * @throws GameActionException if movement fails
   */
  protected boolean moveRandomly() throws GameActionException {
    return move(Utils.randomDirection()) || move(Utils.randomDirection()); // try twice in case many blocked locs
//    if (rc.isMovementReady()) {
//      int failedTries = 0;
//      Direction dir;
//      do {
//        dir = Utils.randomDirection();
//      } while (!rc.canMove(dir) && ++failedTries < 16);
//      if (failedTries < 16) { // only move if we didnt fail 16 times and never find a valid direction to move
//        rc.move(dir);
//      }
//    }
  }

  /**
   * move in this direction or an adjacent direction if can't move
   * @param dir the direction to move in
   * @return if moved
   * @throws GameActionException if movement fails
   */
  protected boolean moveInDirLoose(Direction dir) throws GameActionException {
    return move(dir) || move(dir.rotateLeft()) || move(dir.rotateRight());
  }

  /**
   * move randomly in this general direction
   * @param dir the direction to move in
   * @return if moved
   * @throws GameActionException if movement fails
   */
  protected boolean moveInDirRandom(Direction dir) throws GameActionException {
    return move(Utils.randomSimilarDirectionPrefer(dir)) || move(Utils.randomSimilarDirection(dir));
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
}
