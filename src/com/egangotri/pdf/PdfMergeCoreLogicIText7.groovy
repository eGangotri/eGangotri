package com.egangotri.pdf

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document
import groovy.util.logging.Slf4j;

@Slf4j
class PdfMergeCoreLogicIText7 {
    static void doMerge(File[] files, String finalPdf){
        PdfDocument pdf = new PdfDocument(new PdfWriter(finalPdf));
        PdfMerger merger = new PdfMerger(pdf);

        //Add pages from the first document
        files.each { File mergeable -> {
            PdfDocument mergeableAsPdfDoc = new PdfDocument(new PdfReader(mergeable));
            merger.merge(mergeableAsPdfDoc, 1, mergeableAsPdfDoc.getNumberOfPages());
            mergeableAsPdfDoc.close()
        }}
        pdf.close()
    }
}
