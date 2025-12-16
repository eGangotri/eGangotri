package com.egangotri.test

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

@Slf4j
class LoginToArchive2 {
    static void main(String[] args) {
        System.setProperty("webdriver.http.factory", "jdk-http-client")
        String username = "";
        String password = "";
        ChromeOptions options = new ChromeOptions()
        options.addArguments("--start-maximized")
        options.addArguments("--remote-allow-origins=*")

        WebDriver driver = new ChromeDriver(options)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30))
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        def archiveLoginsMetaDataMap = UploadUtils.getAllArchiveLogins()
        try {
            println "Navigating to Archive.org Login..."
            driver.get("https://archive.org/account/login")

            // ---------------------------------------------------------
            // USE ArchiveUtil for Login (Verifying the Fix)
            // ---------------------------------------------------------
            if (archiveLoginsMetaDataMap) {
                // Just pick the first profile for testing, or the one from args if provided
                String profileToUse = archiveProfiles.find { archiveLoginsMetaDataMap.containsKey(it) }
                if (profileToUse) {
                    log.info "Testing login with profile: $profileToUse"
                    boolean success = com.egangotri.upload.util.ArchiveUtil.logInToArchiveOrg(driver, archiveLoginsMetaDataMap, profileToUse)
                    if (success) {
                        println "SUCCESS: Login complete via ArchiveUtil!"
                    } else {
                        println "FAILURE: Login failed via ArchiveUtil."
                        throw new Exception("Login failed")
                    }
                } else {
                    log.info "No valid profile found to test login."
                }
            } else {
                log.info "No MetaData Cannot proceed."
            }

        } catch (Exception e) {
            println "ERROR: " + e.getMessage()
            e.printStackTrace()
        } finally {
             //driver.quit()
        }
    }
}