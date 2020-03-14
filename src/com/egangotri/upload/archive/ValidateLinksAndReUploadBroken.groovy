package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.util.ValidateLinksUtil
import com.egangotri.upload.vo.ItemsVO
import com.egangotri.upload.vo.LinksVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import static com.egangotri.upload.util.ArchiveUtil.storeQueuedItemsInFile

@Slf4j
class ValidateLinksAndReUploadBroken {
    static Set archiveProfiles = []
    static File identifierFile = null
    static File queuedFile = null
    static List<LinksVO> identifierLinksForTesting = []
    static List<ItemsVO> queuedItemsForTesting = []
    static List<LinksVO> failedLinks = []
    static List<ItemsVO> missedOutQueuedItems = []
    static List<? extends UploadVO> allFailedItems =  []


    static main(args) {
        EGangotriUtil.recordProgramStart("ValidateLinksInArchive")
        setCSVsForValidation(args)
        ArchiveUtil.ValidateLinksAndReUploadBrokenRunning = true
        SettingsUtil.applySettings()
        processUsheredCSV()
        processQueuedCSV()
        findQueueItemsNotInUsheredCSV()
        filterFailedUsheredItems()
        //generateFailedLinksFromStaticList()
        combineAllFailedItems()
        startReuploadOfFailedItems()
        System.exit(0)
    }

    static void setCSVsForValidation(def args) {
        identifierFile = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()
        queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()

        if (!identifierFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }

        if (!queuedFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }
        if (args) {
            println "args $args"
            if (args?.size() > 2) {
                log.error("Only 2 File Name(s) can be accepted.Cannot proceed. Quitting")
                System.exit(0)
            }
            String _file_1 = args.first().endsWith(".csv") ? args.first() : args.first() + ".csv"
            String _file_2 = args.last().endsWith(".csv") ? args.last() : args.last() + ".csv"
            identifierFile = new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER + File.separator + _file_1)
            queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER + File.separator + _file_2)
            if (!identifierFile) {
                log.error("No such File ${identifierFile} in ${EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
            if (!queuedFile) {
                log.error("No such File ${queuedFile} in ${EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }
        println("Identifier File for processing: ${identifierFile.name}")
        println("Queue File for processing: ${queuedFile.name}")
    }

    static void processUsheredCSV() {
        identifierLinksForTesting = ValidateLinksUtil.csvToUsheredItemsVO(identifierFile)
        archiveProfiles = identifierLinksForTesting*.archiveProfile as Set
        log.info("Converted " + identifierLinksForTesting.size() + " links of upload-ushered Item(s) from CSV in " + "Profiles ${archiveProfiles.toString()}")
    }

    //This static variable can only be used with generateFailedLinksFromStaticList()
    static  List<String> _staticListOfBadLinks = ["https://archive.org/details/kalidas_meghaduta_202003_406_"]

    /** This method is used in unique cases. where u have failed indentifiers only and you want to use them to reupload them only
     * // So u take the identifiers , use the ushered-csv to generate the complete VO Object.
     * Then upload the VOS
     */
    static void generateFailedLinksFromStaticList(){
        log.info("generating vos from static list of Links with size: " + _staticListOfBadLinks.size())

        identifierLinksForTesting.eachWithIndex{ LinksVO entry, int i ->
            if(_staticListOfBadLinks*.trim().contains(entry.archiveLink)){
                println("entry.uploadLink: " + entry.uploadLink)
                failedLinks << entry
            }
        }
    }

    static void processQueuedCSV() {
        queuedItemsForTesting = ValidateLinksUtil.csvToItemsVO(queuedFile)
        Set queuedProfiles = queuedItemsForTesting*.archiveProfile as Set
        log.info("Converted " + queuedItemsForTesting.size() + " Queued Item(s) from CSV in " + "Profiles ${queuedProfiles.toString()}")
    }

    // Thsi function produces QueuedItem - IdentifierGeneratedItem
    //Queued Item is a superset of IdentifierGeneratedItem
    static void findQueueItemsNotInUsheredCSV() {
        List allFilePaths = identifierLinksForTesting*.path
        log.info("Searching from ${queuedItemsForTesting?.size()} Queued Item(s) that were never upload-ushered in ${allFilePaths.size()} identifiers")

        queuedItemsForTesting.each { queuedItem ->
            if (!allFilePaths.contains(queuedItem.path)) {
                missedOutQueuedItems << queuedItem
                log.info("\tFound missing Item [${queuedItem.archiveProfile}] ${queuedItem.title} ")
            }
        }
        log.info("${missedOutQueuedItems.size()} Items found in Queued List that were missing. Affected Profies "  +  (missedOutQueuedItems*.archiveProfile as Set).toString())
    }

    static void filterFailedUsheredItems() {
        int testableLinksCount = identifierLinksForTesting.size()
        log.info("Testing ${testableLinksCount} Links in archive for upload-success-confirmation")

        identifierLinksForTesting.eachWithIndex { LinksVO entry, int i ->

            try {
                entry.archiveLink.toURL().text
                print("${i},")
            }
            catch (FileNotFoundException e) {
                failedLinks << entry
                println("Failed Link(${failedLinks.size()} of $testableLinksCount) !!! @ ${i}..")
            }
            catch (Exception e) {
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                failedLinks << entry
            }
            if(i%75 == 0){
                Thread.sleep(5000)
                println("")
            }
        }
        log.info("\n${failedLinks.size()} failedLink" + " Item(s) found in Ushered List that were missing." +
                " Affected Profie(s)" +  (failedLinks*.archiveProfile as Set).toString())
        log.info("Failed Links: " + failedLinks*.archiveLink.toString())
    }

    static void combineAllFailedItems(){
        if (missedOutQueuedItems || failedLinks) {
            allFailedItems = missedOutQueuedItems

            failedLinks.each { failedLink ->
                allFailedItems.add(failedLink)
            }
            log.info("Combined figure for re-uploading(${missedOutQueuedItems.size()} + ${failedLinks.size()}) :" + allFailedItems.size() + " in Profiles: ${allFailedItems*.archiveProfile as Set}" )
        }
    }

    static void startReuploadOfFailedItems() {
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Set<String> profilesWithFailedLinks = allFailedItems*.archiveProfile as Set
        Set<String> purgedProfilesWithFailedLinks = ArchiveUtil.purgeBrokenProfiles(profilesWithFailedLinks, metaDataMap)

        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = allFailedItems.size()
        if(ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION > EGangotriUtil.MAX_UPLODABLES){
            log.info("Uploadable Count ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} exceeds ${EGangotriUtil.MAX_UPLODABLES}. Canoot proceed. Quitting")
            System.exit(1)
        }
        log.info("Toal Uploadable Count for Current Execution ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}")

        int attemptedItemsTotal = 0

        purgedProfilesWithFailedLinks*.toString().eachWithIndex { archiveProfile, index ->
            List<UploadVO> failedItemsForProfile = allFailedItems.findAll { it.archiveProfile == archiveProfile }
            int countOfUploadableItems = failedItemsForProfile.size()
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile. Total Uplodables: ${countOfUploadableItems}"
            if (countOfUploadableItems) {
                log.info "getUploadablesForProfile: $archiveProfile: ${countOfUploadableItems}"
                storeQueuedItemsInFile(failedItemsForProfile)
                List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(metaDataMap, failedItemsForProfile)
                String report = UploadUtils.generateStats([uploadStats], archiveProfile, countOfUploadableItems)
                uploadSuccessCheckingMatrix.put((index + 1), report)
                attemptedItemsTotal += countOfUploadableItems
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }

        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, attemptedItemsTotal)
    }


}
