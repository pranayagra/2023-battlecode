package nogotofriend.robots.micro;

public class MicroConstants {
  public static final int MIN_GROUP_SIZE_TO_MOVE = 3; // min group size to move out
  public static final int TURNS_TO_WAIT = 20; // turns to wait until going back to nearest HQ
  public static final int TURNS_AT_TARGET = 20;
  public static final int CRITICAL_HEALTH = 10;
  public static final int ATTACK_TURN = 150;
  public static final int MAX_MICRO_BYTECODE_REMAINING = 2000;
  public static final double MAX_COOLDOWN_DIFF = 1.2; // TODO: i have no idea what this should be
  public static final double BIG_WIN_HEALTH_MULTIPLIER = 2.5; // how much more our health has to be than theirs to yeet in
  public static final double BIG_LOSE_HEALTH_MULTIPLIER = 1; // how much less our health has to be than theirs to yeet out
  public static final int CARRIER_TURNS_TO_FLEE = 6;
  public static final int CARRIER_DIST_TO_HQ_TO_RUN_HOME = 10;
  public static final int TURNS_SCALAR_TO_GIVE_UP_ON_TARGET_APPROACH = 3;
}

