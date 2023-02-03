package finalbotfinal.robots.pathfinding;

import finalbotfinal.knowledge.Cache;
import finalbotfinal.utils.Utils;
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
    if (!rc.isMovementReady()) return false;
    if (target.equals(Cache.PerTurn.CURRENT_LOCATION)) return false;
    if (target.isAdjacentTo(Cache.PerTurn.CURRENT_LOCATION)) {
      return moveToOrAdjacent(target);
    }
    sp.updateDestination(target);
    if (Cache.PerTurn.ROUNDS_ALIVE == 0) {
      return BugNav.tryBugging();
    }
    return sp.pathToDestination();
  }

  @Override
  public boolean moveAwayFrom(MapLocation target) throws GameActionException {
    Direction away = target.directionTo(Cache.PerTurn.CURRENT_LOCATION);
    return moveTowards(Utils.clampToMap(Cache.PerTurn.CURRENT_LOCATION.translate(away.dx*5, away.dy*5)));
  }
}
