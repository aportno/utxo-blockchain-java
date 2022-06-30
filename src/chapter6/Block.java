package chapter6;

import java.io.Serial;
import java.util.ArrayList;
import java.security.PublicKey;

public class Block implements java.io.Serializable {
    public final static int TRANSACTION_UPPER_LIMIT = 100;
    public final static int TRANSACTION_LOWER_LIMIT = 1;

    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private String hashID;
    private final String previousBlockHashID;
    private final long timestamp;
    private int nonce = 0;
    private final int difficultyLevel;
    private PublicKey creator;
    private boolean isMined = false;
    private byte[] signature;
    private Transaction rewardTransaction;

    public Block(String previousBlockHashID, int difficultyLevel, PublicKey creator) {
        this.previousBlockHashID = previousBlockHashID;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.difficultyLevel = difficultyLevel;
        this.creator = creator;
    }

    public boolean addTransaction (Transaction transaction, PublicKey publicKey) {
        if (this.getTotalNumberOfTransactions() >= Block.TRANSACTION_UPPER_LIMIT) {
            return false;
        } else if (publicKey.equals(this.getCreator()) && !this.isMined() && !this.isSigned()) {
            this.transactions.add(transaction);
            return true;
        } else {
            return false;
        }
    }

    protected String computeHashID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.previousBlockHashID).append(Long.toHexString(this.timestamp)).append(this.computeMerkleRoot()).append(nonce);
        byte[] b = UtilityMethods.messageDigestSHA256_toBytes(sb.toString());
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

    public boolean isDeletedTransaction(Transaction transaction, PublicKey publicKey) {
        if (!this.isMined && !this.isSigned() && publicKey.equals(this.getCreator())) {
            return this.transactions.remove(transaction);
        } else {
            return false;
        }
    }

    public boolean isDeletedTransaction(int index, PublicKey publicKey) {
        if (!this.isMined && !this.isSigned() && publicKey.equals(this.getCreator())) {
            Transaction transaction = this.transactions.remove(index);
            return (transaction != null);
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

    public boolean isVerifiedSignature(PublicKey publicKey, byte[] signature) {
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

    public int getNonce() {
        return this.nonce;
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
