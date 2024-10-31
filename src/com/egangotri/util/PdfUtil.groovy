package com.egangotri.util

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader

class PdfUtil {
    static String NMM_PATH = "D:\\NMM\\"
    static String extractTiffFolderName(File pdfFolder){
        String[] _splitBy =  pdfFolder.name.split("_")
        String extractedDate = (_splitBy?.size()> 1 && _splitBy[1].size() > 10) ? _splitBy[1].substring(0,10) : ""

        String parentFileName = pdfFolder.getParentFile().name
        if(parentFileName.contains("_(")){
            String[] prefix = parentFileName.split("_\\(")
            parentFileName = prefix[0]
        }

        String tiffFolderPath = "${NMM_PATH}${parentFileName}\\${extractedDate}"
        if(extractedDate && new File(tiffFolderPath).exists()){
            return tiffFolderPath;
        }
        return null
    }
    static int countPages(String pdfPath) {
        PdfDocument pdfDoc = null
        try {
            pdfDoc = new PdfDocument(new PdfReader(pdfPath))
            return pdfDoc.getNumberOfPages()
        } catch (Exception e) {
            println "Error counting pages in PDF: ${e.message}"
            e.printStackTrace()
            return -1
        } finally {
            pdfDoc?.close()
        }
    }
}