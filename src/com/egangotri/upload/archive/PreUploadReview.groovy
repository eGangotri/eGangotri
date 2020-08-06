package com.egangotri.upload.archive

import com.egangotri.pdf.BookTitles
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

@Slf4j
class PreUploadReview {
    static int MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME = 6
    static Set<String> setOfEndings = [] as Set
    static List<String> setOfOffendingPaths = []
    static Map<String, List<FileData>> profileAndInvalidNames = [:]
    static Map<String, List<FileData>> profileAndNames = [:]
    static int GRAND_TOTAL_OF_PDF_PAGES = 0

    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        SettingsUtil.applySettings(false)
        Set<String> purgedProfiles = ArchiveUtil.filterInvalidProfiles(archiveProfiles, metaDataMap) as Set
        preview(purgedProfiles)
        System.exit(0)
    }

    static boolean preview(Set<String> profiles) {

        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = ArchiveUtil.getGrandTotalOfAllUploadables(profiles)
        ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB = ArchiveUtil.getGrandTotalOfFileSizeOfAllUploadables(profiles)
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} items")
        BigDecimal sizeInMB = ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} Files of Cumulative Size ${sizeInfo(sizeInMB)}")

        statsForUploadables(profiles)

        log.info("This upload has following Unique Path Endings ${setOfEndings}")
        if (profileAndNames) {
            if (profileAndInvalidNames) {
                log.info("The Following files have names less than ${SettingsUtil.MINIMUM_FILE_NAME_LENGTH} characters")
                getOffendingFiles(profileAndInvalidNames)
                logOffendingFolders(setOfOffendingPaths)
                log.info("Cannot proceed because there are file names shorter than the Minimum Requirement Or Have More than ${MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME} Digits in file name.")
            }
            else{
                log.info("All texts are above minimum file length requirement")
                log.info("The Following are the files that will be uploaded")
                profileAndNames.eachWithIndex { Map.Entry<String, List<FileData>> entry, int index ->
                    log.info "${index + 1}). ${entry.key}"
                    log.info("\t${entry.value.join("\n\t")}")
                    long totalPagesInProfile = entry.value*.numberOfPagesInPdf.sum() as long
                    if(totalPagesInProfile > 0){
                        log.info("\tTotal No. of Pages in Profile[pdf only](${entry.key}): ${totalPagesInProfile}")
                    }
                    log.info("\tTotal File Size in Profile(${entry.key}): ${sizeInfo(entry.value*.sizeInMB.sum() as BigDecimal)}\n")
                }
                if(GRAND_TOTAL_OF_PDF_PAGES > 0){
                    log.info("Total Count of Pages[pdf only]: " + GRAND_TOTAL_OF_PDF_PAGES)
                }
            }
            return profileAndInvalidNames.size() == 0
        }
    }

    static void statsForUploadables(Set<String> profiles){
        profiles.eachWithIndex { archiveProfile, index ->
            List<String> uploadablesForProfile = FileRetrieverUtil.getUploadablesForProfile(archiveProfile)
            if (uploadablesForProfile) {
                List<FileData> shortNames = []
                List<FileData> names = []
                int totalCountOfPages = 0
                uploadablesForProfile.each { String entry ->
                    FileData fileData = new FileData(entry)
                    totalCountOfPages += fileData.numberOfPagesInPdf
                    names << fileData
                    setOfEndings << fileData.fileEnding
                    if (fileData.title.length() < SettingsUtil.MINIMUM_FILE_NAME_LENGTH) {
                        setOfOffendingPaths << fileData.parentFolder
                        shortNames << new FileData(fileData.title, entry)
                    }
                }
                GRAND_TOTAL_OF_PDF_PAGES += totalCountOfPages

                if (shortNames) {
                    profileAndInvalidNames.put(archiveProfile, shortNames)
                }
                profileAndNames.put(archiveProfile, names)
            }
        }

    }
    static void logOffendingFolders(List<String> setOfOffendingPaths){
        log.info("This upload has following offending paths")
        (setOfOffendingPaths.groupBy {it}.sort { a, b -> b.value.size() <=> a.value.size() }).forEach{k,v ->
            log.info("$k\n : ${v.size()}")
        }

    }
    static void getOffendingFiles(Map<String, List<FileData>> profileAndInvalidNames){
        profileAndInvalidNames.eachWithIndex { Map.Entry<String, List<FileData>> entry, int index ->
            log.info "${index + 1}). ${entry.key}"
            entry.value.eachWithIndex{ FileData item, int i ->
                log.info("\t${i+1}). ${item.toString()}")
            }
        }
    }
    static boolean fileNameHasOverAllowedDigits(String fileName) {
        return fileName.findAll( /\d+/ ).join("").size() > MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME
    }

    static String sizeInfo(BigDecimal sizeInMB){
        BigDecimal sizeInGB = sizeInMB/1024
        return "${sizeInMB.round(2)} MB (== ${sizeInGB.round(2)} GB)"
    }
}

class FileData {
    String title
    String absPath
    String parentFolder
    String fileEnding
    int numberOfPagesInPdf = 0
    BigDecimal sizeInMB = 0

    FileData(String entry){
        this.absPath = entry
        this.fileEnding = UploadUtils.getFileEnding(this.absPath)
        this.title = UploadUtils.stripFilePath(this.absPath)
        this.parentFolder = UploadUtils.stripFileTitle(this.absPath)
        if(EGangotriUtil.PDF.endsWith(fileEnding)){
            PdfReader pdfReader = new PdfReader(this.absPath)
            this.numberOfPagesInPdf = pdfReader.getNumberOfPages()
        }
        sizeInMB = (new File(this.absPath).size()/ (1024 * 1024)) as BigDecimal
    }

    FileData(String _title, String _absPath){
        title = _title
        absPath = _absPath
    }
    String toString(){
        //striptitle, "$entry "
        return "${title}${this.numberOfPagesInPdf > 0 ? '[' + this.numberOfPagesInPdf + ' Pages]':''} ${PreUploadReview.sizeInfo(this.sizeInMB)} MB [${parentFolder} ]"
    }


}

