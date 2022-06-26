package chapter4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {
    private KeyPair keyPair;
    private String walletName;
    private static String keyLocation = "keys";

    public Wallet(String walletName, String password) {
        this.keyPair = UtilityMethods.generateKeyPair();
        this.walletName = walletName;

        try {
            populateExistingWallet(walletName, password);
            System.out.println("A wallet exists with the same name and password. Loaded the existing wallet");
        } catch (Exception e) {
            try {
                this.prepareWallet(password);
                System.out.println("Created a new wallet based on the name and password");
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public Wallet(String walletName) {
        this.keyPair = UtilityMethods.generateKeyPair();
        this.walletName = walletName;
    }


    public String getName() {
        return this.walletName;
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    private void prepareWallet(String password) throws IOException, FileNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(this.keyPair);

        byte[] keyBytes = UtilityMethods.encryptionByXOR(byteArrayOutputStream.toByteArray(), password);
        File file = new File(Wallet.keyLocation);

        if (!file.exists()) {
            file.mkdir();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(Wallet.keyLocation
                + "/"
                + this.getName().replace(' ', '_')
                + "_keys");

        fileOutputStream.write(keyBytes);
        fileOutputStream.close();
        byteArrayOutputStream.close();
    }

    private void populateExistingWallet(String walletName, String password) throws IOException, FileNotFoundException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(Wallet.keyLocation
                + "/"
                + walletName.replace(' ', '_') + "_keys");

        byte[] bb = new byte[4096];
        int size = fileInputStream.read(bb);

        fileInputStream.close();

        byte[] data = new byte[size];

        for (int i = 0; i < data.length; i++) {
            data[i] = bb[i];
        }

        byte[] keyBytes = UtilityMethods.decryptionByXOR(data, password);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyBytes));

        this.keyPair = (KeyPair)(objectInputStream.readObject());
        this.walletName = walletName;
    }
}
