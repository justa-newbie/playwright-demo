package com.example;

import com.microsoft.playwright.*;

public class App {
    public static void main(String[] args) {
        // Playwright downloads the browsers in the background on the first run
        try (Playwright playwright = Playwright.create()) {
            
            // setHeadless(false) allows you to visibly see the browser open
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
            );
            
            Page page = browser.newPage();
            page.navigate("https://playwright.dev/java/");
            
            System.out.println("Success! Page title is: " + page.title());
            
            browser.close();
        }
    }
}