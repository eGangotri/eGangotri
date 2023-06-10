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
    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }
        EGangotriUtil.recordProgramStart("eGangotri Archive Logger")
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        if (metaDataMap) {
            archiveProfiles.each { String archiveProfile ->
                {
                    if (metaDataMap.containsKey(archiveProfile)) {
                        log.info "Logging for Profile $archiveProfile"
                        ChromeDriver driver = new ChromeDriver()
                        if (ArchiveUtil.navigateLoginLogic(driver, metaDataMap, archiveProfile)) {
                            getResultsCount(driver, true)
                            UploadUtils.maximizeBrowser(driver)
                        }
                    } else {
                        log.info "$archiveProfile profile doesnt exist in file ${EGangotriUtil.ARCHIVE_PROPERTIES_FILE}. Cannot proceed."
                    }
                }
            }
        } else {
            log.info "No MetaData Cannot proceed. ${EGangotriUtil.ARCHIVE_PROPERTIES_FILE} is either empty or doesnt exist."
        }
        EGangotriUtil.recordProgramEnd()
        System.exit(0)
    }
}

