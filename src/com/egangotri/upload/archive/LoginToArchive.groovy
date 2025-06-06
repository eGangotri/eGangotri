package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.openqa.selenium.chrome.ChromeDriver
import com.egangotri.upload.util.ChromeDriverConfig

import static com.egangotri.upload.util.ArchiveUtil.getResultsCount

@Slf4j
class LoginToArchive {
    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        def archiveLoginsMetaDataMap = UploadUtils.getAllArchiveLogins()
        if (args) {
            log.info "args $args"
            //    args = ["email=indicjournals;range=1-30"]
            if (args[0].contains("email")) {
                String[] args0 = args[0].split(";");
                String[] emailTemplate = args0[0].split("=");
                String[] range = args0[1].split("=")
                String[] rangeSplitWithDash = range[1].split("-")
                def _range = rangeSplitWithDash[0].toInteger()..rangeSplitWithDash[1].toInteger()
                List emails = _range.collect({ emailTemplate[1] + (it == 1 ?"":it) + "@gmail.com" })
                List genProfiles = _range.collect({ "GENPRFL" + it })
                for(int i = 0; i < emails.size(); i++) {
                    archiveLoginsMetaDataMap.put(genProfiles[i], emails[i])
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
        if (archiveLoginsMetaDataMap) {
            archiveProfiles.each { String archiveProfile ->
                {
                    if (archiveLoginsMetaDataMap.containsKey(archiveProfile)) {
                        log.info "Logging for Profile $archiveProfile"
                        ChromeDriver driver = ChromeDriverConfig.createDriver()
                        if (ArchiveUtil.navigateLoginLogic(driver, archiveLoginsMetaDataMap, archiveProfile)) {
                            getResultsCount(driver, true)
                            UploadUtils.maximizeBrowser(driver)
                        }
                    } else {
                        log.info "$archiveProfile profile doesnt exist in file ${EGangotriUtil.ARCHIVE_LOGINS_PROPERTIES_FILES.join(",")}. Cannot proceed."
                    }
                }
            }
        } else {
            log.info "No MetaData Cannot proceed. ${EGangotriUtil.ARCHIVE_LOGINS_PROPERTIES_FILES.join(",")} is either empty or doesnt exist."
        }
        EGangotriUtil.recordProgramEnd()
        System.exit(0)
    }
}

