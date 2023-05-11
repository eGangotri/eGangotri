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

    static boolean generateCSVAlso = true
    static List<String> FOLDER_NAME = ["C:\\tmp\\pdfForMergeTest\\"]
    static String afterDate = "" //format DD-MM-YYYY
    static int afterHour = 0 //format DD-MM-YYYY
    static long afterDateAsLong = 0
    static int START_INDEX = 0
    static int TOTAL_FILES = 0
    static int TOTAL_FILES_SUCCESSFULLY_READ = 0
    static int TOTAL_ERRORS = 0
    static List<String> ERRORENOUS_FILES = []
    static List<String> PASSWORD_PROTECTED_FILES = []
    static int TOTAL_NUM_PAGES = 0

    static List ignoreList = ["C:\\Treasures48\\_freeze\\jngm_kan\\retry_dont"]
    static String PDF = "pdf"

    static StringBuilder MEGA_REPORT = new StringBuilder("")
    static StringBuilder CSV_MEGA_REPORT = new StringBuilder("")

    static boolean DONT_MENTION_SUB_FOLDERS = false;
    static boolean INCLUDE_NUMBER_OF_PAGES = true
    static boolean INCLUDE_FILE_SIZE = true
    static boolean INCLUDE_INDEX = true
    static boolean ONLY_ROOT_DIR_NO_SUBDIRS = false
    static boolean ONLY_PDFS = true

    static String CSV_SEPARATOR = ";"
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
        calculateAfterDate()
        TOTAL_FILES = calculateTotalFileCount()
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
        printFinalStats()
        writeToFile()
    }

    static void calculateAfterDate() {
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

    }

    static String generateCsvHeader() {
        if (generateCSVAlso) {
            //Separator has to be specified on TOP as SEMI-COLON instead of COMMMA
            // as the data has comas all the time
            String  separatorSpecification = "sep=;\n"
            ///
            String pageCountHeader = "${INCLUDE_NUMBER_OF_PAGES ? "${CSV_SEPARATOR}Number of Pages" : ''}"
            String fileSizeHeader = "${INCLUDE_FILE_SIZE ? "${CSV_SEPARATOR}File Size${CSV_SEPARATOR} Units" : ''}"
            String csvReport = "${separatorSpecification}${INCLUDE_INDEX ? "Serial No.${CSV_SEPARATOR} " : ''}File Name${pageCountHeader}${fileSizeHeader}\n"
            return csvReport
        }
        return ""
    }

    static void writeToFile() {
        String _time = new SimpleDateFormat("dd-MMM-yy-HH-MM-ss").format(new Date().time)

        String _folderNames = (FOLDER_NAME.collect { return new File(it) })*.name.join("_")
        String fileName = _folderNames + "_MegaList_" + (ONLY_PDFS ? "pdfs_only" : "all")
        File writeableFile = new File(System.getProperty("user.home"), "${fileName}_${_time}.txt")
        writeableFile << MEGA_REPORT
        log.info("written Logs to file: ${writeableFile.getAbsolutePath()} ")
        if (generateCSVAlso) {
            File writeableCSVFile = new File(System.getProperty("user.home"), "${fileName}_${_time}.csv")
            writeableCSVFile << generateCsvHeader()
            writeableCSVFile << CSV_MEGA_REPORT
            log.info("written CSV to file: ${writeableCSVFile.getAbsolutePath()} ")
        }
    }

    static void addToReportAndPrint(String _report, boolean onlyLogDontCommitToFile = false) {
        log.info(_report)
        if (!onlyLogDontCommitToFile) {
            MEGA_REPORT.append("$_report\n")
        };
    }

    static void addToCSVReport(String _report) {
        if (generateCSVAlso) {
            CSV_MEGA_REPORT.append("$_report\n")
        };
    }

    static String formatInteger(Integer _formattable, String delimiter = "") {
        if(delimiter.contains(",")){
            return _formattable
        }
       else{
            def pattern = "##,##,##,###"
            def moneyform = new DecimalFormat(pattern)
            return moneyform.format(_formattable.toLong())
        }
    }

    static void processOneFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        String readingFolder = "\nReading Folder ${directory}" + (afterDateAsLong ? " for Files created after ${afterDate}" : '') + (afterHour > 0 ? " ${afterHour}:00 Hours" : '');
        addToReportAndPrint(readingFolder, DONT_MENTION_SUB_FOLDERS)

        if (TOTAL_NUM_PAGES > 0) {
            log.info("Already read ${formatInteger(TOTAL_NUM_PAGES)} pages in ${formatInteger(TOTAL_FILES_SUCCESSFULLY_READ)} files")
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
                    if (!ONLY_PDFS || (ONLY_PDFS && file.name.endsWithIgnoreCase(PDF))) {
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
        try {

            if (INCLUDE_NUMBER_OF_PAGES && file.name.endsWithIgnoreCase(PDF)) {
                PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)
                PdfDocument pdfDoc = new PdfDocument(pdfReader);
                numberOfPages = pdfDoc.getNumberOfPages()
                incrementTotalPageCount(numberOfPages)
                pdfDoc.close()
                pdfReader.close()
            }

            String sizeInfo = FileSizeUtil.getFileSizeFormatted(file);
            String pageCountLogic = "${INCLUDE_NUMBER_OF_PAGES && file.name.endsWithIgnoreCase(PDF) ? ', ' + numberOfPages + ' Pages' : ''}";
            String fileSizeLogic = "${INCLUDE_FILE_SIZE && file.name.endsWithIgnoreCase(PDF) ? ', ' + sizeInfo : ''}";
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} ${file.name} ${pageCountLogic} ${fileSizeLogic}";
            addToReportAndPrint(_report);
            String csvSizeInfo = FileSizeUtil.getFileSizeFormatted(file, ", ")
            String csvPageCountLogic = "${INCLUDE_NUMBER_OF_PAGES && file.name.endsWithIgnoreCase(PDF) ? numberOfPages : ''}"
            String csvFileSizeLogic = "${INCLUDE_FILE_SIZE && file.name.endsWithIgnoreCase(PDF) ? csvSizeInfo : ''}"
            String csvReport = "${INCLUDE_INDEX ? index + "${CSV_SEPARATOR} " : ''}${file.name}${CSV_SEPARATOR} ${csvPageCountLogic}${CSV_SEPARATOR} ${csvFileSizeLogic}"
            addToCSVReport(csvReport)
            incrementFileCount()
        }
        catch (com.itextpdf.kernel.exceptions.BadPasswordException bpe) {
            addToErrors(file.absolutePath, true)
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} *****${file.name} is password-protected";
            addToReportAndPrint(_report)
            addToCSVReport(_report.split(/\s/, 2).join(", "))
            log.error("Error in reading Password Protected File for ${file.absolutePath}", bpe)
        }
        catch (Exception e) {
            addToErrors(file.absolutePath)
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} *****${file.name} had an error reading page/count/size";
            addToReportAndPrint(_report)
            addToCSVReport(_report.split(/\s/, 2).join(", "))
            log.error("Error in reading file page-count/size/ for ${file.absolutePath}", e)
        }
    }

    static void incrementFileCount() {
        TOTAL_FILES_SUCCESSFULLY_READ++
    }

    static void addToErrors(String filePath, boolean passwordProtectedError = false) {
        TOTAL_ERRORS++
        if (!passwordProtectedError) {
            ERRORENOUS_FILES << filePath
        } else {
            PASSWORD_PROTECTED_FILES << filePath
        }
    }

    static void incrementTotalPageCount(int numPagesToIncrement) {
        TOTAL_NUM_PAGES += numPagesToIncrement
    }


    static int calculateTotalFileCount() {
        int pdfCount = 0
        for (String folder : FOLDER_NAME) {
            File[] files = FileSizeUtil.allPdfsInDirAsFileList(folder)
            pdfCount += files?.length
        }
        return pdfCount
    }

    static String generateStats(String delimiter = " ") {
        String totalStats = """
Total File Count:${delimiter}${TOTAL_FILES} (** if you are date-filtering then this feature for count of date-filtered not implemented yet)
Total Files Processed:${delimiter}${formatInteger(TOTAL_FILES_SUCCESSFULLY_READ + TOTAL_ERRORS,delimiter)}
Total Files Read Successfully:${delimiter}${formatInteger(TOTAL_FILES_SUCCESSFULLY_READ,delimiter)}
Total Files with Errors(including password-protected):${delimiter}${formatInteger(TOTAL_ERRORS,delimiter)}
Total Files system didnt pick:${delimiter}${formatInteger((TOTAL_FILES - (TOTAL_FILES_SUCCESSFULLY_READ + TOTAL_ERRORS)),delimiter)}
Erroneous File List:${delimiter}${ERRORENOUS_FILES ? "\n" + ERRORENOUS_FILES.join("\t\t\n") : 0}
Password Protected Erroneous File List:${delimiter}${PASSWORD_PROTECTED_FILES ? "\n" + PASSWORD_PROTECTED_FILES.join("\t\t\n") : 0}
Total Pages:${delimiter}${formatInteger(TOTAL_NUM_PAGES)}"""
        return totalStats
    }

    static void printFinalStats() {
        addToReportAndPrint(generateStats())
        addToCSVReport(generateStats(CSV_SEPARATOR))
    }


}
