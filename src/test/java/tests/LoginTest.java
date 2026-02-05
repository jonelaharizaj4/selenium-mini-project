package tests;

import helpers.Log;
import helpers.UserData;
import helpers.UserPersistence;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;

public class LoginTest extends BaseTest {

	@Test
	public void testLogIn() {
		UserPersistence.LoadResult res = UserPersistence.loadOrCreate(() -> registerUserLoggedIn());

    	if (res.fromFile) 
    	{login(res.user);    }
    	UserData user = res.user;

	    Log.info("Username is displayed.");
	    HomePage home = new HomePage(driver);
	    String welcome = home.getWelcomeText().toLowerCase();

	    Assert.assertTrue(
	        welcome.contains(user.firstName.toLowerCase()) &&
	        welcome.contains(user.lastName.toLowerCase()),
	        "Username not shown in welcome message. Actual: " + home.getWelcomeText()
	    );

	    Log.info("Logging out.");
	    logout();
	}

}
