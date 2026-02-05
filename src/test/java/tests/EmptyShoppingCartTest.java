package tests;

import helpers.Log;
import helpers.TestDataHelper;
import helpers.UserPersistence;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.WishListPage;
import utils.TestListener;

public class EmptyShoppingCartTest extends BaseTest {

    private void ensureCartNotEmpty() {
    	UserPersistence.LoadResult res = UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

    	if (res.fromFile) 
    	{login(res.user);    }
    	
    	//registerUserLoggedIn();
    	
        CartPage cartPage = new CartPage(driver);
        cartPage.open();

        int rows = cartPage.getCartRowCount();
        if (rows > 0) {
            TestListener.info("Cart already has items. Rows=" + rows);
            return;
        }

        TestListener.info("Cart is empty -> adding items from wishlist");
        new TestDataHelper(driver).ensureWishlistHasAtLeast(2);

        WishListPage wishListPage = new WishListPage(driver);
        wishListPage.addFirstNWishlistItemsToCart(2);

        if (!cartPage.isOnCartPage()) {
            cartPage.open();
        }

        Assert.assertTrue(cartPage.getCartRowCount() > 0,
                "Precondition failed: cart should contain items after adding from wishlist.");
    }

    @Test
    public void testEmptyShoppingCart() {
        ensureCartNotEmpty();

        CartPage cartPage = new CartPage(driver);
        if (!cartPage.isOnCartPage()) cartPage.open();

        int removed = 0;
        int startRows = cartPage.getCartRowCount();
        TestListener.info("Starting empty cart flow. Initial rows =" + startRows);
        Log.info("Initial rows "+ startRows);

        while (true) {
            int before = cartPage.getCartRowCount();
            if (before == 0) break;

            cartPage.deleteFirstItemFromCart();
            removed++;

            int after = cartPage.getCartRowCount();
            Assert.assertEquals(after, before - 1,
                    "Cart row count should decrease by 1 after deleting an item");
        }

        Assert.assertTrue(cartPage.isEmptyCartMessageDisplayed(),
                "Empty cart message should be displayed: 'You have no items in your shopping cart.'");
        Log.info("Cart emptied successfully. Removed " + removed + " item(s).");
        TestListener.pass("Cart emptied successfully. Removed " + removed + " item(s).");
    }

    
}
