package TestCases;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class View_Market_Offer {

    private WebDriver driver;
    private Actions actions;
    private WebDriverWait wait;
    
    private static String mobileNumber;
    private static String otp;
    private static String bidPrice;

    // === Jenkins/Maven Parameters ===
    @Parameters({"mobileNumber", "bidPrice"})
    @BeforeClass
    public void setup(@Optional("9911991191") String mobile, @Optional("10310") String bid) {
        mobileNumber = mobile;
        bidPrice = bid;

        System.out.println("*************** TestCase Execution for Buyer Market Offer View Flow ***************");
        try {
            WebDriverManager.chromedriver().setup();

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
            
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);

            driver = new ChromeDriver(options);
            actions = new Actions(driver);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            System.out.println("WebDriver setup successful for mobile: " + mobileNumber + ", bid: " + bidPrice);
        } catch (Exception e) {
            Assert.fail("FAILED [Setup]: WebDriver setup failed: " + e.getMessage());
        }
    }

    // === Login Steps ===
    @Test(priority = 1)
    public void testOpenLoginPage() {
        System.out.println("Step 1: Open Login Page");
        try {
            driver.get("https://digielv.mmcm.in/");
            Assert.assertNotNull(driver.getTitle(), "Page did not load or title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Navigation]: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testOpenLoginPage")
    public void testClickLoginRegisterButton() {
        System.out.println("Step 2: Click Login/Register Button");
        clickElement(By.xpath("//button[contains(text(),'Login/Register')]"));
    }

    @Test(priority = 3, dependsOnMethods = "testClickLoginRegisterButton")
    public void testEnterMobileAndClickLogin() {
        System.out.println("Step 3: Enter Mobile Number and Click Login");
        typeText(By.xpath("//input[@placeholder='Enter Your Mobile Number']"), mobileNumber);
        clickElement(By.xpath("//button[normalize-space(text())='Login']"));
    }

    @Test(priority = 4, dependsOnMethods = "testEnterMobileAndClickLogin")
    public void testFetchOtpFromDB() {
        System.out.println("Step 4: Fetch OTP from Database");
        otp = fetchOtpFromDatabase(mobileNumber);
        Assert.assertNotNull(otp, "OTP fetched is null.");
        Assert.assertEquals(otp.length(), 6, "OTP is not 6 digits. OTP=" + otp);
        System.out.println("Fetched OTP: " + otp);
    }

    @Test(priority = 5, dependsOnMethods = "testFetchOtpFromDB")
    public void testEnterOtpInputs() throws InterruptedException {
        System.out.println("Step 5: Enter OTP Inputs");
        List<WebElement> otpInputs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")));
        Assert.assertEquals(otpInputs.size(), 6, "OTP input boxes not found.");

        for (int i = 0; i < 6; i++) {
            WebElement input = otpInputs.get(i);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
            input.click();
            input.clear();
            input.sendKeys(Character.toString(otp.charAt(i)));
            Thread.sleep(100);
        }
        System.out.println("OTP entered successfully.");
    }

    @Test(priority = 6, dependsOnMethods = "testEnterOtpInputs")
    public void testDismissKycPopupIfPresent() {
        System.out.println("Step 6: Dismiss optional KYC popup");
        try {
            WebElement skipBtn = wait.withTimeout(Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//*[normalize-space()='Skip For Now']"))
            );
            skipBtn.click();
            System.out.println("KYC popup dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC popup appeared, continuing.");
        }
    }

    // === Market Offer & Bid Steps ===
    @Test(priority = 7, dependsOnMethods = "testDismissKycPopupIfPresent")
    public void testNavigateToViewMarketOffer() {
        System.out.println("Step 7: Navigate to View Market Offer");
        clickElement(By.xpath("//a[contains(normalize-space(),'Transaction History') or contains(normalize-space(),'Market Offer')]"));
    }

    @Test(priority = 8, dependsOnMethods = "testNavigateToViewMarketOffer")
    public void testClickViewAllOffer() {
        System.out.println("Step 8: Click View All Offer");
        clickElement(By.xpath("//button[contains(text(),'View All Offer')]"));
    }

    @Test(priority = 9, dependsOnMethods = "testClickViewAllOffer")
    public void testClickPlaceOfferToBuy() {
        System.out.println("Step 9: Click Place Offer to Buy");
        clickElement(By.xpath("//button[contains(text(),'Place Offer to Buy')]"));
    }

    @Test(priority = 10, dependsOnMethods = "testClickPlaceOfferToBuy")
    public void testEnterBidPrice() {
        System.out.println("Step 10: Enter Bid Price");
        typeText(By.xpath("//*[@id='integeronly']"), bidPrice);
    }

    @Test(priority = 11, dependsOnMethods = "testEnterBidPrice")
    public void testClickCreateBid() {
        System.out.println("Step 11: Click Create Bid");
        clickElement(By.xpath("//button[contains(text(),'Create Bid')]"));
    }

    @Test(priority = 12, dependsOnMethods = "testClickCreateBid")
    public void testClickContinueButton() {
        System.out.println("Step 12: Click Continue after Bid");
        clickElement(By.xpath("(//button[contains(text(),'Continue')])[last()]"));
        System.out.println("Buyer Market Offer & Bid test completed successfully.");
    }

    // === TearDown ===
    @AfterClass
    public void teardown() {
        System.out.println("Test Execution Completed.");
        if (driver != null) {
            driver.quit();
        }
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            updateIsLoggedInInDB(mobileNumber);
        }
    }

    // === Utility Methods ===
    private void clickElement(By locator) {
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            el.click();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Click failed on locator: " + locator + ", Reason: " + e.getMessage());
        }
    }

    private void typeText(By locator, String text) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            el.clear();
            el.sendKeys(text);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Type failed on locator: " + locator + ", Reason: " + e.getMessage());
        }
    }

    public static String updateIsLoggedInInDB(String mobileNumber) {
        String sql = "UPDATE common.user_mstr SET is_logged_in = 0 WHERE mobile_no = ?";
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        try (Connection conn = DriverManager.getConnection(url, "uatuser", "password@123");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, Long.parseLong(mobileNumber));
            int rows = pstmt.executeUpdate();
            System.out.println("DB Update: is_logged_in reset for mobile " + mobileNumber + ", rows affected: " + rows);
        } catch (Exception e) {
            System.out.println("DB Update error: " + e.getMessage());
        }
        return sql;
    }

    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String sql = "SELECT otp FROM common.user_mstr WHERE mobile_no = ?";
        try (Connection conn = DriverManager.getConnection(url, "uatuser", "password@123");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, Long.parseLong(mobileNumber));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) otp = rs.getString("otp");
        } catch (Exception e) {
            Assert.fail("DB OTP fetch failed: " + e.getMessage());
        }
        return otp;
    }
}
