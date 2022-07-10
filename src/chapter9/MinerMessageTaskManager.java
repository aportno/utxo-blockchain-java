package chapter9;

import chapter8.WalletConnectionAgent;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MinerMessageTaskManager extends WalletMessageTaskManager implements Runnable {
    private boolean isMiningAction = true;
    private ArrayList<Transaction> existingTransactions = new ArrayList<>();
    private WalletConnectionAgent agent;

    public MinerMessageTaskManager(WalletConnectionAgent agent, Miner miner, ConcurrentLinkedQueue<Message> messageConcurrentLinkedQueue) {
        super(agent, miner, messageConcurrentLinkedQueue);
        this.agent = agent;
    }

    protected synchronized  void resetMiningAction() {
        this.isMiningAction = true;
    }

    protected synchronized boolean isMiningAction() {
        return this.isMiningAction;
    }

    protected synchronized void raiseMiningAction() {
        this.isMiningAction = false;
    }

    protected Miner myWallet() {
        return (Miner) (super.myWallet());
    }

    protected void receiveQueryForBlockchainBroadcast(MessageAskForBlockchainBroadcast mabb) {
        PublicKey receiver = mabb.getSenderKey();
        Blockchain ledger = myWallet().getLocalLedger().copy_NotDeepCopy();
        MessageBlockchainPrivate mbp = new MessageBlockchainPrivate(ledger, myWallet().getPublicKey(), receiver);
        boolean isSendMessage = this.agent.isSendMessage(mbp);

        if (isSendMessage) {
            System.out.println(myWallet().getName() + ": local chain sent to requester -> chain size=" + mbp.getMessageBody().getBlockchainSize() +
                    "|" + mbp.getInfoSize());
        } else {
            System.out.println(myWallet().getName() + ": failed to send local blockchain to requester");
        }
    }

    protected void receiveMessageTransactionBroadcast(MessageTransactionBroadcast mtb) {
        Transaction transaction = mtb.getMessageBody();
        for (Transaction each : existingTransactions) {
            if (transaction.equals(each)) {
                return;
            }
        }

        if (!myWallet().isValidatedTransaction(transaction)) {
            System.out.println("Miner " + myWallet().getName() + " found an invalid transaction. Broadcasting to network");
            return;
        }
        this.existingTransactions.add(transaction);

        if (this.existingTransactions.size() >= Block.TRANSACTION_LOWER_LIMIT && this.isMiningAction()) {
            this.raiseMiningAction();
            System.out.println(myWallet().getName() + "transaction limit reached -> mining block");
            MinerTheWorker worker = new MinerTheWorker(myWallet(), this, this.agent, this.existingTransactions);

            Thread miningThread = new Thread(worker);
            miningThread.start();
            this.existingTransactions = new ArrayList<Transaction>();
        }
    }
}
