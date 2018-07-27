package plugins.executers.selenide;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import plugins.executers.selenide.templates.WebDriverName;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;

/**
 * @author nechkin.sergei.sergeevich
 * Класс для получения Web-драйвера.
 */
public class WebDriverGetter {
    private String driverName = "";
    private String reportDirectory = "";
    private PhantomJSDriver phantomDriver;
    private WebDriver webDriver;

    public WebDriverGetter(String programFilesDirectory) {
        this.reportDirectory = programFilesDirectory;
    }

    public String getDriverName() {
        for (WebDriverName webDriverName : getArrayDriverName()) {
            if (new File(webDriverName.fileName).isFile()) {
                driverName = getNameBrowserOfDriverName(webDriverName.driverName);
                return driverName.toUpperCase();
            }
        }
        if ((new File("phantomjs.exe").isFile() || new File("phantomjs").isFile())) {
            driverName = getNameBrowserOfDriverName("phantomjs");
            return driverName.toUpperCase();
        }
        return driverName.toUpperCase();
    }

    private ArrayList<WebDriverName> getArrayDriverName() {
        ArrayList<WebDriverName> webDriverNames = new ArrayList<>();
        webDriverNames.add(new WebDriverName("chrome", "chromedriver.exe", "webdriver.chrome.driver"));
        webDriverNames.add(new WebDriverName("chrome", "chromedriver", "webdriver.chrome.driver"));
        webDriverNames.add(new WebDriverName("edge", "MicrosoftWebDriver.exe", "webdriver.edge.driver"));
        webDriverNames.add(new WebDriverName("edge", "MicrosoftWebDriver", "webdriver.edge.driver"));
        webDriverNames.add(new WebDriverName("marionette", "geckodriver.exe", "webdriver.gecko.driver"));
        webDriverNames.add(new WebDriverName("marionette", "geckodriver", "webdriver.gecko.driver"));
        //webDriverNames.add(new WebDriverName("opera", "operadriver", "webdriver.opera.driver"));
        return webDriverNames;
    }

    public void closeDriver() {
        if (driverName != "") {

            try {
                getWebDriver().close();
            } catch (Exception ignored) {
            }

            try {
                getWebDriver().quit();
            } catch (Exception ignored) {
            }
        }
    }

    public String getNameBrowserOfDriverName(String driverName) {
        switch (driverName) {
            case "chrome":
                return "GOOGLE CHROME";
            case "phantomjs":
                return "GOOGLE CHROME";
            case "edge":
                return "IE";
            case "marionette":
                return "FIREFOX";
        }
        return "";
    }

    public void getWebDriverFormFile() {
        /***Disabled log log4j***/
        org.apache.log4j.Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(org.apache.log4j.Level.OFF);
        org.apache.log4j.Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(org.apache.log4j.Level.OFF);
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        /***********************/

        //System.setProperty("webdriver.chrome.logfile", "\\path\\chromedriver.log");
        //System.setProperty("webdriver.chrome.driver", "\\path\\chromedriver.exe");
        //System.setProperty("webdriver.chrome.args", "--disable-logging");
        System.setProperty("webdriver.chrome.args", "--silent");
        System.setProperty("webdriver.chrome.silentOutput", "true");

        //new ChromeDriver(new ChromeDriverService.Builder().withSilent(true).build());

        Configuration.reportsFolder = null;// reportDirectory + File.separator + "selenide";
        Configuration.browserSize = "1920x1080";
        Configuration.savePageSource = false;

        boolean weDriverPress = false;

        for (WebDriverName webDriverName : getArrayDriverName()) {
            if (new File(webDriverName.fileName).isFile() && !weDriverPress) {
                Configuration.browser = webDriverName.driverName;
                System.setProperty(webDriverName.fileFullName, webDriverName.fileName);
                weDriverPress = true;
                driverName = getNameBrowserOfDriverName(webDriverName.driverName);
                break;
            }
        }

        if ((new File("phantomjs.exe").isFile() || new File("phantomjs").isFile()) && !weDriverPress) {
            DesiredCapabilities caps = new DesiredCapabilities();

            caps.setJavascriptEnabled(true);

            //caps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_PATH_PROPERTY, "phantomjs.exe");

            String[] phantomArgs = new String[]{
                    "--webdriver-loglevel=NONE",
                    "--disk-cache=false"
            };
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);

            try {
                //PhantomJSDriverService.createDefaultService(caps);
                if (phantomDriver == null || !WebDriverRunner.hasWebDriverStarted()) {
                    closeDriver();
                    phantomDriver = new PhantomJSDriver(caps);
                    Configuration.browser = "phantomjs";

                    setWebDriver(phantomDriver);
                }

            } catch (Exception ignored) {
            }

            try {
                DesiredCapabilities.phantomjs().setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
            } catch (Exception ignored) {
            }

            driverName = getNameBrowserOfDriverName("phantomjs");
            //Configuration.browser="phantomjs";
            //getWebDriver().manage().window().fullscreen();
            getWebDriver().manage().window().setSize(new Dimension(1920, 1080));
            getWebDriver().manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
            getWebDriver().manage().timeouts().setScriptTimeout(10, TimeUnit.MILLISECONDS);
            getWebDriver().manage().timeouts().pageLoadTimeout(50000, TimeUnit.MILLISECONDS);
            weDriverPress = true;
        }

        /*if (!weDriverPress) {
            log.info("");
            log.info("	----------------------------------------------------------------------------");
            log.info("	 Missing WEB driver. Copy the file 'webdrivername'.exe in current directory");
            log.info("	 to download the correct driver from the portal");
            log.info("	 http://www.seleniumhq.org/download");
            log.info("	 for firefox 				download geckodriver.exe");
            log.info("	 for Google Chrome 			download chromedriver.exe");
            log.info("	 for Microsoft Edge Driver	download MicrosoftWebDriver.exe");
            log.info("	---------------------------------------------------------------------------");
            log.info("");
        }*/

        /*//EventFiringWebDriver driver = new EventFiringWebDriver(new InternetExplorerDriver());
        WebDriverEventListener errorListener = new AbstractWebDriverEventListener() {
            @Override
            public void onException(Throwable throwable, WebDriver driver) {

            }
        };
        EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverRunner.getWebDriver());
        driver.register(errorListener);
        Selenide.screenshot()
        WebDriverRunner.getWebDriver().
        WebDriverRunner.setWebDriver(driver);
        //WebDriverRunner..addListener(errorListener);*/

        try {
            Logger.getLogger(Selenide.class.getName()).setLevel(Level.OFF);
        } catch (Exception ignored) {
        }

        try {
            Logger.getLogger(getWebDriver().manage().getClass().getName()).setLevel(Level.OFF);
        } catch (Exception ignored) {
        }
    }

    public WebDriver getDriver() {
        if (webDriver == null) {
            getWebDriverFormFile();
            webDriver = getWebDriver();
        }
        return webDriver;
    }
}
