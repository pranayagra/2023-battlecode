package basicbot.robots.pathfinding;

import basicbot.containers.CharSet;
import basicbot.robots.pathfinding.unitpathing.*;
import basicbot.knowledge.Cache;
import battlecode.common.*;

public class SmitePathing {
  public boolean forceBugging = false;
  private static final int MIN_BYTECODE_TO_BFS = 4300;
  RobotController rc;
  Pathing pathing;
  UnitPathing up;
  MapLocation destination;

  CharSet visitedLocations;
  int doBuggingTurns = 0;

  public SmitePathing(RobotController rc, Pathing pathing) {
    this.rc = rc;
    this.pathing = pathing;
    this.visitedLocations = new CharSet();

    switch (Cache.Permanent.ROBOT_TYPE) { // cases for carrier, launcher, amplifier, destabilizer, booster
      case CARRIER:
        up = new CarrierPathing(rc);
        break;
      case LAUNCHER:
        up = new LauncherPathing(rc);
        break;
      case AMPLIFIER:
        up = new AmplifierPathing(rc);
        break;
      case DESTABILIZER:
        up = new DestabilizerPathing(rc);
        break;
      case BOOSTER:
        up = new BoosterPathing(rc);
        break;
      default:
        throw new RuntimeException("Invalid robot type for smite pathing");
    }
  }

  public void updateDestination(MapLocation newDest) {
    if (destination == null || destination.distanceSquaredTo(newDest) != 0) {
      destination = newDest;
      BugNav.setTarget(newDest);
      visitedLocations.clear();
      visitedLocations.add(Cache.PerTurn.CURRENT_LOCATION);
      doBuggingTurns = 0;
    }
  }

  public boolean pathToDestination() throws GameActionException {
    if (destination != null) {
      return pathTo(destination);
//      if ((myType == RobotType.SOLDIER || myType == RobotType.SAGE) && destination.distanceSquaredTo(robot.baseLocation) > 0) {
//        rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, destination, 100 - rc.getTeam().ordinal() * 100, 50, rc.getTeam().ordinal() * 100);
//      } else if ((myType == RobotType.MINER || myType == RobotType.BUILDER) && destination.distanceSquaredTo(robot.baseLocation) > 0) {
//        rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, destination, 150 + 100 - rc.getTeam().ordinal() * 100, 150, 150 + rc.getTeam().ordinal() * 100);
//      } else if (myType == RobotType.ARCHON || myType == RobotType.WATCHTOWER || myType == RobotType.LABORATORY) {
//        rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, destination, 250 - 250 * rc.getTeam().ordinal(), 0, 250 * rc.getTeam().ordinal());
//      } else if (destination.distanceSquaredTo(robot.baseLocation) == 0) {
//        rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, destination, 200, 200, 200);
//      }
    }
    return false;
  }

  private boolean pathTo(MapLocation target) throws GameActionException {
    if (!rc.isMovementReady()) return false;
    int cooldownCost = (int) (Cache.Permanent.ROBOT_TYPE == RobotType.CARRIER
        ? (GameConstants.CARRIER_MOVEMENT_INTERCEPT + rc.getWeight()*GameConstants.CARRIER_MOVEMENT_SLOPE)
        : Cache.Permanent.ROBOT_TYPE.movementCooldown);
    if (forceBugging
        || Clock.getBytecodesLeft() <= MIN_BYTECODE_TO_BFS // not enough bytecode
        || Clock.getBytecodeNum() <= 1000 // doing a move very early in the turn
        || Cache.PerTurn.ALL_NEARBY_ROBOTS.length >= Cache.Permanent.ACTION_RADIUS_SQUARED // too many robots nearby, just bug
    ) {
//      doBuggingTurns += 2;
      return BugNav.tryBugging() && markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
    }
    // if i'm not a special pather or if i still have fuzzy moves left, fuzzy move
    if (doBuggingTurns > 0) {
//      return BugNav.tryBugging();
      if (!BugNav.checkDoneBugging()) {
        doBuggingTurns--;
        if (BugNav.tryBugging()) {
          visitedLocations.add(Cache.PerTurn.CURRENT_LOCATION);
          if (BugNav.checkDoneBugging()) {
            doBuggingTurns = 0;
          }
          return true;
        }
        return false;
      } else {
        doBuggingTurns = 0;
      }
//      return fuzzyMove(target);
      // rc.setIndicatorDot(rc.getLocation(), 0, 0, 0);
//      return;
    }

    // if i'm adjacent to my destination and it is unoccupied / not high rubble, move there
    if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(target)) {
      Direction toTarget = Cache.PerTurn.CURRENT_LOCATION.directionTo(target);
      if (BugNav.canMoveInDirection(toTarget)) {
        return smiteMove(toTarget);
      }
//      return smiteMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(target));
//      if (rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(target))) {
//      }
//      return false;
    }

//    Utils.startByteCodeCounting("unit-bfs");
    // get bfs best direction
    Direction dir = up.bestDir(target); // ~5000 bytecode (4700 avg&median)
//    Utils.finishByteCodeCounting("unit-bfs");


//    if (dir == null || !rc.canMove(dir)) return false; // TODO: this checks null but if null should do something else
//    if (dir != null && !BugNav.canMoveInDirection(dir)) return BugNav.tryBugging();
//    if (dir == null && rc.canM) return BugNav.tryBugging();

