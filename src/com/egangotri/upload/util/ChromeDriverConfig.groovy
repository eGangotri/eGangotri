package com.egangotri.upload.util

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.Duration

class ChromeDriverConfig {
    static ChromeDriver createDriver() {
        ChromeOptions options = new ChromeOptions()
        
        // Add options to handle version mismatch and connection issues
        options.addArguments([
            '--remote-allow-origins=*',  // Allows ChromeDriver to work with any version of Chrome
            '--ignore-certificate-errors',
            '--disable-dev-shm-usage',
            '--no-sandbox',
            '--disable-gpu',  // Helps with headless mode stability
            '--disable-software-rasterizer',  // Additional stability option
            '--disable-extensions'  // Disables extensions that might interfere
        ])
        
        // Create driver with options
        ChromeDriver driver = new ChromeDriver(options)
        
        // Configure timeouts after driver creation
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120))
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30))
        
        // Disable CDP logging
        System.setProperty('webdriver.chrome.verboseLogging', 'false')
        System.setProperty('webdriver.chrome.silentOutput', 'true')
        return driver
    }
}
