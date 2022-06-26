package chapter4;

public class TestEncryptionDecryptionWithAES {
    public static void main(String[] args) {
        String message = "At the most beautiful place, " + "remember you are the most beautiful.";
        String password = "blockchain";

        System.out.println("Message to test: " + message);
        System.out.println("Password to test: " + password + "\n");

        byte[] encrypted = UtilityMethods.encryptionByAES(message.getBytes(), password);

        System.out.println("View of encrypted data: ");
        System.out.println(new String(encrypted) + "\n");

        byte[] decrypted = UtilityMethods.decryptionByAES(encrypted, password);

        System.out.println("Attempt to decrypt message:");
        System.out.println(new String(decrypted) + "\n");

        try {
            System.out.println("Testing with an incorrect password:");

            decrypted = UtilityMethods.decryptionByAES(encrypted, "Block Chain");

            System.out.println(new String(decrypted));
        } catch (Exception e) {
            System.out.println("Runtime exception happened");
            System.out.println("Test with incorrect password successful");
        }
    }
}

