package com.lottery.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class E2ETestBase {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:8080/Lottery";

    protected static final String USER_EMAIL = "user@gmail.com";
    protected static final String USER_PASSWORD = "No64Adonis*";
    protected static final String ADMIN_EMAIL = "admin@gmail.com";
    protected static final String ADMIN_PASSWORD = "No64Adonis*";

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        System.out.println("\n[E2E] [INIT] ========== STARTING TEST: " + testInfo.getDisplayName() + " ==========");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        if (driver != null) {
            driver.quit();
        }
        System.out.println("[E2E] [TERM] ========== FINISHED TEST: " + testInfo.getDisplayName() + " ==========\n");
    }

    protected void logAction(String action) {
        String url = "";
        try { 
            if (driver != null) {
                String fullUrl = driver.getCurrentUrl();
                url = fullUrl != null ? fullUrl.replace(BASE_URL, "") : "";
            }
        } catch (Exception e) {}
        if (url == null || url.isEmpty()) url = "/";
        System.out.println(String.format("[E2E] [%s] %s", url, action));
    }

    protected void navigateTo(String path) {
        logAction("Navigating to path: " + path);
        driver.get(BASE_URL + path);
    }

    protected void goToHomepage()      { navigateTo("/homepage"); }
    protected void goToLogin()         { navigateTo("/login"); }
    protected void goToRegister()      { navigateTo("/register"); }
    protected void goToForgotPassword(){ navigateTo("/forgotPassword.jsp"); }
    protected void goToProfile()       { navigateTo("/profile"); }
    protected void goToUserLottery()   { navigateTo("/userLottery"); }
    protected void goToTicketPurchase(){ navigateTo("/ticketPurchase"); }
    protected void goToMyTickets()     { navigateTo("/myTickets"); }
    protected void goToTopup()         { navigateTo("/topup"); }
    protected void goToPayments()      { navigateTo("/payments"); }
    protected void goToAdminLottery()  { navigateTo("/adminLottery"); }
    protected void goToAdminTransactions() { navigateTo("/adminTransactions"); }
    protected void goToUserManagement(){ navigateTo("/userManagement"); }
    protected void goToExportUsers()   { navigateTo("/exportUsers"); }
    protected void goToPaymentMethodMgmt() { navigateTo("/paymentMethodManagement"); }
    protected void goToPriceManagement()   { navigateTo("/priceManagement"); }
    protected void goToAdminProfile()  { navigateTo("/adminProfile"); }

    protected void loginAs(String email, String password) {
        logAction("Attempting login via UI with email: " + email);
        goToLogin();
        typeById("email", email);
        typeById("password", password);
        clickButtonByText("Login");
        
        wait.until(driver -> !driver.getCurrentUrl().contains("login"));
        logAction("Login successful");
    }

    protected boolean tryLoginAs(String email, String password) {
        logAction("Attempting login (no assert) with email: " + email);
        goToLogin();
        typeById("email", email);
        typeById("password", password);
        clickButtonByText("Login");
        try {
            Thread.sleep(3000); 
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        boolean redirected = !driver.getCurrentUrl().contains("login");
        logAction("Login attempt result: " + (redirected ? "redirected" : "stayed on login"));
        return redirected;
    }

    protected void loginAsUser() {
        loginAs(USER_EMAIL, USER_PASSWORD);
    }

    protected void loginAsAdmin() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected void logout() {
        logAction("Initiating logout");
        navigateTo("/logout");
        
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(driver -> !driver.getCurrentUrl().contains("userLottery")
                    && !driver.getCurrentUrl().contains("userManagement")
                    && !driver.getCurrentUrl().contains("adminLottery"));
        } catch (TimeoutException e) {
            logAction("Logout wait timed out, continuing");
        }
        logAction("Logout completed, URL: " + driver.getCurrentUrl());
    }

    protected String registerNewUser(String email, String firstName, String lastName,
                                      String password, String confirmPassword) {
        goToRegister();
        typeById("email", email);
        typeById("firstName", firstName);
        typeById("lastName", lastName);
        typeById("password", password);
        typeById("confirmPassword", confirmPassword);
        clickButtonByText("Register");
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return email;
    }

    protected void typeById(String id, String text) {
        logAction("Typing into #" + id + " -> '" + (id.toLowerCase().contains("password") ? "****" : text) + "'");
        WebElement el = driver.findElement(By.id(id));
        el.clear();
        el.sendKeys(text);
    }

    protected void typeByName(String name, String text) {
        logAction("Typing into name='" + name + "' -> '" + (name.toLowerCase().contains("password") ? "****" : text) + "'");
        WebElement el = driver.findElement(By.name(name));
        el.clear();
        el.sendKeys(text);
    }

    protected void clickById(String id) {
        logAction("Clicking element #" + id);
        driver.findElement(By.id(id)).click();
    }

    protected void clickButtonByText(String text) {
        logAction("Clicking button containing text: '" + text + "'");
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement btn : buttons) {
            if (btn.getText().trim().contains(text)) {
                btn.click();
                return;
            }
        }
        
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            if (link.getText().trim().contains(text)) {
                link.click();
                return;
            }
        }
        throw new NoSuchElementException("Button with text '" + text + "' not found");
    }

    protected void submitFormByAction(String actionUrl) {
        logAction("Submitting form pointing to action: '" + actionUrl + "'");
        WebElement form = driver.findElement(By.cssSelector("form[action*='" + actionUrl + "']"));
        form.submit();
    }

    protected void selectByVisibleText(String id, String text) {
        logAction("Selecting dropdown #" + id + " value by text: '" + text + "'");
        Select select = new Select(driver.findElement(By.id(id)));
        select.selectByVisibleText(text);
    }

    protected boolean pageContainsText(String text) {
        return driver.getPageSource().contains(text);
    }

    protected boolean hasSuccessMessage() {
        try {
            waitForExpectedMessage(".message.success");
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected boolean hasErrorMessage() {
        try {
            waitForExpectedMessage(".message.error");
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void waitForExpectedMessage(String cssSelector) {
        new WebDriverWait(driver, Duration.ofSeconds(1))
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
    }

    protected String getSuccessMessageText() {
        List<WebElement> msgs = driver.findElements(By.cssSelector(".message.success"));
        return msgs.isEmpty() ? "" : msgs.get(0).getText();
    }

    protected String getErrorMessageText() {
        List<WebElement> msgs = driver.findElements(By.cssSelector(".message.error"));
        return msgs.isEmpty() ? "" : msgs.get(0).getText();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected boolean isOnPage(String urlFragment) {
        return getCurrentUrl().contains(urlFragment);
    }

    protected boolean elementExists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void waitForTextOnPage(String text) {
        wait.until(d -> d.getPageSource().contains(text));
    }

    protected long uniqueTimestamp() {
        return System.currentTimeMillis();
    }

    protected void setWindowSize(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
    }

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void jsSetValue(String selector, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "document.querySelector('" + selector + "').value = '" + value + "';");
    }

    protected void jsClearAndType(String id, String value) {
        logAction("JS clear+type into #" + id + " -> '" + value + "'");
        ((JavascriptExecutor) driver).executeScript(
            "var el = document.getElementById('" + id + "');" +
            "if(el){ el.value = ''; el.value = '" + value + "'; " +
            "el.dispatchEvent(new Event('input', {bubbles:true})); " +
            "el.dispatchEvent(new Event('change', {bubbles:true})); }");
    }

    protected void jsClearAndTypeByName(String name, String value) {
        logAction("JS clear+type into name='" + name + "' -> '" + value + "'");
        ((JavascriptExecutor) driver).executeScript(
            "var el = document.querySelector('[name=\"" + name + "\"]');" +
            "if(el){ el.value = ''; el.value = '" + value + "'; " +
            "el.dispatchEvent(new Event('input', {bubbles:true})); " +
            "el.dispatchEvent(new Event('change', {bubbles:true})); }");
    }

    protected boolean tryClickButtonByText(String text) {
        logAction("Trying to click button: '" + text + "'");
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement btn : buttons) {
            if (btn.getText().trim().contains(text)) {
                btn.click();
                return true;
            }
        }
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            if (link.getText().trim().contains(text)) {
                link.click();
                return true;
            }
        }
        logAction("Button '" + text + "' not found");
        return false;
    }
}
