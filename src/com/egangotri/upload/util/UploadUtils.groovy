package com.egangotri.upload.util


import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.text.SimpleDateFormat

@Slf4j
class UploadUtils {

    static final String USERNAME_TEXTBOX_NAME = "username"
    static final String PASSWORD_TEXTBOX_NAME = "password"
    static final String LOGIN_BUTTON_NAME = "submit-to-login"
    static final String USER_MENU_ID = "user-menu" // only created when User is Signed In
    static final String CHOOSE_FILES_TO_UPLOAD_BUTTON = "file_button_initial"
    static final String UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON = "upload_button"
    static final String PAGE_URL_ITEM_ID = "item_id"
    static final String PAGE_URL = "page_url"
    static final String PAGE_URL_INPUT_FIELD = "input_field"
    static final String LICENSE_PICKER_DIV = "license_picker_row"
    static final String LICENSE_PICKER_RADIO_OPTION = "license_radio_CC0"
    static final int DEFAULT_SLEEP_TIME = 1000
    static final String DATE_TIME_PATTERN = "d-MMM-yyyy_h-mm-a"

    static Map<String, String> SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP = [:]
    static Map<String, List<String>> RANDOM_CREATOR_BY_PROFILE_MAP = [:]
    static final String ARCHIVE_UPLOAD_URL = "https://archive.org/upload?"
    static final String AMPERSAND = "&"

    static int RANDOM_CREATOR_MAX_LIMIT = 50

