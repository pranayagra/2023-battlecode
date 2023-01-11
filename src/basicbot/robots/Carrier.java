package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.containers.HashMap;
import basicbot.utils.Cache;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

public class Carrier extends Robot {
  private MapLocation targetWell;
  private boolean targetWellUpgraded;
  private MapLocation targetHQ;

  HashMap<MapLocation, Direction> wellApproachDirection;

  private CarrierRole role;
  private static final CarrierRole DEFAULT_ROLE = CarrierRole.ADAMANTIUM_COLLECTION;
  private int turnsSinceRoleChange;

  private int maxResourceToCarry;

  int fleeingCounter;
  MapLocation lastEnemyLocation;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    this.role = DEFAULT_ROLE;
    this.turnsSinceRoleChange = Integer.MAX_VALUE;
    wellApproachDirection = new HashMap<>(3);
    determineRole();
    switch (this.role) {
      case ADAMANTIUM_COLLECTION:
        initAdamantiumCollection();
        break;
      case MANA_COLLECTION:
        initManaCollection();
        break;
    }
  }

  @Override
  protected void runTurn() throws GameActionException {
//    if (Cache.PerTurn.ROUND_NUM >= 200) rc.resign();
    if (enemyExists()) {
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
            pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(enemyToAttack.location));
//            Printer.print("moved to attack... " + rc.canAttack(enemyToAttack.location), "loc=" + enemyToAttack.location, "canAct=" + rc.canActLocation(enemyToAttack.location), "robInfo=" + rc.senseRobotAtLocation(enemyToAttack.location));
            if (rc.canAttack(enemyToAttack.location)) {
              rc.attack(enemyToAttack.location);
            }
          }
        }
        updateLastEnemy();
    }

    if (fleeingCounter > 0) {
      // run from lastEnemyLocation
      Direction away = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation).opposite();
      MapLocation fleeDirection = Cache.PerTurn.CURRENT_LOCATION.add(away).add(away).add(away).add(away).add(away);
      // todo: move towards fleeDirection
      pathing.moveInDirLoose(away);
      fleeingCounter--;
    }

    switch (this.role) {
      case ADAMANTIUM_COLLECTION:
      case MANA_COLLECTION:
        if (this.targetWell == null) searchForTargetWell();
        if (this.targetWell != null) runCollection(role.getResourceCollectionType());
        break;
      default:
        determineRole();
    }
    if (turnCount % 10 == 9) {
      determineRole();
    }
    turnsSinceRoleChange++;
  }

  private void determineRole() throws GameActionException {
//    this.role = CarrierRole.ADAMANTIUM_COLLECTION;
    int numLocalCarriers = 0;
    for (RobotInfo friend : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      if (friend.type == RobotType.CARRIER) {
        numLocalCarriers++;
      }
    }
    if (numLocalCarriers > 10 && turnsSinceRoleChange >= 100) {
      this.role = this.role.toggleCollectionType();
    }
  }

  private boolean searchForTargetWell() throws GameActionException {
    while (pathing.moveRandomly()) {
      WellInfo well = getClosestWell(role.getResourceCollectionType());
      if (well != null) {
        rc.setIndicatorString("found well: " + well);
        this.targetWell = well.getMapLocation();
        this.targetWellUpgraded = well.isUpgraded();
        return true;
      }
    }
    return false;
  }

  private void initAdamantiumCollection() throws GameActionException {
    determineTargetWell(ResourceType.ADAMANTIUM);
    maxResourceToCarry = GameConstants.CARRIER_CAPACITY;
  }

  private void initManaCollection() throws GameActionException {
    determineTargetWell(ResourceType.MANA);
    maxResourceToCarry = GameConstants.CARRIER_CAPACITY;
  }

  private void runCollection(ResourceType resourceType) throws GameActionException {
    rc.setIndicatorString("COL " + resourceType + ": " + targetWell + "->" + targetHQ);
    if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
      if (rc.canTransferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType))) {
        rc.transferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType));
      }
      MapLocation targetPosition = targetHQ;
      if (targetWell != null) {
        targetPosition = targetWell;
        while (!targetPosition.isWithinDistanceSquared(targetHQ, RobotType.CARRIER.actionRadiusSquared)) {
          Direction toHQ = targetPosition.directionTo(targetHQ);
          targetPosition = targetPosition.add(Utils.randomSimilarDirectionPrefer(toHQ));
        }
      }
      while (pathing.moveTowards(targetPosition)) {}
