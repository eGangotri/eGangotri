package com.egangotri.upload.archive


import com.egangotri.rest.UploadRestApiCalls
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import com.egangotri.upload.util.ChromeDriverConfig
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.UnhandledAlertException
import java.net.URLEncoder

import java.time.Duration


import static com.egangotri.upload.util.ArchiveUtil.*

@Slf4j
class ArchiveHandler {

    static List<Integer> uploadAllItemsToArchiveByProfile(
            Map metaDataMap, Set<QueuedVO> uploadVos, boolean reupload = false) {
        int countOfUploadedItems = 0
        int uploadFailureCount = 0
        try {
            ChromeDriver driver = ChromeDriverConfig.createDriver()
            String archiveProfile = uploadVos.first().archiveProfile
            List<String> uploadables = uploadVos*.path
            if (!navigateLoginLogic(driver, metaDataMap, archiveProfile)) {
                log.info("cant continue. Login Failed")
                return [countOfUploadedItems, uploadFailureCount]
            }
            if (uploadVos) {
                log.info "Ready to upload ${uploadables.size()} Items(s) for Profile ${uploadVos.first().archiveProfile}"
                //Start Upload of First File in Root Tab
                log.info "Uploading: ${uploadVos.first().title}"
                EGangotriUtil.sleepTimeInSeconds(0.2)
                getResultsCount(driver, true)
                try {
                    if(reupload){
                            log.info("Reuploading ${uploadVos.first().path}")
                            String archiveItemId = uploadVos.first().archiveItemId
                            uploadOneItemV2(driver, uploadVos.first(), archiveItemId)
                        }
                    else {
                            uploadOneItem(driver, uploadVos.first())
                        }
                    countOfUploadedItems++
                }
                catch (Exception e) {
                    log.info("Exception while uploading(${uploadables[0]}). ${(uploadables.size() > 1) ? '\nwill proceed to next tab' : ''}:${e.message}", e)
                    uploadFailureCount++
                }
                // Upload Remaining Files by generating New Tabs
                if (uploadables.size() > 1) {
                    int tabIndex = 1
                    for (uploadVo in uploadVos.drop(1)) {
                        if (uploadFailureCount > EGangotriUtil.UPLOAD_FAILURE_THRESHOLD) {
                            String errMsg = "Too many upload Exceptions More than ${EGangotriUtil.UPLOAD_FAILURE_THRESHOLD}. Quittimg"
                            log.info(errMsg)
                            throw new Exception(errMsg)
                        }

                        log.info "\nUploading: ${uploadVo.title} @ tabNo:$tabIndex"
                        if (!UploadUtils.openNewTab(driver)) {
                            uploadFailureCount++
                            continue
                        }

                        //Switch to new Tab
                        boolean _tabSwitched = UploadUtils.switchToLastOpenTab(driver)
                        if (_tabSwitched) {
                            tabIndex++
                        } else {
                            log.info("Tab Creation Failed during uppload of ${uploadVo.title}.")
                            uploadFailureCount++

                            continue
                        }

                        //Start Upload
                        try {
                            if(reupload){
                                log.info("Reuploading ${uploadVo.path}")
                                String archiveItemId = uploadVo.archiveItemId
                                uploadOneItemV2(driver, uploadVo, archiveItemId)
                            }
                            else {
                                uploadOneItem(driver, uploadVo)
                            }

                        }
                        catch (UnhandledAlertException uae) {
                            log.error("UnhandledAlertException while uploading(${uploadVo.title}.")
                            log.error("will proceed to next tab: ${uae.message}")
                            UploadUtils.hitEnterKey()
                            uploadFailureCount++
                            log.info("Attempt-2 following UnhandledAlertException for ('${uploadVo.title}').")
                            try {
                                if (!UploadUtils.openNewTab(driver)) {
                                    uploadFailureCount++
                                }
                                tabIndex++
                                boolean tabSwitched = UploadUtils.switchToLastOpenTab(driver)
                                if (!tabSwitched) {
                                    log.error("tab not switched. contiuing to next")
                                    continue
                                }
                                uploadOneItem(driver, uploadVo)
                                log.info("****Attempt-2 succeeded if you see this for File '${uploadVo.title}'")
                            }
                            catch (UnhandledAlertException uae2) {
                                log.info("UnhandledAlertException while uploading(${uploadVo.title}).\n will proceed to next tab: ${uae2.message}", uae2)
                                UploadUtils.hitEnterKey()
                                uploadFailureCount++
                                log.info("Failed. Attempt-2 for (${uploadVo.title}). following UnhandledAlertException")
                                continue
                            }
                            catch (Exception e) {
                                log.info("Exception while uploading:(${uploadVo.title}).\n will proceed to next tab:${e.message}", e)
                                uploadFailureCount++
                                continue
                            }
                        }
                        catch (Exception e) {
                            log.info("Exception while uploading::(${uploadVo.title}).\n will proceed to next tab:${e.message}")
                            uploadFailureCount++
                            continue
                        }
                        countOfUploadedItems++
                    }
                }
                getResultsCount(driver, false)
                UploadUtils.maximizeBrowser(driver)
            } else {
                log.info "No File uploadable for profile $archiveProfile"
            }
        }
        catch (Exception e) {
            e.printStackTrace()
        }

        return [countOfUploadedItems, uploadFailureCount]
    }

