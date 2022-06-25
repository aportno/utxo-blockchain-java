package chapter4;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

/*
THIS SCRIPT USES AES FOR EDUCATIONAL PURPOSES ONLY
 */

public class TestCipher_1 {
    public static void main(String[] args) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");

        keyGen.init(secRand);

        SecretKey key = keyGen.generateKey();

        cipher.init(Cipher.ENCRYPT_MODE, key);

        String message = "If you were a drop of tear in my eyes";
        byte[] cipherText = cipher.doFinal(message.getBytes());
        Cipher cipher2 = Cipher.getInstance("AES");

        cipher2.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded = cipher2.doFinal(cipherText);
        String decodedMessage = new String(decoded);

        System.out.println(decodedMessage);
    }
}
