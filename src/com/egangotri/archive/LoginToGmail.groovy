package com.egangotri.archive

import com.egangotri.util.PDFUtil
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

/**
 * Created by user on 1/19/2016.
 */
class LoginToGmail {

    static final String LOGIN_PROFILE = "bm" // "bm", "mm", "jm" , "lk", "sr", "srCP

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

            WebElement pass = driver.findElement(By.id("Passwd"));
            WebElement button = driver.findElement(By.id("signIn"));

            pass.sendKeys(metaDataMap."${LOGIN_PROFILE}.kuta");
            button.click();

            driver.get("http://drive.google.com");
        }
        catch (Exception e) {
            e.printStackTrace()
        }
        println "done"
    }

}
