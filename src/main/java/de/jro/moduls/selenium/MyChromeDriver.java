package de.jro.moduls.selenium;

import java.io.File;
import java.io.IOException;
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
    private int timeout;

    public MyChromeDriver(ChromeOptions capabilities) throws Exception {
        super(capabilities);
    }

    public boolean isBrowserClosed() {
        boolean isClosed = false;
        int t = 0;
        if (this.getSessionId() == null) {
            return true;
        }
        try {
            t = this.getTimeoutOfDriver();
            this.setStdImplicitTimeout();
            this.getTitle();
            super.get("www.google.de");
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
                // TODO Auto-generated catch block
                e.printStackTrace();
                isClosed = true;
            }
        }
        return isClosed;
    }

    public void acceptAlert() {
        try {
            super.switchTo().alert().accept();
        } catch (Exception e) {

        }
    }

    public String captureScreen() throws Exception {
        String path = "";
        try {
            WebDriver augmentedDriver = new Augmenter().augment(this);
            File source = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
            path = source.getName();
            FileUtils.copyFile(source, new File(path));
            return source.getName();
        } catch (IOException e) {
            path = "Failed to capture screenshot: "
                    + e;
            return "";
        }

    }

    public void clear_and_sendvalue(By id, String val) throws Exception {
        try {
            for (int i = 0; i < 5; i++) {
                if (super.findElements(id).size() == 0) {
                    throw new NoSuchElementException("not visible");
                }
                try {
                    // super.findElements(id).get(0).clear();
                    String s1 = "arguments[0].value=''";
                    super.executeScript(s1, super.findElements(id).get(0));
                    Thread.sleep(1000);
                    super.findElements(id).get(0).sendKeys(val);
                    return;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            String s2 = "arguments[0].value='" + val + "'";
            super.executeScript(s2, super.findElements(id).get(0));
            return;
        } catch (Exception e) {
            throw new Exception(e);
        } finally {

        }
    }

    public boolean click(By by) throws Exception {
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
            for (int i = 0; i < 5; i++) {
                try {
                    Actions builder = new Actions(this);
                    builder.moveToElement(super.findElement(by)).click(super.findElement(by));
                    builder.perform();
                    return true;
                } catch (StaleElementReferenceException e1) {

                } catch (Exception e) {
                    try {
                        JavascriptExecutor js = this;
                        js.executeScript(" arguments[0].click();", super.findElement(by));
                        return true;
                    } catch (StaleElementReferenceException e1) {

                    }

                }
            }
        }
        return false;

    }

    public boolean click(WebElement e) throws Exception {

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
            for (int i = 0; i < 5; i++) {
                try {
                    JavascriptExecutor js = this;
                    js.executeScript(" arguments[0].click();", super.findElement(by));
                    Thread.sleep(1000);
                    return true;
                } catch (StaleElementReferenceException e1) {

                } catch (Exception e) {

                    throw new Exception(e);
                }
            }
        }
        return false;

    }

    public boolean clickJavascript(WebElement e) throws Exception {
        for (int i = 0; i < 5; i++) {
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
        try {
            super.switchTo().alert().accept();
        } catch (Exception e) {

        }
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

    public void get(String url, int timeoutsec, int max_tries, String xpath_check) {
        for (int i = 0; i < max_tries; i++) {
            try {
                manage().timeouts().pageLoadTimeout(timeoutsec, java.util.concurrent.TimeUnit.SECONDS);
                get(url);
                // if (i > 1)
                // navigate().refresh();
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
                logger.error("ex while get url" + " counter: " + i + " < " + max_tries + ", e: "
                        + e + " for url="
                        + url);
            }
        }
    }

    public void get(String url, String xpath_check_loaded) {
        get(url, TIMEOUT_WEBDRIVER, 2, xpath_check_loaded);
    }

    public void get(String url, String xpath_check_loaded, int timeoutsec) {
        get(url, timeoutsec, 2, xpath_check_loaded);
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

    private WebElement getWebElement(Object lastObject, String key, String value) {
        WebElement element = null;
        try {
            By by = getBy(key, value);
            Method m = getCaseInsensitiveDeclaredMethod(lastObject, "findElement");
            element = (WebElement) m.invoke(lastObject, by);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

    @Override
    public void quit() {
        try {
            if (this.getSessionId() != null) {
                super.quit();
            }
        } catch (Exception e) {
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

    public boolean removeScreenshot(String name) throws Exception {
        try {
            String path = name;
            File file = new File(path);

            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
                return true;
            } else {
                System.out.println("Delete operation is failed.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void scrollTo(WebElement web, int offsety) throws Exception {
        ((JavascriptExecutor) this).executeScript("arguments[0].scrollIntoView(true);", this.refreshElement(web));
        ((JavascriptExecutor) this).executeScript("window.scrollBy(0," + offsety + ");");
        Thread.sleep(500);
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
            Thread.sleep(2000);
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

    public void stopJavascript() {
        try {
            JavascriptExecutor js = this;
            js.executeScript("debugger;");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitForVisible(By id) throws Exception {
        this.waitForVisible(id, TIMEOUT_WEBDRIVER);
    }

    public void waitForVisible(By id, int max_sec) throws Exception {
        try {
            this.setImplicitTimeout(max_sec);
            WebDriverWait wait = new WebDriverWait(this, max_sec);
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(id));
            if (element == null) {
                throw new Exception("not visible");
            }
        } catch (Exception e) {
            throw e;
        } finally {

        }
        return;
    }

    public void waitForVisible(final WebElement webElement) throws Exception {
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
