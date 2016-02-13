package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import org.slf4j.LoggerFactory

/**
 * Created by user on 2/7/2016.
 */
class LoginToArchive {
    final static org.slf4j.Logger Log = LoggerFactory.getLogger(this.class);

    static final List ARCHIVE_PROFILES = [/*ArchiveHandler.PROFILE_ENUMS.ib, ArchiveHandler.PROFILE_ENUMS.dt, ArchiveHandler.PROFILE_ENUMS.jg, */ArchiveHandler.PROFILE_ENUMS.ib]


    static main(args) {
        println "Start uploading to Archive"
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/UserIdsMetadata.properties")
        ARCHIVE_PROFILES*.toString().each { String archiveProfile ->
            println "Logging for Profile $archiveProfile"
            ArchiveHandler.loginToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
        }
        println "***Browser Launches Done"
    }
}

