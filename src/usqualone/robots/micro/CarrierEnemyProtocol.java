package usqualone.robots.micro;

import usqualone.communications.HqMetaInfo;
import usqualone.knowledge.Cache;
import usqualone.robots.Carrier;
import usqualone.robots.pathfinding.BugNav;
import usqualone.robots.pathfinding.Pathing;
import usqualone.robots.pathfinding.SmitePathing;
import usqualone.utils.Global;
import usqualone.utils.Utils;
import battlecode.common.*;

public class CarrierEnemyProtocol {

  private static RobotController rc;
  private static Carrier carrier;
  private static Pathing pathing;

  private static int fleeingCounter;


  public static MapLocation lastEnemyLocation;
  public static int lastEnemyLocationRound;
  public static RobotInfo cachedLastEnemyForBroadcast;

  public static void init(Carrier carrier) {
    CarrierEnemyProtocol.carrier = carrier;
    rc = Global.rc;
    pathing = Pathing.globalPathing;
  }

  public static boolean doProtocol() throws GameActionException {
    SmitePathing.forceOneBug = true;
    if (enemyExists()) {
      if (rc.getAnchor() == null && rc.getResourceAmount(ResourceType.ELIXIR) == 0) {
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
    MapLocation closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    if (fleeingCounter > 0) {
      if (Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, closestHQ) <= MicroConstants.CARRIER_DIST_TO_HQ_TO_RUN_HOME) {
        fleeingCounter = MicroConstants.CARRIER_TURNS_TO_FLEE;
      }
    }
    MapLocation fleeTarget = closestHQ;
    if (lastEnemyLocation != null) {
      Direction toHome = Cache.PerTurn.CURRENT_LOCATION.directionTo(closestHQ);
      Direction toEnemy = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation);
      if (toHome == toEnemy || toHome == toEnemy.rotateLeft() || toHome == toEnemy.rotateRight()) {
        fleeTarget = Cache.PerTurn.CURRENT_LOCATION.subtract(toEnemy).subtract(toEnemy).subtract(toEnemy).subtract(toEnemy).subtract(toEnemy);
      }
    }
    if (fleeingCounter > 0) {
      // run from lastEnemyLocation
//      Direction away = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation).opposite();
//      MapLocation fleeDirection = Cache.PerTurn.CURRENT_LOCATION.add(away).add(away).add(away).add(away).add(away);
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
    }
    SmitePathing.forceOneBug = false;
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
      lastEnemyLocationRound = rc.getRoundNum();
      fleeingCounter = MicroConstants.CARRIER_TURNS_TO_FLEE;
      // check if we need to cache the enemy for broadcasting (only if no friendly launchers nearby)
      cachedLastEnemyForBroadcast = nearestCombatEnemy;
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
    }
  }


}
