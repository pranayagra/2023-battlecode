package alexlaunchermacro.robots;

import alexlaunchermacro.communications.CommsHandler;
import alexlaunchermacro.communications.HqMetaInfo;
import alexlaunchermacro.communications.MapMetaInfo;
import alexlaunchermacro.containers.HashMap;
import alexlaunchermacro.robots.micro.CarrierWellPathing;
import alexlaunchermacro.utils.Cache;
import alexlaunchermacro.utils.Printer;
import alexlaunchermacro.utils.Utils;
import battlecode.common.*;

import java.util.Arrays;

public class Carrier extends MobileRobot {

  private static final int MAX_CARRYING_CAPACITY = GameConstants.CARRIER_CAPACITY;
  private static final int FAR_FROM_FULL_CAPACITY = MAX_CARRYING_CAPACITY * 4 / 5;
  private static final int MAX_RSS_TO_ENABLE_SCOUT = 1000;
  private static final int SET_WELL_PATH_DISTANCE = RobotType.CARRIER.actionRadiusSquared;
  private static final int MAX_TURNS_STUCK = 3;
  private static final int MAX_ROUNDS_WAIT_FOR_WELL_PATH = 3;
  private static final int MAX_CARRIERS_FILLING_IN_FRONT = 8;
  private static final int TURNS_TO_FLEE = 4;
  private static final int MAX_SCOUT_TURNS = 50;
  private static final int MAX_TURNS_TO_LOOK_FOR_WELL = 10;

  CarrierTask currentTask;
  CarrierTask forcedNextTask;


  private int turnsStuckApproachingWell;
  private HashMap<MapLocation, Direction> wellApproachDirection;
  private MapLocation[] wellQueueOrder;
  private MapLocation wellEntryPoint;
  int wellQueueTargetIndex;
  private int emptierRobotsSeen;
  private int fullerRobotsSeen;
  private int roundsWaitingForQueueSpot;


  int fleeingCounter;
  MapLocation lastEnemyLocation;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    wellApproachDirection = new HashMap<>(3);
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
  
  private CarrierTask determineNewTask() throws GameActionException {
    if (rc.getWeight() >= MAX_CARRYING_CAPACITY) {
      return CarrierTask.DELIVER_RSS_HOME;
    }
    MapLocation closestHQLoc = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
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
//    Printer.print("totalAdamantiumAroundMe: " + totalAdamantiumAroundMe);
//    Printer.print("totalManaAroundMe: " + totalManaAroundMe);
    if (totalManaAroundMe <= 1.75 * totalAdamantiumAroundMe) { // ad < 0.666 * mana
//      Printer.print("Collecting mana");
      return CarrierTask.FETCH_MANA;
    }
    if (totalAdamantiumAroundMe < 0.5 * totalManaAroundMe) {
//      Printer.print("Collecting adamantium");
      return CarrierTask.FETCH_ADAMANTIUM;
    }
//    Printer.print("Collecting mana");
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
      pathing.moveTowards(fleeDirection);
      fleeingCounter--;
    }

