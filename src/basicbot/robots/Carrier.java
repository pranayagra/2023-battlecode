package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.containers.HashMap;
import basicbot.containers.HashSet;
import basicbot.knowledge.RunningMemory;
import basicbot.robots.micro.CarrierWellPathing;
import basicbot.knowledge.Cache;
import basicbot.robots.pathfinding.BugNav;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

import java.util.Arrays;

public class Carrier extends MobileRobot {

  private static final int MAX_CARRYING_CAPACITY = GameConstants.CARRIER_CAPACITY;
  private static final int FAR_FROM_FULL_CAPACITY = MAX_CARRYING_CAPACITY * 9 / 10;
  private static final int MAX_RSS_TO_ENABLE_SCOUT = 1000;
  private static final int SET_WELL_PATH_DISTANCE = RobotType.CARRIER.actionRadiusSquared;
  private static final int MAX_TURNS_STUCK = 3;
  private static final int MAX_ROUNDS_WAIT_FOR_WELL_PATH = 2;
  private static final int MIN_SPOTS_LEFT_FROM_CARRIERS_FILLING_IN_FRONT = 1;
  private static final int TURNS_TO_FLEE = 4;
  private static final int MAX_SCOUT_TURNS = 50;
  private static final int MAX_TURNS_TO_LOOK_FOR_WELL = 10;
  private static final int MIN_TURN_TO_EXPLORE = 30;

  CarrierTask currentTask;
  CarrierTask forcedNextTask;
  CarrierTask HQAssignedTask;


  private int turnsStuckApproachingWell;
  private final HashMap<MapLocation, Direction> wellApproachDirection;
  private final HashSet<MapLocation> blackListWells;
  private MapLocation[] wellQueueOrder;
  private MapLocation wellEntryPoint;
  private int wellQueueSize;

  int wellQueueTargetIndex;
  private int emptierRobotsSeen;
  private int fullerRobotsSeen;
  private int roundsWaitingForQueueSpot;


  int fleeingCounter;
  MapLocation lastEnemyLocation;
  int lastEnemyLocationRound;
  private RobotInfo cachedLastEnemyForBroadcast;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    wellApproachDirection = new HashMap<>(3);
    blackListWells = new HashSet<>(8);
    wellQueueOrder = null;
    resetTask();
  }

  private void resetTask() throws GameActionException {
    CarrierTask previousTask = currentTask;
    currentTask = null;
    if (previousTask != null) {
      previousTask.onTaskEnd();
    }
    currentTask = forcedNextTask == null ? determineNewTask() : forcedNextTask;
    if (currentTask != null) {
      currentTask.onTaskStart(this);
    }
    forcedNextTask = null;
  }

  private void checkAssignedTask() throws GameActionException {
//    Printer.print("Checking assigned task");
    if (Cache.PerTurn.ROUND_NUM % 2 == 0) {
      for (int i = 0; i < HqMetaInfo.hqCount; ++i) {
        if (CommsHandler.readOurHqEvenSpawnExists(i)) {
          MapLocation hqLocation = CommsHandler.readOurHqEvenSpawnLocation(i);
//          Printer.print("hq location: " + hqLocation, "my location: " + Cache.PerTurn.CURRENT_LOCATION);
          if (Cache.PerTurn.CURRENT_LOCATION.equals(hqLocation)) {
            int instruction = CommsHandler.readOurHqEvenSpawnInstruction(i);
            switch (instruction) {
              case 1:
                HQAssignedTask = CarrierTask.FETCH_MANA;
//                Printer.print("HQ assigned me to fetch mana");
                return;
              case 2:
                HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
//                Printer.print("HQ assigned me to fetch adamantium");
                return;
            }
          }
        }
      }
    } else {
      for (int i = 0; i < HqMetaInfo.hqCount; ++i) {
        if (CommsHandler.readOurHqOddSpawnExists(i)) {
          MapLocation hqLocation = CommsHandler.readOurHqOddSpawnLocation(i);
          if (Cache.PerTurn.CURRENT_LOCATION.equals(hqLocation)) {
            int instruction = CommsHandler.readOurHqOddSpawnInstruction(i);
            switch (instruction) {
              case 1:
                HQAssignedTask = CarrierTask.FETCH_MANA;
//                Printer.print("HQ assigned me to fetch mana");
                return;
              case 2:
                HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
//                Printer.print("HQ assigned me to fetch adamantium");
                return;
            }
          }
        }
      }
    }
  }

  private CarrierTask determineNewTask() throws GameActionException {
    if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
      // todo: read from comms
//      checkAssignedTask();
//      Printer.print("checked assign " + HQAssignedTask);
      if (HQAssignedTask != null) return HQAssignedTask;
    }

    if (rc.getWeight() >= MAX_CARRYING_CAPACITY) {
      return CarrierTask.DELIVER_RSS_HOME;
    }
    MapLocation closestHQLoc = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    int closestHQID = HqMetaInfo.getClosestHQ(Cache.PerTurn.CURRENT_LOCATION);
    RobotInfo closestHQ = rc.canSenseRobotAtLocation(closestHQLoc) ? rc.senseRobotAtLocation(closestHQLoc) : null;
    if (closestHQ != null && closestHQ.getTotalAnchors() > 0) {
      return CarrierTask.ANCHOR_ISLAND;
    }
    // TODO: figure out which resource we should be collecting
    int totalAdamantiumAroundMe = 0;
    int totalManaAroundMe = 0;
    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
      switch (robot.type) {
        case CARRIER:
        case HEADQUARTERS:
          totalAdamantiumAroundMe += robot.getResourceAmount(ResourceType.ADAMANTIUM);
          totalManaAroundMe += robot.getResourceAmount(ResourceType.MANA);
      }
    }
    if (totalAdamantiumAroundMe >= MAX_RSS_TO_ENABLE_SCOUT && totalManaAroundMe >= MAX_RSS_TO_ENABLE_SCOUT) {
      return CarrierTask.SCOUT;
    }
    // RSS AROUND ME APPROACH ========
