package helpers;

import org.openqa.selenium.WebDriver;
import pages.HomePage;
import pages.WishListPage;
import pages.WomenProductsPage;

public class TestDataHelper {

    private final WebDriver driver;

    public TestDataHelper(WebDriver driver) {
        this.driver = driver;
    }

    public void ensureWishlistHasAtLeast(int expectedCount) {
        HomePage home = new HomePage(driver);

        home.goToWishlist();
        WishListPage wishlist = new WishListPage(driver);

        int current = wishlist.getNumberOfProducts();
        if (current >= expectedCount) return;

        int missing = expectedCount - current;

        home.goToAllWomen();
        WomenProductsPage women = new WomenProductsPage(driver);
        women.addFirstNToWishlist(missing);

        int after = wishlist.getNumberOfProducts();
        if (after < expectedCount) {
            throw new IllegalStateException(
                    "Precondition failed: Wishlist contains " + after +
                    " items, expected at least " + expectedCount
            );
        }
    }
}
