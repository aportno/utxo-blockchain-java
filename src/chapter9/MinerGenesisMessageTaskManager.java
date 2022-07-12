package chapter9;

import java.util.ArrayList;
import java.util.HashMap;

public class MinerGenesisMessageTaskManager extends MinerMessageTaskManager implements Runnable {
    private int blocksMined = 0;
    private int idleItem = 0;
    private final HashMap<String, KeyNamePair> users = new HashMap<>();
    private final ArrayList<KeyNamePair> waitingListForSignInBonus = new ArrayList<>();
    private final ArrayList<Transaction> waitingTransactionForSignInBonus = new ArrayList<>();
    private final PeerConnectionManager connectionManager;
    private final Miner miner;

    public MinerGenesisMessageTaskManager(Miner miner, PeerConnectionManager connectionManager) {
        super(miner, connectionManager);
        this.connectionManager = connectionManager;
        this.miner = miner;
    }

    /*
    The genesis miner will poll the service relay provider for new users time to time
     */

    public void toDo() {
        try {
            Thread.sleep(Configuration.getThreadSleepTimeMedium());
            if (waitingListForSignInBonus.size() == 0 && users.size() < Configuration.getSignInBonusUsersLimit()) {
                idleItem++;
                if (idleItem >= 100) {
                    idleItem = 0;
                    MessageAddressBroadcastAsk maba = new MessageAddressBroadcastAsk(miner.getPublicKey(), miner.getName());
                    connectionManager.sendMessageByAll(maba);
                }
            } else if (users.size() < Configuration.getSignInBonusUsersLimit() && waitingListForSignInBonus.size() > 0) {
                sendSignInBonus();
            }
        } catch (Exception e) {}
    }

    private void sendSignInBonus() {
        if (waitingTransactionForSignInBonus.size() <= 0 && waitingListForSignInBonus.size() <= 0) {
            return;
        }

        Transaction tx = null;
        String recipient = null;
        if (waitingTransactionForSignInBonus.size() > 0) {
            tx = waitingTransactionForSignInBonus.get(0);
            recipient = connectionManager.getNameFromAddress(tx.getOutputUTXO(0).getReceiver());
            LogManager.log(Configuration.getLogBarMax(), "Re-mine a block for the bonus transaction to " + recipient);
        } else if (waitingListForSignInBonus.size() > 0){
            KeyNamePair publicKey = waitingListForSignInBonus.remove(0);
            recipient = publicKey.walletName();
            int signInBonus = 1000;
            tx = miner.transferFund(publicKey.publicKey(), signInBonus);
            if (tx != null && tx.verifySignature()) {
                LogManager.log(Configuration.getLogBarMax(), miner.getName() + " is sending " + recipient  + " sign-in bonus of " + signInBonus);
            } else {
                waitingListForSignInBonus.add(0, publicKey);
            }
        }

        if (blocksMined < Configuration.getSelfBlocksToMineLimit() && getMiningAction()) {
            blocksMined++;
            raiseMiningAction();
            LogManager.log(Configuration.getLogBarMin(), miner.getName() + " is mining the sign-in bonus block for " + recipient + " by himself");
            ArrayList<Transaction> txs = new ArrayList<>();
            txs.add(tx);

            MinerTheWorker worker = new MinerTheWorker(miner, this, connectionManager, txs);
            Thread miningThread = new Thread(worker);
            miningThread.start();
        } else {
            LogManager.log(Configuration.getLogBarMin(), miner.getName() + " is broadcasting the transaction of sign-in bonus for " + recipient);
            MessageTransactionBroadcast mtb = new MessageTransactionBroadcast(tx);
            connectionManager.sendMessageByAll(mtb);
        }
    }

    protected boolean receiveMessageBlockBroadcast(MessageBlockBroadcast mbb) {
        /*
        This describes how a genesis Miner acts when receiving a block
         */

        connectionManager.sendMessageByAll(mbb);
        Block block = mbb.getMessageBody();
        boolean isVerifiedGuestBlock = miner.isVerifiedGuestBlock(block, miner.getLocalLedger());
        boolean isUpdatedLocalLedger = false;
        if (isVerifiedGuestBlock) {
            isUpdatedLocalLedger = miner.isUpdatedLocalLedger(block);
        }

        if (isVerifiedGuestBlock && isUpdatedLocalLedger) {
            LogManager.log(Configuration.getLogBarMin(), "New block is added to the local blockchain -> blockchain size=" + miner.getLocalLedger().getBlockchainSize());
        } else {
            LogManager.log(Configuration.getLogBarMin(), "New block is rejected");
            if (block.getCreator().equals(miner.getPublicKey())) {
                LogManager.log(Configuration.getLogBarMin(), "Genesis miner's block is rejected");
                String id = UtilityMethods.getKeyString(block.getTransaction(0).getOutputUTXO(0).getReceiver());
                KeyNamePair publicKey = users.get(id);
                Transaction tx = block.getTransaction(0);
                if (publicKey != null && !(miner.getLocalLedger().isTransactionExist(tx))) {
                    waitingTransactionForSignInBonus.add(0, tx);
                } else if (publicKey == null) {
                    System.out.println("ERROR: an existing user for sign-in bonus is not found");
                }
            }
        }
        return isVerifiedGuestBlock && isUpdatedLocalLedger;
    }

    protected void receiveMessageAddressPrivate(MessageAddressPrivate map) {
        ArrayList<KeyNamePair> all = map.getMessageBody();
        for (KeyNamePair each : all) {
            connectionManager.addAddress(each);
            String id = UtilityMethods.getKeyString(each.publicKey());
            if(!each.publicKey().equals(miner.getPublicKey()) && !users.containsKey(id)) {
                users.put(id, each);
                if (users.size() <= Configuration.getSignInBonusUsersLimit()) {
                    this.waitingListForSignInBonus.add(each);
                }
            }
        }

        if (!connectionManager.sendMessageByKey(map.getReceiverKey(), map)) {
            connectionManager.sendMessageByAll(map);
        }
    }

    protected void receiveMessageBroadcastMakingFriend(MessageBroadcastMakingFriend mbmf) {
        connectionManager.sendMessageByAll(mbmf);
        connectionManager.createOutgoingConnection(mbmf.getIpAddress());
        connectionManager.addAddress(mbmf.getKeyNamePair());
        String id = UtilityMethods.getKeyString(mbmf.getSenderKey());
        if (!mbmf.getSenderKey().equals(miner.getPublicKey()) && !users.containsKey(id)) {
            users.put(id, mbmf.getKeyNamePair());
            if (users.size() <= Configuration.getSignInBonusUsersLimit()) {
                waitingListForSignInBonus.add(mbmf.getKeyNamePair());
            }
        }
    }

    protected void receiveMessageTransactionBroadcast(MessageTransactionBroadcast mtb) {
        connectionManager.sendMessageByAll(mtb);
    }
}
