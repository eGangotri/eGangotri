package com.egangotri.archive

import com.egangotri.util.PDFUtil
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by user on 1/19/2016.
 */
class LoginToGmail {

    static final String LOGIN_PROFILE = "sr" // "bm", "mm", "jm" , "lk", "sr", "srCP" , "ij"
    static String FOLDER_NAME = "C:\\hw\\amit"

    static main(args) {
        println "start"
        def metaDataMap = PDFUtil.loadProperties("${PDFUtil.HOME}/archiveProj/GmailData.properties")

        try {
            WebDriver driver = new ChromeDriver()
            driver.get("http://accounts.google.com");
            WebElement id = driver.findElement(By.id("Email"));

            WebElement next = driver.findElement(By.id("next"));
            id.sendKeys(metaDataMap."${LOGIN_PROFILE}.userId");
            next.click();


            WebDriverWait wait = new WebDriverWait(driver, 2);
            wait.until(ExpectedConditions.elementToBeClickable(By.id("Passwd")));

            WebElement pass = driver.findElement(By.id("Passwd"));
            WebElement button = driver.findElement(By.id("signIn"));

            String kuta = metaDataMap."${LOGIN_PROFILE}.kuta" ?:metaDataMap."kuta"

            pass.sendKeys(metaDataMap."${LOGIN_PROFILE}.kuta");
            button.click();

            driver.get("http://drive.google.com");
            uploadToDrive(driver)
        }
        catch (Exception e) {
            e.printStackTrace()
        }
        println "done"
    }

    static void uploadToDrive(def driver){

    }
}
