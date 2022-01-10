
package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import com.egangotri.util.TimeUtil
import groovy.util.logging.Slf4j

@Slf4j
class EGangotriPDFMerger {
    static String ROOT_FOLDER = "C:\\tmp\\pdfForMergeTest"
    static String PDFS_FOLDER = "pdfs"
    static String PDFS_MERGE_FOLDER = "_mergedPdfsTmpLoc"
    static String FINAL_PDFS = "finalPdfsTmpLoc"
    static String PRE_FINAL_PDFS = "pre_finalPdfsTmpLoc"
    static String OLD_LABEL = "_old_disc"
    public static final int CHUNKING_THRESHOLD = 50

    static void main(String[] args) {
        List<String> _mergeables = []
        if(args){
            if(args.length == 2 && args[1] == 'mega'){
                _mergeables = GenericUtil.getDirectoriesSortedByName(args[0])*.absolutePath
                GenericUtil.addReport("""MegaMerge for Folders:\n${_mergeables.join("\n")}  
                started""")
            }
            else _mergeables = args;
            int counter = 0
            for(String _mergeable: _mergeables){
                counter++
                GenericUtil.addReport("Merge for Folder(${_mergeable})\n ${counter} of ${_mergeables.size()} for ${_mergeable} started")
                exec(_mergeable)
            }
        }
       else{
            exec(ROOT_FOLDER)
        }
    }

    static void exec(String _mergeable){
        try {
            File rootDir = new File(_mergeable)
            File[] foldersWithPdf = GenericUtil.getDirectories(rootDir)
            Date startTime = new Date()
            GenericUtil.addReport("""Merge started @ ${startTime} for 
${rootDir.name} for ${foldersWithPdf.size()} Folder(s) :
            ${foldersWithPdf.collect{it.name}.join("\n\t")}
            started""")
            int counter = 0
            for (File subFolder in foldersWithPdf) {
                counter++
                GenericUtil.addReport( "${counter} of ${foldersWithPdf.length}) Process folder \n ${subFolder.name}")
                try{
                    mergeSmallerPdfs(subFolder)
                    GenericUtil.garbageCollectAndPrintMemUsageInfo()
                    mergeFinalPdf(subFolder)
                    GenericUtil.garbageCollectAndPrintMemUsageInfo()
                }

                catch(Exception e){
                    log.info("Error in Process Merge",e)
                    continue
                }
                GenericUtil.garbageCollectAndPrintMemUsageInfo()
            }
            Date endTime = new Date()
            GenericUtil.addReport("Merge finishes ${endTime}. Time Taken: ${TimeUtil.formattedTimeDff(endTime,startTime)}")
            experimentalTally(rootDir)

        } catch (Exception e) {
            log.info("Exception in merge.outer ",e)
        }
        GenericUtil.printReport()
    }

    static void mergeSmallerPdfs(File subFolder) {
        File[] _pdfs = GenericUtil.getDirectories(new File(subFolder, PDFS_FOLDER))
        log.info("mergeSmallerPdfs sorted folders inside $PDFS_FOLDER: \n${_pdfs?.join("\n")}" )

        int counter = 0
        for (File pdfFolder in _pdfs) {
            File[] _pdfFilesWithin = GenericUtil.getPdfs(pdfFolder)
            //log.info("prelim Merge of sub-folders in  ${GenericUtil.reverseEllipsis(pdfFolder)}")
            File folderForDumping = new File(subFolder, PDFS_MERGE_FOLDER)
            if (!folderForDumping.exists()) {
                folderForDumping.mkdir()
            }
            PdfMergeCoreLogicIText5.doMerge(_pdfFilesWithin, folderForDumping.absolutePath + "\\" + pdfFolder.name + ".pdf")
        }
    }

    static void mergeFinalPdf(File subFolders){
        File[] pdfFiles = GenericUtil.getPdfs(new File(subFolders, PDFS_MERGE_FOLDER))
        //If more than 50 files than dont merge them in one shot. can get a memry exception
//        if(pdfFiles?.size()> CHUNKING_THRESHOLD){
//            String preFinalDumpFolder =  subFolders.getParentFile().getAbsolutePath() + "//${PRE_FINAL_PDFS}//"
//            if(!new File(preFinalDumpFolder).exists()){
//                new File(preFinalDumpFolder).mkdir()
//            }
//            log.info("preFinalDumpFolder ${preFinalDumpFolder}")
//            int counter = 0;
//            List chunkedPdfs = pdfFiles.collate(CHUNKING_THRESHOLD)
//            for(def chunkedPdf in chunkedPdfs){
//                counter++
//
//                String preFinalPdf = "${preFinalDumpFolder}-${counter}-.pdf"
//                log.info("chunkedPdf size ${chunkedPdf.size()} \n preFinalPdf:${preFinalPdf}")
//
//                GenericUtil.addReport( "Pre-Final Merge to ${GenericUtil.ellipsis(subFolders.name)}..${GenericUtil.reverseEllipsis(preFinalPdf)}")
//                PdfMergeCoreLogicIText5.doMerge(chunkedPdf as File[], preFinalPdf)
//            }
//            pdfFiles = GenericUtil.getPdfs(new File(subFolders.getParentFile(), PRE_FINAL_PDFS))
//        }
       log.info("processFinalMerge: ${pdfFiles.join("\n\t")}")
        // Resulting pdf
        if(pdfFiles){
            String finalPdfDumpFolder =  subFolders.getParentFile().getAbsolutePath() + "//${FINAL_PDFS}//"
            if(!new File(finalPdfDumpFolder).exists()){
                new File(finalPdfDumpFolder).mkdir()
            }
            String finalPdf = finalPdfDumpFolder + subFolders.name + ".pdf"
            GenericUtil.addReport( "Final Merge to ${GenericUtil.ellipsis(subFolders.name)}..${GenericUtil.reverseEllipsis(finalPdf)}")
            PdfMergeCoreLogicIText5.doMerge(pdfFiles, finalPdf)
        }
    }

    static void experimentalTally(File pdfFolder){
        String[] _splitBy =  pdfFolder.name.split("_")
        log.info("_splitBy ${_splitBy} ${_splitBy?.size()}  ${_splitBy?.size()> 1}")
        String extractDate = (_splitBy?.size()> 1 && _splitBy[1].size() > 10) ? _splitBy[1].substring(0,10) : ""
        String tiffFolderPath = "D:\\NMM\\${pdfFolder.getParentFile().name}\\${extractDate}"
        String finalPdfsPath = "${pdfFolder}\\${FINAL_PDFS}"
        if(extractDate && new File(tiffFolderPath).exists() && new File(finalPdfsPath).exists()){
            log.info("Tally ${tiffFolderPath} against ${finalPdfsPath}")
            Tally.main(tiffFolderPath, finalPdfsPath)
        }
        else{
            log.info("cant Tally ${tiffFolderPath} against ${finalPdfsPath}")

        }
    }
}
