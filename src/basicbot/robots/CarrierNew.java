package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.containers.HashMap;
import basicbot.containers.HashMapNodeVal;
import basicbot.containers.HashSet;
import basicbot.knowledge.RunningMemory;
import basicbot.robots.micro.CarrierWellPathing;
import basicbot.knowledge.Cache;
import basicbot.robots.pathfinding.BugNav;
import basicbot.utils.Printer;
import basicbot.utils.Utils;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarrierNew extends MobileRobot {

    private static final int MAX_CARRYING_CAPACITY = GameConstants.CARRIER_CAPACITY;

    CarrierTask currentTask;
    CarrierTask forcedNextTask;


    int fleeingCounter;

    /*
    HQ BEHAVIOR
    -- WELL CHECK
    if < 50 rounds:
        * check what wells exist in my knowledge
        * if both wells are close to me or no wells are close (say <10 units AD, <20 units mana), then spawn 2:2 carriers
        * if only one well is close, spawn all 4 of that type
        * keep track of how many are spawned and try to maintain a 1:1 ratio? (for now)
    else:
        * check what wells exist in my knowledge
        * assign new carriers on spawn the closest well of that type that has space (must comm); if no well, then no target is set and carrier behavior takes over
    * spawn towards the target well if exists

    -- COMMS
    * carriers will come in and comm the well location and type (and num 3x3 miners if applicable)
    * to solve race conditions, a miner can only write if the comm bits are all 0. It is the last HQ job to clear the comm bits

    CARRIER BEHAVIOR
    -- SPAWN - DONE
    * read HQ message to learn role in game (and location to go to if exists) - my location, role, target, readHQTask()
    * if location exists, go to location
    * if no location, then explore (for AD well do 10x10 box, for mana well do 20x20 box, and maybe do for 100 rounds, and then random target)

    -- ENEMY - DONE
    * if see enemy, then either flee or attack
    * for fleeing, run 6 times towards HQ (maybe comm)
    * for attack, if >=20 resources, attack, then flee

    -- COMMS - DONE
    * as exploring, store in local data structure the well location + types
    * after comming, add to a permanent data structure and remove from local data structure (so that we don't comm the same well twice)
    * if something is written there, then we cannot currently comm. HQ will clear it and then we can comm
    comm: well location, well type, and average num miners in the 3x3 while mining

    -- RETURN TO HQ - DONE
    * return to HQ and deposit resources until empty weight

    -- MINING MICRO - DONE
    * find empty spot in well
    * move randomly in any 9 spots if possible

    -- MINING MACRO - DONE
    * if HQ gave target on spawn, go to it
    * if HQ gave role on spawn but no target, set this as task and explore "randomly" (for AD well do 10x10 box around HQ)
    * if HQ gave no role, rng (no need to implement)

    --


     */

    public CarrierNew(RobotController rc) throws GameActionException {
        super(rc);
        readHQTask();
        Printer.print("CarrierNew: " + HQAssignedTask + " " + HQAssignedTaskLocation);
        if (HQAssignedTask == null) {
            assignRandomTask();
        }
        if (wellMiningFrom == null) wellMiningFrom = HQAssignedTaskLocation;
    }

    private CarrierTask HQAssignedTask;
    private MapLocation HQAssignedTaskLocation;
    private void assignRandomTask() {
        double pAD = Cache.Permanent.MAP_AREA / (60.0*60.0) * Math.exp(-0.01*Cache.PerTurn.ROUND_NUM);
        if (Cache.PerTurn.ROUND_NUM >= 10) {
            pAD = Math.max(0.75, pAD);
            pAD = Math.min(0.25, pAD);
        }

        if (Utils.rng.nextDouble() < pAD) {
            HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
        } else {
            HQAssignedTask = CarrierTask.FETCH_MANA;
        }
    }
    private void readHQTask() throws GameActionException {
        //todo: read HQ comm messages and find which one is applicable to me
        //todo: get task and location
        HQAssignedTask = null;
        HQAssignedTaskLocation = null;
        int readThrough = 8;
        if (Cache.PerTurn.ROUND_NUM % 2 == 0) {
            for (int i = 0; i < readThrough; ++i) {
                if (CommsHandler.readPranayOurHqEvenSpawnExists(i)) {
                    MapLocation spawnLocation = CommsHandler.readPranayOurHqEvenSpawnLocation(i);
                    if (!Cache.PerTurn.CURRENT_LOCATION.equals(spawnLocation)) continue;
                    int instruction = CommsHandler.readPranayOurHqEvenSpawnInstruction(i);
                    if (CommsHandler.readPranayOurHqEvenTargetExists(i)) {
                        HQAssignedTaskLocation = CommsHandler.readPranayOurHqEvenTargetLocation(i);
                    }
                    switch (instruction) {
                        case 1:
                            HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
                            return;
                        case 2:
                            HQAssignedTask = CarrierTask.FETCH_MANA;
                            return;
                    }
                }
            }
        } else {
            for (int i = 0; i < readThrough; ++i) {
                if (CommsHandler.readPranayOurHqOddSpawnExists(i)) {
                    MapLocation spawnLocation = CommsHandler.readPranayOurHqOddSpawnLocation(i);
                    if (!Cache.PerTurn.CURRENT_LOCATION.equals(spawnLocation)) continue;
                    int instruction = CommsHandler.readPranayOurHqOddSpawnInstruction(i);
                    if (CommsHandler.readPranayOurHqOddTargetExists(i)) {
                        HQAssignedTaskLocation = CommsHandler.readPranayOurHqOddTargetLocation(i);
                    }
                    switch (instruction) {
                        case 1:
                            HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
                            return;
                        case 2:
                            HQAssignedTask = CarrierTask.FETCH_MANA;
                            return;
                    }
                }
            }
        }
    }

    private MapLocation wellMiningFrom;
    private void miningMicroMovement() throws GameActionException {
        // can only micro move if we are at the target well and close enough and have space
        if (wellMiningFrom == null) return;
        if (!rc.isMovementReady()) return;
        if (rc.getWeight() >= 39) return;
        if (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellMiningFrom, 2)) return;

        Printer.appendToIndicator(" micro_move");
        do {
            List<Direction> dirs = new ArrayList<>(8);
            for (Direction dir : Utils.directions) {
                MapLocation loc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
                if (!loc.isWithinDistanceSquared(wellMiningFrom, 2)) continue;
                if (rc.canMove(dir)) {
                    dirs.add(dir);
                    break;
                }
            }
            if (dirs.size() == 0) return;
            Direction dirToMove = dirs.get(Utils.rng.nextInt(dirs.size()));
            pathing.move(dirToMove);
        } while (rc.isMovementReady());
    }


    /** WELL DATA STORING PROTOCOL **/
    class WellData {
        ResourceType type;
        int numMiners;
        int numRounds;

        public String toString() {
            return "{type: " + type + " numMiners: " + numMiners + " numRounds: " + numRounds + "}";
        }
    }

    //todo: add another map that reads HQ info and stores what wells the HQ already knows about
    private java.util.HashSet<MapLocation> allKnownWells = new java.util.HashSet<>(); //used to filter out what data we try to comm to HQ (so we don't comm the same well twice)
    private java.util.HashMap<MapLocation, WellData> temporaryWellDataMap = new java.util.HashMap<>(); //clears after we comm to HQ
    private void specialWellData(MapLocation wellLoc) throws GameActionException {
        MapLocation[] allMiningLocations = rc.getAllLocationsWithinRadiusSquared(wellLoc, 2);
        int numMiners = 0;
        int numSensedLocs = 0;
        for (MapLocation loc : allMiningLocations) {
            if (rc.canSenseLocation(loc)) {
                ++numSensedLocs;
                RobotInfo robotInfo = rc.senseRobotAtLocation(loc);
                if (robotInfo != null && robotInfo.team == Cache.Permanent.OUR_TEAM && robotInfo.type == RobotType.CARRIER) {
                    ++numMiners;
                }
            }
        }

//        if (rc.getID() == 11883) {
//            WellData wellData = temporaryWellDataMap.get(wellLoc);
//            Printer.print("wellData: " + wellData);
//            Printer.print("numSensedLocs: " + numSensedLocs + " numMiners: " + numMiners);
//        }

        if (numSensedLocs == 9) {
            // we can update the well data
            WellData wellData = temporaryWellDataMap.get(wellLoc);
            wellData.numMiners += numMiners;
            wellData.numRounds++;
        }
//        if (rc.getID() == 11883) {
//            WellData wellData = temporaryWellDataMap.get(wellLoc);
//            Printer.print("wellData: " + wellData);
//            Printer.print("numSensedLocs: " + numSensedLocs + " numMiners: " + numMiners);
//        }
    }

    //call this method every round
    private void storeWellData() throws GameActionException {
        WellInfo[] wellInfos = rc.senseNearbyWells();
        for (WellInfo wellInfo : wellInfos) {
            MapLocation wellLoc = wellInfo.getMapLocation();
            if (!wellLoc.equals(wellMiningFrom) && allKnownWells.contains(wellLoc)) {
                temporaryWellDataMap.remove(wellLoc);
                continue;
            }
            if (!temporaryWellDataMap.containsKey(wellLoc)) {
                WellData wellData = new WellData();
                wellData.type = wellInfo.getResourceType();
                temporaryWellDataMap.put(wellLoc, wellData);
            }

            if (wellLoc.equals(wellMiningFrom)) {
                // the special well... (store updated values)
                specialWellData(wellLoc);
            }
        }
    }

    private void updateAllKnownWells() throws GameActionException {
        //todo: read carrier-well comm messages and see which wells HQ knows about already
        // for each message, check location and add to allKnownWells
        for (int i = 0; i < 4; ++i) {
            if (CommsHandler.readPranayWellInfoExists(i)) {
                MapLocation wellLoc = CommsHandler.readPranayWellInfoLocation(i);
                allKnownWells.add(wellLoc);
            }
        }
    }

    private boolean wellDataProtocol() throws GameActionException {

        updateAllKnownWells();
        storeWellData();

        MapLocation locWrote = null;
        if (rc.canWriteSharedArray(0,0)) {
            int idx = -1;

            if (!CommsHandler.readPranayWellInfoExists(0)) idx = 0;
            else if (!CommsHandler.readPranayWellInfoExists(1)) idx = 1;
            else if (!CommsHandler.readPranayWellInfoExists(2)) idx = 2;
            else if (!CommsHandler.readPranayWellInfoExists(3)) idx = 3;

            if (idx == -1) return false;

            for (MapLocation wellLoc : temporaryWellDataMap.keySet()) {
                if (wellLoc.equals(wellMiningFrom)) {
                    WellData wellData = temporaryWellDataMap.get(wellLoc);
                    if (wellData.numRounds >= 20) {
                        // todo: create comm structure and write
                        locWrote = wellLoc;
                        int numMiners = Math.min(wellData.numMiners / wellData.numRounds, 8);
                        ResourceType type = wellData.type;
                        CommsHandler.writePranayWellInfoLocation(idx, wellLoc);
                        CommsHandler.writePranayWellInfoType(idx, type.resourceID);
                        CommsHandler.writePranayWellInfoNumMiners(idx, numMiners);
                    }
                } else if (!allKnownWells.contains(wellLoc)) {
                    WellData wellData = temporaryWellDataMap.get(wellLoc);
                    // todo: create comm structure and write
                    locWrote = wellLoc;
                    ResourceType type = wellData.type; //12+1+3
                    CommsHandler.writePranayWellInfoLocation(idx, wellLoc);
                    CommsHandler.writePranayWellInfoType(idx, type.resourceID);
                }
            }
        }

        if (locWrote != null) {
//            if (rc.getID() == 11883) Printer.print("writing well info", "well=" + locWrote, " data=" + temporaryWellDataMap.get(locWrote));
            temporaryWellDataMap.remove(locWrote);
            allKnownWells.add(locWrote);
            return true;
        }

        return false;
    }

    private void moveToTargetWell() throws GameActionException {
        if (wellMiningFrom == null) return;
        if (!rc.isMovementReady()) return;
        if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellMiningFrom, 2)) return;
        if (rc.getWeight() >= 39) return;
        Printer.appendToIndicator(" move_to_well");
        pathing.moveTowards(wellMiningFrom);
        pathing.moveTowards(wellMiningFrom);
    }

    /**ENEMY PROTOCOL STUFF**/
    private void fleeFromEnemy() throws GameActionException {
        if (fleeingCounter > 0) {
            Printer.appendToIndicator(" flee=" + fleeingCounter);
            //todo: HQ location instead of start location
            while (rc.isMovementReady() && --fleeingCounter >= 0 && pathing.moveTowards(Cache.Permanent.START_LOCATION)) {}
            while (rc.isMovementReady() && --fleeingCounter >= 0 && pathing.moveTowards(closestHQLocation)) {}
        }
    }

    private void fightEnemy() throws GameActionException {
        if (rc.getWeight() < 20) return;
        MapLocation locToAttack = null;
        int distance = Integer.MAX_VALUE;
        for (RobotInfo robotInfo : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
            if (robotInfo.type == RobotType.LAUNCHER || robotInfo.type == RobotType.DESTABILIZER) {
                int candidateDistance = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(robotInfo.location);
                if (candidateDistance < distance) {
                    locToAttack = robotInfo.location;
                    distance = candidateDistance;
                }
            }
        }

        if (distance > 16) return;
        Printer.appendToIndicator(" attack=" + locToAttack);
        if (Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(locToAttack) > Cache.Permanent.ACTION_RADIUS_SQUARED) {
            if (rc.isMovementReady()) {
                pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(locToAttack));
                pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(locToAttack));
            }
        }

        if (rc.canAttack(locToAttack)) {
            rc.attack(locToAttack);
        }
    }

    // todo: maybe some well blacklist thing if needed
    private boolean enemyProtocol() throws GameActionException {
        for (RobotInfo robotInfo : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
            if (robotInfo.type == RobotType.LAUNCHER || robotInfo.type == RobotType.DESTABILIZER) {
                fleeingCounter = 6;
                break;
            }
        }

        if (fleeingCounter <= 0) return false;
        fightEnemy();
        fleeFromEnemy();
        return true;
    }

    boolean depositingResources;
    private void returnToHQ() throws GameActionException {
        if (rc.getWeight() == 0) {
            return;
        }
        MapLocation closestHQ = closestHQLocation;
        if (closestHQ == null) return;

        if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(closestHQ)) {
            pathing.moveTowards(closestHQ);
            pathing.moveTowards(closestHQ);
        }
