package com.egangotri.pdf

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import groovy.util.logging.Slf4j
import javax.imageio.ImageIO

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
        mergeImagesToPdf(imageFolder, outputPdfPath)
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

    static void mergeImagesToPdf(String imageFolder, String outputPdfPath) {
        if (!outputPdfPath) {
            log.error("Invalid output path provided.")
            return
        }

        def imageFiles = new File(imageFolder).listFiles()
                .findAll { it.name.toLowerCase().endsWith('.jpg') || it.name.toLowerCase().endsWith('.jpeg') || it.name.toLowerCase().endsWith('.png') || it.name.toLowerCase().endsWith('.tif') || it.name.toLowerCase().endsWith('.tiff') }
                .sort()

        log.info("imageFiles: ${imageFiles?.size()}")
        if (imageFiles?.size() == 0) {
            log.error("No images found in the folder: ${imageFolder}")
            return
        }

        PdfDocument pdfDoc = null
        Document doc = null
        try {
            def pdfWriter = new PdfWriter(outputPdfPath)
            pdfDoc = new PdfDocument(pdfWriter)
            doc = new Document(pdfDoc)

            imageFiles.each { imageFile ->
                try {
                    def bufferedImage = ImageIO.read(imageFile)
                    if (bufferedImage == null) {
                        throw new IOException("Could not read image: ${imageFile.absolutePath}")
                    }

                    def imageData = ImageDataFactory.create(imageFile.absolutePath)
                    def img = new Image(imageData)

                    def pageWidth = bufferedImage.getWidth()
                    def pageHeight = bufferedImage.getHeight()

                    // Add a new page with the correct size for each image
                    pdfDoc.addNewPage(new PageSize(pageWidth, pageHeight))

                    // Place the image at the full page size
                    img.setFixedPosition(0, 0)
                    img.scaleToFit(pageWidth, pageHeight)

                    doc.add(img)
                } catch (Exception e) {
                    println "Error processing image ${imageFile.name}: ${e.message}"
                }
            }
            println "PDF created successfully at: ${outputPdfPath}"

        } catch (Exception e) {
            println "Failed to create PDF: ${e.message}"
        } finally {
            doc?.close()
            pdfDoc?.close()
        }
    }
}
