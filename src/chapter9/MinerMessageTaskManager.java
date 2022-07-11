package chapter9;

import java.security.PublicKey;
import java.util.ArrayList;

public class MinerMessageTaskManager extends WalletMessageTaskManager implements Runnable {
    private boolean isMiningAction = true;
    private final ArrayList<Transaction> existingTransactions = new ArrayList<>();
    private final Miner miner;
    private final PeerConnectionManager connectionManager;

    public MinerMessageTaskManager(Miner miner, PeerConnectionManager connectionManager) {
        super(miner, connectionManager);
        this.miner = miner;
        this.connectionManager = connectionManager;
    }

    protected synchronized  void resetMiningAction() {
        this.isMiningAction = true;
    }

    protected synchronized boolean isMiningAction() {
        return this.isMiningAction;
    }

    protected synchronized boolean getMiningAction() {
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

        if (!connectionManager.sendMessageByKey(receiver, mbp)) {
            connectionManager.sendMessageByAll(mbp);
        }
    }

    protected boolean receiveMessageBlockBroadcast(MessageBlockBroadcast mbb) {
        boolean isReceivedMsgBlockBroadcast = super.receiveMessageBlockBroadcast(mbb);
        if (isReceivedMsgBlockBroadcast) {
            Block block = mbb.getMessageBody();
            for (int i = 0; i < block.getTotalNumberOfTransactions(); i++) {
                Transaction tx = block.getTransaction(i);
                existingTransactions.removeIf(tx::equals);
            }
        }
        return isReceivedMsgBlockBroadcast;
    }

    protected void receiveMessageTransactionBroadcast(MessageTransactionBroadcast mtb) {
        connectionManager.sendMessageByAll(mtb);
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

        if (this.existingTransactions.size() >= Configuration.getBlockTransactionLowerLimit() && this.getMiningAction()) {
            this.raiseMiningAction();
            LogManager.log(Configuration.getLogBarMax(), miner.getName() + " gathered enough transactions to mine block");
            ArrayList<Transaction> txs = new ArrayList<>();
            for (int i = 0, j = 0; i < existingTransactions.size() && j < Configuration.getBlockTransactionUpperLimit(); i++, j++) {
                txs.add(existingTransactions.get(i));
                existingTransactions.remove(i);
                i--;
            }

            MinerTheWorker worker = new MinerTheWorker(miner, this, connectionManager, txs);
            Thread miningThread = new Thread(worker);
            miningThread.start();
        }
    }
}
