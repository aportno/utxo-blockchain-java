package chapter4;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/*
THIS SCRIPT USES RSA
 */

public class TestCipher_2 {
    public static void main(String[] args) throws Exception {
        String msg = "If you were a drop of tear in my eyes, I will never cry";
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        keyPairGen.initialize(4096);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

        byte[] first_byte = cipher.doFinal(msg.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

        byte[] second_byte = cipher.doFinal(first_byte);

        System.out.println(new String(second_byte));
    }
}
