package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public class KeyNamePair implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final PublicKey publicKey;
    private final String walletName;

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
