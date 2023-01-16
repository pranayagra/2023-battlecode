package launchermicroxsquare.robots;

import launchermicroxsquare.communications.HqMetaInfo;
import launchermicroxsquare.communications.MapMetaInfo;
import launchermicroxsquare.communications.RunningMemory;
import launchermicroxsquare.utils.Cache;
import launchermicroxsquare.utils.Printer;
import launchermicroxsquare.utils.Utils;
import battlecode.common.*;

public abstract class MobileRobot extends Robot {

  protected MapLocation explorationTarget;
  protected int turnsExploring;
  /** true if the exploration target is set to random location instead of unexplored lands */
  protected boolean exploringRandomly = false;
  private final int EXPLORATION_REACHED_RADIUS;

  MobileRobot (RobotController rc) throws GameActionException {
    super(rc);
    randomizeExplorationTarget(false);
    EXPLORATION_REACHED_RADIUS = Cache.Permanent.ROBOT_TYPE.actionRadiusSquared;
  }


  protected void randomizeExplorationTarget(boolean forceNotSelf) throws GameActionException {
//    int b = Clock.getBytecodeNum();
//    Printer.print("RUNNING randomizeExplorationTarget(): ");
    switch (Cache.Permanent.ROBOT_TYPE) {
      case CARRIER:
        if (Utils.rng.nextInt(5)<2) {
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
        } else {
          explorationTarget = Utils.randomMapLocation();
        }
        break;
      case LAUNCHER:
      case AMPLIFIER:
        int randomHqIndex = Utils.rng.nextInt(HqMetaInfo.hqCount);
        explorationTarget = HqMetaInfo.enemyHqLocations[randomHqIndex];
        if (rc.canSenseLocation(explorationTarget)) {
          RobotInfo robot = rc.senseRobotAtLocation(explorationTarget);
          if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
//            Printer.print("ERROR: expected enemy HQ is not an HQ " + explorationTarget, "symmetry guess must be wrong, eliminating symmetry (" + MapMetaInfo.guessedSymmetry + ") and retrying...");
            if (rc.canWriteSharedArray(0,0)) {
              MapMetaInfo.writeNot(MapMetaInfo.guessedSymmetry);
            }
            // TODO: eliminate symmetry and retry
//          RunningMemory.publishNotSymmetry(MapMetaInfo.guessedSymmetry);
//          explorationTarget = communicator.archonInfo.replaceEnemyArchon(explorationTarget);
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
    if (!rc.isMovementReady()) {
      if (explorationTarget != null) {
        rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, explorationTarget, 255, 0, 0);
      }
    }
    turnsExploring++;
    pathing.moveTowards(explorationTarget);
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
      rc.setIndicatorString("explored " + oldTarget + " -- now: " + explorationTarget);
      return true;
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
