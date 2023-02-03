package carrierpositioning.robots.micro;

import carrierpositioning.communications.HqMetaInfo;
import carrierpositioning.robots.pathfinding.BugNav;
import carrierpositioning.robots.pathfinding.Pathing;
import carrierpositioning.knowledge.Cache;
import carrierpositioning.utils.Printer;
import carrierpositioning.utils.Utils;
import battlecode.common.*;

public class AttackerFightingMicro {

  public static RobotController rc;
  public static Pathing pathing;


  static boolean attacker = false;
  static boolean shouldStayInRange = false;
  static boolean hurt = false; //TODO: if hurt we want to go back to archon
  static int myActionRange;
  static int myVisionRange;
  static double myDPS;
  static boolean severelyHurt = false;

  static double[] DPS = new double[]{0, 0, 0, 0, 0, 0, 0};
  static int[] visionRange = new int[]{0, 0, 0, 0, 0, 0, 0};


  static double currentDPS = 0;
  static double currentVisionRange;
  static double currentActionRadius;
  static boolean canAttack;

  static int totalAllyArmyHealth;
  static int totalEnemyArmyHealth;


  public static void init(RobotController rc, Pathing pathing) {
    AttackerFightingMicro.rc = rc;
    AttackerFightingMicro.pathing = pathing;
    switch (Cache.Permanent.ROBOT_TYPE) {
      case LAUNCHER:
        attacker = true;
        break;
    }
    myActionRange = Cache.Permanent.ROBOT_TYPE.actionRadiusSquared;
    myVisionRange = Cache.Permanent.ROBOT_TYPE.visionRadiusSquared;

    //TODO: add HQ DPS here too
    DPS[RobotType.LAUNCHER.ordinal()] = RobotType.LAUNCHER.damage * 1.0 * GameConstants.COOLDOWNS_PER_TURN / RobotType.LAUNCHER.actionCooldown;
//    DPS[RobotType.SAGE.ordinal()] = 9;
    visionRange[RobotType.LAUNCHER.ordinal()] = RobotType.LAUNCHER.visionRadiusSquared;
//    rangeExtended[RobotType.SAGE.ordinal()] = 34;
    myDPS = DPS[Cache.Permanent.ROBOT_TYPE.ordinal()];
  }


