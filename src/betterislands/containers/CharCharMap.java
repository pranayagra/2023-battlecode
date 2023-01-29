package betterislands.containers;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

import javax.xml.bind.annotation.XmlType;

public class CharCharMap {
  private StringBuilder keys;
  private StringBuilder values;

  public static final char DEFAULT_CHAR = (char) -1;

  public CharCharMap() {
    keys = new StringBuilder();
    values = new StringBuilder();
  }

  public boolean contains(char key) {
    return keys.indexOf(String.valueOf(key)) != -1;
  }

  public char get(char key) {
    int index = keys.indexOf(String.valueOf(key));
    if (index == -1) {
      throw new IllegalArgumentException("Key not found: " + ((int)key));
    }
    return values.charAt(index);
  }
  public MapLocation getMapLocationOrDefault(char key, MapLocation fallback) {
    char mapLoc = getOrDefault(key);
    if (mapLoc == DEFAULT_CHAR) return fallback;
    return new MapLocation(mapLoc / GameConstants.MAP_MAX_HEIGHT, mapLoc % GameConstants.MAP_MAX_HEIGHT);
  }
  public char getOrDefault(char key) {
    int index = keys.indexOf(String.valueOf(key));
    if (index == -1) {
      return DEFAULT_CHAR;
    }
    return values.charAt(index);
  }

  public void put(char key, MapLocation loc) {
    this.put(key, (char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y));
  }

  public void put(char key, char value) {
    int index = keys.indexOf(String.valueOf(key));
    if (index == -1) {
      keys.append(key);
      values.append(value);
    } else {
      values.setCharAt(index, value);
    }
  }

  public void clear() {
    keys = new StringBuilder();
    values = new StringBuilder();
  }
}
