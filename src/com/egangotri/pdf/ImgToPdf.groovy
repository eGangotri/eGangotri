package com.egangotri.pdf

import groovy.util.logging.Slf4j
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.image.ImageData

@Slf4j
class ImgToPdf {
    static void main(String[] args) {
        String imageFolder = args[0]
        String folderName = new File(imageFolder).getName()
        String outputPdfPath = createOutputPdfName(imageFolder)

        if (!outputPdfPath) {
            log.error("Unable to create a unique PDF file name.")
            return
        }

        log.info("""Processing: folderName: ${folderName}
                outputPdfPath: ${outputPdfPath}""")
        ImgToPdfUtil.convertImagesToPdf(imageFolder, outputPdfPath)
    }

    static String createOutputPdfName(String imageFolder) {
        String folderName = new File(imageFolder).getName()
        String outputPdfPath = "${imageFolder}/${folderName}.pdf"
        File outputPdfFile = new File(outputPdfPath)

        def baseName = outputPdfFile.getName().replaceAll(/\.pdf$/, "")
        def outputDir = outputPdfFile.getParent() ?: "."
        def attempt = 0

        while (outputPdfFile.exists() && attempt < 5) {
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




