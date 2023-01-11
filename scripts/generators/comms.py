from pathlib import Path
import re
import sys

NUM_INTS = 100
BITS_PER_INT = 16
MAX_BITS = NUM_INTS * BITS_PER_INT

LOCATION_KEY = 'loc'
BOOL_KEY = 'bool'

SPECIAL_ATTRS = []
SUFFIX_TO_GENERAL = {
  LOCATION_KEY: {
    'datatype': 'MapLocation',
    'new_suffixes': {
      'x': [6,'({}).x'], # format string to write property to comms
      'y': [6,'({}).y'],
    },
    'read_conversion': 'new MapLocation({})',
    # 'read_method_prefix': 'read',
    # 'write_method_prefix': 'write',
    'method_suffix': 'location',
  },
  BOOL_KEY: {
    'datatype': 'boolean',
    'new_suffixes': {
      'bit': [1,'(({}) ? 1 : 0)'], # format string to write property to comms
    },
    'read_conversion': '(({}) == 1)',
    # 'read_method_prefix': 'is',
    # 'write_method_prefix': 'set',
    'method_suffix': '',
  }
}
SPECIAL_ATTR_SUFFIXES = list(SUFFIX_TO_GENERAL.keys())
SPECIAL_ATTR_BITS = {suffix:sum(suffix_info[0] for suffix_info in general_info['new_suffixes'].values()) for suffix,general_info in SUFFIX_TO_GENERAL.items()}

LOCATION_BITS = SPECIAL_ATTR_BITS[LOCATION_KEY]
BOOL_BITS = SPECIAL_ATTR_BITS[BOOL_KEY]

METAINFO = {
  'hq_count': {
    'bits': 2,
  },
  'map_symmetry': {
    'bits': 3,
    'update': True,
  },
}

SCHEMA = {
  '': {
    'slots': 1,
    'bits': {key: value['bits'] for key,value in METAINFO.items()}
  },
  'our_hq': {
    'slots': 4,
    'bits': {
      'exists_bool': BOOL_BITS,
      'loc': LOCATION_BITS,
      'closest_adamantium_loc': LOCATION_BITS,
      'adamantium_upgraded_bool': BOOL_BITS,
      'closest_mana_loc': LOCATION_BITS,
      'mana_upgraded_bool': BOOL_BITS,
      'closest_elixir_loc': LOCATION_BITS,
      'elixir_upgraded_bool': BOOL_BITS,
    }
  },
  'attack_pods': {
    'slots': 10,
    'bits': {
      'amp_alive_bool': BOOL_BITS,
      'amp_loc': LOCATION_BITS,
      'launcher_registry': 6,
    }
  },
}

def is_suffix_special(attr: str) -> bool:
  return any(attr.endswith(suffix) for suffix in SPECIAL_ATTR_SUFFIXES)

def get_suffix_info(attribute: str) -> tuple[str, str]:
  for suffix in SPECIAL_ATTR_SUFFIXES:
    if attribute.endswith(f"{suffix}"):
      return suffix, SUFFIX_TO_GENERAL[suffix]
  print('ERROR: suffix not found')

def is_special_attr(attr: str) -> bool:
  return attr in SPECIAL_ATTRS or is_suffix_special(attr)

def get_special_attrs(attr_list: list[str]) -> list[str]:
  return [attr for attr in attr_list if is_special_attr(attr)]

def special_to_general(attr: str) -> list[str]:
  print('special attr:', attr)
  for suffix in SPECIAL_ATTR_SUFFIXES:
    if attr.endswith(f"{suffix}"):
      return [f"{attr[:-len(suffix)]}{new_suffix}" for new_suffix in SUFFIX_TO_GENERAL[suffix]['new_suffixes'].keys()]
  print('ERROR: special attr not found')
def get_generated_attrs(schema_datatype: dict) -> list[tuple[str,int]]:
  # [*SCHEMA[datatype]['bits'], 'all']
  attr_list = schema_datatype['bits'].keys()
  attrs = []
  for attr in attr_list:
    if is_special_attr(attr):
      if is_suffix_special(attr):
        suffix, general_info = get_suffix_info(attr)
        prefix = attr[:-len(suffix)]
        for new_suffix, (bits, _) in general_info['new_suffixes'].items():
          attrs.append((f"{prefix}{new_suffix}", bits, 'private'))
    else:
      attrs.append((attr, schema_datatype['bits'][attr], 'public'))
  # attrs.append(('all',sum(schema_datatype['bits'].values()),'public'))
  print('attrs:', attrs)
  return attrs

