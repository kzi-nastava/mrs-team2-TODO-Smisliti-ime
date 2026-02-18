package com.example.e2e.S1.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PassengerRideHistoryPage {
    private final WebDriverWait wait;

    @FindBy(css = "div.item[tabindex='0']")
    private WebElement firstRide;

    public PassengerRideHistoryPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void clickFirstRide() {
        wait.until(ExpectedConditions.elementToBeClickable(firstRide));
        firstRide.click();
    }
}