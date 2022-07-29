package test.de.jro.moduls.selenium;

import utils.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import de.jro.moduls.selenium.*;


public class ModulSelenium {

    @Test
    public void test() {
        Properties prop;
        try {
            prop = new Properties();
            String path = prop.getFileChrome();
            System.setProperty("webdriver.chrome.driver", path);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getLocalizedMessage());
        }
        
        MyChromeDriver driver = null;
        WebDriverChromeFactory factory = new WebDriverChromeFactory();
        try {
            driver = (MyChromeDriver) factory.getWebDriver(false, true);
            driver.get("https://google.de");
        } catch (Exception ex) {
            fail(ex.getLocalizedMessage());
        }finally{
            driver.quit();
        }
    }
}