    // don't know where to go / gonna revisit
    MapLocation windCurrentLoc = dir != null ? Cache.PerTurn.CURRENT_LOCATION.add(dir) : Cache.PerTurn.CURRENT_LOCATION;
    MapInfo nextLocInfo = rc.senseMapInfo(windCurrentLoc);
    if (nextLocInfo.getCurrentDirection() != Direction.CENTER && rc.getMovementCooldownTurns() + cooldownCost * nextLocInfo.getCooldownMultiplier(Cache.Permanent.OUR_TEAM) > GameConstants.COOLDOWN_LIMIT) {
      windCurrentLoc = windCurrentLoc.add(nextLocInfo.getCurrentDirection());
    } else {
      windCurrentLoc = null;
    }

//    if (Cache.Permanent.ID == 13825 && Cache.PerTurn.ROUND_NUM >= 350) {
//      Printer.print("nav towards: " + destination, "best dir: " + dir, "bug can move: " + (dir == null ? "null" : BugNav.canMoveInDirection(dir)));
//      Printer.print("visited: " + (dir == null ? "null" : tracker.contains(Cache.PerTurn.CURRENT_LOCATION.add(dir))), "wind current loc: " + windCurrentLoc, "visited wind current loc: " + (windCurrentLoc == null ? "null" : tracker.contains(windCurrentLoc)));
//    }
    if (dir == null || !BugNav.canMoveInDirection(dir) || visitedLocations.contains(Cache.PerTurn.CURRENT_LOCATION.add(dir)) || (windCurrentLoc != null && visitedLocations.contains(windCurrentLoc))) {
//      Printer.print((dir == null ? "dir is null" : ("Revisited " + Cache.PerTurn.CURRENT_LOCATION.add(dir))) + "-- try fuzzy movement for " + MAX_FUZZY_MOVES + " turns");
//      Printer.print((dir == null ? "dir is null" : ("Revisited " + Cache.PerTurn.CURRENT_LOCATION.add(dir))) + "-- try bugging");
//      Printer.print("current location: " + Cache.PerTurn.CURRENT_LOCATION);
      doBuggingTurns = 2;
//      BugNav.setTarget(target);
      return BugNav.tryBugging() && markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
    } else {
      return smiteMove(dir);
      // rc.setIndicatorDot(rc.getLocation(), 255, 255, 255);
    }
  }

  /**
   * Use this function instead of rc.move(). Still need to verify canMove before calling this.
   */
  public boolean smiteMove(Direction dir) throws GameActionException {
//    MapLocation expectedNewLoc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
    if (pathing.move(dir)) {
//      Cache.PerTurn.whenMoved();
//      if (fuzzyMovesLeft > 0) {
//        addFuzzyVisited(Cache.PerTurn.CURRENT_LOCATION);
//        fuzzyMovesLeft--;
//      }
//    Cache.PerTurn.CURRENT_LOCATION = Cache.PerTurn.CURRENT_LOCATION.add(dir);)
      visitedLocations.add(Cache.PerTurn.CURRENT_LOCATION);
//    robot.nearbyEnemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, robot.enemyTeam);
      return true;
    }
    return false;
  }

