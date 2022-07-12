package chapter9;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Enumeration;

public class WalletMessageTaskManager implements Runnable {
    private boolean isServerRunning = true;
    private final Wallet wallet;
    private final Hashtable<String, Message> existingMessages = new Hashtable<>();
    private ConcurrentLinkedQueue<Message> messageConcurrentLinkedQueue;
    private final PeerConnectionManager connectionManager;
    private WalletSimulator simulator;
    private int idleTime = 0;

    public WalletMessageTaskManager(Wallet wallet, PeerConnectionManager connectionManager) {
        this.wallet = wallet;
        this.connectionManager = connectionManager;
    }

    protected Wallet myWallet() {
        return this.wallet;
    }

    public void setSimulator(WalletSimulator simulator) {
        this.simulator = simulator;
    }

    public void addMessageIntoQueue(Message msg) {
        if (msg.getSenderKey().equals(wallet.getPublicKey()) && !msg.isSelfMessageAllowed()) {
            return;
        }

        if (UtilityMethods.isMessageTooOld(msg.getTimeStamp())) {
            return;
        }

        Message checkMsg = existingMessages.get(msg.getMessageHashID());
        if (checkMsg != null) {
            return;
        }

        existingMessages.put(msg.getMessageHashID(), msg);
        messageConcurrentLinkedQueue.add(msg);
    }

    private synchronized void discardObsoleteMessages() {
        Enumeration<Message> E = existingMessages.elements();
        while (E.hasMoreElements()) {
            Message msg = E.nextElement();
            if (UtilityMethods.isMessageTooOld(msg.getTimeStamp())) {
                existingMessages.remove(msg.getMessageHashID());
            }
        }
    }

    protected void processMessage(Message message) {
        if (message == null) {
            return;
        } else if (!message.isForBroadcast()) {
            if (message.getMessageType() == Message.TEXT_PRIVATE) {
                MessageTextPrivate messageTextPrivate = (MessageTextPrivate) message;
                if (!messageTextPrivate.isValid()) {
                    LogManager.log(Configuration.getLogBarMax(), "In WalletMessageTaskManager.processMessage() -> message tampered");
                    return;
                }
                receivePrivateChatMessage(messageTextPrivate);
            } else if (message.getMessageType() == Message.ADDRESS_PRIVATE) {
                MessageAddressPrivate map = (MessageAddressPrivate) message;
                receiveMessageAddressPrivate(map);
            } else if (message.getMessageType() == Message.BLOCKCHAIN_PRIVATE) {
                MessageBlockchainPrivate mbp = (MessageBlockchainPrivate) message;
                receiveMessageBlockchainPrivate(mbp);
            } else {
                LogManager.log(Configuration.getLogBarMax(), "In WalletMessageTaskManager.processMessage() -> message not supported");
            }
        } else if (message.getMessageType() == Message.BLOCK_BROADCAST) {
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.processMessage() -> check if update required");
            MessageBlockBroadcast mbb = (MessageBlockBroadcast) message;
            this.receiveMessageBlockBroadcast(mbb);
        } else if (message.getMessageType() == Message.BLOCKCHAIN_BROADCAST) {
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.processMessage() -> check if update require");
            MessageBlockchainBroadcast mbb = (MessageBlockchainBroadcast) message;
            boolean isReceivedBlockchainBroadcast = receiveBlockchainBroadcast(mbb);
            if (isReceivedBlockchainBroadcast) {
                LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.processMessage() -> blockchain is updated");
            } else {
                LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.processMessage() -> blockchain is rejected");
            }
        } else if (message.getMessageType() == Message.TRANSACTION_BROADCAST) {
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.processMessage() -> transaction broadcast message");
            MessageTransactionBroadcast mtb = (MessageTransactionBroadcast) message;
            this.receiveMessageTransactionBroadcast(mtb);
        } else if (message.getMessageType() == Message.BLOCKCHAIN_ASK_BROADCAST) {
            MessageAskForBlockchainBroadcast mabb = (MessageAskForBlockchainBroadcast) message;
            if (mabb.isValid()) {
                receiveMessageForBlockchainBroadcast(mabb);
            }
        } else if (message.getMessageType() == Message.TEXT_BROADCAST) {
            MessageTextBroadcast mtb = (MessageTextBroadcast) message;
            receiveMessageTextBroadcast(mtb);
        } else if (message.getMessageType() == Message.ADDRESS_BROADCAST_MAKING_FRIEND) {
            receiveMessageBroadcastMakingFriend((MessageBroadcastMakingFriend) message);
        } else if (message.getMessageType() == Message.ADDRESS_ASK_BROADCAST) {
            receiveMessageAddressBroadcastAsk((MessageAddressBroadcastAsk) message);
        }
    }

