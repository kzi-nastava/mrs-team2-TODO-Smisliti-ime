package com.example.e2e.pages;

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
                By.cssSelector("input[formcontrolname='email'], input[name='email'], input#email")
        ));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[formcontrolname='password'], input[name='password'], input#password")
        ));
        passInput.clear();
        passInput.sendKeys(password);

        try {
            WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
            submit.click();
        } catch (NoSuchElementException e) {
            try {
                WebElement submitByText = driver.findElement(By.xpath("//button[normalize-space(text())='Login' or normalize-space(text())='Prijava']"));
                submitByText.click();
            } catch (NoSuchElementException ex) {
                passInput.sendKeys(Keys.ENTER);
            }
        }

        // wait until either token is present in localStorage or URL changes away from /login
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                Object t = ((JavascriptExecutor) d).executeScript("return window.localStorage.getItem('authToken');");
                if (t != null) return true;
                return d.getCurrentUrl().contains("/admin") || !d.getCurrentUrl().contains("/login");
            });
        } catch (Exception ignored) {
        }
    }

    // Attempt to login via backend API and store token in localStorage. Returns true on success.
    public boolean loginViaApi(String email, String password) {
        // ensure we're on the frontend origin so fetch from frontend (CORS) is allowed
        driver.get(baseUrl);
        String backendBase = System.getProperty("backendBaseUrl", "http://localhost:8080");
        try {
            Object res = ((JavascriptExecutor) driver).executeAsyncScript(
                    "var email = arguments[0]; var pass = arguments[1]; var base = arguments[2]; var cb = arguments[arguments.length-1];\n" +
                            "fetch(base + '/api/auth/login', {\n" +
                            "  method: 'POST',\n" +
                            "  headers: {'Content-Type': 'application/json'},\n" +
                            "  body: JSON.stringify({email: email, password: pass}),\n" +
                            "  credentials: 'include'\n" +
                            "}).then(function(r){ return r.json().then(function(b){ cb({status: r.status, body: b}); }).catch(function(){ r.text().then(function(t){ cb({status: r.status, body: t}); }); }); }).catch(function(e){ cb({err: e.message}); });",
                    email, password, backendBase);

            if (res == null) return false;
            if (res instanceof java.util.Map) {
                java.util.Map m = (java.util.Map) res;
                if (m.containsKey("err")) {
                    System.out.println("DEBUG loginViaApi: fetch error: " + m.get("err"));
                    return false;
                }
                Object statusObj = m.get("status");
                int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : 0;
                Object body = m.get("body");
                if (status == 200 && body instanceof java.util.Map) {
                    java.util.Map bodyMap = (java.util.Map) body;
                    Object tokenObj = bodyMap.get("token");
                    if (tokenObj != null) {
                        String token = tokenObj.toString();
                        ((JavascriptExecutor) driver).executeScript("window.localStorage.setItem('authToken', arguments[0]);", token);
                        // navigate to base to ensure frontend picks it up
                        driver.get(baseUrl);
                        return true;
                    }
                } else {
                    System.out.println("DEBUG loginViaApi: unexpected response status=" + status + " body=" + body);
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG loginViaApi failed: " + e.getMessage());
        }
        return false;
    }
}
