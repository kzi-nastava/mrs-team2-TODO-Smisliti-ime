package com.example.e2e.rating;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class PassengerRideHistoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public PassengerRideHistoryPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    public void open() {
        driver.get(baseUrl + "/passenger/rides");
        try {
            wait.withTimeout(Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ride-history-page")));
        } catch (Exception e) {
            // check if login form is present (redirected)
            try {
                boolean loginForm = !driver.findElements(By.cssSelector("input[formcontrolname='email'], input[name='email'], input#email")).isEmpty();
                if (loginForm) {
                    dumpDebug("passenger-ride-history-redirected-to-login");
                    throw new IllegalStateException("Not logged in: passenger ride history redirected to login");
                }
            } catch (Exception ignored) {}
            // save debug HTML for investigation
            dumpDebug("passenger-ride-history-missing");
            throw new IllegalStateException("Ride history page did not load: .ride-history-page not found", e);
        }
    }

    public List<WebElement> getRideItems() {
        wait.until(d -> d.findElements(By.cssSelector(".list .item")).size() >= 0);
        return driver.findElements(By.cssSelector(".list .item"));
    }

    /**
     * Click the ride item at the given 0-based index and wait for ride details to appear.
     */
    public void openRideByIndex(int index) {
        List<WebElement> items = getRideItems();
        if (items == null || items.isEmpty() || index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Invalid ride index: " + index);
        }
        WebElement item = items.get(index);
        try {
            item.click();
        } catch (Exception e) {
            // fallback to JS click if regular click fails due to overlay
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", item);
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ride-details")));
    }

    /**
     * Click the Rate Ride button inside ride details. Returns a RatePage for interacting with the rating form.
     */
    public RatePage openRateForCurrentRide() {
        try {
            WebElement rateBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rate-button")));
            try {
                rateBtn.click();
            } catch (Exception ex) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", rateBtn);
            }
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Rate button not present or ride not finished", e);
        }
        // wait for rating form to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".rating-form, textarea#comment")));
        return new RatePage(driver, wait);
    }

    /**
     * Convenience: open page, pick ride by index and open rating form
     */
    public RatePage openRideAndGoToRate(int index) {
        open();
        openRideByIndex(index);
        return openRateForCurrentRide();
    }

    private void dumpDebug(String fileNamePrefix) {
        // Implement debug HTML saving logic here
        // For example, save the page source to a file
        String pageSource = driver.getPageSource();
        try {
            File debugFile = new File(Path.of("debug", fileNamePrefix + ".html").toString());
            debugFile.getParentFile().mkdirs(); // create directories if not exist
            org.apache.commons.io.FileUtils.writeStringToFile(debugFile, pageSource, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
