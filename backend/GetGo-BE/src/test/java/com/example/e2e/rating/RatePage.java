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

import java.time.Duration;
import java.util.List;

public class RatePage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "comment")
    WebElement commentField;

    @FindBy(xpath = "//button[text()='Submit']")
    WebElement submitButton;

    @FindBy(css = ".comment-text")
    List<WebElement> comments;

    public RatePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void selectVehicleRating(int rating) {
        WebElement label = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("label[for='vehicle" + rating + "']"))
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", label);
        label.click();
    }

    public void selectDriverRating(int rating) {
        WebElement label = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("label[for='driver" + rating + "']"))
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", label);
        label.click();
    }



    public void enterComment(String text) {
        wait.until(ExpectedConditions.visibilityOf(commentField));
        commentField.clear();
        commentField.sendKeys(text);
    }

    public void submit() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            submitButton.click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("var b = document.querySelector(\"button[type=button], button[type=submit], button:contains('Submit')\"); if(b) b.click();");
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isCommentPresent(String text) {
        wait.until(d -> comments != null);
        for (WebElement el : comments) {
            if (el.getText().contains(text)) {
                return true;
            }
        }
        return false;
    }

}
