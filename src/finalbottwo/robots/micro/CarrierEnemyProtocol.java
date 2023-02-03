package finalbottwo.robots.micro;

import finalbottwo.communications.HqMetaInfo;
import finalbottwo.knowledge.Cache;
import finalbottwo.robots.Carrier;
import finalbottwo.robots.pathfinding.BugNav;
import finalbottwo.robots.pathfinding.Pathing;
import finalbottwo.robots.pathfinding.SmitePathing;
import finalbottwo.utils.Global;
import finalbottwo.utils.Printer;
import finalbottwo.utils.Utils;
import battlecode.common.*;

public class CarrierEnemyProtocol {

  public static final int MAX_BROADCAST_TURNS = 5;
  private static RobotController rc;
  private static Carrier carrier;
  private static Pathing pathing;

  private static int fleeingCounter;
  private static MapLocation closestHQ;
  private static MapLocation fleeTarget;


  public static MapLocation lastEnemyLocation;
  public static int lastEnemyLocationRound;
  public static RobotInfo cachedLastEnemyForBroadcast;
  public static int cachedLastEnemyBroadcastCount;

  public static void init(Carrier carrier) {
    CarrierEnemyProtocol.carrier = carrier;
    rc = Global.rc;
    pathing = Pathing.globalPathing;
  }

  public static boolean doProtocol() throws GameActionException {
    SmitePathing.forceBugging = true;
    if (enemyExists()) {
      attack_enemy: if (rc.getAnchor() == null && rc.getResourceAmount(ResourceType.ELIXIR) == 0) {
        try_escape: {
          if (!rc.isMovementReady()) break try_escape;
          MapLocation escapeTarget = getEscapeLocation();
          if (escapeTarget != null) {
            Printer.appendToIndicator("xcp-" + escapeTarget);
            while (rc.isMovementReady()
                && pathing.moveTowards(escapeTarget)
                && !Cache.PerTurn.CURRENT_LOCATION.equals(escapeTarget)) {
              escapeTarget = getEscapeLocation();
              if (escapeTarget == null) {
                break attack_enemy;
              }
              Printer.appendToIndicator("xcp-" + escapeTarget);
            }
            break attack_enemy;
          }
        }
        RobotInfo enemyToAttack = enemyToAttackIfWorth();
        if (enemyToAttack == null) enemyToAttack = attackEnemyIfCannotRun();
//        Printer.print("enemyToAttack - " + enemyToAttack);
        if (enemyToAttack != null && rc.isActionReady()) {
          // todo: attack it!
          if (rc.canAttack(enemyToAttack.location)) {
//            Printer.print("it can attack w/o moving");
            rc.attack(enemyToAttack.location);
          } else {
            // move and then attack
            if (enemyToAttack.type == RobotType.CARRIER && rc.getResourceAmount(ResourceType.MANA) == 0 && rc.getResourceAmount(ResourceType.ELIXIR) == 0) {
              pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(enemyToAttack.location));
//            Printer.print("moved to attack... " + rc.canAttack(enemyToAttack.location), "loc=" + enemyToAttack.location, "canAct=" + rc.canActLocation(enemyToAttack.location), "robInfo=" + rc.senseRobotAtLocation(enemyToAttack.location));
              if (rc.canAttack(enemyToAttack.location)) {
                rc.attack(enemyToAttack.location);
              }
            }
          }
        }
      }
      updateLastEnemy();
    }
    if (fleeingCounter > 0 && rc.getWeight() > 0) {
      if (Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, closestHQ) <= MicroConstants.CARRIER_DIST_TO_HQ_TO_RUN_HOME) {
        fleeingCounter = MicroConstants.CARRIER_TURNS_TO_FLEE;
      }
    }
    if (fleeingCounter > 0) {
      // run from lastEnemyLocation
//      Direction away = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation).opposite();
//      MapLocation fleeDirection = Cache.PerTurn.CURRENT_LOCATION.add(away).add(away).add(away).add(away).add(away);
      Printer.appendToIndicator("flee-" + fleeingCounter + "(" + lastEnemyLocation + ")>" + fleeTarget);
      while (rc.isMovementReady() && pathing.moveTowards(fleeTarget) && --fleeingCounter >= 0) {
        if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(closestHQ)) {
          carrier.transferAllResources(closestHQ);
          fleeingCounter = 0;
        }
      }
