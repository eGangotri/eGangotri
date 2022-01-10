package com.egangotri.pdf

import com.egangotri.util.GenericUtil
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger;
import groovy.util.logging.Slf4j;

@Slf4j
@Deprecated
class PdfMergeCoreLogicIText7 {
    static String ROOT_FOLDER = "D:\\Treasures35\\_freeze\\Vicky\\April 2021\\en"

    static void main(String[] args) {
        File pdfFolder = new File(ROOT_FOLDER)
        File[] mergeablePdfs = GenericUtil.getPdfs(pdfFolder)
        doMerge(mergeablePdfs, "${ROOT_FOLDER}\\finaPdf4.pdf")
    }
    /***
     * dont use. messes the page order 1-10-11...2-21..3..
     * gives OutOfMemoryError
     * @param mergeablePdfs
     * @param finalPdf
     */
    static void doMerge(File[] mergeablePdfs, String finalPdf){
        if(new File(finalPdf).exists()){
            log.info("\t\tRenaming to ${finalPdf + "${EGangotriPDFMerger.OLD_LABEL}.pdf"}")
            new File(finalPdf).renameTo(finalPdf + "${EGangotriPDFMerger.OLD_LABEL}.pdf")
        }

        PdfDocument pdf = new PdfDocument(new PdfWriter(finalPdf));
        PdfMerger merger = new PdfMerger(pdf);

        //Add pages from the first document
        mergeablePdfs.each { File mergeable -> {
            PdfDocument mergeableAsPdfDoc = new PdfDocument(new PdfReader(mergeable));
            merger.merge(mergeableAsPdfDoc, 1, mergeableAsPdfDoc.getNumberOfPages());
            mergeableAsPdfDoc.close()
        }}
        pdf.close()
    }
}
