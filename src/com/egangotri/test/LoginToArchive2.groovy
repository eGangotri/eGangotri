package com.egangotri.test

import com.egangotri.util.EGangotriUtil
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class LoginToArchive2 {

    static void main(String[] args) {
        System.setProperty("webdriver.http.factory", "jdk-http-client")

        ChromeOptions options = new ChromeOptions()
        options.addArguments("--start-maximized")
        options.addArguments("--remote-allow-origins=*")

        WebDriver driver = new ChromeDriver(options)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30))

        try {
            println "Navigating to Archive.org Login..."
            driver.get("https://archive.org/account/login")

            String username = "indiclibrary19@gmail.com"
            String password = "a@hBmsep123"

            // ---------------------------------------------------------
            // HELPER 1: Deep Search for Shadow DOM
            // ---------------------------------------------------------
            String deepSelectorJS = """
                function querySelectorDeep(selector, root = document) {
                    let element = null;
                    element = root.querySelector(selector);
                    if (element) return element;
                    let walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT, null, false);
                    while(walker.nextNode()) {
                        let node = walker.currentNode;
                        if (node.shadowRoot) {
                            element = querySelectorDeep(selector, node.shadowRoot);
                            if (element) return element;
                        }
                    }
                    return null;
                }
                return querySelectorDeep(arguments[0]);
            """

            // ---------------------------------------------------------
            // HELPER 2: Force Input Event (Crucial for modern frameworks)
            // ---------------------------------------------------------
            String triggerEventJS = """
                arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
                arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
            """

            println "Waiting for elements..."

            // 1. Find and Fill Email
            WebElement emailInput = wait.until(new ExpectedCondition<WebElement>() {
                @Override
                WebElement apply(WebDriver d) {
                    return (WebElement) ((JavascriptExecutor) d).executeScript(deepSelectorJS, "input[type='email']")
                }
            })

            println "Entering Email..."
            emailInput.clear()
            emailInput.sendKeys(username)
            // Trigger the event so the page 'sees' the text
            ((JavascriptExecutor) driver).executeScript(triggerEventJS, emailInput)

            // 2. Find and Fill Password
            WebElement passwordInput = wait.until(new ExpectedCondition<WebElement>() {
                @Override
                WebElement apply(WebDriver d) {
                    return (WebElement) ((JavascriptExecutor) d).executeScript(deepSelectorJS, "input[type='password']")
                }
            })

            println "Entering Password..."
            passwordInput.clear()
            passwordInput.sendKeys(password)
            ((JavascriptExecutor) driver).executeScript(triggerEventJS, passwordInput)

            // ---------------------------------------------------------
            // PERFORM LOGIN (The "Double Tap" Strategy)
            // ---------------------------------------------------------
            println "Attempting Login..."

            // Method A: Press ENTER on the password field (Most reliable)
            passwordInput.sendKeys(Keys.ENTER)
            println "Sent ENTER key to password field."

            Thread.sleep(1000) // Give it a second to process

            // Method B: Find and click the button explicitly if Enter didn't work
            // We check if we are still on the login page before trying to click
            if (driver.getCurrentUrl().contains("login")) {
                try {
                    WebElement submitButton = (WebElement) ((JavascriptExecutor) driver).executeScript(deepSelectorJS, "button[type='submit']")
                    if (submitButton != null) {
                        println "Clicking Submit Button via JS..."
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton)
                    }
                } catch (Exception ignore) {
                    println "Could not find/click button, relying on Enter key."
                }
            }

            // ---------------------------------------------------------
            // VERIFY
            // ---------------------------------------------------------
            println "Waiting for redirection..."
            wait.until({ d -> !d.getCurrentUrl().contains("/login") } as ExpectedCondition)

            println "SUCCESS: Login complete!"
            println "Current URL: " + driver.getCurrentUrl()

        } catch (Exception e) {
            println "ERROR: " + e.getMessage()
            // e.printStackTrace()
        } finally {
            // driver.quit()
        }
    }
}
