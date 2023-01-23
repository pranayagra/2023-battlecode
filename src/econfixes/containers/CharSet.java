package econfixes.containers;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class CharSet {

  private StringBuilder builder;
//  private StringBuffer buffer;
//  private String string;

  public CharSet() {
    builder = new StringBuilder();
//    buffer = new StringBuffer();
//    string = "";
  }

  public boolean contains(char number) {
    return builder.indexOf(String.valueOf(number)) != -1;
//    return buffer.indexOf(String.valueOf(number)) != -1;
//    return string.contains(String.valueOf(number));
  }
  public boolean contains(MapLocation loc) {
    return builder.indexOf(String.valueOf((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y))) != -1;
  }

  public void add(char number) {
    builder.append(number);
//    buffer.append(number);
//    string += number;
  }
  public void add(MapLocation loc) {
    builder.append((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y));
  }

  public void clear() {
    builder = new StringBuilder();
//    buffer = new StringBuffer();
//    string = "";
  }

  public void remove(char number) {
    builder.deleteCharAt(builder.indexOf(String.valueOf(number)));
//    buffer.deleteCharAt(buffer.indexOf(String.valueOf(number)));
//    string = string.replace(String.valueOf(number), "");
  }
  public void remove(MapLocation loc) {
    builder.deleteCharAt(builder.indexOf(String.valueOf((char) (loc.x * GameConstants.MAP_MAX_HEIGHT + loc.y))));
  }
}
