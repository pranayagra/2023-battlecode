package pathingfixes.robots;

import pathingfixes.communications.CommsHandler;
import pathingfixes.communications.Communicator;
import pathingfixes.communications.HqMetaInfo;
import pathingfixes.containers.CharSet;
import pathingfixes.containers.HashMap;
import pathingfixes.knowledge.RunningMemory;
import pathingfixes.knowledge.WellData;
import pathingfixes.robots.micro.CarrierEnemyProtocol;
import pathingfixes.robots.micro.CarrierWellMicro;
import pathingfixes.robots.micro.CarrierWellPathing;
import pathingfixes.knowledge.Cache;
import pathingfixes.robots.micro.MicroConstants;
import pathingfixes.robots.pathfinding.BugNav;
import pathingfixes.utils.Global;
import pathingfixes.utils.Printer;
import pathingfixes.utils.Utils;
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
  private static final int MAX_SCOUT_TURNS = 50;
  private static final int MAX_TURNS_TO_LOOK_FOR_WELL = 100;
  private static final int MIN_TURN_TO_EXPLORE = 30;

  CarrierTask currentTask;
  ResourceType incrementedResource;
  CarrierTask forcedNextTask;
  CarrierTask HQAssignedTask;


  private int turnsStuckApproachingWell;

  private int closestDistToWell;
  private int turnsSinceCloseToWell;
