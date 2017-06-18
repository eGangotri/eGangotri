package com.egangotri.pdf

import com.itextpdf.text.pdf.PdfReader

/**
 * All Titles of PDF's in a Folder and SubFolders
 */
class BookTitles {

    static String FOLDER_NAME = "C:\\hw\\nk"

    static List ignoreList = []

    static String PDF = "pdf"
    static boolean includeNumberOfPages = true
    static boolean includeIndex = true
    static boolean onlyRootDirAndNoSubDirs = false
    static int TOTAL_FILES = 0
    static int TOTAL_NUM_PAGES = 0
    static List<Integer> kriIds = []
    static main(args) {
        String args0 = ""
        if(args?.size() >0){
            args0 = args[0]
        }
        println "args0:$args0"
        //new BookTitles().actor.start()
        //if only the directory specified
        if (BookTitles.onlyRootDirAndNoSubDirs) {
            new BookTitles().processOneFolder(args0 ?: BookTitles.FOLDER_NAME)
        } else {
            //if everything
            new BookTitles().procAdInfinitum(args0 ?: BookTitles.FOLDER_NAME)
        }
        println kriIds.sort()
        println "Total Files: ${TOTAL_FILES}  \t\t Total Pages: ${TOTAL_NUM_PAGES}"
    }

    void processOneFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)

        println "processAFolder $directory"
        int index = 0
        for (File file : directory.listFiles()) {
            if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && file.name.endsWith(PDF)) {
                getIds(file)
                //printFileName(folderAbsolutePath, file, ++index)
            }
        }
    }

    void getIds(File file){
        def kri = (file.name  =~ "KRI(-)\\d+")
        if(kri){
            println "***${kri[0][0]}"
            kriIds << kri[0][0].toString().replaceFirst("KRI-","").toInteger()
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
        PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)

        int numberOfPages = pdfReader.getNumberOfPages()
        println "${includeIndex ? index + ').' : ''} ${file.name} ${includeNumberOfPages ? ', ' + numberOfPages + ' Pages' : ''}"
        incrementTotalPageCount(numberOfPages)
        incrementFileCount()
    }

    void incrementFileCount(){
        TOTAL_FILES++
    }

    void incrementTotalPageCount(int numPagesToIncrement){
        TOTAL_NUM_PAGES += numPagesToIncrement
    }

}