def gen_constants():
  out = """"""
  for datatype in SCHEMA:
    name = datatype.upper() if datatype else 'META'
    out += f"""
  public static final int {name}_SLOTS = {SCHEMA[datatype]['slots']};"""
  return out+"\n"

def get_metainfo_update():
  out = """"""
  for metadatum,metainfo in METAINFO.items():
    if 'update' in metainfo and metainfo['update']:
      # print(metadatum.partition('_'))
      subInfo = metadatum.partition('_')[0] + 'Info'
      datum = capitalize(metadatum.partition('_')[2])
      out += f"""      {subInfo}.update{datum}(read{capitalize(metadatum)}());
"""
  return out

def gen():
  out = """"""""
  bits_so_far = 0
  for datatype in SCHEMA:
    datatype_bits = sum(SCHEMA[datatype]['bits'].values())
    print(f"{datatype} has {datatype_bits} bits")
    prefix_bits = 0

    for attribute,attribute_bits,publicity in get_generated_attrs(SCHEMA[datatype]):
      # if attribute == 'all':
      #   attribute_bits = datatype_bits
      #   prefix_bits = 0
      # else:
      #   attribute_bits = SCHEMA[datatype]['bits'][attribute]

      print(f"  {attribute} has {attribute_bits} bits and publicity {publicity}")
      # read function
      rets = []
      for idx in range(SCHEMA[datatype]['slots']):
        start_bit = bits_so_far + datatype_bits*idx + prefix_bits
        # we want to read attribute_bits starting from start_bit
        start_int = start_bit // 16
        rem = start_bit % 16
        end_int = (start_bit + attribute_bits - 1) // 16
        ret = ""
        if start_int == end_int:
          bitstring = '1' * attribute_bits + '0' * (16 - attribute_bits - rem)
          ret = f"(rc.readSharedArray({start_int}) & {int(bitstring, 2)}) >>> {(16 - attribute_bits - rem)}"
        else:
          part1_bitstring = '1' * (16 - rem)
          part2_bitstring = '1' * (attribute_bits + rem - 16) + '0' * (32 - attribute_bits - rem)
          ret = f"((rc.readSharedArray({start_int}) & {int(part1_bitstring, 2)}) << {(attribute_bits + rem - 16)}) + ((rc.readSharedArray({end_int}) & {int(part2_bitstring, 2)}) >>> {(32 - attribute_bits - rem)})"
        rets.append(ret)

      if SCHEMA[datatype]['slots'] == 1:
        out += f"""
  {publicity} int read{capitalize(datatype)}{capitalize(attribute)}() throws GameActionException {{
    return {rets[0]};
  }}
"""
      else:
        out += f"""
  {publicity} int read{capitalize(datatype)}{capitalize(attribute)}(int idx) throws GameActionException {{
    switch (idx) {{"""
        for idx, ret in enumerate(rets):
          out += f"""
      case {idx}:
          return {ret};"""
        out += f"""
      default:
          return -1;
    }}
  }}
"""


      # write function
      writes = []
      for idx in range(SCHEMA[datatype]['slots']):
        start_bit = bits_so_far + datatype_bits*idx + prefix_bits
        # we want to write attribute_bits starting from start_bit
        start_int = start_bit // 16
        rem = start_bit % 16
        end_int = (start_bit + attribute_bits - 1) // 16
        write = []
        if start_int == end_int:
          bitstring = '1' * rem + '0' * attribute_bits + '1' * (16 - attribute_bits - rem)
          write.append(f"rc.writeSharedArray({start_int}, (rc.readSharedArray({start_int}) & {int(bitstring, 2)}) | (value << {(16 - attribute_bits - rem)}))")
        else:
          part1_bitstring = '1' * rem + '0' * (16 - rem)
          part2_bitstring = '0' * (attribute_bits + rem - 16) + '1' * (32 - attribute_bits - rem)
          value1_bitstring = '1' * (16 - rem) + '0' * (attribute_bits + rem - 16)
          value2_bitstring = '1' * (attribute_bits + rem - 16)
          write.append(f"rc.writeSharedArray({start_int}, (rc.readSharedArray({start_int}) & {int(part1_bitstring, 2)}) | ((value & {int(value1_bitstring, 2)}) >>> {(attribute_bits + rem - 16)}))")
          write.append(f"rc.writeSharedArray({end_int}, (rc.readSharedArray({end_int}) & {int(part2_bitstring, 2)}) | ((value & {int(value2_bitstring, 2)}) << {(32 - attribute_bits - rem)}))")
        writes.append(write)

      if SCHEMA[datatype]['slots'] == 1:
        out += f"""
  {publicity} void write{capitalize(datatype)}{capitalize(attribute)}(int value) throws GameActionException {{"""
        for w in writes[0]:
          out += f"""
    {w};"""
        out += f"""
  }}
"""
      else:
        out += f"""
  {publicity} void write{capitalize(datatype)}{capitalize(attribute)}(int idx, int value) throws GameActionException {{
    switch (idx) {{"""
        for idx, write in enumerate(writes):
          out += f"""
      case {idx}:"""
          for w in write:
            out += f"""
        {w};"""
          out += f"""
        break;"""
        out += f"""
    }}
  }}
"""

      prefix_bits += attribute_bits

    # special attrs
    for attribute in get_special_attrs(SCHEMA[datatype]['bits']):
      if is_suffix_special(attribute):
        suffix,special_attr_info = get_suffix_info(attribute)
        prefix = attribute[:-len(suffix)]
        read_conversion_format_str = special_attr_info['read_conversion']
        method_name_attr = prefix + special_attr_info['method_suffix']
        # read
        if SCHEMA[datatype]['slots'] == 1:
          out += f"""
  public {special_attr_info['datatype']} read{capitalize(datatype)}{capitalize(method_name_attr)}() throws GameActionException {{
    return {read_conversion_format_str.format(', '.join(f"read{capitalize(datatype)}{capitalize(prefix + new_suffix)}()" for new_suffix in special_attr_info['new_suffixes']))};
  }}"""
        else:
          out += f"""
  public {special_attr_info['datatype']} read{capitalize(datatype)}{capitalize(method_name_attr)}(int idx) throws GameActionException {{
    return {read_conversion_format_str.format(', '.join(f"read{capitalize(datatype)}{capitalize(prefix + new_suffix)}(idx)" for new_suffix in special_attr_info['new_suffixes']))};
  }}"""
        #write
        if SCHEMA[datatype]['slots'] == 1:
          out += f"""
  public void write{capitalize(datatype)}{capitalize(method_name_attr)}({special_attr_info['datatype']} value) throws GameActionException {{"""
          for new_suffix,suffix_info in special_attr_info['new_suffixes'].items():
            suffix_write_format_str = suffix_info[1]
            out += f"""
    write{capitalize(datatype)}{capitalize(prefix + new_suffix)}({suffix_write_format_str.format('value')});"""
          out += f"""
  }}"""
        else:
          out += f"""
  public void write{capitalize(datatype)}{capitalize(method_name_attr)}(int idx, {special_attr_info['datatype']} value) throws GameActionException {{"""
          for new_suffix,suffix_info in special_attr_info['new_suffixes'].items():
            suffix_write_format_str = suffix_info[1]
            out += f"""
    write{capitalize(datatype)}{capitalize(prefix + new_suffix)}(idx, {suffix_write_format_str.format('value')});"""
          out += f"""
  }}"""

    bits_so_far += datatype_bits * SCHEMA[datatype]['slots']
  # remove redundant shifts
  out = out.replace(" >>> 0", "")
  out = out.replace(" << 0", "")
  print("Total bit usage: " + str(bits_so_far))
  return out.rstrip() + "\n"

def capitalize(s):
  return ''.join(x.capitalize() for x in s.split('_'))

if __name__ == '__main__':
  assert sum(sum(SCHEMA[datatype]['bits'].values()) for datatype in SCHEMA) <= MAX_BITS, "Too many bits!"
  template_file = Path('./scripts/CommsHandlerTemplate.java')
  out_file = Path('./src/') / sys.argv[1] / 'communications' / 'CommsHandler.java'
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