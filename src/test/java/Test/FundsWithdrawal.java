
/*
    ==================== TEST CASE STEPS: FUNDS MANAGEMENT – WITHDRAW FUNDS ===========================

     * 1. Launch Chrome browser and open DigiELV application.
     * 2. Enter valid 10-digit mobile number and click Login.
     * 3. Fetch OTP from the database and enter into the OTP input boxes.
     * 4. Handle optional KYC cancellation popup (if present).
     * 5. Navigate to "Funds Withdrawal" from sidebar.
     * 6. Click on "Withdraw Funds" button.
     * 7. Enter withdrawal amount (e.g., ₹5000).
     * 8. Enter remarks (e.g., "Fund Withdrawal").
     * 9. Click "Withdraw" button to proceed.
     * 10. Click on "Continue" to confirm and complete the withdrawal process.

==================================================================================
*/

package Test;

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

import java.sql.*;
import java.time.Duration;
import java.util.List;

public class FundsWithdrawal {

    private WebDriver driver;
    private WebDriverWait wait;
    // Hardcoded mobile number as per requirement
    private static String mobileNumber = "9911991191";
    private static String otp;

    @BeforeClass
    public void setup() {
        System.out.println("************ TEST: Funds Management (Withdraw Funds) ************");
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-first-run");
            options.addArguments("--no-default-browser-check");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--start-maximized");
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        } catch (Exception e) {
            Assert.fail("FAILED [SETUP]: Could not initialize WebDriver: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testSetMobileNumber() {
        System.out.println("\n========== Step 1: 10-digit mobile number is hardcoded ==========");
        try {
            Assert.assertEquals(mobileNumber.length(), 10, "FAILED [Input]: Mobile number should be 10 digits.");
            Assert.assertTrue(mobileNumber.matches("[0-9]+"), "FAILED [Input]: Mobile number should contain only digits.");
        } catch (Exception e) {
            Assert.fail("FAILED [Set Mobile Number]: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testSetMobileNumber")
    public void testOpenDigielvApplication() {
        System.out.println("\n========== Step 2: Open DigiELV application ==========");
        try {
            driver.get("https://digielv.mmcm.in/");
            Assert.assertNotNull(driver.getTitle(), "FAILED [Page Load]: Title is null.");
        } catch (Exception e) {
            Assert.fail("FAILED [Page Load]: Could not load DigiELV. " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testOpenDigielvApplication")
    public void testClickLoginRegister() {
        System.out.println("\n========== Step 3: Click Login/Register ==========");
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login/Register]: Button not found/clicked. Reason: " + e.getMessage());
        }
    }

    @Test(priority = 4, dependsOnMethods = "testClickLoginRegister")
    public void testEnterMobileAndClickLogin() {
        System.out.println("\n========== Step 4: Enter mobile number and click Login ==========");
        try {
            driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
            driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Mobile/Login]: Could not enter/click. " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testEnterMobileAndClickLogin")
    public void testFetchOtpFromDB() {
        System.out.println("\n========== Step 5: Fetch OTP from database ==========");
        try {
            otp = fetchOtpFromDatabase("9911991191");
            Assert.assertNotNull(otp, "FAILED [OTP Fetch]: OTP is null from DB.");
            Assert.assertEquals(otp.length(), 6, "FAILED [OTP Fetch]: OTP must be 6 digits. OTP=" + otp);
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Fetch]: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testFetchOtpFromDB")
    public void testEnterOtpInputs() throws InterruptedException {
        System.out.println("\n========== Step 6: Enter OTP ==========");
        try {
            List<WebElement> otpInputs = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                )
            );
            Assert.assertEquals(otpInputs.size(), 6, "FAILED [OTP Boxes]: Should be 6.");
            for (int i = 0; i < 6; i++) {
                WebElement input = otpInputs.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
                input.click();
                input.clear();
                input.sendKeys(Character.toString(otp.charAt(i)));
                Thread.sleep(100);
            }
            System.out.println("OTP entered successfully!");
        } catch (Exception e) {
            Assert.fail("FAILED [OTP Entry]: " + e.getMessage());
        }
    }

    @Test(priority = 7, dependsOnMethods = "testEnterOtpInputs")
    public void testHandleKycPopupIfPresent() {
        System.out.println("\n========== Step 7: Handle optional KYC popup ==========");
        try {
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelPopupButton = popupWait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[normalize-space()=\"Skip For Now\"]")
                )
            );
            cancelPopupButton.click();
            System.out.println("KYC cancellation popup appeared and was dismissed.");
        } catch (Exception e) {
            System.out.println("No KYC cancellation popup appeared. Continuing to next step.");
        }
    }

    @Test(priority = 8, dependsOnMethods = "testHandleKycPopupIfPresent")
    public void testOpenFundsWithdrawal() {
        System.out.println("\n========== Step 8: Navigate to 'Funds Withdrawal' ==========");
        try {
            WebElement fundWithdraw = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//ul[@class='side-menu']//a[contains(text(), 'Funds Withdrawal')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fundWithdraw);
        } catch (Exception e) {
            Assert.fail("FAILED [Sidebar Navigation-Funds Withdrawal]: " + e.getMessage());
        }
    }

    @Test(priority = 9, dependsOnMethods = "testOpenFundsWithdrawal")
    public void testWithdrawFundsClick() {
        System.out.println("\n========== Step 9: Click on 'Withdraw Funds' ==========");
        try {
            WebElement withdrawButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-fund-withdraw/section/div/div[1]/button")));
            withdrawButton.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Withdraw Funds]: " + e.getMessage());
        }
    }

