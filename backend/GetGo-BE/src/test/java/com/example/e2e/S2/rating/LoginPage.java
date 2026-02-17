package com.example.e2e.S2.rating;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

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
        // Use JVM HttpClient to avoid CORS and browser network restrictions
        String backendBase = System.getProperty("backendBaseUrl", "http://localhost:8080");
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String payload = mapper.writeValueAsString(Map.of("email", email, "password", password));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(backendBase + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp == null) {
                System.out.println("DEBUG loginViaApi: null response");
                return false;
            }

            int status = resp.statusCode();
            String body = resp.body();
            if (status == 200 && body != null && !body.isEmpty()) {
                Object parsed = mapper.readValue(body, Object.class);
                String token = extractTokenFromBody(parsed);
                if (token != null) {
                    // set token into browser localStorage so frontend can use it
                    driver.get(baseUrl); // ensure same origin
                    ((JavascriptExecutor) driver).executeScript("window.localStorage.setItem('authToken', arguments[0]);", token);
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> ((JavascriptExecutor) d).executeScript("return window.localStorage.getItem('authToken') != null;") != null);
                    } catch (Exception ignored) {}
                    driver.get(baseUrl); // reload to ensure frontend reads token
                    return true;
                } else {
                    System.out.println("DEBUG loginViaApi: could not extract token from response body: " + body);
                    return false;
                }
            } else {
                System.out.println("DEBUG loginViaApi: unexpected response status=" + status + " body=" + body);
                return false;
            }
        } catch (Exception e) {
            System.out.println("DEBUG loginViaApi failed: " + e.getMessage());
            return false;
        }
    }

    // recursively search for a plausible token string in a nested map/array body
    private String extractTokenFromBody(Object body) {
        try {
            if (body == null) return null;
            if (body instanceof Map) {
                Map map = (Map) body;
                // common keys
                String[] keys = new String[]{"token", "accessToken", "access_token", "jwt", "authToken", "accessJwt"};
                for (String k : keys) {
                    Object v = map.get(k);
                    if (v instanceof String) {
                        String s = (String) v;
                        if (looksLikeToken(s)) return s;
                    }
                }
                // try to find any string value that looks like a token
                for (Object val : map.values()) {
                    if (val instanceof String) {
                        String s = (String) val;
                        if (looksLikeToken(s)) return s;
                    } else {
                        String nested = extractTokenFromBody(val);
                        if (nested != null) return nested;
                    }
                }
            } else if (body instanceof String) {
                String s = (String) body;
                if (looksLikeToken(s)) return s;
            } else if (body instanceof Object[]) {
                Object[] arr = (Object[]) body;
                for (Object o : arr) {
                    String nested = extractTokenFromBody(o);
                    if (nested != null) return nested;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private boolean looksLikeToken(String s) {
        if (s == null) return false;
        s = s.trim();
        // JWT has two dots typically e.g. header.payload.signature
        if (s.split("\\.").length >= 3 && s.length() > 20) return true;
        // or long base64-ish token
        if (s.length() > 40) return true;
        return false;
    }
}

