package com.egangotri.util


import com.egangotri.upload.util.UploadUtils
import groovy.util.logging.Slf4j

@Slf4j
class EGangotriUtil {
    static{
        System.setProperty("webdriver.chrome.silentOutput", "true");
    }
    static final String USER_HOME = System.getProperty('user.home')
    static final String PDF = ".pdf"
    static final String PROPERTIES = ".properties"

    static long PROGRAM_START_TIME_IN_MILLISECONDS = 0
    static long PROGRAM_END_TIME_IN_MILLISECONDS = 0


    static final String EGANGOTRI_BASE_DIR = USER_HOME + File.separator + "eGangotri"

    static final String ARCHIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "archiveLogins" + PROPERTIES
    static final String SETTINGS_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "settings" + PROPERTIES

    static final String GOOGLE_DRIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "googleDriveLogins" + PROPERTIES
    static final String LOCAL_FOLDERS_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "localFolders" + PROPERTIES
    static final String ARCHIVE_METADATA_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "archiveMetadata" + PROPERTIES

    static final String ARCHIVE_ITEMS_QUEUED_FOLDER = EGANGOTRI_BASE_DIR + File.separator + "items_queued"
    static final String ARCHIVE_ITEMS_USHERED_FOLDER = EGANGOTRI_BASE_DIR + File.separator + "items_ushered"
    static final String ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER = EGANGOTRI_BASE_DIR + File.separator + "items_post_validation"

    static String ARCHIVE_QUEUED_ITEMS_FILE = EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER + File.separator + "queued_items_{0}.csv"
    static String ARCHIVE_USHERED_ITEMS_FILE = EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER + File.separator + "ushered_items_{0}.csv"

    static String ARCHIVE_ITEMS_QUEUED_POST_VALIDATION_FILE = EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER + File.separator + "queued_post_validation_items_{0}.csv"
    static String ARCHIVE_ITEMS_USHERED_POST_VALIDATION_FILE = EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER + File.separator + "ushered_post_validation_items_{0}.csv"
    static
    final String UPLOAD_PROFILES_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "uploadProfiles" + PROPERTIES

    static final String FIRST_NAME_FILE = EGANGOTRI_BASE_DIR + File.separator + "first_names.txt"
    static final String LAST_NAME_FILE = EGANGOTRI_BASE_DIR + File.separator + "last_names.txt"
    static final String CODE_404_BAD_DATA_FOLDER = EGANGOTRI_BASE_DIR + File.separator + "_code404_rejectedFiles"
    static final String CODE_503_SLOW_DOWN_FOLDER = EGANGOTRI_BASE_DIR + File.separator + "_code503_rejectedFiles"
    static String MANUAL_SNAPSHOT_REPO = EGangotriUtil.EGANGOTRI_BASE_DIR + File.separator + "Snap2HTML\\listings"
    static String DATE_TIME_AM_PATTERN = "dd-MMM-yyyy hh.mm aa"
    static String DATE_TIME_PATTERN = "dd-MMM-yyyy hh:mm:ss"

    static final int UPLOAD_FAILURE_THRESHOLD = 5
    static int MAX_UPLODABLES = 1000
    static int ARCHIVE_WAITING_PERIOD_ONE_SEC = 1000
    static Boolean GENERATE_ONLY_URLS = false
    static int PARTITION_SIZE = 250
    static boolean PARTITIONING_ENABLED = false
    static boolean GENERATE_RANDOM_CREATOR = false

    static boolean WRITE_TO_MONGO_DB = true
    static String UPLOAD_RUN_ID = ""

    //This not implemented
    static List ACCOUNTS_WITH_RANDOMIZABLE_CREATORS = []

    static List IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS = []
    static boolean CREATOR_FROM_DASH_SEPARATED_STRING = false

    static boolean ADD_RANDOM_INTEGER_TO_PAGE_URL = true

    //using ("A".."z" ) will introduce non-alpha chars
    static final List ASCII_ALPHA_CHARS = ("A".."Z").toList() + ("a".."z").toList()
    static final def ASCII_CHARS_SIZE = ASCII_ALPHA_CHARS.size()


    static final int TIMEOUT_IN_TWO_SECONDS = 5

    static final int TIMEOUT_IN_FIVE_SECONDS = 5
    static final int TEN_TIMES_TIMEOUT_IN_SECONDS = TIMEOUT_IN_FIVE_SECONDS * 10

    static final String USER_ID = "userId"
    //password
    static final String KUTA = "kuta"
    static final String KUTA_SECOND = "kuta2"

    public static List<String> ARCHIVE_PROFILES = getAllArchiveProfiles()
    static List GOOGLE_PROFILES = getAllGoogleDriveProfiles()
    static int GLOBAL_UPLOADING_COUNTER = 0


    static List getAllProfiles(String propertyFileName) {
        Properties properties = new Properties()
        File propertiesFile = new File(propertyFileName)

        if (!propertiesFile.exists()) {
            log.info("$propertyFileName not found.")
            return []
        }

        List profiles = []

        properties.load(propertiesFile.newDataInputStream())
        properties.each { key, v ->
            if (!key.toString().contains(KUTA)) {
                profiles << key
            }
        }
        return profiles
    }

    static List getAllGoogleDriveProfiles() {
        return getAllProfiles(GOOGLE_DRIVE_PROPERTIES_FILE)
    }

    static List getAllArchiveProfiles() {
        return getAllProfiles(ARCHIVE_PROPERTIES_FILE)
    }

    static String hidePassword(String pwd) {
        String hiddenPwd = ""
        pwd.size().times { hiddenPwd += "*" }
        return hiddenPwd
    }

    static void sleepTimeInSeconds(double sleepTimeInSeconds, boolean overrideEgangotriWaitingPeriod = false) {
        if(sleepTimeInSeconds>30){
            sleepTimeInSeconds = 30
        }
        Thread.sleep(overrideEgangotriWaitingPeriod ? (sleepTimeInSeconds * 1000).toInteger() : (EGangotriUtil.ARCHIVE_WAITING_PERIOD_ONE_SEC * sleepTimeInSeconds).toInteger())
    }

    static List<String> getAllUploadProfiles() {
        Properties properties = new Properties()
        File propertiesFile = new File(UPLOAD_PROFILES_PROPERTIES_FILE)
        propertiesFile.withInputStream { stream ->
            properties.load(stream)
        }
        return properties.values()*.toString().toList()
    }

    static void recordProgramStart(String program = "") {
        EGangotriUtil.PROGRAM_START_TIME_IN_MILLISECONDS = System.currentTimeMillis()
        log.info "Program $program started @ " + UploadUtils.getFormattedDateString(EGangotriUtil.PROGRAM_START_TIME_IN_MILLISECONDS)
    }

    static void recordProgramEnd() {
        EGangotriUtil.PROGRAM_END_TIME_IN_MILLISECONDS = System.currentTimeMillis()
        log.info "Program Execution ended @ " + UploadUtils.getFormattedDateString(EGangotriUtil.PROGRAM_END_TIME_IN_MILLISECONDS)
    }


    static List csvToList(String csv) {
        String _csv = csv.replaceAll(/["|\[|\]|'|\s]/, "").trim()
        return _csv ? _csv.split(",")*.trim() : []
    }
}
