package com.egangotri.pdf

import com.egangotri.util.FileSizeUtil
import com.egangotri.util.PdfUtil
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type
/**
 * All Titles of PDF's in a Folder and SubFolders
 */
@Slf4j
class BookTitles {
   // static String DUMP_DIRECTORY = "${System.getProperty("user.home")}\\_bookTitles"
    static String DUMP_DIRECTORY = "C:\\_catalogWork\\_collation\\local"
    static boolean ONLY_PDFS = true

    static boolean generateCSVAlso = true
    static boolean generateExcelAlso = true
    static List<String> FOLDER_NAMES = ["C:\\tmp\\pdfForMergeTest\\"]
    static String afterDate = "" //format DD-MM-YYYY
    static int afterHour = 0 //format DD-MM-YYYY
    static long afterDateAsLong = 0
    static int START_INDEX = 0
    static int FOLDER_INDEX = 0
    static int TOTAL_FILES = 0
    static int TOTAL_FILE_SIZE = 0
    static int TOTAL_FILES_SUCCESSFULLY_READ = 0
    static int TOTAL_ERRORS = 0
    static List<String> ERRORENOUS_FILES = []
    static List<String> PASSWORD_PROTECTED_FILES = []
    static int TOTAL_NUM_PAGES = 0

    static List ignoreList = ["C:\\Treasures48\\_freeze\\jngm_kan\\retry_dont"]
    static String PDF = "pdf"

    static StringBuilder MEGA_REPORT = new StringBuilder("")
    static StringBuilder CSV_MEGA_REPORT = new StringBuilder("")

    static boolean DONT_MENTION_SUB_FOLDERS = true;
    static boolean INCLUDE_NUMBER_OF_PAGES = true
    static boolean INCLUDE_FILE_SIZE = true
    static boolean INCLUDE_TOTAL_FILE_SIZE = true
    static boolean INCLUDE_INDEX = true
    static boolean ONLY_ROOT_DIR_NO_SUBDIRS = false
    static boolean INCLUDE_FOLDER_NAME = true

    static String CSV_SEPARATOR = ";"

    static void main(String[] args) {
        log.info("args ${args}")
        incrementFolderCounter()
        execute(args)
    }

    static void execute(String[] args = []) {
        if (args?.size() > 0) {
            Map argsOneAsMap = [:];
            args.each {
                String it -> {
                    Object[] kv = it.split("=")*.trim()
                    if(kv.length % 2 !== 0){
                        log.error("Invalid argument Must have 2 set of values separated by = ${it}. example path='C:\\tmp, D:\\test'");
                        return
                    }
                    log.info("kv ${kv} ${kv.length}");
                    argsOneAsMap.put(kv[0]?.toString(), kv[1])
                }
            }
            String _folderNames = argsOneAsMap?.paths?.replace("'", "")?.replace("\"", "")
            FOLDER_NAMES = _folderNames.split(",")*.trim().findAll {it.length()>1}.toList()
            if(argsOneAsMap.containsKey("afterDate")){
                afterDate = argsOneAsMap.afterDate == "''" ? "" : argsOneAsMap.afterDate
            }
            if(argsOneAsMap.containsKey("afterHour")) {
                afterHour = (argsOneAsMap.afterHour as int) ?: 0
                if (afterHour) {
                    if (afterHour >= 24 || afterHour < 1) {
                        afterHour = 0
                    }
                }
            }
            log.info("entryset ${argsOneAsMap.entrySet()}");

            if(argsOneAsMap.containsKey("pdfsOnly")) {
                log.info("pdfsOnly ${argsOneAsMap?.pdfsOnly}");
                ONLY_PDFS = argsOneAsMap?.pdfsOnly != "false"
            }
            log.info("all ${FOLDER_NAMES} ${afterDate} ${afterHour} ${ONLY_PDFS}");
        }
        calculateAfterDate()
        TOTAL_FILES = PdfUtil.calculateTotalFileCount(ONLY_PDFS, FOLDER_NAMES)
        addToReportAndPrint("Reading files: $FOLDER_NAMES\n",false)
        for (String folder : FOLDER_NAMES) {
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
            String separatorSpecification = "sep=${CSV_SEPARATOR}\n"
            ///
            String includeFolderName = "${INCLUDE_FOLDER_NAME ? "${CSV_SEPARATOR}Folder Name" : ''}"
            String pageCountHeader = "${INCLUDE_NUMBER_OF_PAGES ? "${CSV_SEPARATOR}Number of Pages" : ''}"
            String fileSizeHeader = "${INCLUDE_FILE_SIZE ? "${CSV_SEPARATOR}File Size${CSV_SEPARATOR} Units" : ''}"
            String totalFileSizeHeader = "${INCLUDE_TOTAL_FILE_SIZE ? "${CSV_SEPARATOR}Total File Size in KB" : ''}"
            String csvReport = "${separatorSpecification}${INCLUDE_INDEX ? "Serial No.${CSV_SEPARATOR}" : ''}File Name${includeFolderName}${pageCountHeader}${fileSizeHeader} ${totalFileSizeHeader}\n"
            return csvReport
        }
        return ""
    }

