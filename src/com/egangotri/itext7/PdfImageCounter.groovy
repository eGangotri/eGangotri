package com.egangotri.itext7

import com.egangotri.pdf.Tally
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfReader
import groovy.util.logging.Slf4j

import com.egangotri.pdf.EGangotriPDFMerger

@Slf4j
class PdfImageCounter {

    static void main(String[] args) {
        log.info("ImageCount " + PdfImageCounter.getPdfImageCount("C:\\tmp\\expWithSmallFilesDest\\${EGangotriPDFMerger.FINAL_PDFS}\\Kalidas.pdf"))
    }

    static int getPdfImageCount(File pdfPath) {
        return getPdfImageCount(pdfPath.absolutePath)
    }

    static int getPdfImageCount(String pdfPath) {
        int imageCount = 0
        PdfDocument document = new PdfDocument(new PdfReader(pdfPath))
        int pageCount = document.getNumberOfPages()
        for (int i = 1; i <= pageCount; i++) {
            PdfPage page = (PdfPage) document.getPage(i)
            def resources = page.getResources()
            def resourceNames = resources?.getResourceNames()
            //log.info(" resources: ${i} " + resources)
           // log.info(" resourceNames: ${i} " + resourceNames + " " + resourceNames?.size())
            if (resourceNames) {
                for (int j = 0; j < resourceNames.size(); j++) {
                    def resourceName = resourceNames[j]
                    //log.info("resourceName: ${j} " + resourceName)
                    if (resourceName) {
                        try {
                            def image = resources?.getImage(resourceName)
                            //log.info("image: ${j} " + image)
                            if (image instanceof com.itextpdf.kernel.pdf.xobject.PdfImageXObject) {
                                imageCount++
                            }
                        }
                        catch (Exception e) {
                            log.error(e.message)
                        }
                    }
                }
            }
        }
        return imageCount - Tally.INTRO_PAGE_ADJUSTMENT;
    }
}
