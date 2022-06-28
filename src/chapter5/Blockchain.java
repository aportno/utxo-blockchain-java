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

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent, ArrayList<UTXO> unspent, ArrayList<Transaction> sentTransactions) {
        double gain = 0.0, spending = 0.0;
        HashMap<String, UTXO> map = new HashMap<>();
        int limit = this.getBlockchainSize();
        for (int i = 0; i < limit; i++) {
            Block block = this.blockchain.findByIndex(i);
            int blockSize = block.getTotalNumberOfTransactions();
            for (int j = 0; j < blockSize; j++) {
                Transaction transaction = block.getTransaction(j);
                int n;
                if (i != 0 && transaction.getSender().equals(publicKey)) {
                    n = transaction.getNumberOfInputUTXOs();
                    for (int k = 0; k < n; k++) {
                        UTXO inputUTXO = transaction.getInputUTXO(k);
                        spent.add(inputUTXO);
                        map.put(inputUTXO.getHashID(), inputUTXO);
                        spending += inputUTXO.getAmountTransferred();
                    }
                    sentTransactions.add(transaction);
                }
                n = transaction.getNumberOfOutputUTXOs();
                for (int k = 0; k < n; k++) {
                    UTXO outputUTXO = transaction.getOutputUTXO(k);
                    if (outputUTXO.getReceiver().equals(publicKey)) {
                        all.add(outputUTXO);
                        gain += outputUTXO.getAmountTransferred();
                    }
                }
            }
        }
        for (UTXO utxo : all) {
            if (!map.containsKey(utxo.getHashID())) {
                unspent.add(utxo);
            }
        }

        return (gain - spending);
    }

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent, ArrayList<UTXO> unspent) {
        ArrayList<Transaction> sendingTransactions = new ArrayList<>();

        return findRelatedUTXOs(publicKey, all, spent, unspent, sendingTransactions);
    }

    public double checkBalance(PublicKey key) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();

        return findRelatedUTXOs(key, all, spent, unspent);
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
}