//  private final Direction[] directions_storage = new Direction[8];
  private final HashMap<MapLocation, Direction> wellApproachDirection;
  private final CharSet blackListWells;
  private MapLocation[] wellQueueOrder;
  private MapLocation wellEntryPoint;
  private int wellQueueSize;

  int wellQueueTargetIndex;
  private int emptierRobotsSeen;
  private int fullerRobotsSeen;
  private int roundsWaitingForQueueSpot;

  public static int targetWellIndexToDecrement = -1;
  private static ResourceType targetWellTypeToDecrement;
  private boolean alreadyReportedFirstManaWell = false;


  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    CarrierEnemyProtocol.init(this);
    wellApproachDirection = new HashMap<>(3);
    blackListWells = new CharSet();
    wellQueueOrder = null;
    resetTask();
  }

  private void resetTask() throws GameActionException {
    CarrierTask previousTask = currentTask;
    currentTask = null;
    if (previousTask != null) {
      previousTask.onTaskEnd(this);
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
//    if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
//      // todo: read from comms
////      checkAssignedTask();
////      Printer.print("checked assign " + HQAssignedTask);
//      if (HQAssignedTask != null) return HQAssignedTask;
//    }

    if (rc.getAnchor() != null) {
      return CarrierTask.ANCHOR_ISLAND;
    }

    if (rc.getWeight() >= MAX_CARRYING_CAPACITY) {
      return CarrierTask.DELIVER_RSS_HOME;
    }

    MapLocation closestHQLoc = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    int closestHQID = HqMetaInfo.getClosestHQ(Cache.PerTurn.CURRENT_LOCATION);

    // reduce the last incremented
//    if (incrementedResource != null && rc.canWriteSharedArray(0, 0)) {
//      // decrement
//      switch (incrementedResource) {
//        case ADAMANTIUM:
//          CommsHandler.writeOurHqAdamantiumIncomeDecrement(closestHQID);
//          break;
//        case MANA:
//          CommsHandler.writeOurHqManaIncomeDecrement(closestHQID);
//          break;
//        case ELIXIR:
//          CommsHandler.writeOurHqElixirIncomeDecrement(closestHQID);
//          break;
//      }
//      incrementedResource = null;
//    }



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



    if (HQAssignedTask != null) {
      if (rc.canWriteSharedArray(0, 0)) {
        switch (HQAssignedTask) {
          case FETCH_ADAMANTIUM:
            CommsHandler.writeOurHqAdamantiumIncomeIncrement(closestHQID);
            incrementedResource = ResourceType.ADAMANTIUM;
            break;
          case FETCH_MANA:
            CommsHandler.writeOurHqManaIncomeIncrement(closestHQID);
            incrementedResource = ResourceType.MANA;
            break;
          case FETCH_ELIXIR:
            CommsHandler.writeOurHqElixirIncomeIncrement(closestHQID);
            incrementedResource = ResourceType.ELIXIR;
            break;
        }
      }
      return HQAssignedTask;
    }

    // we do well task
      CarrierTask wellTask = determineNewWellTask();
      //TODO: fix no well carrier tracking
      // currently a patch since if there are no wells found, the carrier won't update the well.
      // then we need to update the overall income for the first turn when no wells are found
      if (rc.canWriteSharedArray(0, 0)) {
        switch(wellTask.collectionType) {
          case ADAMANTIUM:
            CommsHandler.writeOurHqAdamantiumIncomeIncrement(closestHQID);
            break;
          case MANA:
            CommsHandler.writeOurHqManaIncomeIncrement(closestHQID);
            break;
          case ELIXIR:
            CommsHandler.writeOurHqElixirIncomeIncrement(closestHQID);
            break;
        }
        incrementedResource = wellTask.collectionType;

      }
      return wellTask;
    }

  private CarrierTask determineNewWellTask() throws GameActionException {
    //TODO: fix naming of income. this is now semantically the # of carriers out getting that resource type.
    // TODO: check for existence of Elixir wells
    int maxAdamantiumCarriersBeforeManaSaturation = 3;
    int singleAxisDistBetweenHQs = HqMetaInfo.getClosestSingleAxisDistBetweenOpposingHQs();
    // Printer.print("singleAxisDistBetweenHQs" + singleAxisDistBetweenHQs);

    if (singleAxisDistBetweenHQs < 20) {
      maxAdamantiumCarriersBeforeManaSaturation = 0;
    } else if (singleAxisDistBetweenHQs < 40) {
      maxAdamantiumCarriersBeforeManaSaturation = 1;
    } else if (singleAxisDistBetweenHQs < 60) {
      maxAdamantiumCarriersBeforeManaSaturation = 2;
    }
    int adamantiumCarriers = Communicator.getTotalCarriersMiningType(ResourceType.ADAMANTIUM);
    int manaCarriers = Communicator.getTotalCarriersMiningType(ResourceType.MANA);

//    if ((40 * adamantiumIncome) / 100 > 9) {
    if (adamantiumCarriers < maxAdamantiumCarriersBeforeManaSaturation) {
      // split 1:1
      if (manaCarriers < adamantiumCarriers) {
        return CarrierTask.FETCH_MANA;
      }
      if (adamantiumCarriers < manaCarriers) return CarrierTask.FETCH_ADAMANTIUM;
      // equals
      // TODO: this is a hot patch for when no wells are found. See return site in determineNewTask for better desc.
      // hot patch also needs to work when no AD well or w/e :(
      int closestHQID = HqMetaInfo.getClosestHQ(Cache.PerTurn.CURRENT_LOCATION);
      int manaIncome = CommsHandler.readOurHqManaIncome(closestHQID);
      int adamantiumIncome = CommsHandler.readOurHqAdamantiumIncome(closestHQID);
      if (manaIncome <= adamantiumIncome) {
        return CarrierTask.FETCH_MANA;
      }
      return CarrierTask.FETCH_ADAMANTIUM;
    }

    // now adamantiumCarriers are >= maxAdamantiumCarriersBeforeManaSaturation
    int saturatedManaWells = Communicator.numWellsSaturated(ResourceType.MANA);
    // Printer.print("satured mana wells: " + saturatedManaWells);
    if (saturatedManaWells == 0 ||
        (saturatedManaWells < HqMetaInfo.hqCount * 2 && saturatedManaWells < Communicator.numWellsOfType(ResourceType.MANA))) {

      // not mana saturated yet, get mana
      return CarrierTask.FETCH_MANA;
    }
    // Printer.print("yeeto im here");
    int saturatedAdWells = Communicator.numWellsSaturated(ResourceType.ADAMANTIUM);
    // saturate atleast 1 adamantium well per HQ
    if (saturatedAdWells == 0 ||
        (saturatedAdWells < HqMetaInfo.hqCount && saturatedAdWells < Communicator.numWellsOfType(ResourceType.ADAMANTIUM))) {
      return CarrierTask.FETCH_ADAMANTIUM;
    }

    // GOGOGO MANA WOO (default assuming all the above conditions are met).
    return CarrierTask.FETCH_MANA;

  }

  @Override
  protected void runTurn() throws GameActionException {
    //    if (Cache.PerTurn.ROUND_NUM == 400) {
//      rc.resign();
//    }
//    if (Cache.PerTurn.ROUND_NUM >= 10) rc.resign();

    CarrierEnemyProtocol.doProtocol();


    // run the current task until we fail to complete it (incomplete -> finish on next turn/later)
    while (currentTask != null && currentTask.execute(this)) {
      resetTask();
      if (Clock.getBytecodesLeft() <= 1000) {
        break;
      }
    }

    do_broadcast: if (CarrierEnemyProtocol.cachedLastEnemyForBroadcast != null) {
//      for (RobotInfo friendlyRobot : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
//        if (friendlyRobot.type == RobotType.LAUNCHER && friendlyRobot.location.isWithinDistanceSquared(CarrierEnemyProtocol.cachedLastEnemyForBroadcast.location, friendlyRobot.type.actionRadiusSquared)) {
//          CarrierEnemyProtocol.cachedLastEnemyForBroadcast = null;
//          CarrierEnemyProtocol.cachedLastEnemyForBroadcastRound = CarrierEnemyProtocol.MAX_BROADCAST_TURNS;
//          break do_broadcast;
//        }
//      }
      if (rc.canWriteSharedArray(0,0) && CarrierEnemyProtocol.cachedLastEnemyBroadcastCount++ < CarrierEnemyProtocol.MAX_BROADCAST_TURNS) {
        Communicator.writeEnemy(CarrierEnemyProtocol.cachedLastEnemyForBroadcast);
//        CarrierEnemyProtocol.cachedLastEnemyForBroadcast = null;
      }
    }

    tryCollectResource();
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
    MapLocation closestHqLoc = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    int distToClosestHq = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(closestHqLoc);
    if (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(currentTask.targetHQLoc, (int) (distToClosestHq*1.25))) {
      currentTask.targetHQLoc = closestHqLoc;
    }
    if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(currentTask.targetHQLoc)) {
      Printer.appendToIndicator("-move:" + currentTask.targetHQLoc);
      pathing.moveTowards(currentTask.targetHQLoc);
    }
    if (!rc.isActionReady()) {
      Printer.appendToIndicator("-stay:" + currentTask.targetHQLoc);
      pathing.goTowardsOrStayAtEmptiestLocationNextTo(currentTask.targetHQLoc);
    }
    return transferAllResources(currentTask.targetHQLoc);
  }

  private boolean executeReportInfo() throws GameActionException {
    if (!shouldReportToComms()) return true;
    if (rc.canWriteSharedArray(0, 0)) {
      afterTurnWhenMoved();
      return true;
    }
    // OW move towards HQ
    MapLocation closestHQ = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
    while(pathing.moveTowards(closestHQ)) {};
    return false;
  }

  /**
   * attempts to transfer all resources to the given target
   * @param targetLocation where to transfer to
   * @return true if all resources have been transferred
   * @throws GameActionException any issues with transferring
   */
  public boolean transferAllResources(MapLocation targetLocation) throws GameActionException {
    if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(targetLocation)) return false;
    for (ResourceType type : ResourceType.values()) {
      if (transferResource(targetLocation, type, rc.getResourceAmount(type)) && rc.getWeight() == 0) {
        return true;
      }
    }
    return rc.getWeight() == 0;
  }

  /**
   * Decision whether carrier should report what it knows to comms.
   * @return
   * @throws GameActionException
   */
  private boolean shouldReportToComms() throws GameActionException {
    if (alreadyReportedFirstManaWell) return false;
    if (Communicator.numWellsOfType(ResourceType.MANA) == 0 && RunningMemory.containsWellOfType(ResourceType.MANA)) {
      for (WellData wellData : RunningMemory.wells.values) {
        if (wellData == null) continue;
        if (wellData.type != ResourceType.MANA) continue;
        if(wellData.loc.isWithinDistanceSquared(HqMetaInfo.getClosestHqLocation(wellData.loc), 400))
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
    if (shouldReportToComms()) {
      forcedNextTask = CarrierTask.REPORT_INFO;
      return true;
    }
    no_well: if (currentTask.targetWell == null
        || (currentTask.turnsRunning % 20 == 0 && !currentTask.targetWell.isWithinDistanceSquared(Cache.PerTurn.CURRENT_LOCATION, 400))
        || (CarrierEnemyProtocol.lastEnemyLocation != null
        && currentTask.targetWell.isWithinDistanceSquared(CarrierEnemyProtocol.lastEnemyLocation, 26)
        && Cache.PerTurn.ROUND_NUM - CarrierEnemyProtocol.lastEnemyLocationRound <= 8)) {
      if ((CarrierEnemyProtocol.lastEnemyLocation != null
          && currentTask.targetWell != null
          && currentTask.targetWell.isWithinDistanceSquared(CarrierEnemyProtocol.lastEnemyLocation, 26)
          && Cache.PerTurn.ROUND_NUM - CarrierEnemyProtocol.lastEnemyLocationRound <= 8)) {
        findNewWell(currentTask.collectionType, currentTask.targetWell);
      } else {
        findNewWell(currentTask.collectionType, null);
      }
      if (currentTask.targetWell != null) break no_well;
      MapLocation wellLoc = exploreUntilFoundWell(resourceType);
      if (wellLoc != null) {
        setWell(wellLoc);
        break no_well;
      }
      if (currentTask.turnsRunning > MAX_TURNS_TO_LOOK_FOR_WELL) {
        forcedNextTask = CarrierTask.SCOUT;
        return true;
      }
      return false;
    }
    if (currentTask.targetWell == null) {
      assert false;
    }

    if (doWellCollection(currentTask.targetWell)) {
      return true; // we are full
    }
    if(!approachWell(currentTask.targetWell)) {
      blackListWells.add(currentTask.targetWell);
      switch (currentTask) {
        // currently, keep same rss. Before was to change.
        case FETCH_ADAMANTIUM:
//          forcedNextTask = CarrierTask.FETCH_MANA;
          forcedNextTask = CarrierTask.FETCH_ADAMANTIUM;

        default: // TODO: check if elixir well exists and cycle to that
        case FETCH_MANA:
//          forcedNextTask = CarrierTask.FETCH_ADAMANTIUM;
          forcedNextTask = CarrierTask.FETCH_MANA;
      }
      return true;
    }
    return currentTask.targetWell != null && doWellCollection(currentTask.targetWell);
  }

  /**
   * explores until a nearby well is found
   * @param resourceType the resource type to look for
   * @return the location of the well, or null if none found
   * @throws GameActionException any issues with exploration moves / sensing
   */
  private MapLocation exploreUntilFoundWell(ResourceType resourceType) throws GameActionException {
    do {
      WellInfo[] nearby = rc.senseNearbyWells(resourceType);
      MapLocation foundWell = null;
      int distance = Integer.MAX_VALUE;
      for (int i = nearby.length; --i >= 0;) {
        WellInfo well = nearby[i];
        MapLocation wellLoc = well.getMapLocation();
        if (blackListWells.contains(wellLoc)) continue;
        if (Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(well.getMapLocation()) < distance) {
          foundWell = wellLoc;
        }
      }
      if (foundWell != null) {
        return foundWell;
      }
    } while (tryExplorationMove());
    return null;
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
//      if (currentTask.targetIsland == null) {
      currentTask.targetIsland = findIslandLocationToClaim();
//      }
      if (currentTask.targetIsland != null && currentTask.turnsRunning > Math.max(50, Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, currentTask.targetIsland.islandLocation) * MicroConstants.TURNS_SCALAR_TO_GIVE_UP_ON_TARGET_APPROACH)) {
        forcedNextTask = CarrierTask.ANCHOR_ISLAND;
        return true;
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
      Printer.appendToIndicator("noQ->" + wellLocation + "d=" + Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation));
//      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, wellLocation, 0, 255, 0);
      while (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, SET_WELL_PATH_DISTANCE)) {
        if (pathing.moveTowards(wellLocation)) {
//          Printer.print("moved towards well: " + targetWell + " now=" + Cache.PerTurn.CURRENT_LOCATION);//, "new dir back: " + wellApproachDirection.get(targetWell));
//        wellApproachDirection.setAlreadyContainedValue(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
          Printer.appendToIndicator("moved:" + Cache.PerTurn.CURRENT_LOCATION);
        } else {
          if (rc.isMovementReady()) {
            turnsStuckApproachingWell++;
            Printer.appendToIndicator("couldn't move" + turnsStuckApproachingWell);
            if (turnsStuckApproachingWell >= MAX_TURNS_STUCK) {
              return false;
            }
          }
          break;
        }
      }


      if (rc.getWeight() <= 4) {
        int distToWell = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, wellLocation);
        if (distToWell < closestDistToWell) {
          closestDistToWell = distToWell;
          turnsSinceCloseToWell = 0;
        } else if (distToWell >= 4) {
          if (++turnsSinceCloseToWell >= Math.max(100, closestDistToWell * MicroConstants.TURNS_SCALAR_TO_GIVE_UP_ON_TARGET_APPROACH)) {
            // we've been stuck for a while, give up
            /*BASICBOT_ONLY*///Printer.print("giving up on well: " + wellLocation + " dist=" + distToWell + " closest=" + closestDistToWell + " turns=" + turnsSinceCloseToWell);
            findNewWell(currentTask.collectionType, wellLocation);
//          return false;
          }
        }
      }

      if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellLocation, SET_WELL_PATH_DISTANCE)) {
        wellApproachDirection.put(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
        wellQueueOrder = CarrierWellPathing.getPathForWell(wellLocation, wellApproachDirection.get(wellLocation));
        WellData wellData = RunningMemory.wells.get(wellLocation);
        if (wellData == null) {
          wellData = new WellData(wellLocation, currentTask.collectionType, false, 0);
          wellData.dirty = true;
          RunningMemory.wells.put(wellLocation, wellData);
        }
        boolean wellEntryUndetermined = true;
        wellEntryPoint = wellQueueOrder[0];
        if (wellData.capacity == 0) {
          wellQueueSize = 0;
          for (int i = wellQueueOrder.length; --i >= 0;) {
            if (CarrierWellMicro.isValidStaticQueuePosition(wellLocation, wellQueueOrder[i])) {
              wellQueueSize++;
              if (wellEntryUndetermined) {
                RobotInfo robot = rc.canSenseRobotAtLocation(wellQueueOrder[i]) ? rc.senseRobotAtLocation(wellQueueOrder[i]) : null;
                if (robot == null || robot.type != RobotType.HEADQUARTERS) {
                  wellEntryPoint = wellQueueOrder[i];
                  wellEntryUndetermined = false;
                }
              }
            }
          }
          wellData.capacity = wellQueueSize;
          wellData.dirty = true;
        } else {
          wellQueueSize = wellData.capacity;
          for (int i = wellQueueOrder.length; --i >= 0;) {
            if (CarrierWellMicro.isValidQueuePosition(wellLocation, wellQueueOrder[i])) {
              wellEntryPoint = wellQueueOrder[i];
              wellEntryUndetermined = false;
              break;
            }
          }
        }

        if (wellEntryUndetermined) {
          Printer.appendToIndicator("no entry@" + wellLocation);
          wellEntryPoint = wellQueueOrder[0];
//          Printer.print("couldn't find well entry for: " + wellLocation, "well queue: " + Arrays.toString(wellQueueOrder));
          return false;
        }
//        Printer.print("well queue: " + Arrays.toString(wellQueueOrder), "from direction: " + wellApproachDirection.get(wellLocation));
        Printer.appendToIndicator("setQ:" + Cache.PerTurn.CURRENT_LOCATION + "->" + wellLocation);
      }
    }
    if (wellQueueOrder != null) {
      Printer.appendToIndicator("followQ:" + wellLocation);
      if (!followWellQueue(wellLocation)) return false;
//      } else {
//        Printer.print("Cannot get to well! -- canMove=" + rc.isMovementReady());
    }
    return true;
  }

  /**
   * will follow the collection quuee of the well (circle around the well)
   * will move along queue depending on robots seen
   * ASSUMES - within 2x2 of well
   * @return boolean. false is failed and should try other well. true if good :)
   */
  private boolean followWellQueue(MapLocation wellLocation) throws GameActionException {
    updateRobotsSeenInQueue(wellLocation);
    updateWellQueueTarget();

    Direction dirToWell = Cache.PerTurn.CURRENT_LOCATION.directionTo(wellLocation);
    int distanceToWell = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation);
    if (Utils.DSQ_1by1 < distanceToWell && distanceToWell < Cache.Permanent.VISION_RADIUS_SQUARED
        && rc.canSenseLocation(wellLocation)
        && rc.canSenseLocation(wellLocation.add(dirToWell))
        && rc.canSenseLocation(wellLocation.add(dirToWell.rotateLeft()))
        && rc.canSenseLocation(wellLocation.add(dirToWell.rotateRight()))) { // we're not adjacent but can sense all around
      int numCarriersFarFromFull = 0;
      for (RobotInfo friendly : Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS) {
        if (friendly.type == RobotType.CARRIER && friendly.location.isAdjacentTo(wellLocation)) {
          int friendlyAmount = Utils.getInvWeight(friendly);
          if (friendlyAmount < FAR_FROM_FULL_CAPACITY) {
            numCarriersFarFromFull++;
          }
        }
      }

      if (numCarriersFarFromFull > wellQueueSize - MIN_SPOTS_LEFT_FROM_CARRIERS_FILLING_IN_FRONT) {
        Printer.appendToIndicator("there's " + numCarriersFarFromFull + "/" + wellQueueSize + " far from full so ima dip");
        return false;
//          findNewWell(currentTask.collectionType, currentTask.targetWell);
      }
    }

    no_queue_spot: if (wellQueueTargetIndex == -1) { // no spot in queue
      if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation) && rc.getWeight() < MAX_CARRYING_CAPACITY) { // we're adjacent and below capacity but have no spot in line
        Printer.print("no queue spot but adjacent to well");
        Printer.print("wellQueueOrder=" + Arrays.toString(wellQueueOrder));
        Printer.print("wellQueueTargetIndex=" + wellQueueTargetIndex);
        Printer.print("emptier=" + emptierRobotsSeen);
        Printer.print("fuller=" + fullerRobotsSeen);
        throw new RuntimeException("should have a well spot if already adjacent");
      }
      // we aren't there yet, so consider switching wells
      roundsWaitingForQueueSpot++;
      if (roundsWaitingForQueueSpot > MAX_ROUNDS_WAIT_FOR_WELL_PATH) {
        Printer.appendToIndicator("waiting too long (" + roundsWaitingForQueueSpot + "/" + MAX_ROUNDS_WAIT_FOR_WELL_PATH + ") for well@" + wellLocation + " so ima dip");
//            findNewWell(currentTask.collectionType, currentTask.targetWell);
        return false;
      } else {
        do {
          if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellEntryPoint)) {
            if (pathing.move(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellEntryPoint))) {
              Printer.appendToIndicator("moved towards well entry point");
            }
          }
        } while (pathing.goTowardsOrStayAtEmptiestLocationNextTo(wellEntryPoint));
        Printer.appendToIndicator("waiting @ well=" + wellLocation + ".entry@" + wellEntryPoint + "-turn#" + roundsWaitingForQueueSpot + " -emp=" + emptierRobotsSeen + "-ful=" + fullerRobotsSeen);
