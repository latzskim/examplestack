package com.simpleshop.e2e.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "e2e"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class E2EBaseTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("simpleshop_e2e")
            .withUsername("test")
            .withPassword("test");

    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    void setUpPlaywright() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    void tearDownPlaywright() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUpBrowserContext() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDownBrowserContext() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected void navigateTo(String path) {
        page.navigate(baseUrl() + path);
    }

    protected void loginAs(String email, String password) {
        navigateTo("/login");
        page.locator("#username").fill(email);
        page.locator("#password").fill(password);
        page.locator("button[type='submit']").click();
        page.waitForURL(url -> !url.contains("/login"));
    }

    protected void registerUser(String email, String password, String firstName, String lastName) {
        navigateTo("/register");
        page.locator("#firstName").fill(firstName);
        page.locator("#lastName").fill(lastName);
        page.locator("#email").fill(email);
        page.locator("#password").fill(password);
        page.locator("button[type='submit']").click();
        page.waitForURL(url -> !url.contains("/register"));
    }
}
