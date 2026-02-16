package com.example.e2e.S3;

import com.example.e2e.S3.pages.AdminRideHistoryPage;
import com.example.e2e.S3.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminRideHistoryE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = System.getProperty("baseUrl", "http://localhost:4200");
    private final String adminEmail = System.getProperty("adminEmail", "a@gmail.com");
    private final String adminPassword = System.getProperty("adminPassword", "aaaaaaaa");

    // test email can be overridden with -DtestEmail
    private final String testEmail = System.getProperty("testEmail", "p@gmail.com");

    private LoginPage loginPage;
    private AdminRideHistoryPage historyPage;

    @BeforeAll
    public void beforeAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        String headlessProp = System.getProperty("headless", "true");
        if ("true".equalsIgnoreCase(headlessProp)) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--incognito");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        loginPage = new LoginPage(driver, wait, baseUrl);
        historyPage = new AdminRideHistoryPage(driver, wait);

        // attempt login via API (faster) and fallback to UI login
        boolean apiOk = loginPage.loginViaApi(adminEmail, adminPassword);
        if (!apiOk) loginPage.login(adminEmail, adminPassword);

        driver.get(baseUrl);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- Helpers ---
    private List<Integer> baselinePrices() {
        historyPage.open(baseUrl);
        return historyPage.collectAllPricesForEmail(testEmail);
    }

    private List<LocalDate> baselineDates() {
        historyPage.open(baseUrl);
        return historyPage.collectAllDatesForEmail(testEmail);
    }

    private List<Integer> baselineDurations() {
        historyPage.open(baseUrl);
        return historyPage.collectAllDurationsForEmail(testEmail);
    }

    private List<Double> baselineDistances() {
        historyPage.open(baseUrl);
        return historyPage.collectAllDistancesForEmail(testEmail);
    }

    // --- Sorting tests: Start Date/Time ---
    @Test
    public void sortByStartDate_descending_shouldReturnItemsInDateDescOrder() {
        List<LocalDate> unsorted = baselineDates();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No rides found for " + testEmail);
        List<LocalDate> expected = unsorted.stream().sorted(java.util.Comparator.reverseOrder()).toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Start Date/Time", "Descending", null);
        List<LocalDate> actual = historyPage.collectAllDatesForCurrentFilter();
        assertEquals(expected, actual, "Results are not in descending order by start date");
    }

    @Test
    public void sortByStartDate_ascending_shouldReturnItemsInDateAscOrder() {
        List<LocalDate> unsorted = baselineDates();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No rides found for " + testEmail);
        List<LocalDate> expected = unsorted.stream().sorted(java.util.Comparator.naturalOrder()).toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Start Date/Time", "Ascending", null);
        List<LocalDate> actual = historyPage.collectAllDatesForCurrentFilter();
        assertEquals(expected, actual, "Results are not in ascending order by start date");
    }

    // --- Sorting tests: Duration ---
    @Test
    public void sortByDuration_ascending_shouldReturnItemsInDurationAscOrder() {
        List<Integer> unsorted = baselineDurations();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No durations found for " + testEmail);
        List<Integer> expected = unsorted.stream().sorted().toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Duration", "Ascending", null);
        List<Integer> actual = historyPage.collectAllDurationsForCurrentFilter();
        assertEquals(expected, actual, "Results are not in ascending order by duration");
    }

    @Test
    public void sortByDuration_descending_shouldReturnItemsInDurationDescOrder() {
        List<Integer> unsorted = baselineDurations();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No durations found for " + testEmail);
        List<Integer> expected = unsorted.stream().sorted(java.util.Comparator.reverseOrder()).toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Duration", "Descending", null);
        List<Integer> actual = historyPage.collectAllDurationsForCurrentFilter();
        assertEquals(expected, actual, "Results are not in descending order by duration");
    }

    // --- Sorting tests: Distance ---
    @Test
    public void sortByDistance_ascending_shouldReturnItemsInDistanceAscOrder() {
        List<Double> unsorted = baselineDistances();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No distances found for " + testEmail);
        List<Double> expected = unsorted.stream().sorted().toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Distance", "Ascending", null);
        List<Double> actual = historyPage.collectAllDistancesForCurrentFilter();
        assertEquals(expected, actual, "Results are not in ascending order by distance");
    }

    @Test
    public void sortByDistance_descending_shouldReturnItemsInDistanceDescOrder() {
        List<Double> unsorted = baselineDistances();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No distances found for " + testEmail);
        List<Double> expected = unsorted.stream().sorted(java.util.Comparator.reverseOrder()).toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Distance", "Descending", null);
        List<Double> actual = historyPage.collectAllDistancesForCurrentFilter();
        assertEquals(expected, actual, "Results are not in descending order by distance");
    }

    // --- Sorting tests: Price ---
    @Test
    public void sortByPrice_ascending_shouldReturnItemsInPriceAscOrder() {
        List<Integer> unsorted = baselinePrices();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No prices found for " + testEmail);
        List<Integer> expected = unsorted.stream().sorted().toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Price", "Ascending", null);
        List<Integer> actual = historyPage.collectAllPricesForCurrentFilter();
        assertEquals(expected, actual, "Results are not in ascending order by price");
    }

    @Test
    public void sortByPrice_descending_shouldReturnItemsInPriceDescOrder() {
        List<Integer> unsorted = baselinePrices();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No prices found for " + testEmail);
        List<Integer> expected = unsorted.stream().sorted(java.util.Comparator.reverseOrder()).toList();

        historyPage.searchWithAll(testEmail, "Passenger", "Price", "Descending", null);
        List<Integer> actual = historyPage.collectAllPricesForCurrentFilter();
        assertEquals(expected, actual, "Results are not in descending order by price");
    }

    // --- Date filters ---
    @Test
    public void filterByDate_withRidesOn_14_02_shouldReturnOnlyThoseRides() {
        int year = LocalDate.now().getYear();
        String dateStr = String.format("14.02.%d", year);

        List<LocalDate> unsorted = baselineDates();
        assumeTrue(unsorted != null && !unsorted.isEmpty(), "No rides found for " + testEmail);
        List<LocalDate> expected = unsorted.stream().filter(d -> d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).equals(dateStr)).toList();

        historyPage.searchWithAll(testEmail, "Passenger", null, null, dateStr);
        List<LocalDate> actual = historyPage.collectAllDatesForCurrentFilter();
        if (actual.size() != expected.size()) {
            System.out.println("DEBUG filterByDate_withRidesOn_14_02: expected=" + expected.size() + " actual=" + actual.size());
            historyPage.dumpDebug("filter-14-02-mismatch");
        }
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void filterByDate_withNoRidesOn_11_02_shouldReturnEmptyList() {
        int year = LocalDate.now().getYear();
        String dateStr = String.format("11.02.%d", year);

        historyPage.searchWithAll(testEmail, "Passenger", null, null, dateStr);
        List<LocalDate> actual = historyPage.collectAllDatesForCurrentFilter();
        assertTrue(actual.isEmpty());
    }

    // --- Non-existing user ---
    @Test
    public void searchForNonExistingEmail_shouldReturnZeroRides() {
        historyPage.open(baseUrl);
        String nonExisting = "nonexisting@gmail.com";
        historyPage.searchWithAll(nonExisting, "Passenger", null, null, null);

        List<WebElement> items = historyPage.getResultItems();
        boolean pageSaysNo = driver.getPageSource().contains("No rides found for this user.") || driver.getPageSource().contains("Enter email and search to view rides.");
        assertTrue(items.isEmpty() || pageSaysNo);
    }
}