    protected void receiveMessageTextBroadcast(MessageTextBroadcast mtb) {
        connectionManager.sendMessageByAll(mtb);
        String text = mtb.getMessageBody();
        String name = mtb.getSenderName();
        simulator.appendMessageLineOnBoard(name + ":" + text);
        connectionManager.addAddress(new KeyNamePair(mtb.getSenderKey(), mtb.getSenderName()));
    }

    protected void receiveMessageAddressBroadcastAsk(MessageAddressBroadcastAsk msg) {
        connectionManager.sendMessageByAll(msg);
        KeyNamePair self = new KeyNamePair(wallet.getPublicKey(), wallet.getName());
        ArrayList<KeyNamePair> keyNamePairs = connectionManager.getAllStoredAddresses();
        keyNamePairs.add(self);
        MessageAddressPrivate map = new MessageAddressPrivate(keyNamePairs, wallet.getPublicKey(), msg.getSenderKey());
        if (!connectionManager.sendMessageByKey(map.getSenderKey(), map)) {
            connectionManager.sendMessageByAll(map);
        }
    }

    protected void receiveMessageBroadcastMakingFriend(MessageBroadcastMakingFriend msg) {
        connectionManager.sendMessageByAll(msg);
        connectionManager.createOutgoingConnection(msg.getIpAddress());
    }

    protected void receiveMessageAddressPrivate(MessageAddressPrivate map) {
        if (map.getReceiverKey().equals(wallet.getPublicKey())) {
            ArrayList<KeyNamePair> all = map.getMessageBody();
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.receiveMessageAddressPrivate() -> listing available addresses");
            for (KeyNamePair each: all) {
                if (!each.getPublicKey().equals(wallet.getPublicKey())) {
                    connectionManager.addAddress(each);
                    LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.receiveMessageAddressPrivate() | " + each.getWalletName() + "|" + UtilityMethods.getKeyString(each.getPublicKey()));
                }
            }
        }
    }

    protected void receivePrivateChatMessage(MessageTextPrivate mtp) {
        if (!(mtp.getReceiver().equals(wallet.getPublicKey()))) {
            boolean isSendMessage = connectionManager.sendMessageByKey(mtp.getReceiver(), mtp);
            if (!isSendMessage) {
                connectionManager.sendMessageByAll(mtp);
            }
        } else {
            String text = mtp.getMessageBody();
            String name = mtp.getSenderName();
            simulator.appendMessageLineOnBoard("private <- " + name + " : " + text);
            connectionManager.addAddress(new KeyNamePair(mtp.getSenderKey(), mtp.getSenderName()));
        }
    }

    protected boolean receiveBlockchainBroadcast(MessageBlockchainBroadcast mbb) {
        connectionManager.sendMessageByAll(mbb);
        return wallet.setLocalLedger(mbb.getMessageBody());
    }

