/*
    Testcase Steps for Buyer: View Market Offer and Place Bid

    1. Read mobile number from console
    2. Open Login Page
    3. Click Login/Register Button
    4. Enter Mobile and Click Login
    5. Fetch OTP from Database
    6. Enter OTP Inputs
    7. Dismiss KYC Popup (if any)
    8. Navigate to View Market Offer
    9. Click View All Offer
    10. Click Place Offer to Buy
    11. Enter Bid Price
    12. Click Create Bid
    13. Click Continue
*/

package TestCases;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
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
    private static String mobileNumber = "9911991191";
    private static String otp;

    @BeforeClass
    public void setup() {
        System.out.println("***************  TestCase Execution for Buyer Market Offer View Flow  ***************");
        try {
            // Set ChromeDriver path if not present in system environment
            WebDriverManager.chromedriver().setup();
            // Chrome Preferences
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
            System.out.println("WebDriver setup successful.");
        } catch (Exception e) {
            Assert.fail("FAILED [Setup]: WebDriver failed to setup: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testOpenLoginPage() {
        System.out.println("\n========== Step 2: Navigate to the login page ==========");
        try {
            driver.get("https://digielv.mmcm.in/");
            Assert.assertNotNull(driver.getTitle(), "FAILED [Navigation]: Page did not load or title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load the login page. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testOpenLoginPage")
    public void testClickLoginRegisterButton() {
        System.out.println("\n========== Step 3: Click Login/Register button ==========");
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")));
            forceClick(btn);
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Couldn't find or click the Login/Register button. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testClickLoginRegisterButton")
    public void testEnterMobileAndClickLogin() {
        System.out.println("\n========== Step 4: Enter mobile number and click Login ==========");
        try {
            WebElement inputPhone = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Enter Your Mobile Number']")));
            inputPhone.clear();
            inputPhone.sendKeys(mobileNumber);
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space(text())='Login']")));
            forceClick(loginBtn);
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Login]: Could not enter mobile or click Login. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testEnterMobileAndClickLogin")
    public void testFetchOtpFromDB() {
        System.out.println("\n========== Step 5: Fetch OTP from database ==========");
        try {
            otp = fetchOtpFromDatabase(mobileNumber);
            Assert.assertNotNull(otp, "FAILED [OTP Fetch]: OTP fetched is null from DB (DB error or wrong mobile).");
            Assert.assertEquals(otp.length(), 6, "FAILED [OTP Fetch]: OTP is not 6 digits. OTP=" + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Fetch DB]: Could not fetch OTP from DB: " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testFetchOtpFromDB")
    public void testEnterOtpInputs() throws InterruptedException {
        System.out.println("\n========== Step 6: Enter OTP into input fields ==========");
        try {
            WebDriverWait otpWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            List<WebElement> otpInputs = otpWait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                )
            );
            Assert.assertEquals(otpInputs.size(), 6, "FAILED [OTP Boxes]: Did not find 6 OTP input boxes. Found: " + otpInputs.size());
            for (int i = 0; i < 6; i++) {
                WebElement input = otpInputs.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", input);
                forceClick(input);
                input.clear();
                input.sendKeys(Character.toString(otp.charAt(i)));
                Thread.sleep(100);
            }
            System.out.println("OTP entered successfully: " + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: Could not enter OTP. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testEnterOtpInputs")
    public void testDismissKycPopupIfPresent() {
        System.out.println("\n========== Step 7: Handle/dismiss optional KYC popup if it appears ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[normalize-space()='Skip For Now']")
                )
            );
            forceClick(cancelPopupButton);
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
    }

    @Test(priority = 7, dependsOnMethods = "testDismissKycPopupIfPresent")
    public void testNavigateToViewMarketOffer() {
        System.out.println("\n========== Step 8: Click 'Transaction History' tab in sidebar ==========");
        try {
            WebElement transHist = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"sidebar\"]/ul/li[3]/a")
                )
            );
            forceClick(transHist);
        } catch (Exception e) {
            Assert.fail("FAILED [Sidebar/View Market Offer]: Could not click Transaction History tab. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 8, dependsOnMethods = "testNavigateToViewMarketOffer")
    public void testClickViewAllOffer() {
        System.out.println("\n========== Step 9: Click 'View All Offer' button ==========");
        try {
            WebElement allOfferBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@class=\"btn btn-primary w-100 rounded-pill\" and contains(text(), 'View All Offer')]")
                )
            );
            forceClick(allOfferBtn);
        } catch (Exception e) {
            Assert.fail("FAILED [View All Offer]: Could not click 'View All Offer'. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testClickViewAllOffer")
    public void testClickPlaceOfferToBuy() {
        System.out.println("\n========== Step 10: Click 'Place Offer to Buy' ==========");
        try {
            WebElement buyOfferBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@class=\"btn btn-primary w-100 rounded-pill\" and contains(text(), 'Place Offer to Buy')]")
                )
            );
            forceClick(buyOfferBtn);
        } catch (Exception e) {
            Assert.fail("FAILED [Place Offer To Buy]: Could not click 'Place Offer to Buy'. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 10, dependsOnMethods = "testClickPlaceOfferToBuy")
    public void testEnterBidPrice() {
        System.out.println("\n========== Step 11: Enter bid price ==========");
        try {
            WebElement bidPriceInput = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"integeronly\"]")
                )
            );
            bidPriceInput.clear();
            bidPriceInput.sendKeys("10310");
        } catch (Exception e) {
            Assert.fail("FAILED [Bid Price]: Could not enter bid price. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 11, dependsOnMethods = "testEnterBidPrice")
    public void testClickCreateBid() {
        System.out.println("\n========== Step 12: Click 'Create Bid' ==========");
        try {
            WebElement createBidBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@class=\"btn btn-primary rounded-pill\" and contains(text(), 'Create Bid')]")
                )
            );
            forceClick(createBidBtn);
        } catch (Exception e) {
            Assert.fail("FAILED [Create Bid]: Could not click 'Create Bid'. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 12, dependsOnMethods = "testClickCreateBid")
    public void testClickContinueButton() {
        System.out.println("\n========== Step 13: Click Continue ==========");
        try {
            WebElement continueBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("(//*[@class=\"btn btn-primary w-100 rounded-pill\" and contains(text(), ' Continue ')])[4]")
                )
            );
            forceClick(continueBtn);
            System.out.println("Test scenario for Buyer Market Offer & Bid successful.");
        } catch (Exception e) {
            Assert.fail("FAILED [Continue Button]: Could not click Continue. Reason: " + e.getMessage());
        }
    }

    @AfterClass
    public void teardown() {
        System.out.println("Test Execution Completed.");
        if (driver != null) {
            try { System.out.println("Logged Out Successfully!"); }
            finally { driver.quit(); }
        }
     
        }

    // === Utility Methods ===

    // Universal JavaScript "force click" for any element
    private void forceClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
    }

    // Utility method to read exactly 10 digits from the console
    public static String readTenDigitsFromConsole() {
        StringBuilder sb = new StringBuilder(10);
        System.out.print("Please enter your 10-digit mobile number: ");
        try {
            while (sb.length() < 10) {
                int ch = System.in.read();
                if (ch == -1) break;
                if (ch == '\r' || ch == '\n') continue;
                char c = (char) ch;
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                    System.out.print(c);
                }
            }
            int leftover;
            do { leftover = System.in.read(); } while (leftover != -1 && leftover != '\n');
        } catch (IOException e) {
            Assert.fail("FAILED [Console Read]: Failed reading mobile from console: " + e.getMessage());
        }
        return sb.toString();
    }

    // Utility method to fetch OTP by mobile number from DB
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        String query1 = "SELECT otp FROM common.user_mstr WHERE mobile_no = " + mobileNumber;
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(query1);
            if (rs1.next()) {
                otp = rs1.getString("otp");
            } else {
                Assert.fail("FAILED [DB Query]: No user found with mobile number: " + mobileNumber);
            }
            rs1.close();
            stmt1.close();
            conn.close();
        } catch (Exception e) {
            Assert.fail("FAILED [DB OTP Fetch]: DB error while fetching OTP: " + e.getMessage());
        }
        return otp;
    }
}





