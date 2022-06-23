package chapter3;

import java.security.*;
import java.util.Base64;

public class UtilityMethods {
    public static long uniqueNumber = 0;

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateSignature(PrivateKey privateKey, String message) {
        try {
            Signature first_signature = Signature.getInstance("SHA256withRSA");

            try {
                first_signature.initSign(privateKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }

            try {
                first_signature.update(message.getBytes());
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }

            try {
                return first_signature.sign();
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(PublicKey publicKey, byte[] signature, String message) {
        try {
            Signature second_signature = Signature.getInstance("SHA256withRSA");

            try {
                second_signature.initVerify(publicKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }

            try {
                second_signature.update(message.getBytes());
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }

            try {
                return second_signature.verify(signature);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getKeyString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static long getTimeStamp() {
        return java.util.Calendar.getInstance().getTimeInMillis();
    }

    public static byte[] messageDigestSHA256_toBytes(String msg) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(msg.getBytes());
            return md.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String messageDigestSHA256_toString(String msg) {
        return Base64.getEncoder().encodeToString(messageDigestSHA256_toBytes(msg));
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
