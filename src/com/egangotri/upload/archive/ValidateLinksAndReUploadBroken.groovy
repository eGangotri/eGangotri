package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.UploadableLinksVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class ValidateLinksAndReUploadBroken {
    static Set archiveProfiles = []
    static File identifierFile = null
    static List<UploadableLinksVO> linksForTesting = []
    static List<UploadableLinksVO> failedItems = []

    static main(args) {
        log.info "Starting ValidateLinksInArchive @ " + UploadUtils.getFormattedDateString()
        setIdentifierFile(args)
        UploadUtils.ValidateLinksAndReUploadBrokenRunning = true
        SettingsUtil.applySettings()
        processCSV()
        filterFailedItems()
        if(failedItems){
            startReuploadOfFailedItems()
        }
        log.info "***End of ValidateLinksInArchive Program"
        System.exit(0)
    }

    static void setIdentifierFile(def args){
        identifierFile = new File( EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER ).listFiles()?.sort { -it.lastModified() }?.head()

        if(!identifierFile){
            log.error("No Files in ${EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }
        if (args) {
            println "args $args"
            if(args?.size() != 1){
                log.error("Only 1 File Name can be accepted.Cannot proceed. Quitting")
                System.exit(0)
            }
            identifierFile = new File(EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER + File.separator + args.first())
            if(!identifierFile){
                log.error("No such File ${identifierFile} in ${EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }
        println("latestIdentifierFile ${identifierFile.name}")
    }

    static boolean processCSV() {
        identifierFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect {stripDoubleQuotes(it.trim())}
            linksForTesting.add(new UploadableLinksVO(_fields.toList()))
        }
        archiveProfiles = linksForTesting*.archiveProfile as Set
        log.info("Checking " + linksForTesting.size() + " links in "  + "${archiveProfiles.toString()} Profiles"  )
    }

    static void filterFailedItems(){
        linksForTesting.eachWithIndex { UploadableLinksVO entry, int i ->
            try {
                entry.archiveLink.toURL().text
            }
            catch(FileNotFoundException e){
                failedItems << entry
            }
            catch(Exception e){
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                failedItems << entry
            }
        }
        log.info("Total ${failedItems.size()} failedLinks " + failedItems*.archiveLink.toString())
    }

    static void startReuploadOfFailedItems(){
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Set<String> profilesWithFailedLinks = failedItems*.archiveProfile as Set

        profilesWithFailedLinks*.toString().eachWithIndex { archiveProfile, index ->
            if (!UploadUtils.checkIfArchiveProfileHasValidUserName(metaDataMap, archiveProfile)) {
                return
            }
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile"
            List<UploadableLinksVO> failedItemsForProfile = failedItems.findAll { it.archiveProfile == archiveProfile }

            int countOfUploadablePdfs = failedItemsForProfile.size()
            if (countOfUploadablePdfs) {
                log.info "getUploadablesForProfile: $archiveProfile: ${countOfUploadablePdfs}"
                List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(metaDataMap,failedItemsForProfile)
                String report = UploadUtils.generateStats([uploadStats], archiveProfile, countOfUploadablePdfs)
                uploadSuccessCheckingMatrix.put((index + 1), report)
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }
        ArchiveUtil.printUplodReport(uploadSuccessCheckingMatrix)
    }

    static String stripDoubleQuotes(String field){
            return field.replaceAll("\"","")
    }
}
