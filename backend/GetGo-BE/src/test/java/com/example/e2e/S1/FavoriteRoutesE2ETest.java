package com.example.e2e.S1;

import com.example.e2e.S1.pages.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FavoriteRoutesE2ETest {

    private static WebDriver driver;
    private static final String PASSENGER_EMAIL = "p@gmail.com";
    private static final String PASSENGER_PASSWORD = "pppppppp";

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false
        ));

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        cleanDatabase();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private static void cleanDatabase() {
        try {
            String url = System.getProperty("spring.datasource.url");
            String username = System.getProperty("spring.datasource.username");
            String password = System.getProperty("spring.datasource.password");

            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM favorite_waypoints");
            stmt.execute("DELETE FROM favorite_ride_passengers");
            stmt.execute("DELETE FROM favorite_rides");
            stmt.execute("DELETE FROM active_ride_passengers");
            stmt.execute("DELETE FROM active_rides");
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("DB cleanup failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Favorite route: add, order from favorite, remove")
    void shouldFavoriteRouteOrderAndUnfavorite() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(PASSENGER_EMAIL, PASSENGER_PASSWORD);

        NavBar navBar = new NavBar(driver);
        navBar.goToRideHistory();

        PassengerRideHistoryPage historyPage = new PassengerRideHistoryPage(driver);
        historyPage.clickFirstRide();

        PassengerRideDetailsPage detailsPage = new PassengerRideDetailsPage(driver);
        detailsPage.clickFavorite();

        navBar.goToHome();

        PassengerHomePage homePage = new PassengerHomePage(driver);
        assertTrue(homePage.getFavoritesButtonText().contains("(1)"));

        homePage.openFavorites();
        homePage.selectFirstFavorite();
        assertTrue(homePage.areDestinationsFilled());

        homePage.clickOrderRide();
        assertTrue(homePage.isSuccessMessageDisplayed());

        navBar.goToRideHistory();
        historyPage = new PassengerRideHistoryPage(driver);
        historyPage.clickFirstRide();

        detailsPage = new PassengerRideDetailsPage(driver);
        detailsPage.clickUnfavorite();

        navBar.goToHome();
        homePage = new PassengerHomePage(driver);
        assertTrue(homePage.isFavoritesEmpty());
    }
}