package pages;

import config.Config;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomePage extends BasePage {

    private final By accountMenu = By.cssSelector(".account-cart-wrapper a");
    private final By registerLink =By.linkText("Register");
    private final By loginLink = By.linkText("Log In");
    private final By logoutLink = By.linkText("Log Out");
    private final By wishListLink = By.partialLinkText("My Wishlist");
    private final By welcomeMessage = By.cssSelector(".welcome-msg");
    private final By homeLogo = By.cssSelector("a.logo"); // stable element on homepage

    private final By womenMenu = By.cssSelector("#nav .nav-primary > li.nav-1 > a.level0");
    private final By menMenu   = By.cssSelector("#nav .nav-primary > li.nav-2 > a.level0");
    private final By saleMenu  = By.cssSelector("#nav .nav-primary > li.nav-5 > a.level0");

    private final By womenViewAll = By.cssSelector("#nav .nav-primary > li.nav-1 li.view-all > a");
    private final By menViewAll   = By.cssSelector("#nav .nav-primary > li.nav-2 li.view-all > a");
    private final By saleViewAll  = By.cssSelector("#nav .nav-primary > li.nav-5 li.view-all > a");


    private final Actions actions;

    public HomePage(WebDriver driver) {
        super(driver);
        this.actions = new Actions(driver);
    }

    public void openAccountMenu() {
        safeClick(accountMenu, "Account menu");
    }

    public RegisterPage goToRegister() {
        openAccountMenu();
        click(registerLink);
        return new RegisterPage(driver);
    }

    public LoginPage goToLogin() {
        openAccountMenu();
        click(loginLink);
        return new LoginPage(driver);
    }

    public WishListPage goToWishlist() {
        openAccountMenu();
        click(wishListLink);
        return new WishListPage(driver);
    }

    public void logout() {
        openAccountMenu();
        click(logoutLink);

        wait.until(d ->
            d.getCurrentUrl().contains("logout") ||
            d.findElements(loginLink).size() > 0 ||
            d.findElements(registerLink).size() > 0
        );
    }

	public void waitForLoginToComplete() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(accountMenu));
    }

 
    public void goToBasePage() {
        driver.get(Config.baseUrl());
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeLogo));
    }

    public void reloadPage() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeLogo));
    }

    public boolean isLoggedOut() {
        openAccountMenu();
        boolean loginVisible = isDisplayed(loginLink);
        boolean registerVisible = isDisplayed(registerLink);
        return loginVisible && registerVisible;
    }

    public String getWelcomeText() {
        return getText(welcomeMessage);
    }

    private void hoverAndClick(By hoverTarget, By clickTarget, String name) {
    	handleConsentPopup();

        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(hoverTarget));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", menu);

        actions.moveToElement(menu).perform();

        WebElement viewAll = wait.until(ExpectedConditions.elementToBeClickable(clickTarget));
        try {
            viewAll.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewAll);
        }

        waitForDocumentReady();
    }

    public WomenProductsPage goToAllWomen() {
        hoverAndClick(womenMenu, womenViewAll, "Women");
        return new WomenProductsPage(driver);
    }

    public MenPage goToAllMen() {
        hoverAndClick(menMenu, menViewAll, "Men");
        return new MenPage(driver);
    }

    public SalePage goToAllSale() {
        hoverAndClick(saleMenu, saleViewAll, "Sale");
        return new SalePage(driver);
    }

    public int getWishlistCountFromAccMenu() {
        openAccountMenu();

        WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.partialLinkText("My Wishlist")
        ));

        String text = link.getText(); 
        Matcher m = Pattern.compile("\\((\\d+)").matcher(text);

        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0; 
    }
    
    
}
