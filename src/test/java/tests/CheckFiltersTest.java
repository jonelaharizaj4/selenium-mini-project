package tests;

import helpers.Log;
import helpers.UserPersistence;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.HomePage;
import pages.MenPage;
import utils.TestListener;

import java.util.List;

public class CheckFiltersTest extends BaseTest {

    @Test
    public void testMenFiltersBlackAndPrice() {

        TestListener.info("Test 5: Men filters (Black + Price)");

        UserPersistence.LoadResult res =UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

        if (res.fromFile) {
            login(res.user);
        }

       // registerUserLoggedIn();
        
        TestListener.info("User logged in");

        HomePage home = new HomePage(driver);
        home.goToAllMen();

        MenPage men = new MenPage(driver);
        SoftAssert softly = new SoftAssert();

        men.applyBlackFilter();
        TestListener.info("Applied Black color filter");
        Log.step("Applied Black color filter");

        List<WebElement> productsAfterBlack = men.getVisibleProducts();
        Assert.assertTrue(!productsAfterBlack.isEmpty(), "No product after Black filter.");

        TestListener.info("Products after Black filter: " + productsAfterBlack.size());
        Log.info("Products after Black filter: " + productsAfterBlack.size());

        for (int i = 0; i < productsAfterBlack.size(); i++) {
            WebElement product = productsAfterBlack.get(i);

            boolean borderOk = men.isBlackBorderSelected(product);
            if (!borderOk) {
                String msg = "Blue border missing #" + (i + 1);
                TestListener.fail(msg);
                softly.fail(msg);
            }
        }

        TestListener.info("Black border style verified for all products");
        Log.step("Black border style verified for all products");

        // -------- Price filter --------
        MenPage.PriceRange dynamic = men.readDynamicPriceOption();
        TestListener.info("Dynamic price option read: " + dynamic);
        Log.info("Dynamic price option read: " + dynamic);

        boolean minWithinExpected =
                men.isDynamicOptionMinWithin(0, 99, dynamic);
        softly.assertTrue(minWithinExpected, "Price option not compatible with $0-$99: " + dynamic);

        MenPage.PriceRange applied = men.applyDynamicPriceFilter();
        TestListener.info("Applied price filter: " + applied);
        Log.step("Applied price filter: " + applied);

        men.waitForBothFiltersShown();

        List<WebElement> productsAfterPrice = men.getVisibleProducts();
        TestListener.info("Products after Price filter: " + productsAfterPrice.size());
        Log.info("Products after Price filter: " + productsAfterPrice.size());

        Assert.assertEquals(productsAfterPrice.size(), 3,
                "Expected exactly 3 products after Price filter."
        );

        men.verifyProductsMatchPriceFilter(applied);

        softly.assertAll();

        TestListener.pass("PASS");
        Log.step("END Test 5");
    }
}