//    Printer.print("totalAdamantiumAroundMe: " + totalAdamantiumAroundMe);
//    Printer.print("totalManaAroundMe: " + totalManaAroundMe);
//    if (totalManaAroundMe <= 1.75 * totalAdamantiumAroundMe) { // ad < 0.666 * mana
////      Printer.print("Collecting mana");
//      return CarrierTask.FETCH_MANA;
//    }
//    if (totalAdamantiumAroundMe < 0.5 * totalManaAroundMe) {
////      Printer.print("Collecting adamantium");
//      return CarrierTask.FETCH_ADAMANTIUM;
//    }
//    Printer.print("Collecting mana");
//    return CarrierTask.FETCH_MANA;
//     END RSS AROUND ME APPROACH ====


    final int MAX_INCOME = 31;

    if (HQAssignedTask != null) {
      if (rc.canWriteSharedArray(0, 0)) {
        switch (HQAssignedTask) {
          case FETCH_ADAMANTIUM:
            CommsHandler.writeOurHqAdamantiumIncome(closestHQID, Math.min(CommsHandler.readOurHqAdamantiumIncome(closestHQID) + 1, MAX_INCOME));
            break;
          case FETCH_MANA:
            CommsHandler.writeOurHqManaIncome(closestHQID, Math.min(CommsHandler.readOurHqManaIncome(closestHQID) + 1, MAX_INCOME));
            break;
          case FETCH_ELIXIR:
            CommsHandler.writeOurHqElixirIncome(closestHQID, Math.min(CommsHandler.readOurHqElixirIncome(closestHQID) + 1, MAX_INCOME));
            break;
        }
      }
      return HQAssignedTask;
    }


    int adamantiumIncome = CommsHandler.readOurHqAdamantiumIncome(closestHQID);
    int manaIncome = CommsHandler.readOurHqManaIncome(closestHQID);
    int elixirIncome = CommsHandler.readOurHqElixirIncome(closestHQID);

    double manaWeighting = 2;

    if (Cache.Permanent.MAP_AREA > 900) {
      manaWeighting = 1.75;
    }

    if (Cache.PerTurn.ROUND_NUM < 40 && Utils.maxSingleAxisDist(HqMetaInfo.getClosestEnemyHqLocation(Cache.PerTurn.CURRENT_LOCATION), Cache.PerTurn.CURRENT_LOCATION )> 40) {
      manaWeighting = 0;
    }
    if ((double)Cache.PerTurn.HEALTH / Cache.Permanent.MAX_HEALTH < 0.8) {
      manaWeighting *= 2;
    }

    MapLocation closestEnemy = Communicator.getClosestEnemy(Cache.PerTurn.CURRENT_LOCATION);
    if (closestEnemy != null && closestEnemy.distanceSquaredTo(Cache.PerTurn.CURRENT_LOCATION) < 100) {
      manaWeighting *= 20;
    }
    // TODO: check for existence of Elixir wells
    if ((40 * adamantiumIncome) / 100 > 9) {
      if (rc.canWriteSharedArray(0, 0)) {
        CommsHandler.writeOurHqManaIncome(closestHQID, Math.min(manaIncome + 1, MAX_INCOME));
      }
      HQAssignedTask = CarrierTask.FETCH_MANA;
      return CarrierTask.FETCH_MANA;
    }
    if (manaWeighting * adamantiumIncome < manaIncome) { // TODO: add some weighting factor (maybe based on size)
      if (rc.canWriteSharedArray(0, 0)) {
        CommsHandler.writeOurHqAdamantiumIncome(closestHQID, Math.min(adamantiumIncome + 1, MAX_INCOME));
      }
      HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
      return CarrierTask.FETCH_ADAMANTIUM;
    }
    if (rc.canWriteSharedArray(0, 0)) {

      CommsHandler.writeOurHqManaIncome(closestHQID, Math.min(adamantiumIncome + 1, MAX_INCOME));
    }
    HQAssignedTask = CarrierTask.FETCH_MANA;
    return CarrierTask.FETCH_MANA;


