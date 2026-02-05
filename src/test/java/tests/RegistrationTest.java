package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import helpers.Log;
import helpers.UserData;
import helpers.UserPersistence;
import pages.HomePage;

public class RegistrationTest extends BaseTest {

	@Test
	public void testCreateAccount() {
	    UserData user = registerUserLogout();
        UserPersistence.save(user);
	    Log.info("Registration done.");

	    HomePage home = new HomePage(driver);
	    Assert.assertTrue(home.isLoggedOut(), "User is NOT logged out.");
	}

}
