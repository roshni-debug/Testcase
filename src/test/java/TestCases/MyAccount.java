/*
=========== TEST CASE STEPS: MY ACCOUNT REGISTRATION WITH BANK DETAILS ===========
    1. Launch Chrome browser and open DigiELV application.
    2. Enter valid 10-digit mobile number and click Login.
    3. Fetch OTP from database and enter into OTP input boxes.
    4. Handle optional KYC cancellation popup (if present).
    5. Select Savings as Account Type.
    6. Enter Account Number (from console) and Retype Account Number (same value).
    7. Enter IFSC Code.
    8. Upload bank proof file.
    9. Submit the details.
==================================================================================
*/

package TestCases;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class MyAccount {

    private WebDriver driver;
    private WebDriverWait wait;
    private static String mobileNumber = "8969804960";
    private static String otp;
    private static String accountNumber = "234590234590234590";
    private Scanner scanner;
    

    @BeforeClass
    public void setup() {
        System.out.println("************ TEST: My Account - Register Bank Details ************");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            Assert.fail("FAILED [SETUP]: Could not initialize WebDriver: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testOpenDigielvAndLogin() {
        System.out.println("\n========== Step 2: Open DigiELV application and Login ==========");
        driver.get("https://digielv.mmcm.in/");
        try {
            driver.findElement(By.xpath("//*[@id=\"navbarNav\"]/ul/li[5]/a/button")).click();
            driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
            driver.findElement(By.xpath("//button[normalize-space(text())='Login']")).click();
        } catch (Exception e) {
            Assert.fail("FAILED [Login Page UI]: " + e.getMessage());
        }
    }

    @Test(priority = 2, dependsOnMethods = "testOpenDigielvAndLogin")
    public void testFetchAndEnterOtp() throws InterruptedException {
        System.out.println("\n========== Step 3: Fetch OTP from DB and Enter OTP ==========");
        otp = fetchOtpFromDatabase(mobileNumber);
        Assert.assertNotNull(otp, "FAILED [OTP Fetch]: OTP is null from DB.");
        Assert.assertEquals(otp.length(), 6, "FAILED [OTP Fetch]: OTP must be 6 digits. OTP=" + otp);

        try {
            List<WebElement> otpInputs = new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                            By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")
                    ));
            Assert.assertEquals(otpInputs.size(), 6, "FAILED [OTP Boxes]: Should be 6.");
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
            Assert.fail("FAILED [OTP Entry]: " + e.getMessage());
        }
    }

    @Test(priority = 3, dependsOnMethods = "testFetchAndEnterOtp")
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
    
    @Test(priority = 4, dependsOnMethods = "testHandleKycPopupIfPresent")
    public void testSelectAccountTypeSavings() throws InterruptedException {
        System.out.println("\n========== Step 5: Select Account Type - Savings ==========");
        try {
            WebElement accountTypeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p-dropdown[@formcontrolname='account_type']//div[@class='p-dropdown-trigger']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountTypeDropdown);
            Thread.sleep(500);
            accountTypeDropdown.click();
            System.out.println("Clicked on Account Type dropdown");
            WebElement savingsOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[@role='option' and normalize-space()='Savings']")));
            savingsOption.click();
            System.out.println("Selected 'Savings' account type");
        } catch (Exception e) {
            Assert.fail("FAILED [Account Type Dropdown]: " + e.getMessage());
        }
    }

    @Test(priority = 5, dependsOnMethods = "testSelectAccountTypeSavings")
    public void testEnterAccountNumbers() {
        try {
            WebElement AccountNo = new WebDriverWait(driver, Duration.ofSeconds(90)).until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder='Enter Your Account No']")));
            AccountNo.clear();
            AccountNo.sendKeys(accountNumber);

            WebElement ReAccountNo = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder='Re-enter Account No']")));
            ReAccountNo.clear();
            ReAccountNo.sendKeys(accountNumber);

            System.out.println("Entered account number and re-typed successfully from console.");
        } catch (Exception e) {
            Assert.fail("FAILED [Account Number Input]: " + e.getMessage());
        }
    }

    @Test(priority = 6, dependsOnMethods = "testEnterAccountNumbers")
    public void testEnterIfscCode() {
        System.out.println("\n========== Step 7: Enter IFSC Code ==========");
        String ifscCode = "HDFC0009226";
        try {
            WebElement IFSCcode = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder=\"Enter Your ifsc\"]")));
            IFSCcode.clear();
            IFSCcode.sendKeys(ifscCode);
            System.out.println("Entered IFSC Code: " + ifscCode);
        } catch (Exception e) {
            Assert.fail("FAILED [IFSC Code]: " + e.getMessage());
        }
    }

    @Test(priority = 7, dependsOnMethods = "testEnterIfscCode")
    public void testFileUpload() {
        System.out.println("\n========== Step 8: Upload File ==========");
        String filePath = "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png";
        try {
            uploadFile(driver, "formFile0", filePath);
        } catch (Exception e) {
            Assert.fail("File Upload Failed: " + e.getMessage());
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

    // Ask and validate mobile number from console
    public static String getMobileNumberFromConsole(Scanner scanner) {
        String mobileNumber;
        while (true) {
            System.out.print("Please enter your 10-digit mobile number: ");
            mobileNumber = scanner.nextLine().trim();
            if (mobileNumber.length() == 10 && mobileNumber.matches("[0-9]+")) {
                break;
            } else {
                System.out.println("Invalid mobile number. Please enter exactly 10 digits.");
            }
        }
        return mobileNumber;
    }

    // Ask account number from console (validate for min 10 digits etc.)
    public static String getAccountNumberFromConsole(Scanner scanner) {
        String accountNumber;
        while (true) {
            System.out.print("Enter your Account Number: ");
            accountNumber = scanner.nextLine().trim();
            if (accountNumber.length() >= 10 && accountNumber.matches("[0-9]+")) {
                break;
            } else {
                System.out.println("Invalid Account Number. Please enter a valid number (at least 10 digits).");
            }
        }
        return accountNumber;
    }

    // Fetch OTP by mobile number from DB
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
                System.out.println("No OTP found for mobile number: " + mobileNumber);
            }
            rs1.close();
            stmt1.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error fetching OTP: " + e.getMessage());
        }
        return otp;
    }

    // File upload utility
    public static void uploadFile(WebDriver driver, String inputId, String filePath) throws Exception {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id=\"content\"]/main/app-user-profile/div/div/div[2]/div/div/form/div[3]/div/div[8]/div/input")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].classList.remove('d-none'); arguments[0].style.display='block'; arguments[0].style.visibility='visible';",
                fileInput
        );
        fileInput.sendKeys(filePath);
        System.out.println("File uploaded: " + filePath);
    }
}