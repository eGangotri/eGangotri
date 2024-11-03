package com.egangotri.pdf

import com.egangotri.itext7.PdfImageCounter
import com.egangotri.util.GenericUtil
import com.egangotri.util.PdfUtil
import groovy.util.logging.Slf4j

@Slf4j
class Tally {

    static String TIF_FOLDER = "E:\\ramtek211Dec\\"
    static String PDF_FOLDERS = "E:\\ramtek211Dec\\"
    static List ignoreList = ['otro']
    static String PDF = "pdf"
    static int INTRO_PAGE_ADJUSTMENT = 1
    static final MEGA_TYPE = 'mega'
    static TALLY_RUN_SUCCESS_COUNT = 0
    static ArrayList<TallyPojo> MEGA_TALLY_OBJECT = []

    static void main(String[] args) {
        if(args && args.length > 1 && args.length % 2 != 0){
            log.info("Error Expected Pairs. Will Not Proceed")
            System.exit(0)
        }
        if(args[1] == MEGA_TYPE){
            File dest = new File(args[0]);
            if(!dest.exists()){
                log.error("No Such folder ${args[0]}")
            }
            String src = "${PdfUtil.NMM_PATH}${dest.name}"
            log.info("Mega Tally for ${src} ${dest} started @ ${new Date()}") //
            log.info("MMake sure yarn run move-merged-pdfs has been used to flatten the files before running mega-tally") // yarn run move-merged-pdfs
            MegaTally.execute(dest.absolutePath)
            int dirCount = GenericUtil.getDirectories(new File(src)).length
            String finalSuccessMsg =
                    (TALLY_RUN_SUCCESS_COUNT == dirCount) ? "100-% Success (${TALLY_RUN_SUCCESS_COUNT} == ${dirCount})" :
                    "Failures: ${TALLY_RUN_SUCCESS_COUNT - dirCount} of ${dirCount}"
            log.info("Mega Tally for ${src} ${dest} ended @ ${new Date()} with ${finalSuccessMsg}")
            printMEGA_TALLY_OBJECT()
        }
        else {
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
    }

    static String tally(String tifFolder, String pdfFolder) {
        TallyPojo tallyPojo = new TallyPojo()
        File tifDirectory = new File(tifFolder)
        log.info("\nTally for folders in  ${tifDirectory} started")

        int index = 0
        List tifDirFiles = tifDirectory.listFiles()
        List pdfFiles = new File(pdfFolder).list({ d, f -> f ==~ /(?i).*.pdf/ } as FilenameFilter)


        for (File tifSubDirectory : tifDirFiles) {
            if (tifSubDirectory.isDirectory() && !inIgnoreList(tifSubDirectory)) {
                index++
                log.info("$index of ${tifDirFiles.size()})." +
                        "Tally for Tif Folder '${tifSubDirectory.name}'")
                def tifs = tifSubDirectory.list({ d, f -> f ==~ /(?i).*.tif/ } as FilenameFilter)
                int tifCount = tifs.size()
                File pdfFile = new File(pdfFolder, tifSubDirectory.name + ".pdf")
                if (!pdfFile.exists()) {
                    log.info("****Error was never created\n ${pdfFile}");
                    tallyPojo.NOT_CREATED.push("'${tifSubDirectory.name}'");
                    continue;
                }
                try {
                    tallyItem(tifSubDirectory, pdfFile, tifCount, index, tallyPojo)
                }
                catch (Exception e) {
                    log.info("getPdfPageCount Exception", e)
                    tallyPojo.EXCEPTION_ENCOUNTERED.push("'${pdfFile}")
                    continue
                }

            }
        }
        return tallyReport(tifFolder,pdfFolder,tifDirFiles,pdfFiles,tallyPojo)
    }

    static String tallyReport(String tifFolder,String pdfFolder,List tifDirFiles,List pdfFiles, TallyPojo tallyPojo){
        int tifDirFilesSize = tifDirFiles?.size()?:0
        if(tifDirFilesSize){
            boolean TALLY_RUN_SUCCESS = (tifDirFilesSize == (tallyPojo.MATCHING?.size() ?:0))
            String successMsg = TALLY_RUN_SUCCESS?"100% Success (${tifDirFilesSize} == ${tallyPojo.MATCHING.size()})":
                    "Failure of: ${tifDirFilesSize - tallyPojo.MATCHING.size()} Item(s)"
            String finalReport = """Stats:
                    Pdf Folder: ${pdfFolder}
                    Tif Folder: ${tifFolder}                    
                    Total Tiff Folders expected for Conversion: ${tifDirFilesSize}
                    Total PDFs in Folder: ${pdfFiles?.size()}
                    Match Count: ${successMsg}
                """
            if(TALLY_RUN_SUCCESS){
                TALLY_RUN_SUCCESS_COUNT++
            }
            GenericUtil.addReport(tallyPojo.genFinalReport(tifFolder,pdfFolder,tifDirFiles,pdfFiles))
            MEGA_TALLY_OBJECT.add(tallyPojo)
            return finalReport
        }
        return "Fatal Error. No Files in Tiff FOlder or Tiff Folder Not found ${tifFolder}"
    }
    static String tallyItem(File tifSubDirectory ,File pdfFile, int tifCount, int index, TallyPojo tallyPojo){
        int pdfPageCount = PdfImageCounter.getPdfImageCount(pdfFile)
        GenericUtil.garbageCollectAndPrintMemUsageInfo()
        log.info("""${index}). Checking Tiff Count (${tifCount}) in 
                            ${GenericUtil.dualEllipsis(tifSubDirectory.name)} equals 
                            ${GenericUtil.dualEllipsis(pdfFile.name)} 
                            ${pdfPageCount}""");
        if (pdfPageCount == tifCount) {
            tallyPojo.MATCHING.push("'${pdfFile}");
            GenericUtil.addReport("""pdf (${pdfPageCount}) 
    ${pdfFile.name} 
    Page Count ==  PNG Count
    ${(tifCount)}\n""");
        } else {
            if (pdfPageCount > 0) {
                tallyPojo.NON_MATCHING.push("'${pdfFile}");
            } else {
                tallyPojo.UNCHECKABLE.push("""${pdfFile.name} 
                                        should have ${tifCount + INTRO_PAGE_ADJUSTMENT} pages""");
            }
            GenericUtil.addReport("""**** PDF Count  (${pdfPageCount})
                        for ${pdfFile} is not same as
                        ${tifCount}""");
        }
    }


    static boolean inIgnoreList(File file) {
        String absPath = file.absolutePath.toString()
        def invalid = ignoreList.findAll { ignorableKeyword ->
            absPath.containsIgnoreCase(ignorableKeyword)
        }
        return invalid?.size() > 0
    }

    static void printMEGA_TALLY_OBJECT(){
        MEGA_TALLY_OBJECT?.each({ TallyPojo tp ->{
            if(tp.ERROR_MARGIN !=0 ){
                log.info("Tally Error Detected*** ${tp.TIF_FOLDER} has ErrorMargin ${tp.ERROR_MARGIN}")
            }
        }})
    log.info("""Final Mega Tally Report 
                   Cumulative Error Margin ${MEGA_TALLY_OBJECT*.ERROR_MARGIN.sum()}
""")
    }
}