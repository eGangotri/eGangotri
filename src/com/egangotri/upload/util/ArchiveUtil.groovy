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

@Slf4j
class ArchiveUtil {
    static String ARCHIVE_LOGIN_URL = "https://archive.org/account/login.php"
    static String ARCHIVE_USER_ACCOUNT_URL = "https://archive.org/details/@ACCOUNT_NAME"
    static boolean ValidateLinksAndReUploadBrokenRunning = false

    static void getResultsCount(WebDriver driver, Boolean _startTime = true) {
        WebElement avatar = driver.findElementByClassName("avatar")
        String userName = avatar.getAttribute("alt")
        log.info("userName: ${userName}")
        String archiveUserAccountUrl = ARCHIVE_USER_ACCOUNT_URL.replace("ACCOUNT_NAME", userName.toLowerCase())
        if(!_startTime){
            UploadUtils.openNewTab()
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
        if(ValidateLinksAndReUploadBrokenRunning){
            new File(EGangotriUtil.ARCHIVE_VALIDATION_FILE).append(appendable)
        } else{
            new File(EGangotriUtil.ARCHIVE_IDENTIFIER_FILE).append(appendable)
        }
    }

    static void storeQueuedItemsInFile(List<UploadVO> uploadVos) {
        String appendable = ""
        uploadVos.each{ uploadVo ->
            appendable += voToCSVString(uploadVo)
        }
        new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE).append(appendable)

    }
    static String voToCSVString(UploadVO uploadVo, String _identifier = null) {
        String archiveProfile = uploadVo.archiveProfile
        String uploadLink = uploadVo.uploadLink
        String fileNameWithPath = uploadVo.path
        String title = UploadUtils.getFileTitleOnly(fileNameWithPath)
        String _idntfier = _identifier?"\"$_identifier\"":""
        String appendable = "\"$archiveProfile\", \"$uploadLink\", \"$fileNameWithPath\", \"$title\", ${_idntfier}\n"
        return appendable
    }

    static void createVOSavingFiles() {
        if(ValidateLinksAndReUploadBrokenRunning){
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

    static void createValidationFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER)
        EGangotriUtil.ARCHIVE_VALIDATION_FILE =
                EGangotriUtil.ARCHIVE_VALIDATION_FILE.replace("{0}",UploadUtils.getFormattedDateString())

        generateFile(EGangotriUtil.ARCHIVE_VALIDATION_FILE)
    }

    static String createIdentifierFiles() {
        generateFolder(EGangotriUtil.ARCHIVE_GENERATED_IDENTIFIERS_FOLDER)
        EGangotriUtil.ARCHIVE_IDENTIFIER_FILE =
                EGangotriUtil.ARCHIVE_IDENTIFIER_FILE.replace("{0}",UploadUtils.getFormattedDateString())

        generateFile(EGangotriUtil.ARCHIVE_IDENTIFIER_FILE)
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

    static void printUploadReport(Map<Integer, String> uploadSuccessCheckingMatrix){
        if (uploadSuccessCheckingMatrix) {
            log.info "Upload Report:\n"
            uploadSuccessCheckingMatrix.each { k, v ->
                log.info "$k) $v"
            }
            log.info "\n ***All Items put for upload implies all were attempted successfully for upload. But there can be errors still after attempted upload. best to check manually."
            if(!ValidateLinksAndReUploadBrokenRunning){
                Tuple statsForItemsVO = ValidateLinksUtil.statsForItemsVO(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FILE)
                log.info("\n")
                Tuple statsForLinksVO = ValidateLinksUtil.statsForLinksVO(EGangotriUtil.ARCHIVE_IDENTIFIER_FILE)
                log.info("Are No of Queued Items ( ${statsForItemsVO[1]} = ${statsForItemsVO[0]}) equal to ( ${statsForLinksVO[1]} == ${statsForLinksVO[0]}) Identifier Generated Items? " +
                        "${statsForItemsVO[0] == statsForLinksVO[0]  ? 'Yes': 'No. Short by ${statsForLinksVO[0] - statsForItemsVO[0]}'}")
            }
        }
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
}
