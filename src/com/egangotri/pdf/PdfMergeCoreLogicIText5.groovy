package com.egangotri.pdf

import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

@Slf4j
class PdfMergeCoreLogicIText5 {

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
            log.info("\t\tRenaming to ${finalPdf + "${EGangotriPDFMerger.OLD_LABEL}.pdf"}")
            new File(finalPdf).renameTo(finalPdf + "${EGangotriPDFMerger.OLD_LABEL}.pdf")
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
