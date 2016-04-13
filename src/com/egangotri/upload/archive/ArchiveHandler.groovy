package com.egangotri.upload.archive

import com.egangotri.csv.WriteToExcel
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
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
    static ARCHIVE_WAITING_PERIOD = 8
    static enum ARCHIVE_PROFILE {
        DT, IB, RK, JG, NK, DD
    }

    static String ARCHIVE_URL = "http://archive.org/account/login.php"
    static final String baseUrl = "http://archive.org/upload/?"
    static final String ampersand = "&"

    public static void loginToArchive(def metaDataMap, String archiveUrl, String archiveProfile) {
        uploadToArchive(metaDataMap, archiveUrl, archiveProfile, false)
    }

    public static int uploadToArchive(def metaDataMap, String archiveUrl, String archiveProfile) {
        return uploadToArchive(metaDataMap, archiveUrl, archiveProfile, true)
    }

    public static int uploadToArchive(def metaDataMap, String archiveUrl, String archiveProfile, boolean upload) {
        int countOfUploadedItems = 0;
        Thread.sleep(2000)
       // HashMap<String,String> mapOfArchiveIdAndFileName = [:]
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
                List<String> uploadables = UploadUtils.getUploadablePdfsForProfile(archiveProfile)
                if (uploadables) {
                    Log.info "Ready to upload ${uploadables.size()} Pdf(s) for Profile $archiveProfile"
                    //Get Upload Link
                    String uploadLink = generateURL(archiveProfile)

                    //Start Upload of First File in Root Tab
                    String archiveIdentifier = ArchiveHandler.upload(driver, uploadables[0], uploadLink)
                    countOfUploadedItems++
                   // mapOfArchiveIdAndFileName.put(archiveIdentifier, uploadables[0])
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
                            String rchvIdntfr = ArchiveHandler.upload(driver, fileName, uploadLink)
                            countOfUploadedItems++
                           // mapOfArchiveIdAndFileName.put(rchvIdntfr, fileName)
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

        return countOfUploadedItems
        //WriteToExcel.toCSV(mapOfArchiveIdAndFileName)
    }


    public static String upload(WebDriver driver, String fileNameWIthPath, String uploadLink) {
        Log.info("$fileNameWIthPath goes to $uploadLink")
        //Go to URL
        driver.get(uploadLink);

        WebElement fileButtonInitial = driver.findElement(By.id("file_button_initial"));
        //((RemoteWebElement) fileButtonInitial).setFileDetector(new LocalFileDetector());
        fileButtonInitial.click();
        UploadUtils.pasteFileNameAndCloseUploadPopup(fileNameWIthPath)

        new WebDriverWait(driver, ARCHIVE_WAITING_PERIOD).until(ExpectedConditions.elementToBeClickable(By.id("license_picker_row")));

        WebElement licPicker = driver.findElement(By.id("license_picker_row"));
        licPicker.click()

        WebElement radioBtn = driver.findElement(By.id("license_radio_CC0"));
        radioBtn.click();

        //remove this junk value that pops-up for profiles with Collections
        if (uploadLink.contains("collection=")) {
            driver.findElement(By.className("additional_meta_remove_link")).click()
        }

        WebDriverWait wait = new WebDriverWait(driver, ARCHIVE_WAITING_PERIOD);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("upload_button")));

        String identifier = ""//driver.findElement( By.id("page_url")).getText() //By.xpath("//span[contains(@class, 'gray') and @id='page_url']"))
        println "identifier: $identifier"

        WebElement uploadButton = driver.findElement(By.id("upload_button"));
        uploadButton.click();
        return identifier
    }

    public static String generateURL(String archiveProfile) {
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_METADATA_PROPERTIES_FILE)
        String fullURL = baseUrl + metaDataMap."${archiveProfile}.subjects" + ampersand + metaDataMap."${archiveProfile}.language" + ampersand + metaDataMap."${archiveProfile}.description" + ampersand + metaDataMap."${archiveProfile}.creator"
        if (metaDataMap."${archiveProfile}.collection") {
            fullURL += ampersand + metaDataMap."${archiveProfile}.collection"
        }

        Log.info "generateURL($archiveProfile):fullURL"
        return fullURL
    }
    public static List<String> pickFolderBasedOnArchiveProfile(String archiveProfile) {
        List folderName = []

        if(EGangotriUtil.isAPreCutOffProfile(archiveProfile)){
            folderName = FileUtil.ALL_FOLDERS.values().toList() - FileUtil.ALL_FOLDERS."DT"
        }
        else{
            folderName = [FileUtil.ALL_FOLDERS."${archiveProfile.toUpperCase()}"]
        }

        Log.info "pickFolderBasedOnArchiveProfile($archiveProfile): $folderName"
        return folderName
    }


}