package pages;

import helpers.Css;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class SalePage extends BasePage {

    //Locators: finding tiles, using relative locators in each tile
    private final By productTiles = By.cssSelector(".products-grid li.item");
    private final By productNameLink = By.cssSelector("h2.product-name a");
    private final By regularPrice    = By.cssSelector(".old-price .price");
    private final By salePrice       = By.cssSelector(".special-price .price");

    public SalePage(WebDriver driver) {
        super(driver);
    }

    public void waitForProductsToLoad() {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productTiles));
    }

    public List<ProductTile> getAllTiles() {
        return driver.findElements(productTiles)
                .stream()
                .map(ProductTile::new)
                .toList();
    }

    public List<ProductTile> getDiscountedTiles() {
        List<ProductTile> discounted = new ArrayList<>();
        for (ProductTile tile : getAllTiles()) {
            if (tile.hasRegularAndSalePrice()) discounted.add(tile);
        }
        return discounted;
    }

    // A single product tile on the Sale grid. 
    public class ProductTile {
        private final WebElement root;

        public ProductTile(WebElement root) {
            this.root = root;
        }

        public String getName() {
            return textOr("Unknown", productNameLink);
        }

        public boolean hasRegularAndSalePrice() {
            return exists(regularPrice) && exists(salePrice);
        }

        public String regularPriceText() { return textOr("N/A", regularPrice); }
        public String salePriceText()    { return textOr("N/A", salePrice); }

        // Old price: grey + line-through
        public boolean isRegularPriceStyledAsDiscounted() {
            WebElement el = firstOrNull(regularPrice);
            if (el == null) return false;

            int[] rgb = Css.rgb(el.getCssValue("color"));
            boolean greyish = Css.isGreyish(rgb, 110, 210);
            boolean struck  = getTextDecorationLine(el).contains("line-through");

            return greyish && struck;
        }

        // New price: blue + NOT line-through
        public boolean isSalePriceStyledAsProminent() {
            WebElement el = firstOrNull(salePrice);
            if (el == null) return false;

            int[] rgb = Css.rgb(el.getCssValue("color"));
            boolean blueish = Css.isBlueish(rgb);
            boolean struck  = getTextDecorationLine(el).contains("line-through");

            return blueish && !struck;
        }

        public String summary(boolean regularOk, boolean saleOk) {
            return String.format(
                    "%s | regular=%s (%s) | sale=%s (%s)",
                    getName(),
                    regularPriceText(), regularOk ? "grey + strike" : "wrong style",
                    salePriceText(), saleOk ? "blue" : "wrong style"
            );
        }

        // helpers
        private boolean exists(By locator) {
            return !root.findElements(locator).isEmpty();
        }

        private WebElement firstOrNull(By locator) {
            List<WebElement> els = root.findElements(locator);
            return els.isEmpty() ? null : els.get(0);
        }

        private String textOr(String fallback, By locator) {
            WebElement el = firstOrNull(locator);
            return el == null ? fallback : el.getText().trim();
        }

        private String getTextDecorationLine(WebElement el) {
            // depends on browser
            String line = el.getCssValue("text-decoration-line");
            if (line != null && !line.isBlank()) return line;

            String full = el.getCssValue("text-decoration");
            return full == null ? "" : full;
        }
    }
}
