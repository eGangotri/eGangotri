package com.egangotri

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import groovyx.gpars.GParsPool

/**
 * This Class takes in a Folder and splits all pdfs that are above 100 MB.
 */
class PdfSplitter {
    static String SPLIT_FOLDER_NAME = "split"
    static String PDF = ".pdf"
    static String FOLDER_NAME = "C:\\hw\\megha"
    static List ignoreList = [SPLIT_FOLDER_NAME]

    static Float THRESHOLD = 95.00
    static Float BYTES_IN_A_KILO = 1024.0

    static boolean onlyRootDirAndNoSubDirs = true

    int totalFilesSplittable = 0
    boolean allSplitFilesInPlace = true

    static main(args) {
        String args0 = args?args[0]:null
        println "args0:$args0"
//        if(args0){
//            FOLDER_NAME = args0
//        }
        //if only the directory specified
        if (PdfSplitter.onlyRootDirAndNoSubDirs) {
            new PdfSplitter().processAFolder(args0 ?: PdfSplitter.FOLDER_NAME)
        } else {
            //if everything
            new PdfSplitter().procAdInfinitum(args0 ?: PdfSplitter.FOLDER_NAME)
        }
    }

    void processAFolder(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        println "processAFolder $directory"
        totalFilesSplittable = 0
        def files = directory.listFiles()
        //GParsPool.withPool {
            files.each { File file ->
                if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && file.name.endsWith(PDF)) {
                    splitPdfIfGreaterThanThreshold(folderAbsolutePath, file)
                }
            }

        //}
        println "***Total Files Split: ${totalFilesSplittable}"
    }

    /**
     * Recursive Method
     * @param folderAbsolutePath
     */

    void procAdInfinitum(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)
        //Process Root Folder
        processAFolder(folderAbsolutePath)
        //Then get in Sub-directories and process them
        for (File subDirectory : directory.listFiles()) {
            if (subDirectory.isDirectory() && !ignoreList.contains(subDirectory.name.toString())) {
                procAdInfinitum(subDirectory.absolutePath)
            }
        }
    }

    void splitPdfIfGreaterThanThreshold(String folderAbsolutePath, File file) {
        try {
            String fileName = file.name
            print("\t\tProcessing $fileName");

            //Step 1:
            PdfReader splitPdfBySize = new PdfReader(folderAbsolutePath + "\\" + fileName);

            def bytes = file.length();

            def kilobytes = (bytes / BYTES_IN_A_KILO);
            def fileSizeInMB = (kilobytes / BYTES_IN_A_KILO);

            def formattedSize = new BigDecimal(fileSizeInMB).setScale(2, BigDecimal.ROUND_FLOOR)

            if (fileSizeInMB < PdfSplitter.THRESHOLD) {
                System.err.println "( Size: ${formattedSize} Mb); No Need to Split"
                return
            }

            println "( Size: ${formattedSize}) Will be Split"
            totalFilesSplittable++

            creteSplitFolder(folderAbsolutePath)
            Document document = new Document()

            String absoluteFileName = getSplitFolder(folderAbsolutePath) + "//" + fileName
            def nthPdf = 1

            String splitFileName = absoluteFileName - ".pdf" + "_Part${nthPdf++}.pdf"
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(splitFileName))
            document.open();

            int number_of_pages = splitPdfBySize.getNumberOfPages();
            int pagenumber = 1;               /* To generate file name dynamically */
            int findPdfSize                 /* To get PDF size in bytes */
            float combinedsize = 0;          /* To convert this to Kilobytes and estimate new PDF size */

            for (int i = 1; i <= number_of_pages; i++) {
                if (combinedsize == 0 && i != 1) {             /* Generate new file only for second time when first document size
                          exceeds limit and incoming loop counter is not 1 */
                    document = new Document();
                    pagenumber++;
                    splitFileName = absoluteFileName - ".pdf" + "_Part${nthPdf++}.pdf";  /* Dynamic file name */
                    copy = new PdfCopy(document, new FileOutputStream(splitFileName));
                    document.open();
                }
                copy.addPage(copy.getImportedPage(splitPdfBySize, i));    /* Import pages from original document */
                findPdfSize = copy.getCurrentDocumentSize();    /* Estimate PDF size in bytes */
                combinedsize = (float) findPdfSize / (BYTES_IN_A_KILO);  /* Convert bytes to kilobytes */
                //println "findPdfSize: $findPdfSize"
                //println "combinedsize: $combinedsize"
                //should be less than 99 MB
                //println ("i: $i ,number_of_pages: $number_of_pages")
                if (combinedsize > (THRESHOLD * BYTES_IN_A_KILO) || i == number_of_pages) {
                    println "Done creating $splitFileName"
                    document.close();
                    combinedsize = 0;
                }
            }

            if (document.isOpen()) {
                document.close()
            }
            println("PDF ($fileName) Split By Size Completed. Number of Documents Created: $pagenumber");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    def creteSplitFolder(String folderName = null) {
        File splitFolder = new File(getSplitFolder(folderName))

        if (splitFolder) {
            if (!splitFolder.exists()) {
                println("${PdfSplitter.SPLIT_FOLDER_NAME} missing. Creating");
                splitFolder.mkdir()
            } else {
                println("${PdfSplitter.SPLIT_FOLDER_NAME} already exists.");
            }
        }
    }

    def getSplitFolder(String folderName = null) {
        String rootDir
        if (allSplitFilesInPlace) {
            rootDir = PdfSplitter.FOLDER_NAME
        } else {
            rootDir = folderName ?: PdfSplitter.FOLDER_NAME
        }

        return rootDir + "//" + PdfSplitter.SPLIT_FOLDER_NAME

    }
}
