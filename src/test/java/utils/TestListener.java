package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import helpers.ReportManager;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener {

    private static ExtentReports report = ReportManager.getReport();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        // report is initialized already
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = report.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
        test.get().info("Start test");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("PASS");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("SKIPPED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Object testClass = result.getInstance();
        WebDriver driver = ((tests.BaseTest) testClass).driver;

        String screenshotPath = null;

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String testName = result.getMethod().getMethodName();

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File("target/screenshots/" + testName + "_" + timestamp + ".png");

            dest.getParentFile().mkdirs();
            Files.copy(src.toPath(), dest.toPath());

            screenshotPath = dest.getPath();
            System.out.println("📸 Screenshot saved: " + dest.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("❌ Failed to capture screenshot: " + e.getMessage());
        }

        // Report failure + attach screenshot if available
        test.get().fail(result.getThrowable());
        if (screenshotPath != null) {
            try {
                test.get().addScreenCaptureFromPath(screenshotPath);
            } catch (Exception e) {
                System.out.println("❌ Failed to attach screenshot to report: " + e.getMessage());
            }
        }
    }
    public static void info(String msg) {
        if (test.get() != null) test.get().info(msg);
    }
    public static void pass(String msg) {
        if (test.get() != null) test.get().pass(msg);
    }
    public static void fail(String msg) {
        if (test.get() != null) test.get().fail(msg);
    }

    @Override
    public void onFinish(ITestContext context) {
        report.flush();
        test.remove();
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
}
