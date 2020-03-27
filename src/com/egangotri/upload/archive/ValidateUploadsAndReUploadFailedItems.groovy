package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.util.ValidateUtil
import com.egangotri.upload.vo.ItemsVO
import com.egangotri.upload.vo.LinksVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import static com.egangotri.upload.util.ArchiveUtil.storeQueuedItemsInFile

@Slf4j
class ValidateUploadsAndReUploadFailedItems {
    static Set archiveProfiles = []
    static File usheredFile = null
    static File queuedFile = null
    static List<LinksVO> identifierLinksForTesting = []
    static List<ItemsVO> queuedItemsForTesting = []
    static List<LinksVO> missedOutUsheredItems = []
    static List<ItemsVO> missedOutQueuedItems = []
    static List<? extends UploadVO> allFailedItems =  []
    static List<LinksVO> itemsWith400BadData =  []


    static main(args) {
        SettingsUtil.applySettingsWithReuploaderFlags()
        execute(args)
        System.exit(0)
    }

    static void execute(def args = [] ){
        setCSVsForValidation(args)
        UploadUtils.resetGlobalUploadCounter()
        processUsheredCSV()
        processQueuedCSV()
        findQueueItemsNotInUsheredCSV()
        filterFailedUsheredItems()
        //(generateFailedLinksFromStaticList) for use in special cases only
        //generateFailedLinksFromStaticList()
        combineAllFailedItems()
        startReuploadOfFailedItems()
    }

    static void findMissedQueueItemsOnlyAndReupload(boolean reupload = true){
        SettingsUtil.applySettingsWithReuploaderFlags([false,true,!reupload])
        execute()
    }

    static void findMissedUsheredItemsOnlyAndReupload(boolean reupload = false){
        SettingsUtil.applySettingsWithReuploaderFlags([true,false,reupload])
        if(SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS) {
            missedOutQueuedItems = []
        }
        execute()
    }

    static void processUsheredCSV() {
        identifierLinksForTesting = ValidateUtil.csvToUsheredItemsVO(usheredFile)
        archiveProfiles = identifierLinksForTesting*.archiveProfile as Set
        log.info("Converted " + identifierLinksForTesting.size() + " links of upload-ushered Item(s) from CSV in " + "Profiles ${archiveProfiles.toString()}")
    }

    static void processQueuedCSV() {
        queuedItemsForTesting = ValidateUtil.csvToItemsVO(queuedFile)
        Set queuedProfiles = queuedItemsForTesting*.archiveProfile as Set
        log.info("Converted " + queuedItemsForTesting.size() + " Queued Item(s) from CSV in " + "Profiles ${queuedProfiles.toString()}")
    }

