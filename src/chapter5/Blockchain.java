package chapter5;

import java.io.Serial;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final double MINING_REWARD = 100.0;
    private final LedgerList<Block> blockchain;

    public Blockchain(Block genesisBlock) {
        this.blockchain = new LedgerList<>();
        this.blockchain.add(genesisBlock);
    }

    public synchronized void addBlock(Block block) {
        if (block.getPreviousBlockHashID().equals(this.getLastBlock().getHashID())) {
            this.blockchain.add(block);
        }
    }

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent, ArrayList<UTXO> unspent, ArrayList<Transaction> sentTransactions) {
        // counter to track UTXO received (gain) and UTXO sent (spent)
        double gain = 0.0, spending = 0.0;
        HashMap<String, UTXO> map = new HashMap<>();

        // for-loop boundary
        int limit = this.getBlockchainSize();

        // loop through each block in the blockchain
        for (int i = 0; i < limit; i++) {
            // select block in blockchain
            Block block = this.blockchain.findByIndex(i);

            // find number of transactions in block
            int blockSize = block.getTotalNumberOfTransactions();

            // loop through each transaction in the block
            for (int j = 0; j < blockSize; j++) {
                Transaction transaction = block.getTransaction(j);

                // check if input UTXO sender address is from designated public key (wallet address). Skip genesis block.
                int n;
                if (i != 0 && transaction.getSender().equals(publicKey)) {

                    // count number of input UTXOs involved in the transaction -- these will be spent UTXOs
                    n = transaction.getNumberOfInputUTXOs();

                    // loop through the input UTXO in the transaction
                    for (int k = 0; k < n; k++) {

                        // retrieve the input UTXO
                        UTXO inputUTXO = transaction.getInputUTXO(k);

                        // add the input UTXO to the spent arrayList
                        spent.add(inputUTXO);

                        // map the input UTXO hash ID with the input UTXO object
                        map.put(inputUTXO.getHashID(), inputUTXO);

                        // increment the spend variable with the total amount transferred by the input UTXO
                        spending += inputUTXO.getAmountTransferred();
                    }

                    // record the transaction in the sent transaction arrayList
                    sentTransactions.add(transaction);
                }

                // count number of output UTXOs involved in the transaction -- these will be gained UTXOs
                n = transaction.getNumberOfOutputUTXOs();

                // loop through the output UTXO in the transaction
                for (int k = 0; k < n; k++) {

                    // retrieve the output UTXO
                    UTXO outputUTXO = transaction.getOutputUTXO(k);

                    // check if output UTXO receiver address is from designated public key (wallet address)
                    if (outputUTXO.getReceiver().equals(publicKey)) {

                        // add the output UTXO to the all arrayList
                        all.add(outputUTXO);

                        // increment the gain variable with the total amount transferred by the output UTXO
                        gain += outputUTXO.getAmountTransferred();
                    }
                }
            }
        }

        // loop through each output UTXO in the all arrayList
        for (UTXO utxo : all) {

            // check if the output UTXO is inside the all arrayList. If it is, it is a spent UTXO. If it isn't then it is an unspent UTXO
            if (!map.containsKey(utxo.getHashID())) {

                // add the unspent UTXO object to the unspent arrayList
                unspent.add(utxo);
            }
        }

        return (gain - spending);
    }

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent, ArrayList<UTXO> unspent) {
        ArrayList<Transaction> sendingTransactions = new ArrayList<>();

        return findRelatedUTXOs(publicKey, all, spent, unspent, sendingTransactions);
    }

    public ArrayList<UTXO> findUnspentUTXOs(PublicKey publicKey) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();
        findRelatedUTXOs(publicKey, all, spent, unspent);

        return unspent;
    }

    public double findUnspentUTXOs(PublicKey publicKey, ArrayList<UTXO> unspent) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();

        return findRelatedUTXOs(publicKey, all, spent, unspent);
    }

    public double checkBalance(PublicKey key) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();

        return findRelatedUTXOs(key, all, spent, unspent);
    }

    public Block getGenesisBlock() {
        return this.blockchain.getFirst();
    }

    public Block getLastBlock() {
        return this.blockchain.getLast();
    }

    public int getBlockchainSize() {
        return this.blockchain.size();
    }

    public Block getBlock(int index) {
        return this.blockchain.findByIndex(index);
    }
}
