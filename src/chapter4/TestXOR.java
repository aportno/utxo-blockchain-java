package chapter4;

public class TestXOR {
    public static void main(String[] args) {
        String message = "At the most beautiful place, " + "remember the most beautiful you";
        String password = "blockchain";

        byte[] encryption = UtilityMethods.encryptionByXOR(message.getBytes(), password);

        System.out.println("Our message to encrypt is: " + message);
        System.out.println("The password is: " + password + "\n");

        System.out.println("The encrypted data is: ");
        System.out.println(new String(encryption) + "\n");

        byte[] decryption = UtilityMethods.decryptionByXOR(encryption, password);

        System.out.println("After proper decryption, the message is:");
        System.out.println(new String(decryption));
        System.out.println("\nUsing an incorrect password, " + "the decrypted message looks like:");

        byte[] decrypted = UtilityMethods.decryptionByXOR(encryption, "Block Chain");
        System.out.println(new String(decrypted));
    }
}