    static void writeToFile() {
        String _time = new SimpleDateFormat("dd-MMM-yy-HH-MM-ss").format(new Date().time)

        String _folderNames = (FOLDER_NAMES.collect { return new File(it) })*.name.join("_")
        String fileName = _folderNames + "_MegaList_" + (ONLY_PDFS ? "pdfs_only" : "all")
        String directoryName = DUMP_DIRECTORY + "//${_folderNames}"
        Files.createDirectories(Paths.get(directoryName));
        String pageCountInFileName = "${INCLUDE_NUMBER_OF_PAGES && ONLY_PDFS ? "_(${formatInteger(TOTAL_NUM_PAGES)}pgs)" : ''}";

        String _fileName = "${fileName}_${_time}_${TOTAL_FILES_SUCCESSFULLY_READ}${pageCountInFileName}"
        File writeableFile = new File(directoryName, "${_fileName}.txt")
        def textWriter = writeableFile.newWriter('UTF-8')
        textWriter << MEGA_REPORT
        textWriter.close()

        log.info("written Logs to file: ${writeableFile.getAbsolutePath()} ")
        if (generateCSVAlso) {
            File writeableCSVFile = new File(directoryName, "${_fileName}.csv")
            def csvWriter = writeableCSVFile.newWriter('UTF-8')
            csvWriter << generateCsvHeader()
            csvWriter << CSV_MEGA_REPORT
            csvWriter.close()
            log.info("written CSV to file: ${writeableCSVFile.getAbsolutePath()} ")
            if (generateExcelAlso) {
                CsvToExcel.csvtoXls(writeableCSVFile)
            }
        }
    }