  /**
   * will do micro movements to optimally fight enemies (doesn't micro against lone enemy HQ)
   * @return true if micro was done, false if the robot can do whatever it wants
   * @throws GameActionException any issues with sensing/moving/attacking
   */
  public static boolean doMicro() throws GameActionException {
    if (!rc.isMovementReady()) return false;
    boolean shouldMicro = false;
    severelyHurt = ishurt(Cache.PerTurn.HEALTH, Cache.Permanent.MAX_HEALTH);
    RobotInfo[] enemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    if (enemyRobots.length == 0) return false;
    canAttack = rc.isActionReady();
    int uIndex = enemyRobots.length;
    while (uIndex-- > 0) {
      RobotInfo r = enemyRobots[uIndex];
      switch(r.type) {
        case LAUNCHER:
        case DESTABILIZER:
          shouldMicro = true;
          break;
        case HEADQUARTERS:
          // HANDLED OUTSIDE OF MICRO
        default:
          break;
      }
    }
    if (!shouldMicro) return false;
    // clear out blocked locations for enemyHQs
    BugNav.blockedLocations.clear();

    shouldStayInRange = false;

    totalAllyArmyHealth = 0;
    for (RobotInfo ally : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      switch(ally.type) {
        case LAUNCHER:
          totalAllyArmyHealth += ally.health;
          break;
        default:
          break;
      }
    }
    totalEnemyArmyHealth = 0;
    for (RobotInfo enemy : enemyRobots) {
      switch(enemy.type) {
        case LAUNCHER:
          totalAllyArmyHealth += enemy.health;
          break;
        default:
          break;
      }
    }
    // we should never stay in range if we have the choice right?
//    if (!rc.isActionReady()) shouldStayInRange = true;
//    if (severelyHurt) shouldStayInRange = true;

    MicroInfo[] microInfo = new MicroInfo[9];
    for (int i = 9; --i >= 0;) microInfo[i] = new MicroInfo(Utils.directionsNine[i]);

    double minCooldown = microInfo[0].cooldownMultiplier;
    if (microInfo[1].canMove && minCooldown > microInfo[1].cooldownMultiplier) minCooldown = microInfo[1].cooldownMultiplier;
    if (microInfo[2].canMove && minCooldown > microInfo[2].cooldownMultiplier) minCooldown = microInfo[2].cooldownMultiplier;
    if (microInfo[3].canMove && minCooldown > microInfo[3].cooldownMultiplier) minCooldown = microInfo[3].cooldownMultiplier;
    if (microInfo[4].canMove && minCooldown > microInfo[4].cooldownMultiplier) minCooldown = microInfo[4].cooldownMultiplier;
    if (microInfo[5].canMove && minCooldown > microInfo[5].cooldownMultiplier) minCooldown = microInfo[5].cooldownMultiplier;
    if (microInfo[6].canMove && minCooldown > microInfo[6].cooldownMultiplier) minCooldown = microInfo[6].cooldownMultiplier;
    if (microInfo[7].canMove && minCooldown > microInfo[7].cooldownMultiplier) minCooldown = microInfo[7].cooldownMultiplier;
    if (microInfo[8].canMove && minCooldown > microInfo[8].cooldownMultiplier) minCooldown = microInfo[8].cooldownMultiplier;

    minCooldown *= MicroConstants.MAX_COOLDOWN_DIFF;
    if (minCooldown < 1) minCooldown = 1;
//    System.out.println(minCooldown);
    if (microInfo[8].cooldownMultiplier > minCooldown) microInfo[8].canMove = false;
    if (microInfo[7].cooldownMultiplier > minCooldown) microInfo[7].canMove = false;
    if (microInfo[6].cooldownMultiplier > minCooldown) microInfo[6].canMove = false;
    if (microInfo[5].cooldownMultiplier > minCooldown) microInfo[5].canMove = false;
    if (microInfo[4].cooldownMultiplier > minCooldown) microInfo[4].canMove = false;
    if (microInfo[3].cooldownMultiplier > minCooldown) microInfo[3].canMove = false;
    if (microInfo[2].cooldownMultiplier > minCooldown) microInfo[2].canMove = false;
    if (microInfo[1].cooldownMultiplier > minCooldown) microInfo[1].canMove = false;
    if (microInfo[0].cooldownMultiplier > minCooldown) microInfo[0].canMove = false;

//    boolean danger = false;
//    if (Cache.PerTurn.ROUND_NUM <= MicroConstants.ATTACK_TURN) {
//      MapLocation closestEnemyHQ = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
//      MapLocation closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
//      if (closestEnemyHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < closestHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION)) {
//        danger = true;
//      }
//    }
    RobotInfo[] friendlyRobots = Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS;

    for (RobotInfo enemy : enemyRobots) {
      if (Clock.getBytecodesLeft() < MicroConstants.MAX_MICRO_BYTECODE_REMAINING) break;
      if (!doesDamage(enemy.type)) continue;
      MapLocation enemyLoc = enemy.location;
      RobotInfo closestAllyToEnemy = null;
      int closestAllyToEnemyDist = Integer.MAX_VALUE;
      for (RobotInfo friend : friendlyRobots) {
        if (!isArmy(friend.type)) continue;
        int dist = friend.location.distanceSquaredTo(enemyLoc);
        if (dist < closestAllyToEnemyDist) {
          closestAllyToEnemyDist = dist;
          closestAllyToEnemy = friend;
        }
      }
      int t = enemy.type.ordinal();
      currentDPS = DPS[t] / ((int) (rc.senseMapInfo(enemy.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM)));
      if (enemy.type == RobotType.HEADQUARTERS) currentDPS = RobotType.HEADQUARTERS.damage;
      if (currentDPS <= 0) continue;
      //if (danger && Robot.comm.isEnemyTerritory(unit.getLocation())) currentDPS*=1.5;
      currentVisionRange = visionRange[t];
      currentActionRadius = enemy.type.actionRadiusSquared;
      microInfo[0].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[1].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[2].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[3].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[4].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[5].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[6].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[7].updateForEnemy(enemy, closestAllyToEnemy);
      microInfo[8].updateForEnemy(enemy, closestAllyToEnemy);
    }

    microInfo[0].finishEnemyUpdates();
    microInfo[1].finishEnemyUpdates();
    microInfo[2].finishEnemyUpdates();
    microInfo[3].finishEnemyUpdates();
    microInfo[4].finishEnemyUpdates();
    microInfo[5].finishEnemyUpdates();
    microInfo[6].finishEnemyUpdates();
    microInfo[7].finishEnemyUpdates();
    microInfo[8].finishEnemyUpdates();

    for (RobotInfo friendly : friendlyRobots) {
      if (Clock.getBytecodesLeft() < MicroConstants.MAX_MICRO_BYTECODE_REMAINING) break;
      currentDPS = DPS[friendly.type.ordinal()] / ((int) (rc.senseMapInfo(friendly.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM)));
//      System.out.println(currentDPS);
      if (currentDPS <= 0) continue;
      microInfo[0].updateForAlly(friendly);
      microInfo[1].updateForAlly(friendly);
      microInfo[2].updateForAlly(friendly);
      microInfo[3].updateForAlly(friendly);
      microInfo[4].updateForAlly(friendly);
      microInfo[5].updateForAlly(friendly);
      microInfo[6].updateForAlly(friendly);
      microInfo[7].updateForAlly(friendly);
      microInfo[8].updateForAlly(friendly);
    }

    String indString = "";
    MicroInfo bestMicro = microInfo[8];
    for (int i = 0; i < 8; ++i) {
//      if (microInfo[i].score() > -1000)  indString += microInfo[i].dir + "," + (int) microInfo[i].score() +" ||";
      if(microInfo[i].canMove) indString += microInfo[i].dir;
      indString += microInfo[i].canMove;
      if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
    }

//    if (bestMicro.dir == Direction.CENTER) return true;

    indString = "best: " + bestMicro.dir + "," + (int) bestMicro.score() + " ||" + indString;
    Printer.appendToIndicator(indString);
//    return bestMicro.dir == Direction.CENTER || pathing.move(bestMicro.dir);
    pathing.move(bestMicro.dir);
    return true;
//      if (rc.canMove(bestMicro.dir)) {
//        rc.move(bestMicro.dir);
//        return true;
//      }

  }
  private static boolean doesDamage(RobotType type) throws GameActionException {
    return type == RobotType.LAUNCHER || type == RobotType.HEADQUARTERS;
  }
  private static boolean isArmy(RobotType type) throws GameActionException {
    switch(type) {
      case LAUNCHER:
      case DESTABILIZER:
      case BOOSTER:
        return true;
      default:
        return false;
    }
  }

