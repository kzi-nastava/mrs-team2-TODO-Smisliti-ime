package com.example.e2e;

import com.example.e2e.pages.AdminRideHistoryPage;
import com.example.e2e.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
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
    private final String adminToken = System.getProperty("adminToken", "<PUT_VALID_ADMIN_JWT_HERE>");
    private final String adminEmail = System.getProperty("adminEmail", "a@gmail.com");
    private final String adminPassword = System.getProperty("adminPassword", "aaaaaaaa");

    // test email can be overridden with -DtestEmail
    private final String testEmail = System.getProperty("testEmail", "o@gmail.com");

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

        if (adminToken == null || adminToken.contains("<PUT_VALID_ADMIN_JWT_HERE>")) {
            // Try API login first (faster, avoids UI modals). If it fails, fallback to UI login.
            boolean apiOk = false;
            try {
                apiOk = loginPage.loginViaApi(adminEmail, adminPassword);
            } catch (Throwable t) {
                System.out.println("DEBUG loginViaApi threw: " + t.getMessage());
                apiOk = false;
            }
            if (!apiOk) {
                loginPage.login(adminEmail, adminPassword);
            }
        } else {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.localStorage.setItem('authToken', arguments[0]);", adminToken);
            driver.get(baseUrl);
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testSortAndFilterBehaviors() {
        historyPage.open(baseUrl);

        // collect full unsorted baseline across all pages for the specific email
        List<Integer> unsortedPrices = historyPage.collectAllPricesForEmail(testEmail);
        List<java.time.LocalDate> unsortedDates = historyPage.collectAllDatesForEmail(testEmail);

        // DEBUG: print collected baseline info to help troubleshooting
        System.out.println("DEBUG: collected unsortedPrices.size() = " + (unsortedPrices == null ? 0 : unsortedPrices.size()));
        if (unsortedPrices != null && !unsortedPrices.isEmpty()) {
            System.out.println("DEBUG: sample prices: " + unsortedPrices.subList(0, Math.min(5, unsortedPrices.size())));
        }
        // DEBUG: check how many .card-price elements Selenium sees and print item texts
        int priceElCount = historyPage.countPriceElements();
        System.out.println("DEBUG: countPriceElements() = " + priceElCount);
        List<String> itemTexts = historyPage.getItemTexts();
        System.out.println("DEBUG: first 2 item texts: " + (itemTexts.isEmpty() ? itemTexts : itemTexts.subList(0, Math.min(2, itemTexts.size()))));
        System.out.println("DEBUG: collected unsortedDates.size() = " + (unsortedDates == null ? 0 : unsortedDates.size()));
        if (unsortedDates != null && !unsortedDates.isEmpty()) {
            System.out.println("DEBUG: sample dates: " + unsortedDates.subList(0, Math.min(5, unsortedDates.size())));
        }

        // Require at least dates baseline; if prices are missing we'll skip price-specific assertions
        assumeTrue(unsortedDates != null && !unsortedDates.isEmpty(), "No rides found for " + testEmail + " — ensure frontend is running and test data exists");

        // expected sorted by date desc
        List<java.time.LocalDate> expectedByDateDesc = unsortedDates.stream().sorted(java.util.Comparator.reverseOrder()).toList();

        // apply sort in UI and verify
        historyPage.searchWithAll(testEmail, "Passenger", "Start Date/Time", "Descending", null);
        // collect actual after applying sort across pages (preserve current filter state)
        List<java.time.LocalDate> actualDates = historyPage.collectAllDatesForCurrentFilter();
        assertEquals(expectedByDateDesc.size(), actualDates.size(), "Date count after applying date sort must match baseline");
        for (int i = 0; i < actualDates.size(); i++) {
            assertEquals(expectedByDateDesc.get(i), actualDates.get(i), "Mismatch at index " + i);
        }

        // price ascending - only run if backend has prices
        if (unsortedPrices != null && !unsortedPrices.isEmpty()) {
            // Fetch expected sorted list from backend JSON directly (more reliable)
            String backendPricesJson = historyPage.fetchAllPricesFromBackend(testEmail, "estimatedPrice", "ASC", null);
            List<Integer> expectedByPriceAsc;
            if (backendPricesJson != null) {
                try {
                    expectedByPriceAsc = new java.util.ArrayList<>();
                    String trimmed = backendPricesJson.trim();
                    if (trimmed.startsWith("[")) {
                        // parse array of integers
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(trimmed);
                        while (m.find()) expectedByPriceAsc.add(Integer.parseInt(m.group(1)));
                    } else {
                        expectedByPriceAsc = parsePricesFromBackendJson(backendPricesJson);
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: failed to parse backend prices JSON: " + e.getMessage());
                    expectedByPriceAsc = unsortedPrices.stream().sorted().toList();
                }
            } else {
                expectedByPriceAsc = unsortedPrices.stream().sorted().toList();
            }

            historyPage.searchWithAll(testEmail, "Passenger", "Price", "Ascending", null);
            List<Integer> actualPrices = historyPage.collectAllPricesForCurrentFilter();

            if (actualPrices.size() != expectedByPriceAsc.size()) {
                // give more debug: dump page source and backend preview
                System.out.println("DEBUG: expectedByPriceAsc.size()=" + expectedByPriceAsc.size() + " actualPrices.size()=" + actualPrices.size());
                System.out.println("DEBUG: backend sample prices: " + (expectedByPriceAsc.size() > 0 ? expectedByPriceAsc.subList(0, Math.min(10, expectedByPriceAsc.size())) : expectedByPriceAsc));
                System.out.println("DEBUG: actual sample prices: " + (actualPrices.size() > 0 ? actualPrices.subList(0, Math.min(10, actualPrices.size())) : actualPrices));
                historyPage.dumpDebug("price-sort-mismatch");
            }

            assertEquals(expectedByPriceAsc.size(), actualPrices.size(), "Price count after applying price sort must match baseline");
            for (int i = 0; i < actualPrices.size(); i++) {
                assertEquals(expectedByPriceAsc.get(i), actualPrices.get(i), "Price mismatch at index " + i);
            }
        } else {
            System.out.println("DEBUG: no prices collected for " + testEmail + "; skipping price sorting assertions");
        }

        // date filter test
        LocalDate date = LocalDate.now();
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        List<java.time.LocalDate> expectedDateFiltered = unsortedDates.stream().filter(d -> d.equals(date)).toList();
        historyPage.searchWithAll(testEmail, "Passenger", null, null, dateStr);
        List<java.time.LocalDate> actualDateFiltered = historyPage.collectAllDatesForCurrentFilter();
        assertEquals(expectedDateFiltered.size(), actualDateFiltered.size(), "Filtered date count must match expected");
        for (int i = 0; i < actualDateFiltered.size(); i++) {
            assertEquals(expectedDateFiltered.get(i), actualDateFiltered.get(i));
        }
    }

    // helper: parse prices numbers from backend JSON response (expects JSON with content array of rides)
    private List<Integer> parsePricesFromBackendJson(String json) {
        List<Integer> prices = new java.util.ArrayList<>();
        if (json == null || json.isEmpty()) return prices;
        // A quick-and-dirty parse: find "price":NUMBER occurrences in JSON (sufficient for test)
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"price\"\s*:\s*([0-9]+(?:\\.[0-9]+)?)").matcher(json);
        while (m.find()) {
            String token = m.group(1);
            try {
                double d = Double.parseDouble(token);
                prices.add((int) Math.round(d));
            } catch (NumberFormatException ignored) {}
        }
        return prices;
    }
}
