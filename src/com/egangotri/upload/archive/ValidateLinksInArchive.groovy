package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class ValidateLinksInArchive {
    static Set archiveProfiles = []
    static File latestIdentifierFile = null
    static List<UploadedLinksVO> linksForTesting = []
    static List<UploadedLinksVO> failedLinks = []

    static main(args) {
        log.info "Starting ValidateLinksInArchive @ " + UploadUtils.getFormattedDateString()
        latestIdentifierFile = new File( EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER ).listFiles()?.sort { -it.lastModified() }?.head()

        if(!latestIdentifierFile){
            log.error("No Files in ${EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            println "args $args"
            if(args?.size() != 1){
                log.error("Only 1 File Name can be accepted.Cannot proceed. Quitting")
                System.exit(0)
            }
            latestIdentifierFile = new File(EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER + File.separator + args.first())
            if(!latestIdentifierFile){
                log.error("No such File ${latestIdentifierFile} in ${EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }

        execute()
    }

    static boolean execute() {
        println("latestIdentifierFile ${latestIdentifierFile.name}")
        processCSV()
        filterFailedItems()
        if(failedLinks){
            startReuploadOfFailedItems()
        }
        return true
    }

    static boolean processCSV() {
        latestIdentifierFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect {appendDoubleQuotes(it.trim())}
            println (_fields.class)
            println (_fields)
            linksForTesting.add(new UploadedLinksVO(_fields.toList()))
        }
        archiveProfiles = linksForTesting*.archiveProfile as Set
        println(archiveProfiles)
    }

    static void filterFailedItems(){
        linksForTesting.eachWithIndex { UploadedLinksVO entry, int i ->
            try {
                entry.archiveLink.toURL().text
            }
            catch(FileNotFoundException e){
                println(e.message)
                e.printStackTrace()
                failedLinks << entry
            }
            catch(Exception e){
                log.error("This is an Unsual Error. ${entry.archiveLink} Check Manually" + e.message)
                e.printStackTrace()
                failedLinks << entry
            }
        }
        println("failedLinks*.archiveLink" + failedLinks*.archiveLink)
    }

    static void startReuploadOfFailedItems(){
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        List<List<Integer>> uploadStatsList = []
        Set<String> profilesWithFailedLinks = failedLinks*.archiveProfile as Set
        profilesWithFailedLinks*.toString().eachWithIndex { archiveProfile, index ->
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
                    List<Integer> uploadStats = ArchiveHandler.uploadAllLinksToArchiveByProfile(metaDataMap,
                            archiveProfile, true, failedLinks.findAll { it.archiveProfile == archiveProfile }
                    )
                    uploadStatsList << uploadStats
                    String report = UploadToArchive.generateStats(uploadStatsList, archiveProfile, countOfUploadablePdfs)
                    uploadSuccessCheckingMatrix.put((index + 1), report)
                }
            } else {
                log.info "No uploadable files for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }
        if (uploadSuccessCheckingMatrix) {
            log.info "Upload Report:\n"
            uploadSuccessCheckingMatrix.each { k, v ->
                log.info "$k) $v"
            }
            log.info "\n ***All Items put for upload implies all were attempted successfully for upload. But there can be errors still after attempted upload. best to check manually."
        }

        log.info "***End of ValidateLinksInArchive Program"
    }

    static String appendDoubleQuotes(String field)
    {
        if(!field.endsWith("\"")) {
            return field.concat("\"")
        }
        else {
            return field
        }
    }
}
