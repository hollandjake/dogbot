package com.hollandjake.dogbot.controller;

import com.hollandjake.dogbot.model.Human;
import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.service.HumanService;
import com.hollandjake.dogbot.service.ThreadService;
import com.hollandjake.dogbot.util.BrowserStatus;
import com.hollandjake.dogbot.util.Clipbot;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.hollandjake.dogbot.util.CONSTANTS.MESSENGER_URL;
import static com.hollandjake.dogbot.util.XPATHS.*;

@Slf4j
@Controller
public class WebController {
    private final WebDriver driver;

    private final Environment env;
    private final WebDriverWait wait;
    private final WebDriverWait miniWait;
    private final HumanService humanService;
    private final Clipbot clipbot;

    @Getter
    @Setter
    private BrowserStatus status;
    @Getter
    private Thread thread;
    @Getter
    private Human me;

    @Autowired
    public WebController(Environment env, ThreadService threadService, HumanService humanService) {
        this.env = env;
        this.clipbot = new Clipbot(Toolkit.getDefaultToolkit().getSystemClipboard());
        this.status = BrowserStatus.INIT_DRIVER;
        this.humanService = humanService;
        this.thread = threadService.findByUrl(env.getRequiredProperty("thread.name"));

        String browser = env.getProperty("automation.browser", "chrome");
        String driverPath = env.getProperty("driver.path");
        switch (browser) {
            case "chrome":
                if (driverPath != null && !driverPath.isEmpty()) {
                    System.setProperty("webdriver.chrome.driver", driverPath);
                }
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(
                        "--log-level=3",
                        "--silent",
                        "--lang=en-GB",
                        "--mute-audio",
                        "--disable-infobars",
                        "--disable-notifications");
                this.driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                if (driverPath != null && !driverPath.isEmpty()) {
                    System.setProperty("webdriver.gecko.driver", driverPath);
                }
                FirefoxOptions options = new FirefoxOptions();
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("app.update.enabled", false);
                profile.setPreference("media.volume_scale", "0.0");
                profile.setPreference("intl.accept_languages", "en_gb");
                options.setLogLevel(FirefoxDriverLogLevel.FATAL);

                options.setProfile(profile);
                this.driver = new FirefoxDriver(options);
                break;
            default:
                throw new InvalidConfigurationPropertyValueException("automation.browser",
                        browser,
                        "not supported type must be \"chrome\" or \"firefox\" defaults to \"chrome\"");
        }

        this.wait = new WebDriverWait(driver, 30L, 10);
        this.miniWait = new WebDriverWait(driver, 5L, 10);
        Runtime.getRuntime().addShutdownHook(new java.lang.Thread(this::close));
        this.status = BrowserStatus.INIT_DRIVER_COMPLETE;
    }

    public void open() {
        if (status.equals(BrowserStatus.INIT_DRIVER_COMPLETE)) {
            status = BrowserStatus.INIT_BROWSER;
            driver.get(MESSENGER_URL + thread.getUrl());
            status = BrowserStatus.INIT_BROWSER_COMPLETE;
        }
    }

    public void login() {
        status = BrowserStatus.LOGGING_IN;
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_EMAIL)))
                .sendKeys(env.getRequiredProperty("messenger.email"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_PASS)))
                .sendKeys(env.getRequiredProperty("messenger.password"));

        driver.findElement(By.xpath(LOGIN)).click();
        wait.until(ExpectedConditions.urlToBe(MESSENGER_URL + thread.getUrl()));
        status = BrowserStatus.LOGGING_IN_COMPLETE;
    }

    public Human initMe() {
        status = BrowserStatus.GATHERING_DETAILS;
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(SETTING_COG)))
                .click();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(SETTINGS_DROPDOWN)))
                .click();

        String myName = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(MY_REAL_NAME)))
                .getText();

        driver.findElement(By.xpath(SETTINGS_DONE)).click();
        status = BrowserStatus.GATHERING_DETAILS_COMPLETE;
        return humanService.save(Human.builder().name(myName).build());
    }

    public void close() {
        if (!status.equals(BrowserStatus.CLOSED)) {
            driver.quit();
            status = BrowserStatus.CLOSED;
        }
    }

    public List<WebElement> getMessages() {
        return driver.findElements(By.xpath(OTHERS_MESSAGES));
    }

    public void waitForMessagesToLoad() {
        wait.until(ExpectedConditions.and(
                ExpectedConditions.invisibilityOfElementLocated(By.xpath(MESSAGE_LOADING_WHEEL)),
                ExpectedConditions.visibilityOfElementLocated(By.xpath(OTHERS_MESSAGES))
        ));
    }

    public void waitForMessagesToChange(int initialSize) {
        while (getMessages().size() == initialSize) {
            try {
                wait.until(ExpectedConditions.and(
                        ExpectedConditions.invisibilityOfElementLocated(By.xpath(MESSAGE_LOADING_WHEEL)),
                        ExpectedConditions.not(
                                ExpectedConditions.numberOfElementsToBe(By.xpath(OTHERS_MESSAGES), initialSize)
                        ))
                );
            } catch (Exception ignored) {
                log.debug("Timeout expired waiting for messages");
            }
        }
    }

    public boolean isStartOfChat() {
        int preScroll = getMessages().size();
        scrollToTop();
        try {
            miniWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MESSAGE_LOADING_WHEEL)));
            return false;
        } catch (TimeoutException e) {
            int postScroll = getMessages().size();
            return preScroll == postScroll && atTop();
        }
    }

    public void scrollToElement(WebElement element) {
        if (element != null) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        }
    }

    public boolean atTop() {
        String returnValue = String.valueOf(
                ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].scrollTop == 0;",
                        driver.findElement(By.xpath(MESSAGE_SCROLL_AREA)))
        );
        return Boolean.parseBoolean(
                returnValue
        );
    }

    public void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = 0;",
                driver.findElement(By.xpath(MESSAGE_SCROLL_AREA)));
    }

    public void scrollToMessage(Message message) {
        if (message != null) {
            scrollToElement(message.getWebElement());
        }
    }

    public void restart() {
        if (status == BrowserStatus.INIT_DRIVER_COMPLETE) {
            open();
            login();
            this.me = initMe();
        } else {
            log.info("Refreshing window");
            status = BrowserStatus.INIT_BROWSER;
            driver.navigate().refresh();
            status = BrowserStatus.INIT_BROWSER_COMPLETE;
        }
        waitForMessagesToLoad();
        status = BrowserStatus.AWAITING_MESSAGE;
    }

    public void screenshot() {
        try {
            File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String destination = env.getProperty("screenshot.location");
            File target;
            if (destination != null && destination.isEmpty()) {
                target = File.createTempFile("screenshot", new Date().toString(), new File(destination));
            } else {
                target = File.createTempFile("screenshot", new Date().toString());
            }
            FileCopyUtils.copy(file, target);
            log.info("Copied screenshot to {}", target);
        } catch (IOException e) {
            log.error("Failed to take screenshot", e);
        }
    }

    public void sendMessage(Message message) {
        WebElement inputBox = driver.findElement(By.xpath(INPUT_BOX));

        //Send message
        message.send(inputBox, wait, clipbot);
    }

    public void jiggle(WebElement webElement) {
        Actions actions = new Actions(driver);
        actions.moveToElement(webElement).perform();
        actions.moveToElement(driver.findElement(By.xpath(INPUT_BOX))).perform();
    }

    public void highlightElement(WebElement webElement) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style','border: 2px dotted red;');",
                webElement);
    }

    public int getNumMessages() {
        return getMessages().size();
    }
}
