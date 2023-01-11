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
DIAGONAL_NORTHEAST_PATH = [(1, 1), (0, 1), (0, 0), (-1, 1), (-1, 0), (-1, -1), (0, -1), (1, -1), (1, 0) ]
NORTH_PATH = [(0, 1), (1, 1), (0, 0), (1,0), (1, -1), (0, -1), (-1, -1), (-1, 0), (-1, 1)]

def fix_list(offset_list):
  for i in range(len(offset_list)):
    if offset_list[i] == (0, 1):
      offset_list[i] = "NORTH"
    elif offset_list[i] == (1, 1):
        offset_list[i] = "NORTHEAST"
    elif offset_list[i] == (1, 0):
        offset_list[i] = "EAST"
    elif offset_list[i] == (1, -1):
        offset_list[i] = "SOUTHEAST"
    elif offset_list[i] == (0, -1):
        offset_list[i] = "SOUTH"
    elif offset_list[i] == (-1, -1):
        offset_list[i] = "SOUTHWEST"
    elif offset_list[i] == (-1, 0):
        offset_list[i] = "WEST"
    elif offset_list[i] == (-1, 1):
        offset_list[i] = "NORTHWEST"
    elif offset_list[i] == (0, 0):
        offset_list[i] = "CENTER"

fix_list(DIAGONAL_NORTHEAST_PATH)
fix_list(NORTH_PATH)

def rotateRight(dir):
  if dir == 'CENTER':
    return dir
  return DIRECTIONS[(DIRECTIONS.index(dir) + 1) % 8]

def rotateLeft(dir):
  if dir == 'CENTER':
    return dir
  return DIRECTIONS[(DIRECTIONS.index(dir) + 8 - 1) % 8];

def rotate_90_deg_clockwise(path):
  return [rotateRight(rotateRight(offset_dir)) for offset_dir in path]

def gen_constants():
  out = """"""""
  bits_so_far = 0
  diagonal_path = copy(DIAGONAL_NORTHEAST_PATH)
  path = copy(NORTH_PATH)
  for diag_dir in DIAGONAL_DIRECTIONS:

    out += f"""  public static Direction[] {diag_dir}_OFFSET = {{
      {", ".join([f"Direction.{offset_dir}" for offset_dir in diagonal_path])}
    }};\n"""
    # rotate the boi
    diagonal_path = rotate_90_deg_clockwise(diagonal_path)
  for card_dir in CARDINAL_DIRECTIONS:

    out += f"""  public static Direction[] {card_dir}_OFFSET = {{
      {", ".join([f"Direction.{offset_dir}" for offset_dir in path])}
    }};\n"""
    # rotate the boi
    path = rotate_90_deg_clockwise(path)
  return out.rstrip() + "\n"



if __name__ == '__main__':
  template_file = Path('./scripts/WellPathTemplate.java')
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