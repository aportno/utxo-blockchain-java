package chapter9;

public final class Configuration {
    private static final String KEY_LOCATION = "keys";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEYPAIR_ALGORITHM = "RSA";
    private static final int PORT = 1117;
    private static final int BLOCK_MINING_DIFFICULTY_LEVEL = 20;

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
}
