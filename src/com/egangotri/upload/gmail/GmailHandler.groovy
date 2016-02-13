package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory

/**
 * Created by user on 1/22/2016.
 */
class GmailHandler {
    final static org.slf4j.Logger Log = LoggerFactory.getLogger(this.class);

    def static void login(def metaDataMap, String loginProfile) {
        loginAndUpload(metaDataMap, loginProfile)
    }

    def static void loginAndUpload(def metaDataMap, String loginProfile, String folderName) {
        loginAndUpload(metaDataMap, loginProfile, true, folderName)
    }

    def static void loginAndUpload(
            def metaDataMap, String loginProfile, boolean upload = null, String folderName = null) {
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
        }
        catch (Exception e) {
            e.printStackTrace()
        }
        Log.info "done"
    }

    static void uploadToDrive(def driver, String folderName) {
        driver.findElement(By.xpath("//div[contains(text(),'New')]")).click()
        driver.findElement(By.xpath("//div[contains(text(),'Folder upload')]")).click()
        UploadUtils.tabPasteFolderNameAndCloseUploadPopup(folderName)
    }

}
