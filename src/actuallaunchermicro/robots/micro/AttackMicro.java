package actuallaunchermicro.robots.micro;

import actuallaunchermicro.communications.Communicator;
import actuallaunchermicro.communications.HqMetaInfo;
import actuallaunchermicro.utils.Cache;
import actuallaunchermicro.utils.Utils;
import battlecode.common.*;

public class AttackMicro {
  public static RobotController rc;

  public static void init(RobotController rc) {
    AttackMicro.rc = rc;
  }

  public static MapLocation getBestAttackTarget(boolean onlyAttackers) throws GameActionException {
    RobotInfo[] enemies = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    AttackCandidate bestTarget = null;
    for (RobotInfo enemy : enemies) {
      if (onlyAttackers && !AttackMicro.isAttacker(enemy.getType())) continue;
      if (rc.canAttack(enemy.location)) {
        AttackCandidate at = new AttackCandidate(enemy);
        if (at.isBetterThan(bestTarget)) bestTarget = at;
      }
    }
    if (bestTarget == null) return null;
    return bestTarget.mloc;
  }

  public static MapLocation getBestMovementPosition() throws GameActionException {
    return getBestMovementForAttacking();
  }

  static MapLocation getBestMovementForAttacking() throws GameActionException {
    EnemyTargetForMovement bestTarget = null;
    RobotInfo[] enemies = Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS;
    for (RobotInfo enemy : enemies){
      switch (enemy.type) {
        case HEADQUARTERS:
          continue;
        case LAUNCHER:
        case DESTABILIZER:
          if (Cache.PerTurn.ROUND_NUM <= MicroConstants.ATTACK_TURN && HqMetaInfo.isEnemyTerritory(enemy.location)) continue;
        default:
          EnemyTargetForMovement mt = new EnemyTargetForMovement(enemy);
          if (mt.isBetterThan(bestTarget)) bestTarget = mt;
      }
    }
    MapLocation bestTargetLocation = bestTarget == null ? null : bestTarget.mloc;
    no_target: if (bestTargetLocation == null) {
      MapLocation closestEnemy = Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
      if (closestEnemy == null) break no_target;
      MapLocation towardsEnemy = rc.adjacentLocation(Cache.PerTurn.CURRENT_LOCATION.directionTo(closestEnemy));
      if (towardsEnemy.isWithinDistanceSquared(closestEnemy, Cache.Permanent.VISION_RADIUS_SQUARED)) {
        bestTargetLocation = closestEnemy;
      }
    }
    if (bestTargetLocation != null) {
      MapLocation aimPosition = bestPositionToAimFrom(bestTargetLocation);
      if (aimPosition.isWithinDistanceSquared(bestTargetLocation, Cache.Permanent.ACTION_RADIUS_SQUARED)) return aimPosition;
      return bestTargetLocation;
    }
    return null;//Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
  }

  static MapLocation bestPositionToAimFrom(MapLocation target) throws GameActionException {
    rc.setIndicatorDot(target, 0, 255, 0);
    AimingPosition[] aims = new AimingPosition[9];
    for (Direction dir : Utils.directionsNine) aims[dir.ordinal()] = new AimingPosition(dir, target);
    double minCooldownMultiplier = aims[8].cooldownMultiplier;
    for (int i = 8; i-- > 0; ){
      if (aims[i].canMove && aims[i].cooldownMultiplier < minCooldownMultiplier) minCooldownMultiplier = aims[i].cooldownMultiplier;
    }
    minCooldownMultiplier *= MicroConstants.MAX_COOLDOWN_DIFF;
    for (int i = 8; i-- > 0; ){
      if (aims[i].cooldownMultiplier > minCooldownMultiplier) aims[i].canMove = false;
    }
    AimingPosition bestTarget = aims[8];
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

  public static class AttackCandidate {
    RobotType type;
    int health;
    boolean isAttacker;
    public MapLocation mloc;
    private final int distanceToSelf;

    public boolean isBetterThan(AttackCandidate t){
      if (t == null) return true;
      if (isAttacker != t.isAttacker) return isAttacker;
      if (health != t.health) return health < t.health;
      if (distanceToSelf != t.distanceToSelf) return distanceToSelf < t.distanceToSelf;
      return true;
    }

    public AttackCandidate(RobotInfo r){
      type = r.type;
      health = r.health;
      mloc = r.location;
      isAttacker = isAttacker(type);
      distanceToSelf = mloc.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
    }
  }

  static class EnemyTargetForMovement {
    RobotType type;
    int health;
    int enemyPriority;
    MapLocation mloc;

    boolean isBetterThan(EnemyTargetForMovement t) {
      if (t == null) return true;
      if (enemyPriority > t.enemyPriority) return true;
      if (enemyPriority < t.enemyPriority) return true;
      return health <= t.health;
    }

    EnemyTargetForMovement(RobotInfo r) {
      this.type = r.type;
      this.health = r.health;
      this.mloc = r.location;
      switch (type){
        case HEADQUARTERS:
          enemyPriority = 0;
          break;
        case AMPLIFIER:
          enemyPriority = 1;
          break;
        case CARRIER:
          enemyPriority = 2;
          break;
        case BOOSTER:
          enemyPriority = 3;
          break;
        case LAUNCHER:
          enemyPriority = 4;
          break;
        case DESTABILIZER:
          enemyPriority = 5;
          break;
      }
    }
  }

  static class AimingPosition {
    MapLocation loc;
    int dist;
    double cooldownMultiplier;
    boolean canMove = true;

    AimingPosition(Direction dir, MapLocation target) throws GameActionException {
      if (dir != Direction.CENTER && !rc.canMove(dir)){
        canMove = false;
        return;
      }
      this.loc = rc.adjacentLocation(dir);
      dist = loc.distanceSquaredTo(target);
      cooldownMultiplier = rc.canSenseLocation(loc) ? rc.senseMapInfo(loc).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) : 1;
    }

    boolean isBetterThan(AimingPosition at) throws GameActionException {
      if (canMove != at.canMove) return canMove;
      if (!canMove) return false;

      if (dist != at.dist) return dist < at.dist;
      if (cooldownMultiplier != at.cooldownMultiplier) return cooldownMultiplier < at.cooldownMultiplier;

      MapLocation closestToSelf = Communicator.getClosestEnemy(loc);
      MapLocation closestToAt = Communicator.getClosestEnemy(at.loc);
      if (closestToSelf != null && closestToAt != null){
        int d1 = closestToSelf.distanceSquaredTo(loc);
        int d2 = closestToAt.distanceSquaredTo(at.loc);
        if (d1 != d2) return d1 < d2;
      }
      return loc.equals(Cache.PerTurn.CURRENT_LOCATION);
    }
  }
}