//            rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, wellEntryPoint, 0, 255, 0);
      }
    }

    updateWellQueueTarget();

    if (wellQueueTargetIndex != -1) {  // we have a spot in the queue (wellQueueTargetIndex)
      Printer.appendToIndicator("well queue target position: " + wellQueueOrder[wellQueueTargetIndex]);
//      rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, wellQueueOrder[wellQueueTargetIndex], 0, 255, 0);
      while (rc.isMovementReady() && !Cache.PerTurn.CURRENT_LOCATION.equals(wellQueueOrder[wellQueueTargetIndex])) {
        // not yet in the queue, just go to the entry point
        not_in_queue: if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation)) {
          Printer.appendToIndicator("approaching well (" + wellLocation + ") via: " + wellEntryPoint);
          while (pathing.moveTowards(wellEntryPoint)) {}
          if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation)) {
            break not_in_queue;
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
            Printer.appendToIndicator("entry for " + wellLocation + " blocked (" + turnsStuckApproachingWell + " turns)-- new entry via " + wellEntryPoint);
            if (turnsStuckApproachingWell >= MAX_TURNS_STUCK) {
              findNewWell(currentTask.collectionType, currentTask.targetWell);
              if (currentTask.targetWell != null) {
                return approachWell(currentTask.targetWell);
//                return true;
              }
            }
          }
          break;
        }

        // in the queue, so try to get towards the correct point by navigating through the queue
        int currentPathIndex = -1;
        for (int i = 0; i < wellQueueOrder.length; i++) {
          if (Cache.PerTurn.CURRENT_LOCATION.equals(wellQueueOrder[i])) {
            currentPathIndex = i;
            break;
          }
        }
        Printer.appendToIndicator("moving in well queue: " + wellQueueTargetIndex + "=" + wellQueueOrder[wellQueueTargetIndex] + " -- currently at ind=" + currentPathIndex + "=" + wellQueueOrder[currentPathIndex]);

