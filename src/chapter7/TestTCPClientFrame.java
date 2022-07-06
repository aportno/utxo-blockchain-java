package chapter7;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TestTCPClientFrame extends JFrame {
    private JTextArea textInput;
    private JButton sentButton;
    private JTextArea displayArea;
    private GridBagLayout gridBagLayout;
    private GridBagConstraints gridBagConstraints;
    private MessageManagerTCP_x messenger;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public TestTCPClientFrame(String userName, String ipAddress) throws Exception {
        super(userName);
        setUp();
    }

    protected void sendMsg(String msg) {
        try {
            this.objectOutputStream.writeObject(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUp() {
        this.setSize(500, 400);
        Container container = getContentPane();
        gridBagLayout = new GridBagLayout();
        gridBagConstraints = new GridBagConstraints();
        container.setLayout(gridBagLayout);
        JLabel jLabelInput = new JLabel("Message Board");

        this.displayArea = new JTextArea(50, 100);
        this.textInput = new JTextArea(5, 100);
        this.sentButton = new JButton("Click or hit enter to send message:");
        this.sentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    sendMsg(textInput.getText());
                } catch (Exception e1) {
                    System.out.println("Error: " + e1.getMessage());
                    throw new RuntimeException(e1);
                }
                textInput.setText("");
            }
        });

        this.gridBagConstraints.fill = GridBagConstraints.BOTH;
        this.gridBagConstraints.weightx = 1;
        this.gridBagConstraints.weighty = 0.0;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 0;
        this.gridBagConstraints.gridwidth = 1;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(jLabelInput, this.gridBagConstraints);
        container.add(jLabelInput);

        this.gridBagConstraints.weighty = 0.9;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 1;
        this.gridBagConstraints.gridheight = 9;

        JScrollPane scroll = new JScrollPane(this.displayArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.gridBagLayout.setConstraints(scroll, this.gridBagConstraints);
        container.add(scroll);
        this.displayArea.setEditable(false);
        this.displayArea.setBackground(Color.LIGHT_GRAY);

        this.gridBagConstraints.weighty = 0.0;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 11;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(this.sentButton, this.gridBagConstraints);
        container.add(this.sentButton);

        this.gridBagConstraints.weighty = 0.1;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 12;
        this.gridBagConstraints.gridheight = 2;

        JScrollPane scroll2 = new JScrollPane(this.textInput);
        scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.gridBagLayout.setConstraints(scroll2, this.gridBagConstraints);
        container.add(scroll2);

        this.textInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown() || e.isControlDown()) {
                        textInput.append(System.getProperty("line.separator"));
                    } else {
                        try {
                            sendMsg(textInput.getText());
                        } catch (Exception e2) {
                            System.out.println("Error: " + e2.getMessage());
                            throw new RuntimeException(e2);
                        }
                        e.consume();
                        textInput.setText("");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        this.setVisible(true);
    }

    public static void main(String[] args) {
        String userName = JOptionPane.showInputDialog("Please enter your unique name: ");
        String ipAddress = JOptionPane.showInputDialog("Please enter the server IP address: ");

        if (ipAddress.length() < 5) {
            ipAddress = "localhost";
        }
        TestTCPClientFrame clientFrame = null;
        try {
            clientFrame = new TestTCPClientFrame(userName, ipAddress);
        } catch (Exception e) {
            System.exit(2);
        }
        clientFrame.setVisible(true);
    }
}

class MessageManagerTCP_x implements Runnable {
    private final ObjectInputStream objectInputStream;
    private boolean isServerRunning = true;
    private final JTextArea pane;
    private int errorCount = 0;

    public MessageManagerTCP_x(ObjectInputStream objectInputStream, JTextArea pane) throws IOException {
        this.objectInputStream = objectInputStream;
        this.pane = pane;
    }

    public void close() { isServerRunning = false; }

    public void run() {
        System.out.println("Message manager is running...");
        while (isServerRunning) {
            try {
                SimpleTextMessage simpleTextMessage = (SimpleTextMessage)(this.objectInputStream.readObject());
                if (simpleTextMessage.getMsg().startsWith("END")) {
                    isServerRunning = false;
                } else {
                    pane.append(simpleTextMessage.getSenderName() + ": " + simpleTextMessage.getMsg() + "\n");
                    pane.setCaretPosition(pane.getText().length());
                }
            } catch (Exception e) {
                errorCount++;
                System.out.println("Error: this is only for text messaging");
                e.printStackTrace();
                if (errorCount >= 5) {
                    isServerRunning = false;
                }
            }
        }
        System.out.println("Message manager retired");
        System.exit(1);
    }
}