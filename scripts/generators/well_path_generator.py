"""
Generates the pathing around the 3x3 of a well for optimal entering and leaving depending on the cardinal direction. The cardinal direction is the direction of the BASE relative to the WELL.
The output path is a 9x2 matrix of offsets where offsets[0] is the leaving offset (ex. offsets[0] = [-1, -1]).
"""
from pathlib import Path
import re
import sys
from copy import copy


def gen():
  out = """"""
  return out+"\n"

DIRECTIONS = ['NORTH', 'NORTHEAST', 'EAST', 'SOUTHEAST', 'SOUTH', 'SOUTHWEST', 'WEST', 'NORTHWEST']
DIAGONAL_DIRECTIONS = DIRECTIONS[1::2]
CARDINAL_DIRECTIONS = DIRECTIONS[0::2]
print("diag", DIAGONAL_DIRECTIONS)
print("card", CARDINAL_DIRECTIONS)
DIAGONAL_NORTHEAST_PATH = [(-1, 1), (0, 1), (0, 0), (1, 1), (1, 0), (1, -1), (0, -1), (-1, -1), (-1, 0) ]
NORTH_PATH = [(0, 1), (1, 1), (0, 0), (1,0), (1, -1), (0, -1), (-1, -1), (-1, 0), (-1, 1)] 
# TODO: change to directions
def rotate_90_deg_clockwise(path):
  new_path = []
  for offset in path:
    x, y = offset
    new_path.append((y, -x))
  return new_path

def gen_constants():
  out = """"""""
  bits_so_far = 0
  diagonal_path = copy(DIAGONAL_NORTHEAST_PATH)
  path = copy(NORTH_PATH)
  for dir in DIAGONAL_DIRECTIONS:
    
    out += f"""  public static MapLocation[] {dir}_OFFSET = {{"""
    for offset in diagonal_path:
      out += f"""new MapLocation({offset[0]}, {offset[1]}), """
    out = out[:-2]
    out += f"""}};\n"""
    # rotate the boi
    diagonal_path = rotate_90_deg_clockwise(diagonal_path)
  for dir in CARDINAL_DIRECTIONS:
    
    out += f"""  public static MapLocation[] {dir}_OFFSET = {{"""
    for offset in path:
      out += f"""new MapLocation({offset[0]}, {offset[1]}), """
    out = out[:-2]
    out += f"""}};\n"""
    # rotate the boi
    path = rotate_90_deg_clockwise(path)
  return out.rstrip() + "\n"



if __name__ == '__main__':
  template_file = Path('./scripts/WellPathTemplate.java')
  # out_file = Path('./scripts/test.java')

  out_file = Path('./src/') / sys.argv[1] / 'robots' / 'micro' / 'CarrierWellPathing.java'
  with open(template_file, 'r') as t:
    with open(out_file, 'w') as f:
      for line in t:
        if line.strip().startswith('package') and line.strip().endswith(';'):
          f.write(re.sub(r'(?<=package )\w+',sys.argv[1],line,count=1))
        elif '// CONSTS' in line:
          f.write(gen_constants())
        # elif '// METAINFO UPDATE' in line:
        #   f.write(get_metainfo_update())
        elif '// MAIN READ AND WRITE METHODS' in line:
          f.write(gen())
        else:
          f.write(line)