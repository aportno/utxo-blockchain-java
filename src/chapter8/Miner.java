package chapter8;

public class Miner extends Wallet {

    public Miner(String minerName, String password) {
        super(minerName, password);
    }

    public Miner(String minerName) { super(minerName); }

    public boolean isMinedBlock(Block block) {
        if (block.isMinedBlock(this.getPublicKey())) {
            byte[] signature = UtilityMethods.generateSignature(this.getPrivateKey(), block.getHashID());
            return block.isSignedBlock(this.getPublicKey(), signature);
        } else {
            return false;
        }
    }

    public boolean isAddedTransaction(Transaction tx, Block block) {
        if (this.isValidatedTransaction(tx)) {
            return block.isAddedTransaction(tx, this.getPublicKey());
        } else {
            return false;
        }
    }

    public boolean isDeletedTransaction(Transaction tx, Block block) {
        return block.isDeletedTransaction(tx, this.getPublicKey());
    }

    public boolean isGeneratedRewardTransaction(Block block) {
        double amount = Blockchain.MINING_REWARD + block.getTransactionFeeAmount();
        Transaction tx = new Transaction(this.getPublicKey(), this.getPublicKey(), amount, null);
        UTXO miningRewardUTXO = new UTXOAsMiningReward(tx.getHashID(), tx.getSender(), this.getPublicKey(), amount);

        tx.addOutputUTXO(miningRewardUTXO);
        tx.signTheTransaction(this.getPrivateKey());
        return block.isGeneratedRewardTransaction(this.getPublicKey(), tx);
    }

    public Block createNewBlock(Blockchain ledger, int difficultyLevel) {
        return new Block(ledger.getLastBlock().getHashID(), difficultyLevel, this.getPublicKey());
    }
}
