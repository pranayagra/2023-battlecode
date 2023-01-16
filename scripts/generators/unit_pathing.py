import sys
from pathlib import Path

def encode(x, y):
  # return (x+7) + 15*(y+7)
  return f"$x{'_'if x<0 else ''}{abs(x)}$y{'_'if y<0 else ''}{abs(y)}"

# values from before:
# (20,10), (13,5), (34,20)
ROBOT_SEARCH_RADII = { # (vision radius, minimum search radius)
  'Carrier': (20,10),
  'Launcher': (20,10),
  'Amplifier': (20,10), # 34,20 -- bytecode issue
  'Destabilizer': (20,10),
  'Booster': (20,10),
}
ROBOT_TYPES = list(ROBOT_SEARCH_RADII.keys())

RADII = {
  key: value[0] for key,value in ROBOT_SEARCH_RADII.items()
}
SMALLER_RADII = {
  key: value[1] for key,value in ROBOT_SEARCH_RADII.items()
}

# NORTH(0, 1),
# NORTHEAST(1, 1),
# EAST(1, 0),
# SOUTHEAST(1, -1),
# SOUTH(0, -1),
# SOUTHWEST(-1, -1),
# WEST(-1, 0),
# NORTHWEST(-1, 1),
# CENTER(0, 0),
DIRECTIONS = {
  (0, 1): 'Direction.NORTH',
  (1, 1): 'Direction.NORTHEAST',
  (1, 0): 'Direction.EAST',
  (1, -1): 'Direction.SOUTHEAST',
  (0, -1): 'Direction.SOUTH',
  (-1, -1): 'Direction.SOUTHWEST',
  (-1, 0): 'Direction.WEST',
  (-1, 1): 'Direction.NORTHWEST',
}
DIRECTION_BIT_VEC = {
  (0, 1): 0,#'Direction.NORTH',
  (1, 1): 1,#'Direction.NORTHEAST',
  (1, 0): 2,#'Direction.EAST',
  (1, -1): 3,#'Direction.SOUTHEAST',
  (0, -1): 4,#'Direction.SOUTH',
  (-1, -1): 5,#'Direction.SOUTHWEST',
  (-1, 0): 6,#'Direction.WEST',
  (-1, 1): 7,#'Direction.NORTHWEST',
}
for k,v in DIRECTION_BIT_VEC.items():
  DIRECTION_BIT_VEC[k] = v + 1
DIRECTION_BITS = max(len(bin(v)) - 2 for v in DIRECTION_BIT_VEC.values())

def dist(x, y):
  return x*x + y*y

def gen_constants(radius):
  out = f""""""
  for x in range(-7, 8):
    for y in range(-7, 8):
      if dist(x, y) <= radius:
        out += f"""
    static MapLocation l{encode(x,y)}; // location representing relative coordinate ({x}, {y})
    static int d{encode(x,y)}; // shortest distance to location from current location
    // static Direction dir{encode(x,y)}; // best direction to take now to optimally get to location
"""
  return out

def sign(x):
  if x > 0:
    return 1
  if x < 0:
    return -1
  return 0

def gen_init(radius):
  out = f"""
        l{encode(0,0)} = rc.getLocation();
        d{encode(0,0)} = 0;
        // dir{encode(0,0)} = Direction.CENTER;
"""
  for r2 in range(1, radius+1):
    for x in range(-7, 8):
      for y in range(-7, 8):
        if dist(x, y) == r2:
          out += f"""
        l{encode(x,y)} = l{encode(x - sign(x), y - sign(y))}.add({DIRECTIONS[(sign(x), sign(y))]}); // ({x}, {y}) from ({x - sign(x)}, {y - sign(y)})
        d{encode(x,y)} = 99999 << {DIRECTION_BITS};
        // dir{encode(x,y)} = null;
"""
  return out

def gen_bfs(radius, type):
  visited = {encode(0, 0)}
  out = f"""
"""
  if type == 'Carrier':
    out += f"""
        int moveCooldown = (int) (GameConstants.CARRIER_MOVEMENT_INTERCEPT + rc.getWeight() * GameConstants.CARRIER_MOVEMENT_SLOPE);
"""
  else:
    out += f"""
        int moveCooldown = RobotType.{type.upper()}.movementCooldown;
"""
  out += f"""
        int shiftedMoveCD = moveCooldown << {DIRECTION_BITS};
"""
  for r2 in range(1, radius+1):
    for x in range(-7, 8):
      for y in range(-7, 8):
        if dist(x, y) == r2:
          out += f"""
        if (rc.canSenseLocation(l{encode(x,y)})) {{ // check ({x}, {y})"""
          indent = "  "
          if r2 <= 2:
            out += f"""
          if (rc.sensePassability(l{encode(x,y)}) && !rc.isLocationOccupied(l{encode(x,y)})) {{ """
          else:
            out += f"""
          if (rc.sensePassability(l{encode(x,y)})) {{ """
          if r2 > 2:
            dxdy = [(dx, dy) for dx in range(-1, 2) for dy in range(-1, 2) if (dx, dy) != (0, 0) and dist(x+dx,y+dy) <= radius]
            dxdy = sorted(dxdy, key=lambda dd: dist(x+dd[0], y+dd[1]))
            for dx, dy in dxdy:
              if encode(x+dx, y+dy) in visited:
                out += f"""
          {indent}if (d{encode(x,y)} > d{encode(x+dx,y+dy)}) {{ // from ({x+dx}, {y+dy})
              {indent}d{encode(x,y)} = d{encode(x+dx,y+dy)};
              {indent}// dir{encode(x,y)} = dir{encode(x+dx,y+dy)};
          {indent}}}"""#DIRECTIONS[(-dx, -dy)]
          else:
            # print(unit, (x, y, r2, radius), DIRECTION_BIT_VEC[(x, y)], encode(x,y))
            out += f"""
          {indent}d{encode(x,y)} = d{encode(0,0)} | {DIRECTION_BIT_VEC[(x,y)]}; // from (0, 0)
          {indent}// dir{encode(x,y)} = {DIRECTIONS[(x, y)]};
""".replace(' | 0', '')
          # cost_string = f"((int) (rc.senseCooldownMultiplier(l{encode(x,y)}) * moveCooldown))"
          # cost_string = f"((int) (rc.senseCooldownMultiplier(l{encode(x,y)}) * 20))"
          cost_string = f"((int) (rc.senseMapInfo(l{encode(x,y)}).getCooldownMultiplier(Cache.Permanent.OUR_TEAM) * moveCooldown)) << {DIRECTION_BITS}" if r2 <= 2 else f"shiftedMoveCD"#("0b1"+"0"*DIRECTION_BITS)
          out += f"""
          {indent}d{encode(x,y)} += {cost_string};""" # should be "* moveCooldown" but bytecode
                                      #rc.senseRubble(l{encode(x,y)}) + 10;""" ## should be the incurred cooldown cost of movement here
          # if r2 <= 2:
          out += f"""
          }}"""
          visited.add(encode(x,y))
          out += f"""
        }}
"""
  return out

