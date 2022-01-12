package com.egangotri.pdf

import com.egangotri.itext7.PdfImageCounter
import com.egangotri.util.GenericUtil
import groovy.util.logging.Slf4j

@Slf4j
class Tally {

    static String TIF_FOLDER = "E:\\ramtek211Dec\\"
    static String PDF_FOLDERS = "E:\\ramtek211Dec\\"
    static List ignoreList = ['otro']
    static String PDF = "pdf"
    static int INTRO_PAGE_ADJUSTMENT = 1

    static void main(String[] args) {
        if(args.length % 2 != 0){
            log.info("Error Expected Pairs. Will Not Proceed")
        }
        for(int i =0; i < args.length;i++){
            if(i % 2 == 0 ){
                TIF_FOLDER = args[i]
                PDF_FOLDERS = args[i+1]
                log.info("args${i+1}:$TIF_FOLDER")
                log.info("args${i+2}:$PDF_FOLDERS")
                Tally.tally(TIF_FOLDER, PDF_FOLDERS)
                GenericUtil.printReport()
            }
        }
    }

    static String tally(String tifFolder, String pdfFolder) {
        TallyPojo.resetTallyObj()
        File tifDirectory = new File(tifFolder)
        log.info("\nTally for folders in  ${tifDirectory} started")

        int index = 0
        List tifDirFiles = tifDirectory.listFiles()
        List pdfFiles = new File(pdfFolder).list({ d, f -> f ==~ /(?i).*.pdf/ } as FilenameFilter)


        for (File tifSubDirectory : tifDirFiles) {
            if (tifSubDirectory.isDirectory() && !inIgnoreList(tifSubDirectory)) {
                index++
                log.info("$index of ${tifDirFiles.size()})." +
                        "Tally for Tif Folder ${tifSubDirectory.name}")
                def tifs = tifSubDirectory.list({ d, f -> f ==~ /(?i).*.tif/ } as FilenameFilter)
                int tifCount = tifs.size()
                File pdfFile = new File(pdfFolder, tifSubDirectory.name + ".pdf")
                if (!pdfFile.exists()) {
                    log.info("****Error was never created\n ${pdfFile}");
                    TallyPojo.NOT_CREATED.push("'${tifSubDirectory.name}'");
                    continue;
                }
                try {
                    int pdfPageCount = PdfImageCounter.getPdfImageCount(pdfFile)
                    GenericUtil.garbageCollectAndPrintMemUsageInfo()
                    log.info("""${index}). Checking Tiff Count (${tifCount}) in 
                            ${GenericUtil.dualEllipsis(tifSubDirectory.name)} equals 
                            ${GenericUtil.dualEllipsis(pdfFile.name)} 
                            ${pdfPageCount}""");
                    if (pdfPageCount === tifCount) {
                        TallyPojo.MATCHING.push("'${pdfFile}");
                        GenericUtil.addReport("""pdf (${pdfPageCount}) 
    ${pdfFile.name} 
    Page Count ==  PNG Count
    ${(tifCount)}\n""");
                    } else {
                        if (pdfPageCount > 0) {
                            TallyPojo.NON_MATCHING.push("'${pdfFile}");
                        } else {
                            TallyPojo.UNCHECKABLE.push("""${pdfFile.name} 
                                        should have ${tifCount + INTRO_PAGE_ADJUSTMENT} pages""");
                        }
                        GenericUtil.addReport("""**** PDF Count  (${pdfPageCount})
                        for ${pdfFile} is not same as
                        ${tifCount}""");
                    }
                }
                catch (Exception e) {
                    log.info("getPdfPageCount Exception", e)
                    TallyPojo.EXCEPTION_ENCOUNTERED.push("'${pdfFile}")
                    continue
                }

            }
        }

        String finalReport = """Stats:
                    Pdf Folder: ${pdfFolder}
                    Tif Folder: ${tifFolder}                    
                    Total Tiff Folders expected for Conversion: ${tifDirFiles?.size()}
                    Total PDFs in Folder: ${pdfFiles?.size()}
                """

        GenericUtil.addReport(TallyPojo.genFinalReport(tifFolder,pdfFolder,tifDirFiles,pdfFiles))
        return finalReport
    }

    static boolean inIgnoreList(File file) {
        String absPath = file.absolutePath.toString()
        def invalid = ignoreList.findAll { ignorableKeyword ->
            absPath.containsIgnoreCase(ignorableKeyword)
        }
        return invalid?.size() > 0
    }
}