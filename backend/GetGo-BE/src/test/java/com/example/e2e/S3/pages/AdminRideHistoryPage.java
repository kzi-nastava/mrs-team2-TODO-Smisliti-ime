package com.example.e2e.S3.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminRideHistoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public AdminRideHistoryPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/admin/admin-ride-history");
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        longWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[formcontrolname='email']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".admin-ride-history"))
        ));
    }

    public void setEmail(String email) {
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[formcontrolname='email']")));
        boolean applied = false;
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true })); arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    emailInput, email);
            applied = true;
        } catch (Exception ignored) {
        }

        try {
            emailInput.clear();
            emailInput.sendKeys(email);
            emailInput.sendKeys(org.openqa.selenium.Keys.TAB);
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].blur();", emailInput); } catch (Exception ignored) {}
            applied = true;
        } catch (Exception ignored) {}

        if (!applied) {
            System.out.println("WARN setEmail: unable to programmatically apply email='" + email + "'");
        }

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            shortWait.until(d -> {
                try {
                    WebElement el = d.findElement(By.cssSelector("input[formcontrolname='email']"));
                    String v = el.getAttribute("value");
                    return v != null && v.equals(email);
                } catch (Exception e) { return false; }
            });
        } catch (Exception ignored) {}
    }

    public void setUserType(String type) {
        selectMat("mat-select[formcontrolname='userType']", type);
    }

    public void setSortBy(String sortBy) {
        selectMat("mat-select[formcontrolname='sortBy']", sortBy);
    }

    public void setSortDirection(String dir) {
        selectMat("mat-select[formcontrolname='sortDirection']", dir);
    }

    public void setDate(String dateStr) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        java.time.LocalDate target;
        try {
            target = java.time.LocalDate.parse(dateStr, fmt);
        } catch (Exception e) {
            WebElement dateInputFallback = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[formcontrolname='date']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", dateInputFallback, dateStr);
            return;
        }

        String iso = String.format("%04d-%02d-%02d", target.getYear(), target.getMonthValue(), target.getDayOfMonth());
        String dd = String.valueOf(target.getDayOfMonth());
        String setDateScript =
                "(function(dateIso, visibleStr, day){\n" +
                "  try{\n" +
                "    var input = document.querySelector('input[formcontrolname=\\'date\\']');\n" +
                "    if(!input){ return {ok:false, reason:'no-input'}; }\n" +
                "    try{ input.value = visibleStr; input.dispatchEvent(new Event('input',{bubbles:true})); input.dispatchEvent(new Event('change',{bubbles:true})); input.blur(); }catch(e){}\n" +
                "    try{ if('valueAsDate' in input){ var parts = dateIso.split('-'); input.type='date'; input.valueAsDate = new Date(parts[0], parts[1]-1, parts[2]); input.dispatchEvent(new Event('input',{bubbles:true})); input.dispatchEvent(new Event('change',{bubbles:true})); } }catch(e){}\n" +
                "    try{ if(window.ng && window.ng.getOwningComponent){ var cmp = window.ng.getOwningComponent(input); if(cmp && cmp.searchRideForm && cmp.searchRideForm.get){ try{ cmp.searchRideForm.get('date').setValue(new Date(dateIso)); if(cmp.searchRideForm.updateValueAndValidity) cmp.searchRideForm.updateValueAndValidity(); }catch(e){} } } }catch(e){}\n" +
                "    try{ var toggle = document.querySelector('mat-datepicker-toggle button'); if(toggle){ toggle.click(); setTimeout(function(){ var dayBtn = document.evaluate(\"//mat-calendar//button[normalize-space(.)='" + dd + "' and not(contains(@class,'mat-calendar-body-disabled'))]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; if(dayBtn){ dayBtn.click(); } }, 200); } }catch(e){}\n" +
                "    var val = input.value || input.getAttribute('value') || (input.textContent||'');\n" +
                "    if(val && (val.indexOf(visibleStr) !== -1 || val.indexOf(day) !== -1)) return {ok:true, val:val};\n" +
                "    try{ var parent = input.closest('.mat-form-field'); if(parent){ var disp = parent.querySelector('.mat-form-field-wrapper, .mat-form-field-infix'); if(disp && disp.textContent && disp.textContent.indexOf(visibleStr)!==-1) return {ok:true,val:disp.textContent}; } }catch(e){}\n" +
                "    return {ok:false, reason:'no-match', val: val};\n" +
                "  }catch(e){ return {ok:false, reason: e.message}; }\n" +
                "})(arguments[0], arguments[1], arguments[2]);";

        Object result = ((JavascriptExecutor) driver).executeScript(setDateScript, iso, dateStr, dd);
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            shortWait.pollingEvery(java.time.Duration.ofMillis(200));
            shortWait.until(d -> {
                try {
                    WebElement el = d.findElement(By.cssSelector("input[formcontrolname='date']"));
                    String v = el.getAttribute("value");
                    if (v != null && (v.contains(dateStr) || v.contains(String.format("%02d.%02d.%04d", target.getDayOfMonth(), target.getMonthValue(), target.getYear())))) return true;
                    try {
                        WebElement parent = el.findElement(By.xpath("ancestor::mat-form-field"));
                        if (parent != null && parent.getText().contains(dateStr)) return true;
                    } catch (Exception ignored) {}
                    return false;
                } catch (Exception e) { return false; }
            });
        } catch (Exception e) {
            System.out.println("WARN setDate: wait for date reflection failed: " + e.getMessage() + " execResult=" + result);
        }
    }

    public void clickSearch() {
        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        try {
            searchBtn.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);
        }
        try {
            waitForItemsLoaded();
            ensureItemsPopulated();
        } catch (Exception e) {
            System.out.println("WARN clickSearch: wait after search failed: " + e.getMessage());
            dumpDebug("after-search-wait-failed");
        }

        try {
            String email = (String) ((JavascriptExecutor) driver).executeScript("var e = document.querySelector('input[formcontrolname=\\'email\\']'); return e ? e.value : null;");
            if (email == null) email = "";
            String sortParam = null;
            String dirParam = null;
            String startDateParam = null;
            Object sortVisible = ((JavascriptExecutor) driver).executeScript("var el = document.querySelector('mat-select[formcontrolname=\\'sortBy\\'] .mat-select-value-text span'); return el ? el.textContent.trim() : null;");
            Object dirVisible = ((JavascriptExecutor) driver).executeScript("var el = document.querySelector('mat-select[formcontrolname=\\'sortDirection\\'] .mat-select-value-text span'); return el ? el.textContent.trim() : null;");
            Object dateVisible = ((JavascriptExecutor) driver).executeScript("var el = document.querySelector('input[formcontrolname=\\'date\\']'); return el ? el.value || el.getAttribute('value') || el.textContent : null;");
            String sv = sortVisible == null ? null : sortVisible.toString();
            String dv = dirVisible == null ? null : dirVisible.toString();
            String dvDate = dateVisible == null ? null : dateVisible.toString();
            if (sv != null) {
                switch (sv) {
                    case "Start Date/Time": sortParam = "startTime"; break;
                    case "Duration": sortParam = "estTime"; break;
                    case "Distance": sortParam = "estDistanceKm"; break;
                    case "Price": sortParam = "estimatedPrice"; break;
                    default: sortParam = null;
                }
            }
            if (dv != null) {
                if (dv.equalsIgnoreCase("Descending")) dirParam = "DESC"; else if (dv.equalsIgnoreCase("Ascending")) dirParam = "ASC";
            }
            if (dvDate != null && !dvDate.isBlank()) {
                String s = dvDate.trim();
                java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4})").matcher(s);
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})").matcher(s);
                java.util.regex.Matcher m3 = java.util.regex.Pattern.compile("(\\d{2})-(\\d{2})-(\\d{4})").matcher(s);
                if (m1.find()) {
                    startDateParam = m1.group(1) + "-" + m1.group(2) + "-" + m1.group(3);
                } else if (m2.find()) {
                    startDateParam = m2.group(3) + "-" + m2.group(2) + "-" + m2.group(1);
                } else if (m3.find()) {
                    startDateParam = m3.group(1) + "-" + m3.group(2) + "-" + m3.group(3);
                }
            }
            String json = fetchRidesJson(email, 0, 50, sortParam, dirParam, startDateParam);
            if (json == null) System.out.println("DEBUG clickSearch: backend JSON is null"); else System.out.println("DEBUG clickSearch: backend JSON preview: " + (json.length() > 1000 ? json.substring(0,1000) + "..." : json));
        } catch (Exception e) {
            System.out.println("WARN clickSearch: failed to fetch backend JSON: " + e.getMessage());
        }
    }

    public void clickReset() {
        // Try a few locator strategies to find the Reset button robustly
        By[] locators = new By[]{
                By.xpath("//div[contains(@class,'buttons')]//button[normalize-space(.)='Reset']"),
                By.cssSelector(".buttons button[type='button']"),
                By.xpath("//button[contains(., 'Reset')]")
        };

        WebElement reset = null;
        for (By locator : locators) {
            try {
                reset = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                if (reset != null) break;
            } catch (Exception ignored) {}
        }

        if (reset == null) {
            // last attempt: try to find any button with text containing 'Reset' (case-insensitive)
            try {
                List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                for (WebElement b : allButtons) {
                    try {
                        String t = b.getText();
                        if (t != null && t.trim().equalsIgnoreCase("reset")) { reset = b; break; }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        if (reset == null) {
            System.out.println("WARN clickReset: Reset button not found with any locator");
            throw new NoSuchElementException("Reset button not found");
        }

        try {
            // scroll into view and click using safe patterns
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", reset); } catch (Exception ignored) {}
            WebDriverWait clickableWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            clickableWait.until(ExpectedConditions.elementToBeClickable(reset));
            try {
                reset.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException ex) {
                try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reset); } catch (Exception jsEx) { throw ex; }
            }
        } catch (Exception e) {
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reset); } catch (Exception ex) {
                System.out.println("WARN clickReset: failed to click reset: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }

        try {
            waitForItemsLoaded();
            ensureItemsPopulated();
        } catch (Exception e) {
            System.out.println("WARN clickReset: wait after reset failed: " + e.getMessage());
            dumpDebug("after-reset-wait-failed");
        }
    }

    private void selectMat(String matSelector, String visibleText) {
        By locator = By.cssSelector(matSelector);
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        longWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        WebElement select = driver.findElement(locator);
        try {
            WebElement trigger = select.findElement(By.cssSelector(".mat-select-trigger"));
            longWait.until(ExpectedConditions.elementToBeClickable(trigger));
            trigger.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", select);
        }
        By option = By.xpath("//mat-option//span[normalize-space(text()) = '" + visibleText + "']");
        WebElement opt = longWait.until(ExpectedConditions.elementToBeClickable(option));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", opt);
        opt.click();

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            shortWait.until(d -> {
                try {
                    WebElement sel = d.findElement(locator);
                    Object txt = ((JavascriptExecutor) d).executeScript("const el = arguments[0]; const span = el.querySelector('.mat-select-value-text span'); return span ? span.textContent.trim() : (el.textContent || '');", sel);
                    if (txt == null) return false;
                    String s = txt.toString().trim();
                    return s.equals(visibleText);
                } catch (Exception e) { return false; }
            });
        } catch (Exception ignored) {}
    }

    public List<WebElement> getResultItems() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".list .item .card-date")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".list .item .card-price")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".list .item")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'No rides found for this user.') or contains(text(),'Enter email and search to view rides.') ]"))
            ));
        } catch (Exception ignored) {
        }
        return driver.findElements(By.cssSelector(".list .item"));
    }

    public List<Integer> parsePrices(List<WebElement> items) {
        List<Integer> prices = new ArrayList<>();
        if (items == null || items.isEmpty()) items = driver.findElements(By.cssSelector(".list .item"));
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+[\\.,]?\\d*)");
        for (WebElement item : items) {
            try {
                try {
                    WebElement priceEl = item.findElement(By.cssSelector(".card-price"));
                    String raw = priceEl.getText();
                    String digits = raw.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) {
                        System.out.println("DEBUG parsePrices: found .card-price raw='" + raw + "' digits='" + digits + "'");
                        prices.add(Integer.parseInt(digits));
                        continue;
                    }
                } catch (NoSuchElementException ignored) {
                }

                String txt = item.getText();
                java.util.regex.Matcher m = p.matcher(txt);
                Integer found = null;
                while (m.find()) {
                    String token = m.group(1);
                    String digits = token.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) {
                        found = Integer.parseInt(digits);
                    }
                }
                if (found != null) prices.add(found);
            } catch (StaleElementReferenceException e) {
            }
        }
        return prices;
    }

    public List<LocalDate> parseDates(List<WebElement> items) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<LocalDate> dates = new ArrayList<>();
        if (items == null || items.isEmpty()) items = driver.findElements(By.cssSelector(".list .item"));
        for (WebElement item : items) {
            try {
                try {
                    WebElement dateEl = item.findElement(By.cssSelector(".card-date b, .card-date"));
                    String raw = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", dateEl);
                    if (raw != null) raw = raw.trim(); else raw = "";
                    String dateOnly = raw.replaceAll("[^0-9.]", "").trim();
                    if (!dateOnly.isEmpty()) {
                        try { dates.add(LocalDate.parse(dateOnly, fmt)); } catch (Exception pe) { System.out.println("DEBUG parseDates: failed to parse dateOnly='" + dateOnly + "' -> " + pe.getMessage()); }
                    }
                    continue;
                } catch (NoSuchElementException ignored) { }

                String txt = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", item);
                if (txt == null) txt = item.getText();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})").matcher(txt);
                if (m.find()) {
                    String found = m.group(1);
                    try { dates.add(LocalDate.parse(found, fmt)); } catch (Exception pe) { System.out.println("DEBUG parseDates: failed parse regex date='" + found + "' -> " + pe.getMessage()); }
                }
            } catch (StaleElementReferenceException e) {
            }
        }
        return dates;
    }

    public void waitForItemsLoaded() {
        wait.until(driver -> {
            try {
                if (!driver.findElements(By.cssSelector(".list .item .card-date")).isEmpty()) return true;
                if (!driver.findElements(By.cssSelector(".list .item .card-price")).isEmpty()) return true;
                if (!driver.findElements(By.cssSelector(".list .item")).isEmpty()) return true;
                if (!driver.findElements(By.xpath("//*[contains(text(),'No rides found for this user.') or contains(text(),'Enter email and search to view rides.')]")) .isEmpty()) return true;
            } catch (Exception e) {
            }
            return false;
        });
    }

    public List<WebElement> fetchUnsortedForEmail(String email) {
        searchWithAll(email, "Passenger", null, null, null);
        List<WebElement> items = getResultItems();
        if (items.isEmpty()) dumpDebug("fetch-unsorted-empty");
        return items;
    }

    public List<Integer> collectAllPricesForEmail(String email) {
        searchWithAll(email, "Passenger", null, null, null);
        List<Integer> all = new ArrayList<>();
        boolean firstIteration = true;
        java.util.Set<String> seen = new java.util.HashSet<>();
        while (true) {
            waitForItemsLoaded();
            ensureItemsPopulated();
            List<WebElement> items = getResultItems();
            if (firstIteration && (items == null || items.isEmpty())) {
                dumpDebug("collect-prices-empty-firstpage");
                try {
                    WebElement list = driver.findElement(By.cssSelector(".list"));
                    System.out.println("DEBUG: .list innerHTML (truncated): " + ((JavascriptExecutor) driver).executeScript("return arguments[0].innerHTML.slice(0,1000)", list));
                } catch (Exception e) {
                    System.out.println("DEBUG: unable to read .list innerHTML: " + e.getMessage());
                }
            }
            firstIteration = false;
            List<WebElement> unique = new ArrayList<>();
            for (WebElement it : items) {
                try {
                    String sig = it.getText();
                    if (sig == null) continue;
                    if (seen.contains(sig)) continue;
                    seen.add(sig);
                    unique.add(it);
                } catch (StaleElementReferenceException sere) {
                }
            }
            all.addAll(parsePrices(unique));
            if (!isNextPageAvailable()) break;
            clickNextPage();
        }

        if (all.isEmpty()) {
            int countPriceEls = countPriceElements();
            String src = driver.getPageSource();
            if (countPriceEls > 0 || src.contains("rsd")) {
                List<Integer> fromSrc = extractPricesFromPageSource(src);
                if (!fromSrc.isEmpty()) {
                    System.out.println("DEBUG: extractPricesFromPageSource found: " + fromSrc);
                    return fromSrc;
                }
            }
        }
        return all;
    }

    private List<Integer> extractPricesFromPageSource(String src) {
        List<Integer> res = new ArrayList<>();
        if (src == null || src.isEmpty()) return res;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d{2,6})\\s*rsd", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(src);
        while (m.find()) {
            try {
                res.add(Integer.parseInt(m.group(1)));
            } catch (NumberFormatException ignored) {}
        }
        if (res.isEmpty()) {
            java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("(\\d{2,6})(?=\\D|<)");
            java.util.regex.Matcher m2 = p2.matcher(src);
            while (m2.find()) {
                try { res.add(Integer.parseInt(m2.group(1))); } catch (NumberFormatException ignored) {}
            }
        }
        return res;
    }

    public List<LocalDate> collectAllDatesForEmail(String email) {
        searchWithAll(email, "Passenger", null, null, null);
        List<LocalDate> all = new ArrayList<>();
        boolean firstIteration = true;
        java.util.Set<String> seen = new java.util.HashSet<>();
        while (true) {
            waitForItemsLoaded();
            ensureItemsPopulated();
            List<WebElement> items = getResultItems();
            if (firstIteration && (items == null || items.isEmpty())) {
                dumpDebug("collect-dates-empty-firstpage");
                try {
                    WebElement list = driver.findElement(By.cssSelector(".list"));
                    System.out.println("DEBUG: .list innerHTML (truncated): " + ((JavascriptExecutor) driver).executeScript("return arguments[0].innerHTML.slice(0,1000)", list));
                } catch (Exception e) {
                    System.out.println("DEBUG: unable to read .list innerHTML: " + e.getMessage());
                }
            }
            firstIteration = false;
            List<WebElement> unique = new ArrayList<>();
            for (WebElement it : items) {
                try {
                    String sig = it.getText();
                    if (sig == null) continue;
                    if (seen.contains(sig)) continue;
                    seen.add(sig);
                    unique.add(it);
                } catch (StaleElementReferenceException sere) {
                }
            }
            all.addAll(parseDates(unique));
            if (!isNextPageAvailable()) break;
            clickNextPage();
        }
        return all;
    }

    public int countPriceElements() {
        try {
            return driver.findElements(By.cssSelector(".list .item .card-price")).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> getItemTexts() {
        List<WebElement> items = driver.findElements(By.cssSelector(".list .item"));
        List<String> texts = new ArrayList<>();
        for (WebElement it : items) {
            try {
                texts.add(it.getText());
            } catch (StaleElementReferenceException e) {
                texts.add("<stale>");
            }
        }
        return texts;
    }

    private boolean isNextPageAvailable() {
        try {
            WebElement nextBtn = driver.findElement(By.cssSelector("mat-paginator button[aria-label='Next page']"));
            String ariaDisabled = nextBtn.getAttribute("aria-disabled");
            String disabled = nextBtn.getAttribute("disabled");
            if ((ariaDisabled != null && ariaDisabled.equalsIgnoreCase("true")) || (disabled != null && disabled.equalsIgnoreCase("true"))) {
                return false;
            }
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean clickNextPageWithWait(String prevFirstText) {
        try {
            WebElement nextBtn = driver.findElement(By.cssSelector("mat-paginator button[aria-label='Next page']"));
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", nextBtn);
            } catch (Exception ignored) {}
            try {
                nextBtn.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException ex) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
                } catch (Exception e) {
                    System.out.println("DEBUG clickNextPage: click failed: " + e.getMessage());
                    return false;
                }
            }
            WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            shortWait.pollingEvery(java.time.Duration.ofMillis(200));
            try {
                shortWait.until(d -> {
                    try {
                        waitForItemsLoaded();
                        ensureItemsPopulated();
                        List<WebElement> items = d.findElements(By.cssSelector(".list .item"));
                        if (items.isEmpty()) return true;
                        String first = items.get(0).getText();
                        if (prevFirstText == null) return true;
                        return !first.equals(prevFirstText);
                    } catch (Exception e) {
                        return false;
                    }
                });
            } catch (Exception ignored) {
            }
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void clickNextPage() {
        clickNextPageWithWait(null);
    }

    public void dumpDebug(String prefix) {
        try {
            String ts = String.valueOf(System.currentTimeMillis());
            if (driver instanceof TakesScreenshot) {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Files.copy(scr.toPath(), Path.of("target", prefix + "-" + ts + ".png"));
            }
            String src = driver.getPageSource();
            java.nio.file.Files.writeString(Path.of("target", prefix + "-" + ts + ".html"), src);
            System.out.println("Wrote debug files: target/" + prefix + "-" + ts + ".png/html");
        } catch (Exception e) {
            System.out.println("Failed to write debug state: " + e.getMessage());
        }
    }

    private void ensureItemsPopulated() {
        WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(3));
        shortWait.pollingEvery(java.time.Duration.ofMillis(200));
        try {
            shortWait.until(d -> {
                try {
                    if (!d.findElements(By.cssSelector(".list .item .card-price")).isEmpty()) return true;
                    if (!d.findElements(By.cssSelector(".list .item .card-date")).isEmpty()) return true;
                    List<WebElement> items = d.findElements(By.cssSelector(".list .item"));
                    for (WebElement it : items) {
                        try {
                            String t = it.getText();
                            if (t != null && (t.toLowerCase().contains("rsd") || java.util.regex.Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}").matcher(t).find())) return true;
                        } catch (StaleElementReferenceException ignored) {}
                    }
                } catch (Exception ignored) {
                }
                return false;
            });
        } catch (Exception ignored) {
        }
    }

    public List<Integer> collectAllPricesForCurrentFilter() {
        List<Integer> all = new ArrayList<>();
        int page = 0;
        java.util.Set<String> seen = new java.util.HashSet<>();
        while (true) {
            waitForItemsLoaded();
            ensureItemsPopulated();
            List<WebElement> items = getResultItems();
            System.out.println("DEBUG collectAllPricesForCurrentFilter: page=" + page + " itemsFound=" + items.size());
            if (!items.isEmpty()) {
                try { System.out.println("DEBUG first item text: " + items.get(0).getText().replaceAll("\n"," ").substring(0, Math.min(200, items.get(0).getText().length()))); } catch (Exception ignored) {}
            }

            List<WebElement> unique = new ArrayList<>();
            for (WebElement it : items) {
                try {
                    String sig = it.getText();
                    if (sig == null) continue;
                    if (seen.contains(sig)) continue;
                    seen.add(sig);
                    unique.add(it);
                } catch (StaleElementReferenceException sere) {
                }
            }

            all.addAll(parsePrices(unique));
             if (!isNextPageAvailable()) break;
             String prev = items.isEmpty() ? null : items.get(0).getText();
             clickNextPageWithWait(prev);
             page++;
         }
         if (all.isEmpty()) {
             String src = driver.getPageSource();
             if (src.contains("rsd")) {
                 List<Integer> fromSrc = extractPricesFromPageSource(src);
                 if (!fromSrc.isEmpty()) return fromSrc;
             }
         }
         return all;
     }

    public List<LocalDate> collectAllDatesForCurrentFilter() {
        List<LocalDate> all = new ArrayList<>();
        int page = 0;
        java.util.Set<String> seen = new java.util.HashSet<>();
        while (true) {
            waitForItemsLoaded();
            ensureItemsPopulated();
            List<WebElement> items = getResultItems();
            System.out.println("DEBUG collectAllDatesForCurrentFilter: page=" + page + " itemsFound=" + items.size());
            if (!items.isEmpty()) {
                try { System.out.println("DEBUG first item text: " + items.get(0).getText().replaceAll("\n"," ").substring(0, Math.min(200, items.get(0).getText().length()))); } catch (Exception ignored) {}
            }
            List<WebElement> unique = new ArrayList<>();
            for (WebElement it : items) {
                try {
                    String sig = it.getText();
                    if (sig == null) continue;
                    if (seen.contains(sig)) continue;
                    seen.add(sig);
                    unique.add(it);
                } catch (StaleElementReferenceException sere) {
                }
            }
            all.addAll(parseDates(unique));
             if (!isNextPageAvailable()) break;
             String prev = items.isEmpty() ? null : items.get(0).getText();
             clickNextPageWithWait(prev);
             page++;
         }
         return all;
     }

    public String fetchRidesJson(String email, int page, int size, String sortBy, String direction, String startDate) {
        String backendBase = System.getProperty("backendBaseUrl", "http://localhost:8080");
        Object res = ((JavascriptExecutor) driver).executeAsyncScript(
                 "var email = arguments[0]; var page = arguments[1]; var size = arguments[2]; var sortBy = arguments[3]; var dir = arguments[4]; var base = arguments[5]; var callback = arguments[arguments.length-1];\n" +
                        "var url = base + '/api/admin/rides/passenger?email=' + encodeURIComponent(email) + '&page=' + page + '&size=' + size;\n" +
                        "var startDate = arguments[6];\n" +
                        "if(startDate) url += '&startDate=' + encodeURIComponent(startDate);\n" +
                        "if (sortBy) url += '&sort=' + encodeURIComponent(sortBy);\n" +
                        "if (dir) url += '&direction=' + encodeURIComponent(dir);\n" +
                         "var headers = {'Content-Type':'application/json'}; var token = window.localStorage.getItem('authToken'); if (token) headers['Authorization'] = 'Bearer ' + token;\n" +
                         "fetch(url, {method:'GET', headers: headers, credentials: 'include'})\n" +
                         "  .then(r => r.text())\n" +
                         "  .then(t => callback(t))\n" +
                         "  .catch(e => callback('ERR:' + e.message));",
                email, page, size, sortBy, direction, backendBase, startDate);
        return res == null ? null : res.toString();
    }

    public String fetchAllPricesFromBackend(String email, String sortBy, String direction, String startDate) {
        String backendBase = System.getProperty("backendBaseUrl", "http://localhost:8080");
        try {
            Object res = ((JavascriptExecutor) driver).executeAsyncScript(
                    "var email=arguments[0]; var sortBy=arguments[1]; var dir=arguments[2]; var startDate=arguments[3]; var base=arguments[4]; var cb=arguments[arguments.length-1];\n" +
                            "var url = base + '/api/admin/rides/passenger?email=' + encodeURIComponent(email) + '&page=0&size=1000';\n" +
                            "if(sortBy) url += '&sort=' + encodeURIComponent(sortBy); if(dir) url += '&direction=' + encodeURIComponent(dir); if(startDate) url += '&startDate=' + encodeURIComponent(startDate);\n" +
                            "var headers={'Content-Type':'application/json'}; var token = window.localStorage.getItem('authToken'); if(token) headers['Authorization']='Bearer ' + token;\n" +
                            "fetch(url, {method:'GET', headers: headers, credentials: 'include'}).then(r=>r.json()).then(j=>{ try{ var arr = (j && j.content) ? j.content.map(function(x){ return Math.round(x.price); }) : []; cb(JSON.stringify(arr)); }catch(e){ cb('ERR:'+e.message);} }).catch(e=>cb('ERR:'+e.message));",
                    email, sortBy, direction, startDate, backendBase);
            if (res == null) return null;
            String s = res.toString();
            if (s.startsWith("ERR:")) {
                System.out.println("DEBUG fetchAllPricesFromBackend: fetch error: " + s);
                return null;
            }
            return s;
        } catch (Exception e) {
            System.out.println("DEBUG fetchAllPricesFromBackend failed: " + e.getMessage());
            return null;
        }
    }

    public void searchWithAll(String email, String userType, String sortBy, String sortDirection, String dateStr) {
        clickReset();
        if (email != null) setEmail(email);
        if (userType != null) setUserType(userType);
        if (sortBy != null) setSortBy(sortBy);
        if (sortDirection != null) setSortDirection(sortDirection);
        if (dateStr != null && !dateStr.isEmpty()) setDate(dateStr);

        clickSearch();
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        longWait.pollingEvery(java.time.Duration.ofMillis(200));
        try {
            longWait.until(d -> {
                try {
                    if (!d.findElements(By.cssSelector(".list .item .card-date")).isEmpty()) return true;
                    if (!d.findElements(By.cssSelector(".list .item .card-price")).isEmpty()) return true;
                    if (!d.findElements(By.cssSelector(".list .item")).isEmpty()) return true;
                    if (!d.findElements(By.xpath("//*[contains(text(),'No rides found for this user.') or contains(text(),'Enter email and search to view rides.')]")) .isEmpty()) return true;
                } catch (Exception e) {
                }
                return false;
            });
        } catch (Exception e) {
            System.out.println("WARN searchWithAll: wait after search timed out: " + e.getMessage());
            dumpDebug("search-with-all-timeout");
        }
    }
}
