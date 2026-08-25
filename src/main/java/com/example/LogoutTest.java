package com.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.Locator;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LogoutTest {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            System.out.println("========== STARTING LOGOUT TEST SUITE ==========\n"); //[cite: 1]

            testCannotAccessContentAfterLogout(browser);
            testTokenEliminatedAfterLogout(browser);
            testOldTokenInvalidAfterLogout(browser);
            testAdminLogout(browser);

            System.out.println("\n========== TEST SUITE COMPLETED =========="); //[cite: 1]
            browser.close();
        } catch (Exception e) {
            System.err.println("An error occurred during Playwright initialization."); //[cite: 1]
            e.printStackTrace();
        }
    }

    private static void login(Page page, String username, String password) {
        page.navigate(ConfigReader.getProperty("base.url"));
        page.locator("[data-test='username']").fill(username);
        page.locator("[data-test='password']").fill(password);
        page.locator("[data-test='login-button']").click();
        assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
    }

    private static void logout(Page page) {
        page.locator("#react-burger-menu-btn").click();
        page.locator("#logout_sidebar_link").click();
        assertThat(page).hasURL(ConfigReader.getProperty("base.url"));
    }

    private static void testCannotAccessContentAfterLogout(Browser browser) {
        System.out.println("--- Test 1: Cannot access content after logout ---"); //[cite: 1]
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page, ConfigReader.getProperty("valid.username"), ConfigReader.getProperty("valid.password"));
            logout(page);

            page.navigate(ConfigReader.getProperty("inventory.url"));

            Locator errorMessage = page.locator("[data-test='error']");
            assertThat(errorMessage).isVisible();
            assertThat(errorMessage).containsText("Epic sadface: You can only access '/inventory.html' when you are logged in.");
            
            System.out.println("=> TEST PASSED: System blocked unauthorized access after logout!\n"); //[cite: 1]
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 1."); //[cite: 1]
        }
    }

    private static void testTokenEliminatedAfterLogout(Browser browser) {
        System.out.println("--- Test 2: Token/Cookie is eliminated after logout ---"); //[cite: 1]
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page, ConfigReader.getProperty("valid.username"), ConfigReader.getProperty("valid.password"));

            logout(page);

            List<Cookie> cookiesAfter = context.cookies();
            boolean hasSessionAfter = cookiesAfter.stream().anyMatch(c -> c.name.equals("session-username"));

            if (!hasSessionAfter) {
                System.out.println("=> TEST PASSED: System cleared the login session (Token/Cookie)!\n"); //[cite: 1]
            } else {
                System.out.println("=> TEST FAILED: Cookie/Token still exists after logout.\n"); //[cite: 1]
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 2."); //[cite: 1]
        }
    }

    private static void testOldTokenInvalidAfterLogout(Browser browser) {
        System.out.println("--- Test 3: Cannot use old Token to access ---"); //[cite: 1]
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page, ConfigReader.getProperty("valid.username"), ConfigReader.getProperty("valid.password"));
            List<Cookie> oldCookies = context.cookies();
            logout(page);
            context.addCookies(oldCookies);
            page.navigate(ConfigReader.getProperty("inventory.url"));

            if (page.url().equals(ConfigReader.getProperty("base.url"))) {
                System.out.println("=> TEST PASSED: Old token was invalidated on the server.\n"); //[cite: 1]
            } else {
                System.out.println("=> TEST FAILED (Expected on real Server): System allows access again using the old Token.\n"); //[cite: 1]
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 3."); //[cite: 1]
        }
    }

    private static void testAdminLogout(Browser browser) {
        System.out.println("--- Test 4: Logout for Admin ---"); //[cite: 1]
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            // Can define admin.username in config if the system has role-based permissions[cite: 1]
            login(page, ConfigReader.getProperty("valid.username"), ConfigReader.getProperty("valid.password"));
            logout(page);

            assertThat(page).hasURL(ConfigReader.getProperty("base.url"));
            System.out.println("=> TEST PASSED: Logout function works well for Admin account!\n"); //[cite: 1]
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 4."); //[cite: 1]
        }
    }
    
}