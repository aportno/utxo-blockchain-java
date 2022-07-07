package chapter8;

import java.security.PublicKey;

public class KeyNamePair implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private PublicKey publicKey;
    private String walletName;

    public KeyNamePair(PublicKey publicKey, String walletName) {
        this.publicKey = publicKey;
        this.walletName = walletName;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getWalletName() {
        return walletName;
    }
}
