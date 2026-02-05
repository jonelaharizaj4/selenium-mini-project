package driver;

import config.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {

    public static WebDriver createDriver() {
        String browser = Config.browser().toLowerCase();

        switch (browser) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOptions = new FirefoxOptions();
                if (Config.headless()) ffOptions.addArguments("-headless");
                return new FirefoxDriver(ffOptions);

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chOptions = new ChromeOptions();
                if (Config.headless()) chOptions.addArguments("--headless=new");
                chOptions.addArguments("--start-maximized");
                return new ChromeDriver(chOptions);
        }
    }
}
