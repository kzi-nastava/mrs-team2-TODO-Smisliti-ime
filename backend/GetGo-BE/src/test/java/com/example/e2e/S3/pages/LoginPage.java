package com.example.e2e.S3.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public LoginPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    public void open() {
        driver.get(baseUrl + "/login");
    }

    public void login(String email, String password) {
        open();
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[formcontrolname='email'], input[name='email'], input#login-email, input#email")
        ));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[formcontrolname='password'], input[name='password'], input#login-password, input#password")
        ));
        passInput.clear();
        passInput.sendKeys(password);

        // Try several submit strategies in order
        boolean submitted = false;
        try {
            WebElement submit = null;
            try { submit = driver.findElement(By.cssSelector("button[type='submit']")); } catch (Exception ignored) {}
            if (submit == null) {
                try { submit = driver.findElement(By.id("login-button")); } catch (Exception ignored) {}
            }
            if (submit == null) {
                try { submit = driver.findElement(By.xpath("//button[normalize-space(text())='Login' or normalize-space(text())='Prijava']")); } catch (Exception ignored) {}
            }
            if (submit != null) {
                try { wait.until(ExpectedConditions.elementToBeClickable(submit)); } catch (Exception ignored) {}
                try { submit.click(); submitted = true; } catch (Exception e) { try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit); submitted = true; } catch (Exception ignored) {} }
            }
        } catch (Exception ignored) {}

        if (!submitted) {
            try { passInput.sendKeys(Keys.ENTER); } catch (Exception ignored) {}
        }

        // wait until token is present in either storage or URL changes away from /login
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                try {
                    Object local = ((JavascriptExecutor) d).executeScript("return window.localStorage.getItem('authToken');");
                    Object session = ((JavascriptExecutor) d).executeScript("return window.sessionStorage.getItem('authToken');");
                    if (local != null && !String.valueOf(local).isBlank()) return true;
                    if (session != null && !String.valueOf(session).isBlank()) return true;
                } catch (Exception ignored) {}
                String url = d.getCurrentUrl();
                return url.contains("/admin") || !url.contains("/login");
            });
        } catch (Exception ignored) {
        }
    }
}