//      if (cachedLastEnemyForBroadcast != null) { // we need to broadcast this enemy
//        forcedNextTask = CarrierTask.DELIVER_RSS_HOME;
//        resetTask();
//      }
      if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(closestHQ)) {
        carrier.transferAllResources(closestHQ);
        fleeingCounter = 0;
      }
      if (!fleeTarget.equals(closestHQ) && Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(fleeTarget)) {
        closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        fleeTarget = closestHQ;
//        fleeingCounter = 0;
      }
    }
    SmitePathing.forceBugging = false;
    return fleeingCounter > 0;
  }


  /*
  CARRIER BEHAVIOR AGAINST OPPONENT
  1) if we can kill it effectively, kill it
  2) if we cannot kill it
    2.1) attempt to run away. if we are going die (or maybe easy health is <= 7)
  * */

  private static boolean enemyExists() throws GameActionException {
    return Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0;
  }

  /**
   * determines if there's some location from which we can run away from the enemy
   * @return null if no escape location (or no enemies)
   */
  private static MapLocation getEscapeLocation() throws GameActionException {
    if (Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length == 0) return null;
    MapLocation escapeLocations0 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.NORTH);
    MapLocation escapeLocations1 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.NORTHEAST);
    MapLocation escapeLocations2 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.EAST);
    MapLocation escapeLocations3 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.SOUTHEAST);
    MapLocation escapeLocations4 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.SOUTH);
    MapLocation escapeLocations5 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.SOUTHWEST);
    MapLocation escapeLocations6 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.WEST);
    MapLocation escapeLocations7 = Cache.PerTurn.CURRENT_LOCATION.add(Direction.NORTHWEST);
    boolean isCloud0 = rc.senseCloud(escapeLocations0);
    boolean isCloud1 = rc.senseCloud(escapeLocations1);
    boolean isCloud2 = rc.senseCloud(escapeLocations2);
    boolean isCloud3 = rc.senseCloud(escapeLocations3);
    boolean isCloud4 = rc.senseCloud(escapeLocations4);
    boolean isCloud5 = rc.senseCloud(escapeLocations5);
    boolean isCloud6 = rc.senseCloud(escapeLocations6);
    boolean isCloud7 = rc.senseCloud(escapeLocations7);
    boolean canRunAway0 = BugNav.canMoveInDirection(Direction.NORTH);
    boolean canRunAway1 = BugNav.canMoveInDirection(Direction.NORTHEAST);
    boolean canRunAway2 = BugNav.canMoveInDirection(Direction.EAST);
    boolean canRunAway3 = BugNav.canMoveInDirection(Direction.SOUTHEAST);
    boolean canRunAway4 = BugNav.canMoveInDirection(Direction.SOUTH);
    boolean canRunAway5 = BugNav.canMoveInDirection(Direction.SOUTHWEST);
    boolean canRunAway6 = BugNav.canMoveInDirection(Direction.WEST);
    boolean canRunAway7 = BugNav.canMoveInDirection(Direction.NORTHWEST);
    int attackers0 = 0;
    int attackers1 = 0;
    int attackers2 = 0;
    int attackers3 = 0;
    int attackers4 = 0;
    int attackers5 = 0;
    int attackers6 = 0;
    int attackers7 = 0;
    boolean anyAttackers = false;
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      switch (enemyRobot.type) {
        case HEADQUARTERS:
        case CARRIER:
        case BOOSTER:
        case AMPLIFIER:
          continue;
      }
      anyAttackers = true;
      MapLocation enemyLoc = enemyRobot.location;
      int enemyActionRadSq = rc.senseCloud(enemyLoc) ? GameConstants.CLOUD_VISION_RADIUS_SQUARED : enemyRobot.type.actionRadiusSquared;
      if (canRunAway0 && (isCloud0 ? enemyLoc.isWithinDistanceSquared(escapeLocations0, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations0, enemyActionRadSq))) {
        attackers0++;
      }
      if (canRunAway1 && (isCloud1 ? enemyLoc.isWithinDistanceSquared(escapeLocations1, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations1, enemyActionRadSq))) {
        attackers1++;
      }
      if (canRunAway2 && (isCloud2 ? enemyLoc.isWithinDistanceSquared(escapeLocations2, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations2, enemyActionRadSq))) {
        attackers2++;
      }
      if (canRunAway3 && (isCloud3 ? enemyLoc.isWithinDistanceSquared(escapeLocations3, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations3, enemyActionRadSq))) {
        attackers3++;
      }
      if (canRunAway4 && (isCloud4 ? enemyLoc.isWithinDistanceSquared(escapeLocations4, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations4, enemyActionRadSq))) {
        attackers4++;
      }
      if (canRunAway5 && (isCloud5 ? enemyLoc.isWithinDistanceSquared(escapeLocations5, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations5, enemyActionRadSq))) {
        attackers5++;
      }
      if (canRunAway6 && (isCloud6 ? enemyLoc.isWithinDistanceSquared(escapeLocations6, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations6, enemyActionRadSq))) {
        attackers6++;
      }
      if (canRunAway7 && (isCloud7 ? enemyLoc.isWithinDistanceSquared(escapeLocations7, GameConstants.CLOUD_VISION_RADIUS_SQUARED) : enemyLoc.isWithinDistanceSquared(escapeLocations7, enemyActionRadSq))) {
        attackers7++;
      }
    }
    if (!anyAttackers) {
      return null;
    }
    int minAttackers = Integer.MAX_VALUE;
    MapLocation minAttackerEscapeLocation = null;
    if (canRunAway0 && attackers0 < minAttackers) {
      minAttackers = attackers0;
      minAttackerEscapeLocation = escapeLocations0;
    }
    if (canRunAway1 && attackers1 < minAttackers) {
      minAttackers = attackers1;
      minAttackerEscapeLocation = escapeLocations1;
    }
    if (canRunAway2 && attackers2 < minAttackers) {
      minAttackers = attackers2;
      minAttackerEscapeLocation = escapeLocations2;
    }
    if (canRunAway3 && attackers3 < minAttackers) {
      minAttackers = attackers3;
      minAttackerEscapeLocation = escapeLocations3;
    }
    if (canRunAway4 && attackers4 < minAttackers) {
      minAttackers = attackers4;
      minAttackerEscapeLocation = escapeLocations4;
    }
    if (canRunAway5 && attackers5 < minAttackers) {
      minAttackers = attackers5;
      minAttackerEscapeLocation = escapeLocations5;
    }
    if (canRunAway6 && attackers6 < minAttackers) {
      minAttackers = attackers6;
      minAttackerEscapeLocation = escapeLocations6;
    }
    if (canRunAway7 && attackers7 < minAttackers) {
      minAttackers = attackers7;
      minAttackerEscapeLocation = escapeLocations7;
    }
    if (minAttackers == 0) {
      return minAttackerEscapeLocation;
    }
    return null;
  }

  private static RobotInfo enemyToAttackIfWorth() throws GameActionException {
//    Printer.print("enemyToAttackIfWorth()");
    int myInvSize = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA)/2);
    if (myInvSize <= 4) {
//      Printer.print("null bc invSize <= 4");
      return null;
    }

    // if enemy launcher, consider attacking 1) closest
    RobotInfo bestEnemyToAttack = null;
    int bestValue = 0;

    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      RobotType type = enemyRobot.type;
      if (type == RobotType.HEADQUARTERS) continue;
      int costToBuild = type.buildCostAdamantium + type.buildCostMana + type.buildCostElixir;
      int carryingResourceValue = Utils.getInvWeight(enemyRobot);
      int enemyValue = costToBuild + carryingResourceValue;
      //todo: maybe make if-statement always true for launchers depending on if we have launchers or not
      if (enemyRobot.health / GameConstants.CARRIER_DAMAGE_FACTOR <= enemyValue || type == RobotType.LAUNCHER) { // it is worth attacking this enemy;
        // determine if we have enough to attack it...
        int totalDmg = 0;
        if (enemyRobot.type == RobotType.CARRIER) totalDmg -= rc.getResourceAmount(ResourceType.MANA)/2 * GameConstants.CARRIER_DAMAGE_FACTOR;
        totalDmg += myInvSize * GameConstants.CARRIER_DAMAGE_FACTOR;
        RobotInfo[] robotInfos = rc.senseNearbyRobots(enemyRobot.location, -1, Cache.Permanent.OUR_TEAM); // does not return this robot as well
        for (RobotInfo friendlyRobot : robotInfos) {
          //todo: maybe dont consider launchers in dmg calculation here
          int friendDamage = (int) ((friendlyRobot.getResourceAmount(ResourceType.ADAMANTIUM) + (enemyRobot.type == RobotType.CARRIER ? 0 : friendlyRobot.getResourceAmount(ResourceType.MANA)/2)) * GameConstants.CARRIER_DAMAGE_FACTOR);
          totalDmg += (friendDamage) + friendlyRobot.type.damage;
        }

//        Printer.print("enemy location: " + enemyRobot.location + " we deal: " + totalDmg);
        // todo: consider allowing only launcher to attack or smth?
        if (totalDmg > enemyRobot.health) { // we can kill it
          if (bestEnemyToAttack == null || enemyValue > bestValue || (enemyValue == bestValue && bestEnemyToAttack.health < enemyRobot.health)) {
            bestEnemyToAttack = enemyRobot;
            bestValue = enemyValue;
          }
        }
      }
    }