//      while (pathing.moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetPosition))) {}
      while (pathing.moveRandomly()) {}
      if (rc.canTransferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType))) {
        rc.transferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType));
      }
    } else if (this.targetWell != null) {
      // collect resource
      while (collectResource(targetWell, targetWellUpgraded ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE)) {
        if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
          break;
        }
      }
      while (pathing.moveTowards(targetWell)) {}
//      while (pathing.moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell))) {}
      while (pathing.moveRandomly()) {}
      while (collectResource(targetWell, targetWellUpgraded ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE)) {
        if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
          break;
        }
      }
    } else {
      while (pathing.moveRandomly()) {}
      // periodically check comms for new target
    }
  }

  private boolean collectResource(MapLocation well, int amount) throws GameActionException {
    if (rc.canCollectResource(well, amount)) {
      rc.collectResource(well, amount);
      return true;
    }
    return false;
  }

  private void determineTargetWell(ResourceType resourceType) throws GameActionException {
    CommsHandler.ResourceTypeReaderWriter resourceTypeReaderWriter = CommsHandler.ResourceTypeReaderWriter.fromResourceType(resourceType);
    int hqWithClosestWell = 0;
    boolean closestUpgraded = false;
    int closestWellDistance = Integer.MAX_VALUE;
    MapLocation closestWellLocation = null;
    MapLocation tmpLocation;
    for (int i = 0; i < 4; i++) {
      if (!resourceTypeReaderWriter.readWellExists(i)) continue;

      tmpLocation = resourceTypeReaderWriter.readWellLocation(i);
      int dist = tmpLocation.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION);
      if (dist >= closestWellDistance) continue;

      closestWellDistance = dist;
      hqWithClosestWell = i;
      closestWellLocation = tmpLocation;
      closestUpgraded = resourceTypeReaderWriter.readWellUpgraded(i);
    }
    targetHQ = communicator.commsHandler.readOurHqLocation(hqWithClosestWell);
    if (closestWellLocation == null) {
      targetWell = null;
      if (resourceType == ResourceType.MANA) {
//      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
        hqWithClosestWell++;
        hqWithClosestWell %= rc.readSharedArray(communicator.metaInfo.hqInfo.hqCount);
        targetHQ = communicator.commsHandler.readOurHqLocation(hqWithClosestWell);
//      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
      }
    } else {
      targetWell = closestWellLocation;
      targetWellUpgraded = closestUpgraded;
    }
  }

  /*
  CARRIER BEHAVIOR AGAINST OPPONENT
  1) if we can kill it effectively, kill it
  2) if we cannot kill it
    2.1) attempt to run away. if we are going die (or maybe easy health is <= 7)
  * */

  private boolean enemyExists() throws GameActionException {
    return Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS.length > 0;
  }

  private int getInvWeight(RobotInfo ri) {
    return (ri.getResourceAmount(ResourceType.ADAMANTIUM) + ri.getResourceAmount(ResourceType.MANA) + ri.getResourceAmount(ResourceType.ELIXIR) + (ri.getTotalAnchors() == 0 ? 0 : 40));
  }

  private RobotInfo enemyToAttackIfWorth() throws GameActionException {
//    Printer.print("enemyToAttackIfWorth()");
    int myInvSize = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) + (rc.getAnchor() != null ? 40 : 0));
    if (myInvSize <= 4) {
//      Printer.print("null bc invSize <= 4");
      return null;
    }

    // if enemy launcher, consider attacking 1) closest
    RobotInfo bestEnemyToAttack = null;
    int bestValue = 0;

    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      RobotType type = enemyRobot.getType();
      int costToBuild = type.buildCostAdamantium + type.buildCostMana + type.buildCostElixir;
      int carryingResourceValue = getInvWeight(enemyRobot);
      int enemyValue = costToBuild + carryingResourceValue;
      //todo: maybe make if-statement always true for launchers depending on if we have launchers or not
      if (enemyRobot.health / GameConstants.CARRIER_DAMAGE_FACTOR <= enemyValue || type == RobotType.LAUNCHER) { // it is worth attacking this enemy;
        // determine if we have enough to attack it...
        int totalDmg = 0;
        totalDmg += myInvSize * GameConstants.CARRIER_DAMAGE_FACTOR;
        RobotInfo[] robotInfos = rc.senseNearbyRobots(enemyRobot.location, -1, Cache.Permanent.OUR_TEAM); //assume this returns this robot as well
        for (RobotInfo friendlyRobot : robotInfos) {
          //todo: maybe dont consider launchers in dmg calculation here
          totalDmg += (getInvWeight(friendlyRobot) * GameConstants.CARRIER_DAMAGE_FACTOR) + friendlyRobot.type.damage;
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

  private int numMoves() {
    int moves = 0;
    int cd = rc.getMovementCooldownTurns();
    while (cd < 10 && moves < 5) {
      cd += rc.getType().movementCooldown;
      ++moves;
    }
    return moves;
  }

  private RobotInfo attackEnemyIfCannotRun() {
//    Printer.print("attackEnemyIfCannotRun()");
    int myInvSize = (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) + (rc.getAnchor() != null ? 40 : 0));
    if (myInvSize <= 4) {
//      Printer.print("null bc invSize <= 4");
      return null;
    }

    // sum damage based on how many enemies can currently attack me (this bot must be within action radius of enemy's bot)
    int enemyDamage = 0;
    RobotInfo enemyToAttack = null;
//    int numMoves = numMoves();
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (rc.getLocation().isWithinDistanceSquared(enemyRobot.location, enemyRobot.type.actionRadiusSquared)) {
        enemyDamage += getInvWeight(enemyRobot) * GameConstants.CARRIER_DAMAGE_FACTOR + enemyRobot.type.damage;
      }
      if (rc.getLocation().isWithinDistanceSquared(enemyRobot.location, rc.getType().actionRadiusSquared)) { // todo: need to consider movement here
        if (enemyToAttack == null || (enemyToAttack.type != RobotType.LAUNCHER && enemyRobot.type == RobotType.LAUNCHER))
        enemyToAttack = enemyRobot;
      }
    }

    if (enemyToAttack == null) {
//      Printer.print("enemyToAttack=null" + ", enemies can deal: " + enemyDamage);
    } else {
//      Printer.print("enemyToAttack loc:" + enemyToAttack.location + ", enemies can deal: " + enemyDamage);
    }

    if (enemyDamage > rc.getHealth() - 1) {
//      Printer.print("attack bc no option!!");
      return enemyToAttack;
    }
//    Printer.print("I not die, return null");
    return null;
  }

  private void updateLastEnemy() {
    // get nearest enemy
    // set fleeing to 6
    // todo: consider whether or not to run away from enemy carriers
    MapLocation nearestCombatEnemy = null;
    int distanceToEnemy = Integer.MAX_VALUE;
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemyRobot.type == RobotType.LAUNCHER) {
        int dist = rc.getLocation().distanceSquaredTo(enemyRobot.location);
        if (dist < distanceToEnemy) {
          nearestCombatEnemy = enemyRobot.location;
          distanceToEnemy = dist;
        }
      }
    }
//    Printer.print("updateLastEnemy()");
    if (nearestCombatEnemy != null) {
//      Printer.print("lastEnemyLocation=" + lastEnemyLocation);
      lastEnemyLocation = nearestCombatEnemy;
      fleeingCounter = 6;
    } else {
      fleeingCounter = 0;
    }
  }

  private enum CarrierRole {
    ADAMANTIUM_COLLECTION,
    MANA_COLLECTION;

    CarrierRole toggleCollectionType() {
      switch (this) {
        case ADAMANTIUM_COLLECTION: return MANA_COLLECTION;
        case MANA_COLLECTION: return ADAMANTIUM_COLLECTION;
      }
      return null;
    }

    public ResourceType getResourceCollectionType() {
      switch (this) {
        case ADAMANTIUM_COLLECTION: return ResourceType.ADAMANTIUM;
        case MANA_COLLECTION: return ResourceType.MANA;
      }
      return null;
    }
  }
}
