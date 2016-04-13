package com.egangotri.util

import com.egangotri.upload.archive.ArchiveHandler

/**
 * Created by user on 2/13/2016.
 */
class EGangotriUtil {
    static final String HOME = System.getProperty('user.home')
    static final String PDF = ".pdf"
    static final String EGANGOTRI_BASE_DIR = HOME + File.separator + "eGangotri"
    static final String PROPERTIES = ".properties"
    static final String PRECUTOFF_PROFILE = ArchiveHandler.ARCHIVE_PROFILE.IB.toString()
    static final String MANUSCRIPT_PROFILE = ArchiveHandler.ARCHIVE_PROFILE.DT.toString()


    static final String ARCHIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "archiveLogins" + PROPERTIES
    static final String GOOGLE_DRIVE_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "googleDriveLogins" + PROPERTIES
    static final String LOCAL_FOLDERS_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "localFolders" + PROPERTIES
    static final String ARCHIVE_METADATA_PROPERTIES_FILE = EGANGOTRI_BASE_DIR + File.separator + "archiveMetadata" + PROPERTIES

    static boolean isAPreCutOffProfile(String archiveProfile){
        return (archiveProfile == PRECUTOFF_PROFILE)
    }

    static boolean isAManuscriptProfile(String archiveProfile){
        return (archiveProfile == MANUSCRIPT_PROFILE)
    }


    static boolean isAManuscriptFolder(String folder){
        return folder.contains("avn")
    }

    static List<String> manuscriptFolders(){
        return FileUtil.ALL_FOLDERS.values().toList().findAll {it.contains("avn")}
    }

    static List<String> nonManuscriptFolders(){
        return FileUtil.ALL_FOLDERS.values().toList().findAll {!it.contains("avn")}
    }
}
