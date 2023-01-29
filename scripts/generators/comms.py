from __future__ import annotations
from pathlib import Path
import re
import sys

NUM_INTS = 100
BITS_PER_INT = 16
MAX_BITS = NUM_INTS * BITS_PER_INT
RESOURCES = ['adamantium', 'mana', 'elixir']

LOCATION_KEY = 'loc'
BOOL_KEY = 'bool'
COUNTER_KEY = 'counter'

SPECIAL_ATTRS = []
SUFFIX_TO_GENERAL = {
  LOCATION_KEY: {
    'datatype': 'MapLocation',
    'new_suffixes': {
      'x': [6,'({}).x+1'], # format string to write property to comms
      'y': [6,'({}).y+1'],
    },
    'read_conversion': 'new MapLocation({}-1,{}-1)',
    'method_suffix': 'location',
    'extra_read_suffixes': {
      'exists': ['boolean','!(({}).equals(NONEXISTENT_MAP_LOC))'], # type, format string analyze result of reading
    },
    'extra_consts': [
      'public static final MapLocation NONEXISTENT_MAP_LOC = new MapLocation(-1,-1);',
    ]
  },
  BOOL_KEY: {
    'datatype': 'boolean',
    'new_suffixes': {
      'bit': [1,'(({}) ? 1 : 0)'], # format string to write property to comms
    },
    'read_conversion': '(({}) == 1)',
    'method_suffix': '',
  },
  COUNTER_KEY: {
    'datatype': 'int',
    'new_suffixes': {
      'bits': [0,'({})'], # format string to write property to comms
    },
    'read_conversion': '({})',
    'method_suffix': '',
    'no_default_write': True,
    'extra_write_keys': {
      'MAX_VALUE.bits': lambda bits: 2**bits-1,
      'ATTR': lambda attr: attr.upper(),
    },
    'extra_write_suffixes': { # [args, needed format keys, write format string]
      'reset': [[], [], '0'],
      'set': [['int value'], ['MAX_VALUE'], 'Math.min(Math.max(value, 0), {MAX_VALUE})'],
      'increment': [[], ['READ', 'MAX_VALUE'], 'Math.min(({READ})+1, {MAX_VALUE})'],
      'decrement': [[], ['READ'], 'Math.max(({READ})-1, 0)'],
    },
    'extra_consts': [
      'public static final int {ATTR}MAX_VALUE = {MAX_VALUE};',
    ]
  },
}
SPECIAL_ATTR_SUFFIXES = list(SUFFIX_TO_GENERAL.keys())
SPECIAL_ATTR_BITS = {suffix:sum(suffix_info[0] for suffix_info in general_info['new_suffixes'].values()) for suffix,general_info in SUFFIX_TO_GENERAL.items()}

LOCATION_BITS = SPECIAL_ATTR_BITS[LOCATION_KEY]
BOOL_BITS = SPECIAL_ATTR_BITS[BOOL_KEY]

METAINFO = {
  'hq_count': {
    'bits': 3,
  },
  'map_symmetry': {
    'bits': 3,
  },
}

WELL_SCHEMA = {
  'loc': LOCATION_BITS,
  'upgraded_bool': BOOL_BITS,
  'capacity_counter': 4, # TODO: optimize to 3 bits... this is currently from 0 - 8. Not sure how to make it 1-9 lol
  'current_workers_counter': 4,
}

