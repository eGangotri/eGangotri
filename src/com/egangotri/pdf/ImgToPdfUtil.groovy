package com.egangotri.pdf

import com.egangotri.util.EGangotriUtil
import com.egangotri.util.PdfUtil
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat

@Slf4j
class ImgToPdfUtil {
    static def convertImagesToPdf(File imgFolder, String outputPdf, String imgType = "ANY") {
        log.info(":")
//        Map<String,Object> returnType = [:]
//        returnType.put("imgFolder", imgFolder)
//        returnType.put("outputPdf", outputPdf)
        File[] imageFiles = getImageFiles(imgFolder.absolutePath, imgType)
       // returnType.put("imgCount", imageFiles.length)
        log.info "Processing: ${imageFiles.length} images into pdf."
        if (imageFiles.length == 0) {
            log.error "No images found in folder: ${imgFolder.absolutePath}"
        //    return returnType
        }
        // Create a new PDF document
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputPdf))
        Document doc = null

        try {
            // Create a new Document for adding content
            doc = new Document(pdfDoc)
            for (int i = 0; i < imageFiles.length; i++) {
                File file = imageFiles[i];
                if (file.isFile()) {
                    try {
                        // Load image data from file
                        ImageData imageData = ImageDataFactory.create(file.absolutePath)
                        // Get image dimensions directly from ImageData
                        float imageWidth = imageData.getWidth();
                        float imageHeight = imageData.getHeight();

                        // Set the page size on the PdfDocument object
                        pdfDoc.setDefaultPageSize(new PageSize(imageWidth, imageHeight))

                        // Create an image object
                        Image image = new Image(imageData);

                        // Add image to the PDF document
                        doc.add(image);

                        // Start a new page for the next image
                        if (i < imageFiles.length - 1) {
                            doc.add(new AreaBreak())
                        } else {
                            log.info "All ${imageFiles.length} images added to PDF(${outputPdf})."
                        }
                    } catch (Exception e) {
                        println "Error converting image '${file.name}' to PDF: ${e.message}"
                        e.printStackTrace()
                     //   returnType.put("error", e.message)
//                        return returnType
                    }
                }
            }
        } catch (Exception e) {
            println "Error converting images to PDF: ${e.message}"
            e.printStackTrace()
          //  returnType.put("error", e.message)
  //          return returnType
        }
        finally {
            // Close the Document (important for iText 8)
            doc?.close()
            // Close the PDF document (important for iText 8)
            pdfDoc?.close()
        }
        int pageCount = PdfUtil.countPages(outputPdf)
      //  returnType.put("pdfPageCount", pageCount)
        //returnType.put("success", pageCount === imageFiles.length)
    //    return returnType
    }

    static getExtraCondition(File file, String imgType) {
        Boolean extraCondition = false;
        switch (imgType) {
            case "PNG":
                extraCondition = file.name.toLowerCase().endsWith("png");
                break;
            case "JPG":
                extraCondition = file.name.toLowerCase().endsWith("jpg") || file.name.toLowerCase().endsWith("jpeg");
                break;
            case "TIF":
                extraCondition = file.name.toLowerCase().endsWith("tiff") || file.name.toLowerCase().endsWith("tif");
                break;
            default:
                extraCondition = List.of("jpg", "jpeg", "png", "tif", "tiff")
                        .stream()
                        .anyMatch(ext -> file.getName().toLowerCase().endsWith("." + ext));

                break;
        }
        return extraCondition
    }
    static File[] getImageFiles(String _imgFolder, String imgType) {

        File folder = new File(_imgFolder)
        if (!folder.exists()) {
            log.error("Image folder does not exist: ${_imgFolder}")
            return []
        }
        File[] imageFiles = folder.listFiles(
                { file -> getExtraCondition(file, imgType) } as FileFilter)
        if (imageFiles.length == 0) {
            log.error("No image files found in folder: ${_imgFolder}")
            return []
        }
        return imageFiles
    }
}