    protected void receiveMessageTransactionBroadcast(MessageTransactionBroadcast mtb) {
        connectionManager.sendMessageByAll(mtb);
        Transaction tx = mtb.getMessageBody();
        if (tx.getSender().equals(wallet.getPublicKey())) {
            return;
        }
        int outputUTXOs = tx.getNumberOfOutputUTXOs();
        int totalOutputUTXOs = 0;
        for (int i = 0; i < outputUTXOs; i++) {
            UTXO utxo = tx.getOutputUTXO(i);
            if (utxo.getReceiver().equals(wallet.getPublicKey())) {
                totalOutputUTXOs += utxo.getAmountTransferred();
            }
        }

        if (totalOutputUTXOs > 0) {
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManger.receiveMessageTransactionBroadcast -> payment received");
            MessageTextPrivate mtp = new MessageTextPrivate("Received amount: " + totalOutputUTXOs, wallet.getPrivateKey(), wallet.getPublicKey(), wallet.getName(), tx.getSender());
            connectionManager.sendMessageByKey(mtb.getSenderKey(), mtp);
        }
    }

    protected boolean receiveMessageBlockBroadcast(MessageBlockBroadcast mbb) {
        connectionManager.sendMessageByAll(mbb);
        Block block = mbb.getMessageBody();
        boolean isUpdatedLocalLedger = wallet.isUpdatedLocalLedger(block);
        if (isUpdatedLocalLedger) {
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.receiveMessageBlockBroadcast() -> new block added to local chain");
        } else {
            int size = block.getTotalNumberOfTransactions();
            int counter = 0;
            for (int i = 0; i < size; i++) {
                Transaction tx = block.getTransaction(i);
                if (!wallet.getLocalLedger().isTransactionExist(tx)) {
                    MessageTransactionBroadcast mtb = new MessageTransactionBroadcast(tx);
                    connectionManager.sendMessageByAll(mtb);
                    counter++;
                }
            }
            LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManager.receiveMessageBlockBroadcast() -> block rejected : " + counter + "unpublished transactions released into pool");
        }
        return isUpdatedLocalLedger;
    }

    protected void receiveMessageBlockchainPrivate(MessageBlockchainPrivate mbp) {
        System.out.println("Private message to the chain...");
        if (mbp.getReceiver().equals(myWallet().getPublicKey())) {
            boolean isSetLocalLedger = this.myWallet().setLocalLedger(mbp.getMessageBody());
            if (isSetLocalLedger) {
                System.out.println("Blockchain is updated...");
            } else {
                System.out.println("Rejected the new blockchain...");
            }
        } else {
            System.out.println("ERROR: private message sent to the wrong wallet");
        }
    }

    protected void receiveMessageForBlockchainBroadcast(MessageAskForBlockchainBroadcast mabb) {
        LogManager.log(Configuration.getLogBarMin(), "In WalletMessageTaskManger.receiveQueryForBlockchainBroadcast() -> wallet can only forward the request to blockchain");
        connectionManager.sendMessageByAll(mabb);
    }

    public void close() {
        isServerRunning = false;
    }

    public void toDo() {};

    public void run() {
        while (isServerRunning) {
            try {
                Thread.sleep(Configuration.getThreadSleepTimeMedium());
                idleTime++;
            } catch (InterruptedException ie) {
                LogManager.log(Configuration.getLogBarMin(), "Exception in WalletMessageTaskManager.run() " + ie.getMessage());
            }

            if (idleTime >= 400000) {
                idleTime = 0;
                discardObsoleteMessages();
            }

            if (messageConcurrentLinkedQueue.isEmpty()) {
                toDo();
                try {
                    Thread.sleep(Configuration.getThreadSleepTimeLong());
                } catch (InterruptedException ie) {
                    LogManager.log(Configuration.getLogBarMin(), "Exception in WalletMessageTaskManager.run() " + ie.getMessage());
                }
            } else {
                while (!(messageConcurrentLinkedQueue.isEmpty())) {
                    Message msg = messageConcurrentLinkedQueue.poll();
                    processMessage(msg);
                }
            }
        }
    }
}
