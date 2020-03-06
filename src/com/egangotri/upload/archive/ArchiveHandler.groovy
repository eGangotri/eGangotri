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
    static String ARCHIVE_LOGIN_URL = "https://archive.org/account/login.php"
    static String ARCHIVE_USER_ACCOUNT_URL = "https://archive.org/details/@ACCOUNT_NAME"

    static void loginToArchive(def metaDataMap, String archiveProfile) {
        logInToArchiveOrg(new ChromeDriver(), metaDataMap, archiveProfile)
    }

    static List<List<Integer>> performPartitioningAndUploadToArchive(def metaDataMap, String archiveProfile) {
        return performPartitioningAndUploadToArchive(metaDataMap, archiveProfile, true)
    }

    static boolean logInToArchiveOrg(WebDriver driver, def metaDataMap, String archiveProfile) {
        boolean loginSucess = false
        try {
            driver.get(ARCHIVE_LOGIN_URL)
            log.info("Login to Archive URL $ARCHIVE_LOGIN_URL")
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
            EGangotriUtil.sleepTimeInSeconds(0.2)
            WebDriverWait wait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
            wait.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.USER_MENU_ID)))
            loginSucess = true
        }
        catch (Exception e) {
            log.info("Exeption in logInToArchiveOrg ${e.message}")
            e.printStackTrace()
            throw e
        }
        return loginSucess
    }


    static List<Integer> uploadAllItemsToArchiveByProfile(
            def metaDataMap, String archiveProfile, boolean uploadPermission, List<String> uploadables) {
        int countOfUploadedItems = 0
        int uploadFailureCount = 0

        // HashMap<String,String> mapOfArchiveIdAndFileName = [:]
        try {
            ChromeOptions options = new ChromeOptions()
            // This will disable [1581249040.339][SEVERE]: Timed out receiving message from renderer: 0.100E:\Sri Vatsa\Books\Buddhism\Bhikkhu Sujato\A-History-of-Mindfulness-How-Insight-Worsted-Tranquillity-in-the-Satipaṭṭhāna-Sutta Bhikkhu-Sujato.pdf
            options.setPageLoadStrategy(PageLoadStrategy.NONE)
            WebDriver driver = new ChromeDriver(/*options*/)
            boolean loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile)
            if (!loginSuccess) {
                log.info("Login failed once for ${archiveProfile}. will give it one more shot")
                loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile)
            }
            if (!loginSuccess) {
                log.info("Login failed for Second Time for ${archiveProfile}. will now quit")
                throw new Exception("Not Continuing becuase of Login Failure twice")
            }

            if (uploadPermission) {
                if (uploadables) {
                    String uploadedItemIdentifier = ""
                    log.info "Ready to upload ${uploadables.size()} Pdf(s) for Profile $archiveProfile"
                    //Get Upload Link
                    String uploadLink = UploadUtils.generateURL(archiveProfile, uploadables[0])
                    //Start Upload of First File in Root Tab
                    log.info "Uploading: ${uploadables[0]}"
                    EGangotriUtil.sleepTimeInSeconds(0.2)
                    getResultsCount(driver, "LoginTime")
                    try {
                        uploadedItemIdentifier = ArchiveHandler.uploadOneItem(driver, uploadables[0], uploadLink, archiveProfile)
                        countOfUploadedItems++
                    }
                    catch (Exception e) {
                        log.info("Exception while uploading(${uploadables[0]}). ${(uploadables.size() > 1) ? '\nwill proceed to next tab' : ''}:${e.message}")
                        uploadFailureCount++
                    }
                    // mapOfArchiveIdAndFileName.put(archiveIdentifier, uploadables[0])
                    // Upload Remaining Files by generating New Tabs
                    if (uploadables.size() > 1) {
                        int tabIndex = 1
                        for (uploadableFile in uploadables.drop(1)) {
                            log.info "Uploading: ${UploadUtils.getFileTitleOnly(uploadableFile)} @ tabNo:$tabIndex"
                            UploadUtils.openNewTab()

                            //Switch to new Tab
                            boolean _tabSwitched = UploadUtils.switchToLastOpenTab(driver)
                            if (_tabSwitched) {
                                tabIndex++
                            } else {
                                log.info("Tab Creation Failed.")
                                continue
                            }
                            uploadLink = UploadUtils.generateURL(archiveProfile, uploadableFile)

                            //Start Upload
                            try {
                                uploadedItemIdentifier = uploadOneItem(driver, uploadableFile, uploadLink, archiveProfile)
                            }
                            catch (UnhandledAlertException uae) {
                                log.error("UnhandledAlertException while uploading(${UploadUtils.getFileTitleOnly(uploadableFile)}.")
                                log.error("will proceed to next tab: ${uae.message}")
                                UploadUtils.hitEnterKey()
                                uploadFailureCount++
                                log.info("Attempt-2 following UnhandledAlertException for ('${UploadUtils.getFileTitleOnly(uploadableFile)}').")
                                try {
                                        UploadUtils.openNewTab()
                                        tabIndex++
                                        boolean tabSwitched = UploadUtils.switchToLastOpenTab(driver)
                                        if (!tabSwitched) {
                                        log.error("tab not switched. contiuing to next")
                                        continue
                                    }
                                    uploadedItemIdentifier = uploadOneItem(driver, uploadableFile, uploadLink, archiveProfile)
                                    log.info("****Attempt-2 succeeded if you see this for File '${UploadUtils.getFileTitleOnly(uploadableFile)}'")
                                }
                                catch (UnhandledAlertException uae2) {
                                    log.info("UnhandledAlertException while uploading(${UploadUtils.getFileTitleOnly(uploadableFile)}).\n will proceed to next tab: ${uae2.message}")
                                    UploadUtils.hitEnterKey()
                                    uploadFailureCount++
                                    log.info("Failed. Attempt-2 for (${UploadUtils.getFileTitleOnly(uploadableFile)}). following UnhandledAlertException")
                                    continue
                                }
                                catch (Exception e) {
                                    log.info("Exception while uploading(${UploadUtils.getFileTitleOnly(uploadableFile)}).\n will proceed to next tab:${e.message}")
                                    uploadFailureCount++
                                    continue
                                }
                            }
                            catch (Exception e) {
                                log.info("Exception while uploading(${UploadUtils.getFileTitleOnly(uploadableFile)}).\n will proceed to next tab:${e.message}")
                                uploadFailureCount++
                                continue
                            }
                            if (uploadFailureCount > EGangotriUtil.UPLOAD_FAILURE_THRESHOLD) {
                                log.info("Too many upload Exceptions More than ${EGangotriUtil.UPLOAD_FAILURE_THRESHOLD}. Quittimg")
                                throw new Exception("Too many upload Exceptions More than ${EGangotriUtil.UPLOAD_FAILURE_THRESHOLD}. Quittimg")
                            }
                            countOfUploadedItems++
                            // mapOfArchiveIdAndFileName.put(rchvIdntfr, fileName)
                        }
                    }
                    getResultsCount(driver, "UploadCompletionTime")
                } else {
                    log.info "No File uploadable for profile $archiveProfile"
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace()
        }

        return [countOfUploadedItems, uploadFailureCount]
    }

    static void getResultsCount(WebDriver driver, String sentenceFragment) {
        WebElement avatar = driver.findElementByClassName("avatar")
        String userName = avatar.getAttribute("alt")
        log.info("userName: ${userName}")
        String archiveUserAccountUrl = ARCHIVE_USER_ACCOUNT_URL.replace("ACCOUNT_NAME", userName.toLowerCase())
        if(sentenceFragment == "UploadCompletionTime"){
            UploadUtils.openNewTab()
            UploadUtils.switchToLastOpenTab(driver)
            driver.navigate().to(archiveUserAccountUrl)

        }
        driver.get(archiveUserAccountUrl)
        WebDriverWait webDriverWait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.className("results_count")))
        WebElement resultsCount = driver.findElementByClassName("results_count")
        if (resultsCount) {
            log.info("Results Count at $sentenceFragment: " + resultsCount.text)
            if(sentenceFragment == "UploadCompletionTime"){
                log.info("**Figure captured will update in a while. So not exctly accurate as upload are still happening")
            }
        }
    }

    static int checkForMissingUploadsInArchive(String archiveUrl, List<String> fileNames) {
        int countOfUploadedItems = 0
        EGangotriUtil.sleepTimeInSeconds(4)
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
                    log.info "$numOfUploads $fileName"
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
            log.info("$tabIndex) [$archiveProfile] $uploadLink")
        }
    }


    static List<List<Integer>> performPartitioningAndUploadToArchive(def metaDataMap, String archiveProfile, boolean uploadPermission) {
        List<String> uploadables = UploadUtils.getUploadablePdfsForProfile(archiveProfile)

        List<List<Integer>> uploadStatsList = []
        if (EGangotriUtil.PARTITIONING_ENABLED && uploadables.size > EGangotriUtil.PARTITION_SIZE) {
            def partitions = UploadUtils.partition(uploadables, EGangotriUtil.PARTITION_SIZE)
            log.info("uploadables will be uploaded in ${partitions.size} # of Browsers: ")

            for (List<String> partitionedUploadables : partitions) {
                log.info("Batch of partitioned Items Count ${partitionedUploadables.size} sent for uploads")
                List<Integer> uploadStats = uploadAllItemsToArchiveByProfile(metaDataMap, archiveProfile, uploadPermission, partitionedUploadables)
                uploadStatsList << uploadStats
            }
        } else {
            log.info("No partitioning")
            List<Integer> uploadStats = uploadAllItemsToArchiveByProfile(metaDataMap, archiveProfile, uploadPermission, uploadables)
            uploadStatsList << uploadStats
        }
        uploadStatsList
    }


    static String uploadOneItem(WebDriver driver, String fileNameWithPath, String uploadLink, String archiveProfile) {
        if(EGangotriUtil.CREATOR_FROM_DASH_SEPARATED_STRING && !EGangotriUtil.GENERATE_RANDOM_CREATOR && !EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS.contains(archiveProfile)){
            String lastStringFragAfterDash = UploadUtils.getLastPortionOfTitleUsingSeparator(fileNameWithPath)
            String removeFileEnding = '"' + UploadUtils.removeFileEnding(lastStringFragAfterDash) + '"'
            uploadLink = uploadLink.contains("creator=") ? uploadLink.split("creator=").first() + "creator=" + removeFileEnding : uploadLink
        }
        log.info("URL for upload: \n${uploadLink}")
        log.info("fileNameWithPath:'${UploadUtils.getFileTitleOnly(fileNameWithPath)}' ready for upload")
        //Go to URL
        driver.navigate().to(uploadLink)
        driver.get(uploadLink)

        WebDriverWait waitForFileButtonInitial = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        log.info("waiting for ${UploadUtils.CHOOSE_FILES_TO_UPLOAD_BUTTON} to be clickable")
        try {
            waitForFileButtonInitial.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.CHOOSE_FILES_TO_UPLOAD_BUTTON)))
        }
        catch (WebDriverException webDriverException) {
            UploadUtils.hitEscapeKey()
            log.info("Cannot find Upload Button. " +
                    "Hence quitting by clicking escape key so that tabbing can resume and other uploads can continue. This one has failed though\n" + webDriverException.message)
            throw new Exception("Cant click Choose-Files-To-Upload Button")
        }
        UploadUtils.clickChooseFilesToUploadButtonAndPasteFilePath(driver, fileNameWithPath)
        log.info("waiting for ${UploadUtils.LICENSE_PICKER_DIV} to be clickable")

        try {
            new WebDriverWait(driver, EGangotriUtil.TIMEOUT_IN_TWO_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))
        }
        catch (WebDriverException webDriverException) {
            log.info("WebDriverException(1). Couldnt find (${UploadUtils.LICENSE_PICKER_DIV}). while uploading('${UploadUtils.getFileTitleOnly(fileNameWithPath)}').(${webDriverException.message}) ")
            UploadUtils.hitEscapeKey()
            UploadUtils.clickChooseFilesToUploadButtonAndPasteFilePath(driver, fileNameWithPath)
            try {
                log.info("Attempt-2")
                new WebDriverWait(driver, EGangotriUtil.TIMEOUT_IN_TWO_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))
                log.info("'${UploadUtils.getFileTitleOnly(fileNameWithPath)}' must have succeeded if u see this")
            }
            catch (WebDriverException webDriverException2) {
                log.info("WebDriverException(2). Couldnt find (${UploadUtils.LICENSE_PICKER_DIV}). \nwhile uploading('${UploadUtils.getFileTitleOnly(fileNameWithPath)}').\n(${webDriverException2.message}) ")
                log.info("Attempt-3")
                UploadUtils.hitEscapeKey()
                UploadUtils.clickChooseFilesToUploadButtonAndPasteFilePath(driver, fileNameWithPath)
                new WebDriverWait(driver, EGangotriUtil.TIMEOUT_IN_TWO_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))
                log.info("'${UploadUtils.getFileTitleOnly(fileNameWithPath)}' must have succeeded if u see this")
            }
        }
        UploadUtils.checkAlert(driver)
        WebElement licPicker = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_DIV))
        licPicker.click()


        WebElement radioBtn = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_RADIO_OPTION))
        radioBtn.click()

        if (!fileNameWithPath.endsWith(EGangotriUtil.PDF) && !uploadLink.contains("collection=")) {
            WebElement collectionSpan = driver.findElement(By.id("collection"))
            new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS).until(ExpectedConditions.elementToBeClickable(By.id("collection")))

            collectionSpan.click()
            Select collDropDown = new Select(driver.findElement(By.name("mediatypecollection")))
            collDropDown.selectByValue("data:opensource_media")
        }

        //remove this junk value that pops-up for profiles with Collections
        /* if (uploadLink.contains("collection=")) {
             driver.findElement(By.className("additional_meta_remove_link")).click()
         }*/

        WebDriverWait wait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebDriverWait wait2 = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        wait2.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON)))
        Random _rndm = new Random()
        String identifier = driver.findElement(By.id(UploadUtils.PAGE_URL_ITEM_ID)).getText()
        log.info("identifier is ${identifier}")

        if(EGangotriUtil.ADD_RANDOM_INTEGER_TO_PAGE_URL){
            identifier += "_" + _rndm.nextInt(100)
            driver.findElement(By.id(UploadUtils.PAGE_URL)).click()
            driver.findElement(By.className(UploadUtils.PAGE_URL_INPUT_FIELD)).clear()
            driver.findElement(By.className(UploadUtils.PAGE_URL_INPUT_FIELD)).sendKeys(identifier)
            UploadUtils.hitEnterKey()
            WebDriverWait wait3 = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
            boolean alertWasDetected = UploadUtils.checkAlert(driver, false)
            //for a strange reason the first tab doesnt have alert
            //after that have alert. alert text is always nulll
            if(alertWasDetected){
                driver.findElement(By.className(UploadUtils.PAGE_URL_INPUT_FIELD)).click()
                UploadUtils.hitEnterKey()
            }
            wait3.until(ExpectedConditions.visibilityOfElementLocated(By.id(UploadUtils.PAGE_URL_ITEM_ID)))
            identifier = driver.findElement(By.id(UploadUtils.PAGE_URL_ITEM_ID)).getText()
            log.info("identifier after alteration is ${identifier}")
        }
        UploadUtils.storeArchiveIdentifierInFile(UploadUtils.getFileTitleOnly(fileNameWithPath),identifier)

        WebDriverWait wait4 = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        wait4.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebElement uploadButton = driver.findElement(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON))
        uploadButton.click()
        return identifier
    }
}