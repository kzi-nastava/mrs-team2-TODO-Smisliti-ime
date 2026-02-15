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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RatePage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "comment")
    WebElement commentField;

    @FindBy(xpath = "//button[normalize-space(text())='Submit']")
    WebElement submitButton;

    @FindBy(css = ".comment-text")
    List<WebElement> comments;

    // possible selectors for material snackbars across versions
    private static final List<String> SNACK_SELECTORS = Arrays.asList(
            ".mat-snack-bar-container",
            ".mat-mdc-snack-bar-container",
            "[role='status']",
            ".snack-bar",
            ".cdk-overlay-container .mat-simple-snackbar"
    );

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
                // fallback: find a button whose innerText trimmed equals 'Submit' and click it
                String script = "var buttons = document.querySelectorAll('button');\n" +
                        "for(var i=0;i<buttons.length;i++){ var b = buttons[i]; if(b.innerText && b.innerText.trim() === 'Submit'){ b.click(); return true; }} return false;";
                Object clicked = ((JavascriptExecutor) driver).executeScript(script);
                if (clicked == null || Boolean.FALSE.equals(clicked)) {
                    // as last resort click the first button
                    ((JavascriptExecutor) driver).executeScript("var b = document.querySelector('button[type=button], button[type=submit]'); if(b) b.click();");
                }
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

    // Helper that waits for the first snack-bar container among known selectors and returns its text
    public String waitForSnackBarText(int timeoutSeconds) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        for (String sel : SNACK_SELECTORS) {
            try {
                WebElement snack = shortWait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector(sel))
                );
                return snack.getText();
            } catch (Exception ignored) {
                // try next selector
            }
        }
        throw new RuntimeException("No snack-bar found using known selectors");
    }

    // Helper that waits for a snackbar with expected text (substring match)
    public boolean waitForSnackBarWithText(String expectedText, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            boolean found = shortWait.until(d -> {
                List<WebElement> all = new ArrayList<>();
                for (String sel : SNACK_SELECTORS) {
                    all.addAll(d.findElements(By.cssSelector(sel)));
                }
                if (all.isEmpty()) return false;
                for (WebElement s : all) {
                    try {
                        String t = s.getText();
                        if (t != null && t.contains(expectedText)) return true;
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                return false;
            });
            return found;
        } catch (Exception e) {
            return false;
        }
    }

}
