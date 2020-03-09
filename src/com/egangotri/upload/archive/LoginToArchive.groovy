package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

import static com.egangotri.upload.util.ArchiveUtil.getResultsCount

@Slf4j
class LoginToArchive {
    static final List ARCHIVE_PROFILES = []


    static main(args) {
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }
        log.info "login to Archive"
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        archiveProfiles*.toString().each { String archiveProfile ->
            log.info "Logging for Profile $archiveProfile"
            WebDriver driver = new ChromeDriver()
            ArchiveUtil.logInToArchiveOrg(driver, metaDataMap, archiveProfile)
            getResultsCount(driver, true)
        }
        log.info "***Browser Launches Done"
        System.exit(0)
    }
}

