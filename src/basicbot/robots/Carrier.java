package basicbot.robots;

import basicbot.communications.Communicator;
import basicbot.utils.Cache;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

public class Carrier extends Robot {
  private MapLocation targetWell;
  private boolean targetWellUpgraded;
  private MapLocation targetHQ;

  private CarrierRole role;
  private static final CarrierRole DEFAULT_ROLE = CarrierRole.ADAMANTIUM_COLLECTION;
  private int turnsSinceRoleChange;

  private int maxResourceToCarry;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    this.role = DEFAULT_ROLE;
    this.turnsSinceRoleChange = Integer.MAX_VALUE;
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
    while (moveRandomly()) {
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
      while (moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetPosition))) {}
      while (moveRandomly()) {}
      if (rc.canTransferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType))) {
        rc.transferResource(targetHQ, resourceType, rc.getResourceAmount(resourceType));
      }
    } else if (this.targetWell != null) {
      // collect adamantium
      while (collectResource(targetWell, targetWellUpgraded ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE)) {
        if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
          break;
        }
      }
      while (moveInDirRandom(Cache.PerTurn.CURRENT_LOCATION.directionTo(targetWell))) {}
      while (moveRandomly()) {}
      while (collectResource(targetWell, targetWellUpgraded ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE)) {
        if (rc.getResourceAmount(resourceType) >= maxResourceToCarry) {
          break;
        }
      }
    } else {
      while (moveRandomly()) {}
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
    int sharedArrayOffset = Communicator.CLOSEST_ADAMANTIUM_WELL_INFO_START;
    switch (resourceType) {
      case ADAMANTIUM:
        sharedArrayOffset = Communicator.CLOSEST_ADAMANTIUM_WELL_INFO_START;
        break;
      case MANA:
        sharedArrayOffset = Communicator.CLOSEST_MANA_WELL_INFO_START;
        break;
    }
    int hqWithClosestWell = 0;
    boolean closestUpgraded = false;
    int closestWellDistance = 0b111;
    for (int i = 0; i < 4; i++) {
      int data = rc.readSharedArray(i + sharedArrayOffset);
      int dist = data & 0b111;
//      Printer.print("READ(" + (i+sharedArrayOffset) + "): dist=" + dist);
      if (dist >= closestWellDistance) continue;
      closestWellDistance = dist;
      hqWithClosestWell = i;
      closestUpgraded = (data & 0b1000) != 0;
    }
    targetHQ = communicator.readLocationTopBits(hqWithClosestWell);
    if (closestWellDistance == 0b111) {
      targetWell = null;
      if (resourceType == ResourceType.MANA) {
//      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
        hqWithClosestWell++;
        hqWithClosestWell %= rc.readSharedArray(Communicator.NUM_HQS);
        targetHQ = communicator.readLocationTopBits(hqWithClosestWell);
//      Printer.print("MANA: " + targetWell + " " + targetWellUpgraded + " " + targetHQ);
      }
    } else {
      targetWell = communicator.readLocationTopBits(hqWithClosestWell + sharedArrayOffset);
      targetWellUpgraded = closestUpgraded;
    }
  }

  private RobotInfo shouldAttackEnemy() throws GameActionException {
    // if enemy launcher, consider attacking 1) closest
    RobotInfo bestEnemyToAttack = null;
    int bestValue = 0;

    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      RobotType type = enemyRobot.getType();
      int costToBuild = type.buildCostAdamantium + type.buildCostMana + type.buildCostElixir;
      int carryingResourceValue = enemyRobot.inventory.getWeight();
      int enemyValue = costToBuild + carryingResourceValue;
      //todo: maybe make if-statement always true for launchers depending on if we have launchers or not
      if (enemyRobot.health / GameConstants.CARRIER_DAMAGE_FACTOR <= enemyValue || type == RobotType.LAUNCHER) { // it is worth attacking this enemy;
        // determine if we have enough to attack it...
        int totalDmg = 0;
        RobotInfo[] robotInfos = rc.senseNearbyRobots(enemyRobot.location, -1, Cache.Permanent.OUR_TEAM); //assume this returns this robot as well
        for (RobotInfo friendlyRobot : robotInfos) {
          //todo: maybe dont consider launchers in dmg calculation here
          totalDmg += (friendlyRobot.inventory.getWeight() * GameConstants.CARRIER_DAMAGE_FACTOR) + friendlyRobot.type.damage;
        }
        Printer.print("enemy location: " + enemyRobot.location + " can deal: " + totalDmg);
        // todo: consider allowing only launcher to attack or smth?
        if (totalDmg > enemyRobot.health) { // we can kill it
          if (bestEnemyToAttack == null || enemyValue > bestValue || (enemyValue == bestValue && bestEnemyToAttack.health < enemyRobot.health)) {
            bestEnemyToAttack = enemyRobot;
            bestValue = enemyValue;
          }
        }
      }
    }
    return bestEnemyToAttack;
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
