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
    static List<Map<String,Object>> PDF_MERGE_RESULTS = []

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
        decorateResults(folderName,mergeType)
        log.info("Time taken to convert images to pdf: ${TimeUtil.formatTime(endTime - startTime)}")

    }
    static void decorateResults(String folderName, String mergeType){
        log.info("PDF_MERGE_RESULTS: ${PDF_MERGE_RESULTS.size()} ")
        Map<String, Object> _row = [:]
        _row.put("PdfMergeResultRootFolder", "${folderName}")
        _row.put("mergeType", mergeType)
        PDF_MERGE_RESULTS << _row;

        PDF_MERGE_RESULTS.eachWithIndex { row, i ->
            log.info("${i + 1}). ${row}")
        }
    }

    static void mergeAll(String folderName) {
        Set<Path> allFolders = FolderUtil.listAllSubFoldersinCSV(folderName)
        Map<String, Object> _row = [:]
        _row.put("folderName", folderName)
        _row.put("allFoldersCount", allFolders.size())
        try {
            List<File> allPdfs = []
            allFolders.each { folder ->
                log.info("mergeAll: ${folder}")
                File[] pdfs = GenericUtil.getPdfsSortedByName(folder.toFile())
                allPdfs.addAll(pdfs)
            }
            if (allPdfs.size() > 1) {
                String finalPdf = PdfUtil.createOutputPdfName(allFolders[0].toString())
                _row.put("finalPdf", finalPdf)
                _row.put("pdfsCount", allPdfs.size())
                try {
                    doMerge(allPdfs as File[], finalPdf)
                } catch (Exception e) {
                    e.printStackTrace()
                    _row.put("mergeError", e.message)
                    log.error("MergePdfError while working with ${allPdfs?.size()} pdfs causing err:", e );
                }
            } else {
                log.info("Only one PDF found in ${folderName}")
                _row.put("1PdfOnly", true)
            }
        } catch (Exception e) {
            log.error("MergePdfError while working with ${folderName} causing err:", e)
            _row.put("mergeError", e.message)
        }
        PDF_MERGE_RESULTS << _row
    }

    static void mergePerFolder(String folderName) {
        Set<Path> allFolders = FolderUtil.listAllSubFoldersinCSV(folderName)
        Map<String, Object> _row = [:]
        _row.put("folderName", folderName)
        try {
            allFolders.each { folder ->
                log.info("mergePerFolder: ${folder}")
                File[] allPdfs = GenericUtil.getPdfsSortedByName(folder.toFile())
                if (allPdfs.length > 1) {
                    String finalPdf = PdfUtil.createOutputPdfName(folder.toString())
                    _row.put("finalPdf", finalPdf)
                    _row.put("pdfsCount", allPdfs.length)
                    try {
                        doMerge(allPdfs, finalPdf)
                    }
                    catch (Exception e) {
                        log.error("MergePdfError while working with ${folder} causing err",e);
                        e.printStackTrace()
                        _row.put("mergeError", e.message)
                    }
                } else {
                    log.info("Only one PDF found in ${folder}")
                    _row.put("1PdfOnly", true)
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace()
            log.error("MergePdfError while working with ${folderName} causing err:",e
                    );
            _row.put("mergeError", e.message)
        }
        PDF_MERGE_RESULTS << _row
    }


    /**
     * Merge multiple PDFs into one PDF
     *
     * @param files array of input PDF files
     * @param finalPdf path to the final merged PDF
     */
    static void doMerge1(File[] files, String finalPdf) throws IOException {
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
                GenericUtil.garbageCollectAndPrintMemUsageInfo()
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
    static void doMerge(File[] files, String finalPdf) throws IOException {
        log.info("doMerge ${files?.length} PDFs into ${finalPdf}")
        File outputFile = new File(finalPdf)

        PdfWriter writer = new PdfWriter(outputFile)
        PdfDocument pdfDoc = new PdfDocument(writer)
        PdfMerger merger = new PdfMerger(pdfDoc)

        try {
            int batchSize = 5; // Adjust this based on your needs
            for (int i = 0; i < files.length; i += batchSize) {
                int end = Math.min(i + batchSize, files.length)
                File[] batch = Arrays.copyOfRange(files, i, end)

                for (File file : batch) {
                    log.info("Reading ${file.name}")
                    PdfReader reader = new PdfReader(new FileInputStream(file))
                    PdfDocument sourcePdf = new PdfDocument(reader)

                    log.info("Merging ${file.name} into ${finalPdf}")
                    merger.merge(sourcePdf, 1, sourcePdf.numberOfPages)
                    sourcePdf.close()
                    reader.close()
                    GenericUtil.garbageCollectAndPrintMemUsageInfo()
                }
                // Force garbage collection after processing each batch
                System.gc()
            }
        } catch (IOException e) {
            log.error("Error while merging PDFs: ${e.message}", e)
            throw e
        } finally {
            pdfDoc.close()
            writer.close()
        }
    }

}
