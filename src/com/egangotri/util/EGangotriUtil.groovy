package com.egangotri.util

import com.egangotri.upload.util.UploadUtils
import groovy.util.logging.Slf4j

@Slf4j
class EGangotriUtil {
    static final String HOME = System.getProperty('user.home')
    static final String PDF = ".pdf"
    static final String EGANGOTRI_BASE_DIR = HOME + File.separator + "eGangotri"
    static final String PROPERTIES = ".properties"
    static final String PRECUTOFF_PROFILE = "IB"
    static final List MANUSCRIPT_PROFILES = ["DT"]

    static final String MANUSCRIPTS = "MANUSCRIPTS"


    static final String ARCHIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "archiveLogins" + PROPERTIES
    static final String SETTINGS_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "settings" + PROPERTIES

    static final String BULK_UPLOAD_ARCHIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "bulkUploadArchiveLogins" + PROPERTIES
    static final String GOOGLE_DRIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "googleDriveLogins" + PROPERTIES
    static final String LOCAL_FOLDERS_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "localFolders" + PROPERTIES
    static final String ARCHIVE_METADATA_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "archiveMetadata" + PROPERTIES

    static Boolean GENERATE_ONLY_URLS = false
    static int PARTITION_SIZE = 250
    static boolean PARTITIONING_ENABLED = false


    static
    final String UPLOAD_PROFILES_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "uploadProfiles" + PROPERTIES

    static final String USER_ID = "userId"
    static final String USER_NAME = "username"

    static List ARCHIVE_PROFILES = getAllArchiveProfiles()
    static List GOOGLE_PROFILES = getAllGoogleDriveProfiles()

    static List getAllProfiles(String filePath, String textDiscarder) {
        Properties properties = new Properties()
        File propertiesFile = new File(filePath)

        if(!propertiesFile.exists()){
            println("$filePath not found.")
            return []
        }

        propertiesFile.withInputStream { stream ->
            properties.load(stream)
        }

        List profiles = []

        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (key.contains(textDiscarder)) {
                profiles << (key - textDiscarder)
            }
        }
        return profiles
    }

    static List getAllGoogleDriveProfiles() {
        return getAllProfiles(GOOGLE_DRIVE_PROPERTIES_FILE, ".$USER_ID")
    }

    static List getAllArchiveProfiles() {
        return getAllProfiles(ARCHIVE_PROPERTIES_FILE, ".$USER_NAME")
    }


    static List getAllBulkUploadArchiveProfiles() {
        return getAllProfiles(ARCHIVE_PROPERTIES_FILE, ".$USER_NAME")
    }

    static List getAllUploadProfiles() {
        Properties properties = new Properties()
        File propertiesFile = new File(UPLOAD_PROFILES_PROPERTIES_FILE)
        propertiesFile.withInputStream { stream ->
            properties.load(stream)
        }
        return properties.values().toList()
    }

    static boolean isAPreCutOffProfile(String archiveProfile) {
        return (archiveProfile == PRECUTOFF_PROFILE)
    }

    static boolean isAManuscriptProfile(String uploadProfile) {
        println "uploadProfile $uploadProfile ="
        return getAllManuscriptProfiles().contains(uploadProfile)
    }


    static List<String> nonManuscriptFolders() {
        List<String> nonManuFolders = []
        FileUtil.ALL_FOLDERS.each { k, v ->
            if (!MANUSCRIPT_PROFILES.contains(k)) {
                nonManuFolders << v
            }
        }
        return nonManuFolders
    }

    static List<String> manuscriptFolders() {
        List<String> manuFolders = FileUtil.ALL_FOLDERS.values() - nonManuscriptFolders()
        return manuFolders
    }

    static List<String> getAllManuscriptProfiles() {
        Map uploadProfileMap = UploadUtils.loadProperties(UPLOAD_PROFILES_PROPERTIES_FILE)
        List manuProfiles = uploadProfileMap.get(MANUSCRIPTS).split(",").toList()
        return manuProfiles
    }
}
