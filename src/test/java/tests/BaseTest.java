package tests;

import config.Config;
import driver.DriverFactory;
import helpers.Log;
import helpers.UserData;
import helpers.UserFactory;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import pages.HomePage;
import pages.LoginPage;
import pages.RegisterPage;

@Listeners(utils.TestListener.class)
public class BaseTest {

    public WebDriver driver;

    private static final ThreadLocal<UserData> testUser = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        driver = DriverFactory.createDriver();
        driver.manage().deleteAllCookies();
        driver.get(Config.baseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            if (driver != null) driver.quit();
        } finally {
            testUser.remove();
        }
    }


    public UserData getOrCreateTestUser() {
        UserData u = testUser.get();
        if (u == null) {
            u = UserFactory.createUniqueUser();
            testUser.set(u);
        }
        return u;
    }


    public UserData registerUserLoggedIn() {
        HomePage home = new HomePage(driver);

        RegisterPage registerPage = home.goToRegister();
        registerPage.waitForPage();

        String title = registerPage.getPageTitle();
        Log.info("Register page title: " + title);
        Assert.assertTrue(title.contains("Create"), "Expected Register page title, but got: " + title);

        UserData user = getOrCreateTestUser();

        registerPage.fillAllFields(
            user.firstName,  user.lastName, user.email, user.password
        );
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isSuccessMessageDisplayed(), "Registration failed!");
        Assert.assertEquals(
            registerPage.getSuccessMessage(),
            "Thank you for registering with Tealium Ecommerce.",
            "Unexpected registration success message!"
        );

        return user;
    }


    public UserData registerUserLogout() {
        UserData user = registerUserLoggedIn();
        logout();
        return user;
    }

    public void login(String email, String password) {
        HomePage home = new HomePage(driver);

        LoginPage loginPage = home.goToLogin();
        loginPage.waitForPage();
        loginPage.login(email, password);

        home.waitForLoginToComplete();

        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login failed! Error: " +
        	    (loginPage.isErrorDisplayed() ? loginPage.getErrorMessage() : "No error message shown"));


    }
    public void login(UserData user) {
        login(user.email, user.password);
    }

    public void logout() {
        new HomePage(driver).logout();
    }
    
}
