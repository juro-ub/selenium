package de.jro.moduls.selenium;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyChromeDriver extends ChromeDriver {

    private static final Logger logger = LoggerFactory.getLogger(MyChromeDriver.class);
    private static final int TIMEOUT_WEBDRIVER = 30;
    private static final int MAX_TRY = 5;
    private int timeout;

    public MyChromeDriver(ChromeOptions capabilities) throws Exception {
        super(capabilities);
    }

    /*
        Returns true if the Browser is closed or not reachable
    */
    public boolean isBrowserClosed() {
        boolean isClosed = false;
        int t = 0;
        if (this.getSessionId() == null) {
            return true;
        }
        try {
            //store the actual timeout
            t = this.getTimeoutOfDriver();
            //try to set timeout
            this.setStdImplicitTimeout();
            //try to getTitle
            this.getTitle();           
        } catch (UnreachableBrowserException ubex) {
            isClosed = true;
        } catch (Exception e) {
            isClosed = true;
        } finally {
            try {
                if (t > 0) {
                    this.setImplicitTimeout(t);
                }
            } catch (Exception e) {
                isClosed = true;
            }
        }
        return isClosed;
    }

    public void acceptAlert() {
        super.switchTo().alert().accept();
    }

    public String captureScreen(String path) throws Exception {
        WebDriver augmentedDriver = new Augmenter().augment(this);
        File source = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
        path = source.getName();
        FileUtils.copyFile(source, new File(path));
        return source.getName();
    }

    /*
        Try to send the value via the built in function sendKeys(...)
    
        On failure, try to change the value via javascript
    */
    public void clearAndSendValue(By id, String val) throws Exception {
        for (int i = 0; i < MAX_TRY; i++) {
            
            if (super.findElements(id).size() == 0) {
                throw new NoSuchElementException("not visible");
            }
            
            //clear field
            String s1 = "arguments[0].value=''";
            super.executeScript(s1, super.findElements(id).get(0));            
            
            try {
                //try to set via action
                super.findElements(id).get(0).sendKeys(val);
                return;
            } catch (Exception e) {
                
            }
            
            try {
                //try to set via javascript
                String s2 = "arguments[0].value='" + val + "'";
                super.executeScript(s2, super.findElements(id).get(0));
                return;
            } catch (Exception e) {
                
            }
        }
    }

    /*
        Click on the first element determined by @param by
    
        Try to perform the click several times if an Exception was thrown
    
        Try to perform a click with javascript if the web element click action fails
    
        return true on success
    */
    public boolean click(By by) throws Exception {
        List<WebElement> ele = new ArrayList<WebElement>();
        int old = this.getTimeoutOfDriver();
        try {
            this.setImplicitTimeout(5);
            ele = super.findElements(by);
        } finally {
            this.setImplicitTimeout(old);
        }
        if (ele.size() > 0) {
            for (int i = 0; i < MAX_TRY; i++) {
                if(click(ele.get(0)))
                    return true;
            }
        }
        return false;
    }

    /*
        Try to click the element again if the element is stale
    
        Try to perform a javascript click if an Exception was thrown
    
        return true on success
    */
    public boolean click(WebElement e) {
        for (int i = 0; i < 5; i++) {
            try {
                Actions builder = new Actions(this);
                builder.moveToElement(e).click(e);
                builder.perform();
                return true;
            } catch (StaleElementReferenceException e1) {

            } catch (Exception e2) {
                try {
                    JavascriptExecutor js = this;
                    js.executeScript(" arguments[0].click();", e);
                    return true;
                } catch (StaleElementReferenceException e1) {

                }
            }
        }
        return false;
    }

    
    /*
        Repeat the click if element is stale
    */
    public boolean clickJavascript(By by) throws Exception {
        List<WebElement> ele = new ArrayList<WebElement>();
        int old = this.getTimeoutOfDriver();
        try {
            this.setImplicitTimeout(5);
            ele = super.findElements(by);
        } catch (Exception e) {

        } finally {
            this.setImplicitTimeout(old);
        }
        if (ele.size() > 0) {
            for (int i = 0; i < MAX_TRY; i++) {
                if (clickJavascript(ele.get(0))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /*
        Repeat the click if element is stale
    */
    public boolean clickJavascript(WebElement e) throws Exception {
        for (int i = 0; i < MAX_TRY; i++) {
            try {
                JavascriptExecutor js = this;
                js.executeScript(" arguments[0].click();", e);
                Thread.sleep(1000);
                return true;
            } catch (StaleElementReferenceException e1) {

            } catch (Exception e2) {
                throw new Exception(e2);
            }
        }
        return false;
    }

    public void dismissAlert() {
        super.switchTo().alert().accept();
    }

    @Override
    public void get(String url) {
        if (url == null || url.length() == 0) {
            logger.error("can not load a empty or null url");
        }
        super.get(url);
    }

    public void get(String url, int timeoutsec) {
        get(url, timeoutsec, 2, null);
    }
    
    public void get(String url, String xpath_check_loaded) {
        get(url, TIMEOUT_WEBDRIVER, 2, xpath_check_loaded);
    }

    public void get(String url, String xpath_check_loaded, int timeoutsec) {
        get(url, timeoutsec, 2, xpath_check_loaded);
    }
    
    /*
        @param timeoutsec page load timeout
        @param url page
        @param max_tries max tries to load the page
        @param xpath_check check for an existing element on the page
    */
    public void get(String url, int timeoutsec, int max_tries, String xpath_check) {
        for (int i = 0; i < max_tries; i++) {
            try {
                manage().timeouts().pageLoadTimeout(timeoutsec, java.util.concurrent.TimeUnit.SECONDS);
                get(url);
                if (xpath_check != null && xpath_check.length() > 0) {
                    findElement(By.xpath(xpath_check));
                }
                return;
            } catch (org.openqa.selenium.TimeoutException e) {
                logger.info("timeout while get url " + "counter: " + i + " < " + max_tries + ", url=" + url);
                if (i == (max_tries - 1)) {
                    logger.error("timeout while get url(max tries reached) " + url);
                }
            } catch (Exception e) {
                logger.error("Exception while get url" + " counter: " + i + " < " + max_tries + ", e: "
                        + e + " for url="
                        + url);
            }
        }
    }

    private By getBy(String key, String value) throws InvocationTargetException, IllegalAccessException {
        Class<By> clazz = By.class;
        String methodName = key.replace(" ", "");
        Method m = getCaseInsensitiveStaticDeclaredMethod(clazz, methodName);
        return (By) m.invoke(null, value);
    }

    private Method getCaseInsensitiveDeclaredMethod(Object obj, String methodName) {
        Method[] methods = obj.getClass().getMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equalsIgnoreCase(methodName)) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new IllegalStateException(String.format("%s Method name is not found for this Class %s", methodName,
                    obj.getClass().toString()));
        }
        return method;
    }

    @SuppressWarnings("rawtypes")
    private Method getCaseInsensitiveStaticDeclaredMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equalsIgnoreCase(methodName)) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new IllegalStateException(
                    String.format("%s Method name is not found for this Class %s", methodName, clazz.toString()));
        }
        return method;
    }

    public int getTimeoutOfDriver() {
        return this.timeout;
    }

    private WebElement getWebElement(Object lastObject, String key, String value) throws Exception {
        WebElement element = null;
        By by = getBy(key, value);
        Method m = getCaseInsensitiveDeclaredMethod(lastObject, "findElement");
        element = (WebElement) m.invoke(lastObject, by);
        return element;
    }

    public boolean isElementStale(WebElement e) {
        try {
            e.isDisplayed();
            return false;
        } catch (StaleElementReferenceException ex) {
            return true;
        }
    }

    public WebElement refreshElement(WebElement elem) throws Exception {
        if (!isElementStale(elem)) {
            return elem;
        }
        Object lastObject = null;
        try {
            String[] arr = elem.toString().split("->");
            new ArrayList<String>();
            for (String s : arr) {
                String newstr = s.trim().replaceAll("^\\[+", "").replaceAll("\\]+$", "");
                String[] parts = newstr.split(": ");
                String key = parts[0];
                String value = parts[1];
                int leftBracketsCount = value.length() - value.replace("[", "").length();
                int rightBracketscount = value.length() - value.replace("]", "").length();
                if (leftBracketsCount - rightBracketscount == 1) {
                    value = value + "]";
                }
                if (lastObject == null) {
                    lastObject = this;
                } else {
                    lastObject = getWebElement(lastObject, key, value);
                }
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw new Exception(e);
        }
        return (WebElement) lastObject;
    }

    /*
        returns true on success
    */
    public boolean removeScreenshot(String path) {
        try {
            File file = new File(path);

            if (file.delete()) {
                logger.info(file.getName() + " is deleted!");
                return true;
            } else {
                logger.error("Delete operation is failed.");
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }

    public void scrollTo(WebElement web, int offsety) throws Exception {
        ((JavascriptExecutor) this).executeScript("arguments[0].scrollIntoView(true);", this.refreshElement(web));
        ((JavascriptExecutor) this).executeScript("window.scrollBy(0," + offsety + ");");
    }

    public void scrollToEndOfPage() throws Exception {
        // Get total height
        By selBy = By.tagName("body");
        int initialHeight = this.findElement(selBy).getSize().getHeight();
        int currentHeight = 0;
        while (initialHeight != currentHeight) {
            initialHeight = this.findElement(selBy).getSize().getHeight();
            // Scroll to bottom
            ((JavascriptExecutor) this).executeScript("scroll(0," + initialHeight + ");");
            currentHeight = this.findElement(selBy).getSize().getHeight();
        }
    }

    public void scrollToTopOfPage() throws Exception {
        WebElement element = findElement(By.tagName("body"));
        JavascriptExecutor js = (JavascriptExecutor) this;
        js.executeScript("arguments[0].scrollIntoView();", element);
    }

    public void setStdImplicitTimeout() throws Exception {
        timeout = MyChromeDriver.TIMEOUT_WEBDRIVER;
        super.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    }

    public void setImplicitTimeout(int seconds) throws Exception {
        timeout = seconds;
        super.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
    }

    public void stopJavascript() throws Exception {        
        JavascriptExecutor js = this;
        js.executeScript("debugger;");
        Thread.sleep(1000);
    }

    public void waitForVisibility(By id) throws Exception {
        this.waitForVisibility(id, TIMEOUT_WEBDRIVER);
    }

    public void waitForVisibility(By id, int timeout) throws Exception {
        try {
            this.setImplicitTimeout(timeout);
            WebDriverWait wait = new WebDriverWait(this, timeout);
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(id));
            if (element == null) {
                throw new Exception("Element not visible");
            }
        } catch (Exception e) {
            throw e;
        } finally {

        }
    }

    public void waitForVisibility(final WebElement webElement) throws Exception {
        int old = this.getTimeoutOfDriver();
        try {
            this.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(this, TIMEOUT_WEBDRIVER);
            ExpectedCondition<Boolean> elementIsDisplayed = new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver arg0) {
                    try {
                        webElement.isDisplayed();
                        return true;
                    } catch (NoSuchElementException e) {
                        return false;
                    } catch (StaleElementReferenceException f) {
                        return false;
                    }
                }
            };
            wait.until(elementIsDisplayed);
            this.manage().timeouts().implicitlyWait(TIMEOUT_WEBDRIVER, TimeUnit.SECONDS);
        } finally {
            this.setImplicitTimeout(old);
        }
    }

    public void zoomOut(double ratio) throws Exception {
        NumberFormat formatter = new DecimalFormat("#0.0");
        JavascriptExecutor executor = this;
        executor.executeScript("document.body.style.zoom = '" + formatter.format(ratio) + "'");
    }

    public void zoomOutKeyboard(int count_step) throws Exception {
        WebElement html = this.findElement(By.tagName("html"));
        this.refreshElement(html).sendKeys(Keys.chord(Keys.CONTROL, "0"));
        for (int i = 0; i < count_step; i++) {
            this.refreshElement(html).sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
        }
    }

}
