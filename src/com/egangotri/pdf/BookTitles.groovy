package com.egangotri.pdf

import com.itextpdf.text.pdf.PdfReader
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

    static List<String> FOLDER_NAME = ["D:\\Treasures26\\"]
    static String afterDate = "" //format DD-MM-YYYY
    static int afterHour = 0 //format DD-MM-YYYY
    static long afterDateAsLong = 0

    static List ignoreList = ['otro']

    static String PDF = "pdf"
    static boolean INCLUDE_NUMBER_OF_PAGES = true
    static boolean includeIndex = true
    static boolean onlyRootDirAndNoSubDirs = false
    static int TOTAL_FILES = 0
    static int TOTAL_NUM_PAGES = 0

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
                if(afterDate.contains("-")){
                    try{
                        int diff = -1*Integer.parseInt(afterDate.split("-")[1].trim())
                        date.add(Calendar.DAY_OF_YEAR,diff)
                    }
                    catch(Exception e){
                        log.error("parse error", e)
                    }
                }
                date.set(Calendar.HOUR_OF_DAY, afterHour);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                afterDate =  new SimpleDateFormat("dd-MMM-yy").format(date.time)
                afterDateAsLong = date.time.time
            } else {
                afterDateAsLong = new SimpleDateFormat("dd-MM-yyyy").parse(afterDate).getTime() + (afterHour*60*60*1000)
            }
        }
        log.info("args0:$FOLDER_NAME")

        for(String folder: FOLDER_NAME){
            //if only the directory specified
            if (BookTitles.onlyRootDirAndNoSubDirs) {
                new BookTitles().processOneFolder(folder)
            } else {
                //if everything
                new BookTitles().procAdInfinitum(folder)
            }
        }
        log.info("Total Files: ${formatInteger(TOTAL_FILES)}  \t\t Total Pages: ${formatInteger(TOTAL_NUM_PAGES)}")
    }
    static String formatInteger(Integer _formattable){
        def pattern = "##,##,##,###"
        def moneyform = new DecimalFormat(pattern)
        return moneyform.format(_formattable.toLong())
    }
    void processOneFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        log.info("\nReading Folder ${directory}" + (afterDateAsLong ? " for Files created after ${afterDate}" : '') + (afterHour>0 ? " ${afterHour}:00 Hours" : ''))
        if(TOTAL_NUM_PAGES>0){
            log.info("Already read ${formatInteger(TOTAL_NUM_PAGES)} pages in ${formatInteger(TOTAL_FILES)} files")
        }
        int index = 0
        for (File file : directory.listFiles()) {
            long createDateAsLong = 0

            if(afterDateAsLong){
                BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                        BasicFileAttributes.class)
                createDateAsLong = attr.creationTime().toMillis()
            }

            if (!file.isDirectory() && !inIgnoreList(file)
                    && (!afterDateAsLong || (createDateAsLong > afterDateAsLong))) {
                try{
                    printFileName(folderAbsolutePath, file, ++index)
                }
                catch(Exception e){
                    log.info("Error reading file. will continue" + e)
                }
            }
        }
    }

    static boolean inIgnoreList(File file){
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

    void procAdInfinitum(String folderAbsolutePath) {
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

    void printFileName(String folderAbsolutePath, File file, int index) {
        int numberOfPages = 0

        if (INCLUDE_NUMBER_OF_PAGES && file.name.endsWith(PDF)) {
            PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)
            numberOfPages = pdfReader.getNumberOfPages()
            incrementTotalPageCount(numberOfPages)
        }

        log.info("${includeIndex ? index + ').' : ''} ${file.name} ${INCLUDE_NUMBER_OF_PAGES && file.name.endsWith(PDF) ? ', ' + numberOfPages + ' Pages' : ''}")
        incrementFileCount()
    }

    void incrementFileCount() {
        TOTAL_FILES++
    }

    void incrementTotalPageCount(int numPagesToIncrement) {
        TOTAL_NUM_PAGES += numPagesToIncrement
    }

}
