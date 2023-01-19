package launchermicroxsquare.robots.micro;

import launchermicroxsquare.communications.HqMetaInfo;
import launchermicroxsquare.robots.pathfinding.Pathing;
import launchermicroxsquare.utils.Cache;
import launchermicroxsquare.utils.Utils;
import battlecode.common.*;

public class AttackerMovementMicro {

  public static final int CRITICAL_HEALTH = 10;
  private static final int MAX_MICRO_BYTECODE_REMAINING = 2000;
  public static final int ATTACK_TURN = 250;

  public static RobotController rc;
  public static Pathing pathing;


  static final int INF = 1000000;
  static boolean attacker = false;
  static boolean shouldPlaySafe = false;
  static boolean alwaysInRange = false;
  static boolean hurt = false; //TODO: if hurt we want to go back to archon
  static int myRange;
  static int myVisionRange;
  static double myDPS;
  static boolean severelyHurt = false;

  static double[] DPS = new double[]{0, 0, 0, 0, 0, 0, 0};
  static int[] rangeExtended = new int[]{0, 0, 0, 0, 0, 0, 0};


  static double currentDPS = 0;
  static double currentRangeExtended;
  static double currentActionRadius;
  static boolean canAttack;

  final static double MAX_COOLDOWN_DIFF = 1.2; // TODO: i have no idea what this should be


  public static void init(RobotController rc, Pathing pathing) {
    AttackerMovementMicro.rc = rc;
    AttackerMovementMicro.pathing = pathing;
    switch (Cache.Permanent.ROBOT_TYPE) {
      case LAUNCHER:
        attacker = true;
        break;
    }
    myRange = Cache.Permanent.ROBOT_TYPE.actionRadiusSquared;
    myVisionRange = Cache.Permanent.ROBOT_TYPE.visionRadiusSquared;

    DPS[RobotType.LAUNCHER.ordinal()] = RobotType.LAUNCHER.damage * (4.0 * GameConstants.COOLDOWNS_PER_TURN / RobotType.LAUNCHER.actionCooldown);
//    DPS[RobotType.SAGE.ordinal()] = 9;
    rangeExtended[RobotType.LAUNCHER.ordinal()] = RobotType.LAUNCHER.visionRadiusSquared;
//    rangeExtended[RobotType.SAGE.ordinal()] = 34;
    myDPS = DPS[Cache.Permanent.ROBOT_TYPE.ordinal()];
  }


