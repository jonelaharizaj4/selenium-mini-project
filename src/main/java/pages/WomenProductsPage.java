package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import helpers.Css;
import helpers.Log;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WomenProductsPage extends BasePage {

	private final By firstProductImageLink = By.cssSelector("ul.products-grid li.item:first-child a.product-image");
	private final By firstProductNameLink  = By.cssSelector("ul.products-grid li.item:first-child h2.product-name a");
	
    private final By productTiles = By.cssSelector("ul.products-grid li.item");
    private final By sortBySelect = By.cssSelector("select[title='Sort By']");

    private final By tilePriceBox = By.cssSelector(".price-box");
    private final By salePrice    = By.cssSelector(".special-price .price");
    private final By regularPrice = By.cssSelector(".regular-price .price");

    private final By wishlistLinkInTile = By.cssSelector(".link-wishlist");
    private final By anyMessage = By.cssSelector(
        ".page.messages, .messages, .message-success, .success-msg, .notice-msg"
    );

    public WomenProductsPage(WebDriver driver) {
        super(driver);
    }


	public boolean hoverVerifyStyle() {

        WebElement link = waitForVisibility(firstProductImageLink);
        WebElement name = waitForVisibility(firstProductNameLink);
        jsScrollIntoView(link);

        String expectedName = name.getText().trim();
        String borderBefore = link.getCssValue("border-color");

        new Actions(driver)
                .moveToElement(link)
                .pause(Duration.ofMillis(200))
                .perform();

        new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(d -> !link.getCssValue("border-color").equals(borderBefore));

        String borderAfter = link.getCssValue("border-color");
        String title = link.getAttribute("title");
        title = (title == null) ? "" : title.trim();

        int[] rgb = Css.rgb(borderAfter);

        boolean styleChanged = !borderBefore.equals(borderAfter);
        boolean colorIsBlue = Css.isBlueish(rgb);
        boolean tooltipCorrect = !title.isEmpty() && title.equalsIgnoreCase(expectedName);

        Log.info("Hover border-color: " + borderBefore + " -> " + borderAfter );
        Log.info("Hover tooltip title: '" + title + "'");

        return styleChanged && colorIsBlue && tooltipCorrect;
    }
    
    public WomenProductsPage sortBy(String visibleText) {
        WebElement firstTile = wait.until(ExpectedConditions.presenceOfElementLocated(productTiles));

        WebElement selectEl = waitForClickable(sortBySelect);
        new Select(selectEl).selectByVisibleText(visibleText);

        wait.until(ExpectedConditions.stalenessOf(firstTile));
        waitForGridReady();
        return this;
    }

    public List<Double> visiblePrices() {
        List<Double> prices = new ArrayList<>();
        for (WebElement tile : visibleTiles()) {
            Double p = readTilePrice(tile);
            if (p != null) prices.add(p);
        }
        return prices;
    }

    public void addFirstNToWishlist(int n) {
        for (int i = 0; i < n; i++) {
            List<WebElement> tiles = visibleTiles();
            if (tiles.size() <= i) {
                throw new AssertionError("Not enough products. Needed index " + i + " but only " + tiles.size());
            }

            WebElement tile = tiles.get(i);
            WebElement link = tile.findElement(wishlistLinkInTile);

            safeClick(link);

            wait.until(d ->
                d.getCurrentUrl().contains("/wishlist") ||
                d.findElements(anyMessage).stream().anyMatch(WebElement::isDisplayed)
            );

            if (driver.getCurrentUrl().contains("/wishlist") && i < n - 1) {
                driver.navigate().back();
                waitForGridReady();
            }
        }
    }

    public static boolean isSortedAsc(List<Double> xs) {
        for (int i = 1; i < xs.size(); i++) {
            if (xs.get(i) < xs.get(i - 1)) return false;
        }
        return true;
    }

    public static boolean isSortedDesc(List<Double> xs) {
        for (int i = 1; i < xs.size(); i++) {
            if (xs.get(i) > xs.get(i - 1)) return false;
        }
        return true;
    }

    private List<WebElement> visibleTiles() {
        return wait.until(d -> {
            List<WebElement> tiles = d.findElements(productTiles);
            return (!tiles.isEmpty() && tiles.get(0).isDisplayed()) ? tiles : null;
        });
    }

    private void waitForGridReady() {
        wait.until(d -> !d.findElements(productTiles).isEmpty());
        wait.until(d -> d.findElements(productTiles).get(0).isDisplayed());
    }

    private Double readTilePrice(WebElement tile) {
        try {
            WebElement box = tile.findElement(tilePriceBox);

            List<WebElement> sale = box.findElements(salePrice);
            if (!sale.isEmpty()) return parseMoney(sale.get(0).getText());

            List<WebElement> reg = box.findElements(regularPrice);
            if (!reg.isEmpty()) return parseMoney(reg.get(0).getText());

        } catch (Exception ignored) { }
        return null;
    }

    private double parseMoney(String raw) {
        String cleaned = raw.replaceAll("[^0-9.]", "");
        return Double.parseDouble(cleaned);
    }
}
