package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import com.egangotri.util.TimeUtil
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

@Slf4j
class PDFMerger {
    static String ROOT_FOLDER = ""
    static String PDFS_FOLDER = "pdfs"
    static String PDFS_MERGE_FOLDER = "_mergedPdfs"
    static String OLD_LABEL = "_old_disc"

    // class ItextMerge {
    static void main(String[] args) {
        List<String> _mergeables = []
        if(args){
            if(args.length == 2 && args[1] == 'mega'){
                _mergeables = GenericUtil.getDirectoriesSortedByName(args[0])*.absolutePath
            }
            else _mergeables = args;
            int counter = 0
            for(String _mergeable: _mergeables){
                counter++
                GenericUtil.addReport("Merge for Folder ${counter} of ${_mergeables.size()} for ${_mergeable} started")
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
            GenericUtil.addReport("""MegaMerge started @ ${startTime} for  ${rootDir.name} into Folder(s) :
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
                System.gc()
            }
            Date endTime = new Date()
            GenericUtil.addReport("Mega Merge finishes ${endTime}. Time Taken: ${TimeUtil.formattedTimeDff(endTime,startTime)}")

        } catch (FileNotFoundException e) {
            e.printStackTrace()
        } catch (DocumentException e) {
            e.printStackTrace()
        } catch (IOException e) {
            e.printStackTrace()
        }
        GenericUtil.printReport()
    }

    static void mergeSmallerPdfs(File subFolder){
        File[] _pdfs  = GenericUtil.getDirectories(new File(subFolder, PDFS_FOLDER))
        //log.info("sorted folders inside $PDFS_FOLDER: \n${_pdfs.join("\n")}" )

        int counter = 0
        for (File pdfFolder in _pdfs) {
            File[] _pdfFilesWithin = GenericUtil.getPdfs(pdfFolder)
            //log.info("prelim Merge of sub-folders in  ${GenericUtil.reverseEllipsis(pdfFolder)}")
            File folderForDumping = new File(subFolder, PDFS_MERGE_FOLDER)
            if(!folderForDumping.exists()){
                folderForDumping.mkdir()
            }
            doMerge(_pdfFilesWithin, folderForDumping.absolutePath + "\\" + pdfFolder.name + ".pdf")
        }
    }

    static void mergeFinalPdf(File subFolders){
        File[] pdfFiles = GenericUtil.getPdfs(new File(subFolders, PDFS_MERGE_FOLDER))

        log.info("processFinalMerge:")
        // Resulting pdf
        if(pdfFiles){
            String finalPdfDumpFolder =  subFolders.getParentFile().getAbsolutePath() + "//finalPdfs//"
            if(!new File(finalPdfDumpFolder).exists()){
                new File(finalPdfDumpFolder).mkdir()
            }
            String finalPdf = finalPdfDumpFolder + subFolders.name + ".pdf"
            GenericUtil.addReport( "Final Merge to ${GenericUtil.ellipsis(subFolders.name)}..${GenericUtil.reverseEllipsis(finalPdf)}")
            doMerge(pdfFiles, finalPdf)
        }
    }
    /**
     * Merge multiple pdf into one pdf
     *
     * @param list
     *            of pdf input stream
     * @param outputStream
     *            output file output stream
     */
    static void doMerge(File[] files, String finalPdf)
            throws DocumentException, IOException {
        //log.info("\t\tdoMerge for ${GenericUtil.reverseEllipsis(finalPdf)}")
        Document document = new Document()
        if(new File(finalPdf).exists()){
            log.info("\t\tdRenaming to ${finalPdf + "${OLD_LABEL}.pdf"}")
            new File(finalPdf).renameTo(finalPdf + "${OLD_LABEL}.pdf")
        }
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(finalPdf));
        document.open();

        for (File file : files){
            //log.info("merging ${GenericUtil.reverseEllipsis(file)} into ${GenericUtil.reverseEllipsis(finalPdf)}")
            PdfReader reader = new PdfReader(new FileInputStream(file));
            copy.addDocument(reader);
            copy.freeReader(reader);
            reader.close();
        }
        if(document.isOpen()){
            document.close()
        };
    }
    // }
}
