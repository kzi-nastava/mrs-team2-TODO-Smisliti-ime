package com.example.e2e.S1.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PassengerRideDetailsPage {
    private final WebDriverWait wait;

    @FindBy(className = "favorite-button")
    private WebElement favoriteButton;

    @FindBy(className = "unfavorite-button")
    private WebElement unfavoriteButton;

    public PassengerRideDetailsPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void clickFavorite() {
        wait.until(ExpectedConditions.elementToBeClickable(favoriteButton));
        favoriteButton.click();
    }

    public void clickUnfavorite() {
        wait.until(ExpectedConditions.elementToBeClickable(unfavoriteButton));
        unfavoriteButton.click();
    }
}