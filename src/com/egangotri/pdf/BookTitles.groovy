package com.egangotri.pdf

import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

/**
 * All Titles of PDF's in a Folder and SubFolders
 */
@Slf4j
class BookTitles {

    static String FOLDER_NAME = "D:\\Treasures26\\"

    static List ignoreList = []

    static String PDF = "pdf"
    static boolean includeNumberOfPages = true
    static boolean includeIndex = true
    static boolean onlyRootDirAndNoSubDirs = false
    static int TOTAL_FILES = 0
    static int TOTAL_NUM_PAGES = 0
    static List<Integer> kriIds = []
    static void main(String[] args) {
        execute(args)
    }

    static void execute(String[] args = []){
        if(args?.size() >0){
            FOLDER_NAME = args[0]
        }
        log.info("args0:$FOLDER_NAME")

        //if only the directory specified
        if (BookTitles.onlyRootDirAndNoSubDirs) {
            new BookTitles().processOneFolder(BookTitles.FOLDER_NAME)
        } else {
            //if everything
            new BookTitles().procAdInfinitum(BookTitles.FOLDER_NAME)
        }
        log.info("${ kriIds.sort()}")
        log.info( "Total Files: ${TOTAL_FILES}  \t\t Total Pages: ${TOTAL_NUM_PAGES}")
    }

    void processOneFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)

        log.info( "reading Folder $directory")
        int index = 0
        for (File file : directory.listFiles()) {
            if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && file.name.endsWith(PDF)) {
                //getIds(file)
                printFileName(folderAbsolutePath, file, ++index)
            }
        }
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
            if (subDirectory.isDirectory() && !ignoreList.contains(subDirectory.name.toString())) {
                procAdInfinitum(subDirectory.absolutePath)
            }
        }
    }

    void printFileName(String folderAbsolutePath, File file, int index) {
        int numberOfPages = 0

        if(includeNumberOfPages) {
            PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)
            numberOfPages = pdfReader.getNumberOfPages()
            incrementTotalPageCount(numberOfPages)
        }

        log.info( "${includeIndex ? index + ').' : ''} ${file.name} ${includeNumberOfPages ? ', ' + numberOfPages + ' Pages' : ''}")
        incrementFileCount()
    }

    void incrementFileCount(){
        TOTAL_FILES++
    }

    void incrementTotalPageCount(int numPagesToIncrement){
        TOTAL_NUM_PAGES += numPagesToIncrement
    }

}
