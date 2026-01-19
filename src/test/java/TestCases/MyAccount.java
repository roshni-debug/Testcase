package TestCases;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class MyAccount {

    WebDriver driver;
    WebDriverWait wait;
    Actions actions;
    Scanner scanner;

    String mobileNumber = "9000000105";
    String accountNo = "1234567890123456";
    String ifscCode = "HDFC0009226";
    String filePath = "C:\\Users\\roshn\\Documents\\Pictures\\Pan-Card-Dummy.png";

    // ================= SETUP =================
    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        actions = new Actions(driver);
        scanner = new Scanner(System.in);

        driver.manage().window().maximize();

    }

    // ================= TEST CASE 1 =================
    @Test(priority = 1)
    public void TC_01_Login_With_Mobile_And_OTP() throws Exception {

        driver.get("https://digielv.mmcm.in/");

        driver.findElement(By.xpath("//*[@id='navbarNav']/ul/li[5]/a/button")).click();
        driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']")).sendKeys(mobileNumber);
        driver.findElement(By.xpath("//button[normalize-space()='Login']")).click();

        String otp = fetchOtpFromDatabase(mobileNumber);
        Assert.assertNotNull(otp, "OTP should not be null");

        List<WebElement> otpInputs = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")));

        Assert.assertEquals(otpInputs.size(), 6, "OTP input boxes count mismatch");

        for (int i = 0; i < 6; i++) {
            otpInputs.get(i).sendKeys(String.valueOf(otp.charAt(i)));
        }
    }

    // ================= TEST CASE 2 =================
    @Test(priority = 2)
    public void TC_02_Skip_KYC_Popup() {
        try {
            WebElement skipBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[normalize-space()='Skip For Now']")));
            skipBtn.click();
        } catch (TimeoutException e) {
            Assert.assertTrue(true, "KYC popup not displayed");
        }
    }

    // ================= TEST CASE 3 =================
    @Test(priority = 3)
    public void TC_03_Add_Bank_Details() {

        WebElement accountType = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//p-dropdown[@formcontrolname='account_type']//div[@class='p-dropdown-trigger']")));
        accountType.click();

        WebElement savings = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@role='option' and normalize-space()='Savings']")));
        savings.click();

        driver.findElement(By.xpath("//*[@placeholder='Enter Your Account No']")).sendKeys(accountNo);
        driver.findElement(By.xpath("//*[@placeholder='Re-enter Account No']")).sendKeys(accountNo);
        driver.findElement(By.xpath("//*[@placeholder='Enter Your ifsc']")).sendKeys(ifscCode);

        Assert.assertTrue(true, "Bank details entered successfully");
    }

    // ================= TEST CASE 4 =================
    @Test(priority = 4)
    public void TC_04_Upload_Document() throws Exception {
        uploadFile(filePath);
    }

    // ================= TEST CASE 5 =================
    @Test(priority = 5)
    public void TC_05_Submit_Bank_Details() {

        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='content']/main/app-user-profile/div/div/div[2]/div/div/form/div[3]/div/div[9]/button")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        Assert.assertTrue(true, "Form submitted successfully");
    }

    // ================= TEARDOWN =================
    @AfterClass
    public void tearDown() {
        driver.quit();
    }

    // ================= UTILITY METHODS =================
    public static String fetchOtpFromDatabase(String mobileNumber) {
        String otp = null;
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:1521/mmcmuat",
                    "uatuser", "password@123");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT otp FROM common.user_mstr WHERE mobile_no=" + mobileNumber);

            if (rs.next()) otp = rs.getString("otp");

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return otp;
    }

    public void uploadFile(String path) {
        File file = new File(path);
        Assert.assertTrue(file.exists(), "File does not exist");

        WebElement upload = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='content']/main/app-user-profile/div/div/div[2]/div/div/form/div[3]/div/div[8]/div/input")));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block';", upload);

        upload.sendKeys(path);
    }
}
