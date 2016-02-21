package com.egangotri.upload.archive

import com.egangotri.filter.NonPre57DirectoryFilter
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import org.apache.commons.logging.LogFactory
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.*

/**
 * Created by user on 2/7/2016.
 */
class ArchiveHandler {

    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static enum PROFILE_ENUMS {
        dt, ib, rk, jg
    }

    static String ARCHIVE_URL = "http://archive.org/account/login.php"
    static final String baseUrl = "http://archive.org/upload/?"
    static final String ampersand = "&"

    public static void loginToArchive(def metaDataMap, String archiveUrl, String archiveProfile) {
        uploadToArchive(metaDataMap, archiveUrl, archiveProfile, false)
    }

    public static void uploadToArchive(def metaDataMap, String archiveUrl, String archiveProfile) {
        uploadToArchive(metaDataMap, archiveUrl, archiveProfile, true)
    }

    public static void uploadToArchive(def metaDataMap, String archiveUrl, String archiveProfile, boolean upload) {
        try {

            WebDriver driver = new ChromeDriver()
            driver.get(archiveUrl);

            //Login
            WebElement id = driver.findElement(By.id("username"));
            WebElement pass = driver.findElement(By.id("password"));
            WebElement button = driver.findElement(By.id("submit"));

            id.sendKeys(metaDataMap."${archiveProfile}.username");
            pass.sendKeys(metaDataMap."kuta");
            button.click();

            if (upload) {
                List<String> uploadables = UploadUtils.getFiles(pickFolderBasedOnArchiveProfile(archiveProfile))
                if (uploadables) {
                    Log.info "Ready to upload ${uploadables.size()} Pdf(s) for Profile $archiveProfile"
                    //Get Upload Link
                    String uploadLink = generateURL(archiveProfile)

                    //Start Upload of First File in Root Tab
                    ArchiveHandler.upload(driver, uploadables[0], uploadLink)

                    // Upload Remaining Files by generating New Tabs
                    if (uploadables.size() > 1) {
                        uploadables.drop(1).eachWithIndex { fileName, tabNo ->
                            Log.info "Uploading: $fileName @ tabNo:$tabNo"
                            // Open new tab
                            driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "t");
                            //Switch to new Tab
                            ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
                            driver.switchTo().window(tabs.get(tabNo + 1));

                            //Start Upload
                            ArchiveHandler.upload(driver, fileName, uploadLink)
                        }
                    }
                } else {
                    Log.info "No File uploadable for profile $archiveProfile"
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace()
        }
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

        String identifier = driver.findElement( By.id("page_url")).getText() //By.xpath("//span[contains(@class, 'gray') and @id='page_url']"))
        println "identifier: $identifier"

        WebElement uploadButton = driver.findElement(By.id("upload_button"));
        uploadButton.click();
    }

    public static String generateURL(String archiveProfile) {
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/URLGeneratorMetadata.properties")
        String fullURL = baseUrl + metaDataMap."${archiveProfile}.subjects" + ampersand + metaDataMap."${archiveProfile}.language" + ampersand + metaDataMap."${archiveProfile}.description" + ampersand + metaDataMap."${archiveProfile}.creator"
        if (metaDataMap."${archiveProfile}.collection") {
            fullURL += ampersand + metaDataMap."${archiveProfile}.collection"
        }

        Log.info "generateURL($archiveProfile):fullURL"
        return fullURL
    }
    public static List<String> pickFolderBasedOnArchiveProfile(String archiveProfile) {
        List folderName = []

        switch (archiveProfile) {
            case PROFILE_ENUMS.dt.toString():
                folderName = [FileUtil.DT_DEFAULT]
                break

            case PROFILE_ENUMS.rk.toString():
                folderName = [FileUtil.RK_DEFAULT]
                break

            case PROFILE_ENUMS.jg.toString():
                folderName = [FileUtil.JG_DEFAULT]
                break

            case PROFILE_ENUMS.ib.toString():
                folderName = []

                FileUtil.ALL_FOLDERS.values().collect { UploadUtils.pre57SubFolders(it) }.each {
                    folderName << it
                }
                folderName = folderName.flatten()
                break
        }
        Log.info "pickFolderBasedOnArchiveProfile($archiveProfile): $folderName"
        return folderName
    }


}