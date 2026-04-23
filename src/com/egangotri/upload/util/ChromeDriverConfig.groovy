package com.egangotri.upload.util

import com.egangotri.upload.archive.ArchiveHandler
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.PageLoadStrategy
import java.time.Duration

class ChromeDriverConfig {

    static int findFreePort(int startPort) {
        int currentPort = startPort
        int MAX_TRIES = 100
        int tries = 0
        while (tries < MAX_TRIES) {
            try {
                new java.net.ServerSocket(currentPort).close()
                break
            } catch (Exception e) {
                currentPort++
            }
            tries++
        }
        if (startPort != currentPort) {
            ArchiveHandler.DEFAULT_PORT_FOR_CHROME_REMOTE_DEBUGGING_PORT = currentPort +1
            println "Debug port $startPort was busy. Using port $currentPort instead."
        }
        return currentPort
    }

    static ChromeDriver createDriver(int debugPort=9222) {
        debugPort = findFreePort(debugPort)

        ChromeOptions options = new ChromeOptions()

        // Add options to handle version mismatch and connection issues
        options.addArguments([
            '--remote-allow-origins=*',
            '--disable-blink-features=AutomationControlled',  // Hide automation
            '--disable-dev-shm-usage',
            '--start-maximized',
            '--disable-infobars',
            "--remote-debugging-port=${debugPort}",   // dynamic port
            '--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36'
        ])
        // Use NORMAL to ensure SPA JS bundles fully load before proceeding
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL)

        // Hide webdriver flag
        options.setExperimentalOption('excludeSwitches', ['enable-automation'])
        options.setExperimentalOption('useAutomationExtension', false)
        // Be lenient with certs (should not be needed for archive.org, but avoids sporadic failures)
        options.setAcceptInsecureCerts(true)

        // Create driver with options
        ChromeDriver driver = new ChromeDriver(options)

        // Configure timeouts after driver creation
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120))
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30))

        // Execute stealth JS to hide webdriver property
        driver.executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
            "Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]});" +
            "Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});"
        )

        // Disable CDP logging
        System.setProperty('webdriver.chrome.verboseLogging', 'false')
        System.setProperty('webdriver.chrome.silentOutput', 'true')
        return driver
    }

}
