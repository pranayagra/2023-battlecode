package actuallaunchermicro.robots;

import actuallaunchermicro.communications.Communicator;
import actuallaunchermicro.communications.HqMetaInfo;
import actuallaunchermicro.containers.HashSet;
import actuallaunchermicro.robots.micro.AttackMicro;
import actuallaunchermicro.utils.Cache;
import actuallaunchermicro.utils.Utils;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/**
 * Garbo written class for Sprint 1 just based on my general micro idea.
 * Pathing: to one of our wells (random between AD / Mana), closest enemy well, then eHQ -> eHQ -> eHQ
 */
public class LauncherAlex extends MobileRobot {
  private enum TargetType{
    OUR_HQ,
    OUR_WELL,
    ENEMY_WELL,
    ENEMY_HQ;

    private TargetType next() {
      switch(this) {
        case OUR_HQ:
          return OUR_WELL;
        case OUR_WELL:
          return ENEMY_WELL;
        case ENEMY_WELL:
        case ENEMY_HQ:
        default:
          return ENEMY_HQ;
      }
    }
  }
  private static final int MIN_TURN_TO_MOVE = 0;
  private static final int MIN_GROUP_SIZE_TO_MOVE = 3; // min group size to move out
  private static final int TURNS_TO_WAIT = 20; // turns to wait until going back to nearest HQ
  private static final int TURNS_AT_TARGET = 20;
  private int numTurnsWaiting = 0;
  private int numTurnsNearTarget = 0;
  private HashSet<MapLocation> visitedLocations = new HashSet<>(4);
  private MapLocation targetLocation;
  private MapLocation oldTarget;
  private TargetType currentTargetType = TargetType.OUR_HQ;

  private Launcher microBoi;

  public LauncherAlex(RobotController rc) throws GameActionException {
    super(rc);
    microBoi = new Launcher(rc);
  }

  @Override
  protected void runTurn() throws GameActionException {
    MapLocation ans = AttackMicro.getBestMovementPosition();
    if (ans != null) {
      microBoi.runTurn();
      return;
    }

    rc.setIndicatorString("Ooga booga i A launcher");
    updateTarget();
    // attack all nearby enemy robots
    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    int numEnemies = 0;
    for (int i = allNearbyEnemyRobots.length; --i >= 0 && rc.isActionReady();) {
      RobotInfo enemy = allNearbyEnemyRobots[i];
      if (enemy.type != RobotType.HEADQUARTERS) {
        numEnemies += 1;
      }
      attack(enemy.location);
    }

    // if I could not attack anyone in my vision range above, then consider cloud attacks (and if success, we should move backwards?)
    // need to be careful with cd multiplier causing isAction to be false after a couple successful attacks
    if (rc.isActionReady()) {
      if (attemptCloudAttack()) {
        rc.setIndicatorString("successful cloud attack -> exit early");
        return;
      }
    }
    if (numEnemies == 0) {
      // no enemies nearby
      runTurn_noEnemies();
      return;
    } else {
      // see an enemy
      numTurnsNearTarget = 0;
      doMicro();
    }
  }

