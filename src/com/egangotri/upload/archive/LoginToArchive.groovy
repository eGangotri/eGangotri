package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class LoginToArchive {
    static final List ARCHIVE_PROFILES = []


    static main(args) {
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            println "args $args"
            archiveProfiles = args.toList()
        }
        log.info "login to Archive"
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        archiveProfiles*.toString().each { String archiveProfile ->
            println "Logging for Profile $archiveProfile"
            ArchiveHandler.loginToArchive(metaDataMap, archiveProfile)
        }
        println "***Browser Launches Done"
    }
}

