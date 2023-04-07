package com.egangotri.pdf

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * All Titles of PDF's in a Folder and SubFolders
 */
@Slf4j
class FileNameLongerThanThreshold {

    static List<String> FOLDER_NAME = ["C:\\tmp\\pdfForMergeTest\\"]
    static int THRESHOLD = 240
    static int PATH_THRESHOLD = 2000

    static int START_INDEX = 0
    static int TOTAL_FILES = 0
    static int TOTAL_FILES_WITH_FILE_NAME_LENGTH_THRESHOLD_VIOLATION = 0
    static int TOTAL_FILES_WITH_PATH_NAME_LENGTH_THRESHOLD_VIOLATION = 0
    static int DUAL_VIOLATIONS = 0


    static List ignoreList = []
    static String PDF = "pdf"

    static StringBuilder MEGA_REPORT = new StringBuilder("")

    static boolean DONT_MENTION_SUB_FOLDERS = false;
    static boolean INCLUDE_NUMBER_OF_PAGES = true
    static boolean INCLUDE_INDEX = true
    static boolean ONLY_ROOT_DIR_NO_SUBDIRS = false
    static boolean ONLY_PDFS = false

    static void main(String[] args) {
        execute(args)
    }

    static void execute(String[] args = []) {
        if (args?.size() > 0) {
            String args0 = args[0]
            FOLDER_NAME = args0.split(",")*.trim().toList()
            if (args?.size() > 1) {
                THRESHOLD = args[1].toInteger()
            }
            if (args?.size() > 2) {
                PATH_THRESHOLD = args[2].toInteger()
            }
        }

        addToReportAndPrint("Reading files: $FOLDER_NAME\n")
        for (String folder : FOLDER_NAME) {
            //if only the directory specified
            if (FileNameLongerThanThreshold.ONLY_ROOT_DIR_NO_SUBDIRS) {
                processOneFolder(folder)
            } else {
                //if everything
                procAdInfinitum(folder)
            }
        }

        int totalFilesWithViolations =
                TOTAL_FILES_WITH_FILE_NAME_LENGTH_THRESHOLD_VIOLATION +
                        TOTAL_FILES_WITH_PATH_NAME_LENGTH_THRESHOLD_VIOLATION - DUAL_VIOLATIONS
        String totalStats = """Total No. of Files with Violations: ${formatInteger(totalFilesWithViolations)}
                                TOTAL_FILES_WITH_FILE_NAME_LENGTH_THRESHOLD_VIOLATION: ${TOTAL_FILES_WITH_FILE_NAME_LENGTH_THRESHOLD_VIOLATION}
                                TOTAL_FILES_WITH_PATH_NAME_LENGTH_THRESHOLD_VIOLATION: ${TOTAL_FILES_WITH_PATH_NAME_LENGTH_THRESHOLD_VIOLATION}
                                DUAL_VIOLATIONS: ${DUAL_VIOLATIONS}
                                    """;
        addToReportAndPrint(totalStats)
        writeToFile()
    }

    static void writeToFile() {
        String _folderNames = (FOLDER_NAME.collect { return new File(it) })*.name.join("_")
        String fileName = _folderNames + "_ThresholdExceedsList_" +(ONLY_PDFS? "pdfs_only" : "all")
        File writeableFile = new File(System.getProperty("user.home"), "${fileName}_${new Date().time}.txt")
        writeableFile << MEGA_REPORT
        log.info("written to file: ${writeableFile.getAbsolutePath()} ")
    }

    static void addToReportAndPrint(String _report, onlyLogDontCommitToFile = false) {
        log.info(_report)
        if (!onlyLogDontCommitToFile) {
            MEGA_REPORT.append("$_report\n")
        };
    }

    static String formatInteger(Integer _formattable) {
        def pattern = "##,##,##,###"
        def moneyform = new DecimalFormat(pattern)
        return moneyform.format(_formattable.toLong())
    }

    static void processOneFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        String readingFolder = "\nReading Folder ${directory}"
        //addToReportAndPrint(readingFolder, DONT_MENTION_SUB_FOLDERS)

        for (File file : directory.listFiles()) {
            long createDateAsLong = 0

            if (!inIgnoreList(file)) {
                try {
                    if (!ONLY_PDFS || (ONLY_PDFS && file.name.endsWith(PDF))) {
                        printFileName(file)
                    }
                }
                catch (Exception e) {
                    log.info("Error reading file. will continue" + e)
                }
            }
        }
    }

    static boolean inIgnoreList(File file) {
        String absPath = file.absolutePath.toString()
        def invalid = ignoreList.findAll { ignorableKeyword ->
            absPath.containsIgnoreCase(ignorableKeyword)
        }
        return invalid?.size() > 0
    }
    /**
     * if you have one folder and you want it to go one level deep to process multiple foldrs within
     */

    /**
     * Recursive Method
     * @param folderAbsolutePath
     */

    static void procAdInfinitum(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)

        //Process Root Folder
        processOneFolder(folderAbsolutePath)

        //Then get in Sub-directories and process them
        for (File subDirectory : directory.listFiles()) {
            if (subDirectory.isDirectory() && !inIgnoreList(subDirectory)) {
                procAdInfinitum(subDirectory.absolutePath)
            }
        }
    }

    static void printFileName(File file) {
        int fileLenth = file.name.length()
        int filePathLength = file.path.length()
        if (fileLenth > THRESHOLD || filePathLength > PATH_THRESHOLD) {
        if (fileLenth > THRESHOLD){
            TOTAL_FILES_WITH_FILE_NAME_LENGTH_THRESHOLD_VIOLATION++
        }
        if (filePathLength > PATH_THRESHOLD){
           TOTAL_FILES_WITH_PATH_NAME_LENGTH_THRESHOLD_VIOLATION++
        }
         if (fileLenth > THRESHOLD && filePathLength > PATH_THRESHOLD) {
             DUAL_VIOLATIONS++
         }
        String _report = """${INCLUDE_INDEX ? ++START_INDEX + ').' : ''} ${file.name} 
                ${file.path} 
                has length ${fileLenth}
                and path length ${filePathLength}"""
        addToReportAndPrint(_report)
        }
    }
}
