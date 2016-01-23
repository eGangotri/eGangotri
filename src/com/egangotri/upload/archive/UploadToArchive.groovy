package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
class UploadToArchive {

    static enum PROFILE_ENUMS {
        dt, ib, rk, jg
    }

    static final String ARCHV_PRFL = PROFILE_ENUMS.dt
    static String FOLDER_NAME = "C:\\hw\\ib"

    static String ARCHIVE_URL = "http://archive.org/account/login.php"
    static final String baseUrl = "http://archive.org/upload/?"
    static String PDF = ".pdf"
    static final String ampersand = "&"
    static final String HOME = System.getProperty('user.home')
    static final String ARCHIVE_PROFILE = ARCHV_PRFL.toString()

    static main(args) {
        println "start"
        def metaDataMap = UploadUtils.loadProperties("${HOME}/archiveProj/UserIdsMetadata.properties")
        try {
            WebDriver driver = new ChromeDriver()
            driver.get(ARCHIVE_URL);

            //Login
            WebElement id = driver.findElement(By.id("username"));
            WebElement pass = driver.findElement(By.id("password"));
            WebElement button = driver.findElement(By.id("submit"));

            id.sendKeys(metaDataMap."${ARCHIVE_PROFILE}.username");
            pass.sendKeys(metaDataMap."kuta");
            button.click();

            //Fetch uploadable Files
            List<String> uploadables = UploadUtils.getFiles(pickFolderBasedOnArchiveProfile())

            //Get Upload Link
            String uploadLink = generateURL()

            //Start Upload of First File in Root Tab
            upload(driver, uploadables[0], uploadLink)

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
                    upload(driver, fileName, uploadLink)
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
        UploadUtils.pasteFileNameAndCloseUploadPopup(fileNameWIthPath)

        new WebDriverWait(driver, 3).until(ExpectedConditions.elementToBeClickable(By.id("license_picker_row")));

        WebElement licPicker = driver.findElement(By.id("license_picker_row"));
        licPicker.click()

        WebElement radioBtn = driver.findElement(By.id("license_radio_CC0"));
        radioBtn.click();

        //remove this junk value that pops-up for profiles with Collections
        if (uploadLink.contains("collection=")) {
            driver.findElement(By.className("additional_meta_remove_link")).click()
        }

        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("upload_button")));

        WebElement uploadButton = driver.findElement(By.id("upload_button"));
        uploadButton.click();
    }

    public static String generateURL() {
        def metaDataMap = UploadUtils.loadProperties("${HOME}/archiveProj/URLGeneratorMetadata.properties")
        String fullURL = baseUrl + metaDataMap."${ARCHIVE_PROFILE}.subjects" + ampersand + metaDataMap."${ARCHIVE_PROFILE}.language" + ampersand + metaDataMap."${ARCHIVE_PROFILE}.description" + ampersand + metaDataMap."${ARCHIVE_PROFILE}.creator"
        if (metaDataMap."${ARCHIVE_PROFILE}.collection") {
            fullURL += ampersand + metaDataMap."${ARCHIVE_PROFILE}.collection"
        }

        println fullURL
        return fullURL
    }

    public static String pickFolderBasedOnArchiveProfile(){
        if(ARCHV_PRFL == PROFILE_ENUMS.dt){
             FOLDER_NAME = "C:\\hw\\avn\\AvnManuscripts\\GopinathKavirajTantricSahityaList"
        }
        else if(ARCHV_PRFL == PROFILE_ENUMS.rk){
            FOLDER_NAME = "C:\\hw\\megha"
        }
        else if(ARCHV_PRFL == PROFILE_ENUMS.jg){
            FOLDER_NAME = "C:\\hw\\amit"
        }

        return FOLDER_NAME
    }

}