  static class MicroInfo {

    Direction dir;
    MapLocation location;
    int distToClosestEnemy;
    MapLocation closestEnemyLocation = null;
    double netOutgoingDPS = 0; // +is good, - is bad
    double enemiesTargeting = 0;
    double alliesTargeting = 0;
    boolean canMove = true;
    double cooldownMultiplier = 1;
    private int actionCooldown;
    private boolean haveAttacked;

    public MicroInfo(Direction dir) throws GameActionException {
      this.dir = dir;
      this.haveAttacked = !canAttack;
      this.location = rc.getLocation().add(dir);
      if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
      else {
        // debug
//        System.out.println(rc.canSenseLocation(this.location) ? rc.senseMapInfo(this.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM): 1);
        cooldownMultiplier = rc.canSenseLocation(this.location) ? rc.senseMapInfo(this.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) : 1;
        this.actionCooldown = (int) (Cache.Permanent.ROBOT_TYPE.actionCooldown * cooldownMultiplier);

//        if (!hurt) {
//          this.netOutgoingDPS += myDPS / actionCooldown;
//          this.alliesTargeting += myDPS / actionCooldown;
//          distToClosestEnemy = rangeExtended[RobotType.LAUNCHER.ordinal()];
//        } else {
          distToClosestEnemy = Integer.MAX_VALUE;
//        }
      }
    }

    /**
     * does stuff at the end to finish processing enemies based on closest
     */
    void finishEnemyUpdates() {
      if (distToClosestEnemy <= myVisionRange) {
        alliesTargeting += myDPS; // we can also see them and won't forget about them. If we have memory of enemies, we can remove this line.
      }
    }

