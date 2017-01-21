package com.egangotri.pdf

import com.itextpdf.text.pdf.PdfReader

/**
 * All Titles of PDF's in a Folder and SubFolders
 */
class BookTitles {

    static String FOLDER_NAME = "C:\\hw\\amit\\UPSS"

    static String SPLIT_FOLDER_NAME = "split"
    static List ignoreList = [SPLIT_FOLDER_NAME]

    static String PDF = "pdf"
    static boolean includeNumberOfPages = false
    static boolean includeIndex = false
    static boolean onlyRootDirAndNoSubDirs = false


    int totalFilesSplittable = 0
    boolean allSplitFilesInPlace = true

    static main(args) {
        String args0 = ""//args[0]
        println "args0:$args0"
        //new BookTitles().actor.start()
        //if only the directory specified
        if (BookTitles.onlyRootDirAndNoSubDirs) {
            new BookTitles().processOneFolder(args0 ?: BookTitles.FOLDER_NAME)
        } else {
            //if everything
            new BookTitles().procAdInfinitum(args0 ?: BookTitles.FOLDER_NAME)
        }
    }

    void processOneFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)

        println "processAFolder $directory"
        int index = 0
        for (File file : directory.listFiles()) {
            if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && file.name.endsWith(PDF)) {
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
        PdfReader pdfReader = new PdfReader(folderAbsolutePath + "\\" + file.name)
        int numberOfPages = pdfReader.getNumberOfPages()
        println "${includeIndex ? index + ').' : ''} ${file.name} ${includeNumberOfPages ? ', ' + numberOfPages + ' Pages' : ''}"
    }

}
