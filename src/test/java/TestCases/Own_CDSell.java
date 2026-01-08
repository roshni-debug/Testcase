package TestCases;

import java.sql.*;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Own_CDSell {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    private static final String mobileNumber = "9999999990";
    private static String otp;

    // ===================== SETUP =====================
    @BeforeClass
    public void setup() {

        System.out.println("========== Seller Own CD Sell Test Started ==========");

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        actions = new Actions(driver);
    }

    // ===================== TESTS =====================

    @Test(priority = 1)
    public void openLoginPage() {
        driver.get("https://digielv.mmcm.in/");
        Assert.assertNotNull(driver.getTitle(), "Login page not loaded");
    }

    @Test(priority = 2)
    public void clickLoginRegister() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='navbarNav']/ul/li[5]/a/button"))).click();
    }

    @Test(priority = 3)
    public void enterMobileAndLogin() {
        driver.findElement(By.xpath("//input[@placeholder='Enter Your Mobile Number']"))
                .sendKeys(mobileNumber);
        driver.findElement(By.xpath("//button[normalize-space()='Login']")).click();
    }

    @Test(priority = 4)
    public void fetchOtpFromDB() {

        otp = fetchOtp(mobileNumber);
        Assert.assertNotNull(otp, "OTP not fetched from DB");
        Assert.assertEquals(otp.length(), 6, "OTP length invalid");

        System.out.println("OTP fetched: " + otp);
    }

    @Test(priority = 5)
    public void enterOtp() {

        List<WebElement> otpInputs = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.xpath("//p-inputotp//input[contains(@class,'p-inputotp-input')]")));

        for (int i = 0; i < 6; i++) {
            otpInputs.get(i).sendKeys(String.valueOf(otp.charAt(i)));
        }
    }

    @Test(priority = 6)
    public void dismissKycPopupIfPresent() {
        try {
            WebElement skip = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[normalize-space()='Skip For Now']")));
            skip.click();
        } catch (TimeoutException e) {
            System.out.println("No KYC popup displayed");
        }
    }

    @Test(priority = 7)
    public void navigateToListOfCDs() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(normalize-space(),'List of CDs')]"))).click();
    }

    @Test(priority = 8)
    public void clickSellButton() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Sell')]"))).click();
    }

    @Test(priority = 9)
    public void enterOfferPrice() {
        WebElement price = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@placeholder='Enter offer price here']")));
        price.sendKeys("12000");
    }

    @Test(priority = 10)
    public void clickCreateOffer() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Create Offer')]"))).click();
    }

    @Test(priority = 11)
    public void clickFinalContinue() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Continue')]"))).click();
    }

    // ===================== TEARDOWN =====================
    @AfterClass(alwaysRun = true)
    public void teardown() {

        resetIsLoggedIn(mobileNumber);

        if (driver != null) {
            driver.quit();
        }

        System.out.println("========== Seller CD Sell Test Finished ==========");
    }

    // ===================== DB UTILITIES =====================

    private static String fetchOtp(String mobile) {

        String otp = null;

        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:5432/mmcmuat";
        String user = System.getenv("uatuser");
        String pass = System.getenv("password@123");

        String query = "SELECT otp FROM common.user_mstr WHERE mobile_no = ?";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, mobile);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                otp = rs.getString("otp");
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return otp;
    }

    private static void resetIsLoggedIn(String mobile) {

        String url = "jdbc:postgresql://elv-hyd-uat-cluster.cluster-ro-cxua0wsmu5p7.ap-south-1.rds.amazonaws.com:5432/mmcmuat";
        String user = System.getenv("uatuser");
        String pass = System.getenv("password@123");

        String update = "UPDATE common.user_mstr SET is_logged_in = 0 WHERE mobile_no = ?";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(update)) {

            ps.setString(1, mobile);
            ps.executeUpdate();
            System.out.println("is_logged_in reset for " + mobile);

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