MAIN_SCHEMA = {
  '': {
    'slots': 1,
    'bits': {key: value['bits'] for key,value in METAINFO.items()}
  },
  'our_hq': {
    'slots': 4,
    'bits': {
      'loc': LOCATION_BITS,
      'odd_spawn_loc': LOCATION_BITS,
      'even_spawn_loc': LOCATION_BITS,
      'odd_spawn_instruction': 2,
      'even_spawn_instruction': 2,
      'adamantium_income_counter': 5, # changing any of these income bit#'s require changing the max value checking done manually in HQ and Carrier.
      'mana_income_counter': 5,
      'elixir_income_counter': 5,
    }
  },
  'adamantium_well': {
    'slots': 4,
    'bits': {
      **WELL_SCHEMA,
    }
  },
  'mana_well': {
    'slots': 4,
    'bits': {
      **WELL_SCHEMA,
    }
  },
  'elixir_well': {
    'slots': 4,
    'bits': {
      **WELL_SCHEMA,
    }
  },
  'island_info': {
    'slots': 1,
    'bits': {
      'loc': LOCATION_BITS,
      'owner': 2,
      'round_num': 11,
      'island_id': 6,
      #'size': 2,
    }
  },
  'next_island_to_claim': { # maybe change to all neutral islands rotating
    'slots': 1,
    'bits': {
      'loc': LOCATION_BITS,
    }
  },
  'my_islands': {
    'slots': 1,
    'bits': {
      'loc': LOCATION_BITS,
      'round_num': 11,
      'island_id': 6,
    }
  },
  'enemy': {
    'slots': 19,
    'bits': {
      'odd_loc': LOCATION_BITS,
      'even_loc': LOCATION_BITS,
    }
  },
}

PTESTCARRIER_SCHEMA = {
  '': {
    'slots': 1,
    'bits': {key: value['bits'] for key,value in METAINFO.items()}
  },
  'our_hq': {
    'slots': 4,
    'bits': {
      'loc': LOCATION_BITS,
      # 'odd_spawn_loc': LOCATION_BITS,
      # 'even_spawn_loc': LOCATION_BITS,
      # 'odd_spawn_instruction': 2,
      # 'even_spawn_instruction': 2,
      'adamantium_income': 5, # changing any of these income bit#'s require changing the max value checking done manually in HQ and Carrier.
      'mana_income': 5,
      'elixir_income': 5,
    }
  },
  'pranay_our_hq': {
    'slots': 8,
    'bits': {
      'odd_spawn_loc': LOCATION_BITS,
      'even_spawn_loc': LOCATION_BITS,
      'odd_spawn_instruction': 2,
      'even_spawn_instruction': 2,
      'odd_target_loc': LOCATION_BITS,
      'even_target_loc': LOCATION_BITS,
    }
  },
  'pranay_well_info': {
    'slots': 4,
    'bits': {
      'loc': LOCATION_BITS,
      'type': 2,
      'num_miners': 4,
      'num_good_slots': 4,
    }
  },
  'adamantium_well': {
    'slots': 4,
    'bits': {
      **WELL_SCHEMA,
    }
  },
  'mana_well': {
    'slots': 4,
    'bits': {
      **WELL_SCHEMA,
    }
  },
  # 'elixir_well': {
  #   'slots': 4,
  #   'bits': {
  #     **WELL_SCHEMA,
  #   }
  # },
  'enemy': {
    'slots': 10,
    'bits': {
      'odd_loc': LOCATION_BITS,
      'even_loc': LOCATION_BITS,
    }
  },
  # 'attack_pod': {
  #   'slots': 10,
  #   'bits': {
  #     'amp_alive_bool': BOOL_BITS,
  #     'amp_loc': LOCATION_BITS,
  #     'launcher_registry': 6,
  #   }
  # },
}

SCHEMA = MAIN_SCHEMA

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
def get_raw_attrs(schema_datatype: dict) -> list[tuple[str,int,str]]:
  # [*SCHEMA[datatype]['bits'], 'all']
  attr_list = schema_datatype['bits'].keys()
  attrs = []
  for attr in attr_list:
    if is_special_attr(attr):
      if is_suffix_special(attr):
        suffix, general_info = get_suffix_info(attr)
        prefix = attr[:-len(suffix)]
        for new_suffix, (bits, _) in general_info['new_suffixes'].items():
          attrs.append((f"{prefix}{new_suffix}", bits if bits > 0 else schema_datatype['bits'][attr], 'private'))
    else:
      attrs.append((attr, schema_datatype['bits'][attr], 'public'))
  # attrs.append(('all',sum(schema_datatype['bits'].values()),'public'))
  # print('attrs:', attrs)
  return attrs

