package paymelater.webtests;

/*
 ** DO NOT CHANGE!!
 */

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import paymelater.server.WeShareServer;
import paymelater.webtests.pages.AbstractPage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class that sets up the browser driver and has common user steps.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class WebTestRunner {
    protected WeShareServer server;
    protected String baseUrl;
    protected ChromeDriver driver;
    protected AShot aShot;

    // set up the screenshot directory
    Path screenshotDirectory;

    {
        try {
            screenshotDirectory = Paths.get("target", "screenshots");
            if (!Files.exists(screenshotDirectory)) Files.createDirectory(screenshotDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // each test can specify their own tests data
    protected abstract void setupTestData();

    @BeforeAll
    void startServer() {
        server = new WeShareServer();
        server.start(0);
        baseUrl = "http://localhost:" + server.port();
        setupTestData();
    }

    @AfterEach
    void kill() {
        driver.quit();
    }

    @BeforeEach
    void start() {
        WebDriverManager.chromedriver().setup();
        WebDriverManager.firefoxdriver();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox"); // necessary for grading environment
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");
        options.addArguments("--start-fullscreen");
        driver = new ChromeDriver(options);
        aShot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(500));
    }

    @AfterAll
    void stopServer() {
        server.stop();
    }

    public WebDriver driver() {
        return driver;
    }

    /**
     * Tell the browser to go to a specific page
     *
     * @param page the page
     */
    public void navigateTo(AbstractPage page) {
        driver.get(appUrl(page.path()));
    }

    /**
     * Get the full URL for a page, including protocol, server, port, etc.
     *
     * @param pageUrl the url of the page
     * @return the full URL of the page
     */
    protected String appUrl(String pageUrl) {
        return baseUrl + pageUrl;
    }

    /**
     * Check that the browser is on the correct page
     *
     * @param page The expected page
     */
    public void shouldBeOnPage(AbstractPage page) {
        assertThat(currentPath()).isEqualToIgnoringCase(page.path());
    }

    /**
     * Take a screenshot of the webpage
     *
     * @param filename filename of the image
     * @throws IOException could not write the file
     */
    protected void takeScreenshot(String filename) throws IOException {
        Path destinationFile = createScreenshotFile(filename);
        Screenshot shot = aShot.takeScreenshot(driver);
        ImageIO.write(shot.getImage(), "PNG", destinationFile.toFile());
    }

    /**
     * Get the path and query string from a full URI
     *
     * @param uri the URI to check
     * @return the path and query string of the URI
     */
    protected String pathAndQueryString(String uri) {
        try {
            URI currentUri = new URI(uri);
            if (Objects.isNull(currentUri.getQuery())) {
                return currentUri.getPath();
            } else {
                return currentUri.getPath() + "?" + currentUri.getQuery();
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not parse [ " + driver.getCurrentUrl() + " ] to valid URI");
        }
    }

    private String currentPath() {
        return pathAndQueryString(driver.getCurrentUrl());
    }

    private Path createScreenshotFile(String filename) throws IOException {
        Path testDirectory = getTestScreenshotDirectory();
        return Paths.get(testDirectory.toFile().getPath(), filename + ".png");
    }

    @NotNull
    private Path getTestScreenshotDirectory() throws IOException {
        String currentTest = Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(e -> e.getClassName().equals(getClass().getCanonicalName()))
                .findFirst().map(StackTraceElement::getMethodName).orElseThrow();
        Path testDirectory = Paths.get(screenshotDirectory.toFile().getPath(), currentTest);
        if (!Files.exists(testDirectory)) Files.createDirectory(testDirectory);
        return testDirectory;
    }
}

