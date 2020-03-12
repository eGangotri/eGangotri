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


    static main(args) {
        log.info "Starting ValidateLinksInArchive @ " + UploadUtils.getFormattedDateString()
        setCSVsForValidation(args)
        ArchiveUtil.ValidateLinksAndReUploadBrokenRunning = true
        SettingsUtil.applySettings()
        processUsheredCSV()
        processQueuedCSV()
        findQueueItemsNotInUsheredCSV()
        filterFailedItems()
        if (missedOutQueuedItems || failedLinks) {
            List<? extends UploadVO> allFailedItems =  missedOutQueuedItems
            failedLinks.each { failedLink ->
                allFailedItems.add(failedLink)
            }
            println(allFailedItems.size())
            println(allFailedItems*.archiveProfile)
            startReuploadOfFailedItems(allFailedItems)
        }

        log.info "***End of ValidateLinksInArchive Program"
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
        log.info("Checking " + identifierLinksForTesting.size() + " Identifier Generated links in " + "Profiles ${archiveProfiles.toString()}")
    }

    static void processQueuedCSV() {
        queuedItemsForTesting = ValidateLinksUtil.csvToItemsVO(queuedFile)
        Set queuedProfiles = queuedItemsForTesting*.archiveProfile as Set
        log.info("Checking " + queuedItemsForTesting.size() + " Queued Item(s) in " + "Profiles ${queuedProfiles.toString()}")
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

    static void filterFailedItems() {
        log.info("Testing ${identifierLinksForTesting.size()} Items with Generated Identifiers)")

        identifierLinksForTesting.eachWithIndex { LinksVO entry, int i ->
            try {
                entry.archiveLink.toURL().text
            }
            catch (FileNotFoundException e) {
                failedLinks << entry
            }
            catch (Exception e) {
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                failedLinks << entry
            }
        }
        log.info("${failedLinks.size()} failedLink" + " Item(s) found in Identifier Generated List that were missing." +
                " Affected Profie(s)" +  (failedLinks*.archiveProfile as Set).toString())
        log.info("Failed Links: " + failedLinks*.archiveLink.toString())
    }

    static void startReuploadOfFailedItems(List<UploadVO> reuploadableItems) {
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Set<String> profilesWithFailedLinks = reuploadableItems*.archiveProfile as Set

        profilesWithFailedLinks*.toString().eachWithIndex { archiveProfile, index ->
            if (!UploadUtils.checkIfArchiveProfileHasValidUserName(metaDataMap, archiveProfile)) {
                return
            }
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile"
            List<UploadVO> failedItemsForProfile = reuploadableItems.findAll { it.archiveProfile == archiveProfile }

            int countOfUploadablePdfs = failedItemsForProfile.size()
            if (countOfUploadablePdfs) {
                log.info "getUploadablesForProfile: $archiveProfile: ${countOfUploadablePdfs}"
                storeQueuedItemsInFile(failedItemsForProfile)
                List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(metaDataMap, failedItemsForProfile)
                String report = UploadUtils.generateStats([uploadStats], archiveProfile, countOfUploadablePdfs)
                uploadSuccessCheckingMatrix.put((index + 1), report)
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }
        ArchiveUtil.printUploadReport(uploadSuccessCheckingMatrix)
    }


}
