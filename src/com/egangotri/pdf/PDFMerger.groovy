package com.egangotri.pdf

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import groovy.util.logging.Slf4j

/**
 * Created by user on 4/7/2016.
 */
@Slf4j
class PDFMerger {
    static String ROOT_FOLDER = "E:\\ramtek_4_05-08-2019"
    // class ItextMerge {
    static void main(String[] args) {
        try {
            if(args){
                ROOT_FOLDER = args[0]
            }
            File rootDir = new File(ROOT_FOLDER)
            File[] foldersWithPdf = rootDir.listFiles({ d, f -> d.isDirectory()} as FilenameFilter);
            int counter = 0
            for (File subFolder in foldersWithPdf) {
                counter++
                log.info "${counter})subFolder $subFolder"
                try{
                    mergeStep1(subFolder)
                    processMerge(subFolder)
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

    static void mergeStep1(File subFolder){
        File _pdfs = new File(subFolder, "pdfs")
        File[] subFoldersInside_pdf = _pdfs.listFiles({ d, f -> d.isDirectory()} as FilenameFilter);
        int counter = 0
        log.info("_pdfs ${_pdfs.absolutePath}")
        for (File pdfFolder in subFoldersInside_pdf) {
            def pdfFilesWithin = pdfFolder.listFiles({ d, f -> f ==~ /(?i).*.pdf/ } as FilenameFilter)
            log.info("pdfFolder ${pdfFolder.absolutePath}")
            doMerge(pdfFilesWithin, pdfFolder.absolutePath + ".pdf")
        }
    }

    static void processMerge(File subFolder){
        File[] pdfFiles = new File(subFolder, "pdfs").listFiles({ d, f -> f ==~ /(?i).*.pdf/ } as FilenameFilter)
        // Resulting pdf
        if(pdfFiles){
            String finalPdf = subFolder.getParentFile().getAbsolutePath() + "//" + subFolder.name + ".pdf"
            log.info "Final PdfName ${finalPdf}"
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
        log.info("doMerge for ${finalPdf}")
        Document document = new Document()
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(finalPdf));
        document.open();

        for (def file : files){
            log.info("merging ${file} into ${finalPdf}")
            PdfReader reader = new PdfReader(new FileInputStream(file));
            copy.addDocument(reader);
            copy.freeReader(reader);
            reader.close();
        }
        document.close();
    }
    // }
}
