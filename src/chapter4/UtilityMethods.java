package chapter4;

import jdk.jshell.execution.Util;

import java.io.PrintStream;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class UtilityMethods {
    public static long uniqueNumber = 0;

    public static long getUniqueNumber() {
        return UtilityMethods.uniqueNumber++;
    }

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
        } catch (NoSuchAlgorithmException e) {
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

    public static byte[] encryptionByXOR(byte[] key, String password) {
        byte[] passwordToBytes = UtilityMethods.messageDigestSHA256_toBytes(password);
        byte[] result = new byte[key.length];

        for (int i = 0; i < key.length; i++) {
            int j = i % passwordToBytes.length;
            result[i] = (byte)((key[i] ^ passwordToBytes[j]) & 0xFF);
        }
        return result;
    }

    public static byte[] decryptionByXOR(byte[] key, String password) {
        return encryptionByXOR(key, password);
    }

    public static byte[] encryptionByAES(byte[] key, String password) {
        try {
            byte[] salt = new byte[8];
            SecureRandom rand = new SecureRandom();

            rand.nextBytes(salt);

            String keyAlgo = "PBKDF2WithHmacSHA1";
            SecretKeyFactory factory = SecretKeyFactory.getInstance(keyAlgo);
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
            SecretKey tempKey = factory.generateSecret(keySpec);
            SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            AlgorithmParameters algoParams = cipher.getParameters();
            byte[] iv = algoParams.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] output = cipher.doFinal(key);
            byte[] outputSizeBytes = UtilityMethods.intToBytes(output.length);
            byte[] ivSizeBytes = UtilityMethods.intToBytes(iv.length);
            byte[] data = new byte[Integer.BYTES * 2 + salet.length + iv.length + output.length];

            int j = 0;
            for (int i = 0; i < outputSizeBytes.length; i++, j++) {
                data[j] = outputSizeBytes[i];
            }

            for (int i = 0; i < ivSizeBytes.length; i++, j++) {
                data[j] = ivSizeBytes[i];
            }

            for (int i = 0; i < salt.length; i++, j++) {
                data[j] = salt[i];
            }

            for (int i = 0; i < iv.length; i++, j++) {
                data[j] = iv[i];
            }

            for (int i = 0; i < output.length; i++, j++) {
                data[j] = output[i];
            }

            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void displayTab(PrintStream out, int level, String str) {
        for (int i = 0; i < level; i++) {
            out.print("\t");
        }
        out.println(str);
    }

    public static void displayUTXO(UTXO utxo, PrintStream out, int level) {
        displayTab(out, level, "fund: "
                + utxo.getAmountTransferred()
                + ", receiver: "
                + UtilityMethods.getKeyString(utxo.getReceiver()));
    }

    public static void displayTransaction(Transaction transaction, PrintStream out, int level) {
        displayTab(out, level, "Transaction{");
        displayTab(out, level + 1, "ID: " + transaction.getHashID());
        displayTab(out, level + 1, "Sender: " + UtilityMethods.getKeyString(transaction.getSender()));
        displayTab(out, level + 1, "Amount to be transferred total: " + transaction.getTotalAmountToTransfer());
        displayTab(out, level + 1, "Input:");

        for (int i = 0; i < transaction.getNumberOfInputUTXOs(); i++) {
            UTXO utxo = transaction.getInputUTXO(i);
            displayUTXO(utxo, out, level + 2);
        }

        displayTab(out, level + 1, "Output:");

        for (int i = 0; i < transaction.getNumberOfOutputUTXOs() - 1; i++) {
            UTXO utxo = transaction.getOutputUTXO(i);
            displayUTXO(utxo, out, level + 2);
        }

        UTXO change = transaction.getOutputUTXO(transaction.getNumberOfOutputUTXOs() - 1);

        displayTab(out, level + 2, "Change: " + change.getAmountTransferred());
        displayTab(out, level + 1, "Transaction fee: " + Transaction.TRANSACTION_FEE);

        boolean isVerified = transaction.verifySignature();

        displayTab(out, level + 1, "Signature verification: " + isVerified);
        displayTab(out, level, "}");
    }
}
