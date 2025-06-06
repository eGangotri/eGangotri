package com.egangotri.upload.archive


import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileSizeUtil
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
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
        Hashtable<String, String> metaDataMap = UploadUtils.getAllArchiveLogins()
        SettingsUtil.applySettings(false)
        Set<String> profilesForUpload = ArchiveUtil.filterInvalidProfiles(archiveProfiles, metaDataMap) as Set
        preview(profilesForUpload)
        System.exit(0)
    }

    static boolean preview(Set<String> profiles) {

        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = ArchiveUtil.getGrandTotalOfAllUploadables(profiles)
        ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB = ArchiveUtil.getGrandTotalOfFileSizeOfAllUploadables(profiles)
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} items")
        BigDecimal totalSize = ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB
        log.info("test only Files of Cumulative Size ${FileSizeUtil.formatFileSize(totalSize)}")
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} Files of Cumulative Size ${FileSizeUtil.formatFileSize(totalSize)}")

        statsForUploadables(profiles)

        log.info("This upload has following Unique Path Endings ${setOfEndings}")
        if (profileAndNames) {
            if (profileAndInvalidNames) {
                log.info("The Following files have names less than ${SettingsUtil.MINIMUM_FILE_NAME_LENGTH} characters")
                getOffendingFiles(profileAndInvalidNames)
                logOffendingFolders(setOfOffendingPaths)
                log.info("Cannot proceed because there are file names shorter than the Minimum Requirement Or Have More than ${MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME} Digits in file name.")
            } else {
                log.info("All texts are above minimum file length requirement")
                log.info("The Following are the files that will be uploaded")
                profileAndNames.eachWithIndex { Map.Entry<String, List<FileData>> entries, int index ->
                    log.info "${index + 1}). ${entries.key}"
                    entries.value.eachWithIndex { FileData entry, int counter ->
                        log.info("\t${counter + 1}). ${entry}")
                    }

                    long totalPagesInProfile = entries.value*.numberOfPagesInPdf.sum() as long
                    if (totalPagesInProfile > 0) {
                        log.info("\tTotal No. of Pages in Profile[pdf only](${entries.key}): ${totalPagesInProfile}")
                    }
                    log.info("\tTotal File Size in Profile(${entries.key}): ${FileSizeUtil.formatFileSize(entries.value*.sizeInKB.sum() as BigDecimal)}\n")
                }
                if (GRAND_TOTAL_OF_PDF_PAGES > 0) {
                    log.info("Total Count of Pages[pdf only]: " + GRAND_TOTAL_OF_PDF_PAGES)
                }
                log.info("Total Count of Items ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}")
                log.info(" Total File Size ${FileSizeUtil.formatFileSize(ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB)}")
            }
            return profileAndInvalidNames.size() == 0
        }
    }

    static boolean previewUsingVos(String profile, Set<QueuedVO> vos) {

        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = vos.size();
        ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB = ArchiveUtil.getGrandTotalOfFileSizeOfAllUploadableVOs(vos)
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} items")
        BigDecimal totalSize = ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB
        log.info("test only Files of Cumulative Size ${FileSizeUtil.formatFileSize(totalSize)}")
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} " +
                "Files of Cumulative Size ${FileSizeUtil.formatFileSize(totalSize)}")

        statsForUploadablesForVOs(profile,vos)

        log.info("This upload has following Unique Path Endings ${setOfEndings}")
        if (profileAndNames) {
            if (profileAndInvalidNames) {
                log.info("The Following files have names less than ${SettingsUtil.MINIMUM_FILE_NAME_LENGTH} characters")
                getOffendingFiles(profileAndInvalidNames)
                logOffendingFolders(setOfOffendingPaths)
                log.info("Cannot proceed because there are file names shorter than the Minimum Requirement Or Have More than ${MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME} Digits in file name.")
            } else {
                log.info("All texts are above minimum file length requirement")
                log.info("The Following are the files that will be uploaded")
                profileAndNames.eachWithIndex { Map.Entry<String, List<FileData>> entries, int index ->
                    log.info "${index + 1}). ${entries.key}"
                    entries.value.eachWithIndex { FileData entry, int counter ->
                        log.info("\t${counter + 1}). ${entry}")
                    }

                    long totalPagesInProfile = entries.value*.numberOfPagesInPdf.sum() as long
                    if (totalPagesInProfile > 0) {
                        log.info("\tTotal No. of Pages in Profile[pdf only](${entries.key}): ${totalPagesInProfile}")
                    }
                    log.info("\tTotal File Size in Profile(${entries.key}): ${FileSizeUtil.formatFileSize(entries.value*.sizeInKB.sum() as BigDecimal)}\n")
                }
                if (GRAND_TOTAL_OF_PDF_PAGES > 0) {
                    log.info("Total Count of Pages[pdf only]: " + GRAND_TOTAL_OF_PDF_PAGES)
                }
                log.info("Total Count of Items ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}")
                log.info(" Total File Size ${FileSizeUtil.formatFileSize(ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION_IN_MB)}")
            }
            return profileAndInvalidNames.size() == 0
        }
    }

    static void statsForUploadables(Set<String> profiles) {
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
                        shortNames << new FileData(entry)
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

    static void statsForUploadablesForVOs(String profile, Set<QueuedVO> vos) {
        if (vos) {
            List<FileData> shortNames = []
            List<FileData> names = []
            int totalCountOfPages = 0
            vos.each { QueuedVO entry ->
                FileData fileData = new FileData(entry.path)
                totalCountOfPages += fileData.numberOfPagesInPdf
                names << fileData
                setOfEndings << fileData.fileEnding
                if (fileData.title.length() < SettingsUtil.MINIMUM_FILE_NAME_LENGTH) {
                    setOfOffendingPaths << fileData.parentFolder
                    shortNames << new FileData(entry.path)
                }
            }
            GRAND_TOTAL_OF_PDF_PAGES += totalCountOfPages

            if (shortNames) {
                profileAndInvalidNames.put(profile, shortNames)
            }
            profileAndNames.put(profile, names)
        }

    }

    static void logOffendingFolders(List<String> setOfOffendingPaths) {
        log.info("This upload has following offending paths")
        (setOfOffendingPaths.groupBy { it }.sort { a, b -> b.value.size() <=> a.value.size() }).forEach { k, v ->
            log.info("$k\n : ${v.size()}")
        }

    }

    static void getOffendingFiles(Map<String, List<FileData>> profileAndInvalidNames) {
        profileAndInvalidNames.eachWithIndex { Map.Entry<String, List<FileData>> entry, int index ->
            log.info "${index + 1}). ${entry.key}"
            entry.value.eachWithIndex { FileData item, int i ->
                log.info("\t${i + 1}). ${item.toString()}")
            }
        }
    }
}