//    return Utils.rng.nextBoolean() ? CarrierTask.FETCH_ADAMANTIUM : CarrierTask.FETCH_MANA;
//    if (totalManaAroundMe > totalAdamantiumAroundMe) {
//      return CarrierTask.FETCH_ADAMANTIUM;
//    } else {
//      return CarrierTask.FETCH_MANA;
//    }
  }

  @Override
  protected void runTurn() throws GameActionException {
    //    if (Cache.PerTurn.ROUND_NUM == 400) {
//      rc.resign();
//    }
//    if (Cache.PerTurn.ROUND_NUM >= 10) rc.resign();

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
//      Direction away = Cache.PerTurn.CURRENT_LOCATION.directionTo(lastEnemyLocation).opposite();
//      MapLocation fleeDirection = Cache.PerTurn.CURRENT_LOCATION.add(away).add(away).add(away).add(away).add(away);
      while (--fleeingCounter >= 0 && rc.isMovementReady() && pathing.moveAwayFrom(lastEnemyLocation)) {}
//      if (cachedLastEnemyForBroadcast != null) { // we need to broadcast this enemy
//        forcedNextTask = CarrierTask.DELIVER_RSS_HOME;
//        resetTask();
//      }
    }

    // run the current task until we fail to complete it (incomplete -> finish on next turn/later)
    while (currentTask != null && currentTask.execute(this)) {
      resetTask();
    }

    do_broadcast: if (cachedLastEnemyForBroadcast != null) {
      for (RobotInfo friendlyRobot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
        if (friendlyRobot.type == RobotType.LAUNCHER && friendlyRobot.location.isWithinDistanceSquared(cachedLastEnemyForBroadcast.location, friendlyRobot.type.actionRadiusSquared)) {
          cachedLastEnemyForBroadcast = null;
          break do_broadcast;
        }
      }
      if (rc.canWriteSharedArray(0,0)) {
        Communicator.writeEnemy(cachedLastEnemyForBroadcast);
        cachedLastEnemyForBroadcast = null;
      }
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
      RobotType type = enemyRobot.type;
      if (type == RobotType.HEADQUARTERS) continue;
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

  private RobotInfo attackEnemyIfCannotRun() throws GameActionException {
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
    RobotInfo nearestCombatEnemy = null;
    int myDistanceToNearestEnemy = Integer.MAX_VALUE;
    for (RobotInfo enemyRobot : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
      if (enemyRobot.type == RobotType.LAUNCHER) {
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
      fleeingCounter = TURNS_TO_FLEE;
      // check if we need to cache the enemy for broadcasting (only if no friendly launchers nearby)
      cachedLastEnemyForBroadcast = nearestCombatEnemy;
      for (RobotInfo friendlyRobot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
        if (friendlyRobot.type == RobotType.LAUNCHER) {
          int distToEnemy = friendlyRobot.location.distanceSquaredTo(lastEnemyLocation);
          if (distToEnemy <= myDistanceToNearestEnemy) {
            cachedLastEnemyForBroadcast = null;
            fleeingCounter--;
//            break;
          } else if (distToEnemy <= friendlyRobot.type.actionRadiusSquared) {
            cachedLastEnemyForBroadcast = null;
//            fleeingCounter--;
          }
//          break;
        }
      }
      if (cachedLastEnemyForBroadcast == null) { // we found friends nearby
        fleeingCounter /= 2;
      }
    } else {
      fleeingCounter = 0;
    }
  }

  /**
   * executes the DELIVER_RSS_HOME task
   * @return true if the task is complete
   * @throws GameActionException
   */
  private boolean executeDeliverRssHome() throws GameActionException {
    if (rc.getWeight() == 0) {
      return true;
    }
    rc.setIndicatorString("Deliver rss home: " + currentTask.targetHQLoc);
    if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(currentTask.targetHQLoc)) {
      pathing.moveTowards(currentTask.targetHQLoc);
    }
    if (!rc.isActionReady()) {
      pathing.goTowardsOrStayAtEmptiestLocationNextTo(currentTask.targetHQLoc);
    }
    for (ResourceType type : ResourceType.values()) {
      if (transferResource(currentTask.targetHQLoc, type, rc.getResourceAmount(type)) && rc.getWeight() == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * executes one of the resource fetch tasks (Ad, Ma, El)
   * @param resourceType the type to collect
   * @return true if task complete
   */
  private boolean executeFetchResource(ResourceType resourceType) throws GameActionException {
    if (rc.getWeight() >= MAX_CARRYING_CAPACITY) return true;
    no_well: if (currentTask.targetWell == null
        || (currentTask.turnsRunning % 20 == 0 && !currentTask.targetWell.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, 400))
        || (lastEnemyLocation != null && currentTask.targetWell.isWithinDistanceSquared(lastEnemyLocation, 26) && Cache.PerTurn.ROUND_NUM - lastEnemyLocationRound <= 8)) {
      if ((lastEnemyLocation != null && currentTask.targetWell != null && currentTask.targetWell.isWithinDistanceSquared(lastEnemyLocation, 26) && Cache.PerTurn.ROUND_NUM - lastEnemyLocationRound <= 8)) {
        findNewWell(currentTask.collectionType, currentTask.targetWell);
      } else {
        findNewWell(currentTask.collectionType, null);
      }
      if (currentTask.targetWell != null) break no_well;
//      boolean foundWell = currentTask.targetWell != null;
//      if (foundWell && lastEnemyLocation != null) {
//        int roundsSinceLastEnemy = rc.getRoundNum() - lastEnemyLocationRound;
//        boolean nearEnemy = currentTask.targetWell.isWithinDistanceSquared(lastEnemyLocation, 26) && roundsSinceLastEnemy <= 8;
//        if (nearEnemy) foundWell = false;
//      }
//      if (foundWell) break no_well;
      WellInfo[] nearby = rc.senseNearbyWells(resourceType);
      if (nearby.length == 0) {
        if (currentTask.turnsRunning > MAX_TURNS_TO_LOOK_FOR_WELL) {
          forcedNextTask = CarrierTask.SCOUT;
          return true;
        } else {
          while (tryExplorationMove()) {
            nearby = rc.senseNearbyWells(resourceType);
            int distance = Integer.MAX_VALUE;
            if (nearby.length > 0) {
              for (WellInfo well : nearby) {
                if (Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(well.getMapLocation()) < distance) {
                  currentTask.targetWell = well.getMapLocation();
                }
              }
              break no_well;
            }
          }
          return false;
        }
      }
      currentTask.targetWell = nearby[Utils.rng.nextInt(nearby.length)].getMapLocation();
    }
    if (currentTask.targetWell == null) {
      assert false;
    }

    if (doWellCollection(currentTask.targetWell)) {
      return true; // we are full
    }
    if(!approachWell(currentTask.targetWell)) {
      blackListWells.add(currentTask.targetWell);
      switch(currentTask) {
        case FETCH_ADAMANTIUM:
          forcedNextTask = CarrierTask.FETCH_MANA;
        default: // TODO: check if elixir well exists and cycle to that
        case FETCH_MANA:
          forcedNextTask = CarrierTask.FETCH_ADAMANTIUM;
      }
      return true;
    }
    return currentTask.targetWell != null && doWellCollection(currentTask.targetWell);
  }

  /**
   * executes the ANCHOR_ISLAND task
   * @return true if complete
   * @throws GameActionException any exceptions with anchoring
   */
  private boolean executeAnchorIsland() throws GameActionException {
    if (rc.getAnchor() == null) {
      MapLocation hqWithAnchor = currentTask.targetHQLoc;
//      if (rc.canSenseLocation(hqWithAnchor) && rc.senseRobotAtLocation(hqWithAnchor).getTotalAnchors() == 0) {
//        forcedNextTask = CarrierTask.SCOUT;
//        return true;
//      }
      do {
        if (takeAnchor(hqWithAnchor, Anchor.ACCELERATING) || takeAnchor(hqWithAnchor, Anchor.STANDARD)) {
          break;
        } else if (rc.canSenseLocation(hqWithAnchor) && rc.senseRobotAtLocation(hqWithAnchor).getTotalAnchors() == 0) {
          forcedNextTask = CarrierTask.SCOUT;
          return true;
        }
      } while (pathing.moveTowards(hqWithAnchor));
    }
    if (rc.getAnchor() != null) {
      if (currentTask.targetIsland == null) {
        currentTask.targetIsland = findIslandLocationToClaim();
      }
      return moveTowardsIslandAndClaim();
    }
    return false;
  }

  /**
   * executes the SCOUT task
   * will simply explore until symmetry is known
   * @return true if scouting complete
   * @throws GameActionException any issues with moving
   */
  private boolean executeScout() throws GameActionException {
    if (RunningMemory.knownSymmetry != null) return true;
    if (Cache.PerTurn.ROUND_NUM <= MIN_TURN_TO_EXPLORE) {
//      Printer.print("Scout: too early to explore");
      return true;
    }
    if (currentTask.turnsRunning >= MAX_SCOUT_TURNS) {
      return true;
    }
    doExploration();
    return false;
  }

  /**
   * executes ATTACK task
   * currently does nothing
   * @return true if attack task complete
   * @throws GameActionException any issues while attacking
   */
  private boolean executeAttack() throws GameActionException {
    return true;
  }

  /**
   * will approach the given well and keep track of the entry direction and well queue etc
   * MAY OVERWRITE target well to null
   * @param wellLocation the well to go to
   * @returns True if was able to approach well, False if it got stuck and waited.
   * @throws GameActionException any issues with sensing/moving
   */
  private boolean approachWell(MapLocation wellLocation) throws GameActionException {
    if (wellQueueOrder == null) {
      rc.setIndicatorString("no well queue cycle -- approaching=" + wellLocation + " dist=" + Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation));
//      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, wellLocation, 0, 255, 0);
      while (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, SET_WELL_PATH_DISTANCE)) {
        if (pathing.moveTowards(wellLocation)) {
//          Printer.print("moved towards well: " + targetWell + " now=" + Cache.PerTurn.CURRENT_LOCATION);//, "new dir back: " + wellApproachDirection.get(targetWell));
//        wellApproachDirection.setAlreadyContainedValue(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
          rc.setIndicatorString("did pathing towards well@" + wellLocation);
        } else {
          if (rc.isMovementReady()) {
            rc.setIndicatorString("could not path towards well@" + wellLocation);
            turnsStuckApproachingWell++;
            if (turnsStuckApproachingWell >= MAX_TURNS_STUCK) {
              return false;
            }
          }
          break;
        }
      }
      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, SET_WELL_PATH_DISTANCE)) {
        wellApproachDirection.put(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
        wellQueueOrder = CarrierWellPathing.getPathForWell(wellLocation, wellApproachDirection.get(wellLocation));
        wellEntryPoint = wellQueueOrder[0];
        wellQueueSize = 0;
        for (int i = 0; i < wellQueueOrder.length; i++) {
          if (wellQueueOrder[i].isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, Cache.Permanent.VISION_RADIUS_SQUARED)
              && ((rc.canSenseLocation(wellQueueOrder[i]) && rc.sensePassability(wellQueueOrder[i]))
                  || (rc.senseCloud(wellQueueOrder[i])))
              && !BugNav.blockedLocations.contains(wellQueueOrder[i])
          ) {
            wellQueueSize++;
            RobotInfo robot = rc.canSenseRobotAtLocation(wellQueueOrder[i]) ? rc.senseRobotAtLocation(wellQueueOrder[i]) : null;
            if (robot == null || robot.type != RobotType.HEADQUARTERS) {
              wellEntryPoint = wellQueueOrder[i];
              break;
            }
          }
        }
//        Printer.print("well path: " + Arrays.toString(wellQueueOrder), "from direction: " + wellApproachDirection.get(wellLocation));
        rc.setIndicatorString("set well path:" + Cache.PerTurn.CURRENT_LOCATION + "->" + wellLocation);
      }
    }
    if (wellQueueOrder != null) {
      rc.setIndicatorString("follow well path: " + wellLocation);
      if (!followWellQueue(wellLocation)) return false;
//      } else {
//        Printer.print("Cannot get to well! -- canMove=" + rc.isMovementReady());
    }
    return true;
  }

  /**
   * will follow the collection quuee of the well (circle around the well)
   * will move along path depending on robots seen
   * ASSUMES - within 2x2 of well
   * @return boolean. false is failed and should try other well. true if good :)
   */
  private boolean followWellQueue(MapLocation wellLocation) throws GameActionException {
    updateRobotsSeenInQueue(wellLocation);
    updateWellQueueTarget();

    no_queue_spot: if (wellQueueTargetIndex == -1) { // no spot in queue
      if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation) && rc.getWeight() < MAX_CARRYING_CAPACITY) { // we're adjacent and below capacity but have no spot in line
        Printer.print("no queue spot but adjacent to well");
        Printer.print("wellQueueOrder=" + Arrays.toString(wellQueueOrder));
        Printer.print("wellQueueTargetIndex=" + wellQueueTargetIndex);
        Printer.print("emptier=" + emptierRobotsSeen);
        Printer.print("fuller=" + fullerRobotsSeen);
        throw new RuntimeException("should have a well spot if already adjacent");
//        if (!pathing.moveTowards(wellEntryPoint)) {
//          roundsWaitingForQueueSpot++;
//          if (roundsWaitingForQueueSpot > MAX_ROUNDS_WAIT_FOR_WELL_PATH) {
//            rc.setIndicatorString("failed to move towards well too many times");
//            findNewWell(currentTask.collectionType, currentTask.targetWell);
//          }
//        }
      } else { // we aren't there yet, so consider switching wells
        int numCarriersFarFromFull = 0;
        for (RobotInfo friendly : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
          if (friendly.type == RobotType.CARRIER && friendly.location.isAdjacentTo(wellLocation)) {
            int friendlyAmount = getInvWeight(friendly);
            if (friendlyAmount < FAR_FROM_FULL_CAPACITY) {
              numCarriersFarFromFull++;
            }
          }
        }

        if (numCarriersFarFromFull > wellQueueSize - MIN_SPOTS_LEFT_FROM_CARRIERS_FILLING_IN_FRONT) {
          rc.setIndicatorString("there's " + numCarriersFarFromFull + " far from full so ima dip");
          return false;
//          findNewWell(currentTask.collectionType, currentTask.targetWell);
        } else {
          roundsWaitingForQueueSpot++;
          if (roundsWaitingForQueueSpot > MAX_ROUNDS_WAIT_FOR_WELL_PATH) {
            rc.setIndicatorString("I've waited too long for well@" + wellLocation + " so ima dip");
//            findNewWell(currentTask.collectionType, currentTask.targetWell);
            return false;
          } else {
            do {
              if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellEntryPoint)) {
                if (pathing.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellEntryPoint))) {
                  rc.setIndicatorString("moved towards well entry point");
                }
              }
            } while (pathing.goTowardsOrStayAtEmptiestLocationNextTo(wellEntryPoint));
            rc.setIndicatorString("waiting @ well=" + wellLocation + ".entry@" + wellEntryPoint + "-turn#" + roundsWaitingForQueueSpot + " -emp=" + emptierRobotsSeen + "-ful=" + fullerRobotsSeen);
