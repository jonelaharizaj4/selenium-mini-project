package tests;

import helpers.TestDataHelper;
import helpers.UserPersistence;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.WishListPage;
import utils.TestListener;

import java.math.BigDecimal;
import java.util.List;

public class ShoppingCartTest extends BaseTest {

    @Test
    public void testShoppingCartFromWishlist() {
    	UserPersistence.LoadResult res = UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

    	if (res.fromFile) 
    	{login(res.user);    }
    	
    	//registerUserLoggedIn();
    	
    	new TestDataHelper(driver).ensureWishlistHasAtLeast(2);

    	WishListPage wishlist = new WishListPage(driver);
    	wishlist.addFirstNWishlistItemsToCart(2);

    	CartPage cartPage = new CartPage(driver);
    	  if (!cartPage.isOnCartPage()) {
              cartPage.open();
          }

        cartPage.setQuantityForFirstItemAndUpdate(2);

        List<BigDecimal> rows = cartPage.getRowSubtotals();
        BigDecimal sum = cartPage.sumRowSubtotals();
        BigDecimal grand = cartPage.getGrandTotal();

        TestListener.info("Cart totals | rows=" + rows.size()
        + " | sum=" + sum
        + " | grand=" + grand);
        
        Assert.assertTrue(sum.compareTo(grand) == 0,
                "Sum of row subtotals (" + sum + ") should equal Grand Total (" + grand + ")");
    }


}