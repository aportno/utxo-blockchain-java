package chapter9;

public class LogManager {
    public static void log(int logLevel, String message) {
        if (logLevel >= Configuration.getLogBar()) {
            System.out.println(message);
        }
    }
}
