package com.egangotri.pdf

import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

@Slf4j
class Tally {

    static List<String> TIF_FOLDER = ["E:\\ramtek211Dec\\"]
    static List<String> PDF_FOLDERS = ["E:\\ramtek211Dec\\"]
    static List ignoreList = ['otro']
    static String PDF = "pdf"
    static boolean onlyRootDirAndNoSubDirs = true

    static List<String> NOT_CREATED = [];
    static List<String> NON_MATCHING = [];
    static List<String> MATCHING = [];
    static List<String> UNCHECKABLE = [];
    static int INTRO_PAGE_ADJUSTMENT = 1
    static List<String> REPORT = [];


    static void main(String[] args) {
        execute(args)
    }

    static void execute(String[] args = []) {
        if (args?.size() > 0) {
            String args0 = args[0]
            TIF_FOLDER = args0.split(",")*.trim().toList()
            if (args?.size() > 1) {
                String args1 = args[1]
                PDF_FOLDERS = args1.split(",")*.trim().toList()
            }
        }
        log.info("args0:$TIF_FOLDER")
        log.info("args1:$PDF_FOLDERS")

        for (int i = 0; i < TIF_FOLDER.size(); i++) {
            //if only the directory specified
            if (onlyRootDirAndNoSubDirs) {
                processOneFolder(TIF_FOLDER[i], PDF_FOLDERS[i])
            } else {
                //if everything
                //procAdInfinitum(folder)
            }
            printReport()

        }
    }

    static void processOneFolder(String tifFolder, String pdfFolder) {
        File tifDirectory = new File(tifFolder)
        log.info("\nReading Tif Folder ${tifDirectory}")

        int index = 0
        List tifDirFiles = tifDirectory.listFiles()
        List pdfFiles = new File(pdfFolder).list({ d, f -> f ==~ /(?i).*.pdf/ } as FilenameFilter)


        for (File tifSubDirectory : tifDirFiles) {
            if (tifSubDirectory.isDirectory() && !inIgnoreList(tifSubDirectory)) {
                try {
                    index++
                    log.info("tifSubDirectory ${tifSubDirectory}")
                    def tifs = tifSubDirectory.list({ d, f -> f ==~ /(?i).*.tif/ } as FilenameFilter)
                    int tifCount = tifs.size()

                    File pdfFile = new File(pdfFolder, tifSubDirectory.name + ".pdf")
                    if (!pdfFile.exists()) {
                        addReport("Error ${pdfFile} was never created");
                        NOT_CREATED.push("'${tifSubDirectory.name}'");
                        continue;
                    }
                    int pdfPageCount = getPdfPageCount(pdfFile)

                    addReport("""${index}). Checking Tiff Count in 
                            ${tifSubDirectory} equals 
                            ${pdfFile} 
                            ${pdfPageCount}""");
                    if (pdfPageCount === tifCount) {
                        MATCHING.push("'${pdfFile}");
                        addReport("pdf (${pdfPageCount}) " +
                                "${pdfFile} " +
                                "Page Count == " +
                                "PNG Count " +
                                "${(tifCount)}\n");
                    } else {
                        if (pdfPageCount > 0) {
                            NON_MATCHING.push(pdfFile);
                        } else {
                            UNCHECKABLE.push("${pdfFile} should have ${tifCount + INTRO_PAGE_ADJUSTMENT} pages");
                        }
                        addReport("****PDF Count  (${pdfPageCount}) for ${pdfFile} is not same as ${tifCount}\n");
                    }
                }
                catch (Exception e) {
                    log.info("Error reading file. will continue" + e)
                    continue;
                }
            }
        }


        addReport("""Stats:
                    NON_MATCHING_COUNT: ${NON_MATCHING.size()}
                    MATCHING_COUNT: ${MATCHING.size()}
                    UNCHECKABLE_COUNT: ${UNCHECKABLE.size()}
                    NOT_CREATED_COUNT: ${NOT_CREATED.size()}
                    Total Tiff Folders expected for Conversion: ${tifDirFiles.size()}
                    Total PDFs in Folder: ${pdfFiles.size()}
                    Ready For Upload: ${MATCHING}
                    Manually check ${UNCHECKABLE}
                    Pdf Folder: ${pdfFolder}
                    Tif Folder: ${tifFolder}
                    Reconvert [${NOT_CREATED.join(",") }]
                    Error Margin: ${tifDirFiles.size()} - ${pdfFiles.size()} = ${tifDirFiles.size() - pdfFiles.size()}
                """)
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

//    static void procAdInfinitum(String folderAbsolutePath) {
//        File directory = new File(folderAbsolutePath)
//
//        //Process Root Folder
//        processOneFolder(folderAbsolutePath)
//
//        //Then get in Sub-directories and process them
//        for (File subDirectory : directory.listFiles()) {
//            if (subDirectory.isDirectory() && !inIgnoreList(subDirectory)) {
//                procAdInfinitum(subDirectory.absolutePath)
//            }
//        }
//    }

    static int getPdfPageCount(File pdfFile) {
        int numberOfPages = 0

        if (pdfFile.name.endsWith(PDF)) {
            log.info(pdfFile.getAbsolutePath())
            PdfReader pdfReader = new PdfReader(pdfFile.getAbsolutePath())
            numberOfPages = pdfReader.getNumberOfPages() - INTRO_PAGE_ADJUSTMENT
        }
        return numberOfPages;
    }

    static String addReport(String report) {
        REPORT.push(report)
        log.info(report)
    }

    static void printReport() {
        REPORT.eachWithIndex { x, i ->
            {
                log.info("${i + 1}). ${x}")
            }
        }
    }
}