@Slf4j
class FileData {
    String title
    String absPath
    String parentFolder
    String fileEnding
    int numberOfPagesInPdf = 0
    BigDecimal sizeInKB = 0.0

    FileData(String entry) {
        this.absPath = entry
        this.fileEnding = UploadUtils.getFileEnding(this.absPath)
        this.title = UploadUtils.stripFilePath(this.absPath)
        this.parentFolder = UploadUtils.stripFileTitle(this.absPath)
        if (EGangotriUtil.PDF.endsWith(fileEnding)) {
            PdfReader pdfReader
            try {
             pdfReader = new PdfReader(this.absPath);
            log.info("Reading ${this.absPath}")
                PdfDocument pdfDoc = new PdfDocument(pdfReader);
                this.numberOfPagesInPdf = pdfDoc.getNumberOfPages()
            }
            catch(Exception e){
                log.error("Error reading ${this.absPath} ${e}");
                this.numberOfPagesInPdf = 0
            }
            finally {
                if (pdfReader) {
                    pdfReader.close()
                }
            }
        }
        sizeInKB = FileSizeUtil.fileSizeInKBForPath(this.absPath)
    }

    FileData(String _title, String _absPath) {
        title = _title
        absPath = _absPath
    }

    String toString() {
        return "${title}${this.numberOfPagesInPdf > 0 ? ' [' + this.numberOfPagesInPdf + ' Pages]' : ''} ${FileSizeUtil.formatFileSize(this.sizeInKB)} \n\t\t[${parentFolder}]"
    }
}

