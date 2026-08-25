package com.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginTest {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            System.out.println("========== START LOGIN TEST SUITE ==========\n");

            testValidLogin(browser);
            testInvalidLogin(browser);
            testMissingPassword(browser);
            testSqlInjection(browser);
            testInputSpace(browser);
            testMissingUsername(browser);
            testPasswordMasking(browser);
            testLockedOutUser(browser);
            testCaseSensitivity(browser);
            testTabKeyNavigation(browser);
            testXssinjection(browser);
             testEnterkeyLogin( browser);

            System.out.println("\n========== TEST SUITE COMPLETED ==========");
            browser.close();
        } catch (Exception e) {
            System.err.println("An error occurred during Playwright initialization.");
            e.printStackTrace();
        }
    }

    private static void testValidLogin(Browser browser) {
        System.out.println("--- Test 1: Successful Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            page.locator("[data-test='login-button']").click();

            assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
            System.out.println("=> TEST PASSED: Login successful!\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 1.");
            e.printStackTrace();
        }
    }

    private static void testInvalidLogin(Browser browser) {
        System.out.println("--- Test 2: Invalid Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("invalid.password"));
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            assertThat(errorMessage).isVisible();
            assertThat(errorMessage).containsText("Epic sadface: Username and password do not match any user in this service");
            
            System.out.println("=> TEST PASSED: The system blocked the login and displayed the correct error message!\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 2.");
        }
    }

    private static void testMissingPassword(Browser browser) {
        System.out.println("--- Test 3: Empty Password ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
            page.locator("[data-test='password']").fill("");
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            if (errorText.contains("Password is required")) {
                System.out.println("=> TEST PASSED: The system successfully blocked and reported the missing password error correctly.\n");
            } else {
                System.out.println("=> TEST FAILED: Incorrect error message or system handled it incorrectly.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 3.");
        }
    }

    private static void testSqlInjection(Browser browser) {
        System.out.println("--- Test 4: SQL Injection Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            String sqlInjectionPayload = ConfigReader.getProperty("sql.injection.payload"); 
            page.locator("[data-test='username']").fill(sqlInjectionPayload);
            page.locator("[data-test='password']").fill("random_password");
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            if (errorText.contains("Epic sadface")) {
                System.out.println("=> TEST PASSED: The system is secure, intercepted the SQL Injection attempt.\n");
            } else {
                System.out.println("=> TEST FAILED: Warning! The system might have been bypassed or handled it incorrectly.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 4.");
        }
    }

    private static void testInputSpace(Browser browser) {
        System.out.println("--- Test 5: Continuous Spaces Input ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            page.locator("[data-test='username']").fill("          ");
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            if (errorText.contains("Epic sadface")) {
                System.out.println("=> TEST PASSED: The system successfully blocked login with spaces.\n");
            } else {
                System.out.println("=> TEST FAILED: System behavior is not as expected.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 5.");
        }
    }
    
    private static void testMissingUsername(Browser browser) {
        System.out.println("--- Test 6: Empty Username ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            page.locator("[data-test='username']").fill("");
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            if (errorText.contains("Username is required")) {
                System.out.println("=> TEST PASSED: The system reported the missing Username error correctly.\n");
            } else {
                System.out.println("=> TEST FAILED: Incorrect error message or system handled it incorrectly.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in the empty Username scenario.");
        }
    }

    private static void testPasswordMasking(Browser browser) {
        System.out.println("--- Test 7: Check Password Masking ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            String typeAttribute = page.locator("[data-test='password']").getAttribute("type");
            
            if ("password".equals(typeAttribute)) {
                System.out.println("=> TEST PASSED: The interface encrypted the display correctly (type='password').\n");
            } else {
                System.out.println("=> TEST FAILED: WARNING! The password field is not set to type='password'.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 7.");
        }
    }
    private static void testLockedOutUser(Browser browser) {
        System.out.println("--- Test 8: Locked Out User Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            // Fetch both the locked out username and valid password from config.properties
            page.locator("[data-test='username']").fill(ConfigReader.getProperty("locked.username"));
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            if (errorText.contains("Sorry, this user has been locked out.")) {
                System.out.println("=> TEST PASSED: The system successfully blocked the locked-out user.\n");
            } else {
                System.out.println("=> TEST FAILED: Incorrect error message or system allowed the locked-out user to log in.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 8.");
            e.printStackTrace();
        }
    }
    private static void testCaseSensitivity(Browser browser) {
        System.out.println("--- Test 9: Case Sensitivity Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            // Fetch the valid username and change its casing (e.g., to UPPERCASE)
            String caseAlteredUsername = ConfigReader.getProperty("valid.username").toUpperCase();
            
            page.locator("[data-test='username']").fill(caseAlteredUsername);
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            // Verify that the system treats the wrong casing as an invalid login
            if (errorText.contains("Epic sadface: Username and password do not match any user in this service")) {
                System.out.println("=> TEST PASSED: The system is case-sensitive and correctly blocked the login.\n");
            } else {
                System.out.println("=> TEST FAILED: The system might not be case-sensitive or handled the error incorrectly.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 9.");
            e.printStackTrace();
        }
    }
    private static void testTabKeyNavigation(Browser browser) {
        System.out.println("--- Test 10: Tab Key Navigation ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            // Focus on the username field and press Tab to navigate to the password field
            page.locator("[data-test='username']").focus();
            
            page.keyboard().press("Tab");

            // Check if the password field is focused
             assertThat(page.locator("[data-test='password']")).isFocused();
            page.keyboard().press("Tab");
            // Check if the login button is focused
            assertThat(page.locator("[data-test='login-button']")).isFocused();
            System.out.println("=> TEST PASSED: Tab key navigation works correctly.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 10.");
            e.printStackTrace();
        }catch (AssertionError e) {
            System.err.println("=> TEST FAILED: An assertion error occurred in Test 10.");
            e.printStackTrace();
        }

    }
    private static void testXssinjection(Browser browser) {
        System.out.println("--- Test 11: XSS Injection Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            String xssInjectionPayload = ConfigReader.getProperty("xss.injection.payload"); 
            page.locator("[data-test='username']").fill(xssInjectionPayload);
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            page.locator("[data-test='login-button']").click();

            Locator errorMessage = page.locator("[data-test='error']");
            errorMessage.waitFor(); 
            String errorText = errorMessage.textContent();

            if (errorText.contains("Epic sadface")) {
                System.out.println("=> TEST PASSED: The system is secure, intercepted the XSS Injection attempt.\n");
            } else {
                System.out.println("=> TEST FAILED: Warning! The system might have been bypassed or handled it incorrectly.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 11.");
            e.printStackTrace();
        }
    }
    private static void testEnterkeyLogin(Browser browser) {
        System.out.println("--- Test 12: Enter Key Login ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            page.navigate(ConfigReader.getProperty("base.url"));

            page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            // Press Enter key to submit the form
            page.keyboard().press("Enter");

            assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
            System.out.println("=> TEST PASSED: Login successful using Enter key!\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 12.");
            e.printStackTrace();
        }
    }
}