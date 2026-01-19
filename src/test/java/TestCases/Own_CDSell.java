/*
    Testcase Steps for Seller: Sell Own CD
    1. Read mobile number from console
    2. Open Login Page
    3. Click Login/Register Button
    4. Enter Mobile and Click Login
    5. Fetch OTP from Database
    6. Enter OTP Inputs
    7. Dismiss KYC Popup (if any)
    8. Navigate to List of CDs
    9. Click Sell Button
    10. Enter Offer Price
    11. Click Create Offer
    12. Click Final Continue

    Each step mentions why the testcase may fail in the comments.
*/

package TestCases;

import java.time.Duration;
import java.util.List;
import java.io.IOException;
import java.sql.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Own_CDSell {

    private WebDriver driver;
    private Actions actions;
    private WebDriverWait wait;
    private static String mobileNumber = "9999999990";
    private static String otp;

    @BeforeClass
    public void setup() {
        System.out.println("***************  TestCase Execution for Seller CD Sell Flow  ***************");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
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
        // Why might this test fail?
        // - Page may not load (site down, network issue, bad URL).
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
        // Why might this test fail?
        // - Button not found (XPath incorrect)
        // - Button overlays or browser compatibility issue
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Couldn't find or click the Login/Register button. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testClickLoginRegisterButton")
    public void testEnterMobileAndClickLogin() {
        System.out.println("\n========== Step 4: Enter mobile number and click Login ==========");
        // Why might this test fail?
        // - Input field not found, site layout changed, network lag
        try {
            driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
            driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Login]: Could not enter mobile or click Login. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testEnterMobileAndClickLogin")
    public void testFetchOtpFromDB() {
        System.out.println("\n========== Step 5: Fetch OTP from database ==========");
        // Why might this test fail?
        // - DB not reachable, wrong credentials, mobile not found, OTP not present or not 6 digits
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
        // Why might this test fail?
        // - OTP fields not found, not interactable, mismatch count, JS errors
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

        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: Could not enter OTP. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testEnterOtpInputs")
    public void testDismissKycPopupIfPresent() {
        System.out.println("\n========== Step 7: Handle/dismiss optional KYC popup if it appears ==========");
        // Why might this test fail?
        // - Popup not present (not a failure)
        // - Popup can't be clicked
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div[2]/div/div[3]/button[1]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was handled.");
        } catch (Exception e) {
            System.out.println("No KYC popup present, continuing. (Reason: " + e.getMessage() + ")");
        }
    }

    @Test(priority = 7, dependsOnMethods = "testDismissKycPopupIfPresent")
    public void testNavigateToListOfCDs() {
        System.out.println("\n========== Step 8: Click 'List of CDs' from the sidebar ==========");
        // Why might this test fail?
        // - Sidebar or link not present or wrong XPath
        try {
            WebDriverWait sideBar = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement listCDs = sideBar.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(normalize-space(.), 'List of CDs')]"))
            );
            listCDs.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Sidebar/List of CDs]: Could not click 'List of CDs'. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 8, dependsOnMethods = "testNavigateToListOfCDs")
    public void testClickSellButton() {
        System.out.println("\n========== Step 9: Click Sell button ==========");
        // Why might this test fail?
        // - Button not found or index/class selector not matching
        try {
            WebDriverWait button = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement sell = button.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@class='btn w-md-50 w-100 rounded-pill btn-primary ng-star-inserted'][2]"))
            );
            sell.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Sell Button]: Could not click Sell button. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testClickSellButton")
    public void testEnterOfferPrice() {
        System.out.println("\n========== Step 10: Enter Offer Price ==========");
        // Why might this test fail?
        // - Field might not be found or uneditable
        try {
            WebDriverWait textArea = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement price = textArea.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Enter offer price here\"]"))
            );
            price.sendKeys("12000");
        } catch (Exception e) {
            Assert.fail("FAILED [Offer Price]: Could not enter offer price. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 10, dependsOnMethods = "testEnterOfferPrice")
    public void testClickCreateOffer() {
        System.out.println("\n========== Step 11: Click Create Offer ==========");
        // Why might this test fail?
        // - Button not found or unclickable
        try {
            WebDriverWait button1 = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement offerPrice = button1.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='button' and contains(.,'Create Offer')]"))
            );
            offerPrice.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Create Offer]: Could not click Create Offer. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 11, dependsOnMethods = "testClickCreateOffer")
    public void testClickContinueButton() {
        System.out.println("\n========== Step 12: Click Final Continue ==========");
        // Why might this test fail?
        // - Button not found or not enabled/clickable
        try {
            WebDriverWait button2 = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement continueBtn = button2.until(
                ExpectedConditions.elementToBeClickable(By.xpath("(//*[@class=\"btn btn-primary w-100 rounded-pill\" and contains(text(), 'Continue')])[1]"))
            );
            continueBtn.click();
            System.out.println("Test scenario for CD Sell completed successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [Final Continue]: Could not click the final Continue. Reason: " + e.getMessage());
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
    
    	
    // Utility method to fetch OTP by mobile
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

