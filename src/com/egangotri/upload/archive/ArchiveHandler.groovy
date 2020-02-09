package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.UnhandledAlertException

@Slf4j
class ArchiveHandler {
    static final int ARCHIVE_WAITING_PERIOD = 200

    static String ARCHIVE_URL = "https://archive.org/account/login.php"
    static final int UPLOAD_FAILURE_THRESHOLD = 5

    static void loginToArchive(def metaDataMap, String archiveProfile) {
        logInToArchiveOrg(new ChromeDriver(), metaDataMap, archiveProfile)
    }

    static List<Integer> uploadToArchive(def metaDataMap, String archiveProfile) {
        return uploadToArchive(metaDataMap, archiveProfile, true)
    }

    static boolean logInToArchiveOrg(WebDriver driver, def metaDataMap, String archiveProfile) {
        boolean loginSucess = false
        try {
            driver.get(ARCHIVE_URL)
            log.info("Login to Archive URL $ARCHIVE_URL")
            //Login
            WebElement id = driver.findElement(By.name(UploadUtils.USERNAME_TEXTBOX_NAME))
            WebElement pass = driver.findElement(By.name(UploadUtils.PASSWORD_TEXTBOX_NAME))
            WebElement button = driver.findElement(By.name(UploadUtils.LOGIN_BUTTON_NAME))

            String username = metaDataMap."${archiveProfile}.username"
            id.sendKeys(username)
            String kuta = metaDataMap."${archiveProfile}.kuta" ?: metaDataMap."kuta"
            pass.sendKeys(kuta)
            //button.click doesnt work
            button.submit()
            //pass.click()
            Thread.sleep(ARCHIVE_WAITING_PERIOD * 5)
            WebDriverWait wait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS);
            wait.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.USER_MENU_ID)));
            loginSucess = true
        }
        catch (Exception e) {
            log.info("Exeption in logInToArchiveOrg ${e.message}")
            e.printStackTrace()
            throw e
        }
        return loginSucess
    }


    static List<Integer> uploadToArchive(
            def metaDataMap, String archiveProfile, boolean uploadPermission, List<String> uploadables) {
        int countOfUploadedItems = 0
        int uploadFailureCount = 0

        Thread.sleep(4000)
        // HashMap<String,String> mapOfArchiveIdAndFileName = [:]
        try {
            ChromeOptions options = new ChromeOptions();
            // This will disable [1581249040.339][SEVERE]: Timed out receiving message from renderer: 0.100E:\Sri Vatsa\Books\Buddhism\Bhikkhu Sujato\A-History-of-Mindfulness-How-Insight-Worsted-Tranquillity-in-the-Satipaṭṭhāna-Sutta Bhikkhu-Sujato.pdf
            options.setPageLoadStrategy(PageLoadStrategy.NONE);
            WebDriver driver = new ChromeDriver(options)
            boolean loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile)
            if (!loginSuccess) {
                println("Login failed once for ${archiveProfile}. Will give it one more shot")
                loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile)
            }
            if (!loginSuccess) {
                println("Login failed for Second Time for ${archiveProfile}. will now quit")
                throw new Exception("Not Continuing becuase of Login Failure twice")
            }

            if (uploadPermission) {
                if (uploadables) {
                    log.info "Ready to upload ${uploadables.size()} Pdf(s) for Profile $archiveProfile"
                    //Get Upload Link
                    String uploadLink = UploadUtils.generateURL(archiveProfile, uploadables[0])

                    //Start Upload of First File in Root Tab
                    log.info "Uploading: ${uploadables[0]}"
                    Thread.sleep(ARCHIVE_WAITING_PERIOD)
                    ArchiveHandler.upload(driver, uploadables[0], uploadLink)
                    countOfUploadedItems++
                    // mapOfArchiveIdAndFileName.put(archiveIdentifier, uploadables[0])
                    // Upload Remaining Files by generating New Tabs
                    if (uploadables.size() > 1) {
                        int tabIndex = 1
                        for (uploadableFile in uploadables.drop(1)) {
                            log.info "Uploading: $uploadableFile @ tabNo:$tabIndex"
                            UploadUtils.openNewTab(0)

                            //Switch to new Tab
                            ArrayList<String> chromeTabsList = new ArrayList<String>(driver.getWindowHandles())
                            //there is a bug in retrieving the size of chromeTabsList in Selenium.
                            //use of last() instead of chromeTabsList.get(tabIndex+1) saves the issue
                            println "chromeTabsList.size(): ${chromeTabsList.size()} , tabIndex:$tabIndex"
                            try {
                                driver.switchTo().window(chromeTabsList.last())
                                tabIndex++
                            }
                            catch (Exception e) {
                                log.info("Exception while switching to new Tab", e)
                            }
                            uploadLink = UploadUtils.generateURL(archiveProfile, uploadableFile)

                            //Start Upload
                            try {
                                String rchvIdntfr = ArchiveHandler.upload(driver, uploadableFile, uploadLink)
                            }
                            catch (UnhandledAlertException uae) {
                                log.info("UnhandledAlertException while uploading. willl proceed to next tab: ${uae.message}")
                                UploadUtils.hitEnterKey()
                                uploadFailureCount++
                            }
                            catch (Exception e) {
                                log.info("Exception while uploading. willl proceed to next tab:${e.message}")
                                uploadFailureCount++
                            }
                            finally {
                                if (uploadFailureCount > UPLOAD_FAILURE_THRESHOLD) {
                                    println("Too many upload Exceptions More than ${UPLOAD_FAILURE_THRESHOLD}. Quittimg")
                                    throw new Exception("Too many upload Exceptions More than ${UPLOAD_FAILURE_THRESHOLD}. Quittimg")
                                }
                            }
                            countOfUploadedItems++
                            // mapOfArchiveIdAndFileName.put(rchvIdntfr, fileName)
                        }
                    }
                } else {
                    log.info "No File uploadable for profile $archiveProfile"
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace()
        }

        return [countOfUploadedItems, uploadFailureCount]
        //WriteToExcel.toCSV(mapOfArchiveIdAndFileName)
    }

    static int checkForMissingUploadsInArchive(String archiveUrl, List<String> fileNames) {
        int countOfUploadedItems = 0
        Thread.sleep(4000)
        try {

            WebDriver driver = new ChromeDriver()
            driver.get(archiveUrl)
            def results = []
            if (fileNames) {
                fileNames.each { fileName ->
                    log.info("Check for $fileName")
                    //Go to URL
                    driver.get(archiveUrl)

                    WebElement searchList = driver.findElement(By.className("searchlist"))
                    searchList.sendKeys(fileName)
                    searchList.submit()

                    //new WebDriverWait(driver, ARCHIVE_WAITING_PERIOD).until(ExpectedConditions.textToBePresentInElement(By.cssSelector("h3.co-top-row")))
                    String numOfUploads = driver.findElement(By.cssSelector("h3.co-top-row")).text
                    println "$numOfUploads $fileName"
                    results << [numOfUploads, fileName]
                }

            } else {
                log.info "No Filenames"
            }

        }
        catch (Exception e) {
            e.printStackTrace()
        }

        return countOfUploadedItems
        //WriteToExcel.toCSV(mapOfArchiveIdAndFileName)
    }


    static void generateAllUrls(String archiveProfile, List<String> uploadables) {
        uploadables.eachWithIndex { fileName, tabIndex ->
            String uploadLink = UploadUtils.generateURL(archiveProfile, fileName)
            log.info("$tabIndex) $uploadLink")
        }
    }


    static List<Integer> uploadToArchive(def metaDataMap, String archiveProfile, boolean uploadPermission) {
        List<String> uploadables = UploadUtils.getUploadablePdfsForProfile(archiveProfile)

        int uploadCount = 0
        int uploadFailureCount = 0
        List uploadStats = []

        if (EGangotriUtil.PARTITIONING_ENABLED && uploadables.size > EGangotriUtil.PARTITION_SIZE) {
            def partitions = UploadUtils.partition(uploadables, EGangotriUtil.PARTITION_SIZE)
            log.info("uploadables will be uploaded in ${partitions.size} # of Browsers: ")

            for (List<String> partitionedUploadables : partitions) {
                log.info("Batch of partitioned Items Count ${partitionedUploadables.size} sent for uploads")
                uploadStats = uploadToArchive(metaDataMap, archiveProfile, uploadPermission, partitionedUploadables)
                uploadCount += uploadStats[0]
                uploadFailureCount += uploadStats[1]
            }
        } else {
            log.info("No partitioning")
            uploadStats = uploadToArchive(metaDataMap, archiveProfile, uploadPermission, uploadables)
            uploadCount += uploadStats[0]
            uploadFailureCount += uploadStats[1]
        }
        [uploadCount, uploadFailureCount]
    }


    static String upload(WebDriver driver, String fileNameWithPath, String uploadLink) {
        log.info("fileNameWithPath:$fileNameWithPath ready for upload")
        //Go to URL
        driver.navigate().to(uploadLink);
        driver.get(uploadLink);

        WebDriverWait waitForFileButtonInitial = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        try {
            waitForFileButtonInitial.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.CHOOSE_FILES_TO_UPLOAD_BUTTON)))
        }
        catch (WebDriverException webDriverException) {
            UploadUtils.hitEscapeKey()
            println("Cannot find Upload Button. " +
                    "Hence quitting by clicking escape key so that tabbing can resume and other uploads can continue. This one has failed though" + webDriverException.message)
            throw new Exception("Cant click Choose-Files-To-Upload Button")
        }
        UploadUtils.clickChooseFilesToUploadButton(driver, fileNameWithPath)

        try {
            new WebDriverWait(driver, EGangotriUtil.TIMEOUT_IN_TWO_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))
        }

        catch (WebDriverException webDriverException) {
            log.info("Exception while uploading. willl proceed to next tab", webDriverException.message)
            UploadUtils.hitEscapeKey()
            UploadUtils.clickChooseFilesToUploadButton(driver, fileNameWithPath)
            try {
                new WebDriverWait(driver, EGangotriUtil.TIMEOUT_IN_TWO_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))
            }
            catch (WebDriverException webDriverException2) {
                log.info("Exception while uploading. willl proceed to next tab", webDriverException2.message)
                UploadUtils.hitEscapeKey()
                UploadUtils.clickChooseFilesToUploadButton(driver, fileNameWithPath)
                new WebDriverWait(driver, EGangotriUtil.TIMEOUT_IN_TWO_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))
            }
        }
        WebElement licPicker = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_DIV))
        licPicker.click()

        WebElement radioBtn = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_RADIO_OPTION))
        radioBtn.click()

        if (!fileNameWithPath.endsWith(EGangotriUtil.PDF) && !uploadLink.contains("collection=")) {
            WebElement collectionSpan = driver.findElement(By.id("collection"))
            new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id("collection")))

            collectionSpan.click()
            Select collDropDown = new Select(driver.findElement(By.name("mediatypecollection")))
            collDropDown.selectByValue("data:opensource_media");
        }

        //remove this junk value that pops-up for profiles with Collections
        /* if (uploadLink.contains("collection=")) {
             driver.findElement(By.className("additional_meta_remove_link")).click()
         }*/


        WebDriverWait wait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        wait.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON)))

        String identifier = "" //driver.findElement(By.id("item_id")).innerHtml()

        WebElement uploadButton = driver.findElement(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON))
        uploadButton.click()
        return identifier
    }
}