package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

/**
 * Created by user on 4/7/2016.
 */
@Slf4j
class PDFMerger {
    static String ROOT_FOLDER = "E:\\ramtek_4_05-08-2019"
    static String PDFS_FOLDER = "pdfs"
    static String PDFS_MERGE_FOLDER = "_mergedPdfs"

    // class ItextMerge {
    static void main(String[] args) {
        try {
            if(args){
                ROOT_FOLDER = args[0]
            }
            File rootDir = new File(ROOT_FOLDER)
            File[] foldersWithPdf = GenericUtil.getDirectories(rootDir)
            int counter = 0
            for (File subFolder in foldersWithPdf) {
                counter++
                GenericUtil.addReport( "${counter}) Process folder \n ${GenericUtil.reverseEllipsis(subFolder)}")
                try{
                    mergeSmallerPdfs(subFolder)
                    processFinalMerge(subFolder)
                }
                catch(Exception e){
                    log.info("Error in Process Merge",e)
                    continue
                }
                System.gc()
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace()
        } catch (DocumentException e) {
            e.printStackTrace()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    static void mergeSmallerPdfs(File subFolder){
        File _pdfs = new File(subFolder, PDFS_FOLDER)
        File[] sorted_subFoldersInside_pdf  = GenericUtil.getDirectories(_pdfs)
        log.info("sorted folders inside $PDFS_FOLDER: \n${sorted_subFoldersInside_pdf.join("\n")}" )

        int counter = 0
        for (File pdfFolder in sorted_subFoldersInside_pdf) {
            File[] sorted_pdfFilesWithin = GenericUtil.getPdfs(pdfFolder)
            log.info("prelim Merge of sub-folders in  ${GenericUtil.reverseEllipsis(pdfFolder)}")
            File folderForDumping = new File(subFolder, PDFS_MERGE_FOLDER)
            if(!folderForDumping.exists()){
                folderForDumping.mkdir()
            }
            doMerge(sorted_pdfFilesWithin, folderForDumping.absolutePath + "\\" + pdfFolder.name + ".pdf")
        }
    }

    static void processFinalMerge(File subFolders){
        File[] pdfFiles = GenericUtil.getPdfs(new File(subFolders, PDFS_MERGE_FOLDER))
        log.info("processFinalMerge: \n" + pdfFiles.join("\n") )
        // Resulting pdf
        if(pdfFiles){
            String finalPdf = subFolders.getParentFile().getAbsolutePath() + "//" + subFolders.name + ".pdf"
            GenericUtil.addReport( "Final Merge to ${GenericUtil.reverseEllipsis(finalPdf)}")
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
        log.info("\t\tdoMerge for ${GenericUtil.reverseEllipsis(finalPdf)}")
        Document document = new Document()
        if(new File(finalPdf).exists()){
            log.info("\t\tdRenaming to ${finalPdf + "_old.pdf"}")
            new File(finalPdf).renameTo(finalPdf + "_old.pdf")
        }
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(finalPdf));
        document.open();

        for (File file : files){
            log.info("merging ${GenericUtil.reverseEllipsis(file)} into ${GenericUtil.reverseEllipsis(finalPdf)}")
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
