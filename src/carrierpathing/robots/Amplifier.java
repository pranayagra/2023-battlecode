package carrierpathing.robots;

import carrierpathing.communications.HqMetaInfo;
import carrierpathing.communications.MapMetaInfo;
import carrierpathing.utils.Cache;
import carrierpathing.utils.Printer;
import carrierpathing.utils.Utils;
import battlecode.common.*;

public class Amplifier extends Robot {
  public Amplifier(RobotController rc) throws GameActionException {
    super(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    MapLocation closestEnemyLocation = closestEnemyLauncher();
    if (closestEnemyLocation != null) {
      //todo: comm() location
      //todo: update so we stay slighly out of range of enemy launcher
      int currDistance = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(closestEnemyLocation);
      if (currDistance >= 20) {
        // safe, chill here

      } else {
        Direction away = Cache.PerTurn.CURRENT_LOCATION.directionTo(closestEnemyLocation).opposite();
        MapLocation fleeDirection = Cache.PerTurn.CURRENT_LOCATION.add(away).add(away).add(away).add(away).add(away);
        pathing.moveTowards(fleeDirection);
        return;
      }
    }

    MapLocation targetLocation = locationToStayInFrontOfFriendlyLaunchers();
    if (targetLocation != null) {
      pathing.moveTowards(targetLocation);
    } else {
//      pathing.moveTowards(Cache.Permanent.ENEMY_HQ_LOCATION);

    }
  }

  private void separateFromOtherAmplifiers() {

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
//        Printer.print("ERROR: expected enemy HQ is not an HQ " + HQLocation, "symmetry guess must be wrong, eliminating symmetry (" + MapMetaInfo.guessedSymmetry + ") and retrying...");
        if (rc.canWriteSharedArray(0,0)) {
          MapMetaInfo.writeNot(MapMetaInfo.guessedSymmetry);
        }
        // TODO: eliminate symmetry and retry
//          RunningMemory.publishNotSymmetry(MapMetaInfo.guessedSymmetry);
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
      if (MapMetaInfo.knownSymmetry == null) {
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

    Direction towardsEnemyHQ = furthestFriendlyLauncherFromBase.directionTo(HQLocations[closestGlobalHQ]);
    MapLocation targetLocation = furthestFriendlyLauncherFromBase.add(towardsEnemyHQ).add(towardsEnemyHQ).add(towardsEnemyHQ);
    return targetLocation;
  }

  private void commEnemies() {

  }

  private void goBehindLaunchers() {

  }
}