  /**
   * will do micro movements to optimally fight enemies
   * @return true if micro was done, false if the robot can do whatever it wants
   * @throws GameActionException any issues with sensing/moving/attacking
   */
  public static boolean doMicro() throws GameActionException {
    if (!rc.isMovementReady()) return false;
    shouldPlaySafe = false;
    severelyHurt = ishurt(Cache.PerTurn.HEALTH, Cache.Permanent.MAX_HEALTH);
    RobotInfo[] enemyRobots = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    if (enemyRobots.length == 0) return false;
    canAttack = rc.isActionReady();

    int uIndex = enemyRobots.length;
    while (uIndex-- > 0){
      RobotInfo r = enemyRobots[uIndex];
      switch(r.type){
        case LAUNCHER:
        case DESTABILIZER:
          shouldPlaySafe = true;
          break;
        default:
          break;
      }
    }
    if (!shouldPlaySafe) return false;

    alwaysInRange = false;
    if (!rc.isActionReady()) alwaysInRange = true;
    if (severelyHurt) alwaysInRange = true;

    MicroInfo[] microInfo = new MicroInfo[9];
    for (int i = 0; i < 9; ++i) microInfo[i] = new MicroInfo(Utils.directionsNine[i]);

    double minCooldown = microInfo[8].cooldownMultiplier;
    if (microInfo[7].canMove && minCooldown > microInfo[7].cooldownMultiplier) minCooldown = microInfo[7].cooldownMultiplier;
    if (microInfo[6].canMove && minCooldown > microInfo[6].cooldownMultiplier) minCooldown = microInfo[6].cooldownMultiplier;
    if (microInfo[5].canMove && minCooldown > microInfo[5].cooldownMultiplier) minCooldown = microInfo[5].cooldownMultiplier;
    if (microInfo[4].canMove && minCooldown > microInfo[4].cooldownMultiplier) minCooldown = microInfo[4].cooldownMultiplier;
    if (microInfo[3].canMove && minCooldown > microInfo[3].cooldownMultiplier) minCooldown = microInfo[3].cooldownMultiplier;
    if (microInfo[2].canMove && minCooldown > microInfo[2].cooldownMultiplier) minCooldown = microInfo[2].cooldownMultiplier;
    if (microInfo[1].canMove && minCooldown > microInfo[1].cooldownMultiplier) minCooldown = microInfo[1].cooldownMultiplier;
    if (microInfo[0].canMove && minCooldown > microInfo[0].cooldownMultiplier) minCooldown = microInfo[0].cooldownMultiplier;

    minCooldown *= MAX_COOLDOWN_DIFF;

    if (microInfo[8].cooldownMultiplier > minCooldown) microInfo[8].canMove = false;
    if (microInfo[7].cooldownMultiplier > minCooldown) microInfo[7].canMove = false;
    if (microInfo[6].cooldownMultiplier > minCooldown) microInfo[6].canMove = false;
    if (microInfo[5].cooldownMultiplier > minCooldown) microInfo[5].canMove = false;
    if (microInfo[4].cooldownMultiplier > minCooldown) microInfo[4].canMove = false;
    if (microInfo[3].cooldownMultiplier > minCooldown) microInfo[3].canMove = false;
    if (microInfo[2].cooldownMultiplier > minCooldown) microInfo[2].canMove = false;
    if (microInfo[1].cooldownMultiplier > minCooldown) microInfo[1].canMove = false;
    if (microInfo[0].cooldownMultiplier > minCooldown) microInfo[0].canMove = false;

    boolean danger = false;
    if (Cache.PerTurn.ROUND_NUM <= ATTACK_TURN) {
      MapLocation closestEnemyHQ = HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION);
      MapLocation closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
      if (closestEnemyHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < closestHQ.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION)) {
        danger = true;
      }
    }

    for (RobotInfo enemy : enemyRobots) {
      if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
      int t = enemy.type.ordinal();
      currentDPS = DPS[t] / ((int) (rc.senseMapInfo(enemy.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * enemy.type.actionCooldown));
      if (currentDPS <= 0) continue;
      //if (danger && Robot.comm.isEnemyTerritory(unit.getLocation())) currentDPS*=1.5;
      currentRangeExtended = rangeExtended[t];
      currentActionRadius = enemy.type.actionRadiusSquared;
      microInfo[0].updateEnemy(enemy);
      microInfo[1].updateEnemy(enemy);
      microInfo[2].updateEnemy(enemy);
      microInfo[3].updateEnemy(enemy);
      microInfo[4].updateEnemy(enemy);
      microInfo[5].updateEnemy(enemy);
      microInfo[6].updateEnemy(enemy);
      microInfo[7].updateEnemy(enemy);
      microInfo[8].updateEnemy(enemy);
    }

    RobotInfo[] friendlyRobots = Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS;
    for (RobotInfo friendly : friendlyRobots) {
      if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
      currentDPS = DPS[friendly.type.ordinal()] / ((int) (rc.senseMapInfo(friendly.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * friendly.type.actionCooldown));
      if (currentDPS <= 0) continue;
      microInfo[0].updateAlly(friendly);
      microInfo[1].updateAlly(friendly);
      microInfo[2].updateAlly(friendly);
      microInfo[3].updateAlly(friendly);
      microInfo[4].updateAlly(friendly);
      microInfo[5].updateAlly(friendly);
      microInfo[6].updateAlly(friendly);
      microInfo[7].updateAlly(friendly);
      microInfo[8].updateAlly(friendly);
    }

    MicroInfo bestMicro = microInfo[8];
    for (int i = 0; i < 8; ++i) {
      if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
    }

//    if (bestMicro.dir == Direction.CENTER) return true;

    return bestMicro.dir == Direction.CENTER || pathing.move(bestMicro.dir);
//      if (rc.canMove(bestMicro.dir)) {
//        rc.move(bestMicro.dir);
//        return true;
//      }

  }

  static class MicroInfo {

    Direction dir;
    MapLocation location;
    int minDistanceToEnemy = INF;
    MapLocation closestEnemyLocation = null;
    double DPSreceived = 0;
    double enemiesTargeting = 0;
    double alliesTargeting = 0;
    boolean canMove = true;
    double cooldownMultiplier = 0;
    private int actionCooldown;

    public MicroInfo(Direction dir) throws GameActionException {
      this.dir = dir;
      this.location = rc.getLocation().add(dir);
      if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
      else {
        cooldownMultiplier = rc.canSenseLocation(this.location) ? rc.senseMapInfo(this.location).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) : 1;
        actionCooldown = (int) (Cache.Permanent.ROBOT_TYPE.actionCooldown * cooldownMultiplier);
        if (!hurt){
          this.DPSreceived -= myDPS / actionCooldown;
          this.alliesTargeting += myDPS / actionCooldown;
          minDistanceToEnemy = rangeExtended[RobotType.LAUNCHER.ordinal()];
        } else {
          minDistanceToEnemy = INF;
        }
      }
    }

    void updateEnemy(RobotInfo unit){
      if (!canMove) return;
      int dist = unit.location.distanceSquaredTo(location);
      if (dist < minDistanceToEnemy) {
        minDistanceToEnemy = dist;
        closestEnemyLocation = unit.location;
      }
      if (dist <= currentActionRadius) DPSreceived += currentDPS;
      if (dist <= currentRangeExtended) enemiesTargeting += currentDPS;
    }

    void updateAlly(RobotInfo unit){
      if (!canMove) return;
//      alliesTargeting += currentDPS;
      if (closestEnemyLocation == null) return;
      int dist = unit.location.distanceSquaredTo(closestEnemyLocation);
//      if (dist <= currentActionRadius) DPSreceived += currentDPS;
      if (dist <= currentRangeExtended) alliesTargeting += currentDPS;
    }


    int safe(){
      if (!canMove) return -1;
      if (DPSreceived > 0) return 0;
      if (enemiesTargeting > alliesTargeting) return 1;
      return 2;
    }

    boolean inRange(){
      if (alwaysInRange) return true;
      return minDistanceToEnemy <= myRange;
    }

    //equal => true
    boolean isBetter(MicroInfo M){

      if (safe() > M.safe()) return true;
      if (safe() < M.safe()) return false;

      if (inRange() && !M.inRange()) return true;
      if (!inRange() && M.inRange()) return false;

      if (!severelyHurt) {
        if (alliesTargeting > M.alliesTargeting) return true;
        if (alliesTargeting < M.alliesTargeting) return false;
      }

      if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
      else return minDistanceToEnemy <= M.minDistanceToEnemy;
    }
  }

  public static boolean ishurt(int health, int maxHealth) {
    return health < CRITICAL_HEALTH;
  }
}
