package tests;

import helpers.Log;
import helpers.UserPersistence;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.WishListPage;
import pages.WomenProductsPage;
import utils.TestListener;

import java.util.List;

public class SortingTest extends BaseTest {

	@Test
	public void testSortByPriceAndWishlistCount() {

	    TestListener.info("Test 6: Sort by Price + Wishlist count");

	    UserPersistence.LoadResult res = UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

    	if (res.fromFile) 
    	{login(res.user);    }
    	
	    //registerUserLoggedIn();
	    
	    TestListener.info("User logged in");

	    HomePage home = new HomePage(driver);

	    WishListPage wl = home.goToWishlist();
	    wl.clearAllItemsIfAny();
	    home.goToBasePage();

	    int before = home.getWishlistCountFromAccMenu();
	    Log.info("Wishlist count before = " + before);
	    TestListener.info("Wishlist count before: " + before);

	    WomenProductsPage women = home.goToAllWomen();

	    Log.step("Sort products by Price");
	    TestListener.info("Sort products by Price");
	    women.sortBy("Price");
	    List<Double> prices = women.visiblePrices();

	    Log.info("Sorted prices (console only): " + prices);
	    TestListener.info("Collected " + prices.size() + " product prices");

	    boolean asc = WomenProductsPage.isSortedAsc(prices);
	    boolean desc = WomenProductsPage.isSortedDesc(prices);

	    if (asc) {
	        Log.info("Sorting verified: ASC");
	        TestListener.info("Price sorting verified: ASC");
	    } else if (desc) {
	        Log.info("Sorting verified: DESC");
	        TestListener.info("Price sorting verified: DESC");
	    } else {
	        Log.warn("Products are NOT sorted. Prices=" + prices);
	        TestListener.fail("Products are NOT sorted by price");
	    }

	    Assert.assertTrue(asc || desc, "Products are NOT sorted by price." );

	    women.addFirstNToWishlist(2);

	    int after = home.getWishlistCountFromAccMenu();
	    Log.info("Wishlist count AFTER adding items = " + after);
	    TestListener.info("Wishlist count after adding items: " + after);

	    Assert.assertEquals(after, 2,"Expected 'My Wish List (2 items)'");

	    Log.step("Test 6 END: PASSED");
	    TestListener.pass("Test 6 finished successfully");
	}


}