    static void setCSVsForValidation(def args) {
        usheredFile = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()
        queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()

        if (!usheredFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }

        if (!queuedFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
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
            usheredFile = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER + File.separator + _file_1)
            queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER + File.separator + _file_2)
            if (!usheredFile) {
                log.error("No such File ${usheredFile} in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
            if (!queuedFile) {
                log.error("No such File ${queuedFile} in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }
        log.info("Identifier File for processing: ${usheredFile.name}")
        log.info("Queue File for processing: ${queuedFile.name}")
    }

    // Thsi function produces QueuedItem - IdentifierGeneratedItem
    //Queued Item is a superset of IdentifierGeneratedItem
    static void findQueueItemsNotInUsheredCSV() {
        if(SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Queued Items will be ignored for upload")
            return
        }
        List allFilePaths = identifierLinksForTesting*.path
        log.info("Searching from ${queuedItemsForTesting?.size()} Queued Item(s) that were never upload-ushered in ${allFilePaths.size()} identifiers")

        queuedItemsForTesting.eachWithIndex { queuedItem, index ->
            if (!allFilePaths.contains(queuedItem.path)) {
                missedOutQueuedItems << queuedItem
                log.info("\tFound missing Item [ (# $index). ${queuedItem.archiveProfile}] ${queuedItem.title} ")
            }
        }
        log.info("${missedOutQueuedItems.size()}/${queuedItemsForTesting.size()} Items found in Queued List that missed upload. Affected Profies "  +  (missedOutQueuedItems*.archiveProfile as Set).toString())
    }

    static void filterFailedUsheredItems() {
        if(SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS){
            missedOutUsheredItems = []
            log.info("Ushered Items will be ignored for upload")
            return
        }
        int testableLinksCount = identifierLinksForTesting.size()
        log.info("Testing ${testableLinksCount} Links in archive for upload-success-confirmation")

        identifierLinksForTesting.eachWithIndex { LinksVO entry, int i ->
            String urlText = ""
            try {
                urlText = entry.archiveLink.toURL().text
                int checkDownloadOptions = urlText.count("format-group")
                if(checkDownloadOptions < 2){
                    itemsWith400BadData << entry
                    log.info("\nCode 404 Bad Data File: \"${entry.archiveLink}\" Counter # ${i}..")
                }
                print("${i},")
            }
            catch (FileNotFoundException e) {
                missedOutUsheredItems << entry
                log.info("\nFailed Link: \"${entry.archiveLink}\"(${missedOutUsheredItems.size()} of $testableLinksCount) !!! @ ${i}..")
            }
            catch (Exception e) {
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                missedOutUsheredItems << entry
            }
            if(i%35 == 0){
                //Thread.sleep(5000)
                System.gc()
                log.info("")
            }
        }
        log.info("\n${missedOutUsheredItems.size()} failedLink" + " Item(s) found in Ushered List that were missing." +
                " Affected Profie(s)" +  (missedOutUsheredItems*.archiveProfile as Set).toString())
        log.info("Failed Links: " + (missedOutUsheredItems*.archiveLink.collect{ _link-> "'" + _link + "'"}))
        log.info("Found ${itemsWith400BadData?.size()} Code 400 Bad Data Files: (repair with pdftk and reupload manually)\n"
                + (itemsWith400BadData*.path.collect{ _path-> "'" + _path + "'\n"}))
        copy400BadDataFilesToSpecialFolder()
    }


    //This static variable can only be used with generateFailedLinksFromStaticList()
    static  List<String> _staticListOfBadLinks =['https://archive.org/details/weorournationhooddefinedshrim.s_a']

    /** This method is used in unique cases.
     *  Where u have a list of failed Archive Urls and you want to use them to reupload them only
     * So u take the links copy paste to _staticListOfBadLinks ,
     * have following settings:
     * IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=true
     * IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=false
     * ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS=false
     *generating vos
     * comment out call to filterFailedUsheredItems()
     * uncomment call to generateFailedLinksFromStaticList() and execute the program
     *
     * .
     * Then upload the VOS
     */
    static void generateFailedLinksFromStaticList(){
        log.info("generating vos from static list of Links with size: " + _staticListOfBadLinks.size())
        SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=true
        SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=false
        SettingsUtil.ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS=false

        identifierLinksForTesting.eachWithIndex{ LinksVO entry, int i ->
            if(_staticListOfBadLinks*.trim().contains(entry.archiveLink)){
                log.info("entry.uploadLink: " + entry.uploadLink)
                missedOutUsheredItems << entry
            }
        }
    }

    static void combineAllFailedItems(){
        if (missedOutQueuedItems || missedOutUsheredItems) {
            allFailedItems.addAll(missedOutQueuedItems)

            missedOutUsheredItems.each { failedLink ->
                allFailedItems.add(failedLink)
            }
            log.info("Combined figure for re-uploading(${missedOutQueuedItems.size()} + ${missedOutUsheredItems.size()}) :" + allFailedItems.size() + " in Profiles: ${allFailedItems*.archiveProfile as Set}" )
        }
    }

    static void copy400BadDataFilesToSpecialFolder(){
        ArchiveUtil.generateFolder(EGangotriUtil.CODE_404_BAD_DATA_FOLDER)
        if(itemsWith400BadData){
            itemsWith400BadData.forEach{ LinksVO code400BadDataItem ->
                try {
                    Files.copy(new File(code400BadDataItem.path).toPath(),
                            new File(EGangotriUtil.CODE_404_BAD_DATA_FOLDER +  File.separator + code400BadDataItem.title).toPath())
                    log.info("copying ${code400BadDataItem.path} to ${EGangotriUtil.CODE_404_BAD_DATA_FOLDER}")
                }
                catch(Exception e){
                    log.error("Error copying ${code400BadDataItem.path} ${e.message}")
                    e.printStackTrace()
                }

            }
        }
    }
    static void startReuploadOfFailedItems() {
        if(SettingsUtil.ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS){
            log.info("Only stats generated. No Uploading due to Setting")
            return
        }
        Set<String> profilesWithFailedLinks = allFailedItems*.archiveProfile as Set
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Set<String> validProfiles = ArchiveUtil.filterInvalidProfiles(profilesWithFailedLinks, metaDataMap)
        _execute(validProfiles, metaDataMap)
    }

    static _execute(Set<String> profiles, Hashtable<String, String> metaDataMap){
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        EGangotriUtil.recordProgramStart("ValidateUploadsAndReUploadFailedItems")

        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = allFailedItems.size()
        ValidateUtil.validateMaxUploadableLimit()

        int attemptedItemsTotal = 0

        profiles.eachWithIndex { archiveProfile, index ->
            List<UploadVO> failedItemsForProfile = allFailedItems.findAll { it.archiveProfile == archiveProfile }
            int countOfUploadableItems = failedItemsForProfile.size()
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile. Total Uplodables: ${countOfUploadableItems}/${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}"
            if (countOfUploadableItems) {
                storeQueuedItemsInFile(failedItemsForProfile)
                List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(metaDataMap, failedItemsForProfile)
                String report = UploadUtils.generateStats([uploadStats], archiveProfile, countOfUploadableItems)
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