//            rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, wellEntryPoint, 0, 255, 0);
          }
        }
      }
    }

    updateWellQueueTarget();

    if (wellQueueTargetIndex != -1) {  // we have a spot in the queue (wellQueueTargetIndex)
      rc.setIndicatorString("well queue target position: " + wellQueueOrder[wellQueueTargetIndex]);
//      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, wellQueueOrder[wellQueueTargetIndex], 0, 255, 0);
      while (rc.isMovementReady() && !Cache.PerTurn.CURRENT_LOCATION.equals(wellQueueOrder[wellQueueTargetIndex])) {
        // not yet in the path, just go to the entry point
        not_in_path: if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation)) {
          rc.setIndicatorString("approaching well (" + wellLocation + ") via: " + wellEntryPoint);
          while (pathing.moveTowards(wellEntryPoint)) {}
          if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation)) {
            break not_in_path;
          }
          if (rc.isMovementReady()) {
            for (int i = 0; i < wellQueueOrder.length; i++) {
              if (rc.canSenseLocation(wellQueueOrder[i]) && rc.sensePassability(wellQueueOrder[i]) && !BugNav.blockedLocations.contains(wellQueueOrder[i])) {
                RobotInfo robot = rc.senseRobotAtLocation(wellQueueOrder[i]);
                if (robot == null || (robot.type != RobotType.HEADQUARTERS && robot.type != RobotType.CARRIER)) {
                  wellEntryPoint = wellQueueOrder[i];
                  break;
                }
              }
            }
            turnsStuckApproachingWell++;
            rc.setIndicatorString("entry for " + wellLocation + " blocked (" + turnsStuckApproachingWell + " turns)-- new entry via " + wellEntryPoint);
            if (turnsStuckApproachingWell >= MAX_TURNS_STUCK) {
              findNewWell(currentTask.collectionType, currentTask.targetWell);
              if (currentTask.targetWell != null) {
                approachWell(currentTask.targetWell);
                return true;
              }
            }
          }
          break;
        }

        // in the path, so try to get towards the correct point by navigating through the path
        int currentPathIndex = -1;
        for (int i = 0; i < wellQueueOrder.length; i++) {
          if (Cache.PerTurn.CURRENT_LOCATION.equals(wellQueueOrder[i])) {
            currentPathIndex = i;
            break;
          }
        }
        rc.setIndicatorString("moving in well queue: " + wellQueueTargetIndex + "=" + wellQueueOrder[wellQueueTargetIndex] + " -- currently at ind=" + currentPathIndex + "=" + wellQueueOrder[currentPathIndex]);

