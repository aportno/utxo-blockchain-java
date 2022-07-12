package chapter9;

import java.io.Serial;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final LedgerList<Block> blockchain;

    private Blockchain(LedgerList<Block> blockchain) {
        this.blockchain = new LedgerList<>();
        int chainSize = blockchain.size();
        for (int i = 0; i < chainSize; i++) {
            this.blockchain.add(blockchain.findByIndex(i));
        }
    }

    public Blockchain(Block genesisBlock) {
        this.blockchain = new LedgerList<>();
        this.blockchain.add(genesisBlock);
    }

    public synchronized Blockchain copy_NotDeepCopy() {
        return new Blockchain(this.blockchain);
    }

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent,
                                   ArrayList<UTXO> unspent, ArrayList<Transaction> sentTransactions, ArrayList<UTXO> rewards) {
        // counter to track UTXO received (gain) and UTXO sent (spent)
        double gain = 0.0, spending = 0.0;

        // map will contain all spent input UTXOs from wallet
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

                // check if sender address in transaction is from designated public key (wallet address). Skip genesis block.
                int n;
                if (i != 0 && transaction.getSender().equals(publicKey)) {

                    // first we count the number of input UTXOs involved in the transaction -- these will be spent UTXOs
                    n = transaction.getNumberOfInputUTXOs();

                    // loop through each input UTXO in each transaction
                    for (int k = 0; k < n; k++) {

                        // retrieve the input UTXO
                        UTXO inputUTXO = transaction.getInputUTXO(k);

                        // add the input UTXO to the spent arrayList -- we know this input UTXO was sent from the wallet address
                        spent.add(inputUTXO);

                        // map the input UTXO hash ID with the input UTXO object to record the spent UTXO -- this hash map contains all spent input UTXOs from the wallet
                        map.put(inputUTXO.getHashID(), inputUTXO);

                        // increment the spend variable with the total amount transferred by the input UTXO
                        spending += inputUTXO.getAmountTransferred();
                    }

                    // record the history of the transaction in the sentTransaction arrayList
                    sentTransactions.add(transaction);
                } // all transactions where the input UTXO has the sender as the public key have been checked

                // now we count the number of output UTXOs involved in the transaction -- these will be gained UTXOs
                n = transaction.getNumberOfOutputUTXOs();

                // loop through the output UTXOs in each transaction
                for (int k = 0; k < n; k++) {

                    // retrieve the output UTXO
                    UTXO outputUTXO = transaction.getOutputUTXO(k);

                    // check if output UTXO receiver address is from designated public key (wallet address)
                    if (outputUTXO.getReceiver().equals(publicKey)) {

                        // add the output UTXO to the all arrayList -- this might include transaction where the sender and receiver of the UTXO is the wallet
                        all.add(outputUTXO);

                        // increment the gain variable with the total amount transferred by the output UTXO
                        gain += outputUTXO.getAmountTransferred();
                    }
                }
            }

            if (block.getCreator().equals(publicKey)) {
                Transaction rewardTx = block.getRewardTransaction();
                if (rewardTx != null && rewardTx.getNumberOfOutputUTXOs() > 0) {
                    UTXO outputUTXO = rewardTx.getOutputUTXO(0);
                    if (outputUTXO.getReceiver().equals(publicKey)) {
                        rewards.add(outputUTXO);
                        all.add(outputUTXO);
                        gain += outputUTXO.getAmountTransferred();
                    }
                }
            }
        }

        // loop through each output UTXO in the all arrayList
        for (UTXO utxo : all) {

            // check if the output UTXO is inside the hash map containing all spent input UTXOs
            if (!map.containsKey(utxo.getHashID())) { // If the output UTXO is not in the hash map of spent UTXOs, then it is an unspent input UTXO

                // add the unspent UTXO object to the unspent arrayList
                unspent.add(utxo);
            } // if the output UTXO is in the hash map of spent UTXOs, it is a spent UTXO
        }

        // return the difference between the UTXOs received and the UTXOs spent
        return (gain - spending);
    }

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent,
                                   ArrayList<UTXO> unspent, ArrayList<Transaction> sentTransactions) {
        ArrayList<UTXO> rewards = new ArrayList<>();

        return findRelatedUTXOs(publicKey, all, spent, unspent, sentTransactions, rewards);
    }

    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent, ArrayList<UTXO> unspent) {
        ArrayList<Transaction> sendingTransactions = new ArrayList<>();
        return findRelatedUTXOs(publicKey, all, spent, unspent, sendingTransactions);
    }

    public double findUnspentUTXOs(PublicKey publicKey, ArrayList<UTXO> unspent) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();

        return findRelatedUTXOs(publicKey, all, spent, unspent);
    }

    public double checkBalance(PublicKey publicKey) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();

        return findRelatedUTXOs(publicKey, all, spent, unspent);
    }

    public synchronized boolean isAddedBlock(Block block) {
        if (this.getBlockchainSize() == 0) {
            this.blockchain.add(block);
            return true;
        } else if (block.getPreviousBlockHashID().equals(this.getLastBlock().getHashID())) {
            this.blockchain.add(block);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidatedBlockchain(Blockchain ledger) {
        int limit = ledger.getBlockchainSize() - 1;
        for (int i = limit; i > 0; i--) {
            Block currentBlock = ledger.getBlock(i);
            boolean isVerifiedBlock = currentBlock.isVerifiedSignature(currentBlock.getCreator());
            if (!isVerifiedBlock) {
                System.out.println("validateBlockCHain(): block " + (i+1) + " signature is invalid.");
                return false;
            }

            isVerifiedBlock = UtilityMethods.hashMeetsDifficultyLevel(currentBlock.getHashID(), currentBlock.getDifficultyLevel()) && currentBlock.computeHashID().equals(currentBlock.getHashID());
            if (!isVerifiedBlock) {
                System.out.println("validateBlockChain(): block " + (i+1) + " had a bad hash");
            }

            Block previousBlock = ledger.getBlock(i-1);
            isVerifiedBlock = currentBlock.getPreviousBlockHashID().equals(previousBlock.getHashID());
            if (!isVerifiedBlock) {
                System.out.println("validateBlockChain(): block " + (i+1) + " previous block hash is invalid");
            }
        }

        Block genesisBlock = ledger.getGenesisBlock();
        boolean isVerifiedBlock = genesisBlock.isVerifiedSignature(genesisBlock.getCreator());
        if (!isVerifiedBlock) {
            System.out.println("validateBlockChain(): genesis block " + "has incorrect signature");
            return false;
        }

        isVerifiedBlock = UtilityMethods.hashMeetsDifficultyLevel(genesisBlock.getHashID(), genesisBlock.getDifficultyLevel()) && genesisBlock.computeHashID().equals(genesisBlock.getHashID());
        if (!isVerifiedBlock) {
            System.out.println("validateBlockChain(): genesis block has bad hash");
            return false;
        }
        return true;

    }

    protected boolean isTransactionExist (Transaction transaction) {
        int limit = this.blockchain.size() - 1;
        for (int i = limit; i > 0; i--) {
            Block block = this.blockchain.findByIndex(i);
            int numberTx = block.getTotalNumberOfTransactions();
            for (int j = 0; j < numberTx; j++) {
                Transaction transaction2 = block.getTransaction(j);
                if (transaction.equals(transaction2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public PublicKey getGenesisMiner() {
        return this.getGenesisBlock().getCreator();
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
