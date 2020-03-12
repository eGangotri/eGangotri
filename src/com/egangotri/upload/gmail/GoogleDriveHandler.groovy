package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.*

@Slf4j
class GoogleDriveHandler {
    def static boolean login(Hashtable<String, String> metaDataMap, String loginProfile) {
        return loginAndUpload(metaDataMap, loginProfile)
    }

    def static boolean loginAndUpload(Hashtable<String, String> metaDataMap, String loginProfile, String folderName) {
        return loginAndUpload(metaDataMap, loginProfile, true, folderName)
    }

    def static boolean loginAndUpload(
            Hashtable<String, String> metaDataMap, String loginProfile, boolean upload = null, String folderName = null) {
        boolean res = false
        try {
            String userHome = System.getProperty('user.home')
            log.info "System.getProperty(\"webdriver.chrome.driver\")" + System.getProperty("webdriver.chrome.driver")
            System.setProperty("webdriver.chrome.driver", "${userHome}${File.separator}chromedriver${File.separator}chromedriver.exe")
            // log.info "System.getProperty(\"webdriver.chrome.driver\")" + System.getProperty("webdriver.chrome.driver")

            WebDriver driver = new ChromeDriver()
            driver.get("https://accounts.google.com")
            WebElement id = driver.findElement(By.id("identifierId"))

            id.sendKeys(metaDataMap."${loginProfile}.userId")
//            WebElement next = driver.findElement(By.id("identifierNext"))
//            next.click()
            id.sendKeys(Keys.RETURN)


            WebDriverWait wait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
            wait.until(ExpectedConditions.elementToBeClickable(By.name("password")))

            WebElement pass = driver.findElement(By.name("password"))
            String kuta = metaDataMap."${loginProfile}.kuta" ?: metaDataMap."kuta"
            log.info "kuta:$kuta"
            pass.sendKeys(kuta)
            pass.sendKeys(Keys.RETURN)
            EGangotriUtil.sleepTimeInSeconds(1)
            driver.get("http://drive.google.com")
            if (upload) {
                uploadToDrive(driver, folderName)
            }
            res = true
        }
        catch (Exception e) {
            log.info("drive log error", e)
            e.printStackTrace()
        }
        return res
    }

    static void uploadToDrive(def driver, String folderName) {
        driver.findElement(By.xpath("//div[contains(text(),'My Drive')]")).click()
        driver.findElement(By.xpath("/html/body/div[12]")).click()
        UploadUtils.tabPasteFolderNameAndCloseUploadPopup(folderName)
    }

}