    static readTextFileAndDumpToList(String fileName) {
        List list = []
        File file = new File(fileName)
        def line = ""
        file.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                list << line
            }
        }
        return list
    }

    static Hashtable<String, String> loadProperties(String fileName) {
        Properties properties = new Properties()
        File propertiesFile = new File(fileName)
        Hashtable<String, String> metaDataMap = [:]

        if (propertiesFile.exists()) {
            propertiesFile.withInputStream {
                properties.load(it)
            }


            properties.entrySet().each { entry ->
                String key = entry.key
                String val = new String(entry.value.getBytes("ISO-8859-1"), "UTF-8")
                if (key.endsWith(".description")) {
                    val = encodeString(val)
                }
                metaDataMap.put(key, val)
            }

            metaDataMap.each {
                k, v ->
                    //log.info "$k $v"
            }
        }
        return metaDataMap
    }

    def static encodeString(def stringToEncode) {

        def reservedCharacters = [32: 1, 33: 1, 42: 1, 34: 1, 39: 1, 40: 1, 41: 1, 59: 1, 58: 1, 64: 1, 38: 1, /*61:1,*/ 43: 1, 36: 1, 33: 1, 47: 1, 63: 1, 37: 1, 91: 1, 93: 1, 35: 1]

        def encoded = stringToEncode.collect { letter ->
            reservedCharacters[(int) letter] ? "%" + Integer.toHexString((int) letter).toString().toUpperCase() : letter
        }
        return encoded.join("")
    }

    static boolean hasAtleastOneUploadablePdfForProfile(String archiveProfile) {
        List<File> folders = pickFolderBasedOnArchiveProfile(archiveProfile).collect { new File(it) }
        boolean atlestOne = false
        log.info "folders: $folders"
        if (EGangotriUtil.isAPreCutOffProfile(archiveProfile) && hasAtleastOnePdfInPreCutOffFolders(folders)) {
            atlestOne = true
        } else if (hasAtleastOnePdfExcludePreCutOff(folders)) {
            atlestOne = true
        }
        log.info "atlestOne[$archiveProfile]: $atlestOne"
        return atlestOne
    }

    static List<String> getUploadablesForProfile(String archiveProfile) {
        List<File> folders = pickFolderBasedOnArchiveProfile(archiveProfile).collect { String fileName -> fileName ? new File(fileName) : null }
        List<String> items = []
        if (EGangotriUtil.isAPreCutOffProfile(archiveProfile)) {
            items = getItemsInPreCutOffFolders(folders)
        } else {
            items = getAllItemsExceptPreCutOff(folders)
        }
        return items
    }

    static int getCountOfUploadableItemsForProfile(String archiveProfile) {
        return getUploadablesForProfile(archiveProfile)?.size()
    }


    static boolean hasAtleastOnePdf(File folder) {
        return hasAtleastOnePdf(folder, false)
    }

    static boolean hasAtleastOnePdf(File folder, boolean excludePreCutOff) {
        return getAllPdfs(folder, excludePreCutOff)?.size()
    }

    static boolean hasAtleastOnePdfExcludePreCutOff(File folder) {
        return hasAtleastOnePdf(folder, true)
    }

    static boolean hasAtleastOnePdfExcludePreCutOff(List<File> folders) {
        boolean atlestOne = false
        folders.each { folder ->
            if (hasAtleastOnePdfExcludePreCutOff(folder)) {
                atlestOne = true
            }
        }
        return atlestOne
    }

    static boolean hasAtleastOnePdfInPreCutOffFolders(List<File> folders) {
        boolean atlestOne = false
        if (getItemsInPreCutOffFolders(folders)) {
            atlestOne = true
        }
        return atlestOne
    }

    static List<String> getAllItemsExceptPreCutOff(File folder) {
        getAllPdfs(folder, true)
    }

    static List<String> getAllItemsExceptPreCutOff(List<File> folders) {
        List<String> pdfs = []
        folders.each { folder ->
            pdfs.addAll(getAllItemsExceptPreCutOff(folder))
        }
        return pdfs
    }

    static List<String> getAllPdfs(File folder, Boolean excludePreCutOff) {
        List<String> pdfs = []
        Map optionsMap = [type      : FileType.FILES,
                          nameFilter: ~(FileUtil.PDF_REGEX)
        ]
        if (excludePreCutOff) {
            optionsMap.put("excludeFilter", { it.absolutePath.toLowerCase().contains(FileUtil.PRE_CUTOFF) || it.absolutePath.toLowerCase().contains(FileUtil.UPLOAD_KEY_WORD) })
        }
        if (!folder.exists()) {
            log.error("$folder doesnt exist. returning")
            return []
        }
        folder.traverse(optionsMap) {
            pdfs << it.absolutePath
        }
        return pdfs.sort()
    }

    static List<String> getAllPdfs(File folder) {
        return getAllPdfs(folder, false)
    }

    static List<String> getPdfsInPreCutOffFolder(File folder) {
        List<String> pdfs = []
        Map optionsMap = [type  : FileType.FILES,
                          filter: {
                              it.absolutePath.contains(FileUtil.PRE_CUTOFF) /*&&
                                      it.name.endsWith(EGangotriUtil.PDF)*/
                          }
        ]
        if (!folder.exists()) {
            log.error("$folder doesnt exist. returning")
            return []
        }
        folder.traverse(optionsMap) {
            log.info ">>>" + it
            log.info "${it.absolutePath.contains(FileUtil.PRE_CUTOFF)}"
            pdfs << it.absolutePath
        }
        return pdfs
    }

    static List<String> getItemsInPreCutOffFolders(List<File> folders) {
        List<String> pdfs = []
        folders.each { folder ->
            pdfs.addAll(getPdfsInPreCutOffFolder(folder))
        }
        return pdfs
    }

    static boolean checkIfArchiveProfileHasValidUserName(Map metaDataMap, String archiveProfile, boolean logErrMsg = true) {
        boolean success = false
        String username = metaDataMap."${archiveProfile}.username"
        String userNameInvalidMsg = "Invalid/Non-Existent"
        String errMsg2 = " UserName [$username] in ${stripFilePath(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)} file for $archiveProfile"
        if (username?.trim()) {
            success = username ==~ /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}/
            if (!success) {
                userNameInvalidMsg = "Invalid Email Format of"
            }
        }
        if (!success && logErrMsg) {
            log.info("${userNameInvalidMsg}${errMsg2}")
        }
        return success
    }


    static void hitEscapeKey() {
        Robot robot = new Robot()
        robot.keyPress(KeyEvent.VK_ESCAPE)
        robot.keyRelease(KeyEvent.VK_ESCAPE)
    }

    static void hitEnterKey() {
        Robot robot = new Robot()
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
    }

    static void clickChooseFilesToUploadButtonAndPasteFilePath(WebDriver driver, String fileNameWithPath) {
        //Note this id is already tested to be clickable
        WebElement fileButtonInitial = driver.findElement(By.id(CHOOSE_FILES_TO_UPLOAD_BUTTON))
        fileButtonInitial.click()
        log.info("$CHOOSE_FILES_TO_UPLOAD_BUTTON clicked")
        pasteFileNameAndCloseUploadPopup(fileNameWithPath)
    }

    static void pasteFileNameAndCloseUploadPopup(String fileNameWithPath) {
        // A short pause is a must and must be atleast a second
        EGangotriUtil.sleepTimeInSeconds(1, true)
        setClipboardData(fileNameWithPath)
        //native key strokes for CTRL, V and ENTER keys
        Robot robot = new Robot()
        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        Thread.sleep(10)
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
        EGangotriUtil.sleepTimeInSeconds(0.01)
    }

    static void setClipboardData(String string) {
        StringSelection stringSelection = new StringSelection(string)
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null)
    }

    static List getAStashOfFilesForUpload(String src) {
        List<String> filterables = getFilterables()
        List<String> allPdfs = getAllPdfs(new File(src))
        if (filterables) {
            log.info("filterables.size():${filterables.size()} '${filterables[0]}'")
            log.info("allPdfs2.size() before:${allPdfs.size()} '${allPdfs.first()}'")
            int count = 0

            def x = ['nnn', 'E:\\bngl2\\85685712 Jennings Vedantic Buddhism Of The Buddha.pdf']
            def y = "85685712 Jennings Vedantic Buddhism Of The Buddha.pdf"
            log.info(x.findIndexOf { it =~ /($y)$/ })
            filterables.each { String filterable ->
                int count2 = 0
                try {
                    int idx = allPdfs.findIndexOf { pdf ->
                        String y2 = pdf.substring(src.length() + 1)

                        if (count2++ < 10) {
                            log.info("$filterable == ${y2} ($pdf) " + filterable.equals(y2))
                        }
                        filterable.equals(y2)
                    }

                    if (idx >= 0) {
                        log.info("${count++}).idx:$idx, $filterable == ${allPdfs.get(idx)}")
                        allPdfs.remove(idx)
                    }
                }

                catch (Exception e) {
                    log.error(e.message)
                }

            }
            log.info("\nallPdfs2.size() after:${allPdfs.size()} '${allPdfs.first()}'")
        }
        if (allPdfs?.size() > 100) {
            return allPdfs[0..100]
        } else {
            return allPdfs[0..allPdfs?.size()]
        }

    }

    static List<String> getFilterables() {
        String fileName = System.getProperty("user.home") + "${File.separator}eGangotri${File.separator}archiveFileName.txt"
        List<String> list = new File(fileName).readLines()
        List<String> filterables = []
        if (list) {
            filterables = list[0].split(",").collect { "${it}.pdf" }
        }
        return filterables
    }


    static def partition(List<String> partitionableList, int size) {
        def partitions = []
        int partitionCount = partitionableList.size() / size

        partitionCount.times { partitionNumber ->
            def start = partitionNumber * size
            def end = start + size - 1
            partitions << partitionableList[start..end]
        }

        if (partitionableList.size() % size) partitions << partitionableList[partitionCount * size..-1]
        return partitions
    }

    static void throwNoCreatorSpecifiedErrorIfNoRandomCreatorFlagAndQuit() {
        if (!EGangotriUtil.GENERATE_RANDOM_CREATOR) {
            throw new Exception("No Creator. Pls provide Creator in archiveMetadata.properties file")
        }
    }

    static String generateCreatorsForProfileAndPickARandomOne(String archiveProfile) {
        throwNoCreatorSpecifiedErrorIfNoRandomCreatorFlagAndQuit()
        if (!RANDOM_CREATOR_BY_PROFILE_MAP || !RANDOM_CREATOR_BY_PROFILE_MAP.containsKey(archiveProfile)) {
            RANDOM_CREATOR_BY_PROFILE_MAP.put(archiveProfile, null)
        }
        if (!RANDOM_CREATOR_BY_PROFILE_MAP["${archiveProfile}"]) {
            RANDOM_CREATOR_BY_PROFILE_MAP["${archiveProfile}"] = randomCreators()
        }
        List randomCreators = RANDOM_CREATOR_BY_PROFILE_MAP["${archiveProfile}"]
        String randomPick = randomCreators[new Random().nextInt(randomCreators.size)]
        return "creator=${randomPick}"
    }

    static List randomCreators() {
        List firstNames = UploadUtils.readTextFileAndDumpToList(EGangotriUtil.FIRST_NAME_FILE)
        List lastNames = UploadUtils.readTextFileAndDumpToList(EGangotriUtil.LAST_NAME_FILE)
        Random rnd = new Random()
        List creators = []
        int MAX_CREATORS = UploadUtils.RANDOM_CREATOR_MAX_LIMIT
        int max = firstNames.size() > lastNames.size() ? (firstNames.size() > MAX_CREATORS ? MAX_CREATORS : firstNames.size()) : (lastNames.size() > MAX_CREATORS ? MAX_CREATORS : lastNames.size())
        (1..max).each {
            int idx1 = rnd.nextInt(firstNames.size)
            int idx2 = rnd.nextInt(lastNames.size)
            creators << "${firstNames[idx1].trim().capitalize()} ${lastNames[idx2].trim().capitalize()}"
        }
        return creators
    }

    static String getOrGenerateSupplementaryURL(String archiveProfile) {
        if (!SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP || !SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP.containsKey(archiveProfile)) {
            SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP.put(archiveProfile, null)
        }
        if (!SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP["${archiveProfile}"]) {
            def metaDataMap = loadProperties(EGangotriUtil.ARCHIVE_METADATA_PROPERTIES_FILE)
            String _creator = metaDataMap."${archiveProfile}.creator"
            if (!_creator) {
                throwNoCreatorSpecifiedErrorIfNoRandomCreatorFlagAndQuit()
            }

            String _subjects = metaDataMap."${archiveProfile}.subjects"
            if (!_subjects) {
                _subjects = !EGangotriUtil.GENERATE_RANDOM_CREATOR ? "subject=" + _creator.replaceAll("creator=", "") : null
            }

            String _lang = metaDataMap."${archiveProfile}.language" ?: "language=eng"
            String _fileNameAsDesc = "{0}"
            String _desc = metaDataMap."${archiveProfile}.description"
            if (_desc && _desc?.contains("description=")) {
                _desc = _desc.replaceAll("description=", "")
            }
            String desc_and_file_name = "description=${_desc ? "${_desc}, ${_fileNameAsDesc}" : _fileNameAsDesc}"
            String supplementary_url = desc_and_file_name + AMPERSAND + _lang
            if (metaDataMap."${archiveProfile}.collection") {
                supplementary_url += AMPERSAND + metaDataMap."${archiveProfile}.collection"
            }
            if (_subjects) {
                supplementary_url += AMPERSAND + _subjects
            }
            if (!EGangotriUtil.GENERATE_RANDOM_CREATOR) {
                supplementary_url += AMPERSAND + _creator
            }
            SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP["${archiveProfile}"] = supplementary_url
        }
        String url = SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP["${archiveProfile}"]
        if ((EGangotriUtil.GENERATE_RANDOM_CREATOR)) {
            String _creator = generateCreatorsForProfileAndPickARandomOne(archiveProfile)
            url += AMPERSAND + _creator

            if (!url.contains("subject=")) {
                String _subjects = "subject=" + _creator.replaceAll("creator=", "")
                url += AMPERSAND + _subjects
            }
        }
        return url
    }


    static String generateUploadUrl(String archiveProfile, String fileNameToBeUsedAsUniqueDescription = "") {
        String supplementary_url = getOrGenerateSupplementaryURL(archiveProfile)
        String insertDescription = insertDescriptionInUploadUrl(supplementary_url, fileNameToBeUsedAsUniqueDescription)
        return ARCHIVE_UPLOAD_URL + insertDescription
    }

    static insertDescriptionInUploadUrl(String supplementary_url, String fileNameToBeUsedAsUniqueDescription){
        return supplementary_url.replace('{0}', "'${_removeAmpersandAndFetchTitleOnly(fileNameToBeUsedAsUniqueDescription)}'")
    }

    static String _removeAmpersandAndFetchTitleOnly(String title) {
        return stripFilePathAndFileEnding(title.replaceAll(AMPERSAND, ""))
    }

    /***
     *
     * @param title Ex: C:\books\set-1\Hamlet by Shakespeare.pdf
     * @return Hamlet by Shakespeare
     */
    static String stripFilePathAndFileEnding(String title) {
        return removeFileEnding(stripFilePath(title))
    }

    /***
     *
     * @param title Ex: C:\books\set-1\Hamlet by Shakespeare.pdf
     * @return Hamlet by Shakespeare.pdf
     */
    static String stripFilePath(String title) {
        return title.trim().drop(title.lastIndexOf(File.separator) + 1)
    }

    /***
     *
     * @param title Ex: Hamlet by Shakespeare.pdf
     * @return Hamlet by Shakespeare
     */
    static String removeFileEnding(String title) {
        return title.contains(".") ? title.trim().tokenize(".").dropRight(1).join(".") : title
    }

    static String getLastPortionOfTitleUsingSeparator(String title, String separator = "-") {
        return title.contains(separator) ? title.split("-").last() : title
    }

    static List<String> pickFolderBasedOnArchiveProfile(String archiveProfile) {
        List folderName = []
        if (EGangotriUtil.isAPreCutOffProfile(archiveProfile)) {
            folderName = FileUtil.ALL_FOLDERS.values().toList()
        } else {
            folderName = [FileUtil.ALL_FOLDERS."${archiveProfile.toUpperCase()}"]
        }
        log.info "pickFolderBasedOnArchiveProfile($archiveProfile): $folderName"
        return folderName
    }

    static boolean switchToLastOpenTab(ChromeDriver driver) {
        try {
            ArrayList<String> chromeTabsList = new ArrayList<String>(driver.getWindowHandles())
            //there is a bug in retrieving the size of chromeTabsList in Selenium.
            //use of last() instead of chromeTabsList.get(tabIndex+1) saves the issue
            log.info "chromeTabsList.size(): ${chromeTabsList.size()}"
            driver.switchTo().window(chromeTabsList.last())
        }
        catch (Exception e) {
            log.info("Exception while switching to new Tab ${e.message}")
            return false
        }
        return true
    }

    static void openNewTab(ChromeDriver driver, float sleepTimeInSeconds = 0.1) {
        try {
            if (sleepTimeInSeconds > 0) {
                EGangotriUtil.sleepTimeInSeconds(sleepTimeInSeconds)
            }
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.open('','_blank');");
        }
        catch (Exception _ex) {
            log.error("openNewTab Exception: ${_ex.message}")
        }
    }

    static void minimizeBrowser(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.blur()");
    }

    static void closeBrowser(WebDriver driver) {
        driver.quit()
    }

    static void tabPasteFolderNameAndCloseUploadPopup(String fileName) {
        log.info "$fileName  being pasted"
        // A short pause, just to be sure that OK is selected
        EGangotriUtil.sleepTimeInSeconds(1)
        setClipboardData(fileName)
        //native key strokes for CTRL, V and ENTER keys
        Robot robot = new Robot()
        robot.keyPress(KeyEvent.VK_TAB)
        robot.keyRelease(KeyEvent.VK_TAB)

        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_V)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
    }

    static boolean checkAlert(WebDriver driver, Boolean accept = true) {
        boolean alertWasDetected = false
        try {
            WebDriverWait webDriverWait = new WebDriverWait(driver, 1)
            webDriverWait.until(ExpectedConditions.alertIsPresent())
            Alert alert = driver.switchTo().alert()
            log.info("Found Alert Text: ->${alert.getText()}<-")
            if (accept) {
                alert.accept()
            } else {
                alert.dismiss()
            }
            alertWasDetected = true
        } catch (Exception e) {
            //log.info("No alert detected")
        }
        return alertWasDetected
    }

    static getFormattedDateString() {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(new Date())
    }

    static String generateStats(List<List<Integer>> uploadStats, String archiveProfile, Integer countOfUploadablePdfs){
        int uplddSum = uploadStats.collect { elem -> elem.first() }.sum()
        String statsAsPlusSeparatedValues = uploadStats.collect { elem -> elem.first() }.join(" + ")
        String countOfUploadedItems = uploadStats.size() > 1 ? "($statsAsPlusSeparatedValues) = $uplddSum" : uploadStats.first().first()

        int excSum = uploadStats.collect { elem -> elem.last() }.sum()
        String excpsAsPlusSeparatedValues = uploadStats.collect { elem -> elem.last() }.join(" + ")
        String exceptionCount = uploadStats.size() > 1 ? "($excpsAsPlusSeparatedValues) = $excSum" : uploadStats.first().last()
        log.info("Uploaded $countOfUploadedItems items with (${exceptionCount}) Exceptions for Profile: $archiveProfile")

        String statusMsg = countOfUploadablePdfs == uplddSum ? 'Success. All items were put for upload.' : "${(uplddSum == 0) ? 'All' : 'Some'} Failed!"
        String report = "$archiveProfile, \t Total $countOfUploadablePdfs,\t " +
                "Attempted Upload Count $countOfUploadedItems,\t with  ${exceptionCount} Exceptions \t $statusMsg"
        println(report)
        return report
    }

}