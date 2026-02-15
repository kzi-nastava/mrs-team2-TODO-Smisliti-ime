package com.example.e2e.rating;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class PassengerRideHistoryPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private static String PAGE_URL_SUFFIX = "/passenger/passenger-ride-history";

    @FindBy(css = ".item")
    List<WebElement> rideItems;

    public PassengerRideHistoryPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl + PAGE_URL_SUFFIX);

        // wait until either items are present or the 'no rides found' placeholder is visible
        try {
            wait.until(d -> d.findElements(By.cssSelector(".item")).size() > 0
                    || d.findElements(By.cssSelector(".list div[style*='No rides found']")).size() > 0
                    || d.findElements(By.cssSelector(".list .item")).size() >= 0);
        } catch (Exception ignored) {
        }

        PageFactory.initElements(driver, this);
    }

    public boolean hasRides() {
        return rideItems != null && !rideItems.isEmpty();
    }

    public void openFirstRide() {
        rideItems.get(0).click();
    }

    public int getRideCount() {
        return rideItems.size();
    }
}