    // run the current task until we fail to complete it (incomplete -> finish on next turn/later)
    while (currentTask != null && currentTask.execute(this)) {
      resetTask();
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
      lastEnemyLocation = nearestCombatEnemy;
      fleeingCounter = TURNS_TO_FLEE;
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
    no_well: if (currentTask.targetWell == null) {
      findNewWell(currentTask.collectionType, currentTask.targetWell);
      if (currentTask.targetWell != null) break no_well;
      WellInfo[] nearby = rc.senseNearbyWells(resourceType);
      if (nearby.length == 0) {
        if (currentTask.turnsRunning > MAX_TURNS_TO_LOOK_FOR_WELL) {
          forcedNextTask = CarrierTask.SCOUT;
          return true;
        } else {
          while (tryExplorationMove()) {
            nearby = rc.senseNearbyWells(resourceType);
            if (nearby.length > 0) {
              currentTask.targetWell = nearby[Utils.rng.nextInt(nearby.length)].getMapLocation();
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
    approachWell(currentTask.targetWell);
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
    if (MapMetaInfo.knownSymmetry != null) return true;
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
   * will appreach the given well and keep track of the entry direction and well queue etc
   * MAY OVERWRITE target well to null
   * @param wellLocation the well to go to
   * @throws GameActionException any issues with sensing/moving
   */
  private void approachWell(MapLocation wellLocation) throws GameActionException {
    if (wellQueueOrder == null) {
      rc.setIndicatorString("no well path -- approaching=" + wellLocation + " dist=" + Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation));
      while (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, SET_WELL_PATH_DISTANCE)) {
        if (pathing.moveTowards(wellLocation)) {
//          Printer.print("moved towards well: " + targetWell + " now=" + Cache.PerTurn.CURRENT_LOCATION);//, "new dir back: " + wellApproachDirection.get(targetWell));
//        wellApproachDirection.setAlreadyContainedValue(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
          rc.setIndicatorString("did pathing towards well:" + Cache.PerTurn.CURRENT_LOCATION + "->" + wellLocation);
        } else {
          if (rc.isMovementReady()) {
            rc.setIndicatorString("could not path towards well:" + Cache.PerTurn.CURRENT_LOCATION + "->" + wellLocation);
            turnsStuckApproachingWell++;
            if (turnsStuckApproachingWell >= MAX_TURNS_STUCK) {
              findNewWell(currentTask.collectionType, currentTask.targetWell);
              if (currentTask.targetWell != null) {
                approachWell(currentTask.targetWell);
                return;
              }
            }
          }
          break;
        }
      }
      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, SET_WELL_PATH_DISTANCE)) {
        wellApproachDirection.put(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
        wellQueueOrder = CarrierWellPathing.getPathForWell(wellLocation, wellApproachDirection.get(wellLocation));
        wellEntryPoint = wellQueueOrder[0];
        for (int i = 0; i < wellQueueOrder.length; i++) {
          if (rc.canSenseLocation(wellQueueOrder[i]) && rc.sensePassability(wellQueueOrder[i])) {
            RobotInfo robot = rc.senseRobotAtLocation(wellQueueOrder[i]);
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
      followWellQueue(wellLocation);
//      } else {
//        Printer.print("Cannot get to well! -- canMove=" + rc.isMovementReady());
    }
  }

  /**
   * will follow the collection quuee of the well (circle around the well)
   * will move along path depending on robots seen
   * ASSUMES - within 2x2 of well
   */
  private void followWellQueue(MapLocation wellLocation) throws GameActionException {
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

        if (numCarriersFarFromFull > MAX_CARRIERS_FILLING_IN_FRONT) {
          rc.setIndicatorString("there's people in the way so ima dip");
          findNewWell(currentTask.collectionType, currentTask.targetWell);
        } else {
          roundsWaitingForQueueSpot++;
          if (roundsWaitingForQueueSpot > MAX_ROUNDS_WAIT_FOR_WELL_PATH) {
            rc.setIndicatorString("there's people in the way so ima dip");
            findNewWell(currentTask.collectionType, currentTask.targetWell);
          } else {
            do {
              if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellEntryPoint)) {
                if (pathing.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellEntryPoint))) {
                  rc.setIndicatorString("moved towards well entry point");
                }
              }
            } while (pathing.goTowardsOrStayAtEmptiestLocationNextTo(wellEntryPoint));
            rc.setIndicatorString("waiting @ well=" + wellLocation + ".entry@" + wellEntryPoint + "-turn#" + roundsWaitingForQueueSpot + " -emp=" + emptierRobotsSeen + "-ful=" + fullerRobotsSeen);
          }
        }
      }
    }

    updateWellQueueTarget();

    if (wellQueueTargetIndex != -1) {  // we have a spot in the queue (wellQueueTargetIndex)
      rc.setIndicatorString("well queue target position: " + wellQueueOrder[wellQueueTargetIndex]);
      while (rc.isMovementReady() && !Cache.PerTurn.CURRENT_LOCATION.equals(wellQueueOrder[wellQueueTargetIndex])) {
        // not yet in the path, just go to the entry point
        if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation)) {
          rc.setIndicatorString("approaching well via: " + wellEntryPoint);
          while (pathing.moveTowards(wellEntryPoint)) {}
          if (rc.isMovementReady()) {
            turnsStuckApproachingWell++;
            if (turnsStuckApproachingWell >= MAX_TURNS_STUCK) {
              findNewWell(currentTask.collectionType, currentTask.targetWell);
              if (currentTask.targetWell != null) {
                approachWell(currentTask.targetWell);
                return;
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
          while (moveTrial >= 0 && !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellQueueOrder[moveTrial]) && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellQueueOrder[moveTrial])))) {
            // we can't move (adjacent + can move)
            moveTrial--;
          }
          if (moveTrial < 0) {
            break;
          }
        }
        if (currentPathIndex > wellQueueTargetIndex || currentPathIndex == -1) {
          while (moveTrial < 9 && !(Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellQueueOrder[moveTrial]) && rc.canMove(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellQueueOrder[moveTrial])))) {
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
    if (!rc.canSenseLocation(queuePosition)) {
      return true;
    }
    if (!rc.sensePassability(queuePosition)) {
      return false;
    }
    if (rc.canSenseRobotAtLocation(queuePosition)) {
      return false;
    }
    return true;
  }

  /**
   * will figure out the next best well to go to
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
      if (!wellLocation.equals(toAvoid)) {
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
    while (tryExplorationMove()) {
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