////        Printer.print("following queue: " + Cache.PerTurn.CURRENT_LOCATION + "|" + rc.getLocation() + " --> " + wellPathToFollow[moveTrial]);
////        Printer.print("following queue: ->" + wellPathToFollow[moveTrial]);
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
//        Printer.print("following queue: " + Cache.PerTurn.CURRENT_LOCATION + "|" + rc.getLocation() + " --> " + wellPathToFollow[moveTrial]);
//        Printer.print("following queue: ->" + wellPathToFollow[moveTrial]);
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
        int friendlyAmount = Utils.getInvWeight(friendly);
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
      MapLocation queueTarget = wellQueueOrder[testSpot];
      if (queueTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || CarrierWellMicro.isValidQueuePosition(currentTask.targetWell, queueTarget)) {
        // we can move to the target (or already there)
        wellQueueTargetIndex = testSpot;
        roundsWaitingForQueueSpot = 0;
        return;
//      } else {
//        Printer.print("can't move from=" + Cache.PerTurn.CURRENT_LOCATION + ".to=" + queueTarget + " sense=" + rc.canSenseLocation(queueTarget) + ".bot=" + rc.canSenseRobotAtLocation(queueTarget));
      }
    }
    while (maxFillSpot >= 0) {
      MapLocation queueTarget = wellQueueOrder[maxFillSpot];
      if (queueTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || CarrierWellMicro.isValidQueuePosition(currentTask.targetWell, queueTarget)) {
        // we can move to the target (or already there)
        wellQueueTargetIndex = maxFillSpot;
        roundsWaitingForQueueSpot = 0;
        return;
//      } else {
//        Printer.print("can't move from=" + Cache.PerTurn.CURRENT_LOCATION + ".to=" + queueTarget + " sense=" + rc.canSenseLocation(queueTarget) + ".bot=" + rc.canSenseRobotAtLocation(queueTarget));
      }
      maxFillSpot--;
    }
