package com.egangotri.util

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader

class PdfUtil {
    static PDF_RENAME_ATTEMPTS = 5
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
        try (PdfReader reader = new PdfReader(pdfPath);
             PdfDocument pdfDoc = new PdfDocument(reader)) {
            return pdfDoc.getNumberOfPages()
        } catch (Exception e) {
            println "Error counting pages in PDF: ${e.message}"
            e.printStackTrace()
            return -1
        }
    }

    static File outputPdfFilePerFormat(String dirAbsPath){
        String folderName = new File(dirAbsPath).getName()
        String outputPdfPath = "${dirAbsPath}/${folderName}.pdf"
        File outputPdfFile = new File(outputPdfPath)
        return outputPdfFile;
    }
    static String createOutputPdfName(String dirAbsPath) {
        File outputPdfFile = outputPdfFilePerFormat(dirAbsPath)

        def baseName = outputPdfFile.getName().replaceAll(/\.pdf$/, "")
        def outputDir = outputPdfFile.getParent() ?: "."
        def attempt = 0

        while (outputPdfFile.exists() && attempt < PdfUtil.PDF_RENAME_ATTEMPTS) {
            attempt++
            outputPdfFile = new File("${outputDir}/${baseName}_${attempt}.pdf")
        }

        if (outputPdfFile.exists()) {
            println "Failed to create a unique file name for output PDF after 5 attempts."
            return ""
        }
        return outputPdfFile.getAbsolutePath()
    }
}