////        Printer.print("following path: " + Cache.PerTurn.CURRENT_LOCATION + "|" + rc.getLocation() + " --> " + wellPathToFollow[moveTrial]);
////        Printer.print("following path: ->" + wellPathToFollow[moveTrial]);
//        boolean moved = false;
//        if (currentPathIndex < wellQueueTargetIndex) {
//          moved = pathing.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellQueueOrder[currentPathIndex + 1])) || pathing.stayAtEmptiestLocationNextTo(wellLocation);
//        } else {
//          moved = pathing.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellQueueOrder[currentPathIndex - 1])) || pathing.stayAtEmptiestLocationNextTo(wellLocation);
//        }
//        if (!moved) break;
        int moveTrial = wellQueueTargetIndex;
        if (currentPathIndex < wellQueueTargetIndex || currentPathIndex == -1) {
          while (moveTrial >= 0
              && (
              !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellQueueOrder[moveTrial])
                  && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellQueueOrder[moveTrial])))
//                  || BugNav.blockedLocations.contains(wellQueueOrder[moveTrial])
//                  || !windOk(wellQueueOrder[moveTrial], wellLocation)
          )) {
            // we can't move (adjacent + can move || blocked || wind is bad)
            moveTrial--;
          }
          if (moveTrial < 0) {
            break;
          }
        }
        if (currentPathIndex > wellQueueTargetIndex || currentPathIndex == -1) {
          while (moveTrial < 9
              && (
              !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellQueueOrder[moveTrial])
                  && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellQueueOrder[moveTrial])))
