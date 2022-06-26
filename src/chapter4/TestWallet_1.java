package chapter4;

public class TestWallet_1 {
    public static void main(String[] args) {
        java.util.Scanner userInput = new java.util.Scanner(System.in);

        System.out.println("To create a wallet, please give your name: ");
        String userName = userInput.nextLine();

        System.out.println("Please create a password: ");
        String userPassword = userInput.nextLine();

        userInput.close();
        Wallet firstWallet = new Wallet(userName, userPassword);
        System.out.println("Wallet created for " + firstWallet.getName());

        Wallet secondWallet = new Wallet(userName, userPassword);
        System.out.println("Wallet loaded successfully, " + "wallet name = " + secondWallet.getName());
    }
}
