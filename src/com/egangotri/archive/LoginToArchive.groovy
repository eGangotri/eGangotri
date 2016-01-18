package com.egangotri.archive

import com.egangotri.util.PDFUtil
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
class LoginToArchive {
    static final int numberOfTimes = 4

    static main(args) {
        println "start"
        def metaDataMap = PDFUtil.loadProperties("${PDFUtil.HOME}/archiveProj/UserIdsMetadata.properties")


        try {
            WebDriver driver = new ChromeDriver()
            driver.get("http://archive.org/account/login.php");
            WebElement id = driver.findElement(By.id("username"));
            WebElement pass = driver.findElement(By.id("password"));
            WebElement button = driver.findElement(By.id("submit"));

            id.sendKeys(metaDataMap."${PDFUtil.ARCHIVE_PROFILE}.username");
            pass.sendKeys(getKut(metaDataMap));
            button.click();

            //Get Upload Link
            String uploadLink = ArchiveURLGeneratorWithMetadata.generateURL()
            driver.get(uploadLink);

            if (numberOfTimes > 1) {
                (1..(numberOfTimes - 1)).each { tabNo ->
                    // Open new tab
                    driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "t");
                    //Switch to new Tab
                    ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(tabNo));
                    //Go to URL
                    driver.get(uploadLink);
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace()
        }
        println "done"
    }

    public static getKut(def metaDataMap) {
        def listOfIntegers = (0..2).collect({ int i ->
            i * 2 + 1
        })
        String kut = ""
        ['aap', 'kahan', 'ho'].each { it ->
            listOfIntegers.each { num -> kut += metaDataMap."$it"[num] }
        }
        return kut.substring(0, kut.length() - 1)
    }
}

