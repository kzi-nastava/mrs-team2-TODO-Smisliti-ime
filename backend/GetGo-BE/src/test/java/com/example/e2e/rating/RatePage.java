package com.example.e2e.rating;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RatePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public RatePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void setVehicleRating(int stars) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("stars must be 1..5");
        String selector = "input#vehicle" + stars;
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        try { input.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input); }
    }

    public void setDriverRating(int stars) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("stars must be 1..5");
        String selector = "input#driver" + stars;
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        try { input.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input); }
    }

    public void setComment(String text) {
        WebElement ta = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("textarea#comment, textarea[name='comment']")));
        ta.clear();
        ta.sendKeys(text);
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rating-form button[type='button'], .rating-form button[type='submit'], button[data-test='submit-rating']")));
        try { btn.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }
    }

    public String getSuccessMessage() {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rating-success, .success-message, .toast-success")));
            return el.getText();
        } catch (Exception e) {
            return null;
        }
    }

    public String getErrorMessage() {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message, .rating-error")));
            return el.getText();
        } catch (Exception e) {
            return null;
        }
    }

    // Return true if the comment text is visible in post-submit comments list
    public boolean hasComment(String comment) {
        try {
            // look for any element with class comment-text
            List<WebElement> comments = driver.findElements(By.cssSelector(".comments-list .comment-text, .comment-text"));
            for (WebElement c : comments) {
                String txt = c.getText();
                if (txt != null && txt.contains(comment)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSubmitEnabled() {
        try {
            WebElement btn = driver.findElement(By.cssSelector(".rating-form button[type='button'], .rating-form button[type='submit'], button[data-test='submit-rating']"));
            return btn.isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

}
