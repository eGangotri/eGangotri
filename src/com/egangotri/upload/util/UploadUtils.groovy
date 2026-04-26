package com.egangotri.upload.util

import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.awt.Robot
import java.awt.Toolkit

import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

import java.text.SimpleDateFormat
import java.time.Duration

@Slf4j
class UploadUtils {

    static final String USERNAME_TEXTBOX_NAME = 'username'
    static final String PASSWORD_TEXTBOX_NAME = 'password'
    static final String LOGIN_BUTTON_NAME = 'submit-to-login'
    static final String CHOOSE_FILES_TO_UPLOAD_BUTTON_AS_CLASS = 'js-uploader-file-input-initial'

    static final String UPLOAD_AND_CREATE_YOUR_ITEM_BUTTON = 'upload_button'
    static final String PAGE_URL_ITEM_ID = 'item_id'
    static final String PAGE_URL = 'page_url'
    static final String PAGE_URL_INPUT_FIELD = 'input_field'
    static final String LICENSE_PICKER_DIV = 'license_picker_row'
    static final String LICENSE_PICKER_RADIO_OPTION = 'license_radio_CC0'
    static final int DEFAULT_SLEEP_TIME = 1000
    static final String DATE_TIME_PATTERN = 'd-MMM-yyyy_h-mm-a'

    static Map<String, String> SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP = [:]
    static Map<String, List<String>> RANDOM_CREATOR_BY_PROFILE_MAP = [:]
    static final String ARCHIVE_UPLOAD_URL = 'https://archive.org/upload/?'
    static final String AMPERSAND = '&'

    static int RANDOM_CREATOR_MAX_LIMIT = 50

    static String DEFAULT_SUBJECT_DESC = ''

    static Map<String, String> updateMapByAppendingAdditionalSubjectDescription(Map<String, String> map) {
        map.each { key, value ->
            {
                if (key.endsWith('.subjects') && DEFAULT_SUBJECT_DESC != '') {
                    map[key] = value + "${value}, ${DEFAULT_SUBJECT_DESC}"
                }
                if (key.endsWith('.description')  && DEFAULT_SUBJECT_DESC != '') {
                    map[key] = value + "${value}, ${DEFAULT_SUBJECT_DESC}"
                }
            }
        }
        log.info("Updated map with subject description: ${map}")
        return map
    }

    static Map<String, String> getArchiveMetadataKeyValues() {
        Map<String, String> metadataMap = readPropsFromListOfPropFiles(EGangotriUtil.ARCHIVE_METADATA_PROPERTIES_FILES)

        // Handle default subject description if configured
        if (DEFAULT_SUBJECT_DESC?.size() > 0) {
            try {
                return updateMapByAppendingAdditionalSubjectDescription(metadataMap)
            } catch (Exception e) {
                log.error("Failed to update map with subject description: ${e.message}")
                return metadataMap
            }
        }

        return metadataMap
    }

    /**
     * Splits each CSV value in loadedProps into its first two comma-separated tokens,
     * populating loadedProps2 (first token) and loadedProps3 (second token).
     */
    static void splitPropsOnFirstTwoCSVValues(Map<String, String> loadedProps,
                                                      Map<String, String> loadedProps2,
                                                      Map<String, String> loadedProps3) {
        loadedProps.each { String key, String value ->
            if (value.contains(',')) {
                String[] valueArray = value.split(',')
                if (valueArray[0]?.trim()?.size() > 0) {
                    loadedProps2.put(key, valueArray[0]?.trim())
                }
                if (valueArray.length > 1 && valueArray[1]?.trim()?.length() > 0) {
                    loadedProps3.put(key, valueArray[1]?.trim())
                }
            }
            else {
                loadedProps2.put(key, value)
                loadedProps3.put(key, '')
            }
        }
                                                      }