def gen_selection(radius, smaller_radius):
  out = f"""
        int target_dx = target.x - l{encode(0,0)}.x;
        int target_dy = target.y - l{encode(0,0)}.y;
        int dirEnc;
        switch (target_dx) {{"""
  for tdx in range(-7, 8):
    if tdx**2 <= radius:
      out += f"""
                case {tdx}:
                    switch (target_dy) {{"""
      for tdy in range(-7, 8):
        if dist(tdx, tdy) <= radius:
          out += f"""
                        case {tdy}:
                            dirEnc = d{encode(tdx, tdy)} & 0b{"1"*DIRECTION_BITS};
                            return dirEnc-- == 0 ? null : Utils.directions[dirEnc]; // dir{encode(tdx, tdy)}; // destination is at relative location ({tdx}, {tdy})"""
      out += f"""
                    }}
                    break;"""
  out += f"""
        }}
        
        int ans = 0;
        double bestScore = 0;
        double currDist = Math.sqrt(l{encode(0,0)}.distanceSquaredTo(target));
        """
  for x in range(-7, 8):
    for y in range(-7, 8):
      if smaller_radius < dist(x, y) <= radius: # on the edge of the radius radius
        out += f"""
        double score{encode(x,y)} = (currDist - Math.sqrt(l{encode(x,y)}.distanceSquaredTo(target))) / (d{encode(x,y)} >>> {DIRECTION_BITS});
        if (score{encode(x,y)} > bestScore) {{
            bestScore = score{encode(x,y)};
            ans = d{encode(x, y)}; // dir{encode(x,y)};
        }}
"""
  return out

def gen_print(radius):
  out = f"""
        // System.out.println("LOCAL DISTANCES:");"""
  for y in range(7, -8, -1):
    if y**2 <= radius:
      out += f"""
        // System.out.println("""
      for x in range(-7, 8):
        if dist(x, y) <= radius:
          out += f""""\\t" + d{encode(x,y)} + """
        else:
          out += f""""\\t" + """
      out = out[:-3] + """);"""
  out += f"""
        // System.out.println("DIRECTIONS:");"""
  for y in range(7, -8, -1):
    if y**2 <= radius:
      out += f"""
        // System.out.println("""
      for x in range(-7, 8):
        if dist(x, y) <= radius:
          out += f""""\\t" + dir{encode(x,y)} + """
        else:
          out += f""""\\t" + """
      out = out[:-3] + """);"""
  return out

def gen_full(bot, unit):
  radius = RADII[unit]
  smaller_radius = SMALLER_RADII[unit]
  out_file = Path('./src/') / bot / 'robots' / 'pathfinding' / 'unitpathing' / f'{unit}Pathing.java'
  with open(out_file, 'w') as f:
    f.write(f"""// Inspired by https://github.com/IvanGeffner/battlecode2021/blob/master/thirtyone/BFSMuckraker.java.
package {bot}.robots.pathfinding.unitpathing;

import battlecode.common.*;
import {bot}.utils.Cache;
import {bot}.utils.Utils;

public class {unit}Pathing implements UnitPathing {{
    
    RobotController rc;
{gen_constants(radius)}

    public {unit}Pathing(RobotController rc) {{
        this.rc = rc;
    }}

    public Direction bestDir(MapLocation target) throws GameActionException {{
{gen_init(radius)}
{gen_bfs(radius,unit)}
{gen_print(radius)}
{gen_selection(radius, smaller_radius)}
        
        dirEnc = ans & 0b{"1"*DIRECTION_BITS};
        return dirEnc-- == 0 ? null : Utils.directions[dirEnc];
    }}
}}
""")

if __name__ == '__main__':
  if len(sys.argv) == 2:
    for unit in ROBOT_TYPES:
      gen_full(sys.argv[1], unit)
  else:
    gen_full(sys.argv[1], sys.argv[2])