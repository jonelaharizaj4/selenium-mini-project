package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import helpers.UserPersistence;
import pages.HomePage;
import pages.WomenProductsPage;
import utils.TestListener;

public class HoverTest extends BaseTest {

    @Test
    public void testHoverEffectOnWomenProduct() {

        TestListener.info("Test 3: Hover style on Women product");

        UserPersistence.LoadResult res =UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

        if (res.fromFile) {
            login(res.user);
        }
        
        //registerUserLoggedIn();
        
        TestListener.info("User logged in");

        HomePage homePage = new HomePage(driver);
        homePage.goToAllWomen();

        WomenProductsPage womenPage = new WomenProductsPage(driver);

        boolean hoverProven = womenPage.hoverVerifyStyle();

        if (hoverProven) {
            TestListener.pass("Hover effect proven (style change and/or tooltip detected)");
        } else {
            TestListener.fail("Hover effect NOT proven (no style change / tooltip detected)");
        }

        Assert.assertTrue(
                hoverProven,
                "Hover effect not proven: neither style change nor tooltip/name was detected."
        );
    }
}