    static List<Map<String, String>> readPropsFromListOfPropFilesForCSVValues(List propFiles) {
        Map<String, String> metadataMap = [:]
        Map<String, String> metadataMap2 = [:]
        if (!propFiles) {
            log.warn('No archive metadata properties files configured')
            return [[:], [:]]
        }
        propFiles.each { String fileName ->
            try {
                File propsFile = new File(fileName)
                if (propsFile.exists()) {
                    log.info("Loading archive metadata from: ${fileName}")
                    Map<String, String> loadedProps = loadProperties(fileName)
                    if (loadedProps) {
                        Map<String, String> loadedProps2 = [:]
                        Map<String, String> loadedProps3 = [:]
                        splitPropsOnFirstTwoCSVValues(loadedProps, loadedProps2, loadedProps3)
                        metadataMap.putAll(loadedProps2)
                        metadataMap2.putAll(loadedProps3)
                    } else {
                        log.warn("No properties loaded from file: ${fileName}")
                    }
                } else {
                    log.warn("Archive metadata properties file not found: ${fileName}")
                }
            } catch (Exception e) {
                log.error("Failed to load properties from ${fileName}: ${e.message}")
            }
        }
        return [metadataMap, metadataMap2]
    }
    static  Map<String, String>  readPropsFromListOfPropFiles(List propFiles) {
        Map<String, String> metadataMap = [:]
        // Null check for the properties files array
        if (!propFiles) {
            log.warn('No archive metadata properties files configured')
            return  [:]
        }
        propFiles.each { String fileName ->
            try {
                File propsFile = new File(fileName)
                if (propsFile.exists()) {
                    log.info("Loading archive metadata from: ${fileName}")
                    Map<String, String> loadedProps = loadProperties(fileName)

                    // Null check for loaded properties
                    if (loadedProps) {
                        metadataMap.putAll(loadedProps)
                    } else {
                        log.warn("No properties loaded from file: ${fileName}")
                    }
                } else {
                    log.warn("Archive metadata properties file not found: ${fileName}")
                }
            } catch (Exception e) {
                log.error("Failed to load properties from ${fileName}: ${e.message}")
            }
        }
        return metadataMap
    }

    static List<Map<String, String>> ALL_ARCHIVE_LOGIN_AND_PROFILES_LIST = new ArrayList()

    static  List<Map<String, String>>  getAllArchiveLoginsAndProfiles() {
        if (ALL_ARCHIVE_LOGIN_AND_PROFILES_LIST.isEmpty()) {
            ALL_ARCHIVE_LOGIN_AND_PROFILES_LIST = readPropsFromListOfPropFilesForCSVValues(EGangotriUtil.ARCHIVE_LOGINS_PROPERTIES_FILES)
        }
        return ALL_ARCHIVE_LOGIN_AND_PROFILES_LIST
    }

    static Map<String, String> getAllArchiveLogins() {
        List<Map<String, String>> loginProfiles = getAllArchiveLoginsAndProfiles()
        log.info('archive logins.' + loginProfiles.get(0))
        return loginProfiles.size() > 0 ? loginProfiles[0] : [:]
    }

    static Map<String, String> getAllArchiveLoginProfileNames() {
        List<Map<String, String>> loginProfiles = getAllArchiveLoginsAndProfiles()
        return loginProfiles.size() > 1 ? loginProfiles[1] : [:]
    }

