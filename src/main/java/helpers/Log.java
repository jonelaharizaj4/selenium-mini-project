package helpers;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void step(String msg) {
        System.out.println("[" + LocalTime.now().format(F) + "][STEP] " + msg);
    }

    public static void info(String msg) {
        System.out.println("[" + LocalTime.now().format(F) + "][INFO] " + msg);
    }

    public static void warn(String msg) {
        System.out.println("[" + LocalTime.now().format(F) + "][WARN] " + msg);
    }

    public static void error(String msg) {
        System.out.println("[" + LocalTime.now().format(F) + "][ERROR] " + msg);
    }
}
