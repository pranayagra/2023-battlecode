package sprint2balance.robots.pathfinding;

import sprint2balance.knowledge.Cache;
import battlecode.common.*;

public class SmartPathing extends Pathing {

  SmitePathing sp;

  public SmartPathing(RobotController rc) {
    super(rc);
    if (Cache.Permanent.ROBOT_TYPE != RobotType.HEADQUARTERS) {
      this.sp = new SmitePathing(rc, this);
    }
  }


  @Override
  public boolean moveTowards(MapLocation target) throws GameActionException {
//    int interestingTilesCounter = 0;
//    for (MapInfo mapInfo : rc.senseNearbyMapInfos()) {
//      if (!mapInfo.isPassable() || mapInfo.getCooldownMultiplier(Cache.Permanent.OUR_TEAM) != 1) {
//        interestingTilesCounter++;
//      }
//    }
//    if (interestingTilesCounter <= 2) {
//      return sp.cautiousGreedyMove(target);
//    }
    sp.updateDestination(target);
    if (Cache.PerTurn.ROUNDS_ALIVE == 1) {
      return sp.cautiousGreedyMove(target);
    }
    return sp.pathToDestination();
  }

  @Override
  public boolean moveAwayFrom(MapLocation target) throws GameActionException {
    Direction away = target.directionTo(Cache.PerTurn.CURRENT_LOCATION);
    return moveTowards(Cache.PerTurn.CURRENT_LOCATION.translate(away.dx*5, away.dy*5));
  }
}
