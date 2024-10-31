import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image
import java.nio.file.Files

class ImgToPdfUtil {
    static void convertImagesToPdf(String imgFolder, String outputPdf) {
        // Create a new PDF document
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputPdf))
        Document doc = null
        try {
            // Create a new Document for adding content
            doc = new Document(pdfDoc)

            // Iterate through all files in the directory
            File imgFolderDir = new File(imgFolder)
            imgFolderDir.eachFile { file ->
                if (file.isFile()) {
                    String mimeType = Files.probeContentType(file.toPath())
                    if (mimeType.startsWith("image/")) {
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
                            doc.add(new AreaBreak())
                        } catch (Exception e) {
                            println "Error converting image '${file.name}' to PDF: ${e.message}"
                            e.printStackTrace()
                            return
                        }
                    } else {
                        println "Skipping file '${file.name}' as it's not an image."
                    }
                }
            }

        } catch (Exception e) {
            println "Error converting images to PDF: ${e.message}"
            e.printStackTrace()
        }
        finally {
            // Close the Document (important for iText 8)
            doc?.close()
            // Close the PDF document (important for iText 8)
            pdfDoc?.close()
        }
    }
}