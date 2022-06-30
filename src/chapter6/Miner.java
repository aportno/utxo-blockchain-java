package chapter6;

public class Miner extends Wallet {

    public Miner(String minerName, String password) {
        super(minerName, password);
    }

    public boolean mineBlock(Block block) {
        return (block.isMinedBlock());
    }
}
