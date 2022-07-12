package chapter9;

public final class Configuration {
    private static final String KEY_LOCATION = "keys";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEYPAIR_ALGORITHM = "RSA";
    private static final double MINING_REWARD = 100.0;
    private static final int PORT = 1118;
    private static final int SELF_BLOCKS_TO_MINE_LIMIT = 2;
    private static final int SIGN_IN_BONUS_USERS_LIMIT = 4;
    private static final int BLOCK_TRANSACTION_UPPER_LIMIT = 100;
    private static final int BLOCK_TRANSACTION_LOWER_LIMIT = 2;
    private static final int BLOCK_MINING_DIFFICULTY_LEVEL = 20;
    private static final int THREAD_SLEEP_TIME_SHORT = 100;
    private static final int THREAD_SLEEP_TIME_MEDIUM = 250;
    private static final int THREAD_SLEEP_TIME_LONG = 1000;
    private static final int LOG_BAR = 0;
    private static final int LOG_BAR_MAX = 10;
    private static final int LOG_BAR_MIN = 0;
    private static final int OUTGOING_CONNECTIONS_LIMIT = 6;
    private static final int INCOMING_CONNECTIONS_LIMIT = 20;
    private static final long MESSAGE_BURIED_TIME_LIMIT = 864_000_000;

    public static String getKeyLocation() {
        return KEY_LOCATION;
    }

    public static String getHashAlgorithm() {
        return HASH_ALGORITHM;
    }

    public static String getSignatureAlgorithm() {
        return SIGNATURE_ALGORITHM;
    }

    public static String getKeypairAlgorithm() {
        return KEYPAIR_ALGORITHM;
    }

    public static int getPORT() {
        return PORT;
    }

    public static int getBlockMiningDifficultyLevel() {
        return BLOCK_MINING_DIFFICULTY_LEVEL;
    }

    public static int getSignInBonusUsersLimit() {
        return SIGN_IN_BONUS_USERS_LIMIT;
    }

    public static int getSelfBlocksToMineLimit() {
        return SELF_BLOCKS_TO_MINE_LIMIT;
    }

    public static int getBlockTransactionUpperLimit() {
        return BLOCK_TRANSACTION_UPPER_LIMIT;
    }

    public static int getBlockTransactionLowerLimit() {
        return BLOCK_TRANSACTION_LOWER_LIMIT;
    }

    public static double getMiningReward() { return MINING_REWARD; }

    public static long getMessageBuriedTimeLimit() {
        return MESSAGE_BURIED_TIME_LIMIT;
    }

    public static int getThreadSleepTimeShort() {
        return THREAD_SLEEP_TIME_SHORT;
    }

    public static int getThreadSleepTimeMedium() {
        return THREAD_SLEEP_TIME_MEDIUM;
    }

    public static int getThreadSleepTimeLong() {
        return THREAD_SLEEP_TIME_LONG;
    }

    public static int getLogBar() {
        return LOG_BAR;
    }

    public static int getLogBarMax() {
        return LOG_BAR_MAX;
    }

    public static int getLogBarMin() {
        return LOG_BAR_MIN;
    }

    public static int getOutgoingConnectionsLimit() {
        return OUTGOING_CONNECTIONS_LIMIT;
    }

    public static int getIncomingConnectionsLimit() {
        return INCOMING_CONNECTIONS_LIMIT;
    }
}
