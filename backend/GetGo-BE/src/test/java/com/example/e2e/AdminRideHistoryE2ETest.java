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
    private final String adminEmail = System.getProperty("adminEmail", "a@gmail.com");
    private final String adminPassword = System.getProperty("adminPassword", "aaaaaaaa");

    // test email can be overridden with -DtestEmail
    private final String testEmail = System.getProperty("testEmail", "jova@gmail.com");

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

        // ensure we are on the app root after login
        driver.get(baseUrl);
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

        // collect baseline (unsorted) data for the provided email
        List<Integer> unsortedPrices = historyPage.collectAllPricesForEmail(testEmail);
        List<java.time.LocalDate> unsortedDates = historyPage.collectAllDatesForEmail(testEmail);

        System.out.println("INFO: collected unsortedPrices.size() = " + (unsortedPrices == null ? 0 : unsortedPrices.size()));
        if (unsortedPrices != null && !unsortedPrices.isEmpty()) {
            System.out.println("INFO: sample prices: " + unsortedPrices.subList(0, Math.min(5, unsortedPrices.size())));
        }
        int priceElCount = historyPage.countPriceElements();
        System.out.println("INFO: countPriceElements() = " + priceElCount);
        List<String> itemTexts = historyPage.getItemTexts();
        if (!itemTexts.isEmpty()) System.out.println("INFO: first item text snippet: " + itemTexts.get(0).replaceAll("\n"," ").substring(0, Math.min(200, itemTexts.get(0).length())));
        System.out.println("INFO: collected unsortedDates.size() = " + (unsortedDates == null ? 0 : unsortedDates.size()));

        assumeTrue(unsortedDates != null && !unsortedDates.isEmpty(), "No rides found for " + testEmail + " — ensure frontend is running and test data exists");

        // expected list sorted by date descending (newest first)
        List<java.time.LocalDate> expectedByDateDesc = unsortedDates.stream().sorted(java.util.Comparator.reverseOrder()).toList();

        // apply sort by start time (descending) and verify
        retry(() -> historyPage.searchWithAll(testEmail, "Passenger", "Start Date/Time", "Descending", null));
        List<java.time.LocalDate> actualDates = retry(() -> historyPage.collectAllDatesForCurrentFilter());
        assertEquals(expectedByDateDesc.size(), actualDates.size(), "Date count after applying date sort must match baseline");
        for (int i = 0; i < actualDates.size(); i++) {
            assertEquals(expectedByDateDesc.get(i), actualDates.get(i), "Mismatch at index " + i);
        }

        // price ascending: derive expected order from unsortedPrices
        if (unsortedPrices != null && !unsortedPrices.isEmpty()) {
            List<Integer> expectedByPriceAsc = unsortedPrices.stream().sorted().toList();

            retry(() -> historyPage.searchWithAll(testEmail, "Passenger", "Price", "Ascending", null));
            List<Integer> actualPrices = retry(() -> historyPage.collectAllPricesForCurrentFilter());

            if (actualPrices.size() != expectedByPriceAsc.size()) {
                System.out.println("INFO: expectedByPriceAsc.size()=" + expectedByPriceAsc.size() + " actualPrices.size()=" + actualPrices.size());
                historyPage.dumpDebug("price-sort-mismatch");
            }

            assertEquals(expectedByPriceAsc.size(), actualPrices.size(), "Price count after applying price sort must match baseline");
            for (int i = 0; i < actualPrices.size(); i++) {
                assertEquals(expectedByPriceAsc.get(i), actualPrices.get(i), "Price mismatch at index " + i);
            }
        } else {
            System.out.println("INFO: no prices collected for " + testEmail + "; skipping price sorting assertions");
        }

        // date filter test: check today's date filter
        LocalDate date = LocalDate.now();
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        List<java.time.LocalDate> expectedDateFiltered = unsortedDates.stream().filter(d -> d.equals(date)).toList();
        retry(() -> historyPage.searchWithAll(testEmail, "Passenger", null, null, dateStr));
        List<java.time.LocalDate> actualDateFiltered = retry(() -> historyPage.collectAllDatesForCurrentFilter());
        assertEquals(expectedDateFiltered.size(), actualDateFiltered.size(), "Filtered date count must match expected");
        for (int i = 0; i < actualDateFiltered.size(); i++) {
            assertEquals(expectedDateFiltered.get(i), actualDateFiltered.get(i));
        }
    }

    // small retry helper that retries supplier up to 3 times with small backoff
    private <T> T retry(java.util.concurrent.Callable<T> c) {
        Exception last = null;
        for (int i = 0; i < 3; i++) {
            try {
                return c.call();
            } catch (Exception e) {
                last = e;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException(last);
    }

    private void retry(Runnable r) {
        for (int i = 0; i < 3; i++) {
            try { r.run(); return; } catch (Exception e) { try { Thread.sleep(500); } catch (InterruptedException ignored) {} }
        }
        // run once more to throw
        r.run();
    }
}
