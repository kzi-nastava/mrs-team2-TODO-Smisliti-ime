package com.example.e2e.rating;

import com.example.e2e.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RateRideE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private LoginPage loginPage;
    private PassengerRideHistoryPage historyPage;
    private RatePage ratePage;

    private final String baseUrl = System.getProperty("baseUrl", "http://localhost:4200");
    private final String testEmail = System.getProperty("testEmail", "p@gmail.com");
    private final String testPassword = System.getProperty("testPassword", "pppppppp");
    private final String testRideId = System.getProperty("testRideId", "1");

    @BeforeAll
    public void beforeAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) options.addArguments("--headless=new");
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));

        loginPage = new LoginPage(driver, wait, baseUrl);
        historyPage = new PassengerRideHistoryPage(driver, wait, baseUrl);

        boolean ok = loginPage.loginViaApi(testEmail, testPassword);
        if (!ok) loginPage.login(testEmail, testPassword);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void happyPath_rateRide() {
        int index = 0; // pick first ride
        ratePage = historyPage.openRideAndGoToRate(index);
        String comment = "Great ride from E2E test" + System.currentTimeMillis();
        ratePage.setVehicleRating(5);
        ratePage.setDriverRating(5);
        ratePage.setComment(comment);
        ratePage.submit();

        String success = ratePage.getSuccessMessage();
        assertNotNull(success, "Expected a success message after rating");
        // also verify comment is visible in comments list
        assertTrue(ratePage.hasComment(comment), "Submitted comment should be visible in the comments list");
    }

    @Test
    public void invalidRating_shouldPreventSubmit() {
        int index = 0;
        ratePage = historyPage.openRideAndGoToRate(index);
        // do not set star ratings
        ratePage.setComment("No stars");
        // submit should be disabled or error appears
        boolean enabled = ratePage.isSubmitEnabled();
        if (enabled) {
            ratePage.submit();
            assertNotNull(ratePage.getErrorMessage());
        } else {
            assertFalse(enabled);
        }
    }
}

