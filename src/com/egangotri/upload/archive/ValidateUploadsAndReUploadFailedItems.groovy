package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.util.ValidateUtil
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.upload.vo.UsheredVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class ValidateUploadsAndReUploadFailedItems {
    static Set archiveProfiles = []
    static File USHERED_ITEMS_FILE = null
    static File ALL_UPLOADABLE_ITEMS_FILE = null

    static Set<QueuedVO> ALL_UPLOADABLE_ITEMS_FOR_TESTING = [] as Set
    static Set<UsheredVO> USHERED_LINKS_FOR_TESTING = []

    static Set<QueuedVO> MISSED_OUT_ALL_UPLOADABLE_ITEMS = []
    static Set<UsheredVO> MISSED_OUT_USHERED_ITEMS = []

    static Set<? extends UploadVO> ALL_FAILED_ITEMS =  []
    static Set<UsheredVO> ITEMS_WITH_CODE_404_BAD_DATA =  []
    static Set<UsheredVO> ITEMS_WITH_CODE_503_SLOW_DOWN =  []


    static void main(String[] args) {
        execute(args)
        System.exit(0)
    }

    static void execute(String[] args, String programName = "ValidateUploadsAndReUploadFailedItems"){
        EGangotriUtil.recordProgramStart(programName)
        SettingsUtil.applySettingsWithReuploaderFlags()
        startValidation(args)
    }

    static void startValidation(String[] args = null,
                                boolean dontUseFailedLinksFromStaticList = true){
        setCSVsForValidation(args)
        processAllUplodableCSV()
        processUsheredCSV()
        findAllUploadableItemsNotInUsheredCSV()
       if(dontUseFailedLinksFromStaticList) {
           filterFailedUsheredItems()
       }
        else{
           //(generateFailedLinksFromStaticList) for use in special cases only
           ReuploadUsingLinks.generateFailedLinksFromStaticList()
       }
        combineAllFailedItems()
        startReuploadOfFailedItems()
    }

    static void findMissedQueueItemsOnlyAndReupload(boolean generateStatsOnly = true){
        EGangotriUtil.recordProgramStart("findMissedQueueItemsOnlyAndReupload")
        SettingsUtil.applySettingsWithReuploaderFlags([false,true,!generateStatsOnly,false])
        startValidation()
    }

    static void findMissedUsheredItemsOnlyAndReupload(boolean generateStatsOnly = false){
        EGangotriUtil.recordProgramStart("findMissedUsheredItemsOnlyAndReupload")
        SettingsUtil.applySettingsWithReuploaderFlags([true,false,generateStatsOnly,false])
        startValidation()
    }
    static void setCSVsForValidation(String[] args) {
        ALL_UPLOADABLE_ITEMS_FILE = ValidateUtil.getLastModifiedFile(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER)
        USHERED_ITEMS_FILE = ValidateUtil.getLastModifiedFile(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER)

        if (!ALL_UPLOADABLE_ITEMS_FILE) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }

        if (!USHERED_ITEMS_FILE) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }

        if (args) {
            log.info "args $args"
            if (args?.size() > 2) {
                log.error("Only 2 File Name(s) can be accepted.Cannot proceed. Quitting")
                System.exit(0)
            }
            String _file_1 = args.first().endsWith(".csv") ? args.first() : args.first() + ".csv"
            String _file_2 = args.last().endsWith(".csv") ? args.last() : args.last() + ".csv"

            ALL_UPLOADABLE_ITEMS_FILE = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER + File.separator + _file_1)
            USHERED_ITEMS_FILE = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER + File.separator + _file_2)

            if (!ALL_UPLOADABLE_ITEMS_FILE) {
                log.error("No such File ${ALL_UPLOADABLE_ITEMS_FILE} in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }

            if (!USHERED_ITEMS_FILE) {
                log.error("No such File ${USHERED_ITEMS_FILE} in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }
        log.info("ALL_UPLOADABLE_ITEMS_FILE for processing: ${ALL_UPLOADABLE_ITEMS_FILE.name}")
        log.info("USHERED_ITEMS_FILE for processing: ${USHERED_ITEMS_FILE.name}")
    }

    static void processAllUplodableCSV() {
        if(SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Queued Items will be ignored for upload")
            return
        }
        ALL_UPLOADABLE_ITEMS_FOR_TESTING = ValidateUtil.csvToQueuedVO(ALL_UPLOADABLE_ITEMS_FILE)
        Set allUploadableProfiles = ALL_UPLOADABLE_ITEMS_FOR_TESTING*.archiveProfile as Set
        log.info("Converted " + ALL_UPLOADABLE_ITEMS_FOR_TESTING.size() + " Queued Item(s) from CSV in Profiles ${allUploadableProfiles.toString()}")
    }

    static void processUsheredCSV() {
        if(SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Queued Items will be ignored for upload")
            return
        }
        USHERED_LINKS_FOR_TESTING = ValidateUtil.csvToUsheredItemsVO(USHERED_ITEMS_FILE)
        archiveProfiles = USHERED_LINKS_FOR_TESTING*.archiveProfile as Set
        log.info("Converted " + USHERED_LINKS_FOR_TESTING.size() + " links of upload-ushered Item(s) from CSV in Profiles ${archiveProfiles.toString()}")
    }

    // This function produces QueuedItem - usheredItem
    //Queued Item is a superset of usheredItem
    static void findAllUploadableItemsNotInUsheredCSV() {
        if(SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            MISSED_OUT_ALL_UPLOADABLE_ITEMS = ALL_UPLOADABLE_ITEMS_FOR_TESTING
            return
        }
        List usheredLinksPaths = USHERED_LINKS_FOR_TESTING*.path
        log.info("Searching from ${ALL_UPLOADABLE_ITEMS_FOR_TESTING?.size()} Queued Item(s) that were never upload-ushered in ${USHERED_LINKS_FOR_TESTING.size()} identifiers")

        ALL_UPLOADABLE_ITEMS_FOR_TESTING.eachWithIndex { allUploadedItem, index ->
            if (!usheredLinksPaths.contains(allUploadedItem.path)) {
                MISSED_OUT_ALL_UPLOADABLE_ITEMS << allUploadedItem
                log.info("\tFound missing Item [ (# $index). ${allUploadedItem.archiveProfile}] ${allUploadedItem.title} ")
            }
        }
        log.info("${MISSED_OUT_ALL_UPLOADABLE_ITEMS.size()}/${ALL_UPLOADABLE_ITEMS_FOR_TESTING.size()} Items found in Queued List that missed upload.")
        log.info("Affected Profies "  +  (MISSED_OUT_ALL_UPLOADABLE_ITEMS*.archiveProfile as Set).toString())
    }

    static void filterFailedUsheredItems() {
        if(SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("\n\nUshered Items will be ignored for upload")
            return
        }
        int testableLinksCount = USHERED_LINKS_FOR_TESTING.size()
        if(!testableLinksCount){
            log.info("No Items in Upload Ushered File")
            return
        }
        log.info("\nTesting ${testableLinksCount} Links in archive to be tested for upload-success-confirmation")
        USHERED_LINKS_FOR_TESTING.eachWithIndex { UsheredVO entry, int i ->
            String urlText = ""
            try {
                urlText = entry.archiveLink.toURL().text
                checkIfCode404BadFile(urlText, entry,i)
                print("${i},")
            }
            catch (FileNotFoundException e) {
                MISSED_OUT_USHERED_ITEMS << entry
                log.info("\nFailed Link (${MISSED_OUT_USHERED_ITEMS.size()} of $testableLinksCount): \"${entry.archiveLink}\" @ ${i}..")
            }
            catch (Exception e) {
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                MISSED_OUT_USHERED_ITEMS << entry
            }
            if(i> 0 && i%35 == 0){
                System.gc()
                log.info("")
            }
        }
        logUsheredMissedInfo()
    }

    static void  checkIfCode404BadFile(String urlText, UsheredVO entry, int counter){
        int checkDownloadOptions = urlText.count("format-group")
        int _4Files = urlText.count("4 Files")
        if(checkDownloadOptions < 2 && _4Files == 1){
            ITEMS_WITH_CODE_404_BAD_DATA << entry
            log.info("\nCode 404 Bad Data File: \"${entry.archiveLink}\" Counter # ${counter}..")
           ValidateUtil.moveFile(entry, EGangotriUtil.CODE_404_BAD_DATA_FOLDER)
        }
    }
    static void logUsheredMissedInfo(){
       String _msg = "\nFound ${MISSED_OUT_USHERED_ITEMS.size()}/${USHERED_LINKS_FOR_TESTING.size()} failed Ushered Link(s)."
        ValidateUtil.logPerProfile(_msg,MISSED_OUT_USHERED_ITEMS,"archiveLink")

        String _msg2 = "\nFound ${ITEMS_WITH_CODE_404_BAD_DATA?.size()}/${USHERED_LINKS_FOR_TESTING.size()} Code 400 Bad Data Files: (repair with pdftk and reupload manually)"
        ValidateUtil.logPerProfile(_msg2, ITEMS_WITH_CODE_404_BAD_DATA,"path")
    }

    static void combineAllFailedItems(){
        if (MISSED_OUT_ALL_UPLOADABLE_ITEMS || MISSED_OUT_USHERED_ITEMS) {
            ALL_FAILED_ITEMS.addAll(MISSED_OUT_ALL_UPLOADABLE_ITEMS)

            MISSED_OUT_USHERED_ITEMS.each { failedLink ->
                ALL_FAILED_ITEMS.add(failedLink)
            }
            log.info("Combined figure for re-uploading(${MISSED_OUT_ALL_UPLOADABLE_ITEMS.size()} + ${MISSED_OUT_USHERED_ITEMS.size()}) :" + ALL_FAILED_ITEMS.size() + " in Profiles: ${ALL_FAILED_ITEMS*.archiveProfile as Set}" )
        }
    }

    static void move503SlowDownFilesToSpecialFolder(){
        if(MISSED_OUT_USHERED_ITEMS){
            log.info("\n\nStarting moving 503 Slow Down Item(s)")
            MISSED_OUT_USHERED_ITEMS.eachWithIndex{ UsheredVO _missedOutItems, int counter ->
                ValidateUtil.moveFile(_missedOutItems,EGangotriUtil.CODE_503_SLOW_DOWN_FOLDER, "${counter+1}).")
            }
        }
    }

    static void startReuploadOfFailedItems() {
        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = ALL_FAILED_ITEMS.size()
        if(SettingsUtil.MOVE_FILES_DUE_TO_CODE_503_SLOW_DOWN){
            move503SlowDownFilesToSpecialFolder()
            return
        }
        if(SettingsUtil.ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Only stats generated. No Uploading due to Setting")
            return
        }
        if(!ALL_FAILED_ITEMS){
            ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = 0
            log.info("Rejoice !!! There is no Failed Item. So quitting")
            return
        }
        Set<String> profilesWithFailedLinks = ALL_FAILED_ITEMS*.archiveProfile as Set
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_LOGINS_PROPERTIES_FILE)
        Set<String> validProfiles = ArchiveUtil.filterInvalidProfiles(profilesWithFailedLinks, metaDataMap)
        executeReupload(validProfiles, metaDataMap)
    }

    static executeReupload(Set<String> profiles, Hashtable<String, String> metaDataMap){
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]

        ValidateUtil.validateMaxUploadableLimit()

        int attemptedItemsTotal = 0
        ArchiveUtil.storeAllUplodableItemsInFile(ALL_FAILED_ITEMS)

        profiles.eachWithIndex { archiveProfile, index ->
            Set<? extends UploadVO> failedVOsForProfile = ALL_FAILED_ITEMS.findAll {def vo -> vo.archiveProfile == archiveProfile } as Set<? extends UploadVO>
            int countOfUploadableItems = failedVOsForProfile.size()

            log.info "${index + 1}). Starting Validation upload(s) in archive.org for Profile $archiveProfile. Total Uplodables: ${countOfUploadableItems}/${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}"
            if (countOfUploadableItems) {
                List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(metaDataMap, failedVOsForProfile)
                String report = UploadUtils.generateStats(uploadStats, archiveProfile, countOfUploadableItems)
                uploadSuccessCheckingMatrix.put((index + 1), report)
                attemptedItemsTotal += countOfUploadableItems
            }
            else {
                log.info "No uploadable files for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }

        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, attemptedItemsTotal)
    }
}