//  public boolean fuzzyMove(MapLocation target) throws GameActionException {
//    if (!rc.isMovementReady()) return false;
//
//    // Don't move if adjacent to destination and something is blocking it
//    if (Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(target) <= 2 && !rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(target))) {
//      return false;
//    }
//    // TODO: This is not optimal! Sometimes taking a slower move is better if its diagonal.
//    MapLocation myLocation = rc.getLocation();
//    Direction toDest = myLocation.directionTo(target);
//    Direction[] dirs = {toDest, toDest.rotateLeft(), toDest.rotateRight(), toDest.rotateLeft().rotateLeft(), toDest.rotateRight().rotateRight(), toDest.opposite().rotateLeft(), toDest.opposite().rotateRight(), toDest.opposite()};
//    int cooldownCost = (int) (Cache.Permanent.ROBOT_TYPE == RobotType.CARRIER
//        ? (GameConstants.CARRIER_MOVEMENT_INTERCEPT + rc.getWeight()*GameConstants.CARRIER_MOVEMENT_SLOPE)
//        : Cache.Permanent.ROBOT_TYPE.movementCooldown);
//    int cost = 99999;
//    Direction optimalDir = null;
//    for (int i = 0; i < dirs.length; i++) {
//      // Prefer forward moving steps over horizontal shifts
//      if (i > 2 && optimalDir != null) {
//        break;
//      }
//      Direction dir = dirs[i];
//      MapLocation newLoc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
//      if (rc.canMove(dir) && !isFuzzyVisited(newLoc)) {//!newLoc.isWithinDistanceSquared(Cache.PerTurn.PREVIOUS_LOCATION, 0)) {
//        int newCost = (int) ((rc.canSenseLocation(newLoc) ? rc.senseMapInfo(newLoc).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) : 1)
//            * cooldownCost);//rc.senseRubble(myLocation.add(dir));
//        // add epsilon boost to forward direction
//        if (dir == toDest) {
//          newCost -= 1;
//        }
//        if (newCost < cost) {
//          cost = newCost;
//          optimalDir = dir;
//        }
//      }
//    }
//
//
//    if (optimalDir != null) {
//      return smiteMove(optimalDir);
//    }
//    return false;
//  }

  /**
   * Move in the directon to the target, either directly or 45 degrees left or right, if there
   * is an open move with less than 20 rubble.
   *
   * @param target where to go towards
   * @return true if moved, false otherwise
   * @throws GameActionException
   */
  public boolean cautiousGreedyMove(MapLocation target) throws GameActionException {
    if (!rc.isMovementReady()) return false;

    // Get direction to target; check rubble in that direction, to the left, and to the right,
    // and move to the direction with the least rubble, as long as that rubble is at most 20.
    Direction dir = Cache.PerTurn.CURRENT_LOCATION.directionTo(target);
    double bestCooldownMultiplier = 1; // we want lower cooldown scaling
    Direction bestDir = null;

    MapLocation loc = Cache.PerTurn.CURRENT_LOCATION.add(dir.rotateLeft());
    if (rc.onTheMap(loc) && rc.canMove(dir.rotateLeft())) {
      double cooldownMuliplier = rc.senseMapInfo(loc).getCooldownMultiplier(Cache.Permanent.OUR_TEAM);
      if (cooldownMuliplier < bestCooldownMultiplier) {
        bestCooldownMultiplier = cooldownMuliplier;
        bestDir = dir.rotateLeft();
      }
    }
    loc = Cache.PerTurn.CURRENT_LOCATION.add(dir.rotateRight());
    if (rc.onTheMap(loc) && rc.canMove(dir.rotateRight())) {
      double cooldownMuliplier = rc.senseMapInfo(loc).getCooldownMultiplier(Cache.Permanent.OUR_TEAM);
      if (cooldownMuliplier < bestCooldownMultiplier) {
        bestCooldownMultiplier = cooldownMuliplier;
        bestDir = dir.rotateRight();
      }
    }

    loc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
    if (rc.onTheMap(loc) && rc.canMove(dir)) {
      double cooldownMuliplier = rc.senseMapInfo(loc).getCooldownMultiplier(Cache.Permanent.OUR_TEAM);
      if (cooldownMuliplier <= bestCooldownMultiplier) {
        bestCooldownMultiplier = cooldownMuliplier;
        bestDir = dir;
      }
    }
    if (bestDir != null) {
      return smiteMove(bestDir);
    }
    return false;
  }

  private boolean markVisitedAndRetTrue(MapLocation loc) {
    visitedLocations.add(loc);
    return true;
  }
}