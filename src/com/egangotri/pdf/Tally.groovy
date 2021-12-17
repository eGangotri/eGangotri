package com.egangotri.pdf

import com.egangotri.itext7.PdfImageCounter
import com.egangotri.util.GenericUtil
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
    static List<String> EXCEPTION_ENCOUNTERED = [];
    static int INTRO_PAGE_ADJUSTMENT = 1


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
            }
            GenericUtil.printReport()

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
                index++
                log.info("Tif Folder ${tifSubDirectory}")
                def tifs = tifSubDirectory.list({ d, f -> f ==~ /(?i).*.tif/ } as FilenameFilter)
                int tifCount = tifs.size()
                File pdfFile = new File(pdfFolder, tifSubDirectory.name + ".pdf")
                if (!pdfFile.exists()) {
                    log.info("****Error was never created\n ${pdfFile.name}");
                    NOT_CREATED.push("'${tifSubDirectory.name}'");
                    continue;
                }
                try {
                    int pdfPageCount = PdfImageCounter.getPdfImageCount(pdfFile)

                    log.info("""${index}). Checking Tiff Count (${tifCount}) in 
                            ${GenericUtil.dualEllipsis(tifSubDirectory.name)} equals 
                            ${GenericUtil.dualEllipsis(pdfFile.name)} 
                            ${pdfPageCount}""");
                    if (pdfPageCount === tifCount) {
                        MATCHING.push("'${pdfFile}");
                        GenericUtil.addReport("""pdf (${pdfPageCount}) 
    ${pdfFile.name} 
    Page Count ==  PNG Count
    ${(tifCount)}\n""");
                    } else {
                        if (pdfPageCount > 0) {
                            NON_MATCHING.push("'${pdfFile}");
                        } else {
                            UNCHECKABLE.push("""${pdfFile.name} 
                                        should have ${tifCount + INTRO_PAGE_ADJUSTMENT} pages""");
                        }
                        GenericUtil.addReport("""**** PDF Count  (${pdfPageCount})
                        for ${pdfFile} is not same as
                        ${tifCount}""");
                    }
                }
                catch (Exception e) {
                    log.info("getPdfPageCount Exception", e)
                    EXCEPTION_ENCOUNTERED.push("'${pdfFile}")
                    continue
                }

            }
        }


        GenericUtil.addReport("""Stats:
                    Pdf Folder: ${pdfFolder}
                    Tif Folder: ${tifFolder}                    
                    NON_MATCHING_COUNT: ${NON_MATCHING.size()}
                    MATCHING_COUNT: ${MATCHING.size()}
                    UNCHECKABLE_COUNT: ${UNCHECKABLE.size()}
                    NOT_CREATED_COUNT: ${NOT_CREATED.size()}
                    EXCEPTION_ENCOUNTERED_COUNT: ${EXCEPTION_ENCOUNTERED.size()}
                    Total Tiff Folders expected for Conversion: ${tifDirFiles.size()}
                    Total PDFs in Folder: ${pdfFiles.size()}
                    Ready For Upload: ${MATCHING}
                    Manually check ${UNCHECKABLE}
                    Reconvert (Uncreated) ${NOT_CREATED}
                    Reconvert (Erroneous Page Count) ${NON_MATCHING}
                    Reconvert (Exception Encountered) ${EXCEPTION_ENCOUNTERED}
                    Error Margin: ${tifDirFiles.size()} - ${MATCHING.size()} = ${tifDirFiles.size() - MATCHING.size()}
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


}