    static void addToReportAndPrint(String _report, boolean onlyLogDontCommitToFile = false, boolean includeFolderIndex = false) {
        String _includeFolderIndex = (INCLUDE_INDEX && includeFolderIndex) ? "${FOLDER_INDEX}-" :""
        log.info( _includeFolderIndex + _report)
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
        if (delimiter.contains(",")) {
            return _formattable
        } else {
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
        String exception = ""
        try {
            if (INCLUDE_NUMBER_OF_PAGES && file.name.endsWithIgnoreCase(PDF)) {
                try{
                    PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)
                    PdfDocument pdfDoc = new PdfDocument(pdfReader);
                    numberOfPages = pdfDoc.getNumberOfPages()
                    incrementTotalPageCount(numberOfPages)
                    pdfDoc.close()
                    pdfReader.close()
                }
                catch (Exception e) {
                    addToErrors(file.absolutePath)
                    exception = "Error in reading file page-count/size/ for ${file.absolutePath}" + e.message;
                    log.error("Error in reading file page-count/size/ for ${file.absolutePath}", e)
                }            }
            if (INCLUDE_TOTAL_FILE_SIZE && file.name.endsWithIgnoreCase(PDF)) {
                incrementTotalFileSize(FileSizeUtil.fileSizeInKB(file))
            }
            String sizeInfo = FileSizeUtil.getFileSizeFormatted(file);
            String folderName = "${INCLUDE_FOLDER_NAME ? ', ' + folderNameToCenterName(file.parent) : ''}";
            String pageCountLogic = "${INCLUDE_NUMBER_OF_PAGES && file.name.endsWithIgnoreCase(PDF) ? ', ' + numberOfPages + ' Pages' : ''}";
            String fileSizeLogic = "${INCLUDE_FILE_SIZE && file.name.endsWithIgnoreCase(PDF) ? ', ' + sizeInfo : ''}";
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} ${file.name} ${folderName} ${pageCountLogic} ${fileSizeLogic} ${exception?'*****' + exception:''}";
            addToReportAndPrint(_report,false,true);
            String csvFolderName = "${INCLUDE_FOLDER_NAME ? CSV_SEPARATOR + folderNameToCenterName(file.parent) : ''}";
            String csvPageCountLogic = "${INCLUDE_NUMBER_OF_PAGES && file.name.endsWithIgnoreCase(PDF) ? CSV_SEPARATOR + numberOfPages : ''}"
            String csvSizeInfo = FileSizeUtil.getFileSizeFormatted(file, CSV_SEPARATOR)
            String csvRawSizeInfo = FileSizeUtil.fileSizeInKB(file)
            String csvFileSizeLogic = "${INCLUDE_FILE_SIZE && file.name.endsWithIgnoreCase(PDF) ? csvSizeInfo : ''}"
            String csvTotalFileSizeLogic = "${INCLUDE_TOTAL_FILE_SIZE && file.name.endsWithIgnoreCase(PDF) ? csvRawSizeInfo : ''}"
            String csvReport = "${INCLUDE_INDEX ? index + "${CSV_SEPARATOR}" : ''}${file.name}${csvFolderName}${csvPageCountLogic}${CSV_SEPARATOR}${csvFileSizeLogic}${CSV_SEPARATOR}${csvTotalFileSizeLogic}${exception?CSV_SEPARATOR+'*****' + exception:''}"
            addToCSVReport(csvReport)
            incrementFileCount()
        }
        catch (com.itextpdf.kernel.exceptions.BadPasswordException bpe) {
            addToErrors(file.absolutePath, true)
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} *****${file.name} is password-protected";
            addToReportAndPrint(_report,false)
            addToCSVReport(_report.split(/\s/, 2).join(CSV_SEPARATOR))
            log.error("Error in reading Password Protected File for ${file.absolutePath}", bpe)
        }
        catch (Exception e) {
            addToErrors(file.absolutePath)
            String _report = "${INCLUDE_INDEX ? index + ').' : ''} *****${file.name} had an error reading page/count/size";
            addToReportAndPrint(_report,false)
            addToCSVReport(_report.split(/\s/, 2).join(CSV_SEPARATOR))
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

    static void incrementTotalFileSize(BigDecimal fileSizeinKB) {
        TOTAL_FILE_SIZE += fileSizeinKB
    }


    static String generateStats(String delimiter = " ") {
        String totalStats = """
${ONLY_PDFS ? "PDF Stats" : "ALL File Stats"}:
Total File Count:${delimiter}${TOTAL_FILES} (** if you are date-filtering then this feature for count of date-filtered not implemented yet)
Total Files Processed:${delimiter}${formatInteger(TOTAL_FILES_SUCCESSFULLY_READ + TOTAL_ERRORS, delimiter)}
Total Files with Errors(including password-protected):${delimiter}${formatInteger(TOTAL_ERRORS, delimiter)}
Total Files system didnt pick:${delimiter}${formatInteger((TOTAL_FILES - (TOTAL_FILES_SUCCESSFULLY_READ + TOTAL_ERRORS)), delimiter)}
Erroneous File List:${delimiter}${ERRORENOUS_FILES ? "\n" + ERRORENOUS_FILES.join("\t\t\n") : 0}
Password Protected Erroneous File List:${delimiter}${PASSWORD_PROTECTED_FILES ? "\n" + PASSWORD_PROTECTED_FILES.join("\t\t\n") : 0}
Total Files Read Successfully:${delimiter}${formatInteger(TOTAL_FILES_SUCCESSFULLY_READ, delimiter)}
Total Size of Files:${delimiter}${FileSizeUtil.formatFileSize(TOTAL_FILE_SIZE)}
Total Pages:${delimiter}${formatInteger(TOTAL_NUM_PAGES)}"""
        return totalStats
    }

    static void printFinalStats() {
        addToReportAndPrint(generateStats(),false)
        addToCSVReport(generateStats(CSV_SEPARATOR))
    }

    static void incrementFolderCounter(){
        FOLDER_INDEX++;
    }

    static String folderNameToCenterName(String folderName){
        if(folderName.containsIgnoreCase("${File.separator}sv_shastri")){
            return "SV-Shastri (DAV-Lahore Coll.)"
        }
        if(folderName.containsIgnoreCase("${File.separator}kangri")){
            return "Gurukul Kangri Collection"
        }
        if(folderName.containsIgnoreCase("${File.separator}jngm")){
            return "Jangamwadi Varanasi"
        }
        if(folderName.containsIgnoreCase("${File.separator}panini")){
            return "Panini Kanya Maha Vidyalaya Collection"
        }
        if(folderName.containsIgnoreCase("${File.separator}test")){
            return "Test"
        }
        if(folderName.containsIgnoreCase("${File.separator}tmp")){
            return "Temp"
        }
        return folderName
    }

    static void resetCounters (){
        START_INDEX = 0
        TOTAL_FILES = 0
        TOTAL_FILE_SIZE = 0
        TOTAL_FILES_SUCCESSFULLY_READ = 0
        TOTAL_ERRORS = 0
        ERRORENOUS_FILES = []
        PASSWORD_PROTECTED_FILES = []
        TOTAL_NUM_PAGES = 0
        MEGA_REPORT = new StringBuilder("")
        CSV_MEGA_REPORT = new StringBuilder("")
    }

    static String counterStats (){
        log.info("""
        TOTAL_FILES: ${TOTAL_FILES}
        TOTAL_FILE_SIZE: ${TOTAL_FILE_SIZE}
        TOTAL_FILES_SUCCESSFULLY_READ: ${TOTAL_FILES_SUCCESSFULLY_READ}
        TOTAL_ERRORS: ${TOTAL_ERRORS}
        ERRORENOUS_FILES: ${ERRORENOUS_FILES}
        PASSWORD_PROTECTED_FILES:${PASSWORD_PROTECTED_FILES}
        TOTAL_NUM_PAGES:${TOTAL_NUM_PAGES}
    """);
    }
}
