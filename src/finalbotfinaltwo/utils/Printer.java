package finalbotfinaltwo.utils;

import finalbotfinaltwo.knowledge.Cache;

public class Printer {
    public static StringBuilder print = new StringBuilder();
    public static StringBuilder indicator = new StringBuilder();

    public static void print(String s) {
      print.append(s).append("\n");
    }

    public static void print(String s, String s2) {
      print(s); print(s2);
    }

    public static void print(String s, String s2, String s3) {
      print(s); print(s2); print(s3);
    }

    public static void print(String s, String s2, String s3, String s4) {
      print(s); print(s2); print(s3); print(s4);
    }

    public static void cleanPrint() {
      print = new StringBuilder();
      indicator = new StringBuilder();
      print.append(" *** ");
      print.append(Cache.PerTurn.CURRENT_LOCATION);
      print.append(" ***\n");
    }

    public static void submitPrint() {
      if (print.length() > 25) {
        System.out.println(print);
      }
//      if (Printer.indicator.toString().length() > 0) Printer.appendToIndicator(Printer.indicator.toString());
      Global.rc.setIndicatorString(Printer.indicator.toString());
      cleanPrint();
    }

    public static void appendToIndicator(String s) { indicator.append(s);}
}
