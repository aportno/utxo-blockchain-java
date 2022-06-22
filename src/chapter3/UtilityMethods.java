package chapter3;
import java.security.MessageDigest;
import java.util.Base64;

public class UtilityMethods {
    public static byte[] messageDigestSHA256_toBytes(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(message.getBytes());
            return md.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String messageDigestSHA256_toString(String message) {
        return Base64.getEncoder().encodeToString(messageDigestSHA256_toBytes(message));
    }

    public static long getTimeStamp() {
        return java.util.Calendar.getInstance().getTimeInMillis();
    }

    public static boolean hashMeetsDifficultyLevel(String hash, int difficultyLevel) {
        char[] chars = hash.toCharArray();
        for (int i = 0; i < difficultyLevel; i++) {
            if (chars[i] != '0') {
                return false;
            }
        }
        return true;
    }

    public static String toBinaryString(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            int num = ((int)hash[i]);
            String str = Integer.toBinaryString(num);
            while (str.length() < 8) {
                str = "0" + str;
            }
            sb.append(str);
        }
        return sb.toString();
    }
}
