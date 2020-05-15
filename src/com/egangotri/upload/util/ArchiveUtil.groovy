package com.egangotri.upload.util

import com.egangotri.upload.vo.QueuedVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.text.DecimalFormat

@Slf4j
class ArchiveUtil {
    static String ARCHIVE_LOGIN_URL = "https://archive.org/account/login.php"
    static final String ARCHIVE_DOCUMENT_DETAIL_URL = "https://archive.org/details/"
    static String ARCHIVE_USER_ACCOUNT_URL = "${ARCHIVE_DOCUMENT_DETAIL_URL}@ACCOUNT_NAME"
    static int GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = 0
    static BigDecimal GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB = 0
    public static boolean VALIDATE_UPLOAD_AND_REUPLOAD_FAILED_ITEMS = false
    private static DecimalFormat df = new DecimalFormat("0.00");

    static void getResultsCount(ChromeDriver driver, Boolean _startTime = true) {
        WebElement userMenu = driver.findElement(By.xpath("//*[@id=\"wrap\"]/topnav-element"))
        JsonSlurper jsonSlurper = new JsonSlurper()
        log.info("userName: ${userMenu.getAttribute("config")}")

        String config = userMenu.getAttribute("config")
        String userName = jsonSlurper.parseText(config).username
        log.info("userName: ${userName}")

        String archiveUserAccountUrl = ARCHIVE_USER_ACCOUNT_URL.replace("ACCOUNT_NAME", userName.toLowerCase())
        if (!_startTime) {
            UploadUtils.openNewTab(driver)
            UploadUtils.switchToLastOpenTab(driver)
            driver.navigate().to(archiveUserAccountUrl)
        }
        driver.get(archiveUserAccountUrl)
        WebDriverWait webDriverWait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.className("results_count")))
        WebElement resultsCount = driver.findElementByClassName("results_count")
        if (resultsCount) {
            log.info("Results Count at ${_startTime ? "LoginTime" : 'UploadCompletionTime'}: " + resultsCount.text)
            if (!_startTime) {
                log.info("**Figure captured will update in a while. So not exctly accurate as upload are still happening")
            }
        }
    }

    static boolean navigateLoginLogic(ChromeDriver driver, Map metaDataMap, String archiveProfile) throws Exception {
        List<String> kuta = [metaDataMap."${archiveProfile}.${EGangotriUtil.KUTA}" ?: metaDataMap."${EGangotriUtil.KUTA}"] as List<String>
        if (metaDataMap."${EGangotriUtil.KUTA_SECOND}") {
            kuta << metaDataMap."${EGangotriUtil.KUTA_SECOND}"
        }

        boolean loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile, kuta.first())
        if (!loginSuccess) {
            log.info("Login failed once for ${archiveProfile}. will give it one more shot")
            //loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile, kuta.first())

            if (kuta.size() > 1 && !loginSuccess) {
                log.info("Login with Second Password ${archiveProfile}. Attempt 1")
                loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile, kuta.last())
                if (!loginSuccess) {
                    log.info("Login with Second Password ${archiveProfile}. will give it one more shot")
                    //loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile, kuta.last())
                }
            }
        }
        if (!loginSuccess) {
            log.info("Login failed for Second Time for ${archiveProfile}. will now quit")
            throw new Exception("Not Continuing because of Login Failure twice")
        }
        return loginSuccess
    }


    //create QueuedVO
    static Set<QueuedVO> generateVOsFromFileNames(String archiveProfile, List<String> uploadables) {
        Set<QueuedVO> vos = [] as Set
        uploadables.each { uploadable ->
            vos << new QueuedVO(archiveProfile, uploadable)
        }
        return vos
    }

    static Set<QueuedVO> generateUploadVoForAllUploadableItems(Collection<String> profiles) {
        Set<QueuedVO> vos = []
        profiles.eachWithIndex { String profile, index ->
            FileRetrieverUtil.getUploadablesForProfile(profile).each { String filePath ->
                QueuedVO vo = new QueuedVO(profile, filePath)
                vos << vo
            }
        }
        return vos
    }

    static void storeArchiveIdentifierInFile(UploadVO uploadVo, String _identifier) {
        String appendableFilePath = VALIDATE_UPLOAD_AND_REUPLOAD_FAILED_ITEMS ?
                EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE :
                EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE
        File appendableFile = new File(appendableFilePath)
        String appendable = voToCSVString(uploadVo, _identifier)
        appendableFile.append(appendable)

    }

    static void storeAllUplodableItemsInFile(Set<? extends UploadVO> vos) {
        String appendableFilePath = VALIDATE_UPLOAD_AND_REUPLOAD_FAILED_ITEMS ? EGangotriUtil.ARCHIVE_ITEMS_ALL_UPLOADABLES_POST_VALIDATION_FILE :
                EGangotriUtil.ARCHIVE_ALL_UPLOADABLE_ITEMS_FILE
        File appendableFile = new File(appendableFilePath)
        //check if it has entries. if yes make sure there is no duplication
        Set<QueuedVO> alreadyIn = ValidateUtil.csvToQueuedVO(appendableFile)
        String appendable = ""
        if(alreadyIn){
            log.info("${alreadyIn.size()} already exist in queue record. ${vos.size()} will be added minus duplicates")
            int counter = 0
            vos.each { vo ->
                if (!alreadyIn.contains(vo)) {
                    appendable += voToCSVString(vo)
                    counter++
                }
            }
            log.info("${counter} added. Were there duplicates ? ${vos.size() == counter ? 'No' : 'Yes'} diff is " + (vos.size() - counter))
        }
        else{
            appendable += vosToCSVString(vos)
            log.info("Added ${vos.size()} ")
        }
        appendableFile.append(appendable)
    }

    /**
     *
     * @param uploadVo
     * @param _identifier The identifier is used only in UsheredVO to capture the unique Archive Item Id
     * @return
     */
    static String voToCSVString(UploadVO uploadVo, String _identifier = null) {
        String archiveProfile = uploadVo.archiveProfile
        String uploadLink = uploadVo.uploadLink
        String fileNameWithPath = uploadVo.path
        String title = UploadUtils.stripFilePath(fileNameWithPath)
        String _idntfier = _identifier ? ",\"$_identifier\"" : ""
        String appendable = "\"$archiveProfile\", \"$uploadLink\", \"$fileNameWithPath\", \"$title\" ${_idntfier}\n"
        return appendable
    }

    static String vosToCSVString(Set<UploadVO> uploadVos) {
        String appendable = ""
        uploadVos.each{ vo ->
            appendable += voToCSVString(vo)
        }
        return appendable
    }

    static void createVOSavingFiles() {
        if (VALIDATE_UPLOAD_AND_REUPLOAD_FAILED_ITEMS) {
            createValidationFiles()
        } else {
            createAllUploadableVOFiles()
            createUsheredFiles()
        }
    }

    static void createAllUploadableVOFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER)
        EGangotriUtil.ARCHIVE_ALL_UPLOADABLE_ITEMS_FILE =
                EGangotriUtil.ARCHIVE_ALL_UPLOADABLE_ITEMS_FILE.replace("{0}", UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ALL_UPLOADABLE_ITEMS_FILE)
    }

    static String createUsheredFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER)
        EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE.replace("{0}", UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE)
    }


    static void createValidationFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER)
        createValidationAllUplodableFiles()
        createValidationUsheredFiles()
    }

    static void createValidationUsheredFiles() {
        EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE.replace("{0}", UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE)
    }

    static void createValidationAllUplodableFiles() {
        EGangotriUtil.ARCHIVE_ITEMS_ALL_UPLOADABLES_POST_VALIDATION_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_ALL_UPLOADABLES_POST_VALIDATION_FILE.replace("{0}", UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ITEMS_ALL_UPLOADABLES_POST_VALIDATION_FILE)
    }

    static void generateFolder(String folderName) {
        File folder = new File(folderName)
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    static void generateFile(String fileName) {
        File file = new File(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    static void printFinalReport(Map<Integer, String> uploadSuccessCheckingMatrix, int attemptedItemsTotal) {
        if (uploadSuccessCheckingMatrix) {
            log.info "Final Report:\n"
            uploadSuccessCheckingMatrix.each { k, v ->
                log.info "$k) $v"
            }
            if (VALIDATE_UPLOAD_AND_REUPLOAD_FAILED_ITEMS) {
                compareQueuedWithUsheredStats(EGangotriUtil.ARCHIVE_ITEMS_ALL_UPLOADABLES_POST_VALIDATION_FILE, EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE)
            } else {
                compareQueuedWithUsheredStats(EGangotriUtil.ARCHIVE_ALL_UPLOADABLE_ITEMS_FILE, EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE)
            }
            long totalTime = EGangotriUtil.PROGRAM_END_TIME_IN_MILLISECONDS - EGangotriUtil.PROGRAM_START_TIME_IN_MILLISECONDS
            log.info("Start Time: " + UploadUtils.getFormattedDateString(new Date(EGangotriUtil.PROGRAM_START_TIME_IN_MILLISECONDS)))
            log.info("End Time: " + UploadUtils.getFormattedDateString(new Date(EGangotriUtil.PROGRAM_END_TIME_IN_MILLISECONDS)))

            log.info("Total Time Taken: ${df.format(totalTime / (60 * 1000))} minutes(s) [ ${df.format(totalTime / (60 * 60 * 1000))} hour(s)]")
            log.info("Total Items attempted: $attemptedItemsTotal")
            log.info("Grand Total of all Items meant for upload: $GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION")
            log.info("Average Upload Time/Item: ${df.format((totalTime / (60 * 1000)) / attemptedItemsTotal)} minute(s)/item")
            log.info("Average Item Uploaded per minute: ${df.format(attemptedItemsTotal / (totalTime / (60 * 1000)))} item/minute")
        }
    }

    static void compareQueuedWithUsheredStats(String queuedFile, String usheredFile) {
        Tuple statsForQueued = ValidateUtil.statsForItemsVO(queuedFile)
        Tuple statsForUshered = ValidateUtil.statsForUsheredItemsVO(usheredFile)
        String equality = (statsForQueued[0] == statsForUshered[0]) ? "Yes" : "\nNo. Short by ${Math.abs(statsForUshered[0] - statsForQueued[0])} item(s)"
        log.info("Are No of Queued Items ( [${statsForQueued[1]}] = ${statsForQueued[0]}) equal to ( [${statsForUshered[1]}] = ${statsForUshered[0]}) Upload-Ushered Items? " +
                equality)

    }

    static boolean logInToArchiveOrg(ChromeDriver driver, def metaDataMap, String archiveProfile, String kuta = "") {
        boolean loginSucess = false
        try {
            driver.get(ArchiveUtil.ARCHIVE_LOGIN_URL)
            log.info("Login to Archive URL $ArchiveUtil.ARCHIVE_LOGIN_URL")
            //Login
            WebElement id = driver.findElement(By.name(UploadUtils.USERNAME_TEXTBOX_NAME))
            WebElement pass = driver.findElement(By.name(UploadUtils.PASSWORD_TEXTBOX_NAME))
            WebElement button = driver.findElement(By.name(UploadUtils.LOGIN_BUTTON_NAME))

            String username = metaDataMap."${archiveProfile}"
            id.sendKeys(username)
            if (!kuta) {
                kuta = metaDataMap."${archiveProfile}.${EGangotriUtil.KUTA}" ?: metaDataMap."${EGangotriUtil.KUTA}"
            }
            pass.sendKeys(kuta)
            button.submit()
            EGangotriUtil.sleepTimeInSeconds(0.2)
            WebDriverWait wait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
            wait.until(ExpectedConditions.elementToBeClickable(By.id(UploadUtils.USER_MENU_ID)))
            loginSucess = true
        }
        catch (Exception e) {
            log.info("Exception in logInToArchiveOrg ${e.message}")
            e.printStackTrace()
        }
        return loginSucess
    }

    static void garbageCollectAndPrintMemUsageInfo() {
        if (EGangotriUtil.GLOBAL_UPLOADING_COUNTER % 100 == 0) {
            double memUse = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
            log.info("Garbage Collecting after every 100th upload.\nMemory being used: ${Math.round(memUse)} mb.")
            System.gc()
            memUse = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
            log.info("Memory use after Garbage Collection: ${Math.round(memUse)} mb")
        }
    }

    static String enhanceIdentifier(String originalIdentifier) {
        //identifier length shouldnt be more that 101 chars
        if (originalIdentifier.length() > 95) {
            originalIdentifier = originalIdentifier.substring(0, 95)
        }
        Random _rndm = new Random()
        String enhancedIdentifier = "${originalIdentifier}_" + _rndm.nextInt(1000) + "_" + EGangotriUtil.ASCII_ALPHA_CHARS[_rndm.nextInt(EGangotriUtil.ASCII_CHARS_SIZE)]
        return enhancedIdentifier
    }

    static int getGrandTotalOfAllUploadables(Collection<String> profiles) {
        return getAllUploadables(profiles).size()
    }

    static BigDecimal getGrandTotalOfFileSizeOfAllUploadables(Collection<String> profiles) {
        Long totalSize = 0
        List<String> allUploadables = getAllUploadables(profiles)
        allUploadables.each { String filePath ->
            totalSize += new File(filePath).size() as long
        }
        return totalSize / (1024 * 1024)
    }

    static List<String> getAllUploadables(Collection<String> profiles) {
        List<String> allUploadables = []
        profiles.eachWithIndex { String profile, index ->
            allUploadables.addAll(FileRetrieverUtil.getUploadablesForProfile(profile))
        }
        return allUploadables
    }

    static Collection<String> filterInvalidProfiles(Collection<String> profiles, Hashtable<String, String> metaDataMap) {
        return profiles.findAll { profile -> UploadUtils.checkIfArchiveProfileHasValidUserName(metaDataMap, profile) } as Set
    }

    static restrictExtensionsToPdfOnlyForAcccountsMarkedIgnoreCreatorSettings(String profile) {
        if (EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS.contains((profile))) {
            SettingsUtil.ALLOWED_EXTENSIONS = []
            SettingsUtil.IGNORE_EXTENSIONS = []
        }
    }
}
