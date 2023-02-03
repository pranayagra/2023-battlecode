package pathingfixes.robots.pathfinding;

import pathingfixes.containers.CharSet;
import pathingfixes.robots.pathfinding.unitpathing.*;
import pathingfixes.knowledge.Cache;
import pathingfixes.utils.Printer;
import battlecode.common.*;

public class SmitePathing {
  public static boolean forceBugging = false;
  public static boolean forceOneBug = false;
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
    int cooldownCost = (int) (Cache.Permanent.ROBOT_TYPE == RobotType.CARRIER
        ? (GameConstants.CARRIER_MOVEMENT_INTERCEPT + rc.getWeight()*GameConstants.CARRIER_MOVEMENT_SLOPE)
        : Cache.Permanent.ROBOT_TYPE.movementCooldown);
    if (forceBugging
        || forceOneBug
        || Clock.getBytecodesLeft() <= MIN_BYTECODE_TO_BFS // not enough bytecode
//        || Clock.getBytecodeNum() <= 1000 // doing a move very early in the turn
        || Cache.PerTurn.ALL_NEARBY_ROBOTS.length >= Cache.Permanent.ACTION_RADIUS_SQUARED // too many robots nearby, just bug
    ) {
      forceOneBug = false;
//      doBuggingTurns += 2;
      return BugNav.tryBugging() && markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
    }
    // if i'm not a special pather or if i still have fuzzy moves left, fuzzy move
    if (doBuggingTurns > 0) {
      doBuggingTurns--;
//      return BugNav.tryBugging() && markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
//      if (!BugNav.checkDoneBugging()) {
//        doBuggingTurns--;
        if (BugNav.tryBugging()) {
          if (BugNav.checkDoneBugging()) {
            doBuggingTurns = 0;
          }
          return markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
        }
//        return false;
//      } else {
//        doBuggingTurns = 0;
//      }
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
    Printer.appendToIndicator("bfs>"+target);


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

    
//    if (Cache.Permanent.ID == 10596 && Cache.PerTurn.ROUND_NUM >= 180) {
//      Printer.print("nav towards: " + destination, "best dir: " + dir, "bug can move: " + (dir == null ? "null" : BugNav.canMoveInDirection(dir)));
//      Printer.print("visited: " + (dir == null ? "null" : visitedLocations.contains(Cache.PerTurn.CURRENT_LOCATION.add(dir))), "wind current loc: " + windCurrentLoc, "visited wind current loc: " + (windCurrentLoc == null ? "null" : visitedLocations.contains(windCurrentLoc)));
//    }
    if (dir == null || !BugNav.canMoveInDirection(dir) || visitedLocations.contains(Cache.PerTurn.CURRENT_LOCATION.add(dir)) || (windCurrentLoc != null && visitedLocations.contains(windCurrentLoc))) {
//      Printer.print((dir == null ? "dir is null" : ("Revisited " + Cache.PerTurn.CURRENT_LOCATION.add(dir))) + "-- try fuzzy movement for " + MAX_FUZZY_MOVES + " turns");
//      Printer.print((dir == null ? "dir is null" : ("Revisited " + Cache.PerTurn.CURRENT_LOCATION.add(dir))) + "-- try bugging");
//      Printer.print("current location: " + Cache.PerTurn.CURRENT_LOCATION);
      doBuggingTurns += 4;
//      BugNav.setTarget(target);
      return BugNav.tryBugging() && markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
    } else {
      Printer.appendToIndicator("sp="+dir);
      return smiteMove(dir);
      // rc.setIndicatorDot(rc.getLocation(), 255, 255, 255);
    }
  }

  /**
   * Use this function instead of rc.move(). Still need to verify canMove before calling this.
   */
  public boolean smiteMove(Direction dir) throws GameActionException {
    if (pathing.move(dir)) {
      return markVisitedAndRetTrue(Cache.PerTurn.CURRENT_LOCATION);
    }
    return false;
  }

  private boolean markVisitedAndRetTrue(MapLocation loc) throws GameActionException {
    visitedLocations.add(loc);
    if (!rc.isMovementReady() && rc.canSenseLocation(loc)) {
      MapInfo info = rc.senseMapInfo(loc);
      if (info.getCurrentDirection() != Direction.CENTER) {
        visitedLocations.add(loc.add(info.getCurrentDirection()));
      }
    }
    return true;
  }
}
