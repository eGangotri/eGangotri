package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.ItemsVO
import com.egangotri.upload.vo.LinksVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

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
        setIdentifierFile(args)
        ArchiveUtil.ValidateLinksAndReUploadBrokenRunning = true
        SettingsUtil.applySettings()
        processIdentifierCSV()
        processQueuedCSV()
        findQueueItemsNotInIdentifierCSV()
        filterFailedItems()
        if (missedOutQueuedItems) {
            startReuploadOfFailedItems(missedOutQueuedItems)
        }
        if (failedLinks) {
            startReuploadOfFailedItems(failedLinks)
        }
        log.info "***End of ValidateLinksInArchive Program"
        System.exit(0)
    }

    static void setIdentifierFile(def args) {
        identifierFile = new File(EGangotriUtil.ARCHIVE_GENERATED_IDENTIFIERS_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()
        queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER).listFiles()?.sort { -it.lastModified() }?.head()

        if (!identifierFile) {
            log.error("No Files in ${EGangotriUtil.ARCHIVE_GENERATED_IDENTIFIERS_FOLDER}.Cannot proceed. Quitting")
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
            String _file_2 = args[1].endsWith(".csv") ? args[1] : args[1] + ".csv"
            identifierFile = new File(EGangotriUtil.ARCHIVE_GENERATED_IDENTIFIERS_FOLDER + File.separator + _file_1)
            queuedFile = new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER + File.separator + _file_1)
            if (!identifierFile) {
                log.error("No such File ${identifierFile} in ${EGangotriUtil.ARCHIVE_GENERATED_IDENTIFIERS_FOLDER}.Cannot proceed. Quitting")
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

    static void processIdentifierCSV() {
        identifierFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            identifierLinksForTesting.add(new LinksVO(_fields.toList()))
        }
        archiveProfiles = identifierLinksForTesting*.archiveProfile as Set
        log.info("Checking " + identifierLinksForTesting.size() + " links in " + "${archiveProfiles.toString()} Profiles")
    }

    static void processQueuedCSV() {
        queuedFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            queuedItemsForTesting.add(new ItemsVO(_fields.toList()))
        }
        Set queuedProfiles = queuedItemsForTesting*.archiveProfile as Set
        log.info("Checking " + queuedItemsForTesting.size() + " links in " + "${queuedProfiles.toString()} Profiles")
    }

    // Thsi function produces QueuedItem - IdentifierGeneratedItem
    //Queued Item is a superset of IdentifierGeneratedItem
    static void findQueueItemsNotInIdentifierCSV() {
        List allFilePaths = identifierLinksForTesting*.fullFilePath
        log.info("Found ${allFilePaths.size()} Items in identifier file")

        queuedItemsForTesting.each { queuedItem ->
            if (!allFilePaths.contains(queuedItem.fullFilePath)) {
                missedOutQueuedItems << queuedItem
                log.info("Found missed Item ${queuedItem.fileTitle} ")

            }
        }
        log.info("Found ${missedOutQueuedItems.size()} Items from Queued List that were missed")
    }

    static void filterFailedItems() {
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
        log.info("Total ${failedLinks.size()} failedLinks " + failedLinks*.archiveLink.toString())
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
                List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(metaDataMap, failedItemsForProfile)
                String report = UploadUtils.generateStats([uploadStats], archiveProfile, countOfUploadablePdfs)
                uploadSuccessCheckingMatrix.put((index + 1), report)
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }
        ArchiveUtil.printUplodReport(uploadSuccessCheckingMatrix)
    }

    static String stripDoubleQuotes(String field) {
        return field.replaceAll("\"", "")
    }
}