//        if (!rc.isActionReady()) {
//            pathing.goTowardsOrStayAtEmptiestLocationNextTo(currentTask.targetHQLoc);
//        }
        if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(closestHQ)) {
            depositingResources = true;
        }
    }

    private void transferResourceToHQ(MapLocation HQLoc) throws GameActionException {
//        transferAllToHQ = true;
        for (ResourceType type : ResourceType.values()) {
            if (transferResource(HQLoc, type, rc.getResourceAmount(type)) && rc.getWeight() == 0) {
                return;
            }
        }
    }



    // call this method every round no matter what
    private void collectIfPossible() throws GameActionException {
        WellInfo[] wellInfos = rc.senseNearbyWells(2);
        if (wellInfos.length > 0) {
            Printer.appendToIndicator(" collecting");
            doWellCollection(wellInfos[0].getMapLocation());
        }
    }

    MapLocation target = null;
    private void randomExplore() throws GameActionException {
        if (target == null || Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(target, 10)) {
            do {
                target = Utils.randomMapLocation();
            } while (target.distanceSquaredTo(Cache.Permanent.START_LOCATION) >= 200);
        }
        pathing.moveTowards(target);
        pathing.moveTowards(target);
    }

    private void checkIfNewWellExists() {
        if (wellMiningFrom != null) return;
        WellInfo[] wellInfos = rc.senseNearbyWells(HQAssignedTask.collectionType);
        if (wellInfos.length > 0) {
            wellMiningFrom = wellInfos[0].getMapLocation();
            Printer.appendToIndicator(" newWell=" + wellMiningFrom);
            return;
        }
    }

    private MapLocation closestHQLocation = null;
    @Override
    protected void runTurn() throws GameActionException {
        closestHQLocation = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
//        if (Cache.PerTurn.ROUND_NUM >= 800) rc.resign();
        Printer.appendToIndicator("task=" + HQAssignedTask.collectionType + " miningFrom=" + wellMiningFrom);

        wellDataProtocol();

        if (enemyProtocol()) {
            collectIfPossible();
            return;
        }

        if (rc.getWeight() >= 39 && rc.getAnchor() == null) returnToHQ();

        if (depositingResources) {
            transferResourceToHQ(closestHQLocation);
            if (rc.getWeight() == 0 || rc.getAnchor() != null) depositingResources = false;
        }

        collectIfPossible();

        miningMicroMovement();

        checkIfNewWellExists();
        if (!depositingResources) {
            if (wellMiningFrom == null) {
                randomExplore();
                rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, target, 255, 0, 0);
            } else {
                moveToTargetWell();
            }
        }

        collectIfPossible();


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
