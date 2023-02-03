package pathinglaunchers.robots;

import pathinglaunchers.communications.CommsHandler;
import pathinglaunchers.communications.HqMetaInfo;
import pathinglaunchers.knowledge.Cache;
import pathinglaunchers.knowledge.RunningMemory;
import pathinglaunchers.utils.Printer;
import pathinglaunchers.utils.Utils;
import battlecode.common.*;

public class Amplifier extends MobileRobot {
  public static MapLocation lastTargetLocation;
  public Amplifier(RobotController rc) throws GameActionException {
    super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    CommsHandler.writeNumAmpsIncrement();
    MapLocation closestEnemyLocation = closestEnemyLauncher();
    if (closestEnemyLocation != null) {
//      rc.setIndicatorDot(closestEnemyLocation, 255, 0, 0);
      int currDistance = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(closestEnemyLocation);
      // if furthestDistance is < 27, maximize distance
      // if furthestDistance is >= 27, minimize distance
      MapLocation bestLocation = bestTileToMove(closestEnemyLocation, currDistance);
      pathing.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(bestLocation));
      Printer.appendToIndicator("Running away from enemy @" + closestEnemyLocation);
//      if (rc.isMovementReady()) {
//        if (currDistance <= 20) {
//          // todo: consider trying to run even if we have to get closer to enemy
//          pathing.moveAwayFrom(closestEnemyLocation);
//        }
//      }
      return;
    }