//    Printer.print("bestEnemyToAttack=" + bestEnemyToAttack + ", value=" + bestValue);
    return bestEnemyToAttack;
    //todo: perform the attack here?
  }

  /**
   * determines if we can run away based on enemy damage
   * @return the enemy to attack ONLY if we cannot run away from enemy
   * @throws GameActionException
   */
  private static RobotInfo attackEnemyIfCannotRun() throws GameActionException {
//    Printer.print("attackEnemyIfCannotRun()");
    int myInvSize = rc.getWeight();
    if (myInvSize <= 4) {
//      Printer.print("null bc invSize <= 4");
      return null;
    }

    // sum damage based on how many enemies can currently attack me (this bot must be within action radius of enemy's bot)
    int enemyDamage = 0;
    RobotInfo enemyToAttack = null;
//    int numMoves = numMoves();
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemyRobot.type == RobotType.HEADQUARTERS) continue;
      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(enemyRobot.location, enemyRobot.type.actionRadiusSquared)) {
        enemyDamage += Utils.getInvWeight(enemyRobot) * GameConstants.CARRIER_DAMAGE_FACTOR + enemyRobot.type.damage;
      }
      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(enemyRobot.location, Cache.Permanent.ACTION_RADIUS_SQUARED)) { // todo: need to consider movement here
        if (enemyToAttack == null || ((!(enemyToAttack.type == RobotType.LAUNCHER || enemyToAttack.type == RobotType.DESTABILIZER)) && (enemyRobot.type == RobotType.LAUNCHER || enemyRobot.type == RobotType.DESTABILIZER)))
          enemyToAttack = enemyRobot;
      }
    }

