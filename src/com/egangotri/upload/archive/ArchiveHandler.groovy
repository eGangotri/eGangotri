package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by user on 2/7/2016.
 */
class ArchiveHandler {

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
                    println "Ready to upload ${uploadables.size()} Pdf(s) for Profile $archiveProfile"
                    //Get Upload Link
                    String uploadLink = generateURL(archiveProfile)

                    //Start Upload of First File in Root Tab
                    ArchiveHandler.upload(driver, uploadables[0], uploadLink)

                    // Upload Remaining Files by generating New Tabs
                    if (uploadables.size() > 1) {
                        uploadables.drop(1).eachWithIndex { fileName, tabNo ->
                            println "Uploading: $fileName @ tabNo:$tabNo"
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
                    println "No File uploadable for profile $archiveProfile"
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

        WebElement uploadButton = driver.findElement(By.id("upload_button"));
        uploadButton.click();
    }

    public static String generateURL(String archiveProfile) {
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/URLGeneratorMetadata.properties")
        String fullURL = baseUrl + metaDataMap."${archiveProfile}.subjects" + ampersand + metaDataMap."${archiveProfile}.language" + ampersand + metaDataMap."${archiveProfile}.description" + ampersand + metaDataMap."${archiveProfile}.creator"
        if (metaDataMap."${archiveProfile}.collection") {
            fullURL += ampersand + metaDataMap."${archiveProfile}.collection"
        }

        println fullURL
        return fullURL
    }

    public static List pickFolderBasedOnArchiveProfile(String archiveProfile) {
        List folderName

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

                pre57SubFolders(FileUtil.RK_DEFAULT + File.separator + FileUtil.PRE_57).each {
                    folderName << it
                }

                pre57SubFolders(FileUtil.JG_DEFAULT + File.separator + FileUtil.PRE_57).each {
                    folderName << it
                }
                break
        }
        println "folderName: $folderName archiveProfile: " + archiveProfile
        return folderName
    }

    static List pre57SubFolders(File directory) {
        List subFolders = []
        List<File> subDirs = directory.listFiles()?.toList()
        if (anyPre57Folder(subDirs)) {
            File _fl
            subFolders << subDirs.find { _fl.name.equals(FileUtil.PRE_57) }.absolutePath
        } else {
            //Go One Level Deep
            if (subDirs) {
                for (int i = 0; i < subDirs.size(); i++) {
                    File oneLevelDeep = subDirs[i]
                    if (oneLevelDeep.isDirectory()) {
                        if (anyPre57Folder(oneLevelDeep.listFiles().toList())) {
                            subFolders << (oneLevelDeep.listFiles().find { File _sbFl -> _sbFl.name.equals(FileUtil.PRE_57) }).absolutePath
                        }
                    }
                }
            }
        }
        return subFolders
    }

    static boolean anyPre57Folder(List<File> subDirs) {
        if (subDirs) {
            if (subDirs.any { it.isDirectory() && it.name.equals(FileUtil.PRE_57) }) {
                return true
            }
        }

        return false
    }

    static List pre57SubFolders(String folderName) {
        return pre57SubFolders(new File(folderName))
    }
}