  private void doMicro() throws GameActionException {
    microBoi.runTurn();
  }
  private void updateTarget() throws GameActionException {
//    MapLocation ans = AttackMicro.getBestTarget();
//    if (ans != null) {
//      // need to immediately respond
//      oldTarget = targetLocation;
//      targetLocation = ans;
//      return;
//    }
    // restore old target
    if(oldTarget != null) {
      targetLocation = oldTarget;
      oldTarget = null;
    }
    if (numTurnsNearTarget > TURNS_AT_TARGET || targetLocation == null) {
      numTurnsNearTarget = 0;
      // update the target
      currentTargetType = currentTargetType.next();
      switch(currentTargetType) {
        case OUR_WELL:
          ResourceType rt = ResourceType.ADAMANTIUM;
          if(Utils.rng.nextBoolean()) rt = ResourceType.MANA;
          targetLocation = Communicator.getClosestWellLocation(Cache.PerTurn.CURRENT_LOCATION, rt); // TODO: make pick mana vs ad
          return;
        case ENEMY_WELL: // TODO: go to enemy well -- rn just go to HQ
        case ENEMY_HQ:

//          targetLocation = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
          MapLocation[] enemyHQs = HqMetaInfo.enemyHqLocations;
          if(visitedLocations.size == enemyHQs.length) { visitedLocations = new HashSet<>(4);}

          MapLocation closestHQ = null;
          int bestDist = 1000000;
          for (MapLocation enemyHQ : enemyHQs) {
            if(visitedLocations.contains(enemyHQ)) continue;
            int dist = enemyHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
            if (dist < bestDist) {
              bestDist = dist;
              closestHQ = enemyHQ;
            }
          }
          targetLocation = closestHQ;
          visitedLocations.add(targetLocation);
      }
    }

  }
  private void runTurn_noEnemies() throws GameActionException {
    if (currentTargetType == TargetType.OUR_HQ) {
      attemptMoveTowards(targetLocation);
    }
    RobotInfo[] allNearbyEnemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    RobotInfo[] allAllyLaunchers = Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS;
    MapLocation closestFriendToTargetLoc = Cache.PerTurn.CURRENT_LOCATION;
    int closestFriendDistToTargetDist = closestFriendToTargetLoc.distanceSquaredTo(targetLocation);
    if(Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(targetLocation, 9)) {
      numTurnsNearTarget++;
    }
    // adjacentAllyLaunchers
    int adjacentAllyLaunchers = 0;
    for (RobotInfo ri : allAllyLaunchers) {
      if (ri.type == RobotType.LAUNCHER) {
        int friendToTargetDist = ri.location.distanceSquaredTo(targetLocation);
        if(friendToTargetDist < closestFriendDistToTargetDist) {
          closestFriendToTargetLoc = ri.location;
          closestFriendDistToTargetDist = friendToTargetDist;
        }
        if(ri.location.isAdjacentTo(Cache.PerTurn.CURRENT_LOCATION)) {
          adjacentAllyLaunchers++;
        }
      }
    }

    if (adjacentAllyLaunchers < MIN_GROUP_SIZE_TO_MOVE - 1) {
      if(allAllyLaunchers.length > 1 && !closestFriendToTargetLoc.isAdjacentTo(Cache.PerTurn.CURRENT_LOCATION)) { // 1 for self
        // move towards friend closest to current target
        rc.setIndicatorString("moving towards friend at " + closestFriendToTargetLoc + "target: " + targetLocation);
        attemptMoveTowards(closestFriendToTargetLoc);
        return;
      }
      // stay still, not enough friends
      numTurnsWaiting++;
      if(numTurnsWaiting > TURNS_TO_WAIT) {
        // go back to nearest HQ
        currentTargetType = TargetType.OUR_HQ;
        targetLocation = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        rc.setIndicatorString("retreating towards " + targetLocation);
        attemptMoveTowards(targetLocation);
      }
    } else {
      // ayy we got friends, now we can go!
      numTurnsWaiting = 0;
      rc.setIndicatorString("got friends! lessgo! to " + targetLocation + " - turns@target:" + numTurnsNearTarget);
      attemptMoveTowards(targetLocation);
    }
    // y not
    if (rc.isActionReady()) {
      attemptCloudAttack();
    }
  }

  private boolean attemptMoveTowards(MapLocation target) throws GameActionException{
    if(Cache.PerTurn.ROUND_NUM % 2 == 0) {
      return pathing.moveTowards(target);
    }
    return false;
  }
  private boolean attack(MapLocation loc) throws GameActionException {
//    if (rc.canAttack(loc)) return false; //todo : Remove after!
    if (rc.canAttack(loc)) {
      rc.attack(loc);
      return true;
    }
    return false;
  }

  private boolean attemptCloudAttack() throws GameActionException {
    int cells = (int) Math.ceil(Math.sqrt(Cache.Permanent.ACTION_RADIUS_SQUARED));
    for (int i = -cells; i <= cells; ++i) {
      for (int j = -cells; j <= cells; ++j) {
        MapLocation loc = Cache.PerTurn.CURRENT_LOCATION.translate(i, j);
        if (rc.canAttack(loc)) {
          rc.attack(loc);
          return true;
        }
      }
    }
    return false;
//    MapLocation[] allLocationsWithinAction = rc.getAllLocationsWithinRadiusSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.ACTION_RADIUS_SQUARED);
//    for (MapLocation loc : allLocationsWithinAction) {
//      if (Cache.PerTurn.ROUND_NUM == 131 && rc.getID() == 10216) {
//        Printer.print("Checking " + loc + ", has? " + rc.canActLocation(loc) + " and " + rc.canAttack(loc));
//      }
//      if (rc.canAttack(loc)) {
//        rc.attack(loc);
//        return;
//      }
//    }
  }
}
