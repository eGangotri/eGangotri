package com.egangotri.pdf

import com.egangotri.util.EGangotriUtil
import com.egangotri.util.GenericUtil
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

@Slf4j
class ImgToPdfUtil {
    static String IMG_TYPE_ANY = "ANY";
    static Map<String,Object> convertImagesToPdf(File imgFolder, String outputPdf, String imgType = IMG_TYPE_ANY) {
        Map<String,Object> resultsMap = [:]
        resultsMap.put("imgFolder", imgFolder.absolutePath)
        resultsMap.put("outputPdf", outputPdf)
        File[] imageFiles = getImageFiles(imgFolder.absolutePath, imgType)
        resultsMap.put("imgCount", imageFiles.length as Integer)
        log.info "Processing: ${imageFiles.length} images into pdf."
        if (imageFiles.length == 0) {
            log.error "No images found in folder: ${imgFolder.absolutePath}"
            resultsMap.put("0Images", true);
            return resultsMap
        }
        // Create a new PDF document
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new BufferedOutputStream(new FileOutputStream(outputPdf))))
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
                        imageData = null
                        image = null
                        if((i+1)%100==0){
                            GenericUtil.garbageCollectAndPrintMemUsageInfo()
                        }
                        // Start a new page for the next image
                        if (i < imageFiles.length - 1) {
                            doc.add(new AreaBreak())
                        } else {
                            log.info "All ${imageFiles.length} images added to PDF(${outputPdf})."
                        }
                    } catch (Exception e) {
                        println "Error converting image '${file.name}' to PDF: ${e.message}"
                        e.printStackTrace()
                        resultsMap.put("error", e.message)
                        resultsMap.put("imgPdfPgCountSame", false);
                        return resultsMap
                    }
                }
            }
        } catch (Exception e) {
            println "Error converting images to PDF: ${e.message}"
            e.printStackTrace()
            resultsMap.put("error", e.message)
            resultsMap.put("imgPdfPgCountSame", false);
            return
        }
        finally {
            // Close the Document (important for iText 8)
            doc?.close()
            // Close the PDF document (important for iText 8)
            pdfDoc?.close()
        }
        int pageCount = PdfUtil.countPages(outputPdf)
        resultsMap.put("pdfPageCount", pageCount as Integer)
        resultsMap.put("imgPdfPgCountSame", (pageCount == imageFiles.length) as Boolean)
        return resultsMap
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