    MapLocation targetLocation = locationToStayInFrontOfFriendlyLaunchers();
    if ((targetLocation == null && lastTargetLocation == null) || Cache.PerTurn.CURRENT_LOCATION.equals(targetLocation)) {
      Printer.appendToIndicator("Target location null, doing exploration");
      doExploration();
      return;
    }
    if (targetLocation == null) targetLocation = lastTargetLocation;
    targetLocation = Utils.clampToMap(targetLocation);
    lastTargetLocation = targetLocation;
    MapLocation friendlyAmpTooClose = separateFromOtherAmplifiers(targetLocation);
    if (friendlyAmpTooClose != null) {
      MapLocation targetLauncher = farthestAllyLauncherFrom(friendlyAmpTooClose);
      if (targetLauncher != null) {
        Printer.appendToIndicator("friendly amp too close, going to launcher @" + targetLauncher);
        pathing.moveTowards(targetLauncher);
        return;
      }

      Printer.appendToIndicator("friendly amp too close @" + friendlyAmpTooClose);
      pathing.moveAwayFrom(friendlyAmpTooClose);
//        // todo: think of better logic to separate
//        randomizeExplorationTarget(true);
//        doExploration();
      return;
    }
//      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, targetLocation, 0, 255, 0);
    Printer.appendToIndicator("moving towards target @" + targetLocation);
    pathing.moveTowards(targetLocation);

  }


  private MapLocation bestTileToMove(MapLocation closestEnemyLocation, int currDistance) throws GameActionException {
    // if possible, get closest distance that is >= 27
    MapLocation bestLocation = Cache.PerTurn.CURRENT_LOCATION;
    int bestDistance = currDistance;
    if (rc.isMovementReady()) {
      for (Direction d : Utils.directionsNine) {
        if (!rc.canMove(d)) continue;

        MapLocation candidateLocation = Cache.PerTurn.CURRENT_LOCATION.add(d);
        int candidateDistance = candidateLocation.distanceSquaredTo(closestEnemyLocation);

        // always better to move out of danger
        if (bestDistance <= 26 && candidateDistance >= 27) {
          bestLocation = candidateLocation;
          bestDistance = candidateDistance;
        }

        // if both are in danger, move to the one that is further away
        if (bestDistance <= 26 && candidateDistance > bestDistance) {
          bestLocation = candidateLocation;
          bestDistance = candidateDistance;
        }

        // if both are out of danger, move to the one that is closer but still safe
        if (candidateDistance >= 27 && candidateDistance < bestDistance) {
          bestLocation = candidateLocation;
          bestDistance = candidateDistance;
        }
      }
    }
    return bestLocation;
  }

  /**
   * Go away from other friendly amplifiers.
   * @param targetLocation
   * @return null if don't need to. Maplocation of the other amp we should take one step away from
   * @throws GameActionException
   */
  private MapLocation separateFromOtherAmplifiers(MapLocation targetLocation) throws GameActionException {
//    int myDistanceToTarget = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, targetLocation);
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (robot.type != RobotType.AMPLIFIER) continue;
      if (robot.location.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) > 20) continue;
      return robot.location;
    }
    return null;
  }

  /**
   *
   * @param location
   * @return the farthest ally launcher from the given location. Null if not found
   * @throws GameActionException
   */
  private MapLocation farthestAllyLauncherFrom(MapLocation location) throws GameActionException {
    int farthestDist = 0;
    MapLocation bestLoc = null;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (robot.type != RobotType.LAUNCHER) continue;
      int dist = robot.location.distanceSquaredTo(location);
      if (dist > farthestDist) {
        bestLoc = robot.location;
        farthestDist = dist;
      }
    }
    return bestLoc;
  }


  private MapLocation closestEnemyLauncher() {
    MapLocation closestLauncher = null;
    int distance = Integer.MAX_VALUE;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (robot.type == RobotType.LAUNCHER) {
        int dist = robot.location.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
        if (dist < distance) {
          distance = dist;
          closestLauncher = robot.location;
        }
      }
    }
    return closestLauncher;
  }

  private boolean ifValidEnemyHQ(MapLocation HQLocation) throws GameActionException {
    if (rc.canSenseLocation(HQLocation)) {
      RobotInfo robot = rc.senseRobotAtLocation(HQLocation);
      if (robot == null || robot.type != RobotType.HEADQUARTERS || robot.team != Cache.Permanent.OPPONENT_TEAM) {
//        Printer.print("ERROR: expected enemy HQ is not an HQ " + HQLocation, "symmetry guess must be wrong, eliminating symmetry (" + RunningMemory.guessedSymmetry + ") and retrying...");
        RunningMemory.markInvalidSymmetry(RunningMemory.guessedSymmetry);
        // TODO: eliminate symmetry and retry
//          RunningMemory.publishNotSymmetry(RunningMemory.guessedSymmetry);
//          explorationTarget = communicator.archonInfo.replaceEnemyArchon(explorationTarget)
        return false;
      }
    }
    return true;
  }

  private MapLocation locationToStayInFrontOfFriendlyLaunchers() throws GameActionException {
    int numHQs = HqMetaInfo.hqCount;
    int numTries = 0;
    MapLocation[] HQLocations = new MapLocation[numHQs];
    for (int i = 0; i < numHQs && numTries < 4; i++) {
      MapLocation HQLocation = HqMetaInfo.enemyHqLocations[i];
      // check whether or not we are confident that the symmetry is right before doing this over and over
      if (RunningMemory.knownSymmetry == null) {
        if (!ifValidEnemyHQ(HQLocation)) {
          i = -1;
          ++numTries;
          continue;
        }
      }
      HQLocations[i] = HQLocation;
    }

    if (Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length == 0) {
      return null;
    }

    MapLocation furthestFriendlyLauncherFromBase = null;
    int furthestDistance = Integer.MAX_VALUE;
    int closestGlobalHQ = -1;

    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (robot.type == RobotType.LAUNCHER) {
        int distance = Integer.MAX_VALUE;
        int closestHQ = -1;
        for (int i = 0; i < numHQs; i++) {
          int candidateDistance = Utils.maxSingleAxisDist(robot.location, HQLocations[i]);
          if (candidateDistance < distance) {
            distance = candidateDistance;
            closestHQ = i;
          }
        }

        if (distance < furthestDistance) {
          furthestDistance = distance;
          furthestFriendlyLauncherFromBase = robot.location;
          closestGlobalHQ = closestHQ;
        }
      }
    }

    if (furthestFriendlyLauncherFromBase == null) {
      return null;
    }

//    Direction towardsEnemyHQ = furthestFriendlyLauncherFromBase.directionTo(HQLocations[closestGlobalHQ]);
//    MapLocation targetLocation = furthestFriendlyLauncherFromBase.add(towardsEnemyHQ).add(towardsEnemyHQ);
    Direction awayFromEnemyHQ = furthestFriendlyLauncherFromBase.directionTo(HQLocations[closestGlobalHQ]).opposite();
    MapLocation targetLocation = furthestFriendlyLauncherFromBase.add(awayFromEnemyHQ).add(awayFromEnemyHQ);
    return targetLocation;
  }
}
