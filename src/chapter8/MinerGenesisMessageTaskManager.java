package chapter8;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MinerGenesisMessageTaskManager extends MinerMessageTaskManager implements Runnable {
    public static final int SELF_BLOCKS_TO_MINE_LIMIT = 2;
    public static final int SIGN_IN_BONUS_USERS_LIMIT = 1000;
    private int blocksMined = 0;
    private final int signInBonus = 1000;
    private HashMap<String, KeyNamePair> users = new HashMap<>();
    private ArrayList<KeyNamePair> waitingListForSignInBonus = new ArrayList<>();
    private WalletConnectionAgent agent;

    public MinerGenesisMessageTaskManager(
            WalletConnectionAgent agent,
            Miner miner,
            ConcurrentLinkedQueue<Message> messageConcurrentLinkedQueue) {
        super(agent, miner, messageConcurrentLinkedQueue);
        this.agent = agent;
    }

    /*
    The genesis miner will poll the service relay provider for new users time to time
     */

    public void toDo() {
        try {
            Thread.sleep(agent.sleepTime * 10);
            if (waitingListForSignInBonus.size() == 0 && users.size() < SIGN_IN_BONUS_USERS_LIMIT) {
                MessageTextPrivate mtp = new MessageTextPrivate(
                        Message.TEXT_ASK_ADDRESSES,
                        myWallet().getPrivateKey(),
                        myWallet().getPublicKey(),
                        myWallet().getName(),
                        this.agent.getServerAddress());
                Thread.sleep(agent.sleepTime * 10);
            } else {
                sendSignInBonus();
            }
        } catch (Exception e) {}
    }

    private void sendSignInBonus() {
        if (waitingListForSignInBonus.size() <= 0) {
            return;
        }

        KeyNamePair keyNamePair = waitingListForSignInBonus.remove(0);
        Transaction transaction = myWallet().transferFund(keyNamePair.getPublicKey(), signInBonus);
        if (transaction != null && transaction.verifySignature()) {
            System.out.println(myWallet().getName() + " is sending " + keyNamePair.getWalletName() + " sign-in bonus of " + signInBonus);
            if (blocksMined < SELF_BLOCKS_TO_MINE_LIMIT && this.isMiningAction()) {
                blocksMined++;
                this.raiseMiningAction();
                System.out.println(myWallet().getName() + " is mining the sign-in bonus block for " + keyNamePair.getWalletName() + " solely");

                ArrayList<Transaction> minedTx = new ArrayList<>();
                minedTx.add(transaction);
                MinerTheWorker worker = new MinerTheWorker(myWallet(), this, this.agent, minedTx);
                Thread miningThread = new Thread(worker);
                miningThread.start();
            } else {
                System.out.println(myWallet().getName() + " is broadcasting the sign-in bonus transaction for " + keyNamePair.getWalletName());
                MessageTransactionBroadcast mtb = new MessageTransactionBroadcast(transaction);
                this.agent.isSendMessage(mtb);
            }
        } else {
            waitingListForSignInBonus.add(0, keyNamePair);
        }
    }

    protected void receiveMessageBlockBroadcast(MessageBlockBroadcast mbb) {
        Block block = mbb.getMessageBody();
        boolean isVerifiedGuestBlock = myWallet().isVerifiedGuestBlock(block, myWallet().getLocalLedger());
        boolean isUpdatedLocalLedger = false;
        if (isVerifiedGuestBlock) {
            isUpdatedLocalLedger = this.myWallet().isUpdatedLocalLedger(block);
        }

        if (isVerifiedGuestBlock && isUpdatedLocalLedger) {
            System.out.println("New block added to local blockchain -> block size is now " + this.myWallet().getLocalLedger().getBlockchainSize());
            displayWallet_MinerBalance(myWallet());
        }
    }

    public static final void displayWallet_MinerBalance(Wallet miner) {
        ArrayList<UTXO> all = new ArrayList<>();
        ArrayList<UTXO> spent = new ArrayList<>();
        ArrayList<UTXO> unspent = new ArrayList<>();
        ArrayList<Transaction> transactions = new ArrayList<>();

        double balance = miner.getLocalLedger().findRelatedUTXOs(miner.getPublicKey(), all, spent, unspent, transactions);
        System.out.println("\t" + miner.getName() + ": balance=" + balance + ", local blockchain size=" + miner.getLocalLedger().getBlockchainSize());

        double income = 0;
        System.out.println("\tAll UTXOs:");
        for (UTXO each : all) {
            System.out.println("\t\t" + each.getAmountTransferred() + "|" + each.getHashID());
            income += each.getAmountTransferred();
        }
        System.out.println("\tTotal income -> " + income);

        System.out.println("\tSpent UTXOs:");
        income = 0;
        for (UTXO each : spent) {
            System.out.println("\t\t" + each.getAmountTransferred() + "|" + each.getHashID() + "|from=" + UtilityMethods.getKeyString(each.getSender()) + "|to=" + UtilityMethods.getKeyString(each.getReceiver()));
            income += each.getAmountTransferred();
        }
        System.out.println("\tTotal spending -> " + income);

        double txFee = transactions.size() * Transaction.TRANSACTION_FEE;
        System.out.println("\tUnspent UTXOs:");
        income = 0;
        for (UTXO each : unspent) {
            System.out.println("\t\t" + each.getAmountTransferred() + "|" + each.getHashID() + "|from=" + UtilityMethods.getKeyString(each.getSender()) + "|to=" + UtilityMethods.getKeyString(each.getReceiver()));
            income += each.getAmountTransferred();
        }
        System.out.println("\tTotal spent -> " + income);
    }
}
