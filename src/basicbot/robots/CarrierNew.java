package basicbot.robots;

import basicbot.communications.CommsHandler;
import basicbot.communications.Communicator;
import basicbot.communications.HqMetaInfo;
import basicbot.containers.HashMap;
import basicbot.containers.HashSet;
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

    int fleeingCounter;

    private static final int SET_WELL_PATH_DISTANCE = 2;//RobotType.CARRIER.actionRadiusSquared;

    private int turnsStuckApproachingWell;
    private final HashMap<MapLocation, Direction> wellApproachDirection;
    private final HashSet<MapLocation> blackListWells;
    private MapLocation[] wellQueueOrder;
    private MapLocation wellEntryPoint;
    int wellQueueTargetIndex;
    private int emptierRobotsSeen;
    private int fullerRobotsSeen;
    private int roundsWaitingForQueueSpot;

    private RobotInfo cachedLastEnemyForBroadcast;
    private int cachedLastEnemyForBroadcastRound = -10;

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
//        if (rc.getID() == 11401) {
//            System.out.println("bytes used-1: " + Clock.getBytecodeNum());
//        }
        wellApproachDirection = new HashMap<>(3);
        blackListWells = new HashSet<>(8);
        wellQueueOrder = null;
        readHQTask();
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
        HQAssignedTask = null;
        HQAssignedTaskLocation = null;
        int readThrough = 8;
        if (Cache.PerTurn.ROUND_NUM % 2 == 0) {
            for (int i = 0; i < readThrough; ++i) {
                if (CommsHandler.readPranayOurHqEvenSpawnExists(i)) {
                    MapLocation spawnLocation = CommsHandler.readPranayOurHqEvenSpawnLocation(i);
                    if (!Cache.Permanent.START_LOCATION.equals(spawnLocation)) continue;
                    int instruction = CommsHandler.readPranayOurHqEvenSpawnInstruction(i);
                    if (CommsHandler.readPranayOurHqEvenTargetExists(i)) {
                        HQAssignedTaskLocation = CommsHandler.readPranayOurHqEvenTargetLocation(i);
                        CommsHandler.writePranayOurHqEvenTargetLocation(i, CommsHandler.NONEXISTENT_MAP_LOC);
                    }
                    switch (instruction) {
                        case 1:
                            HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
                            break;
                        case 2:
                            HQAssignedTask = CarrierTask.FETCH_MANA;
                            break;
                    }
                    return;
                }
            }
        } else {
            for (int i = 0; i < readThrough; ++i) {
                if (CommsHandler.readPranayOurHqOddSpawnExists(i)) {
                    MapLocation spawnLocation = CommsHandler.readPranayOurHqOddSpawnLocation(i);
                    if (!Cache.Permanent.START_LOCATION.equals(spawnLocation)) continue;
                    int instruction = CommsHandler.readPranayOurHqOddSpawnInstruction(i);
                    if (CommsHandler.readPranayOurHqOddTargetExists(i)) {
                        HQAssignedTaskLocation = CommsHandler.readPranayOurHqOddTargetLocation(i);
                        CommsHandler.writePranayOurHqOddTargetLocation(i, CommsHandler.NONEXISTENT_MAP_LOC);
                    }
                    switch (instruction) {
                        case 1:
                            HQAssignedTask = CarrierTask.FETCH_ADAMANTIUM;
                            break;
                        case 2:
                            HQAssignedTask = CarrierTask.FETCH_MANA;
                            break;
                    }
                    return;
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
        if (!Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellMiningFrom, SET_WELL_PATH_DISTANCE)) return;

        closeToTargetWell = false;
        roundNumWhenCloseToTarget = 2000;
        numRoundsCloseToTarget = 0;
        Printer.appendToIndicator(" micro_move");
//        if (approachWell(wellMiningFrom)) return;

        boolean bigWeight = rc.getWeight() >= 30;
        do {
            List<Direction> dirs = new ArrayList<>(8);
            int distance = Cache.PerTurn.CURRENT_LOCATION.distanceSquaredTo(closestHQLocation);
            for (Direction dir : Utils.directions) {
                MapLocation loc = Cache.PerTurn.CURRENT_LOCATION.add(dir);
                int dist = loc.distanceSquaredTo(closestHQLocation);
                if (!loc.isWithinDistanceSquared(wellMiningFrom, 2)) continue;
                if (rc.canMove(dir) && rc.senseMapInfo(loc).getCurrentDirection() == Direction.CENTER) {
                    if (bigWeight) {
                        if (dist < distance) {
                            distance = dist;
                            dirs.clear();
                            dirs.add(dir);
                        } else if (dist == distance) {
                            dirs.add(dir);
                        }
                    } else {
                        dirs.add(dir);
                        distance = dist;
                    }
                }
            }
            if (dirs.size() == 0) return;
            Direction dirToMove = dirs.get(Utils.rng.nextInt(dirs.size()));
            pathing.move(dirToMove);
        } while (rc.isMovementReady() && false);
    }


    /** WELL DATA STORING PROTOCOL **/
    class WellData {
        ResourceType type;
        int numMiners;
        int numGoodLocations;
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
        int numGoodLocations = 0;
        for (MapLocation loc : allMiningLocations) {
            if (rc.canSenseLocation(loc)) {
                ++numSensedLocs;
                RobotInfo robotInfo = rc.senseRobotAtLocation(loc);
                if (robotInfo != null && robotInfo.team == Cache.Permanent.OUR_TEAM && robotInfo.type == RobotType.CARRIER) {
                    ++numMiners;
                }
                MapInfo mapInfo = rc.senseMapInfo(loc);
                if (mapInfo != null && mapInfo.isPassable() && mapInfo.getCurrentDirection() == Direction.CENTER) {
                    ++numGoodLocations;
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
            wellData.numMiners = Math.max(wellData.numMiners, numMiners);
            wellData.numGoodLocations = Math.max(wellData.numGoodLocations, numGoodLocations);
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
                    if (wellData.numRounds >= 25) {
                        // todo: create comm structure and write
                        locWrote = wellLoc;
                        int numMiners = Math.min(wellData.numMiners, 7);
                        ResourceType type = wellData.type;
                        CommsHandler.writePranayWellInfoLocation(idx, wellLoc);
                        CommsHandler.writePranayWellInfoType(idx, type.resourceID);
                        CommsHandler.writePranayWellInfoNumMiners(idx, numMiners);
                        CommsHandler.writePranayWellInfoNumGoodSlots(idx, wellData.numGoodLocations);
//                        Printer.print("writing well(>25) info", "well=" + wellLoc, " data=" + temporaryWellDataMap.get(wellLoc));
                        break;
                    } else if (!allKnownWells.contains(wellLoc)) {
                        ResourceType type = wellData.type; //12+1+3
                        CommsHandler.writePranayWellInfoLocation(idx, wellLoc);
                        CommsHandler.writePranayWellInfoType(idx, type.resourceID);
                        CommsHandler.writePranayWellInfoNumGoodSlots(idx, wellData.numGoodLocations);
//                        CommsHandler.writePranayWellInfoNumMiners(idx, 0);
                        allKnownWells.add(wellLoc);
//                        Printer.print("writing well(imp) info", "well=" + wellLoc, " data=" + temporaryWellDataMap.get(wellLoc));
                        return true;
                    }
                } else if (!allKnownWells.contains(wellLoc)) {
                    WellData wellData = temporaryWellDataMap.get(wellLoc);
                    // todo: create comm structure and write
                    locWrote = wellLoc;
                    ResourceType type = wellData.type; //12+1+3
                    CommsHandler.writePranayWellInfoLocation(idx, wellLoc);
                    CommsHandler.writePranayWellInfoType(idx, type.resourceID);
                    CommsHandler.writePranayWellInfoNumGoodSlots(idx, wellData.numGoodLocations);
//                    CommsHandler.writePranayWellInfoNumMiners(idx, 0);
//                    Printer.print("writing well(gen) info", "well=" + wellLoc, " data=" + temporaryWellDataMap.get(wellLoc));
                    break;
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

    private boolean closeToTargetWell;
    private int roundNumWhenCloseToTarget;
    private int numRoundsCloseToTarget;
    private void moveToTargetWell() throws GameActionException {
        if (wellMiningFrom == null) return;
        if (!rc.isMovementReady()) return;
        if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellMiningFrom, SET_WELL_PATH_DISTANCE)) return;
        if (rc.getWeight() >= 39) return;

        // maybe get num spots available at mine instead of just trying
        if (!closeToTargetWell && Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(wellMiningFrom, 16)) {
            closeToTargetWell = true;
            roundNumWhenCloseToTarget = Cache.PerTurn.ROUND_NUM;
            ++numRoundsCloseToTarget;
        }

        // tried for 10 rounds, give up
        if (closeToTargetWell && (Cache.PerTurn.ROUND_NUM - roundNumWhenCloseToTarget >= 20 && numRoundsCloseToTarget >= 10) || (Cache.PerTurn.ROUND_NUM - roundNumWhenCloseToTarget >= 50)) {
            closeToTargetWell = false;
            roundNumWhenCloseToTarget = 2000;
            numRoundsCloseToTarget = 0;
            blackListWells.add(wellMiningFrom);
            wellMiningFrom = null;
            target = null;
            return;
        }

        Printer.appendToIndicator(" move_to_well");
        if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
            pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(wellMiningFrom));
        } else {
            pathing.moveTowards(wellMiningFrom);
        }
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
        if (rc.getHealth() >= 130) return;
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
    private int healthLastRound = 150;
    private boolean enemyProtocol() throws GameActionException {

        // currently fleeing and close to HQ, keep fleeing
        if (fleeingCounter > 0) {
            if (Utils.maxSingleAxisDist(Cache.PerTurn.CURRENT_LOCATION, closestHQLocation) <= 10) {
                fleeingCounter = 6;
            }
        }

        // currently fleeing and now next to HQ, transfer resources and stop fleeing
        if (fleeingCounter > 0) {
            if (Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(closestHQLocation, 2)) {
                transferResourceToHQ(closestHQLocation);
                fleeingCounter = 0;
            }
        }

        // if there are enemies, flee!
        for (RobotInfo robotInfo : Cache.PerTurn.ALL_NEARBY_ENEMY_ROBOTS) {
            if (robotInfo.type == RobotType.LAUNCHER || robotInfo.type == RobotType.DESTABILIZER) {
                cachedLastEnemyForBroadcast = robotInfo;
                cachedLastEnemyForBroadcastRound = Cache.PerTurn.ROUND_NUM;
                fleeingCounter = 6;
                closeToTargetWell = false; // not sure about this one
                target = null;
                break;
            }
        }


//        if (fleeingCounter == 6) {
//            // if I see an enemy but did not get attacked and I have friends nearby, do not run away
//            if (rc.getHealth() >= healthLastRound && rc.getHealth() >= 80 && Cache.PerTurn.ALL_NEARBY_FRIENDLY_ROBOTS.length > 0) {
//                fleeingCounter = 0;
//            }
//        }

        // not fleeing
        if (fleeingCounter <= 0) return false;
        fightEnemy(); // only fight if I have resources and I am not high on health
        fleeFromEnemy(); //always flee if see enemy
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
        if (wellMiningFrom != null && wellMiningFrom.isAdjacentTo(Cache.PerTurn.CURRENT_LOCATION)) {
            doWellCollection(wellMiningFrom);
        } else {
            WellInfo[] wellInfos = rc.senseNearbyWells(2);
            if (wellInfos.length > 0) {
                doWellCollection(wellInfos[0].getMapLocation());
            }
        }
    }

    MapLocation target = null;
    MapLocation[] locs = new MapLocation[8];
    int currDistance = 10;
    private void generateTargets(MapLocation center, int distance) {
        locs[0] = center.translate(distance, 0);
        locs[1] = center.translate(-distance, 0);
        locs[2] = center.translate(0, distance);
        locs[3] = center.translate(0, -distance);
        locs[4] = center.translate(distance, distance);
        locs[5] = center.translate(-distance, distance);
        locs[6] = center.translate(distance, -distance);
        locs[7] = center.translate(-distance, -distance);
    }

    int startIndex = 0;
    int currIndex = 0;
    private void setupExplore() {
        startIndex = Utils.rng.nextInt(8);
        currIndex = (startIndex + 1) % 8;
        int maxDist = 10 + Math.max(0, (Cache.PerTurn.ROUND_NUM - 150) / 100 * 10);
        currDistance = Utils.rng.nextInt(maxDist) + 10;
    }

    private void randomExplore(boolean random) throws GameActionException {
        int maxDistance = 15;
        if (Cache.PerTurn.ROUND_NUM >= 100) {
            maxDistance = 15 + Math.max(0, (Cache.PerTurn.ROUND_NUM - 100) / 100 * 10);
        }
        if (Cache.PerTurn.ROUND_NUM <= 500) {
            maxDistance = Math.min(maxDistance, 30);
        }
        if (startIndex == currIndex) {
            setupExplore();
        }
        if (target == null || Cache.PerTurn.CURRENT_LOCATION.isWithinDistanceSquared(target, 10)) {
            do {
                target = Utils.randomMapLocation();
//                else
//                target = locs[currIndex];
//                currIndex = (currIndex + 1) % 8;
            } while (Utils.maxSingleAxisDist(Cache.Permanent.START_LOCATION, target) > maxDistance);
        }
        if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
            pathing.moveInDirLoose(Cache.PerTurn.CURRENT_LOCATION.directionTo(target));
        } else {
            pathing.moveTowards(target);
        }
        pathing.moveTowards(target);
    }

    private void checkIfNewWellExists() {
        if (wellMiningFrom != null) return;
        WellInfo[] wellInfos = rc.senseNearbyWells(HQAssignedTask.collectionType);

        for (WellInfo wellInfo : wellInfos) {
            if (!blackListWells.contains(wellInfo.getMapLocation())) {
                Printer.appendToIndicator(" newWell=" + wellMiningFrom);
                wellMiningFrom = wellInfo.getMapLocation();
                return;
            }
        }
    }

    private MapLocation closestHQLocation = null;
    @Override
    protected void runTurn() throws GameActionException {
        closestHQLocation = HqMetaInfo.getClosestHqLocation(Cache.PerTurn.CURRENT_LOCATION);
        Printer.appendToIndicator("task=" + HQAssignedTask.collectionType + " miningFrom=" + wellMiningFrom);

        wellDataProtocol();
        transferResourceToHQ(closestHQLocation);

        if (enemyProtocol()) {
            collectIfPossible();
        } else {
            collectIfPossible();

            if (rc.getWeight() >= 39 && rc.getAnchor() == null) returnToHQ();

            if (depositingResources) {
                transferResourceToHQ(closestHQLocation);
                if (rc.getWeight() == 0 || rc.getAnchor() != null) depositingResources = false;
            }

            miningMicroMovement();

            checkIfNewWellExists();

            if (!depositingResources) {
                if (wellMiningFrom == null) {
                    randomExplore(true);
                    rc.setIndicatorLine(Cache.PerTurn.CURRENT_LOCATION, target, 255, 0, 0);
                } else {
                    moveToTargetWell();
                }
            }

            collectIfPossible();
        }

        healthLastRound = rc.getHealth();

        if (cachedLastEnemyForBroadcast != null && (fleeingCounter > 0 || Cache.PerTurn.ROUND_NUM - cachedLastEnemyForBroadcastRound <= 6)) {
            Printer.appendToIndicator(" comm_enemy");
            if (rc.canWriteSharedArray(0, 0)) {
                Communicator.writeEnemy(cachedLastEnemyForBroadcast);
            }
        }


    }

    private boolean approachWell(MapLocation wellLocation) throws GameActionException {
        if (wellQueueOrder == null) {
            wellApproachDirection.put(wellLocation, wellLocation.directionTo(Cache.PerTurn.CURRENT_LOCATION));
            wellQueueOrder = CarrierWellPathing.getPathForWell(wellLocation, wellApproachDirection.get(wellLocation));
            wellEntryPoint = wellQueueOrder[0];
            for (int i = 0; i < wellQueueOrder.length; i++) {
                if (rc.canSenseLocation(wellQueueOrder[i]) && rc.sensePassability(wellQueueOrder[i]) && !BugNav.blockedLocations.contains(wellQueueOrder[i])) {
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
            }
        }

        updateWellQueueTarget();

        if (wellQueueTargetIndex != -1) {  // we have a spot in the queue (wellQueueTargetIndex)
            rc.setIndicatorString("well queue target position: " + wellQueueOrder[wellQueueTargetIndex]);
            while (rc.isMovementReady() && !Cache.PerTurn.CURRENT_LOCATION.equals(wellQueueOrder[wellQueueTargetIndex])) {
                // not yet in the path, just go to the entry point
                if (!Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellLocation)) {
                    rc.setIndicatorString("approaching well (" + wellLocation + ") via: " + wellEntryPoint);
                    while (pathing.moveTowards(wellEntryPoint)) {}
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
                        rc.setIndicatorString("well (" + wellLocation + ") entry blocked -- redetermine entry point -> " + wellEntryPoint);
                        turnsStuckApproachingWell++;
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
        return true;
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
            if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || isValidQueuePosition(wellMiningFrom, pathTarget)) {
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
            if (pathTarget.equals(Cache.PerTurn.CURRENT_LOCATION) || isValidQueuePosition(wellMiningFrom, pathTarget)) {
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
        if (Cache.PerTurn.CURRENT_LOCATION.isAdjacentTo(wellMiningFrom)) {
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
