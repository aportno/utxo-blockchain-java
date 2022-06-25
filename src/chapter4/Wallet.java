package chapter4;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {
    private KeyPair keyPair;
    private String walletName;
    private static String keyLocation = "keys";

    public Wallet(String walletName) {
        this.keyPair = UtilityMethods.generateKeyPair();
        this.walletName = walletName;
    }

    public String getName() {
        return this.walletName;
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }
}