    static String getArchiveLoginProfileName(String archiveProfileKey) {
        String value = getAllArchiveLoginProfileNames()?.get(archiveProfileKey).trim()
        return ((!value || value == '*') ? '' : value)
    }
    static readTextFileAndDumpToList(String fileName) {
        List<String> list = []
        File file = new File(fileName)
        def line = ''
        file.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                list << line
            }
        }
        return list
    }

    static Map<String, String> loadProperties(String fileName) {
        Properties properties = new Properties()
        File propertiesFile = new File(fileName)
        Map<String, String> metaDataMap = [:]

        if (propertiesFile.exists()) {
            propertiesFile.withInputStream {
                properties.load(it)
            }

            properties.entrySet().each { entry ->
                String key = entry.key
                String val = new String(entry.value.toString().getBytes('ISO-8859-1'), 'UTF-8')
                if (key.endsWith('.description')) {
                    val = encodeString(val)
                }
                metaDataMap.put(key.trim(), val.trim())
            }

            metaDataMap.each {
                String k, String v ->
            //log.info "$k $v"
            }
        }
        return metaDataMap
    }

    def static encodeString(def stringToEncode) {
        def reservedCharacters = [32: 1, 33: 1, 42: 1, 34: 1, 39: 1, 40: 1, 41: 1, 59: 1, 58: 1, 64: 1, 38: 1, /*61:1,*/ 43: 1, 36: 1, 33: 1, 47: 1, 63: 1, 37: 1, 91: 1, 93: 1, 35: 1]

        def encoded = stringToEncode.collect { letter ->
            reservedCharacters[(int) letter] ? '%' + Integer.toHexString((int) letter).toString().toUpperCase() : letter
        }
        return encoded.join('')
    }

    static void resetGlobalUploadCounter() {
        EGangotriUtil.GLOBAL_UPLOADING_COUNTER = 0
    }

    static boolean checkIfArchiveProfileHasValidUserName(Map metaDataMap, String archiveProfile, boolean logErrMsg = true) {
        boolean success = false
        String username = metaDataMap."${archiveProfile}"
        String userNameInvalidMsg = 'Invalid/Non-Existent'
        String errMsg2 = " UserName [$username] in ${stripFilePath(EGangotriUtil.ARCHIVE_LOGINS_PROPERTIES_FILES.join(','))} file for $archiveProfile"
        if (username?.trim()) {
            success = username ==~ /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}/
            if (!success) {
                userNameInvalidMsg = 'Invalid Email Format of'
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

    static void uploadFileUsingSendKeys(ChromeDriver driver, String fileNameWithPath) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver
            WebElement _hiddenDiv = driver.findElement(By.xpath('//*[@id="file_drop_contents"]/div[2]'))
            js.executeScript("arguments[0].className='XXXX'", _hiddenDiv)
            js.executeScript('arguments[0].click()', _hiddenDiv)
            WebElement uploadElement = driver.findElement(By.id('file_input_initial'))
            new WebDriverWait(driver, Duration.ofSeconds(EGangotriUtil.TIMEOUT_IN_TWO_SECONDS)).until(ExpectedConditions.elementToBeClickable(By.id('file_input_initial')))
            uploadElement.sendKeys(fileNameWithPath)
        }
        catch (Exception e) {
            log.info('uploadFileUsingSendKeys', e)
        }
    }

    static void setClipboardData(String string) {
        StringSelection stringSelection = new StringSelection(string)
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null)
    }

    static <QueuedVO> Set<Set<QueuedVO>> partition(List<QueuedVO> partitionableList, int size) {
        def partitions = []
        int partitionCount = (int) (partitionableList.size() / size)

        partitionCount.times { partitionNumber ->
            def start = partitionNumber * size
            def end = start + size - 1
            partitions << partitionableList[start..end]
        }

        if (partitionableList.size() % size) partitions << partitionableList[partitionCount * size..-1]
        return partitions as Set<Set<QueuedVO>>
    }

    static void throwNoCreatorSpecifiedErrorIfNoRandomCreatorFlagAndQuit() {
        if (!EGangotriUtil.GENERATE_RANDOM_CREATOR && !EGangotriUtil.CREATOR_FROM_DASH_SEPARATED_STRING) {
            throw new Exception('No Creator. Pls provide Creator in archiveMetadata.properties file')
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
        List<String> randomCreators = RANDOM_CREATOR_BY_PROFILE_MAP["${archiveProfile}"]
        String randomPick = randomCreators[new Random().nextInt(randomCreators.size)]
        return "creator=${randomPick}"
    }

    static List<String> randomCreators() {
        List<String> firstNames = readTextFileAndDumpToList(EGangotriUtil.FIRST_NAME_FILE)
        List<String> lastNames = readTextFileAndDumpToList(EGangotriUtil.LAST_NAME_FILE)
        Random rnd = new Random()
        List<String> creators = []
        int MAX_CREATORS = RANDOM_CREATOR_MAX_LIMIT
        int max = firstNames.size() > lastNames.size() ? (firstNames.size() > MAX_CREATORS ? MAX_CREATORS : firstNames.size()) : (lastNames.size() > MAX_CREATORS ? MAX_CREATORS : lastNames.size())
        (1..max).each {
            int idx1 = rnd.nextInt(firstNames.size())
            int idx2 = rnd.nextInt(lastNames.size())
            creators << "${firstNames[idx1].trim().capitalize()} ${lastNames[idx2].trim().capitalize()}"
        }
        return creators
    }

    static String getOrGenerateSupplementaryURL(String archiveProfile) {
        if (!SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP || !SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP.containsKey(archiveProfile)) {
            SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP.put(archiveProfile, null)
        }
        if (!SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP["${archiveProfile}"]) {
            def metaDataMap = getArchiveMetadataKeyValues()
            if (!metaDataMap."${archiveProfile}.creator") {
                throwNoCreatorSpecifiedErrorIfNoRandomCreatorFlagAndQuit()
            }
            String _creator = 'creator=' + metaDataMap."${archiveProfile}.creator"
            _creator = _creator?.replaceAll(/[#!&]/, '')?.replaceAll('null', ' ')
            String _subjects = metaDataMap."${archiveProfile}.subjects"
            _subjects = _subjects?.replaceAll(/[#!&]/, '')?.replaceAll('null', ' ')
            if (!_subjects) {
                _subjects = !EGangotriUtil.GENERATE_RANDOM_CREATOR ? _creator.replaceAll('creator=', '') : null
            }

            String _desc = metaDataMap."${archiveProfile}.description"

            String filelabelVal = '{0}'
            String desc_and_file_name = "description=${_desc ? "${filelabelVal}, ${_desc}" : "${filelabelVal}"}"
            String enhancedUrl = desc_and_file_name
            if (metaDataMap."${archiveProfile}.collection") {
                enhancedUrl += AMPERSAND + 'collection=' + metaDataMap."${archiveProfile}.collection"
            }
            if (_subjects) {
                if (_subjects.contains(',') && (_subjects.contains('\"') || _subjects.contains("'"))) {
                    def doubleQuoteRegex = /".*?"/
                    def singleQuoteRegex = /'.*?'/
                    def subjectsInsideSingleQuotes = _subjects.findAll(singleQuoteRegex)
                    def subjectsInsideDoubleQuotes = _subjects.findAll(doubleQuoteRegex)
                    def both = subjectsInsideSingleQuotes + subjectsInsideDoubleQuotes
                    both.each { it ->
                        _subjects = _subjects.replaceAll(it, '')
                    }
                    def allSubjects = both.collect { it -> it.replaceAll(',', ' ') } + _subjects.split(/\s*,\s*/)*.trim().findAll { String item -> !item.isEmpty() }
                    _subjects = allSubjects.join(',')
                }
                enhancedUrl += AMPERSAND + 'subject=' + _subjects
            }
            if (!EGangotriUtil.GENERATE_RANDOM_CREATOR) {
                enhancedUrl += AMPERSAND + _creator
            }
            SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP["${archiveProfile}"] = enhancedUrl
        }
        String url = SUPPLEMENTARY_URL_FOR_EACH_PROFILE_MAP["${archiveProfile}"]
        if ((EGangotriUtil.GENERATE_RANDOM_CREATOR)) {
            String _creator = generateCreatorsForProfileAndPickARandomOne(archiveProfile)
            url += AMPERSAND + _creator

            if (!url.contains('subject=')) {
                String _subjects = 'subject=' + _creator.replaceAll('creator=', '')
                url += AMPERSAND + _subjects
            }
        }
        return url
    }

    static String getOrGenerateSupplementaryURLV2(String _subjects,
                                                 String _desc,
                                                 String creator = '') {
        String _creator = "creator=${creator}"
        String filelabelVal = '{0}'
        String desc_and_file_name = "description=${_desc ? "${filelabelVal}, ${_desc}" : "${filelabelVal}"}"
        String enhancedUrl = desc_and_file_name //+ AMPERSAND + _lang
        if (_subjects) {
            if (_subjects.contains(',') && (_subjects.contains('\"') || _subjects.contains("'"))) {
                def doubleQuoteRegex = /".*?"/
                def singleQuoteRegex = /'.*?'/
                def subjectsInsideSingleQuotes = _subjects.findAll(singleQuoteRegex)
                def subjectsInsideDoubleQuotes = _subjects.findAll(doubleQuoteRegex)
                def both = subjectsInsideSingleQuotes + subjectsInsideDoubleQuotes
                both.each { it ->
                    _subjects = _subjects.replaceAll(it, '')
                }
                def allSubjects = both.collect { it -> it.replaceAll(',', ' ') } + _subjects.split(/\s*,\s*/)*.trim().findAll { String item -> !item.isEmpty() }
                _subjects = allSubjects.join(',')
            }
            enhancedUrl += AMPERSAND + 'subject=' + _subjects
        }
        if (!EGangotriUtil.GENERATE_RANDOM_CREATOR) {
            enhancedUrl += AMPERSAND + _creator
        }
        return enhancedUrl
                                                 }

    static String generateUploadUrl(String archiveProfile, String fileNameToBeUsedAsUniqueDescription = '') {
        if (SettingsUtil.BARE_BONES_SUBJECT_DESC_URL) {
            String uploadUrl = ARCHIVE_UPLOAD_URL + "description=${archiveProfile}&subject=${archiveProfile}&creator=${archiveProfile}"
            return uploadUrl
        }
        else {
            String enhancedUrl = getOrGenerateSupplementaryURL(archiveProfile)
            String insertDescription = insertDescriptionInUploadUrl(enhancedUrl, fileNameToBeUsedAsUniqueDescription)

            String uploadUrl = ARCHIVE_UPLOAD_URL + insertDescription
            return uploadUrl.replaceAll('\"', "'")
        }
    }

    static String generateUploadUrlV2(String fileNameToBeUsedAsUniqueDescription,
                                      String _subjects,
                                      String _desc,
                                      String creator = '') {
        String enhancedUrl = getOrGenerateSupplementaryURLV2(_subjects, _desc, creator)
        String insertDescription = insertDescriptionInUploadUrl(enhancedUrl, fileNameToBeUsedAsUniqueDescription)
        String uploadUrl = ARCHIVE_UPLOAD_URL + insertDescription
        return uploadUrl.replaceAll('\"', "'")
                                      }

    static insertDescriptionInUploadUrl(String enhancedUrl, String fileNameToBeUsedAsUniqueDescription) {
        String desc = _removeAmpersandAndFetchTitleOnly(fileNameToBeUsedAsUniqueDescription)
        desc = fixEvalIssueInString(desc)
        return enhancedUrl.replace('{0}', "'${desc}'")
    }

    static String _removeAmpersandAndFetchTitleOnly(String title) {
        return stripFilePathAndFileEnding(title?.replaceAll(AMPERSAND, ''))
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
     * @param filePath Ex: C:\books\set-1\Hamlet by Shakespeare.pdf
     * @return Hamlet by Shakespeare.pdf
     */
    static String stripFilePath(String filePath) {
        log.info("filePath: ${filePath}")
        return filePath ? filePath?.trim()?.drop(filePath?.lastIndexOf(File.separator) + 1) : ''
    }

    /***
     *
     * @param filePath Ex: C:\books\set-1\Hamlet by Shakespeare.pdf
     * @return C:\books\set-1
     */
    static String stripFileTitle(String filePath) {
        return filePath ? filePath?.trim()?.take(filePath?.lastIndexOf(File.separator) + 1) : ''
    }

    /***
     *
     * @param title Ex: Hamlet by Shakespeare.pdf
     * @return Hamlet by Shakespeare
     */
    static String removeFileEnding(String title) {
        return title?.contains('.') ? title?.trim()?.tokenize('.')?.dropRight(1)?.join('.') : title
    }

    /***
     *
     * @param title Ex: Hamlet by Shakespeare.pdf
     * @return pdf
     */
    static String getFileEnding(String title) {
        return title?.contains('.') ? title?.trim()?.tokenize('.')?.last() : title
    }

    static String getLastPortionOfTitleUsingSeparator(String title, String separator = '-') {
        return title?.contains(separator) ? title?.split('-')?.last() : title
    }

    static boolean switchToLastOpenTab(ChromeDriver driver) {
        try {
            ArrayList<String> chromeTabsList = new ArrayList<String>(driver.getWindowHandles())
            //there is a bug in retrieving the size of chromeTabsList in Selenium.
            //use of last() instead of chromeTabsList.get(tabIndex+1) saves the issue
            driver.switchTo().window(chromeTabsList.last())
        }
        catch (Exception e) {
            log.info("Exception while switching to new Tab ${e.message}")
            return false
        }
        return true
    }

    static boolean openNewTab(ChromeDriver enhancedUrl, double sleepTimeInSeconds = 0.1) {
        try {
            if (sleepTimeInSeconds > 0) {
                EGangotriUtil.sleepTimeInSeconds(sleepTimeInSeconds)
            }
            JavascriptExecutor js = (JavascriptExecutor) enhancedUrl
            js.executeScript("window.open('','_blank');");
        }
        catch (Exception _ex) {
            log.error("openNewTab Exception: ${_ex.message}")
            return false
        }
        return true
    }

    static void maximizeBrowser(ChromeDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver
        js.executeScript('window.blur()')
        driver.manage().window().maximize()
    }

    static void closeBrowser(ChromeDriver driver) {
        driver.quit()
    }

    static boolean checkAlert(ChromeDriver driver, Boolean accept = true) {
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

    static String getFormattedDateString(Date date = null) {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(date ?: new Date())
    }

    static String getFormattedDateString(long date) {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(date > 0 ? new Date(date) : new Date())
    }

    static String generateStats(List<List<Integer>> uploadStats, String archiveProfile, Integer countOfUplodableFiles) {
        Integer uplddSum = uploadStats.collect { List<Integer> elem -> elem.first() }.sum() as Integer
        String statsAsPlusSeparatedValues = uploadStats.collect { elem -> elem.first() }.join(' + ')
        String countOfUploadedItems = uploadStats.size() > 1 ? "($statsAsPlusSeparatedValues) = $uplddSum" : uploadStats?.first()?.first()

        Integer excSum = uploadStats.collect { elem -> elem.last() }.sum() as int
        String excpsAsPlusSeparatedValues = uploadStats.collect { elem -> elem.last() }.join(' + ')
        String exceptionCount = uploadStats.size() > 1 ? "($excpsAsPlusSeparatedValues) = $excSum" : uploadStats?.first()?.last()
        log.info("Uploaded $countOfUploadedItems items with (${exceptionCount}) Exceptions for Profile: $archiveProfile")

        String statusMsg = countOfUplodableFiles == uplddSum ? 'Success. All items were put for upload.' : "${(uplddSum == 0) ? 'All' : 'Some'} Failed!"
        String report = "$archiveProfile, \t Total $countOfUplodableFiles,\t " +
                "Attempted Upload Count $countOfUploadedItems,\t with  ${exceptionCount} Exceptions \t $statusMsg"
        log.info(report)
        return report
    }
    /**
     * Fixes the issue of eval being encoded as %65val in the upload link.
     * @param stringWhichCouldHaveEval The string which could have eval in it.
     * @return The string with eval fixed.
     issue caused when such unix like keywords such as
     eval/time/select/make  are existing in the description
     making the archive server think that this is a injection attack
     */
    static String fixEvalIssueInString(String textToAlter) {
        String result = maintainCase(textToAlter,/(?i)eval/, 'eval.')
        result = maintainCase(result,/(?i)time/, '.')
        result = maintainCase(result,/(?i)select/, '.')
        result = maintainCase(result,/(?i)make/, '.')
        return URLEncoder.encode(result, "UTF-8")
    }

    static String maintainCase(String textToAlter, String regex, String replacement) {
        String result = textToAlter.replaceAll(regex) { match -> match + replacement }
        return result
    }
}
