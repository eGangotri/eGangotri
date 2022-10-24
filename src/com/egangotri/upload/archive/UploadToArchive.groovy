package com.egangotri.upload.archive

import com.egangotri.rest.RestUtil
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.util.ValidateUtil
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the resources${File.separator}chromedriverDEL.exe,
 * use VM Argument -Dwebdriver.chrome.driver=resources${File.separator}chromedriverDEL.exe

 C:\ws\eGangotri>java -Dwebdriver.chrome.driver=resources${File.separator}chromedriverDEL.exe -jar ./build/libs/eGangotri.jar "PRFL-1"
 java -Dwebdriver.chrome.driver=/latestJarForUse/chromedriver\chromedriverDEL.exe -jar ./build/libs/eGangotri.jar "PRFL-1"
 ** Dont use \ followed by a U
 */
@Slf4j
class UploadToArchive {

    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        SettingsUtil.applySettings()
        Set<String> purgedProfiles = ArchiveUtil.filterInvalidProfiles(archiveProfiles, metaDataMap) as Set
        boolean previewSuccess = true

        if(EGangotriUtil.WRITE_TO_MONGO_DB){
            boolean isOn = RestUtil.checkIfDBServerIsOn()
            if(!isOn){
                log.info("This Upload Run is configured to write to DB but the DB Server is not on\nHence cannot proceed")
                return
            }
        }
        if(SettingsUtil.PREVIEW_FILES){
            previewSuccess = PreUploadReview.preview(purgedProfiles)
        }

        if(previewSuccess){
            execute(purgedProfiles, metaDataMap)
            if( ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION> 0 && SettingsUtil.REUPLOAD_OF_FAILED_ITEMS_ON_SETTING){
                log.info("Going to sleep for ${SettingsUtil.REUPLOAD_FAILED_ITEMS_WAIT_PERIOD_IN_MINUTES} minutes ...@${UploadUtils.getFormattedDateString()}\n")
                //Wait for 1 Hour and check Links also
                Thread.sleep(SettingsUtil.REUPLOAD_FAILED_ITEMS_WAIT_PERIOD_IN_MINUTES*1000*60)
                log.info("Will now reupload any missed item...\n")
                ValidateUploadsAndReUploadFailedItems.execute(new String[0])
                if( ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION> 0) {
                    log.info("Second Sleep Session started for ${SettingsUtil.REUPLOAD_FAILED_ITEMS_WAIT_PERIOD_IN_MINUTES}.....@${UploadUtils.getFormattedDateString()}\n")
                    //Wait for some time and check Links also
                    Thread.sleep(SettingsUtil.REUPLOAD_FAILED_ITEMS_WAIT_PERIOD_IN_MINUTES*1000*60)
                    log.info("Second check/reupload started...\n")
                    CopyPostValidationFoldersToQueuedAndUsheredFolders.execute(new String[0])
                    if( ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION == 0){
                        log.info("All missed files were uploaded succesfully. `Aum Shanti`\n")
                    }
                    else {
                        log.info("${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} item(s) had to be reuploaded. Must check there upload status manually\n")
                    }
                }
            }
        }
        else{
            log.info("Preview failed")
        }
        System.exit(0)
    }

    static void execute(Set<String> profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        EGangotriUtil.recordProgramStart("eGangotri Archiver")
        ValidateUtil.validateMaxUploadableLimit()
        int attemptedItemsTotal = 0
        Set<QueuedVO> allUploadablesAsVO = ArchiveUtil.generateUploadVoForAllUploadableItems(profiles)
        ArchiveUtil.storeAllUplodableItemsInFile(allUploadablesAsVO)
        profiles.eachWithIndex { archiveProfile, index ->
            Integer countOfUploadableItems = FileRetrieverUtil.getCountOfUploadableItemsForProfile(archiveProfile)
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile. Total Uplodables: ${countOfUploadableItems}/${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}"
            if (countOfUploadableItems) {
                if (EGangotriUtil.GENERATE_ONLY_URLS) {
                    List<String> uploadables = FileRetrieverUtil.getUploadablesForProfile(archiveProfile)
                    ArchiveHandler.generateAllUrls(archiveProfile, uploadables)
                } else {
                    List<String> uploadables = FileRetrieverUtil.getUploadablesForProfile(archiveProfile)
                    Set<QueuedVO> vos = ArchiveUtil.generateVOsFromFileNames(archiveProfile,uploadables)
                    List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(metaDataMap, vos)
                    String report = UploadUtils.generateStats(uploadStats, archiveProfile, countOfUploadableItems)
                    uploadSuccessCheckingMatrix.put((index + 1), report)
                }
                attemptedItemsTotal += countOfUploadableItems
            } else {
                log.info "No uploadable files for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5, true)
        }

        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, attemptedItemsTotal)
    }
}


