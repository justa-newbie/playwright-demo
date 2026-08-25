package com.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DashboardTest {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            System.out.println("========== STARTING DASHBOARD TEST SUITE ==========\n");

            testDashboardElementsLoad(browser);
            testAddToCartUpdatesBadge(browser);
            testProductSorting(browser);
            
            // New Tests 
            testProductDescriptionClick(browser);
            testDashboardLoadPerformance(browser);
            testAddAllProducts(browser);
            testAddAndRemoveProduct(browser);
            testProductSortingNameAZ(browser);
            testAboutPageNavigation(browser);
            System.out.println("\n========== TEST SUITE COMPLETED ==========");
            browser.close();
        } catch (Exception e) {
            System.err.println("An error occurred during Playwright initialization.");
            e.printStackTrace();
            
        }
    }

    /**
     * Helper method to log in and reach the dashboard before each test.
     */
    private static void login(Page page) {
        page.navigate(ConfigReader.getProperty("base.url"));
        page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
        page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
        page.locator("[data-test='login-button']").click();
        
        // Ensure we are on the inventory page before proceeding
        assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
    }

    private static void testDashboardElementsLoad(Browser browser) {
        System.out.println("--- Test 1: Verify Core Dashboard Elements Load ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            Locator pageTitle = page.locator(".title");
            assertThat(pageTitle).isVisible();
            assertThat(pageTitle).hasText("Products");

            Locator inventoryItems = page.locator(".inventory_item");
            if (inventoryItems.count() > 0) {
                System.out.println("=> TEST PASSED: Products are displayed on the dashboard.\n");
            } else {
                System.out.println("=> TEST FAILED: No products loaded on the dashboard.\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 1.");
            e.printStackTrace();
        }
    }

    private static void testAddToCartUpdatesBadge(Browser browser) {
        System.out.println("--- Test 2: Add to Cart Updates Badge ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            Locator firstAddToCartBtn = page.locator("button:has-text('Add to cart')").first();
            firstAddToCartBtn.click();

            Locator cartBadge = page.locator(".shopping_cart_badge");
            assertThat(cartBadge).isVisible();
            assertThat(cartBadge).hasText("1");

            System.out.println("=> TEST PASSED: Cart badge successfully updated after adding an item.\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 2.");
            e.printStackTrace();
        }
    }

    private static void testProductSorting(Browser browser) {
        System.out.println("--- Test 3: Product Sorting (Low to High) ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            Locator sortDropdown = page.locator(".product_sort_container");
            sortDropdown.selectOption("lohi");

            Locator prices = page.locator(".inventory_item_price");
            
            double firstPrice = Double.parseDouble(prices.nth(0).textContent().replace("$", ""));
            double secondPrice = Double.parseDouble(prices.nth(1).textContent().replace("$", ""));

            if (firstPrice <= secondPrice) {
                System.out.println("=> TEST PASSED: Products successfully sorted by price (Low to High).\n");
            } else {
                System.out.println("=> TEST FAILED: Sorting did not work correctly. First price: " + firstPrice + ", Second price: " + secondPrice + "\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 3.");
            e.printStackTrace();
        }
    }
    private static void testProductDescriptionClick(Browser browser) {
        System.out.println("--- Test 4: Navigate to Product Description and Read ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            // 1. Log in and reach the dashboard
            login(page);

            // 2. Locate the first product name and click it to go to the details page
            Locator allProducts = page.locator(".inventory_item_name");
            int productCount = allProducts.count();
            System.out.println("Found " + productCount + " products on the dashboard.\n");

            // 3. Loop through each product one by one
            for (int i = 0; i < productCount; i++) {
                // Re-query the locator inside the loop to ensure fresh DOM references
                Locator currentProduct = page.locator(".inventory_item_name").nth(i);
                String productName = currentProduct.textContent();
                
                // Click into the product details page
                currentProduct.click();

                // Wait for the description to load and extract the text
                Locator productDescription = page.locator(".inventory_details_desc");
                assertThat(productDescription).isVisible();
                String descriptionText = productDescription.textContent();

                // Print the findings to the terminal
                System.out.println("Product " + (i + 1) + " / " + productCount + ": " + productName);
                System.out.println("Description: " + descriptionText);
                System.out.println("--------------------------------------------------");

                // Go back to the dashboard for the next iteration
                page.goBack();
                
                // Verify we are successfully back on the inventory page
                // Note: Make sure ConfigReader is accessible or replace with hardcoded URL check if preferred
                assertThat(page).hasURL(ConfigReader.getProperty("inventory.url"));
            }

            System.out.println("=> TEST PASSED: Successfully read descriptions for all " + productCount + " products.\n");

        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 5.");
            e.printStackTrace();
        }
    }
    private static void testDashboardLoadPerformance(Browser browser) {
        System.out.println("--- Test 5: Dashboard Load Performance (Glitch Check) ---");
        
        // Define our maximum acceptable load time in milliseconds (e.g., 2 seconds)
        long maxAcceptableLoadTimeMs = 2000; 

        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            
            // 1. Navigate and fill in credentials
            page.navigate(ConfigReader.getProperty("base.url"));
            
            // Note: To see this test intentionally fail on SauceDemo, 
            // you can hardcode "performance_glitch_user" here.
            page.locator("[data-test='username']").fill(ConfigReader.getProperty("valid.username"));
            page.locator("[data-test='password']").fill(ConfigReader.getProperty("valid.password"));
            
            // 2. Start the timer right before clicking the login button
            long startTime = System.currentTimeMillis();
            page.locator("[data-test='login-button']").click();
            
            // 3. Wait for a core element to render to prove the dashboard loaded
            // We use .waitFor() which will throw an exception if it times out completely
            page.locator(".inventory_item").first().waitFor();
            
            // 4. Stop the timer
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            
            System.out.println("Dashboard rendered in " + loadTime + " ms.");

            // 5. Evaluate the performance
            if (loadTime > maxAcceptableLoadTimeMs) {
                System.out.println("=> TEST FAILED: Performance glitch detected! Load time (" + loadTime + " ms) exceeded the limit of " + maxAcceptableLoadTimeMs + " ms.\n");
            } else {
                System.out.println("=> TEST PASSED: No performance glitches. Load time was acceptable.\n");
            }

        } catch (Exception e) {
            System.err.println("=> TEST FAILED: The page hung completely and timed out before loading.");
            e.printStackTrace();
        }
    }
    private static void testAddAllProducts(Browser browser) {
        System.out.println("--- Test 6: Add All Products To Cart ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            Locator allProducts = page.locator(".inventory_item");
            int productCount = allProducts.count();
            Locator cartBadge = page.locator(".shopping_cart_badge");

            System.out.println("Found " + productCount + " products. Starting add process...");

            for (int i = 0; i < productCount; i++) {
                Locator currentProduct = allProducts.nth(i);
                Locator actionButton = currentProduct.locator("button");

                // 1. Kiểm tra trạng thái ban đầu là "Add to cart"
                assertThat(actionButton).hasText("Add to cart");

                // 2. Click thêm vào giỏ hàng
                actionButton.click();

                // 3. Kiểm tra button đổi thành "Remove"
                assertThat(actionButton).hasText("Remove");

                // 4. Kiểm tra số lượng trên badge giỏ hàng khớp với số lượng đã add (i + 1)
                assertThat(cartBadge).isVisible();
                assertThat(cartBadge).hasText(String.valueOf(i + 1));
            }

            System.out.println("=> TEST PASSED: Successfully added all " + productCount + " products and verified UI states.\n");
        } catch (AssertionError ae) {
          System.out.println("=> TEST FAILED: " + ae.getMessage() + "\n");
        } catch (Exception e) {
        System.err.println("=> TEST FAILED: Unexpected error in this test.");
           e.printStackTrace();
        }
    }
    private static void testAddAndRemoveProduct(Browser browser) {
        System.out.println("--- Test 7: Add and Remove a Product ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            login(page);

            Locator firstProduct = page.locator(".inventory_item").first();
            Locator actionButton = firstProduct.locator("button");
            Locator cartBadge = page.locator(".shopping_cart_badge");

            // 1. Kiểm tra trạng thái ban đầu là "Add to cart"
            assertThat(actionButton).hasText("Add to cart");

            // 2. Click thêm vào giỏ hàng
            actionButton.click();

            // 3. Kiểm tra button đổi thành "Remove"
            assertThat(actionButton).hasText("Remove");

            // 4. Kiểm tra số lượng trên badge giỏ hàng là 1
            assertThat(cartBadge).isVisible();
            assertThat(cartBadge).hasText("1");

            // 5. Click để remove sản phẩm khỏi giỏ hàng
            actionButton.click();

            // 6. Kiểm tra button đổi lại thành "Add to cart"
            assertThat(actionButton).hasText("Add to cart");

            // 7. Kiểm tra badge giỏ hàng biến mất
            assertThat(cartBadge).not().isVisible();

            System.out.println("=> TEST PASSED: Successfully added and removed a product, verified UI states.\n");
        } catch (AssertionError ae) {
          System.out.println("=> TEST FAILED: " + ae.getMessage() + "\n");
        } catch (Exception e) {
          System.err.println("=> TEST FAILED: Unexpected error in this test.");
          e.printStackTrace();
        }
    }
    private static void testProductSortingNameAZ(Browser browser) {
        System.out.println("--- Test 8: Product Sorting (Name A to Z) ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            // 1. Đăng nhập để vào dashboard
            login(page);

            // 2. Tìm dropdown sắp xếp và chọn option A to Z (value trên SauceDemo là "az")
            Locator sortDropdown = page.locator(".product_sort_container");
            sortDropdown.selectOption("az");

            // 3. Lấy locator chứa tất cả tên sản phẩm hiện ra
            Locator productNames = page.locator(".inventory_item_name");
            
            // 4. Lấy text của sản phẩm đầu tiên và thứ hai để so sánh
            String firstName = productNames.nth(0).textContent();
            String secondName = productNames.nth(1).textContent();

            // 5. Sử dụng compareTo để kiểm tra thứ tự bảng chữ cái.
            // Nếu firstName đứng trước hoặc bằng secondName, kết quả sẽ <= 0
            if (firstName.compareToIgnoreCase(secondName) <= 0) {
                System.out.println("=> TEST PASSED: Products successfully sorted by name (A to Z).\n");
            } else {
                System.out.println("=> TEST FAILED: Sorting did not work correctly. First name: " + firstName + ", Second name: " + secondName + "\n");
            }
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 8 (Sorting A to Z).");
            e.printStackTrace();
        }
    }
    private static void testAboutPageNavigation(Browser browser) {
        System.out.println("--- Test 9: Navigate to About Page ---");
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            // 1. Đăng nhập để vào trang dashboard
            login(page);

            // 2. Click vào biểu tượng menu (hamburger) ở góc trên bên trái
            Locator menuButton = page.locator("#react-burger-menu-btn");
            menuButton.click();

            // 3. Tìm và click vào thẻ "About" trên sidebar
            Locator aboutLink = page.locator("#about_sidebar_link");
            
            // Đợi một chút để animation của sidebar mở ra hoàn toàn trước khi click
            aboutLink.waitFor();
            aboutLink.click();

            // 4. Kiểm tra URL xem đã chuyển hướng thành công đến trang About chưa
            // Trang SauceDemo thường chuyển hướng đến trang chủ của Sauce Labs
            assertThat(page).hasURL("https://saucelabs.com/");

            System.out.println("=> TEST PASSED: Successfully navigated to the About page.\n");
        } catch (AssertionError ae) {
            System.out.println("=> TEST FAILED: Verification failed - " + ae.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("=> TEST FAILED: An error occurred in Test 9 (About Page Navigation).");
            e.printStackTrace();
        }
    }
}