//    while (minFillSpot < 9) {
//      MapLocation queueTarget = wellQueueOrder[minFillSpot];
//      if (queueTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || CarrierWellMicro.isValidQueuePosition(currentTask.targetWell, queueTarget)) {
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
//      MapLocation queueTarget = wellPathToFollow[i];
//      // already there or can move there
//      if (queueTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || (rc.canSenseLocation(queueTarget) && !rc.canSenseRobotAtLocation(queueTarget))) {
//        wellPathTargetIndex = i;
//        if (i >= emptierRobotsSeen) {
//          return;
//        }
//      }
//    }
  }

  /**
   * will figure out the next best well to go to (respects blackListWells)
   * avoids saturated wells.
   * Sets alot of state variables.
   * @param resourceType the well type to look for
   * @param toAvoid a map location to exclude from the search
   * @throws GameActionException any issues duirng search (comms, sensing)
   */
  private void findNewWell(ResourceType resourceType, MapLocation toAvoid) throws GameActionException {
    CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(resourceType);
    MapLocation closestWellLocation = null;
    if (rc.canWriteSharedArray(0,0) && targetWellIndexToDecrement != -1) {
      CommsHandler.ResourceTypeReaderWriter decrementWriter = CommsHandler.ResourceTypeReaderWriter.fromResourceType(targetWellTypeToDecrement);
      decrementWriter.writeWellCurrentWorkersDecrement(targetWellIndexToDecrement);
      targetWellIndexToDecrement = -1;
    }
    int closestDist = Integer.MAX_VALUE;
    int closestWellInd = -1;
    for (int i = 0; i < CommsHandler.ADAMANTIUM_WELL_SLOTS; i++) {
      if (!writer.readWellExists(i)) continue;
      MapLocation wellLocation = writer.readWellLocation(i);
      if (wellLocation.equals(toAvoid) || blackListWells.contains(wellLocation)) continue;
//      if (writer.readWellCapacity(i) <= writer.readWellCurrentWorkers(i)) continue;
      MapLocation loc = writer.readWellLocation(i);
      int extraCapacity = Utils.maxCarriersPerWell(writer.readWellCapacity(i), Utils.maxSingleAxisDist(HqMetaInfo.getClosestHqLocation(loc), loc));
      if (writer.readWellCurrentWorkers(i) >= extraCapacity) continue;
      if (CarrierEnemyProtocol.lastEnemyLocation != null && wellLocation.isWithinDistanceSquared(CarrierEnemyProtocol.lastEnemyLocation, 26) && Cache.PerTurn.ROUND_NUM - CarrierEnemyProtocol.lastEnemyLocationRound <= 7) {
        continue;
      }

      int dist = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(wellLocation);
      if (dist < closestDist) {
        closestDist = dist;
        closestWellLocation = wellLocation;
        closestWellInd = i;
      }
    }
    if (rc.canWriteSharedArray(0,0)) {
      writer.writeWellCurrentWorkersIncrement(closestWellInd);
      targetWellIndexToDecrement = closestWellInd;
      targetWellTypeToDecrement = resourceType;
//    } else if (closestWellLocation != null){
//      Printer.print("Could not increment!!" + closestWellLocation + resourceType);
    }

    setWell(closestWellLocation);
  }

  private void setWell(MapLocation wellLocation) {
    currentTask.targetWell = wellLocation;

    this.turnsStuckApproachingWell = 0;
    this.closestDistToWell = wellLocation != null ? Utils.maxSingleAxisDist(wellLocation, Cache.PerTurn.CURRENT_LOCATION) : Integer.MAX_VALUE;
    this.turnsSinceCloseToWell = 0;

    this.wellQueueOrder = null;
    this.wellEntryPoint = null;
    this.wellQueueSize = 0;

    this.wellQueueTargetIndex = -1;
    this.emptierRobotsSeen = 0;
    this.fullerRobotsSeen = 0;

    this.roundsWaitingForQueueSpot = 0;

    if (wellLocation != null) {
      Direction dirBackFromWell;// = closestWellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION);
      MapLocation targetHQ = HqMetaInfo.getClosestHqLocation(wellLocation);
      if (targetHQ != null && targetHQ.isWithinDistanceSquared(wellLocation, RobotType.HEADQUARTERS.visionRadiusSquared)) {
//        Printer.print("close to HQ, use hq dir - hq=" + targetHQ + " -- well=" + closestWellLocation + " -- dir=" + closestWellLocation.directionTo(targetHQ));
        dirBackFromWell = wellLocation.directionTo(targetHQ);
        this.wellApproachDirection.put(wellLocation, dirBackFromWell);
      }
    }
  }

  /**
   * will explore around until an island location is found
   * @return the found location of the island
   * @throws GameActionException any errors while sensing
   */
  private IslandInfo findIslandLocationToClaim() throws GameActionException {
    // go to unclaimed island
    IslandInfo islandToClaim = getClosestUnclaimedIsland();
    if (islandToClaim == null) {
      islandToClaim = getClosestEnemyIsland();
//      if (islandToClaim == null) {
//        islandToClaim = getClosestFriendlyIsland();
//        if (islandToClaim != null) {
//          return new IslandInfo(Utils.applySymmetry(islandToClaim.islandLocation, RunningMemory.guessedSymmetry), islandToClaim.islandId, -1, Team.NEUTRAL);
//        }
//      }
    }
    if (islandToClaim == null) {
      // explore for island
      while (doIslandFindingMove()) {
        int[] nearbyIslands = rc.senseNearbyIslands();
        if (nearbyIslands.length > 0) {
          MapLocation closestUnclaimedIsland = null;
          int closestIslandID = -1;
          int closestDistance = Integer.MAX_VALUE;
          for (int islandID : nearbyIslands) {
            if (rc.senseTeamOccupyingIsland(islandID) == Team.NEUTRAL) {
              MapLocation islandLocation = rc.senseNearbyIslandLocations(islandID)[0];
              int candidateDistance = Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, islandLocation);
              if (candidateDistance < closestDistance) {
                closestUnclaimedIsland = islandLocation;
                closestIslandID = islandID;
                closestDistance = candidateDistance;
              }
            }
          }
          if (closestUnclaimedIsland != null) {
            islandToClaim = new IslandInfo(closestUnclaimedIsland, closestIslandID, Cache.PerTurn.ROUND_NUM, Team.NEUTRAL);
            break;
          }
        }
      }
    }
    if (islandToClaim != null) {
      islandToClaim.updateLocationToClosestOpenLocation(Cache.PerTurn.CURRENT_LOCATION);
      return islandToClaim;
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
    IslandInfo islandToClaim = currentTask.targetIsland;
    if (islandToClaim == null) return false;

    MapLocation islandLocationToClaim = islandToClaim.islandLocation;
    Printer.appendToIndicator("attempt claim island: " + islandLocationToClaim + " -- trying for " + currentTask.turnsRunning + " turns");

    // someone else claimed it while we were moving to the unclaimed island
    if (rc.canSenseLocation(islandLocationToClaim)) {
      Team occupyingTeam = rc.senseTeamOccupyingIsland(rc.senseIsland(islandLocationToClaim));
      if (occupyingTeam != Team.NEUTRAL) {
        localIslandInfo[islandToClaim.islandId].updateTeam(occupyingTeam);
        islandToClaim = findIslandLocationToClaim();
        if (islandToClaim == null) return false;
        currentTask.targetIsland = islandToClaim;
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
    FETCH_ADAMANTIUM(ResourceType.ADAMANTIUM, "Ad"),
    FETCH_MANA(ResourceType.MANA, "Ma"),
    FETCH_ELIXIR(ResourceType.ELIXIR, "El"),
    DELIVER_RSS_HOME(ResourceType.NO_RESOURCE, "Home"),
    ANCHOR_ISLAND(ResourceType.NO_RESOURCE, "Anchor"),
    SCOUT(ResourceType.NO_RESOURCE, "Scout"),
    REPORT_INFO(ResourceType.NO_RESOURCE, "Report"),
    ATTACK(ResourceType.NO_RESOURCE, "Atk");

    public MapLocation targetHQLoc;
    public MapLocation targetWell;
    // index in public comms array - TODO: this can be handled with a map in Communicator potentially
    final public ResourceType collectionType;
    public IslandInfo targetIsland;
    public int turnsRunning;
    private String name;

    CarrierTask(ResourceType resourceType, String name) {
      collectionType = resourceType;
      this.name = name;
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
        case REPORT_INFO:
          carrier.alreadyReportedFirstManaWell = true;
          break;
      }
    }

    public void onTaskEnd(Carrier carrier) throws GameActionException {
      if (Global.rc.canWriteSharedArray(0,0) && targetWellIndexToDecrement != -1) {
        CommsHandler.ResourceTypeReaderWriter writer = CommsHandler.ResourceTypeReaderWriter.fromResourceType(targetWellTypeToDecrement);
        writer.writeWellCurrentWorkersDecrement(targetWellIndexToDecrement);
        targetWellIndexToDecrement = -1;
      }
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
        case REPORT_INFO:
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
      Printer.appendToIndicator("task=" + this.name + ":" + turnsRunning + "R");
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
        case REPORT_INFO:
          return carrier.executeReportInfo();
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

  /**
   * tries to collect from any adjacent well
   * @return true if collected
   * @throws GameActionException
   */
  private boolean tryCollectResource() throws GameActionException {
    return collectResource(Cache.PerTurn.CURRENT_LOCATION, -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.NORTH), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.SOUTH), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.EAST), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.WEST), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.NORTHEAST), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.NORTHWEST), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.SOUTHEAST), -1)
        || collectResource(Cache.PerTurn.CURRENT_LOCATION.add(Direction.SOUTHWEST), -1);
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
