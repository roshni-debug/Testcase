/*
    Testcase Steps for Buyer: Bids_Cancel
 *  1. Get 10-digit mobile number input
 *  2. Navigate to login page
 *  3. Click Login/Register button
 *  4. Enter mobile number and click Login
 *  5. Fetch OTP from database and enter OTP
 *  6. Dismiss KYC popup if present
 *  7. Click 'My Bids' 
 *  8. Click 'Cancel Bid'
 *  9. Confirm cancellation
 * 10. Click 'Continue' to finish bid cancellation
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

public class OfferCancel {

    private WebDriver driver;
    private Actions actions;
    private WebDriverWait wait;
    private static String mobileNumber = "9999999990";
    private static String otp;

    @BeforeClass
    public void setup() {
        System.out.println("***************  TestCase Execution for Buyer Bids Cancellation Flow  ***************");
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
    public void testGetMobileNumberInput() {
        System.out.println("\n========== Step 1: Get 10-digit mobile number input from console ==========");
        try {
            mobileNumber = readTenDigitsFromConsole();
            Assert.assertEquals(mobileNumber.length(), 10, "FAILED [Input Error]: Mobile number entered is not 10 digits.");
        } catch (Exception e) {
            Assert.fail("FAILED [Console Input]: Could not read mobile number from console. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testGetMobileNumberInput")
    public void testOpenLoginPage() {
        System.out.println("\n========== Step 2: Navigate to the login page ==========");
        try {
            driver.get("https://digielv.mmcm.in/");
            Assert.assertNotNull(driver.getTitle(), "FAILED [Navigation]: Page did not load or title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load the login page. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testOpenLoginPage")
    public void testClickLoginRegisterButton() {
        System.out.println("\n========== Step 3: Click Login/Register button ==========");
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Couldn't find or click the Login/Register button. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testClickLoginRegisterButton")
    public void testEnterMobileAndClickLogin() {
        System.out.println("\n========== Step 4: Enter mobile number and click Login ==========");
        try {
            driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
            driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Login]: Could not enter mobile or click Login. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testEnterMobileAndClickLogin")
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

    @Test(priority = 6, dependsOnMethods = "testFetchOtpFromDB")
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
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
                input.click();
                input.clear();
                input.sendKeys(Character.toString(otp.charAt(i)));
                Thread.sleep(100);
            }
            System.out.println("OTP entered successfully: " + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: Could not enter OTP. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 7, dependsOnMethods = "testEnterOtpInputs")
    public void testDismissKycPopupIfPresent() {
        System.out.println("\n========== Step 7: Handle/dismiss optional KYC popup if it appears ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    // Edit to match actual button if necessary; fallback to original ID path:
                    By.xpath("//*[normalize-space()=\"Skip For Now\"]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
    }


    @Test(priority = 8, dependsOnMethods = "testDismissKycPopupIfPresent")
    public void testMyBids() {
        System.out.println("\n========== Step 9: Click 'My Bids' button ==========");
        try {
            WebDriverWait BidBtnWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement allOfferBtn = BidBtnWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='My Bids']")
                )
            );
            allOfferBtn.click();
        } catch (Exception e) {
            Assert.fail("FAILED [My Bids]: Could not click 'My Bids'. Reason: " + e.getMessage());
        }
        
    }
    
        @Test(priority = 9, dependsOnMethods = "testMyBids")
        public void testClickCancelBid() {
        	   System.out.println("\n========== Step 10: Click 'Cancel' button ==========");
        try {
            WebDriverWait buyOfferBtnWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement buyOfferBtn = buyOfferBtnWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@class=\"btn btn-danger w-100 rounded-pill ng-star-inserted\" and contains(text(),'Cancel Bid')]")
                )
            );
            buyOfferBtn.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Cancel Bid]: Could not click 'Cancel Bid Button'. Reason: " + e.getMessage());
        }
    }
    
    @Test(priority = 10, dependsOnMethods = "testClickCancelBid")
    public void testcliconconfirmbutton() {
    	   System.out.println("\n========== Step 10: Click 'Confirm' button ==========");
           try {
               WebDriverWait Confirmbtn = new WebDriverWait(driver, Duration.ofSeconds(10));
               WebElement Button = Confirmbtn.until(
                   ExpectedConditions.elementToBeClickable(
                       By.xpath("(//*[@class=\"btn btn-success rounded-pill\" and contains(text(), \"Confirm\")])[2]")
                   )
               );
               Button.click();
           } catch (Exception e) {
               Assert.fail("FAILED [Cancel Bid]: Could not click 'Cancle Bid Button'. Reason: " + e.getMessage());
           }
       }
    	
    @Test(priority = 10, dependsOnMethods = "testcliconconfirmbutton")
    public void ContinueButton() {
    	   System.out.println("\n========== Step 10: Click 'Continue' button ==========");
           try {
               WebDriverWait Continuebtn = new WebDriverWait(driver, Duration.ofSeconds(10));
               WebElement Button1 = Continuebtn.until(
                   ExpectedConditions.elementToBeClickable(
                       By.xpath("(//*[@class=\"btn btn-primary w-100 rounded-pill\" and contains(text(), ' Continue ')])[1]")
                   )
               );
               Button1.click();
           } catch (Exception e) {
               Assert.fail("FAILED [Continue Button]: Could not click 'Continue Button'. Reason: " + e.getMessage());
           }
       }

    @AfterClass
    public void teardown() {
        System.out.println("Test Execution Completed.");

        if (driver != null) {
            try {
                System.out.println("Logged Out Successfully!");
            } finally {
                driver.quit();   //
            }
        }

        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            updateIsLoggedInInDB(mobileNumber);
        }
    }


    // === Utility Methods ===
    	public static String updateIsLoggedInInDB(String mobileNumber) {
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        String update = "UPDATE common.user_mstr SET is_logged_in = 0 WHERE mobile_no = ?";

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement pstmt = conn.prepareStatement(update);
            pstmt.setLong(1, Long.parseLong(mobileNumber));  
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("is_logged_in reset in DB for mobile: " + mobileNumber);
            } else {
                System.out.println("No row updated for mobile: " + mobileNumber);
            }
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error updating is_logged_in in DB: " + e.getMessage());
        }
		return update;  
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
