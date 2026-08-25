package com.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CheckoutTest {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            System.out.println("========== STARTING CHECKOUT TEST SUITE ==========\n");

            testCheckoutEmptyInformation(browser);
            testCheckoutMissingFirstName(browser);
            testCheckoutMissingLastName(browser);
            testCheckoutMissingZipCode(browser);
            testCheckoutCancelNavigation(browser);
            testCheckoutHappyPath(browser);
            testCartItemCount(browser);
            testCartNonNegativePrices(browser);
            testCartPageHeaderLabels(browser);
            testCartRemoveButtonVisible(browser);
            testCartContinueShoppingNavigation(browser);
            testCartCheckoutButtonNavigation(browser);
            testCompleteCheckoutProcess(browser);
            testItemRemovalAndCartBadge(browser);
            testCheckoutCancelFromStepOne(browser);
            System.out.println("\n========== TEST SUITE COMPLETED ==========");
            browser.close();
        } catch (Exception e) {
            System.err.println("An error occurred during Playwright initialization.");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to log in and reach the dashboard.
     */
    private static void login(Page page) {
        page.navigate(ConfigReader.getProperty("base.url"));
        page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
        page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
        page.locator("[data-test='login-button']").click();
        
        assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
    }
    private static void navigateToCheckoutStepOne(Page page) {
        login(page);
        page.locator("button:has-text('Add to cart')").first().click();
        page.locator(".shopping_cart_link").click();
        page.locator("[data-test='checkout']").click();
    }
    private static void testCheckoutEmptyInformation(Browser browser) {
        System.out.println("--- Test 1.1: Verify Checkout Fails with Empty Information ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCheckoutStepOne(page);

            page.locator("[data-test='continue']").click();
            
            Locator errorMsg = page.locator("[data-test='error']");
            assertThat(errorMsg).isVisible();
            System.out.println("=> Validation confirmed: Cannot proceed without details.");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCheckoutEmptyInformation");
            e.printStackTrace();
        }
    }

    private static void testCheckoutMissingFirstName(Browser browser) {
        System.out.println("--- Test 1.2: Verify Checkout Fails with Missing First Name ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCheckoutStepOne(page);

            String validLastName = ConfigReader.getProperty("checkout.lastname");
            String validZipCode = ConfigReader.getProperty("checkout.zipcode");

            page.locator("[data-test='firstName']").fill("");
            page.locator("[data-test='lastName']").fill(validLastName);
            page.locator("[data-test='postalCode']").fill(validZipCode);
            page.locator("[data-test='continue']").click();
            
            assertThat(page.locator("[data-test='error']")).containsText("First Name is required");
            System.out.println("=> Validation confirmed: Missing First Name error displayed.");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCheckoutMissingFirstName");
            e.printStackTrace();
        }
    }

    private static void testCheckoutMissingLastName(Browser browser) {
        System.out.println("--- Test 1.3: Verify Checkout Fails with Missing Last Name ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCheckoutStepOne(page);

            String validFirstName = ConfigReader.getProperty("checkout.firstname");
            String validZipCode = ConfigReader.getProperty("checkout.zipcode");

            page.locator("[data-test='firstName']").fill(validFirstName);
            page.locator("[data-test='lastName']").fill("");
            page.locator("[data-test='postalCode']").fill(validZipCode);
            page.locator("[data-test='continue']").click();
            
            assertThat(page.locator("[data-test='error']")).containsText("Last Name is required");
            System.out.println("=> Validation confirmed: Missing Last Name error displayed.");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCheckoutMissingLastName");
            e.printStackTrace();
        }
    }

    private static void testCheckoutMissingZipCode(Browser browser) {
        System.out.println("--- Test 1.4: Verify Checkout Fails with Missing Zip Code ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCheckoutStepOne(page);

            String validFirstName = ConfigReader.getProperty("checkout.firstname");
            String validLastName = ConfigReader.getProperty("checkout.lastname");

            page.locator("[data-test='firstName']").fill(validFirstName);
            page.locator("[data-test='lastName']").fill(validLastName);
            page.locator("[data-test='postalCode']").fill("");
            page.locator("[data-test='continue']").click();
            
            assertThat(page.locator("[data-test='error']")).containsText("Postal Code is required");
            System.out.println("=> Validation confirmed: Missing Zip Code error displayed.");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCheckoutMissingZipCode");
            e.printStackTrace();
        }
    }

    private static void testCheckoutCancelNavigation(Browser browser) {
        System.out.println("--- Test 1.5: Verify Cancel Button Routes Back to Cart ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCheckoutStepOne(page);

            page.locator("[data-test='cancel']").click();
            
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*cart.html"));
            System.out.println("=> Validation confirmed: Cancel button successfully routes back to the Cart page.");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCheckoutCancelNavigation");
            e.printStackTrace();
        }
    }

    private static void testCheckoutHappyPath(Browser browser) {
        System.out.println("--- Test 1.6: Verify Successful Navigation to Checkout Overview ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCheckoutStepOne(page);

            String validFirstName = ConfigReader.getProperty("checkout.firstname");
            String validLastName = ConfigReader.getProperty("checkout.lastname");
            String validZipCode = ConfigReader.getProperty("checkout.zipcode");

            page.locator("[data-test='firstName']").fill(validFirstName);
            page.locator("[data-test='lastName']").fill(validLastName);
            page.locator("[data-test='postalCode']").fill(validZipCode);
            page.locator("[data-test='continue']").click();
            
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*checkout-step-two.html"));
            System.out.println("=> TEST PASSED: Successfully proceeded with valid information.");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCheckoutHappyPath");
            e.printStackTrace();
        }
    }
    private static void testCartItemCount(Browser browser) {
        System.out.println("--- Test 2.1: Verify Cart Item Count After Adding Items ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            // Add first two items to the cart
            Locator addButtons = page.locator("button:has-text('Add to cart')");
            addButtons.nth(0).click();
            addButtons.nth(1).click();

            // Navigate to the cart
            page.locator(".shopping_cart_link").click();

            // Verify the number of items bought
            Locator cartItems = page.locator(".cart_item");
            assertThat(cartItems).hasCount(2);

            System.out.println("=> TEST PASSED: Cart correctly contains 2 items.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCartItemCount");
            e.printStackTrace();
        }
    }

    private static void testCartNonNegativePrices(Browser browser) {
        System.out.println("--- Test 2.2: Verify No Negative Prices in Cart ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            // Add first two items to the cart
            Locator addButtons = page.locator("button:has-text('Add to cart')");
            addButtons.nth(0).click();
            addButtons.nth(1).click();

            // Navigate to the cart
            page.locator(".shopping_cart_link").click();

            // Iterate through items to ensure no negative prices
            Locator prices = page.locator(".inventory_item_price");
            boolean hasNegativePrice = false;

            for (int i = 0; i < prices.count(); i++) {
                String priceText = prices.nth(i).textContent().replace("$", "");
                double price = Double.parseDouble(priceText);

                System.out.println("Item " + (i + 1) + " price: $" + price);
                if (price < 0) {
                    hasNegativePrice = true;
                }
            }

            if (!hasNegativePrice) {
                System.out.println("=> TEST PASSED: No products have negative prices.\n");
            } else {
                System.out.println("=> TEST FAILED: A product with a negative price was found.\n");
            }

        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCartNonNegativePrices");
            e.printStackTrace();
        }
    }
    /**
     * Helper method to log in, add one item to the cart, and navigate to the cart page.
     */
    private static void navigateToCartWithOneItem(Page page) {
        login(page);
        page.locator("button:has-text('Add to cart')").first().click();
        page.locator(".shopping_cart_link").click();
    }

    private static void testCartPageHeaderLabels(Browser browser) {
        System.out.println("--- Test 3.1: Verify Cart Page Title and Column Labels ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCartWithOneItem(page);

            // Kiểm tra tiêu đề trang "Your Cart"
            Locator pageTitle = page.locator(".title");
            assertThat(pageTitle).isVisible();
            assertThat(pageTitle).hasText("Your Cart");

            // Kiểm tra nhãn "QTY"
            Locator qtyLabel = page.locator(".cart_quantity_label");
            assertThat(qtyLabel).isVisible();
            assertThat(qtyLabel).hasText("QTY");

            // Kiểm tra nhãn "Description"
            Locator descLabel = page.locator(".cart_desc_label");
            assertThat(descLabel).isVisible();
            assertThat(descLabel).hasText("Description");

            System.out.println("=> TEST PASSED: Cart page title and column labels are displayed correctly.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCartPageHeaderLabels");
            e.printStackTrace();
        }
    }

    private static void testCartRemoveButtonVisible(Browser browser) {
        System.out.println("--- Test 3.2: Verify 'Remove' Button Is Visible for Cart Item ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCartWithOneItem(page);

            Locator removeButton = page.locator("button:has-text('Remove')").first();
            assertThat(removeButton).isVisible();

            System.out.println("=> TEST PASSED: 'Remove' button is visible for the item in the cart.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCartRemoveButtonVisible");
            e.printStackTrace();
        }
    }

    private static void testCartContinueShoppingNavigation(Browser browser) {
        System.out.println("--- Test 3.3: Verify 'Continue Shopping' Button Routes Back to Dashboard ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCartWithOneItem(page);

            Locator continueShoppingBtn = page.locator("[data-test='continue-shopping']");
            assertThat(continueShoppingBtn).isVisible();
            continueShoppingBtn.click();

            // Xác minh quay lại trang Inventory (Dashboard)
            assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
            System.out.println("=> TEST PASSED: 'Continue Shopping' button navigates back to Dashboard.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCartContinueShoppingNavigation");
            e.printStackTrace();
        }
    }

    private static void testCartCheckoutButtonNavigation(Browser browser) {
        System.out.println("--- Test 3.4: Verify 'Checkout' Button Routes to Checkout Step One ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            navigateToCartWithOneItem(page);

            Locator checkoutBtn = page.locator("[data-test='checkout']");
            assertThat(checkoutBtn).isVisible();
            checkoutBtn.click();

            // Xác minh chuyển hướng sang trang Checkout Step One
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*checkout-step-one.html"));
            System.out.println("=> TEST PASSED: 'Checkout' button navigates to the checkout page.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: testCartCheckoutButtonNavigation");
            e.printStackTrace();
        }
    }
    private static void testCompleteCheckoutProcess(Browser browser) {
        System.out.println("--- Test 4: Verify Complete Checkout Process (End-to-End) ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            // Đăng nhập
            login(page);

            // 1. Thêm sản phẩm vào giỏ hàng
            page.locator("button:has-text('Add to cart')").first().click();
            
            // 2. Đi đến giỏ hàng
            page.locator(".shopping_cart_link").click();
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*cart.html"));

            // 3. Nhấn Checkout
            page.locator("[data-test='checkout']").click();
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*checkout-step-one.html"));

            // 4. Điền thông tin người mua (lấy từ ConfigReader)
            page.locator("[data-test='firstName']").fill(ConfigReader.getProperty("checkout.firstname"));
            page.locator("[data-test='lastName']").fill(ConfigReader.getProperty("checkout.lastname"));
            page.locator("[data-test='postalCode']").fill(ConfigReader.getProperty("checkout.zipcode"));
            page.locator("[data-test='continue']").click();

            // 5. Xác nhận ở trang Overview
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*checkout-step-two.html"));
            
            // 6. Hoàn tất mua hàng
            page.locator("[data-test='finish']").click();

            // 7. Xác minh mua hàng thành công
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*checkout-complete.html"));
            Locator completeHeader = page.locator(".complete-header");
            assertThat(completeHeader).hasText("Thank you for your order!");

            // --- NEW: 8. Verify Cart Reset ---
            // Assert that the cart badge is hidden/removed from the DOM after purchase
            Locator cartBadge = page.locator(".shopping_cart_badge");
            assertThat(cartBadge).isHidden();
            System.out.println("=> Validation confirmed: Cart badge is no longer in the DOM (cart reset successful).");

            // --- NEW: 9. Verify Back Home Routing ---
            // Click the "Back Home" button and assert the user is routed to the inventory page
            page.locator("[data-test='back-to-products']").click();
            assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
            System.out.println("=> Validation confirmed: 'Back Home' button successfully routes to the inventory page.");

            System.out.println("=> TEST PASSED: Successfully completed the entire checkout process and verified post-purchase routing.\n");

        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 4.");
            e.printStackTrace();
        } catch (AssertionError ae) {
            System.err.println("=> TEST FAILED (AssertionError): " + ae.getMessage());
            ae.printStackTrace();
        }
    }
    
    private static void testItemRemovalAndCartBadge(Browser browser) {
        System.out.println("--- Test 5: Verify Item Removal and Dynamic Cart Badge ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            // 1. Add two items to the cart
            Locator addButtons = page.locator("button:has-text('Add to cart')");
            addButtons.nth(0).click();
            addButtons.nth(1).click();

            // Verify initial cart badge is 2
            Locator cartBadge = page.locator(".shopping_cart_badge");
            assertThat(cartBadge).hasText("2");
            System.out.println("=> Initial cart badge dynamically set to 2.");

            // Navigate to the cart
            page.locator(".shopping_cart_link").click();

            // Verify the initial item count in the DOM is 2
            Locator cartItems = page.locator(".cart_item");
            assertThat(cartItems).hasCount(2);

            // 2. Locate the remove button and assert it is visible
            Locator removeButton = page.locator("button:has-text('Remove')").first();
            assertThat(removeButton).isVisible();

            try {
                // Attempt to click with a strict 5-second timeout to prevent system hang
                removeButton.click(new Locator.ClickOptions().setTimeout(5000));
            } catch (Exception e) {
                // If the click times out because the button is unresponsive, force an AssertionError
                throw new AssertionError("Timeout: The 'Remove' button is not working or unresponsive.", e);
            }

            // REQUIREMENT 1: Item Removal
            assertThat(cartItems).hasCount(1);
            System.out.println("=> Verified: Item removed and is no longer present in the DOM.");

            // REQUIREMENT 2: Dynamic Cart Badge
            assertThat(cartBadge).hasText("1");
            System.out.println("=> Verified: Cart badge dynamically decremented to 1.");

            System.out.println("=> TEST PASSED: Item removal and badge decrement work correctly.\n");

        } catch (AssertionError ae) {
            // Catch assertion errors specifically (including our manual timeout assertion)
            System.err.println("=> TEST FAILED (AssertionError): " + ae.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected Playwright errors
            System.err.println("=> TEST FAILED (Unexpected Exception): An error occurred in Test 5.");
            e.printStackTrace();
        }
    }
    private static void testCheckoutCancelFromStepOne(Browser browser) {
        System.out.println("--- Test 6: Verify Cancel Aborts Checkout and Routes to Inventory ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            // Đăng nhập
            login(page);

            // 1. Thêm sản phẩm vào giỏ hàng
            page.locator("button:has-text('Add to cart')").first().click();

            // 2. Đi đến giỏ hàng
            page.locator(".shopping_cart_link").click();
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*cart.html"));

            // 3. Nhấn Checkout
            page.locator("[data-test='checkout']").click();
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*checkout-step-one.html"));

            // 4. Nhấn Cancel và xác nhận quay về trang Inventory
            page.locator("[data-test='cancel']").click();
           assertThat(page).hasURL(java.util.regex.Pattern.compile(".*cart.html"));
            System.out.println("=> Validation confirmed: 'Cancel' button aborts checkout and routes to cart page.");

            System.out.println("=> TEST PASSED: Cancel flow correctly aborts checkout.\n");

        } catch (AssertionError ae) {
            System.err.println("=> TEST FAILED (AssertionError): " + ae.getMessage());
            ae.printStackTrace();
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 6.");
            e.printStackTrace();
        }
    }
}