    static int checkForMissingUploadsInArchive(String archiveUrl, List<String> fileNames) {
        int countOfUploadedItems = 0
        EGangotriUtil.sleepTimeInSeconds(4)
        try {

            ChromeDriver driver = ChromeDriverConfig.createDriver()
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
    }


    static void generateAllUrls(String archiveProfile, List<String> uploadables) {
        uploadables.eachWithIndex { fileName, tabIndex ->
            String uploadLink = UploadUtils.generateUploadUrl(archiveProfile, fileName)
            log.info("${tabIndex + 1}) [$archiveProfile] $uploadLink")
        }
    }

    static List<List<Integer>> performPartitioningAndUploadToArchive(Map metaDataMap, Set<UploadVO> uploadVos, boolean reupload = false) {
        List<List<Integer>> uploadStatsList = []
        if (EGangotriUtil.PARTITIONING_ENABLED && uploadVos.size() > EGangotriUtil.PARTITION_SIZE) {
            String archiveProfile = uploadVos.first().archiveProfile
            Set<Set<QueuedVO>> partitions = UploadUtils.partition(uploadVos as List<QueuedVO>, EGangotriUtil.PARTITION_SIZE)
            log.info(" ${partitions.size()} Browsers will be created for Profile $archiveProfile: ")
            int partitionCounter = 0
            for (Set<QueuedVO> partitionedVos : partitions) {
                log.info("Batch # ${++partitionCounter}/${partitions.size()}. ${partitionedVos.size()} Item(s) queued for upload")
                List<Integer> uploadStats = uploadAllItemsToArchiveByProfile(metaDataMap, partitionedVos, reupload)
                uploadStatsList << uploadStats
            }
        } else {
            log.info("No partitioning")
            List<Integer> uploadStats = uploadAllItemsToArchiveByProfile(metaDataMap, uploadVos as Set<QueuedVO>, reupload)
            uploadStatsList << uploadStats
        }
        uploadStatsList
    }


    static <T extends UploadVO> String uploadOneItem(ChromeDriver driver, T uploadVO) {
        String fileNameWithPath = uploadVO.path
        String uploadLink = uploadVO.uploadLink
        String archiveProfile = uploadVO.archiveProfile

        if (EGangotriUtil.CREATOR_FROM_DASH_SEPARATED_STRING && !EGangotriUtil.GENERATE_RANDOM_CREATOR && !EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS.contains(archiveProfile)) {
            String fileNameOnly = UploadUtils.stripFilePathAndFileEnding(fileNameWithPath)
            String strAfterDash = UploadUtils.getLastPortionOfTitleUsingSeparator(fileNameOnly).trim()
            if (fileNameOnly.contains("-")) {
                String _strAfterDash = strAfterDash?.replaceAll(/[#!&]/,"")
                uploadLink = uploadLink.contains("creator=") ? uploadLink.split("creator=").first() + "creator=" + _strAfterDash  : uploadLink
            }
            if (uploadLink.contains("subject=null")) {
                String _strAfterDash = strAfterDash?.replaceAll(/[#!&]/,"")
                String _fileNameOnly = fileNameOnly?.replaceAll(/[#!&]/,'')
                uploadLink = uploadLink.replaceAll("subject=null", "subject=${_strAfterDash ?: _fileNameOnly}")
            }
        }

        uploadLink = uploadLink.replaceAll(/[#!]/, "").replaceAll("null", " ")
        //uploadLink += "&uploader=info@archive.org" 
        log.info("\tURL for upload: \n${uploadLink}")
        log.info("\tfileNameWithPath:'${UploadUtils.stripFilePath(fileNameWithPath)}' ready for upload")

        def encodedUploadLink = uploadLink.replaceAll("\n", "%0A%0A")
        println "encodedUploadLink: ${encodedUploadLink}"
        println "uploadLink: ${uploadLink}"

        //Go to URL
        driver.navigate().to(encodedUploadLink)
        driver.get(encodedUploadLink)

        WebDriverWait waitForChooseFilesToUploadToShowUp = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TIMEOUT_IN_TWO_SECONDS))
        try {
            waitForChooseFilesToUploadToShowUp.until(
                    ExpectedConditions.elementToBeClickable(By.className(UploadUtils.CHOOSE_FILES_TO_UPLOAD_BUTTON_AS_CLASS)))
        }
        catch (WebDriverException webDriverException) {
            UploadUtils.hitEscapeKey()
            log.info("\tCannot find Upload Button. " +
                    "\tHence quitting by clicking escape key so that tabbing can resume and other uploads can continue. This one has failed though\n" + webDriverException.message)
            throw new Exception("Cant click Choose-Files-To-Upload Button")
        }

        UploadUtils.uploadFileUsingSendKeys(driver, fileNameWithPath)
        new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TIMEOUT_IN_TWO_SECONDS)).until(ExpectedConditions
                .elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))

        WebElement licPicker = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_DIV))
        licPicker.click()
        WebElement radioBtn = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_RADIO_OPTION))
        radioBtn.click()
        if (!fileNameWithPath.endsWith(EGangotriUtil.PDF) && !uploadLink.contains("collection=")) {
            WebElement collectionSpan = driver.findElement(By.id("collection"))
            new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)).until(ExpectedConditions.elementToBeClickable(By.id("collection")))
            collectionSpan.click()
            Select collDropDown = new Select(driver.findElement(By.name("mediatypecollection")))
            collDropDown.selectByValue("data:opensource_media")
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait2.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON)))
        String identifier = driver.findElement(By.id(UploadUtils.PAGE_URL_ITEM_ID)).getText()

        if (true) {
            identifier = extendIdentifierByPrepending(identifier)
            driver.findElement(By.id(UploadUtils.PAGE_URL)).click()
            WebElement pgUrlInputField = driver.findElement(By.className(UploadUtils.PAGE_URL_INPUT_FIELD))
            pgUrlInputField.clear()
            pgUrlInputField.sendKeys(identifier)
            pgUrlInputField.sendKeys(Keys.ENTER)
            boolean alertWasDetected = UploadUtils.checkAlert(driver, false)
            //for a strange reason the first tab doesnt have alert
            //after that have alert. alert text is always nulll
            if (alertWasDetected) {
                log.info("alert detected while identifier was being tweaked")
                pgUrlInputField.click()
                pgUrlInputField.sendKeys(Keys.ENTER)
            }
            WebDriverWait wait3 = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
            wait3.until(ExpectedConditions.visibilityOfElementLocated(By.id(UploadUtils.PAGE_URL_ITEM_ID)))
            String identifierNowInTextBox = driver.findElement(By.id(UploadUtils.PAGE_URL_ITEM_ID)).getText()
            ///log.info("Is our tweaked identifier ->${identifier}<- == ->${identifierNowInTextBox}<- [identifier in text Box Now] (${identifier == identifierNowInTextBox })")
            identifier = identifierNowInTextBox
        }
        String accessUrl = "${ARCHIVE_DOCUMENT_DETAIL_URL}/${identifier}"
        log.info("\tidentifier: ${identifier}")
        log.info("\tAccess Url: ${accessUrl}")
        ALL_ACCESS_URLS_GENERATED_IN_UPLOAD_CYCLE << """${uploadVO.title}
        ${accessUrl}
        """.toString()
        storeArchiveIdentifierInFile(uploadVO, identifier)

        WebDriverWait wait4 = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait4.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebElement uploadButton = driver.findElement(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON))
        uploadButton.click()
        EGangotriUtil.GLOBAL_UPLOADING_COUNTER++
        garbageCollectAndPrintMemUsageInfoOnEvery100thUpload()
        log.info("\tDocument # ${EGangotriUtil.GLOBAL_UPLOADING_COUNTER}/${GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} sent for upload @ ${UploadUtils.getFormattedDateString()}")
        if (SettingsUtil.WRITE_TO_MONGO_DB) {
            try {
                UploadRestApiCalls.addToQueue(uploadVO, EGangotriUtil.UPLOAD_CYCLE_ID, "X");
                UploadRestApiCalls.addToUshered(uploadVO, EGangotriUtil.UPLOAD_CYCLE_ID, "X", identifier);
            }
            catch (Exception e) {
                log.info("Exception calling addToUshered", e)
            }
        }
        return identifier
    }

    static <T extends UploadVO> String uploadOneItemV2(ChromeDriver driver, T uploadVO, String archiveItemId) {
        String fileNameWithPath = uploadVO.path
        String uploadLink = uploadVO.uploadLink

        log.info("\tURL for upload: \n${uploadLink}")
        log.info("\tfileNameWithPath:'${UploadUtils.stripFilePath(fileNameWithPath)}' ready for upload")
        //Go to URL
        driver.navigate().to(uploadLink)
        driver.get(uploadLink)

        WebDriverWait waitForChooseFilesToUploadToShowUp = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TIMEOUT_IN_TWO_SECONDS))
        try {
            waitForChooseFilesToUploadToShowUp.until(
                    ExpectedConditions.elementToBeClickable(By.className(UploadUtils.CHOOSE_FILES_TO_UPLOAD_BUTTON_AS_CLASS)))
        }
        catch (WebDriverException webDriverException) {
            UploadUtils.hitEscapeKey()
            log.info("\tCannot find Upload Button. " +
                    "\tHence quitting by clicking escape key so that tabbing can resume and other uploads can continue. This one has failed though\n" + webDriverException.message)
            throw new Exception("Cant click Choose-Files-To-Upload Button")
        }

        UploadUtils.uploadFileUsingSendKeys(driver, fileNameWithPath)
        new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TIMEOUT_IN_TWO_SECONDS)).until(ExpectedConditions
                .elementToBeClickable(By.id(UploadUtils.LICENSE_PICKER_DIV)))

        WebElement licPicker = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_DIV))
        licPicker.click()
        WebElement radioBtn = driver.findElement(By.id(UploadUtils.LICENSE_PICKER_RADIO_OPTION))
        radioBtn.click()
        if (!fileNameWithPath.endsWith(EGangotriUtil.PDF) && !uploadLink.contains("collection=")) {
            WebElement collectionSpan = driver.findElement(By.id("collection"))
            new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)).until(ExpectedConditions.elementToBeClickable(By.id("collection")))
            collectionSpan.click()
            Select collDropDown = new Select(driver.findElement(By.name("mediatypecollection")))
            collDropDown.selectByValue("data:opensource_media")
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait2.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON)))
        driver.findElement(By.id(UploadUtils.PAGE_URL)).click()
        WebElement pgUrlInputField = driver.findElement(By.className(UploadUtils.PAGE_URL_INPUT_FIELD))
        pgUrlInputField.clear()
        pgUrlInputField.sendKeys(archiveItemId)
        pgUrlInputField.sendKeys(Keys.ENTER)
        boolean alertWasDetected = UploadUtils.checkAlert(driver, false)
        //for a strange reason the first tab doesnt have alert
        //after that have alert. alert text is always nulll
        if (alertWasDetected) {
            log.info("alert detected while identifier was being tweaked")
            pgUrlInputField.click()
            pgUrlInputField.sendKeys(Keys.ENTER)
        }
        WebDriverWait wait3 = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait3.until(ExpectedConditions.visibilityOfElementLocated(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebDriverWait wait4 = new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS))
        wait4.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.PAGE_URL_ITEM_ID)))

        WebElement uploadButton = driver.findElement(By.id(UploadUtils.UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON))
        uploadButton.click()
        EGangotriUtil.GLOBAL_UPLOADING_COUNTER++
        garbageCollectAndPrintMemUsageInfoOnEvery100thUpload()
        log.info("\tDocument # ${EGangotriUtil.GLOBAL_UPLOADING_COUNTER}/${GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} sent for upload @ ${UploadUtils.getFormattedDateString()}")
        return archiveItemId
    }
}