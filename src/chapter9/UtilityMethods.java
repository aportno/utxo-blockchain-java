package chapter9;

import java.security.*;
import java.util.Base64;

public class UtilityMethods {
    public static long uniqueNumber = 0;

    public static long getUniqueNumber() {
        return UtilityMethods.uniqueNumber++;
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(Configuration.getKeypairAlgorithm());
            keyPairGen.initialize(2048);
            return keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateSignature(PrivateKey privateKey, String message) {
        try {
            Signature first_signature = Signature.getInstance(Configuration.getSignatureAlgorithm());

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
            Signature second_signature = Signature.getInstance(Configuration.getSignatureAlgorithm());

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
            MessageDigest md = MessageDigest.getInstance(Configuration.getHashAlgorithm());
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
        for (byte b : hash) {
            StringBuilder str = new StringBuilder(Integer.toBinaryString(b));
            while (str.length() < 8) {
                str.insert(0, "0");
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

    public static void displayTab(StringBuilder out, int level, String str) {
        out.append("\t".repeat(Math.max(0, level)));
        out.append(str).append(System.getProperty("line.separator"));
    }

    public static void displayUTXO(UTXO utxo, StringBuilder out, int level) {
        displayTab(out, level, "fund: "
                + utxo.getAmountTransferred()
                + ", receiver: "
                + UtilityMethods.getKeyString(utxo.getReceiver()));
    }

    public static void displayTransaction(Transaction transaction, StringBuilder out, int level) {
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

    public static void displayBlock(Block block, StringBuilder out, int level) {
        displayTab(out, level, "Block{");
        displayTab(out, level, "\tID: " + block.getHashID());

        for (int i = 0; i < block.getTotalNumberOfTransactions(); i++) {
            displayTransaction(block.getTransaction(i), out, level + 1);
        }

        if (block.getRewardTransaction() != null) {
            displayTab(out, level, "\tReward transaction: ");
            displayTransaction(block.getRewardTransaction(), out, level + 1);
        }

        displayTab(out, level, "}");
    }

    public static void displayBlockchain(Blockchain ledger, StringBuilder out, int level) {
        displayTab(out, level, "Blockchain{ number of blocks: " + ledger.getBlockchainSize());

        for (int i = 0; i < ledger.getBlockchainSize(); i++) {
            Block block = ledger.getBlock(i);
            displayBlock(block, out, level + 1);
        }

        displayTab(out, level, "}");
    }

    public static String computeMerkleTreeRootHash(String[] hashes) {
        // applies a recursive algorithm to generate the root hash starting from the tree leaves
        return computeMerkleTreeRootHash(hashes, 0, hashes.length - 1);
    }

    private static String computeMerkleTreeRootHash(String[] hashes, int from, int to) {
        // recursively builds up root hash
        if (to - from + 1 == 1) {
            return hashes[to];
        } else if (to - from + 1 == 2) {
            // compute the hashID from the two nodes below
            return messageDigestSHA256_toString(hashes[from] + hashes[to]);
        } else {
            // continue dividing the array into two parts to reach the leaves
            int mid = (from + to) / 2;
            String message = computeMerkleTreeRootHash(hashes, from, mid) + computeMerkleTreeRootHash(hashes, mid + 1, to);
            return messageDigestSHA256_toString(message);
        }
    }

    public static boolean isMessageTooOld(long timeStamp) {
        long currentTime = getTimeStamp();
        return (currentTime - timeStamp) >= Configuration.getMessageBuriedTimeLimit();
    }
}