    /**
     *
     * @param enemy the enemy. THIS CAN BE HQ
     * @param closestAllyToEnemy the closest ally (NOT SELF) to the enemy. NULLABLE
     */
    void updateForEnemy(RobotInfo enemy, RobotInfo closestAllyToEnemy) {
      if (!canMove) return;
      boolean canAttackAlly = false;
      if (closestAllyToEnemy != null
          && closestAllyToEnemy.location.distanceSquaredTo(enemy.location) <= currentActionRadius) {
        canAttackAlly = true;
      }
      int dist = enemy.location.distanceSquaredTo(location);
      if (dist < distToClosestEnemy && enemy.type != RobotType.HEADQUARTERS) {
        distToClosestEnemy = dist;
        closestEnemyLocation = enemy.location;
      }
      if (dist <= currentActionRadius && !haveAttacked) {
        haveAttacked = true;
        netOutgoingDPS += myDPS / actionCooldown;
      }
//      if (dist <= currentActionRadius) {
      if (dist <= currentVisionRange) {
        netOutgoingDPS -= currentDPS;
        // we can also attack them

      } else if (canAttackAlly) {
        netOutgoingDPS -= currentDPS;
      }

      if (dist <= currentVisionRange) enemiesTargeting += currentDPS;
    }

    void updateForAlly(RobotInfo ally) {
      if (!canMove) return;
//      alliesTargeting += currentDPS;
      if (closestEnemyLocation == null) return;
      int dist = ally.location.distanceSquaredTo(closestEnemyLocation);
//      if (dist <= currentActionRadius) netOutgoingDPS += currentDPS; // cuz they hit each other and then move outta range
      if (dist <= currentVisionRange) netOutgoingDPS += currentDPS;

      if (dist <= currentVisionRange) alliesTargeting += currentDPS;
    }


    /**
     * Score: higher is better.
     * @return
     */
    double score() {
      if (!canMove) return -1 * Double.MAX_VALUE;

      double score = netOutgoingDPS;
      if (bigWinning() && !haveAttacked) score -= distToClosestEnemy;
//      if (bigLosing()) {
//        if (distToClosestEnemy > RobotType.LAUNCHER.visionRadiusSquared) {
//          score += 100;
//
//        }
//        score += 5 * distToClosestEnemy;
//        // incentivize moving towards closest ally HQ
//
//        int distToAllyHq = HqMetaInfo.getClosestHqLocation(location).distanceSquaredTo(location);
//        score -= (0.01) * distToAllyHq;
//      }
      return score;
//      if (enemiesTargeting > alliesTargeting) return 1;
//      return 2;
    }
    boolean bigWinning() {
      return totalAllyArmyHealth > MicroConstants.BIG_WIN_HEALTH_MULTIPLIER * totalEnemyArmyHealth;
    }
    boolean bigLosing() {
      return totalAllyArmyHealth < MicroConstants.BIG_LOSE_HEALTH_MULTIPLIER * totalEnemyArmyHealth;
    }

    boolean inRange() {
//      if (shouldStayInRange) return true;
//      return distToClosestEnemy <= myActionRange;
      // I don't understand the logic here, so commenting out
      return shouldStayInRange; // currently, this is always false.
    }

    //equal => true
    boolean isBetter(MicroInfo otherMicro) {

      if (score() > otherMicro.score()) return true;
      if (score() < otherMicro.score()) return false;

//      if (inRange() && !otherMicro.inRange()) return true;
//      if (!inRange() && otherMicro.inRange()) return false;
      double thisTargetingDiff = this.alliesTargeting - this.enemiesTargeting;
      double otherTargetingDiff = otherMicro.alliesTargeting - otherMicro.enemiesTargeting;
      if (thisTargetingDiff > otherTargetingDiff) return true;
      if (thisTargetingDiff < otherTargetingDiff) return false;
      if (distToClosestEnemy > otherMicro.distToClosestEnemy) return true;
      if (distToClosestEnemy < otherMicro.distToClosestEnemy) return false;
//      if (enemiesTargeting )
//      if (!severelyHurt) {
        if (alliesTargeting > otherMicro.alliesTargeting) return true;
        if (alliesTargeting < otherMicro.alliesTargeting) return false;
//      }

      if (inRange()) return distToClosestEnemy >= otherMicro.distToClosestEnemy;
      else return distToClosestEnemy <= otherMicro.distToClosestEnemy;
    }
  }

  public static boolean ishurt(int health, int maxHealth) {
    return health < MicroConstants.CRITICAL_HEALTH;
  }
}