//                  || BugNav.blockedLocations.contains(wellQueueOrder[moveTrial])
//                  || !windOk(wellQueueOrder[moveTrial], wellLocation)
          )) {
            // we can't move (adjacent + can move)
            moveTrial++;
          }
          if (moveTrial >= 9) {
            break;
          }
        }
//        Printer.print("following path: " + Cache.PerTurn.CURRENT_LOCATION + "|" + rc.getLocation() + " --> " + wellPathToFollow[moveTrial]);
//        Printer.print("following path: ->" + wellPathToFollow[moveTrial]);
        pathing.forceMoveTo(wellQueueOrder[moveTrial]);
      }
    }
    return true;
  }

  private boolean windOk(MapLocation testLocation, MapLocation wellLocation) throws GameActionException {
    if (!rc.canSenseLocation(testLocation)) return true; // can't sense, so assume it's ok
    MapInfo testLocInfo = rc.senseMapInfo(testLocation);
    Direction wind = testLocInfo.getCurrentDirection();
    if (wind == Direction.CENTER) return true; // no wind
    MapLocation next = testLocation.add(wind);
    int moveCD = ((int) (GameConstants.CARRIER_MOVEMENT_INTERCEPT + rc.getWeight() * GameConstants.CARRIER_MOVEMENT_SLOPE));
    int newMoveCD = rc.getMovementCooldownTurns() + ((int) (moveCD * (rc.canSenseLocation(next) ? rc.senseMapInfo(next).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) : 1)));
    if (newMoveCD < GameConstants.COOLDOWN_LIMIT) return true; // can move again
    return next.isAdjacentTo(wellLocation);
  }

  /**
   * senses around and updates the number of emptier/fuller robots seen near this well
   * if we aren't close to the well, leave the info as is
   * @return whether an update was made or not
   */
  private boolean updateRobotsSeenInQueue(MapLocation wellLocation) throws GameActionException {
    if (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, 9)) { // well is still far away
      if (emptierRobotsSeen > 0 || fullerRobotsSeen > 0) {
//        Printer.print("not close to well, so not updating emptier/fuller robots seen");
        emptierRobotsSeen = 0;
        fullerRobotsSeen = 0;
        return true;
      }
      return false;
    }
    int newEmptierSeen = 0;
    int newFullerSeen = 0;
    int myAmount = rc.getWeight();
    for (RobotInfo friendly : rc.senseNearbyRobots(wellLocation, Utils.DSQ_2by2, Cache.Permanent.OUR_TEAM)) {
      if (friendly.type == RobotType.CARRIER) {
        int friendlyAmount = getInvWeight(friendly);
        if (friendlyAmount == myAmount) {
          if (friendly.ID < Cache.Permanent.ID) {
            newEmptierSeen++;
          } else {
            newFullerSeen++;
          }
        } else if (friendlyAmount < myAmount) {
          newEmptierSeen++;
        } else if (friendlyAmount < MAX_CARRYING_CAPACITY) {
          newFullerSeen++;
        }
      }
    }
    boolean changed = newEmptierSeen != emptierRobotsSeen || newFullerSeen != fullerRobotsSeen;
    emptierRobotsSeen = newEmptierSeen;
    fullerRobotsSeen = newFullerSeen;
    return changed;
  }
  /**
   * finds the optimal point in the well queue to stand
   * @throws GameActionException any issues during computation
   */
  private void updateWellQueueTarget() throws GameActionException {
    if (wellQueueOrder == null) return;
    wellQueueTargetIndex = -1;
    if (rc.getWeight() >= MAX_CARRYING_CAPACITY) return;

    int minFillSpot = emptierRobotsSeen;
    int maxFillSpot = 8 - fullerRobotsSeen;
    int testSpot;
    while (minFillSpot <= maxFillSpot) {
      if (minFillSpot < (8-maxFillSpot)) {
        testSpot = minFillSpot;
        minFillSpot++;
      } else {
        testSpot = maxFillSpot;
        maxFillSpot--;
      }
      MapLocation pathTarget = wellQueueOrder[testSpot];
      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || isValidQueuePosition(currentTask.targetWell, pathTarget)) {
        // we can move to the target (or already there)
        wellQueueTargetIndex = testSpot;
        roundsWaitingForQueueSpot = 0;
        return;
//      } else {
//        Printer.print("can't move from=" + Cache.PerTurn.CURRENT_LOCATION + ".to=" + pathTarget + " sense=" + rc.canSenseLocation(pathTarget) + ".bot=" + rc.canSenseRobotAtLocation(pathTarget));
      }
    }
    while (maxFillSpot >= 0) {
      MapLocation pathTarget = wellQueueOrder[maxFillSpot];
      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || isValidQueuePosition(currentTask.targetWell, pathTarget)) {
        // we can move to the target (or already there)
        wellQueueTargetIndex = maxFillSpot;
        roundsWaitingForQueueSpot = 0;
        return;
//      } else {
//        Printer.print("can't move from=" + Cache.PerTurn.CURRENT_LOCATION + ".to=" + pathTarget + " sense=" + rc.canSenseLocation(pathTarget) + ".bot=" + rc.canSenseRobotAtLocation(pathTarget));
      }
      maxFillSpot--;
    }
