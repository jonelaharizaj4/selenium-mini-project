package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
public class RegisterPage extends BasePage {

    private final By firstName = By.id("firstname");
    private final By lastName = By.id("lastname");
    private final By email = By.id("email_address");
    private final By password = By.id("password");
    private final By confirmPassword = By.id("confirmation");
    private final By registerBtn = By.cssSelector("button[title='Register']");
    private final By successMsg = By.cssSelector(".success-msg span");
    private final By errorMsg = By.cssSelector(".error-msg span");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        waitForUrlToContain("/customer/account/create");
        waitForTitleToContain("Create New Customer Account");
        waitForVisibility(firstName);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public void fillAllFields(String fName, String lName, String mail, String pass) {
        type(firstName, fName);
        type(lastName, lName);
        type(email, mail);
        type(password, pass);
        type(confirmPassword, pass);
    }

    public void clickRegister() {
        safeClick(registerBtn, "Click Register");
        waitForRegistrationResult();
    }


    public void waitForRegistrationResult() {
        waitForDocumentReady();
        wait.until(d -> isDisplayed(successMsg) || isDisplayed(errorMsg) || !d.getCurrentUrl().contains("/customer/account/create"));
    }

    public String getErrorMessage() {
        return getText(errorMsg);
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMsg);
    }

    public String getSuccessMessage() {
        return getText(successMsg);
    }
}
