package spawnorder.robots.micro;

import spawnorder.communications.Communicator;
import spawnorder.communications.HqMetaInfo;
import spawnorder.utils.Cache;
import spawnorder.utils.Utils;
import battlecode.common.*;

public class AttackMicro {
  public static RobotController rc;
  private static boolean chickenBehavior;

  public static void init(RobotController rc) {
    AttackMicro.rc = rc;
  }

  public static void checkChickenBehavior(){
    if (!chickenBehavior && AttackerMovementMicro.ishurt(Cache.PerTurn.HEALTH, Cache.Permanent.MAX_HEALTH)) chickenBehavior = true;
    if (chickenBehavior && Cache.PerTurn.HEALTH >= Cache.Permanent.MAX_HEALTH) chickenBehavior = false;
  }

  public static MapLocation getBestTarget() throws GameActionException {
    MapLocation bestAttackingTarget = getBestAttackingTarget();
//    if (chickenBehavior) {
//      MapLocation ans = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
//      if (ans != null) {
//        if (bestAttackingTarget != null && !AttackerMovementMicro.ishurt(Cache.PerTurn.HEALTH, Cache.Permanent.MAX_HEALTH)){
//          if (bestAttackingTarget.distanceSquaredTo(ans) <= 53){
//            return bestAttackingTarget;
//          }
//        }
//        int d = ans.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
//        if (d <= 13) return Cache.PerTurn.CURRENT_LOCATION;
//        return ans;
//      }
//    }
    return bestAttackingTarget;
  }

  static MapLocation getBestAttackingTarget() throws GameActionException {
    MoveTarget bestTarget = null;
    RobotInfo[] enemies = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    for (RobotInfo enemy : enemies){
      if (HqMetaInfo.isEnemyTerritory(enemy.location) && enemy.type != RobotType.CARRIER) continue;
      MoveTarget mt = new MoveTarget(enemy);
      if (mt.isBetterThan(bestTarget)) bestTarget = mt;
    }
    if (bestTarget != null) {
      MapLocation ans = bestAim(bestTarget.mloc);
      if (ans.distanceSquaredTo(bestTarget.mloc) <= Cache.Permanent.ACTION_RADIUS_SQUARED) return ans;
      return bestTarget.mloc;
    }
    return Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
  }

  static MapLocation bestAim(MapLocation target) throws GameActionException {
    rc.setIndicatorDot(target, 0, 255, 0);
    AimTarget[] aims = new AimTarget[9];
    for (Direction dir : Utils.directionsNine) aims[dir.ordinal()] = new AimTarget(dir, target);
    double minCooldownMultiplier = aims[8].cooldownMultiplier;
    for (int i = 8; i-- > 0; ){
      if (aims[i].canMove && aims[i].cooldownMultiplier < minCooldownMultiplier) minCooldownMultiplier = aims[i].cooldownMultiplier;
    }
    minCooldownMultiplier *= AttackerMovementMicro.MAX_COOLDOWN_DIFF;
    for (int i = 8; i-- > 0; ){
      if (aims[i].cooldownMultiplier > minCooldownMultiplier) aims[i].canMove = false;
    }
    AimTarget bestTarget = aims[8];
    for (int i = 8; i-- > 0; ){
      if (aims[i].isBetterThan(bestTarget)){
        bestTarget = aims[i];
      }
    }
    return bestTarget.loc;

  }

  public static boolean isAttacker(RobotType type) {
    return type == RobotType.LAUNCHER || type == RobotType.DESTABILIZER;
  }

  public static class AttackTarget {
    RobotType type;
    int health;
    boolean attacker = false;
    public MapLocation mloc;

    public boolean isBetterThan(AttackTarget t){
      if (t == null) return true;
      if (attacker & !t.attacker) return true;
      if (!attacker & t.attacker) return false;
      return health <= t.health;
    }

    public AttackTarget(RobotInfo r){
      type = r.type;
      health = r.getHealth();
      mloc = r.getLocation();
      switch(type){
        case LAUNCHER:
        case DESTABILIZER:
          attacker = true;
        default:
          break;
      }
    }
  }

  static class MoveTarget {
    RobotType type;
    int health;
    int priority;
    MapLocation mloc;

    boolean isBetterThan(MoveTarget t) {
      if (t == null) return true;
      if (priority > t.priority) return true;
      if (priority < t.priority) return true;
      return health <= t.health;
    }

    MoveTarget(RobotInfo r) {
      this.type = r.type;
      this.health = r.health;
      this.mloc = r.location;
      switch (type){
        case HEADQUARTERS:
          priority = 0;
          break;
        case AMPLIFIER:
          priority = 1;
          break;
        case CARRIER:
          priority = 2;
          break;
        case BOOSTER:
          priority = 3;
          break;
        case LAUNCHER:
          priority = 4;
          break;
        case DESTABILIZER:
          priority = 5;
          break;
      }
    }
  }

  static class AimTarget {
    MapLocation loc;
    int dist;
    double cooldownMultiplier;
    boolean canMove = true;

    AimTarget(Direction dir, MapLocation target) throws GameActionException {
      if (dir != Direction.CENTER && !rc.canMove(dir)){
        canMove = false;
        return;
      }
      this.loc = rc.adjacentLocation(dir);
      dist = loc.distanceSquaredTo(target);
      cooldownMultiplier = rc.canSenseLocation(loc) ? rc.senseMapInfo(loc).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) : 1;
    }

    boolean isBetterThan(AimTarget at){
      if (!canMove) return false;
      if (!at.canMove) return true;

      return dist < at.dist;
    }
  }
}
