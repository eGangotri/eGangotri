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
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        if (args) {
            log.info "args $args"
            //    args = ["email=indicjournals;range=1-30"]
            if (args[0].contains("email")) {
                String[] args0 = args[0].split(";");
                String[] emailTemplate = args0[0].split("=");
                String[] range = args0[1].split("=")
                String[] rangeSplitWithDash = range[1].split("-")
                def _range = rangeSplitWithDash[0].toInteger()..rangeSplitWithDash[1].toInteger()
                List emails = _range.collect({ emailTemplate[1] + (it === 1 ?"":it) + "@gmail.com" })
                List genProfiles = _range.collect({ "GENPRFL" + it })
                for(int i = 0; i < emails.size(); i++) {
                    metaDataMap.put(genProfiles[i], emails[i])
                }
                log.info("emails $emails")
                log.info("genProfiles $genProfiles")
                archiveProfiles = genProfiles
                log.info("archiveProfiles $genProfiles")

            } else {
                archiveProfiles = args.toList()
            }
        }
        EGangotriUtil.recordProgramStart("eGangotri Archive Logger")
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

