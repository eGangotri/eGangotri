
package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import com.egangotri.util.PdfUtil
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
    public static final MEGA_TYPE = 'mega'

    static void main(String[] args) {
        List<String> _mergeables = []
        if (args) {
            if (args.length == 2 && args[1] == MEGA_TYPE) {
                _mergeables = GenericUtil.getDirectories(args[0])*.absolutePath
                GenericUtil.addReport("""MegaMerge for Folders:\n${_mergeables.join("\n")}  
                started""")
            } else _mergeables = args;
            int counter = 0
            for (String _mergeable : _mergeables) {
                counter++
                GenericUtil.addReport("""Merge for 
                    Folder:
${_mergeable}
                         ${counter} of ${_mergeables.size()} 
for ${_mergeable} started""")
                exec(_mergeable)
            }
        } else {
            exec(ROOT_FOLDER)
        }
        Tally.printMEGA_TALLY_OBJECT()
    }

    static void exec(String _mergeable) {
        try {
            File rootDir = new File(_mergeable)
            File[] foldersWithPdf = GenericUtil.getDirectories(rootDir)
            Date startTime = new Date()
            GenericUtil.addReport("""*Merge started @ ${startTime} for 
${rootDir.name}
 for ${foldersWithPdf?.size()} Folder(s) :
 ${foldersWithPdf.collect { it.name }.join("\n\t")}
            started""")
            int counter = 0
            for (File subFolder in foldersWithPdf) {
                counter++
                GenericUtil.addReport("${counter} of ${foldersWithPdf.length}) Process folder \n ${subFolder.name}")
                try {
                    mergeSmallerPdfs(subFolder)
                    GenericUtil.garbageCollectAndPrintMemUsageInfo()
                    mergeFinalPdf(subFolder)
                    GenericUtil.garbageCollectAndPrintMemUsageInfo()
                }

                catch (Exception e) {
                    log.info("Error in Process Merge", e)
                    continue
                }
                GenericUtil.garbageCollectAndPrintMemUsageInfo()
            }
            Date endTime = new Date()
            GenericUtil.addReport("Merge finishes ${endTime}. Time Taken: ${TimeUtil.formattedTimeDff(endTime, startTime)}")
            tallyPostMerge(rootDir)

        } catch (Exception e) {
            log.info("Exception in merge.outer ", e)
        }
        GenericUtil.printReport()
    }

    static void mergeSmallerPdfs(File subFolder) {
        File[] _pdfs = GenericUtil.getDirectoriesSortedByName(new File(subFolder, PDFS_FOLDER))
        //log.info("mergeSmallerPdfs sorted folders inside $PDFS_FOLDER: \n${_pdfs?.join("\n")}" )

        int counter = 0
        for (File pdfFolder in _pdfs) {
            File[] _pdfFilesWithin = GenericUtil.getPdfsSortedByName(pdfFolder)
            //log.info("prelim Merge of sub-folders in  ${GenericUtil.reverseEllipsis(pdfFolder)} \n  ${_pdfFilesWithin?.join("\n")}")
            File folderForDumping = new File(subFolder, PDFS_MERGE_FOLDER)
            if (!folderForDumping.exists()) {
                folderForDumping.mkdir()
            }
            PdfMergeCoreLogicIText5.doMerge(_pdfFilesWithin, folderForDumping.absolutePath + "\\" + pdfFolder.name + ".pdf")
        }
    }

    static void mergeFinalPdf(File subFolders) {
        File[] pdfFiles = GenericUtil.getPdfsSortedByName(new File(subFolders, PDFS_MERGE_FOLDER))
        //log.info("pdfFiles ${pdfFiles.join("\n")}")
        if (pdfFiles) {
            String finalPdfDumpFolder = subFolders.getParentFile().getAbsolutePath() + "//${FINAL_PDFS}//"
            if (!new File(finalPdfDumpFolder).exists()) {
                new File(finalPdfDumpFolder).mkdir()
            }
            String finalPdf = finalPdfDumpFolder + subFolders.name + ".pdf"
            GenericUtil.addReport("Final Merge to ${GenericUtil.ellipsis(subFolders.name)}..${GenericUtil.reverseEllipsis(finalPdf)}")
            PdfMergeCoreLogicIText5.doMerge(pdfFiles, finalPdf)
        }
    }

    static void tallyPostMerge(File pdfFolder) {
        try {
            String tiffFolderPath = PdfUtil.extractTiffFolderName(pdfFolder)
            String finalPdfsPath = "${pdfFolder}\\${FINAL_PDFS}"
            if (tiffFolderPath) {
                log.info("Tally ${tiffFolderPath} against ${finalPdfsPath}")
                Tally.main(tiffFolderPath, finalPdfsPath)
            } else {
                log.info("cant Tally ${tiffFolderPath} against ${finalPdfsPath}")
            }
        }
        catch (Exception e) {
            log.info("Exception in post merge tally", e)
        }
    }
}