def get_generated_attrs(schema_datatype: dict) -> list[tuple[str,str,bool]]: # name, java type, can write
  attr_list = schema_datatype['bits'].keys()
  attrs = []
  for attr in attr_list:
    if is_special_attr(attr):
      if is_suffix_special(attr):
        suffix,special_attr_info = get_suffix_info(attr)
        prefix = attr[:-len(suffix)]
        method_name_attr = f"{prefix}{special_attr_info['method_suffix']}"
        attrs.append((f"{method_name_attr}", special_attr_info['datatype'], False))
        no_default_write = special_attr_info.get('no_default_write', False)
        if not no_default_write:
          attrs.append((f"{prefix}{special_attr_info['method_suffix']}", [f"{special_attr_info['datatype']} value"], True))
        if 'extra_read_suffixes' in special_attr_info:
          for extra_suffix, (extra_type, _) in special_attr_info['extra_read_suffixes'].items():
            attrs.append((f"{prefix}{extra_suffix}", extra_type, False))
        if 'extra_write_suffixes' in special_attr_info:
          for extra_suffix, (extra_args, _, _) in special_attr_info['extra_write_suffixes'].items():
            attrs.append((f"{f'{method_name_attr}_' if no_default_write else ''}{extra_suffix}", extra_args, True))
    else:
      attrs.append((attr, 'int', False))
      attrs.append((attr, ['int value'], True))
  # print('attrs:', attrs)
  return attrs

def gen_constants():
  out = """"""
  for datatype in SCHEMA:
    name = datatype.upper() if datatype else 'META'
    out += f"""
  public static final int {name}_SLOTS = {SCHEMA[datatype]['slots']};"""

  for suffix,suffix_info in SUFFIX_TO_GENERAL.items():
    if 'extra_consts' in suffix_info:
      join_str = "\n  "
      out += f"""
  {join_str.join(const_info for const_info in suffix_info['extra_consts'] if ('{' not in const_info and '}' not in const_info))}
"""
  return out+"\n"

def gen():
  out = """"""""
  bits_so_far = 0
  for datatype in SCHEMA:
    datatype_bits = sum(SCHEMA[datatype]['bits'].values())
    # print(f"{datatype} has {datatype_bits} bits")
    prefix_bits = 0

    for attribute,attribute_bits,publicity in get_raw_attrs(SCHEMA[datatype]):
      # print(f"  {attribute} has {attribute_bits} bits and publicity {publicity}")
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
  {publicity} static int read{capitalize(datatype)}{capitalize(attribute)}() throws GameActionException {{
    return {rets[0]};
  }}
