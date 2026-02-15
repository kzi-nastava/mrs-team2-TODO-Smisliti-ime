package com.example.e2e.rating;

import com.example.e2e.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RateRideE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private LoginPage loginPage;

    private final String baseUrl = "http://localhost:4200";
    private final String passengerEmail = "p@gmail.com";
    private final String passengerPassword = "pppppppp";

    @BeforeAll
    void setupAll() {
        WebDriverManager.chromedriver().setup();
//        WebDriverManager.chromedriver().browserVersion("144.0.7559.133").setup();
    }

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String baseUrl = "http://localhost:4200";
        loginPage = new LoginPage(driver, wait, baseUrl);

        boolean loggedIn = loginPage.loginViaApi(
                "p@gmail.com",
                "pppppppp"
        );

        if (!loggedIn) {
            throw new RuntimeException("Login via API failed!");
        }
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void testRateRideHappyPath() {

        PassengerRideHistoryPage historyPage =
                new PassengerRideHistoryPage(driver, baseUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        assertTrue(historyPage.hasRides());

        historyPage.openFirstRide();

        PassengerRideDetailsPage detailsPage =
                new PassengerRideDetailsPage(driver);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.cssSelector(".rate-button")));

        assertTrue(detailsPage.isRateButtonVisible());

        detailsPage.clickRateRide();

        wait.until(ExpectedConditions.urlContains("/rate"));

        RatePage ratePage = new RatePage(driver);

        String comment = "E2E test comment";

        ratePage.selectVehicleRating(5);
        ratePage.selectDriverRating(5);
        ratePage.enterComment(comment);

        ratePage.submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.cssSelector(".comment-text")));

        assertTrue(ratePage.isCommentPresent(comment));
    }

    @Test
    void testSubmitWithoutRatings() {

        PassengerRideHistoryPage historyPage =
                new PassengerRideHistoryPage(driver, baseUrl);

        historyPage.openFirstRide();

        PassengerRideDetailsPage detailsPage =
                new PassengerRideDetailsPage(driver);

        if (!detailsPage.isRateButtonVisible()) {
            return; // no rides to rate, skip test
        }

        detailsPage.clickRateRide();

        wait.until(ExpectedConditions.urlContains("/rate"));

        RatePage ratePage = new RatePage(driver);

        ratePage.submit();

        // should stay on /rate page with validation errors, not navigate away
        wait.until(d -> d.getCurrentUrl().contains("/rate"));
        assertTrue(driver.getCurrentUrl().contains("/rate"));

        assertTrue(ratePage.waitForSnackBarWithText("Please fill all fields", 5));
    }

    @Test
    void testSubmitOnlyCommentWithoutRatings() {
        PassengerRideHistoryPage historyPage = new PassengerRideHistoryPage(driver, baseUrl);
        historyPage.openFirstRide();
        PassengerRideDetailsPage detailsPage = new PassengerRideDetailsPage(driver);

        if (!detailsPage.isRateButtonVisible()) return;

        detailsPage.clickRateRide();
        wait.until(ExpectedConditions.urlContains("/rate"));

        RatePage ratePage = new RatePage(driver);

        // just comment, no ratings
        ratePage.enterComment("Just a comment");

        ratePage.submit();

        // treba da ostane na /rate stranici
        wait.until(d -> d.getCurrentUrl().contains("/rate"));
        assertTrue(driver.getCurrentUrl().contains("/rate"));

        // debug dump if snackbar not found
        boolean found = ratePage.waitForSnackBarWithText("Please fill all fields", 5);
        if (!found) {
            try {
                Object bodyText = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.body.innerText || document.body.textContent;");
                System.out.println("DEBUG: body text after submit:\n" + String.valueOf(bodyText).substring(0, Math.min(2000, String.valueOf(bodyText).length())));
            } catch (Exception ignored) {
            }
        }

        assertTrue(found);
    }

    @Test
    void testSubmitOnlyVehicleRatingWithoutCommentOrDriverRating() {
        PassengerRideHistoryPage historyPage = new PassengerRideHistoryPage(driver, baseUrl);
        historyPage.openFirstRide();
        PassengerRideDetailsPage detailsPage = new PassengerRideDetailsPage(driver);

        if (!detailsPage.isRateButtonVisible()) return;

        detailsPage.clickRateRide();
        wait.until(ExpectedConditions.urlContains("/rate"));

        RatePage ratePage = new RatePage(driver);

        // just vehicle rating, no driver rating or comment
        ratePage.selectVehicleRating(4);

        ratePage.submit();

        wait.until(d -> d.getCurrentUrl().contains("/rate"));
        assertTrue(driver.getCurrentUrl().contains("/rate"));

        assertTrue(ratePage.waitForSnackBarWithText("Please fill all fields", 5));
    }

    @Test
    void testSubmitOnlyDriverRatingWithoutCommentOrVehicleRating() {
        PassengerRideHistoryPage historyPage = new PassengerRideHistoryPage(driver, baseUrl);
        historyPage.openFirstRide();
        PassengerRideDetailsPage detailsPage = new PassengerRideDetailsPage(driver);

        if (!detailsPage.isRateButtonVisible()) return;

        detailsPage.clickRateRide();
        wait.until(ExpectedConditions.urlContains("/rate"));

        RatePage ratePage = new RatePage(driver);

        // samo ocena za vozača, bez komentara i ocene vozila
        ratePage.selectDriverRating(3);
        ratePage.submit();

        assertTrue(ratePage.waitForSnackBarWithText("Please fill all fields", 5));

    }


    @Test
    void testCannotRateRideTwice() {
        PassengerRideHistoryPage historyPage = new PassengerRideHistoryPage(driver, baseUrl);
        historyPage.openFirstRide();
        PassengerRideDetailsPage detailsPage = new PassengerRideDetailsPage(driver);

        if (!detailsPage.isRateButtonVisible()) return;

        detailsPage.clickRateRide();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/rate"));

        RatePage ratePage = new RatePage(driver);
        ratePage.selectVehicleRating(5);
        ratePage.selectDriverRating(5);
        ratePage.enterComment("First rating attempt");
        ratePage.submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".mat-snack-bar-container")));

        historyPage.openFirstRide();
        detailsPage = new PassengerRideDetailsPage(driver);

        detailsPage.clickRateRide();
        wait.until(ExpectedConditions.urlContains("/rate"));

        ratePage = new RatePage(driver);
        ratePage.selectVehicleRating(4);
        ratePage.selectDriverRating(4);
        ratePage.enterComment("Second rating attempt");
        ratePage.submit();

        boolean errorDisplayed = ratePage.waitForSnackBarWithText("Ride already rated", 5);

        assertTrue(errorDisplayed);
    }

}
