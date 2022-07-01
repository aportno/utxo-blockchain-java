package chapter6;

import java.io.Serial;
import java.security.PublicKey;

public class UTXOAsMiningReward extends UTXO {
    @Serial
    private static final long serialVersionUID = 1L;

    public UTXOAsMiningReward(String parentTransactionID, PublicKey sender, PublicKey receiver, double amountToTransfer) {
        super(parentTransactionID, sender, receiver, amountToTransfer);
    }

    public boolean isMiningReward() { return true; }
}