"""
      else:
        out += f"""
  {publicity} static int read{capitalize(datatype)}{capitalize(attribute)}(int idx) throws GameActionException {{
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
  {publicity} static void write{capitalize(datatype)}{capitalize(attribute)}(int value) throws GameActionException {{"""
        for w in writes[0]:
          out += f"""
    {w};"""
        out += f"""
  }}
"""
      else:
        out += f"""
  {publicity} static void write{capitalize(datatype)}{capitalize(attribute)}(int idx, int value) throws GameActionException {{
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
  public static {special_attr_info['datatype']} read{capitalize(datatype)}{capitalize(method_name_attr)}() throws GameActionException {{
    return {read_conversion_format_str.format(*(f"read{capitalize(datatype)}{capitalize(prefix + new_suffix)}()" for new_suffix in special_attr_info['new_suffixes']))};
  }}"""
        else:
          out += f"""
  public static {special_attr_info['datatype']} read{capitalize(datatype)}{capitalize(method_name_attr)}(int idx) throws GameActionException {{
    return {read_conversion_format_str.format(*(f"read{capitalize(datatype)}{capitalize(prefix + new_suffix)}(idx)" for new_suffix in special_attr_info['new_suffixes']))};
  }}"""

        if 'extra_read_suffixes' in special_attr_info:
          for extra_suffix,(extra_type,analyzer_format_str) in special_attr_info['extra_read_suffixes'].items():
            # print(suffix,extra_suffix,extra_type,analyzer_format_str)
            if SCHEMA[datatype]['slots'] == 1:
              out += f"""
  public static {extra_type} read{capitalize(datatype)}{capitalize(prefix + extra_suffix)}() throws GameActionException {{
    return {analyzer_format_str.format(f"read{capitalize(datatype)}{capitalize(method_name_attr)}()")};
  }}"""
            else:
              out += f"""
  public static {extra_type} read{capitalize(datatype)}{capitalize(prefix + extra_suffix)}(int idx) throws GameActionException {{
    return {analyzer_format_str.format(f"read{capitalize(datatype)}{capitalize(method_name_attr)}(idx)")};
  }}"""
        #write
        no_default_write = 'no_default_write' in special_attr_info and special_attr_info['no_default_write']
        if not no_default_write:
          if SCHEMA[datatype]['slots'] == 1:
            out += f"""
  public static void write{capitalize(datatype)}{capitalize(method_name_attr)}({special_attr_info['datatype']} value) throws GameActionException {{"""
            for new_suffix,suffix_info in special_attr_info['new_suffixes'].items():
              suffix_write_format_str = suffix_info[1]
              out += f"""
    write{capitalize(datatype)}{capitalize(prefix + new_suffix)}({suffix_write_format_str.format('value')});"""
            out += f"""
  }}"""
          else:
            out += f"""
  public static void write{capitalize(datatype)}{capitalize(method_name_attr)}(int idx, {special_attr_info['datatype']} value) throws GameActionException {{"""
            for new_suffix,suffix_info in special_attr_info['new_suffixes'].items():
              suffix_write_format_str = suffix_info[1]
              out += f"""
    write{capitalize(datatype)}{capitalize(prefix + new_suffix)}(idx, {suffix_write_format_str.format('value')});"""
            out += f"""
  }}"""
        else:
          assert len(special_attr_info['new_suffixes']) == 1, f"no_default_write not supported for {attribute} (datatype {datatype})"
        if 'extra_write_suffixes' in special_attr_info:
          read_func = f"read{capitalize(datatype)}{capitalize(method_name_attr)}"
          write_func = f"write{capitalize(datatype)}{capitalize(method_name_attr)}" if not no_default_write else f"write{capitalize(datatype)}{capitalize(prefix + list(special_attr_info['new_suffixes'].keys())[0])}"
          # print(suffix,read_func)
          base_format_keys = {}
          if 'extra_write_keys' in special_attr_info:
            for key,value in special_attr_info['extra_write_keys'].items():
              if key.endswith('.bits'):
                base_format_keys[key[:-5]] = value(SCHEMA[datatype]['bits'][attribute])
              else:
                base_format_keys[key] = value
          for extra_suffix,(extra_args,needed_format_keys,analyzer_format_str) in special_attr_info['extra_write_suffixes'].items():
            format_keys = {k:base_format_keys[k] for k in needed_format_keys if k != 'READ'}
            # print(attribute,extra_suffix,extra_args,needed_format_keys,analyzer_format_str, end='\t')
            if SCHEMA[datatype]['slots'] == 1:
              if 'READ' in needed_format_keys:
                format_keys['READ'] = f"{read_func}()"
              # print(format_keys)
              out += f"""
  public static void write{capitalize(datatype)}{capitalize(method_name_attr + '_' + extra_suffix)}({', '.join(extra_args)}) throws GameActionException {{
    {write_func}({analyzer_format_str.format(**format_keys)});
  }}"""
            else:
              if 'READ' in needed_format_keys:
                format_keys['READ'] = f"{read_func}(idx)"
              # print(format_keys)
              out += f"""
  public static void write{capitalize(datatype)}{capitalize(method_name_attr + '_' + extra_suffix)}(int idx{', ' if extra_args else ''}{', '.join(extra_args)}) throws GameActionException {{
    {write_func}(idx, {analyzer_format_str.format(**format_keys)});
  }}"""
    bits_so_far += datatype_bits * SCHEMA[datatype]['slots']
  # remove redundant shifts
  out = out.replace(" >>> 0", "")
  out = out.replace(" << 0", "")
  print("Total bit usage: " + str(bits_so_far))
  return out.rstrip() + "\n"

def gen_resource():
  out = """"""
  resource_datatypes = list(set([re.sub(re.compile(f"({'|'.join(RESOURCES)})"), '{}', datatype) for datatype in SCHEMA if any(rss in datatype for rss in RESOURCES)]))
  # print('schemas for resources:',resource_datatypes)
  for datatype_fmt in resource_datatypes:
    valid_rss = [rss for rss in RESOURCES if datatype_fmt.format(rss) in SCHEMA]
    if not valid_rss:
      continue
    # ensure all rss have same format
    assert all(SCHEMA[datatype_fmt.format(rss)] == SCHEMA[datatype_fmt.format(valid_rss[0])] for rss in valid_rss[1:])

    print(f'{datatype_fmt}:  RSS:{valid_rss}')
    generic_name = re.sub(r'_?\{\}_?', '', datatype_fmt)
    base_type = datatype_fmt.format(valid_rss[0])
    for attribute,out_type,can_write in get_generated_attrs(SCHEMA[base_type]):
      # print(f"  {attribute} can be specialized by rss under schema for {generic_name} -- {can_write=}")
      can_read = not can_write
      # read
      if can_read:
        if SCHEMA[base_type]['slots'] == 1:
          out += f"""
    public {out_type} read{capitalize(generic_name)}{capitalize(attribute)}() throws GameActionException {{
      switch (this) {{"""
          for rss in valid_rss:
            out += f"""
        case {rss.upper()}:
          return CommsHandler.read{capitalize(datatype_fmt.format(rss))}{capitalize(attribute)}();"""
          out += f"""
        default:
          throw new RuntimeException("read{capitalize(generic_name)}{capitalize(attribute)} not defined for " + this);
      }}
    }}
"""
        else:
          out += f"""
    public {out_type} read{capitalize(generic_name)}{capitalize(attribute)}(int idx) throws GameActionException {{
      switch (this) {{"""
          for rss in valid_rss:
            out += f"""
        case {rss.upper()}:
          return CommsHandler.read{capitalize(datatype_fmt.format(rss))}{capitalize(attribute)}(idx);"""
          out += f"""
        default:
          throw new RuntimeException("read{capitalize(generic_name)}{capitalize(attribute)} not defined for " + this);
      }}
    }}
"""
      # write
      if can_write:
        # print("write",generic_name,attribute,out_type)
        if SCHEMA[base_type]['slots'] == 1:
          out += f"""
    public void write{capitalize(generic_name)}{capitalize(attribute)}({', '.join(out_type)}) throws GameActionException {{
      switch (this) {{"""
          for rss in valid_rss:
            out += f"""
        case {rss.upper()}:
          CommsHandler.write{capitalize(datatype_fmt.format(rss))}{capitalize(attribute)}({', '.join(t.split()[1] for t in out_type)});
          break;"""
          out += f"""
        default:
          throw new RuntimeException("write{capitalize(generic_name)}{capitalize(attribute)} not defined for " + this);
      }}
    }}
"""
        else:
          out += f"""
    public void write{capitalize(generic_name)}{capitalize(attribute)}(int idx{', ' if out_type else ''}{', '.join(out_type)}) throws GameActionException {{
      switch (this) {{"""
          for rss in valid_rss:
            out += f"""
        case {rss.upper()}:
          CommsHandler.write{capitalize(datatype_fmt.format(rss))}{capitalize(attribute)}(idx{', ' if out_type else ''}{', '.join(t.split()[1] for t in out_type)});
          break;"""
          out += f"""
        default:
          throw new RuntimeException("write{capitalize(generic_name)}{capitalize(attribute)} not defined for " + this);
      }}
    }}
"""

  return out.rstrip() + "\n"

def capitalize(s):
  return ''.join(x.capitalize() for x in s.split('_'))

if __name__ == '__main__':
  assert sum(sum(SCHEMA[datatype]['bits'].values()) for datatype in SCHEMA) <= MAX_BITS, "Too many bits!"
  template_file = Path('./scripts/CommsHandlerTemplate.java')
  out_file = Path('./src/') / sys.argv[1] / 'communications' / 'CommsHandler.java'
  with open(template_file, 'r') as t:
    with open(out_file, 'w') as f:
      old_root_pkg = None
      for line in t:
        if line.strip().startswith('package') and line.strip().endswith(';'):
          old_root_pkg = re.search(r'(?<=package )\w+', line).group(0)
        if '// CONSTS' in line:
          f.write(gen_constants())
        elif '// MAIN READ AND WRITE METHODS' in line:
          f.write(gen())
        elif '// RESOURCE READERS AND WRITERS' in line:
          f.write(gen_resource())
        else:
          f.write(line.replace(old_root_pkg, sys.argv[1]))