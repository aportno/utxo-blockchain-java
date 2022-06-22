package chapter3;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/*
THIS SCRIPT USES AES FOR EDUCATIONAL PURPOSES ONLY
 */

public class TestCipher_1 {
    public static void main(String[] args) throws Exception {
        Cipher first_cipher = Cipher.getInstance("AES");
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");

        keyGen.init(secRand);

        SecretKey key = keyGen.generateKey();

        first_cipher.init(Cipher.ENCRYPT_MODE, key);

        String message = "If you were a drop of tear in my eyes";
        byte[] cipherText = first_cipher.doFinal(message.getBytes());
        Cipher second_cipher = Cipher.getInstance("AES");

        second_cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded = second_cipher.doFinal(cipherText);
        String decodedMessage = new String(decoded);

        System.out.println(decodedMessage);
    }
}
