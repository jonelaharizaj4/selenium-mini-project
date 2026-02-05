package pages;

import helpers.Log;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.Config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CartPage extends BasePage {

    private static final String CART_PATH = "checkout/cart/";
    private static final String CART_URL_PART = "/checkout/cart";
    private static final int LOAD_SEC = 20;


    private final By cartTable = By.id("shopping-cart-table");
    private final By cartRows  = By.cssSelector("#shopping-cart-table tbody tr");

    private final By emptyCartMsg = By.cssSelector(".cart-empty");

    private final By qtyInputInRow  = By.cssSelector("td.product-cart-actions input.qty");
    private final By updateBtnInRow = By.cssSelector("td.product-cart-actions button.btn-update");
    private final By removeBtnInRow = By.cssSelector("td.product-cart-remove a.btn-remove");

    private final By rowSubtotalPrice = By.cssSelector("td.product-cart-total .price");
    private final By totalsTable = By.id("shopping-cart-totals-table");
    private final By grandTotalPrice = By.cssSelector("#shopping-cart-totals-table tfoot .price");

    public CartPage(WebDriver driver) {
        super(driver);
    }
    
    private WebDriverWait w() { return new WebDriverWait(driver, Duration.ofSeconds(LOAD_SEC)); }


    public void open() {
        driver.get(Config.baseUrl() + CART_PATH);

        w().until(ExpectedConditions.urlContains("/checkout/cart"));
        w().until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(cartTable),
                ExpectedConditions.presenceOfElementLocated(emptyCartMsg)
        ));
    }

    public boolean isOnCartPage() {
        return driver.getCurrentUrl().contains(CART_URL_PART);
    }

    public void setQuantityForFirstItemAndUpdate(int qty) {
        Log.step("Cart: set quantity=" + qty + " for first item and update");

        if (!isOnCartPage()) open();

        w().until(d -> !d.findElements(cartRows).isEmpty() || !d.findElements(emptyCartMsg).isEmpty());

        if (driver.findElements(cartRows).isEmpty()) {
            throw new RuntimeException("Cart is empty - cannot set quantity.");
        }

        WebElement firstRow = driver.findElements(cartRows).get(0);
        WebElement qtyInput = firstRow.findElement(qtyInputInRow);
        scrollIntoViewCentered(qtyInput);

        qtyInput.click();
        qtyInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        qtyInput.sendKeys(String.valueOf(qty));
        qtyInput.sendKeys(Keys.TAB);

        WebElement updateBtn = firstRow.findElement(updateBtnInRow);
        scrollIntoViewCentered(updateBtn);
        safeClick(updateBtn);

        w().until(d -> {
            try {
                List<WebElement> rowsNow = d.findElements(cartRows);
                if (rowsNow.isEmpty()) return false;

                WebElement rowNow = rowsNow.get(0);
                String valNow = rowNow.findElement(qtyInputInRow).getAttribute("value");
                return String.valueOf(qty).equals(valNow);
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    
    public List<BigDecimal> getRowSubtotals() {
        w().until(d -> d.findElements(cartRows).size() > 0);

        List<WebElement> rows = driver.findElements(cartRows);
        List<BigDecimal> out = new ArrayList<>();

        for (WebElement row : rows) {
            String subtotalText = safeText(row, rowSubtotalPrice);
            out.add(parseMoney(subtotalText));
        }
        return out;
    }

    public BigDecimal sumRowSubtotals() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal b : getRowSubtotals()) sum = sum.add(b);
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getGrandTotal() {
        w().until(ExpectedConditions.presenceOfElementLocated(totalsTable));

        String gtText = w().until(ExpectedConditions.visibilityOfElementLocated(grandTotalPrice)).getText().trim();
        return parseMoney(gtText);
    }

    public void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    private String safeText(WebElement scope, By locator) {
        try {
            WebElement el = scope.findElement(locator);
            return el.getText() == null ? "" : el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private BigDecimal parseMoney(String s) {
        String cleaned = (s == null) ? "" : s.replaceAll("[^0-9.]", "");
        if (cleaned.trim().isEmpty()) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
    }

    public int getCartRowCount() {
        return driver.findElements(cartRows).size();
    }

    public void deleteFirstItemFromCart() {
        w().until(d -> d.findElements(cartRows).size() > 0);
        int before = getCartRowCount();

        WebElement firstRow = driver.findElements(cartRows).get(0);
        WebElement removeBtn = firstRow.findElement(removeBtnInRow);
        scrollIntoViewCentered(removeBtn);
        safeClick(removeBtn);

        w().until(d -> d.findElements(cartRows).size() == before - 1 || !d.findElements(emptyCartMsg).isEmpty());
    }

    public boolean isEmptyCartMessageDisplayed() {
        return !driver.findElements(emptyCartMsg).isEmpty()
                && driver.findElement(emptyCartMsg).getText().contains("You have no items in your shopping cart.");
    }
    
    
}