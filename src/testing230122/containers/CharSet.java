package testing230122.containers;

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

  public void add(char number) {
    builder.append(number);
//    buffer.append(number);
//    string += number;
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
}