//    if (enemyToAttack == null) {
////      Printer.print("enemyToAttack=null" + ", enemies can deal: " + enemyDamage);
//    } else {
////      Printer.print("enemyToAttack loc:" + enemyToAttack.location + ", enemies can deal: " + enemyDamage);
//    }

    if (enemyDamage >= 1.5*Cache.PerTurn.HEALTH) {
//      Printer.print("attack bc no option!!");
      return enemyToAttack;
    }
//    Printer.print("I not die, return null");
    return null;
  }

  private static void updateLastEnemy() {
    // get nearest enemy
    // set fleeing to 6
    // todo: consider whether or not to run away from enemy carriers
    RobotInfo nearestCombatEnemy = null;
    int myDistanceToNearestEnemy = Integer.MAX_VALUE;
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemyRobot.type == RobotType.LAUNCHER || enemyRobot.type == RobotType.DESTABILIZER) {
        int dist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(enemyRobot.location);
        if (dist < myDistanceToNearestEnemy) {
          nearestCombatEnemy = enemyRobot;
          myDistanceToNearestEnemy = dist;
        }
      }
    }
//    Printer.print("updateLastEnemy()");
    if (nearestCombatEnemy != null) {
      lastEnemyLocation = nearestCombatEnemy.location;
      lastEnemyLocationRound = Cache.PerTurn.ROUND_NUM;
      fleeingCounter = MicroConstants.CARRIER_TURNS_TO_FLEE;
      // check if we need to cache the enemy for broadcasting (only if no friendly launchers nearby)
      cachedLastEnemyForBroadcast = nearestCombatEnemy;
      cachedLastEnemyBroadcastCount = 0;
      for (RobotInfo friendlyRobot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
        if (friendlyRobot.type == RobotType.LAUNCHER) {
          int distToEnemy = friendlyRobot.location.distanceSquaredTo(lastEnemyLocation);
          if (distToEnemy <= myDistanceToNearestEnemy) {
//            cachedLastEnemyForBroadcast = null;
            fleeingCounter--;
//            break;
          } else if (distToEnemy <= friendlyRobot.type.actionRadiusSquared) {
//            cachedLastEnemyForBroadcast = null;
          }
//          break;
        }
      }
//      if (cachedLastEnemyForBroadcast == null) { // we found friends nearby
//        fleeingCounter /= 2;
//      }
      closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
      fleeTarget = closestHQ;
      if (lastEnemyLocation != null && myDistanceToNearestEnemy <= Cache.Permanent.VISION_RADIUS_SQUARED*2) {
        Direction toHome = Cache.PerTurn.CURRENT_LOCATION.directionTo(closestHQ);
        Direction toEnemy = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation);
        if (myDistanceToNearestEnemy <= nearestCombatEnemy.type.actionRadiusSquared || toHome == toEnemy || toHome == toEnemy.rotateLeft() || toHome == toEnemy.rotateRight()) {
          fleeTarget = Cache.PerTurn.CURRENT_LOCATION.subtract(toEnemy).subtract(toEnemy).subtract(toEnemy).subtract(toEnemy).subtract(toEnemy);
        }
        fleeTarget = Utils.clampToMap(fleeTarget);
      }
    }
  }


}
