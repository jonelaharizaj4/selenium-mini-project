package tests;

import helpers.Log;
import helpers.UserPersistence;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.HomePage;
import pages.SalePage;
import utils.TestListener;

import java.util.List;

public class SaleTest extends BaseTest {

    @Test
    public void testSalePriceStyles() {
        HomePage home = new HomePage(driver);
        
        UserPersistence.LoadResult res = UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

    	if (res.fromFile) 
    	{login(res.user);    }
    	
        //registerUserLoggedIn();
    	
        TestListener.info("Sale page");
        SalePage salePage = home.goToAllSale();
        salePage.waitForProductsToLoad();

        List<SalePage.ProductTile> discounted = salePage.getDiscountedTiles();
        TestListener.info("Discounted products found: " + discounted.size());

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(!discounted.isEmpty(), "No discounted products.");

        for (SalePage.ProductTile tile : discounted) {
            boolean regularOk = tile.isRegularPriceStyledAsDiscounted();
            boolean saleOk    = tile.isSalePriceStyledAsProminent();

            String summary = tile.summary(regularOk, saleOk);
            Log.info("[SALE] " + summary);

            if (regularOk && saleOk) TestListener.pass(summary);
            else TestListener.fail(summary);

            softly.assertTrue(regularOk, "Old price style wrong for: " + tile.getName());
            softly.assertTrue(saleOk,    "New price style wrong for: " + tile.getName());
        }

        softly.assertAll();
    }
}
