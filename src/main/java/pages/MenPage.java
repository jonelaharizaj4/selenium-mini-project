package pages;

import helpers.Log;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class MenPage extends BasePage {

    private final By blackSwatchImg = By.cssSelector("a.swatch-link img[alt='Black']");
    private final By productCards   = By.cssSelector("ul.products-grid > li.item");

    private final By appliedFiltersBlock   = By.cssSelector("div.currently");
    private final By currentBlackImg  = By.cssSelector("div.currently li.swatch-current img[alt='Black']");
    private final By currentPriceItem = By.xpath("//div[contains(@class,'currently')]//li[.//span[normalize-space()='Price:']]");

    // Price options after black filter
    private final By priceLinks = By.cssSelector("a[href*='price=']");

    public MenPage(WebDriver driver) {
        super(driver);
    }

    public static class PriceRange {
        public final double min;
        public final Double max; // null => and above

        public PriceRange(double min, Double max) {
            this.min = min;
            this.max = max;
        }

        public boolean isAndAbove() { return max == null; }

        @Override
        public String toString() {
            return isAndAbove()
                    ? String.format("$%.2f and above", min)
                    : String.format("$%.2f - $%.2f", min, max);
        }
    }

    public void applyBlackFilter() {
        Log.step("Apply Black color filter");

        WebElement img = waitForVisibility(blackSwatchImg);
        WebElement link = img.findElement(By.xpath("./ancestor::a[1]"));
        jsClick(link);

        wait.until(d -> d.getCurrentUrl().contains("color=20"));
        waitForProductsStable();

        Log.info("Black filter applied");
    }

    public List<WebElement> getVisibleProducts() {
        return wait.until(d -> {
            try {
                List<WebElement> items = d.findElements(productCards);
                if (!items.isEmpty() && items.get(0).isDisplayed()) return items;
                return null;
            } catch (StaleElementReferenceException e) {
                return null;
            }
        });
    }

    public boolean isBlackBorderSelected(WebElement product) {
        try {
            WebElement blackLi =
                    product.findElement(By.cssSelector("li[data-option-label='black'].selected"));

            WebElement swatchLink  = blackLi.findElement(By.cssSelector("a.swatch-link"));
            WebElement swatchLabel = blackLi.findElement(By.cssSelector("span.swatch-label"));

            String border = swatchLink.getCssValue("border");

            if (border == null || border.isBlank() || border.contains("none")) {
                border = swatchLabel.getCssValue("border");
            }

            if (border == null || border.isBlank()) {
                return false;
            }

            String expectedBlue = "rgb(51, 153, 204)";

            return border.contains(expectedBlue);

        } catch (NoSuchElementException e) {
            return false;
        }
    }


    public String getPriceText(WebElement product) {
        try {
            return product.findElement(By.cssSelector(".price")).getText().trim();
        } catch (Exception e) {
            return "N/A";
        }
    }

    public double getPriceValue(WebElement product) {
        return parseMoney(getPriceText(product));
    }

    private void waitForProductsStable() {
        wait.until(d -> {
            List<WebElement> items = d.findElements(productCards);
            return !items.isEmpty() && items.get(0).isDisplayed();
        });
    }

    public PriceRange readDynamicPriceOption() {
        WebElement link = wait.until(d -> {
            List<WebElement> links = d.findElements(priceLinks);
            for (WebElement l : links) {
                try {
                    if (l.isDisplayed()) return l;
                } catch (StaleElementReferenceException ignored) {}
            }
            return null;
        });

        PriceRange r = parsePriceRange(link.getText());
        return r;
    }

    public boolean isDynamicOptionMinWithin(double expectedMin, double expectedMax, PriceRange dynamic) {
        return dynamic.min >= expectedMin && dynamic.min <= expectedMax;
    }

    public PriceRange applyDynamicPriceFilter() {

        WebElement oldFirst = null;
        List<WebElement> before = driver.findElements(productCards);
        if (!before.isEmpty()) oldFirst = before.get(0);

        WebElement link = wait.until(d -> {
            List<WebElement> links = d.findElements(priceLinks);
            for (WebElement l : links) {
                try {
                    if (l.isDisplayed()) return l;
                } catch (StaleElementReferenceException ignored) {}
            }
            return null;
        });

        PriceRange r = parsePriceRange(link.getText());
        jsClick(link);

        if (oldFirst != null) {
            try {
                wait.until(ExpectedConditions.stalenessOf(oldFirst));
            } catch (TimeoutException ignored) {}
        }

        waitForProductsStable();
        return r;
    }

    public void waitForBothFiltersShown() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(appliedFiltersBlock));
        wait.until(ExpectedConditions.visibilityOfElementLocated(currentBlackImg));
        wait.until(ExpectedConditions.visibilityOfElementLocated(currentPriceItem));
        Log.info("Both filters visible");
    }

    public void verifyProductsMatchPriceFilter(PriceRange applied) {

        List<WebElement> products = getVisibleProducts();
        int n = products.size();

        for (int i = 0; i < n; i++) {
            try {
                WebElement p = driver.findElements(productCards).get(i);
                double price = getPriceValue(p);

                boolean ok = applied.isAndAbove()
                        ? price >= applied.min
                        : (price >= applied.min && price <= applied.max);

                if (!ok) throw new AssertionError("Price " + price + " not matching " + applied);

            } catch (StaleElementReferenceException e) {
                i--; 
            }
        }

        Log.info("All " + n + " products match applied price filter");
    }

    private PriceRange parsePriceRange(String text) {
        String cleaned = text.replaceAll("\\(\\d+\\)", "").trim();

        if (cleaned.contains("and above")) {
            double min = parseMoney(cleaned.split("and above")[0].trim());
            return new PriceRange(min, null);
        }

        if (cleaned.contains("-")) {
            String[] parts = cleaned.split("-");
            double min = parseMoney(parts[0].trim());
            double max = parseMoney(parts[1].trim());
            return new PriceRange(min, max);
        }

        return new PriceRange(0, null);
    }

    private double parseMoney(String s) {
        String t = s.replace("$", "").replace(",", "").trim();
        if (t.isEmpty() || t.equalsIgnoreCase("N/A")) return 0.0;
        return Double.parseDouble(t);
    }
}
