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
import java.util.List;

public class PassengerHomePage {
    private final WebDriverWait wait;
    private final WebDriver driver;

    @FindBy(className = "favorites-toggle-btn")
    private WebElement favoritesToggleButton;

    @FindBy(id = "order-ride-btn")
    private WebElement orderRideButton;

    public PassengerHomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    private void safeClick(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));

        ((JavascriptExecutor) driver)
                .executeScript(
                        "arguments[0].scrollIntoView({block: 'center'});",
                        element
                );

        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    public void openFavorites() {
        safeClick(favoritesToggleButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("favorites-list")
        ));
    }

    public void selectFirstFavorite() {
        WebElement firstFavorite = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".favorite-item:first-child"))
        );
        firstFavorite.click();
    }

    public boolean areDestinationsFilled() {
        wait.until(d -> {
            List<WebElement> inputs = d.findElements(By.cssSelector(".dest-row input"));
            return inputs.size() >= 2
                    && !inputs.getFirst().getAttribute("value").isEmpty()
                    && !inputs.getLast().getAttribute("value").isEmpty();
        });
        return true;
    }

    public void clickOrderRide() {
        wait.until(ExpectedConditions.elementToBeClickable(orderRideButton));
        orderRideButton.click();
    }

    public boolean isSuccessMessageDisplayed() {
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Ride ordered successfully')]")
        ));
        return toast.isDisplayed();
    }

    public String getFavoritesButtonText() {
        wait.until(ExpectedConditions.visibilityOf(favoritesToggleButton));
        return favoritesToggleButton.getText();
    }

    public boolean isFavoritesEmpty() {
        safeClick(favoritesToggleButton);

        WebElement emptyMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("favorites-empty")
                )
        );
        return emptyMsg.isDisplayed();
    }

}