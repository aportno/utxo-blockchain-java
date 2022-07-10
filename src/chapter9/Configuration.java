package chapter9;

public final class Configuration {
    private static final String KEY_LOCATION = "keys";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEYPAIR_ALGORITHM = "RSA";
    private static final double MINING_REWARD = 100.0;
    private static final int PORT = 1117;
    private static final int SELF_BLOCKS_TO_MINE_LIMIT = 2;
    private static final int SIGN_IN_BONUS_USERS_LIMIT = 4;
    private static final int BLOCK_TRANSACTION_UPPER_LIMIT = 100;
    private static final int BLOCK_TRANSACTION_LOWER_LIMIT = 2;
    private static final int BLOCK_MINING_DIFFICULTY_LEVEL = 20;
    private static final int THREAD_SLEEP_TIME_SHORT = 100;
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

    public static double getMiningReward() {
        return MINING_REWARD;
    }

    public static long getMessageBuriedTimeLimit() {
        return MESSAGE_BURIED_TIME_LIMIT;
    }

    public static int getThreadSleepTimeShort() {
        return THREAD_SLEEP_TIME_SHORT;
    }

}
