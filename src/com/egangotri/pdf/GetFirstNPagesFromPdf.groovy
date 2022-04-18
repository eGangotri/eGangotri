package com.egangotri.pdf

import groovy.util.logging.Slf4j

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * All Titles of PDF's in a Folder and SubFolders
 */
@Slf4j
class GetFirstNPagesFromPdf {

    static File MAIN_FOLDER
    static String OUTPUT_FOLDER_NAME = ""
    static String PDF = "pdf"
    static final String TRUNCATION_SUFFIX = "_truncated"

    static int PAGE_LIMIT = 5
    static boolean ONLY_PDFS = true

    static void main(String[] args) {
        execute(args)
    }

    static void execute(String[] args = []) {
        if(args.size() > 0 ){
            MAIN_FOLDER = new File(args[0])
        }
        if(args.size() == 2){
            PAGE_LIMIT = args[1].toInteger()
        }
        OUTPUT_FOLDER_NAME = MAIN_FOLDER.getAbsolutePath() + TRUNCATION_SUFFIX
        File outputFolderAsFile = new File(OUTPUT_FOLDER_NAME)
        if(!outputFolderAsFile.exists()){
            outputFolderAsFile.mkdir()
        }
        log.info("created outputFolderAsFile ${outputFolderAsFile.getAbsolutePath()}")
        log.info("Reading files: $MAIN_FOLDER\n")
        processOneFolder(MAIN_FOLDER)
        for (File folder : MAIN_FOLDER.listFiles()) {
            procAdInfinitum(folder)
        }
    }

    /**
     * if you have one folder and you want it to go one level deep to process multiple foldrs within
     */

    /**
     * Recursive Method
     * @param folderAbsolutePath
     */

    static void procAdInfinitum(File directory) {
        //Process Root Folder
        processOneFolder(directory)

        //Then get in Sub-directories and process them
        for (File subDirectory : directory.listFiles()) {
            if (subDirectory.isDirectory()) {
                procAdInfinitum(subDirectory)
            }
        }
    }
    static void processOneFolder(File directory) {
        log.info("\nReading Folder ${directory}")
        int counter = 0
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                try {
                    if (!ONLY_PDFS || (ONLY_PDFS && file.name.endsWith(PDF))) {
                        String parentPath = file.parentFile.absolutePath
                        File outputFolder =
                                new File(parentPath.replaceFirst(MAIN_FOLDER.name,new File(OUTPUT_FOLDER_NAME).name))
                        if(!outputFolder.exists()){
                            outputFolder.mkdir()
                        }
                        File outputFile = new File(outputFolder.absolutePath,file.name)
                        log.info("${++counter} Converting File: ${file} to outputFile ${outputFile}")
                        readAndUsePdf(file, outputFile)
                    }
                }
                catch (Exception e) {
                    log.info("Error reading file. will continue" + e)
                }
            }
        }
    }

    static readAndUsePdf(File pdfFileName, File outputFile){
        Document outputDocument = new Document()
        PdfWriter writer = PdfWriter.getInstance(outputDocument,
                new FileOutputStream(outputFile))
        outputDocument.open()

        PdfReader reader = new PdfReader(pdfFileName.getAbsolutePath())
        int numberOfPages = reader.getNumberOfPages()
        PdfImportedPage page
        // Go through all pages
        for (int i = 1; i <= (PAGE_LIMIT<= numberOfPages ? PAGE_LIMIT: numberOfPages) ; i++) {
            page = writer.getImportedPage(reader, i)
            Image instance = Image.getInstance(page)
            outputDocument.add(instance)
        }
        outputDocument.close()
    }

}
