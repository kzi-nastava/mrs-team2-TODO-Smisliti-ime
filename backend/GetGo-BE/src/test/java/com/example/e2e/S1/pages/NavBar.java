package com.example.e2e.S1.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NavBar {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "nav-hamburger")
    private WebElement hamburgerButton;

    @FindBy(id = "nav-ride-history")
    private WebElement rideHistoryItem;

    @FindBy(id = "nav-home")
    private WebElement homeItem;

    public NavBar(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    private void openMenu() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nav-hamburger")));
        // Normal select and click clicks on icon so js executor is needed
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("mat-mdc-menu-panel")));
    }

    public void goToRideHistory() {
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(rideHistoryItem));
        rideHistoryItem.click();
        wait.until(ExpectedConditions.urlContains("/passenger/passenger-ride-history"));
    }

    public void goToHome() {
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(homeItem));
        homeItem.click();
        wait.until(ExpectedConditions.urlContains("/registered-home"));
    }
}