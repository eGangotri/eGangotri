package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=${System.getProperty('user.home')}${File.separator}chromedriver${File.separator}chromedriver.exe
 * chromedriver.exe
 C:\ws\eGangotri>java -Dwebdriver.chrome.driver=${System.getProperty('user.home')}${File.separator}chromedriver${File.separator}chromedriver.exe -jar ./build/libs/eGangotri.jar "DT"
 java -Dwebdriver.chrome.driver=/Users/user/chromedriver\chromedriver.exe -jar ./build/libs/eGangotri.jar "DT"
 ** Dont use \ followeb by a U

 */
@Slf4j
class UploadToArchive {

    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }s
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        SettingsUtil.applySettings()
        execute(archiveProfiles, metaDataMap)
    }

    static void execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive @ " + UploadUtils.getFormattedDateString()

        profiles*.toString().eachWithIndex { archiveProfile, index ->
            if (!UploadUtils.checkIfArchiveProfileHasValidUserName(metaDataMap, archiveProfile)) {
                return
            }
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile"
            Integer countOfUploadablePdfs = UploadUtils.getCountOfUploadablePdfsForProfile(archiveProfile)
            if (countOfUploadablePdfs) {
                log.info "getUploadablesForProfile: $archiveProfile: ${countOfUploadablePdfs}"
                if (EGangotriUtil.GENERATE_ONLY_URLS) {
                    List<String> uploadables = UploadUtils.getUploadablesForProfile(archiveProfile)
                    ArchiveHandler.generateAllUrls(archiveProfile, uploadables)
                } else {
                    List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(metaDataMap, archiveProfile)
                    String report = UploadUtils.generateStats(uploadStats, archiveProfile, countOfUploadablePdfs)
                    uploadSuccessCheckingMatrix.put((index + 1), report)
                }
            } else {
                log.info "No uploadable files for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }

        ArchiveUtil.printUplodReport(uploadSuccessCheckingMatrix)
        log.info "***End of Upload to Archive Program"
        System.exit(0)

    }


}


