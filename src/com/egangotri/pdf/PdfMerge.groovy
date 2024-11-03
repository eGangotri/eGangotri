package com.egangotri.pdf

import com.egangotri.util.FolderUtil
import com.egangotri.util.GenericUtil
import com.egangotri.util.PdfUtil
import com.egangotri.util.TimeUtil
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import groovy.util.logging.Slf4j

import java.nio.file.Path

@Slf4j
class PdfMerge {
    enum MERGE_TYPE {
        MERGE_ALL,
        MERGE_PER_FOLDER
    }

    static void main(String[] args) {
        String folderName = args[0]

        String mergeType = MERGE_TYPE.MERGE_ALL.name()
        if (args.length > 1) {
            mergeType = args[1].trim() == MERGE_TYPE.MERGE_ALL.name() ? MERGE_TYPE.MERGE_ALL.name() : MERGE_TYPE.MERGE_PER_FOLDER.name();
        }
        long startTime = System.currentTimeMillis()
        log.info("Recieved folderName: ${folderName} with mergeType: ${mergeType}")
        if (mergeType == MERGE_TYPE.MERGE_ALL.name()) {
            mergeAll(folderName)
        } else {
            mergePerFolder(folderName)
        }
        long endTime = System.currentTimeMillis()
        //decorateResults(folderName, allFolders, imgType)
        log.info("Time taken to convert images to pdf: ${TimeUtil.formatTime(endTime - startTime)}")

    }

    static void mergeAll(String folderName) {
        Set<Path> allFolders = FolderUtil.listAllSubFoldersinCSV(folderName)
        try {
            List<File> allPdfs = []
            allFolders.each { folder ->
                log.info("mergeAll: ${folder}")
                File[] pdfs = GenericUtil.getPdfsSortedByName(folder.toFile())
                allPdfs.addAll(pdfs)
            }
            if (allPdfs.size() > 1) {
                String finalPdf = PdfUtil.createOutputPdfName(allFolders[0].toString())
                try {
                    doMerge(allPdfs as File[], finalPdf)
                } catch (Exception e) {
                    e.printStackTrace()
                    log.error("MergePdfError while working with ${allPdfs?.size()} pdfs causing err:", e );
                }
            } else {
                log.info("Only one PDF found in ${folderName}")
            }
        } catch (Exception e) {
            log.error("ImgToPdfError while working with ${folderName} causing err:\n" +
                    " ${e.getStackTrace()}");
        }
    }

    static void mergePerFolder(String folderName) {
        Set<Path> allFolders = FolderUtil.listAllSubFoldersinCSV(folderName)
        try {
            allFolders.each { folder ->
                log.info("mergePerFolder: ${folder}")
                File[] allPdfs = GenericUtil.getPdfsSortedByName(folder.toFile())
                if (allPdfs.length > 1) {
                    String finalPdf = PdfUtil.createOutputPdfName(folder.toString())
                    try {
                        doMerge(allPdfs, finalPdf)
                    }
                    catch (Exception e) {
                        log.error("ImgToPdfError while working with ${folder} causing err:\n" +
                                " ${e.getStackTrace()}");
                    }
                } else {
                    log.info("Only one PDF found in ${folder}")
                }
            }
        }
        catch (Exception e) {
            log.error("ImgToPdfError while working with ${folderName} causing err:\n" +
                    " ${e.getStackTrace()}");
        }
    }


    /**
     * Merge multiple PDFs into one PDF
     *
     * @param files array of input PDF files
     * @param finalPdf path to the final merged PDF
     */
    static void doMerge(File[] files, String finalPdf) throws IOException {
        log.info("doMerge ${files?.length} PDFs into ${finalPdf}")
        File outputFile = new File(finalPdf)

        // Create the PdfDocument for the output file
        PdfWriter writer = new PdfWriter(outputFile)
        PdfDocument pdfDoc = new PdfDocument(writer)
        PdfMerger merger = new PdfMerger(pdfDoc)

        try {
            files.each { File file ->
                log.info("Reading ${file?.name}")
                PdfReader reader = new PdfReader(new FileInputStream(file))
                PdfDocument sourcePdf = new PdfDocument(reader)

                log.info("Merging ${file.name} into ${finalPdf}")
                merger.merge(sourcePdf, 1, sourcePdf.numberOfPages)
                sourcePdf.close() // Close source PDF after merging
                reader.close()
            }
        } catch (IOException e) {
            log.error("Error while merging PDFs: ${e.message}", e)
            e.printStackTrace()
            throw e
        } finally {
            pdfDoc.close() // Ensure the output document is closed
            writer.close()
        }
    }

}