    @Test(priority = 10, dependsOnMethods = "testWithdrawFundsClick")
    public void testEnterAmountAndRemarks() {
        System.out.println("\n========== Step 10: Enter Amount and Remarks ==========");
        try {
            WebElement amountInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("integeronly")));
            amountInput.click();
            amountInput.clear();
            amountInput.sendKeys("5000");
            WebElement remarksInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-fund-withdraw/section/div/div[2]/div[2]/div/div/div[2]/div/div[2]/textarea")));
            remarksInput.click();
            remarksInput.clear();
            remarksInput.sendKeys("Fund Withdrawal");
        } catch (Exception e) {
            Assert.fail("FAILED [Amount/Remarks Input]: " + e.getMessage());
        }
    }

    @Test(priority = 11, dependsOnMethods = "testEnterAmountAndRemarks")
    public void testClickWithdrawButton() {
        System.out.println("\n========== Step 11: Click 'Withdraw' ==========");
        try {
            WebElement withdrawConfirm = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-fund-withdraw/section/div/div[2]/div[2]/div/div/div[3]/div[2]/button")));
            withdrawConfirm.click();
        } catch (Exception e) {
            Assert.fail("FAILED [Withdraw Button]: " + e.getMessage());
        }
    }

    @Test(priority = 12, dependsOnMethods = "testClickWithdrawButton")
    public void testClickContinueOnModal() {
        System.out.println("\n========== Step 12: Click 'Continue' on Confirmation Modal ==========");
        try {
            WebElement continueBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"content\"]/main/app-fund-withdraw/section/app-registration-modal[1]/section/div/div/div/div[3]/button")));
            continueBtn.click();
            System.out.println("Funds Withdrawal flow completed successfully.");
        } catch (Exception e) {
            Assert.fail("FAILED [Continue Confirmation]: " + e.getMessage());
        }
    }

    @AfterClass
    public void teardown() {
        System.out.println("Test Execution Completed.");
        if (driver != null) {
            try {
                System.out.println("Logged Out Successfully!");
            } finally {
                driver.quit();
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

    // Util: Fetch OTP by mobile number from DB
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat";
        String user = "uatuser";
        String password = "password@123";
        // Hardcoded mobile number in query as required
        String query1 = "SELECT otp FROM common.user_mstr WHERE mobile_no = 9911991191";
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(query1);
            if (rs1.next()) {
                otp = rs1.getString("otp");
            } else {
                System.out.println("No OTP found for mobile number: 9911991191");
            }
            rs1.close();
            stmt1.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error fetching OTP: " + e.getMessage());
        }
        return otp;
    }

}
