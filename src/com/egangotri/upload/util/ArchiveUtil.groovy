package com.egangotri.upload.util

import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.text.DecimalFormat

@Slf4j
class ArchiveUtil {
    static String ARCHIVE_LOGIN_URL = "https://archive.org/account/login.php"
    static String ARCHIVE_USER_ACCOUNT_URL = "https://archive.org/details/@ACCOUNT_NAME"
    static int GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = 0
    public static boolean ValidateUploadsAndReUploadFailedItems = false
    private static DecimalFormat df = new DecimalFormat("0.00");

    static void getResultsCount(WebDriver driver, Boolean _startTime = true) {
        WebElement avatar = driver.findElementByClassName("avatar")
        String userName = avatar.getAttribute("alt")
        log.info("userName: ${userName}")
        String archiveUserAccountUrl = ARCHIVE_USER_ACCOUNT_URL.replace("ACCOUNT_NAME", userName.toLowerCase())
        if(!_startTime){
            UploadUtils.openNewTab(driver)
            UploadUtils.switchToLastOpenTab(driver)
            driver.navigate().to(archiveUserAccountUrl)
        }
        driver.get(archiveUserAccountUrl)
        WebDriverWait webDriverWait = new WebDriverWait(driver, EGangotriUtil.TEN_TIMES_TIMEOUT_IN_SECONDS)
        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.className("results_count")))
        WebElement resultsCount = driver.findElementByClassName("results_count")
        if (resultsCount) {
            log.info("Results Count at ${ _startTime ? "LoginTime": 'UploadCompletionTime'}: " + resultsCount.text)
            if(!_startTime){
                log.info("**Figure captured will update in a while. So not exctly accurate as upload are still happening")
            }
        }
    }

    static void navigateLoginLogic(WebDriver driver, Map metaDataMap, String archiveProfile) throws Exception{
        boolean loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile)
        if (!loginSuccess) {
            log.info("Login failed once for ${archiveProfile}. will give it one more shot")
            loginSuccess = logInToArchiveOrg(driver, metaDataMap, archiveProfile)
        }
        if (!loginSuccess) {
            log.info("Login failed for Second Time for ${archiveProfile}. will now quit")
            throw new Exception("Not Continuing because of Login Failure twice")
        }
    }

    static void storeArchiveIdentifierInFile(UploadVO uploadVo, String _identifier) {
        String appendable = voToCSVString(uploadVo, _identifier)
        if(ValidateUploadsAndReUploadFailedItems){
            new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE).append(appendable)
        } else{
            new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE).append(appendable)
        }
    }

    static void storeQueuedItemsInFile(List<UploadVO> uploadVos) {
        String appendable = ""
        uploadVos.each{ uploadVo ->
            appendable += voToCSVString(uploadVo)
        }
        if(ValidateUploadsAndReUploadFailedItems){
            new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_POST_VALIDATION_FILE).append(appendable)
        } else{
            new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE).append(appendable)
        }

    }
    static String voToCSVString(UploadVO uploadVo, String _identifier = null) {
        String archiveProfile = uploadVo.archiveProfile
        String uploadLink = uploadVo.uploadLink
        String fileNameWithPath = uploadVo.path
        String title = UploadUtils.stripFilePath(fileNameWithPath)
        String _idntfier = _identifier?"\", $_identifier\"":""
        String appendable = "\"$archiveProfile\", \"$uploadLink\", \"$fileNameWithPath\", \"$title\" ${_idntfier}\n"
        return appendable
    }

    static void createVOSavingFiles() {
        if(ValidateUploadsAndReUploadFailedItems){
            createValidationFiles()
        }
        else{
            createQueuedVOFiles()
            createIdentifierFiles()
        }
    }

    static void createQueuedVOFiles(){
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER)
        EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE.replace("{0}",UploadUtils.getFormattedDateString())

        generateFile(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE)
    }

    static String createIdentifierFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER)
        EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE.replace("{0}",UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE)
    }


    static void createValidationFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER)
        createValidationQueuedFiles()
        createValidationUsheredFiles()
    }

    static void createValidationUsheredFiles(){
        EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE.replace("{0}",UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE)
    }

    static void createValidationQueuedFiles(){
        EGangotriUtil.ARCHIVE_ITEMS_QUEUED_POST_VALIDATION_FILE =
                EGangotriUtil.ARCHIVE_ITEMS_QUEUED_POST_VALIDATION_FILE.replace("{0}",UploadUtils.getFormattedDateString())
        generateFile(EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE)
    }


    static void generateFolder(String folderName) {
        File folder = new File(folderName)
        if(!folder.exists()){
            folder.mkdir()
        }
    }

    static void generateFile(String fileName) {
        File file = new File(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    static void printFinalReport(Map<Integer, String> uploadSuccessCheckingMatrix, int attemptedItemsTotal){
        if (uploadSuccessCheckingMatrix) {
            log.info "Final Report:\n"
            uploadSuccessCheckingMatrix.each { k, v ->
                log.info "$k) $v"
            }
            if(ValidateUploadsAndReUploadFailedItems) {
                compareQueuedWithUsheredStats(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_POST_VALIDATION_FILE, EGangotriUtil.ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE)
            }
            else {
                compareQueuedWithUsheredStats(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE, EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOR_UPLOAD_FILE)
             }
            int totalTime = EGangotriUtil.PROGRAM_END_TIME_IN_SECONDS-EGangotriUtil.PROGRAM_START_TIME_IN_SECONDS
            log.info("Total Time Taken: ${df.format(totalTime/60)} minutes(s) [ ${df.format(totalTime/(60*60))} hour(s)]")
            log.info("Total Items attempted: $attemptedItemsTotal")
            log.info("Grand Total of all Items meant for upload: $GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION")
            log.info("Average Upload Time: ${df.format((totalTime/60)/attemptedItemsTotal)} minute(s)/item")
        }
    }

    static void compareQueuedWithUsheredStats(String queuedFile, String usheredFile){
        Tuple statsForQueued = ValidateUtil.statsForItemsVO(queuedFile)
        log.info("\n")
        Tuple statsForUshered = ValidateUtil.statsForUsheredItemsVO(usheredFile)
        String equality = (statsForQueued[0] == statsForUshered[0]) ? "Yes" : "\nNo. Short by ${Math.abs(statsForUshered[0] - statsForQueued[0])} item(s)"
        log.info("Are No of Queued Items ( [${statsForQueued[1]}] = ${statsForQueued[0]}) equal to ( [${statsForUshered[1]}] = ${statsForUshered[0]}) Upload-Ushered Items? " +
                equality)

    }
    static boolean logInToArchiveOrg(ChromeDriver driver, def metaDataMap, String archiveProfile) {
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
            String kuta = metaDataMap."${archiveProfile}.${EGangotriUtil.KUTA}" ?: metaDataMap."${EGangotriUtil.KUTA}"
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

    static void garbageCollectAndPrintUsageInfo(){
        if( EGangotriUtil.GLOBAL_UPLOADING_COUNTER % 100 == 0){
            double memUse = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024)
            log.info("Memory being used: ${memUse} mb. Garbage Collecting after every 100th upload.")
            System.gc()
            memUse = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024)
            log.info("Memory use after Garbage Collection: ${memUse} mb")
        }
    }

    static String enhanceIdentifier(String originalIdentifier){
        //identifier length shouldnt be more that 101 chars
        if(originalIdentifier.length() > 95 ){
            originalIdentifier = originalIdentifier.substring(0,95)
        }
        Random _rndm = new Random()
        String enhancedIdentifier = "${originalIdentifier}_" + _rndm.nextInt(1000) + "_" + EGangotriUtil.ASCII_ALPHA_CHARS[_rndm.nextInt(EGangotriUtil.ASCII_CHARS_SIZE)]
        return enhancedIdentifier
    }

    static int getGrandTotalOfAllUploadables(Collection<String> profiles){
        return getAllUploadables(profiles).size()
    }

    static List<String> getAllUploadables(Collection<String> profiles){
        List<String> allUploadables = []
        profiles.eachWithIndex { String profile, index ->
            allUploadables.addAll(UploadUtils.getUploadablesForProfile(profile))
        }
        return allUploadables
    }
    static Collection<String> purgeBrokenProfiles(Collection<String> profiles,  Hashtable<String, String> metaDataMap){
        return profiles.findAll { profile -> UploadUtils.checkIfArchiveProfileHasValidUserName(metaDataMap, profile)} as Set
    }
}
