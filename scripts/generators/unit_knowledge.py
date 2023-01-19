import sys
from pathlib import Path

def encode(x, y):
  # return (x+7) + 15*(y+7)
  return f"$x{'_'if x<0 else ''}{abs(x)}$y{'_'if y<0 else ''}{abs(y)}"

# values from before:
# (20,10), (13,5), (34,20)
ROBOT_VISION_RAD_SQ = { # vision radius
  'Headquarters': 34,
  'Carrier': 20,
  'Launcher': 20,
  'Amplifier': 34, # 34 -- bytecode issue
  'Destabilizer': 20,
  'Booster': 20,
}
ROBOT_TYPES = list(ROBOT_VISION_RAD_SQ.keys())


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

MAX_DELTA = 4 # max movements between prev and curr location

def dist(x, y):
  return x*x + y*y


def sign(x):
  if x > 0:
    return 1
  if x < 0:
    return -1
  return 0

def gen_constants(radius):
  # 1 for center, 8 for directions, 1 for null
  return f"""
  public static final int MAX_DELTA = {MAX_DELTA};
  public static final int USED_VISION_RAD = {radius};
"""

def gen_unseen(radius, unittype):
  # radius = ROBOT_VISION_RAD_SQ[unit]
  # print(radius)
  out = f"""
    switch (curr.x - prev.x) {{"""
  for dx in range(-MAX_DELTA, MAX_DELTA+1):
    out += f"""
      case {dx}:
        switch (curr.y - prev.y) {{"""
    for dy in range(-MAX_DELTA, MAX_DELTA+1):
      out += f"""
          case {dy}: // delta ({dx}, {dy})
            return new MapLocation[]{{"""
      for rdx in range(-radius, radius+1):
        for rdy in range(-radius, radius+1):
          if dist(rdx, rdy) <= radius and dist(rdx+dx, rdy+dy) > radius:
            out += f"""
              {f"curr.add({DIRECTIONS[(rdx,rdy)]})" if (rdx, rdy) in DIRECTIONS else f"curr.translate({rdx}, {rdy})"}, // offset ({rdx}, {rdy})"""
      out += f"""
            }};"""
    out += f"""
        }}
        break;"""
  out += f"""
    }}
    return new MapLocation[0];
"""
  return out.strip('\n')

def gen_full(bot, unit):
  radius = ROBOT_VISION_RAD_SQ[unit]
  out_file = Path('./src/') / bot / 'knowledge' / 'unitknowledge' / f'{unit}Knowledge.java'
  with open(out_file, 'w') as f:
    f.write(f"""
package {bot}.knowledge.unitknowledge;

import battlecode.common.*;
import {bot}.utils.Utils;

public class {unit}Knowledge extends UnitKnowledge {{
    
{gen_constants(radius)}

  // Automatically calculates the newly discovered locations based on a previous and current location
  @Override
  protected MapLocation[] newUnseenMapLocationsImpl(MapLocation prev, MapLocation curr) {{
{gen_unseen(radius, unit)}
  }}
}}
""".lstrip())



if __name__ == '__main__':
  if len(sys.argv) == 2:
    for unit in ROBOT_TYPES:
      gen_full(sys.argv[1], unit)
  else:
    gen_full(sys.argv[1], sys.argv[2])