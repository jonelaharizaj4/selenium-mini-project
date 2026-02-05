package config;

public class Config {
    public static String baseUrl() {
        return System.getProperty("baseUrl", "https://ecommerce.tealiumdemo.com/");
    }

    public static String browser() {
        return System.getProperty("browser", "chrome");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    public static int timeoutSec() {
        return Integer.parseInt(System.getProperty("timeoutSec", "10"));
    }
}
