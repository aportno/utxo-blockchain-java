package chapter9;

import java.io.Serial;
import java.security.PublicKey;

public record KeyNamePair(PublicKey publicKey, String walletName) implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
