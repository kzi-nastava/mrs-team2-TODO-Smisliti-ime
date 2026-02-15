package com.example.e2e.S2.rating;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PassengerRideDetailsPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(css = ".rate-button")
    WebElement rateButton;

    public PassengerRideDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public boolean isRateButtonVisible() {
        try {
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".rate-button")));
            return el != null && el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickRateRide() {
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rate-button")));
            el.click();
        } catch (Exception e) {
            // fallback to JS click in case of overlay / weird material animation
            try {
                ((JavascriptExecutor) driver).executeScript("var el = document.querySelector('.rate-button'); if (el) el.click();");
            } catch (Exception ignored) {
            }
        }
    }
}
