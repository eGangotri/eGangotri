package com.egangotri.pdf

import com.egangotri.util.FileSizeUtil
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
class BookTitles {

    static List<String> FOLDER_NAME = ["C:\\tmp\\pdfForMergeTest\\"]
    static String afterDate = "" //format DD-MM-YYYY
    static int afterHour = 0 //format DD-MM-YYYY
    static long afterDateAsLong = 0
    static int START_INDEX = 0
    static int TOTAL_FILES_SUCCCESSFULLY_READ = 0
    static int TOTAL_ERRORS = 0
    static List<String> ERRORENOUS_FILES = []
    static int TOTAL_NUM_PAGES = 0

    static List ignoreList = ["C:\\Treasures48\\_freeze\\jngm_kan\\retry_dont"]
    static String PDF = "pdf"

    static StringBuilder MEGA_REPORT = new StringBuilder("")

    static boolean DONT_MENTION_SUB_FOLDERS = false;
    static boolean INCLUDE_NUMBER_OF_PAGES = true
    static boolean INCLUDE_FILE_SIZE = true
    static boolean INCLUDE_INDEX = true
    static boolean ONLY_ROOT_DIR_NO_SUBDIRS = false
    static boolean ONLY_PDFS = true

    static void main(String[] args) {
        execute(args)
    }

    static void execute(String[] args = []) {
        if (args?.size() > 0) {
            String args0 = args[0]
            FOLDER_NAME = args0.split(",")*.trim().toList()
            if (args?.size() > 1) {
                afterDate = args[1]
                if (args?.size() > 2) {
                    if (args[2].isInteger()) {
                        afterHour = Integer.parseInt(args[2])
                        if (afterHour >= 24 || afterHour < 1) {
                            afterHour = 0
                        }
                    }
                }
            }
        }
        if (afterDate) {
            if (afterDate.toLowerCase().startsWith("today")) {
                Calendar date = new GregorianCalendar();
                if (afterDate.contains("-")) {
                    try {
                        int diff = -1 * Integer.parseInt(afterDate.split("-")[1].trim())
                        date.add(Calendar.DAY_OF_YEAR, diff)
                    }
                    catch (Exception e) {
                        log.error("parse error", e)
                    }
                }
                date.set(Calendar.HOUR_OF_DAY, afterHour);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                afterDate = new SimpleDateFormat("dd-MMM-yy").format(date.time)
                afterDateAsLong = date.time.time
            } else {
                afterDateAsLong = new SimpleDateFormat("dd-MM-yyyy").parse(afterDate).getTime() + (afterHour * 60 * 60 * 1000)
            }
        }
        addToReportAndPrint("Reading files: $FOLDER_NAME\n")
        for (String folder : FOLDER_NAME) {
            //if only the directory specified
            if (BookTitles.ONLY_ROOT_DIR_NO_SUBDIRS) {
                processOneFolder(folder)
            } else {
                //if everything
                procAdInfinitum(folder)
            }
        }
        String totalStats = """Total Files Processed: ${formatInteger(TOTAL_FILES_SUCCCESSFULLY_READ+TOTAL_ERRORS)}
Total Files Read Successfully: ${formatInteger(TOTAL_FILES_SUCCCESSFULLY_READ)}
Total Files with Errors: ${formatInteger(TOTAL_ERRORS)}
Total Pages: ${formatInteger(TOTAL_NUM_PAGES)}"""

        addToReportAndPrint(totalStats)
        writeToFile()
    }

    static void writeToFile() {
        String _folderNames = (FOLDER_NAME.collect { return new File(it) })*.name.join("_")
        String fileName = _folderNames + "_MegaList_" +(ONLY_PDFS? "pdfs_only" : "all")
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
        String readingFolder = "\nReading Folder ${directory}" + (afterDateAsLong ? " for Files created after ${afterDate}" : '') + (afterHour > 0 ? " ${afterHour}:00 Hours" : '');
        addToReportAndPrint(readingFolder, DONT_MENTION_SUB_FOLDERS)

        if (TOTAL_NUM_PAGES > 0) {
            log.info("Already read ${formatInteger(TOTAL_NUM_PAGES)} pages in ${formatInteger(TOTAL_FILES_SUCCCESSFULLY_READ)} files")
        }
        for (File file : directory.listFiles()) {
            long createDateAsLong = 0

            if (afterDateAsLong) {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                        BasicFileAttributes.class)
                createDateAsLong = attr.creationTime().toMillis()
            }

            if (!file.isDirectory() && !inIgnoreList(file)
                    && (!afterDateAsLong || (createDateAsLong > afterDateAsLong))) {
                try {
                    if (!ONLY_PDFS || (ONLY_PDFS && file.name.endsWith(PDF))) {
                        printFileNamePageCountFileSize(folderAbsolutePath, file, ++START_INDEX)
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

    static void printFileNamePageCountFileSize(String folderAbsolutePath, File file, int index) {
        int numberOfPages = 0
        try{

        if (INCLUDE_NUMBER_OF_PAGES && file.name.endsWith(PDF)) {
               PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)
               PdfDocument pdfDoc = new PdfDocument(pdfReader);
               numberOfPages = pdfDoc.getNumberOfPages()
               incrementTotalPageCount(numberOfPages)
        }

        String sizeInfo = FileSizeUtil.getFileSizeFormatted(file)
        String pageCountLogic = "${INCLUDE_NUMBER_OF_PAGES && file.name.endsWith(PDF) ? ', ' + numberOfPages + ' Pages' : ''}"
        String fileSizeLogic = "${INCLUDE_FILE_SIZE && file.name.endsWith(PDF) ? ', ' + sizeInfo : ''}"
        String _report = "${INCLUDE_INDEX ? index + ').' : ''} ${file.name} ${pageCountLogic} ${fileSizeLogic}";
        addToReportAndPrint(_report)
        incrementFileCount()
        }
        catch(Exception e){
            addToErrors(file.absolutePath)
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} ${file.name} had an error reading page/count/size";
            addToReportAndPrint(_report)
            log.error("Error in reading file page-count/size/ for ${file.absolutePath}",e)
        }
    }

    static void incrementFileCount() {
        TOTAL_FILES_SUCCCESSFULLY_READ++
    }

    static void addToErrors(String filePath) {
        TOTAL_ERRORS++
        ERRORENOUS_FILES << filePath

    }
    static void incrementTotalPageCount(int numPagesToIncrement) {
        TOTAL_NUM_PAGES += numPagesToIncrement
    }

}
