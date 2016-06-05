package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
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
            WebDriver driver = new ChromeDriver()
            driver.get("http://accounts.google.com");
            WebElement id = driver.findElement(By.id("Email"));

            WebElement next = driver.findElement(By.id("next"));
            id.sendKeys(metaDataMap."${loginProfile}.userId");
            next.click();


            WebDriverWait wait = new WebDriverWait(driver, 2);
            wait.until(ExpectedConditions.elementToBeClickable(By.id("Passwd")));

            WebElement pass = driver.findElement(By.id("Passwd"));
            WebElement button = driver.findElement(By.id("signIn"));

            String kuta = metaDataMap."${loginProfile}.kuta" ?: metaDataMap."kuta"

            pass.sendKeys(metaDataMap."${loginProfile}.kuta");
            button.click();

            driver.get("http://drive.google.com");
            if (upload) {
                uploadToDrive(driver, folderName)
            }
            res = true
        }
        catch (Exception e) {
            e.printStackTrace()
        }
        return res
    }

    static void uploadToDrive(def driver, String folderName) {
        driver.findElement(By.xpath("//div[contains(text(),'New')]")).click()
        driver.findElement(By.xpath("//div[contains(text(),'Folder upload')]")).click()
        UploadUtils.tabPasteFolderNameAndCloseUploadPopup(folderName)
    }

}
