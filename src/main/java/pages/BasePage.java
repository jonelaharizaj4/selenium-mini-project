package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import helpers.Log;

import java.time.Duration;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected Actions actions;

    private static final int DEFAULT_TIMEOUT_SEC = 10;

    private final By consentPopup  = By.cssSelector(".privacy_prompt");
    private final By consentOptOut = By.cssSelector(".privacy_prompt #privacy_pref_optout");
    private final By consentSubmit = By.cssSelector(".privacy_prompt #consent_prompt_submit");
    private final By consentCloseX = By.cssSelector(".privacy_prompt .close_btn_thick");


    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SEC));
        this.js = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
    }

    public void waitForDocumentReady() {
        try {
            wait.until(d -> "complete".equals(
                    ((JavascriptExecutor) d).executeScript("return document.readyState")
            ));
        } catch (TimeoutException e) {
            System.out.println("[WAIT] Document readyState timeout -> continuing.");
        }
    }

    // Wait helpers
    public WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void waitForTitleToContain(String partialTitle) {
        wait.until(ExpectedConditions.titleContains(partialTitle));
    }

    public void waitForUrlToContain(String partialUrl) {
        wait.until(ExpectedConditions.urlContains(partialUrl));
    }

    public void click(By locator) {
        clickCore(locator, null, false);
    }

    protected void type(By locator, String text) {
        WebElement el = waitForVisibility(locator);
        scrollIntoViewCentered(el);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(text);
    }

    public String getText(By locator) {
        return waitForVisibility(locator).getText().trim();
    }

    public boolean isDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void jsClick(By locator) {
        WebElement element = waitForPresence(locator);
        js.executeScript("arguments[0].click();", element);
    }

    public void jsClick(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        js.executeScript("arguments[0].click();", element);
    }

    public void jsScrollIntoView(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }


    private void clickCore(By locator, String logName, boolean dismissPopup) {
        if (dismissPopup) handleConsentPopup();

        if (logName != null) {
            System.out.println("[CLICK] " + logName);
        }

        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoViewCentered(el);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(el));
            el.click();
        } catch (StaleElementReferenceException stale) {
            WebElement fresh = wait.until(ExpectedConditions.elementToBeClickable(locator));
            try {
                fresh.click();
            } catch (Exception e2) {
                js.executeScript("arguments[0].click();", fresh);
            }
        } catch (ElementClickInterceptedException | TimeoutException e) {
            js.executeScript("arguments[0].click();", el);
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", el);
        }
    }

    protected void scrollIntoViewCentered(WebElement el) {
        js.executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", el
        );
    }

    protected void safeClick(WebElement element) {
        try {
            scrollIntoViewCentered(element);
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    protected void safeClick(By locator, String nameForLogs) {
        clickCore(locator, nameForLogs, true);
    }

     protected void handleConsentPopup() {
        try {
            if (driver.findElements(consentPopup).isEmpty()) return;

            if (!driver.findElements(consentOptOut).isEmpty()) {
                driver.findElements(consentOptOut).get(0).click();
            }

            if (!driver.findElements(consentSubmit).isEmpty()) {
                driver.findElements(consentSubmit).get(0).click();
            } else if (!driver.findElements(consentCloseX).isEmpty()) {
                driver.findElements(consentCloseX).get(0).click();
            }

        } catch (Exception ignored) {
        	Log.error(ignored.getMessage());
        }
    }



}
