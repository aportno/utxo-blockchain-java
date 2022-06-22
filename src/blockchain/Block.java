package blockchain;
import java.util.ArrayList;
import java.util.Calendar;

public class Block implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private int difficultyLevel = 20;
    private ArrayList<String> transactions = new ArrayList<String>();
    private long timestamp;
    private String previousBlockHashID;
    private int nonce = 0;
    private String hashID;

    public Block(String previousBlockHashID, int difficultyLevel) {
        this.previousBlockHashID = previousBlockHashID;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.difficultyLevel = difficultyLevel;
    }

    protected String computeHashID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.previousBlockHashID + Long.toHexString(this.timestamp));
        for (String t : transactions) {
            sb.append(t);
        }
        sb.append(Integer.toHexString(this.difficultyLevel) + nonce);
        byte[] b = UtilityMethods.messageDigestSHA256_toBytes(sb.toString());
        return UtilityMethods.toBinaryString(b);
    }

    public void addTransaction (String s) {
        this.transactions.add(s);
    }

    public String getHashID() {
        return this.hashID;
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

    protected boolean mineTheBlock() {
        this.hashID = this.computeHashID();
        while (!UtilityMethods.hashMeetsDifficultyLevel(this.hashID, this.difficultyLevel)) {
            this.nonce++;
            this.hashID = this.computeHashID();
        }
        return true;
    }

    public int getDifficultyLevel() {
        return this.difficultyLevel;
    }
}
