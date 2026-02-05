package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    private final By emailField = By.id("email");
    private final By passwordField = By.id("pass");
    private final By loginButton = By.id("send2");

    private final By errorMsg = By.cssSelector(".error-msg span");
    private final By welcomeMsg = By.cssSelector(".welcome-msg");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        waitForUrlToContain("/customer/account/login");
        waitForVisibility(emailField);
    }

    public void login(String email, String password) {
        type(emailField, email);
        type(passwordField, password);
        safeClick(loginButton, "Click Login");
        waitForLoginResult();
    }

    public void waitForLoginResult() {
        waitForDocumentReady();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(welcomeMsg),
            ExpectedConditions.visibilityOfElementLocated(errorMsg)
        ));
    }


    public boolean isErrorDisplayed() {
        return isDisplayedNow(errorMsg);
    }
    public boolean isLoginSuccessful() {
        return isDisplayedNow(welcomeMsg) && !isDisplayedNow(errorMsg);
    }

    public String getErrorMessage() {
        return getText(errorMsg);
    }
    
    public boolean isDisplayedNow(By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            for (WebElement el : els) {
                if (el.isDisplayed()) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
