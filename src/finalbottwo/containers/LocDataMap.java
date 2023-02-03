package finalbottwo.containers;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class LocDataMap<V> {
  StringBuilder keys;
  public V[] values;
  public int size;

  public LocDataMap(V[] storage) {
    keys = new StringBuilder(storage.length);
    values = storage;
  }

  public boolean contains(MapLocation loc) {
    return keys.indexOf(String.valueOf((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y))) != -1;
  }

  public V get(MapLocation loc) {
    int index = keys.indexOf(String.valueOf((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y)));
    if (index == -1) return null;
    return values[index];
  }
  public V getOrDefault(char key, V fallback) {
    int index = keys.indexOf(String.valueOf(key));
    if (index == -1) {
      return fallback;
    }
    return values[index];
  }

  public void put(MapLocation loc, V value) {
    int index = keys.indexOf(String.valueOf((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y)));
    if (index == -1) {
      keys.append((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y));
      values[size++] = value;
    } else {
      values[index] = value;
    }
  }

  public V[] getValues() {
    return values;
  }

  public void clear() {
    keys = new StringBuilder();
  }
}
