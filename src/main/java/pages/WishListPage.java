package pages;

import config.Config;
import helpers.Log;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class WishListPage extends BasePage {

    private static final String WISHLIST_PATH = "wishlist/";
    private static final String WISHLIST_URL_PART = "/wishlist";
    private static final String CART_URL_PART = "/checkout/cart";
    private static final String CONFIGURE_URL_PART = "/wishlist/index/configure";
    private static final String PRODUCT_URL_PART_1 = "/catalog/product";
    private static final String PRODUCT_URL_PART_2 = "/product";

    private static final int LOAD_SEC = 20;
    private static final int CART_OUTCOME_SEC = 25;
    private final By productOptionsWrapper = By.id("product-options-wrapper");

    private final By wishlistRows = By.cssSelector("#wishlist-table tbody tr[id^='item_']");
    private final By emptyWishlist = By.cssSelector(".wishlist-empty");
    private final By addToCartInRow = By.cssSelector("button.btn-cart[title='Add to Cart'], button.btn-cart");
    private final By removeInRow = By.cssSelector("a.btn-remove[title='Remove Item'], a.btn-remove");

    private final By colorLinks  = By.cssSelector("#configurable_swatch_color li a.swatch-link");
    private final By sizeLinks   = By.cssSelector("#configurable_swatch_size  li a.swatch-link");
    private final By selectedColorLabel = By.id("select_label_color");
    private final By selectedSizeLabel  = By.id("select_label_size");
    private final By superAttributeSelects = By.cssSelector("select.super-attribute-select");

    private final By addToCartButton = By.cssSelector("button.btn-cart[title='Add to Cart'], button.btn-cart");

    private final By anyMessage = By.cssSelector(".success-msg, .error-msg, .notice-msg, .messages, .page.messages");
    private final By successMsg = By.cssSelector(".message-success, .messages .success, .success-msg");

    public WishListPage(WebDriver driver) {
        super(driver);
    }

    public void openWishlist() {
        if (!driver.getCurrentUrl().contains(WISHLIST_URL_PART)) {
            driver.get(Config.baseUrl() + WISHLIST_PATH);
            wait.until(ExpectedConditions.urlContains(WISHLIST_URL_PART));
        }
        waitUntilLoaded();
    }

    public int getNumberOfProducts() {
        openWishlist();
        return driver.findElements(wishlistRows).size();
    }

    public boolean isEmpty() {
        openWishlist();
        return driver.findElements(wishlistRows).isEmpty();
    }

    public void clearAllItemsIfAny() {
        openWishlist();
        int safety = 0;
        while (safety++ < 30 && removeFirstWishlistItem()) {
        }

        if (safety >= 30 && !driver.findElements(wishlistRows).isEmpty()) {
            throw new RuntimeException("Wishlist clear exceeded safety limit. Remove action may be failing.");
        }

        Log.info("Wishlist cleared");
    }

    public void addFirstNWishlistItemsToCart(int n) {
        Log.step("Wishlist: add first " + n + " item(s) to cart");
        openWishlist();

        for (int i = 0; i < n; i++) {
            addFirstItemToCart();
            if (i < n - 1) openWishlist();
        }
    }

    private boolean removeFirstWishlistItem() {
        List<WebElement> rows = driver.findElements(wishlistRows);
        if (rows.isEmpty()) return false;

        int before = rows.size();

        WebElement removeBtn = rows.get(0).findElement(removeInRow);
        scrollIntoViewCentered(removeBtn);
        safeClick(removeBtn);

        acceptAlertIfPresent();
        waitUntilRowCountDecreases(before);
        waitUntilLoaded();
        return true;
    }

    private void addFirstItemToCart() {
        assertNotEmpty();

        WebElement firstRow = waitSec(LOAD_SEC).until(d -> {
            List<WebElement> rows = d.findElements(wishlistRows);
            return rows.isEmpty() ? null : rows.get(0);
        });

        WebElement addBtn = firstRow.findElement(addToCartInRow);
        scrollIntoViewCentered(addBtn);
        jsClick(addBtn);

        waitForAddToCartOutcome();

        if (isOnCartPage()) return;

        if (isOnConfigurableProductFlow()) {
            configureIfNeeded();
            clickAddToCartOnProductPage();
            waitForCartOrMessage();
        }
    }

    private void waitUntilLoaded() {
        waitSec(LOAD_SEC).until(d ->
                !d.findElements(wishlistRows).isEmpty() ||
                !d.findElements(emptyWishlist).isEmpty()
        );
    }

    private void assertNotEmpty() {
        boolean rowsPresent = !driver.findElements(wishlistRows).isEmpty();
        boolean emptyMsg = !driver.findElements(emptyWishlist).isEmpty();
        if (emptyMsg && !rowsPresent) {
            throw new RuntimeException("Wishlist is empty - cannot add to cart.");
        }
    }

    private void waitForAddToCartOutcome() {
        waitSec(LOAD_SEC).until(ExpectedConditions.or(
                ExpectedConditions.urlContains(CART_URL_PART),
                ExpectedConditions.presenceOfElementLocated(productOptionsWrapper),
                ExpectedConditions.presenceOfElementLocated(anyMessage)
        ));
    }


    private boolean isOnCartPage() {
        return driver.getCurrentUrl().contains(CART_URL_PART);
    }

    private boolean isOnConfigurableProductFlow() {
        String url = driver.getCurrentUrl();
        return url.contains(CONFIGURE_URL_PART) ||
               url.contains(PRODUCT_URL_PART_1) ||
               url.contains(PRODUCT_URL_PART_2);
    }

    private void waitForCartOrMessage() {
        waitSec(CART_OUTCOME_SEC).until(ExpectedConditions.or(
                ExpectedConditions.urlContains(CART_URL_PART),
                ExpectedConditions.visibilityOfElementLocated(successMsg),
                ExpectedConditions.visibilityOfElementLocated(anyMessage)
        ));
    }

    private void waitUntilRowCountDecreases(int before) {
        waitSec(LOAD_SEC).until(d -> {
            int now = d.findElements(wishlistRows).size();
            boolean emptyMsg = !d.findElements(emptyWishlist).isEmpty();
            return now < before || now == 0 || emptyMsg;
        });
    }

    private void configureIfNeeded() {
        if (trySelectColorAndSize()) return;
        selectFirstNonEmptyOptionInVisibleSelects(superAttributeSelects);
    }

    private boolean trySelectColorAndSize() {
        if (driver.findElements(colorLinks).isEmpty() || driver.findElements(sizeLinks).isEmpty()) {
            return false;
        }

        WebDriverWait w = waitSec(LOAD_SEC);

        List<WebElement> colors = w.until(ExpectedConditions.presenceOfAllElementsLocatedBy(colorLinks));

        for (int ci = 0; ci < colors.size(); ci++) {
            colors = driver.findElements(colorLinks);
            if (colors.isEmpty()) break;

            WebElement color = colors.get(ci);
            if (!isSwatchEnabled(color)) continue;

            clickSwatch(color);
            waitUntilLabelHasText(selectedColorLabel);

            List<WebElement> sizes = w.until(ExpectedConditions.presenceOfAllElementsLocatedBy(sizeLinks));

            for (int si = 0; si < sizes.size(); si++) {
                sizes = driver.findElements(sizeLinks);
                if (sizes.isEmpty()) break;

                WebElement size = sizes.get(si);
                if (!isSwatchEnabled(size)) continue;

                clickSwatch(size);
                waitUntilLabelHasText(selectedSizeLabel);

                if (isAnyAddToCartButtonVisibleAndEnabled()) return true;
            }
        }

        throw new RuntimeException("No valid Color+Size combination was found.");
    }

    private boolean isSwatchEnabled(WebElement swatchLink) {
        try {
            WebElement li = swatchLink.findElement(By.xpath("./ancestor::li[1]"));
            String cls = li.getAttribute("class");
            return (cls == null || !cls.contains("not-available")) && swatchLink.isDisplayed();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private void clickSwatch(WebElement swatchLink) {
        scrollIntoViewCentered(swatchLink);
        jsClick(swatchLink);
    }

    private void waitUntilLabelHasText(By label) {
        waitSec(15).until(d -> {
            List<WebElement> els = d.findElements(label);
            return !els.isEmpty() && !els.get(0).getText().trim().isEmpty();
        });
    }

    private boolean isAnyAddToCartButtonVisibleAndEnabled() {
        for (WebElement b : driver.findElements(addToCartButton)) {
            try {
                if (b.isDisplayed() && b.isEnabled()) return true;
            } catch (StaleElementReferenceException ignored) {}
        }
        return false;
    }

    private void selectFirstNonEmptyOptionInVisibleSelects(By selectLocator) {
        for (WebElement selectEl : driver.findElements(selectLocator)) {
            String cls = selectEl.getAttribute("class");
            if (!selectEl.isDisplayed() || (cls != null && cls.contains("no-display"))) continue;

            Select sel = new Select(selectEl);
            for (WebElement opt : sel.getOptions()) {
                String value = opt.getAttribute("value");
                if (value != null && !value.trim().isEmpty()) {
                    sel.selectByValue(value);
                    return;
                }
            }
        }
    }

    private void clickAddToCartOnProductPage() {
        WebDriverWait w = waitSec(CART_OUTCOME_SEC);
        w.until(ExpectedConditions.presenceOfAllElementsLocatedBy(addToCartButton));

        WebElement btn = w.until(d -> {
            for (WebElement b : d.findElements(addToCartButton)) {
                try {
                    if (b.isDisplayed() && b.isEnabled()) return b;
                } catch (StaleElementReferenceException ignored) {}
            }
            return null;
        });

        if (btn == null) throw new RuntimeException("Add to Cart button not found/enabled on product page.");

        scrollIntoViewCentered(btn);
        try {
            w.until(ExpectedConditions.elementToBeClickable(btn));
            btn.click();
        } catch (Exception e) {
            jsClick(btn);
        }
    }

    private WebDriverWait waitSec(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void acceptAlertIfPresent() {
        try {
            Alert a = waitSec(3).until(ExpectedConditions.alertIsPresent());
            a.accept();
        } catch (TimeoutException ignored) {
        }
    }
}
