package com.example.e2e.S1.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:4200";

    @FindBy(id = "login-email")
    private WebElement emailInput;

    @FindBy(id = "login-password")
    private WebElement passwordInput;

    @FindBy(id = "login-button")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL + "/login");
        PageFactory.initElements(driver, this);
    }

    public void login(String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(emailInput));
        emailInput.clear();
        emailInput.sendKeys(email);

        passwordInput.clear();
        passwordInput.sendKeys(password);

        loginButton.click();
    }
}