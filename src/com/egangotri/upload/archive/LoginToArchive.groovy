package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Slf4j
class LoginToArchive {
    static final List ARCHIVE_PROFILES = [/*ArchiveHandler.PROFILE_ENUMS.IB, ArchiveHandler.PROFILE_ENUMS.DT, ArchiveHandler.PROFILE_ENUMS.JG, */ArchiveHandler.ARCHIVE_PROFILE.IB]


    static main(args) {
        println "Start uploading to Archive"
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        ARCHIVE_PROFILES*.toString().each { String archiveProfile ->
            println "Logging for Profile $archiveProfile"
            ArchiveHandler.loginToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
        }
        println "***Browser Launches Done"
    }
}

