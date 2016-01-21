package com.egangotri.archive

import com.egangotri.util.PDFUtil
import org.apache.bcel.generic.Select
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.LocalFileDetector
import org.openqa.selenium.remote.RemoteWebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.w3c.dom.html.HTMLSelectElement

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit
import org.openqa.selenium.support.ui.Select;


/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
class LoginToArchive {
    static String PDF = ".pdf"
    static String FOLDER_NAME = "C:\\hw\\avn\\AvnManuscripts\\GopinathKavirajTantricSahityaList"
    static String ARCHIVE_URL = "http://archive.org/account/login.php"


    static main(args) {
        println "start"
        def metaDataMap = PDFUtil.loadProperties("${PDFUtil.HOME}/archiveProj/UserIdsMetadata.properties")
        try {
            WebDriver driver = new ChromeDriver()
            driver.get(ARCHIVE_URL);

            //Login
            WebElement id = driver.findElement(By.id("username"));
            WebElement pass = driver.findElement(By.id("password"));
            WebElement button = driver.findElement(By.id("submit"));

            id.sendKeys(metaDataMap."${PDFUtil.ARCHIVE_PROFILE}.username");
            pass.sendKeys(metaDataMap."kuta");
            button.click();

            //Fetch uploadable Files
            List<String> uploadables = getFiles(FOLDER_NAME)

            //Get Upload Link
            String uploadLink = ArchiveURLGeneratorWithMetadata.generateURL()

            //Start Upload of First File in Root Tab
            upload(driver,uploadables[0],uploadLink)

            // Upload Remaining Files by generating New Tabs
            if (uploadables.size() > 1) {
                uploadables.drop(1).eachWithIndex { fileName, tabNo ->
                    println "fileName: $fileName tabNo:$tabNo"
                    // Open new tab
                    driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "t");
                    //Switch to new Tab
                    ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(tabNo + 1));

                    //Start Upload
                    upload(driver,fileName,uploadLink)
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace()
        }
        println "done"
    }


    public static void upload(WebDriver driver, String fileNameWIthPath, String uploadLink) {
        //Go to URL
        driver.get(uploadLink);

        WebElement fileButtonInitial = driver.findElement(By.id("file_button_initial"));
        //((RemoteWebElement) fileButtonInitial).setFileDetector(new LocalFileDetector());
        fileButtonInitial.click();
        pasteFileNameAndCloseUploadPopup(fileNameWIthPath)

        new WebDriverWait(driver, 3).until(ExpectedConditions.elementToBeClickable(By.id("license_picker_row")));

        WebElement licPicker = driver.findElement(By.id("license_picker_row"));
        licPicker.click()

        WebElement radioBtn = driver.findElement(By.id("license_radio_CC0"));
        radioBtn.click();

        //remove this junk value that pops-up for profiles with Collections
        if(uploadLink.contains("collection=")) {
           driver.findElement(By.className("additional_meta_remove_link")).click()
        }

        WebDriverWait wait = new WebDriverWait(driver, 3);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("upload_button")));

        WebElement uploadButton = driver.findElement(By.id("upload_button"));
        uploadButton.click();
    }


    public static void pasteFileNameAndCloseUploadPopup(String fileName) {
        println "$fileName  being pasted"
        // A short pause, just to be sure that OK is selected
        Thread.sleep(1000);
        setClipboardData(fileName);
        //native key strokes for CTRL, V and ENTER keys
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
    public static void setClipboardData(String string) {
        StringSelection stringSelection = new StringSelection(string);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

    static List<String> getFiles(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        println "processAFolder $directory"
        def files = directory.listFiles()
        List<String> uploadables = []
        files.each { File file ->
            if (!file.isDirectory() && file.name.endsWith(PDF)) {
                uploadables << file.absolutePath
            }
        }
        println "***Total Files uploadables: ${uploadables.size()}"
        return uploadables
    }
}

