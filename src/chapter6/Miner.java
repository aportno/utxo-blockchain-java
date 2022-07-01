package chapter6;

public class Miner extends Wallet {

    public Miner(String minerName, String password) {
        super(minerName, password);
    }

    public Miner(String minerName) { super(minerName); }

    public boolean mineBlock(Block block) {
        if (block.isMinedBlock(this.getPublicKey())) {
            byte[] signature = UtilityMethods.generateSignature(this.getPrivateKey(), block.getHashID());
            return block.isSignedBlock(this.getPublicKey(), signature);
        } else {
            return false;
        }
    }
}
