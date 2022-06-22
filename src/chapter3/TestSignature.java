package chapter3;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

/*
THIS SCRIPT USES RSA SHA-256
 */

public class TestSignature {
    public static void main(String[] args) throws Exception {
        String msg = "If you never come, how do I age alone?";
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        keyPairGen.initialize(2048);

        KeyPair keyPair = keyPairGen.generateKeyPair();
        Signature first_sig = Signature.getInstance("SHA256withRSA");
        first_sig.initSign(keyPair.getPrivate());
        first_sig.update(msg.getBytes());
        byte[] digitalSignature = first_sig.sign();

        System.out.println(new String(digitalSignature));
        Signature second_sig = Signature.getInstance("SHA256withRSA");
        second_sig.initVerify(keyPair.getPublic());
        second_sig.update(msg.getBytes());
        boolean verified = second_sig.verify(digitalSignature);

        System.out.println("Verified=" + verified);
    }
}
