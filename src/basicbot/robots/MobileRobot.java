package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.knowledge.Cache;
import basicbot.knowledge.RunningMemory;
import basicbot.robots.micro.MicroConstants;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

public abstract class MobileRobot extends Robot {

  protected MapLocation explorationTarget;
  protected int turnsExploring;
  protected int closestDistanceToExplorationTarget = Integer.MAX_VALUE;
  protected int turnsSinceClosestDistanceToExplorationTarget = 0;
  /** true if the exploration target is set to random location instead of unexplored lands */
  protected boolean exploringRandomly = false;
  public final int EXPLORATION_REACHED_RADIUS;

  MobileRobot (RobotController rc) throws GameActionException {
    super(rc);
    randomizeExplorationTarget(false);
    EXPLORATION_REACHED_RADIUS = Cache.Permanent.ROBOT_TYPE.actionRadiusSquared;
  }


  protected void randomizeExplorationTarget(boolean forceNotSelf) throws GameActionException {
//    int b = Clock.getBytecodeNum();
//    Printer.print("RUNNING randomizeExplorationTarget(): ");
    turnsExploring = 0;
    closestDistanceToExplorationTarget = Integer.MAX_VALUE;
    turnsSinceClosestDistanceToExplorationTarget = 0;
    switch (Cache.Permanent.ROBOT_TYPE) {
      case CARRIER:
        // TODO: make this more efficient (i.e. box around current location)
        int distBound = Cache.PerTurn.ROUND_NUM / 5 + 8;
        MapLocation closestHq = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        MapLocation explorationCenter = closestHq;
        MapLocation closestAD = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, ResourceType.ADAMANTIUM);
        MapLocation closestMA = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, ResourceType.MANA);
        if (closestMA == null || Utils.maxSingleAxisDist(closestMA, explorationCenter) >= Carrier.SWITCH_HQ_FOR_WELL_DISTANCE) {
          if (closestAD != null && Utils.maxSingleAxisDist(closestAD, explorationCenter) < Carrier.SWITCH_HQ_FOR_WELL_DISTANCE) {
            explorationCenter = closestAD;
            distBound = Math.min(distBound, 10);
          }
        }
        if (closestAD == null || Utils.maxSingleAxisDist(closestAD, explorationCenter) >= Carrier.SWITCH_HQ_FOR_WELL_DISTANCE) {
          explorationCenter = closestHq;
          distBound = Math.min(distBound, 10);
        }
        int minDist = 5;
        if (distBound <= 30) {
          int tries = 50;
          MapLocation loc;
          boolean valid;
          int dist;
          int minX = Math.max(0, explorationCenter.x - distBound);
          int maxX = Math.min(Cache.Permanent.MAP_WIDTH-1, explorationCenter.x + distBound);
          int xBound = maxX - minX + 1;
          int minY = Math.max(0, explorationCenter.y - distBound);
          int maxY = Math.min(Cache.Permanent.MAP_HEIGHT-1, explorationCenter.y + distBound);
          int yBound = maxY - minY + 1;
          do {
            loc = new MapLocation(Utils.rng.nextInt(xBound)+minX, Utils.rng.nextInt(yBound)+minY);
            dist = Utils.maxSingleAxisDist(loc, explorationCenter);
            valid = (minDist <= dist && dist <= distBound && (Cache.PerTurn.ROUND_NUM > 800 || !HqMetaInfo.isEnemyTerritory(loc)));
          } while (!valid && --tries > 0);
//          if (tries == 0) {
//            loc = HqMetaInfo.getFurthestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
//          }
//          Printer.print("Carrier early game exploration: " + loc);
          explorationTarget = loc;
          break;
        }
        if (RunningMemory.knownSymmetry == null && Utils.rng.nextInt(5)<2) {
//          MapLocation oldTarget = explorationTarget;
//          int tries = 10;
//          do {
          int xRand = Utils.rng.nextInt(4);
          int yRand = Utils.rng.nextInt(4);
          explorationTarget = new MapLocation(
              1 + (xRand == 3 ? 1 : xRand)*(Cache.Permanent.MAP_WIDTH/2-1),
              1 + (yRand == 3 ? 1 : yRand)*(Cache.Permanent.MAP_HEIGHT/2-1)
          );
//          } while (--tries > 0 && explorationTarget.equals(oldTarget));
          break;
        }
        explorationTarget = Utils.randomMapLocation();
        break;
      case LAUNCHER:
      case AMPLIFIER:
        int randomHqIndex = Utils.rng.nextInt(HqMetaInfo.hqCount);
        explorationTarget = HqMetaInfo.enemyHqLocations[randomHqIndex];
        if (rc.canSenseLocation(explorationTarget)) {
          RobotInfo robot = rc.senseRobotAtLocation(explorationTarget);
          if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
            Printer.print("ERROR: expected enemy HQ is not an HQ " + explorationTarget, "symmetry guess must be wrong, eliminating symmetry (" + RunningMemory.guessedSymmetry + ") and retrying...");
            RunningMemory.markInvalidSymmetry(RunningMemory.guessedSymmetry);
          }
        }
        if (checkShouldNotCrossMidpoint()) { // TODO: this will make the unit stay at the mid point
          MapLocation friendly = HqMetaInfo.getClosestHqLocation(explorationTarget);
          MapLocation enemy = explorationTarget;
          Direction backHome = enemy.directionTo(friendly);
          int tries = 20;
          while (tries-- > 0 && friendly.distanceSquaredTo(explorationTarget) > enemy.distanceSquaredTo(explorationTarget)) {
            explorationTarget = explorationTarget.add(backHome);
          }
//      if (tries < 19) return;
          tries = 20;
          backHome = backHome.opposite();
          while (tries-- > 0 && friendly.distanceSquaredTo(explorationTarget) < enemy.distanceSquaredTo(explorationTarget)) {
            explorationTarget = explorationTarget.add(backHome);
          }
        }
        break;
      default:
        Printer.print("No random exploration behavior defined for " + Cache.Permanent.ROBOT_TYPE);
        explorationTarget = Utils.randomMapLocation();
        break;
    }
    exploringRandomly = true;
  }

  private boolean checkShouldNotCrossMidpoint() {
    return Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length < Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length;
  }

  /**
   * assuming there is a explorationTarget for the droid, approach it
   * @return if the droid has reached (actionRadius) the explorationTarget
   * @throws GameActionException if movement or line indication fails
   */
  private boolean goToExplorationTarget() throws GameActionException {
    turnsExploring++;
    if (!rc.isMovementReady()) {
      /*BASICBOT_ONLY*/if (explorationTarget != null) {
      /*BASICBOT_ONLY*/  rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, explorationTarget, 255, 0, 0);
      /*BASICBOT_ONLY*/}
    } else {
      pathing.moveTowards(explorationTarget);
    }
    return Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(explorationTarget, EXPLORATION_REACHED_RADIUS);
  }

  /**
   * generic exploration process for droids to see the rest of the map
   * @return true if the target was reached & updated
   * @throws GameActionException if exploring/moving fails
   */
  protected boolean doExplorationOnce() throws GameActionException {
//    Printer.print("RUNNING doExploration(): ", "old explorationTarget: " + explorationTarget);
    // if we are explorating smartly and the chunk has been explored already
//    System.out.println("  " + Cache.PerTurn.CURRENT_LOCATION + " - \nexploringRandomly: " + exploringRandomly + "\nExploration target: " + explorationTarget + "\nalready explored: " + !communicator.chunkInfo.chunkIsGoodForMinerExploration(explorationTarget));
//    Printer.print("explorationTarget: " + explorationTarget);
    if (goToExplorationTarget()) {
      MapLocation oldTarget = explorationTarget;
      randomizeExplorationTarget(true);
      Printer.appendToIndicator("explored " + oldTarget + " -- now: " + explorationTarget);
      return true;
    } else {
      int distance = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, explorationTarget);
      if (distance < closestDistanceToExplorationTarget) {
        closestDistanceToExplorationTarget = distance;
        turnsSinceClosestDistanceToExplorationTarget = 0;
      } else if (++turnsSinceClosestDistanceToExplorationTarget >= Math.max(50, closestDistanceToExplorationTarget * MicroConstants.TURNS_SCALAR_TO_GIVE_UP_ON_TARGET_APPROACH)) {
        MapLocation oldTarget = explorationTarget;
        randomizeExplorationTarget(true);
        Printer.appendToIndicator("gave up on exploring " + oldTarget + " -- now: " + explorationTarget);
        return true;
      }
    }
    return false;
  }

  /**
   * will doExplorationOnce and return if moved
   * @return true if robot moved
   * @throws GameActionException any issues with moving
   */
  protected boolean tryExplorationMove() throws GameActionException {
    int moveCD = rc.getMovementCooldownTurns();
    doExplorationOnce();
    return moveCD < rc.getMovementCooldownTurns();
  }

  /**
   * will repeatedly doExplorationOnce until fails to move
   * @return true if the exploration target was updated
   * @throws GameActionException any errors encountered
   */
  protected boolean doExploration() throws GameActionException {
    int moveCD;
    boolean reached = false;
    do {
      moveCD = rc.getMovementCooldownTurns();
      reached |= doExplorationOnce();
    } while (moveCD < rc.getMovementCooldownTurns());
    return reached;
  }

}
