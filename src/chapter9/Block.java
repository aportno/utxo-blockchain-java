package chapter9;

import java.io.Serial;
import java.security.PublicKey;
import java.util.ArrayList;

public class Block implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private String hashID;
    private final String previousBlockHashID;
    private final long timestamp;
    private long nonce = Long.MIN_VALUE;
    private final int difficultyLevel;
    private final PublicKey creator;
    private boolean isMined = false;
    private byte[] signature;
    private Transaction rewardTransaction;

    public Block(String previousBlockHashID, int difficultyLevel, PublicKey creator) {
        this.previousBlockHashID = previousBlockHashID;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.difficultyLevel = difficultyLevel;
        this.creator = creator;
    }

    protected String computeHashID() {
        byte[] b = UtilityMethods.messageDigestSHA256_toBytes(this.previousBlockHashID + Long.toHexString(this.timestamp) + this.computeMerkleRoot() + nonce);
        return UtilityMethods.toBinaryString(b);
    }

    private String computeMerkleRoot() {
        String[] hashes;

        if (this.rewardTransaction == null) {
            hashes = new String[this.transactions.size()];

            for (int i = 0; i < this.transactions.size(); i++) {
                hashes[i] = this.transactions.get(i).getHashID();
            }
        } else {
            hashes = new String[this.transactions.size() + 1];

            for (int i = 0; i < this.transactions.size(); i++) {
                hashes[i] = this.transactions.get(i).getHashID();
            }

            hashes[hashes.length - 1] = this.rewardTransaction.getHashID();
        }
        return UtilityMethods.computeMerkleTreeRootHash(hashes);
    }

    public boolean isAddedTransaction(Transaction transaction, PublicKey publicKey) {
        if (this.getTotalNumberOfTransactions() >= Configuration.getBlockTransactionUpperLimit()) {
            return false;
        } else if (publicKey.equals(this.getCreator()) && !this.isMined() && !this.isSigned()) {
            this.transactions.add(transaction);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isMinedBlock(PublicKey publicKey) {
        if (!this.isMined && publicKey.equals(this.getCreator())) {
            this.hashID = this.computeHashID();
            while (!UtilityMethods.hashMeetsDifficultyLevel(this.hashID, this.difficultyLevel)) {
                this.nonce++;
                this.hashID = this.computeHashID();
            }
            this.isMined = true;
        }
        return this.isMined;
    }

    public boolean isGeneratedRewardTransaction(PublicKey publicKey, Transaction rewardTransaction) {
        if (this.rewardTransaction == null && publicKey.equals(this.creator)) {
            this.rewardTransaction = rewardTransaction;
            return true;
        } else {
            return false;
        }
    }

    public boolean isSignedBlock(PublicKey publicKey, byte[] signature) {
        // Verify signatures before wallet/miner adds block to its local chain. Requires creator public key
        if (!isSigned()) {
            if (publicKey.equals(this.creator)) {
                if (UtilityMethods.verifySignature(publicKey, signature, this.getHashID())) {
                    this.signature = signature;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVerifiedSignature(PublicKey publicKey) {
        return UtilityMethods.verifySignature(publicKey, this.signature, this.getHashID());
    }

    public boolean isMined() {
        return this.isMined;
    }

    public boolean isSigned() {
        return this.signature != null;
    }

    public PublicKey getCreator() {
        return this.creator;
    }

    public Transaction getRewardTransaction() {
        return this.rewardTransaction;
    }

    public double getTransactionFeeAmount() {
        return this.transactions.size() * Transaction.TRANSACTION_FEE;
    }

    public Transaction getTransaction(int index) {
        return this.transactions.get(index);
    }

    public int getTotalNumberOfTransactions() {
        return this.transactions.size();
    }

    public int getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getPreviousBlockHashID() {
        return this.previousBlockHashID;
    }

    public String getHashID() {
        return this.hashID;
    }
}