//    while (minFillSpot < 9) {
//      MapLocation pathTarget = wellQueueOrder[minFillSpot];
//      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || isValidQueuePosition(currentTask.targetWell, pathTarget)) {
//        // we can move to the target (or already there)
//        wellQueueTargetIndex = minFillSpot;
//        roundsWaitingForQueueSpot = 0;
//        return;
//      }
//      minFillSpot++;
//    }
    if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(currentTask.targetWell)) {
      // we are adjacent to the well, so we can't move anywhere
      for (int i = 0; i < 9; i++) {
        if (wellQueueOrder[i].equals(Cache.PerTurn.CURRENT_LOCATION)) {
          wellQueueTargetIndex = i;
          break;
        }
      }
      roundsWaitingForQueueSpot = 0;
    }

//    for (int i = 0; i < maxFillSpot; i++) {
//      MapLocation pathTarget = wellPathToFollow[i];
//      // already there or can move there
//      if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || (rc.canSenseLocation(pathTarget) && !rc.canSenseRobotAtLocation(pathTarget))) {
//        wellPathTargetIndex = i;
//        if (i >= emptierRobotsSeen) {
//          return;
//        }
//      }
//    }
  }

  /**
   * checks if a certain position is a valid queueing spot for the given well location
   * should be passable and not a robot?
   * ASSUMES adjacency
   * @param wellLocation the well location
   * @param queuePosition the position to check
   * @return true if the position is valid
   * @throws GameActionException
   */
  private boolean isValidQueuePosition(MapLocation wellLocation, MapLocation queuePosition) throws GameActionException {
    local_checks: {
      if (!rc.canSenseLocation(queuePosition)) break local_checks; // assume it is valid if can't sense
      if (rc.canSenseRobotAtLocation(queuePosition)) return false; // blocked by a robot
      MapInfo mapInfo = rc.senseMapInfo(queuePosition);
      if (!mapInfo.isPassable()) return false; // isn't passable
      if (!queuePosition.add(mapInfo.getCurrentDirection()).isAdjacentTo(wellLocation)) return false; // gets blown away
    }
    if (BugNav.blockedLocations.contains(queuePosition)) return false; // blocked by a bugnav
    if (rc.canSenseLocation(queuePosition)
        && BugNav.blockedLocations.contains(
        queuePosition.add(rc.senseMapInfo(queuePosition).getCurrentDirection()
        ))) return false; // pushed into a blocked location
    return true;
  }

  /**
   * will figure out the next best well to go to (respects blackListWells)
   * @param resourceType the well type to look for
   * @param toAvoid a map location to exclude from the search
   * @throws GameActionException any issues duirng search (comms, sensing)
   */
  private void findNewWell(ResourceType resourceType, MapLocation toAvoid) throws GameActionException {
    CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(resourceType);
    MapLocation closestWellLocation = null;
    int closestDist = Integer.MAX_VALUE;
    for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
      if (!writer.readWellExists(i)) break;
      MapLocation wellLocation = writer.readWellLocation(i);
      if (!wellLocation.equals(toAvoid) && !blackListWells.contains(wellLocation)) {
        if (lastEnemyLocation != null && wellLocation.isWithinDistanceSquared(lastEnemyLocation, 26) && Cache.PerTurn.ROUND_NUM - lastEnemyLocationRound <= 7) {
          continue;
        }
        int dist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation);
        if (dist < closestDist) {
          closestDist = dist;
          closestWellLocation = wellLocation;
        }
      }
    }
    currentTask.targetWell = closestWellLocation;
    this.turnsStuckApproachingWell = 0;
    this.wellQueueOrder = null;
    this.wellQueueTargetIndex = -1;
    this.emptierRobotsSeen = 0;
    this.fullerRobotsSeen = 0;
    if (closestWellLocation != null) {
      Direction dirBackFromWell;// = closestWellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION);
      MapLocation targetHQ = HqMetaInfo.getClosestHqLocation(closestWellLocation);
      if (targetHQ != null && targetHQ.isWithinDistanceSquared(closestWellLocation, RobotType.HEADQUARTERS.visionRadiusSquared)) {
//        Printer.print("close to HQ, use hq dir - hq=" + targetHQ + " -- well=" + closestWellLocation + " -- dir=" + closestWellLocation.directionTo(targetHQ));
        dirBackFromWell = closestWellLocation.directionTo(targetHQ);
        this.wellApproachDirection.put(closestWellLocation, dirBackFromWell);
      }
    }
  }

  /**
   * will explore around until an island location is found
   * @return the found location of the island
   * @throws GameActionException any errors while sensing
   */
  private MapLocation findIslandLocationToClaim() throws GameActionException {
    // go to unclaimed island
    while (doIslandFindingMove()) {
      int[] nearbyIslands = rc.senseNearbyIslands();
      if (nearbyIslands.length > 0) {
        MapLocation closestUnclaimedIsland = null;
        int closestDistance = Integer.MAX_VALUE;
        for (int islandID : nearbyIslands) {
          if (rc.senseTeamOccupyingIsland(islandID) == Team.NEUTRAL) {
            MapLocation islandLocation = rc.senseNearbyIslandLocations(islandID)[0];
            int candidateDistance = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, islandLocation);
            if (candidateDistance < closestDistance) {
              closestUnclaimedIsland = islandLocation;
              closestDistance = candidateDistance;
            }
          }
        }
        return closestUnclaimedIsland;
      }
    }
    return null;
  }

  private boolean doIslandFindingMove() throws GameActionException {
    if (!rc.isMovementReady()) return false;
//    int avgX = 0;
//    int avgY = 0;
//    int numFriends = 0;
//    for (RobotInfo robot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
//      if (robot.type == RobotType.CARRIER) {
//        avgX += robot.location.x;
//        avgY += robot.location.y;
//        numFriends++;
//      }
//    }
    if (currentTask.turnsRunning % 100 == 0) {
      randomizeExplorationTarget(true);
    }
//    if (numFriends <= 15) { // not too many nearby friends
    return tryExplorationMove();
//    }
//    MapLocation avgLocation = new MapLocation(avgX / numFriends, avgY / numFriends);
//    Direction toAvg = Cache.PerTurn.CURRENT_LOCATION.directionTo(avgLocation);
//    int tries = 10;
//    while (tries-- > 0 && Cache.PerTurn.CURRENT_LOCATION.directionTo(explorationTarget).equals(toAvg)) {
//      randomizeExplorationTarget(true);
//    }
//    if (tries == 0) {
//      explorationTarget = new MapLocation(
//          Cache.Permanent.MAP_WIDTH*Utils.rng.nextInt(2),
//          Cache.Permanent.MAP_HEIGHT*Utils.rng.nextInt(2));
//    }
//    return pathing.moveAwayFrom(avgLocation);
  }

  /**
   * moves towards an unclaimed island and tries to claim it
   * @return true if anchor placed / island claimed
   * @throws GameActionException any issues with sensing / anchoring
   */
  private boolean moveTowardsIslandAndClaim() throws GameActionException {
    // go to unclaimed island
    MapLocation islandLocationToClaim = currentTask.targetIsland;
    if (islandLocationToClaim == null) return false;

    // someone else claimed it while we were moving to the unclaimed island
    if (rc.canSenseLocation(islandLocationToClaim)) {
      if (rc.senseTeamOccupyingIsland(rc.senseIsland(islandLocationToClaim)) != Team.NEUTRAL) {
        islandLocationToClaim = findIslandLocationToClaim();
        if (islandLocationToClaim == null) return false;
      }
    }

    // check if we are on a claimable island
    do {
      int islandID = rc.senseIsland(Cache.PerTurn.CURRENT_LOCATION);
      if ((islandID != -1 && rc.senseTeamOccupyingIsland(islandID) == Team.NEUTRAL)) {
        if (rc.canPlaceAnchor()) {
          rc.placeAnchor();
          return true;
        }
      }
    } while (pathing.moveTowards(islandLocationToClaim));
    return false;
  }

  private enum CarrierTask {
    FETCH_ADAMANTIUM(ResourceType.ADAMANTIUM),
    FETCH_MANA(ResourceType.MANA),
    FETCH_ELIXIR(ResourceType.ELIXIR),
    DELIVER_RSS_HOME(ResourceType.NO_RESOURCE),
    ANCHOR_ISLAND(ResourceType.NO_RESOURCE),
    SCOUT(ResourceType.NO_RESOURCE),
    ATTACK(ResourceType.NO_RESOURCE);

    public MapLocation targetHQLoc;
    public MapLocation targetWell;
    final public ResourceType collectionType;
    public MapLocation targetIsland;
    public int turnsRunning;

    CarrierTask(ResourceType resourceType) {
      collectionType = resourceType;
    }

    public void onTaskStart(Carrier carrier) throws GameActionException {
      turnsRunning = 0;
      switch (this) {
        case FETCH_ADAMANTIUM:
        case FETCH_MANA:
        case FETCH_ELIXIR:
          carrier.findNewWell(this.collectionType, null);
          break;
        case DELIVER_RSS_HOME:
          targetHQLoc = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
          break;
        case ANCHOR_ISLAND:
          targetHQLoc = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
          break;
        case SCOUT:
          break;
        case ATTACK:
          break;
      }
    }

    public void onTaskEnd() throws GameActionException {
      switch (this) {
        case FETCH_ADAMANTIUM:
        case FETCH_MANA:
        case FETCH_ELIXIR:
          targetWell = null;
          break;
        case DELIVER_RSS_HOME:
          targetHQLoc = null;
          break;
        case ANCHOR_ISLAND:
          targetHQLoc = null;
          break;
        case SCOUT:
          break;
        case ATTACK:
          break;
      }
    }

    /**
     * executes the turn logic for the given task
     * @param carrier the carrier on which to run the tasl
     * @return true if the task is complete
     * @throws GameActionException any exception thrown by the robot controller
     */
    public boolean execute(Carrier carrier) throws GameActionException {
      turnsRunning++;
      carrier.rc.setIndicatorString("Carrier - current task: " + this + " - turns: " + turnsRunning);
      switch (this) {
        case FETCH_ADAMANTIUM:
          return carrier.executeFetchResource(ResourceType.ADAMANTIUM);
        case FETCH_MANA:
          return carrier.executeFetchResource(ResourceType.MANA);
        case FETCH_ELIXIR:
          return carrier.executeFetchResource(ResourceType.ELIXIR);
        case DELIVER_RSS_HOME:
          return carrier.executeDeliverRssHome();
        case ANCHOR_ISLAND:
          return carrier.executeAnchorIsland();
        case SCOUT:
          return carrier.executeScout();
        case ATTACK:
          return carrier.executeAttack();
      }
      return false;
    }
  }

  private boolean transferResource(MapLocation target, ResourceType resourceType, int resourceAmount) throws GameActionException {
    if (!rc.canTransferResource(target, resourceType, resourceAmount)) {
      return false;
    }
    rc.transferResource(target, resourceType, resourceAmount);
    return true;
  }

  /**
   * will collect as much as possible from the target well
   * @return true if full now
   * @throws GameActionException any issues during collection
   */
  private boolean doWellCollection(MapLocation wellLoc) throws GameActionException {
    while (collectResource(wellLoc, -1)) {
      if (rc.getWeight() >= MAX_CARRYING_CAPACITY) return true;
    }
    return false;
  }

  private boolean collectResource(MapLocation well, int amount) throws GameActionException {
    if (rc.canCollectResource(well, amount)) {
      rc.collectResource(well, amount);
      return true;
    }
    return false;
  }

  private boolean takeAnchor(MapLocation hq, Anchor anchorType) throws GameActionException {
    if (!rc.canTakeAnchor(hq, anchorType)) return false;
    rc.takeAnchor(hq, anchorType);
    return true;
  }
}
