package chapter2;

public class TestHashing {
    public static void main(String[] args) {
        String msg = "If you are a drop of tears in my eye";
        String hash = UtilityMethods.messageDigestSHA256_toString(msg);
        System.out.println(hash);
    }
}
