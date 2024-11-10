package com.egangotri.pdf

import com.egangotri.util.FolderUtil
import com.egangotri.util.GenericUtil
import com.egangotri.util.PdfUtil
import com.egangotri.util.TimeUtil
import groovy.util.logging.Slf4j

import java.nio.file.Path

@Slf4j
class VerifyImgToPdf {
    static List<Map<String, Object>> VERIFY_IMG_TO_PDF_RESULTS = []

    static void main(String[] args) {
        String folderName = args[0]
        String imgType = ImgToPdfUtil.IMG_TYPE_ANY
        if (args.length > 1) {
            imgType = args[1].trim();
        }
        long startTime = System.currentTimeMillis()
        log.info("Recieved folderName: ${folderName}")
        List<Path> allFolders = FolderUtil.listAllSubfolders(folderName)
        try {
            exec(allFolders, imgType)
        }
        catch (Exception e) {
            log.error("ImgToPdfVerifyError while working with ${folderName} causing err:\n" +
                    " ${e.getStackTrace()}");
        }
        long endTime = System.currentTimeMillis()
        decorateResults(folderName, allFolders, imgType)
        log.info("Time taken to verify images to pdf: ${TimeUtil.formatTime(endTime - startTime)}")
    }

    static void decorateResults(String folderName, List<Path> allFolders, String imgType) {
        List<Map<String, Object>> filteredResults =
                VerifyImgToPdf.VERIFY_IMG_TO_PDF_RESULTS.findAll { !it.containsKey("0Images") || !it["0Images"] }


        log.info("Img2Pdf Results: ${VERIFY_IMG_TO_PDF_RESULTS.size()} ")
        Map<String, Object> latRow = [:]
        latRow.put("Img2PdfRoot", "${folderName}")
        latRow.put("imgType", imgType)
        latRow.put("SubFolderCountTotal", allFolders.size())
        latRow.put("EmptySubFolders", allFolders.size() - filteredResults.size())
        latRow.put("NonEmptySubFolders", filteredResults.size())
        int count = VerifyImgToPdf.VERIFY_IMG_TO_PDF_RESULTS.findAll { it.get("imgPdfPgCountSame") == true }?.size();
        latRow.put("CountImgPdfPgCountSame", count)
        latRow.put("VERIFICATION_SUCCESS", "${filteredResults.size() == count}(${count} of ${filteredResults.size()})")

        if (filteredResults.size() != count) {
            latRow.put("SHORT_BY", "${filteredResults.size() - count} of ${filteredResults.size()}")
        }
        filteredResults << latRow;

        filteredResults.eachWithIndex { row, i ->
            log.info("${i + 1}). ${row}")
        }
    }

    static Map<String, Object> verifyImgToPdfSuccess(File imgFolder, File outputPdf, String imgType = ImgToPdfUtil.IMG_TYPE_ANY) {
        Map<String, Object> resultMap = [:]
        resultMap.put("imgFolder", imgFolder.absolutePath)
        resultMap.put("outputPdf", outputPdf.absolutePath)
        resultMap.put("imgType", imgType)
        resultMap.put("imgPdfPgCountSame", false)
        resultMap.put("pdfPageCount", 0)

        File[] imageFiles = ImgToPdfUtil.getImageFiles(imgFolder.absolutePath, imgType)
        resultMap.put("imgCount", imageFiles.length as Integer)
        if (imageFiles.length == 0) {
            resultMap.put("0Images", true)
            return resultMap
        }
        int pdfPageCount = PdfUtil.countPages(outputPdf.absolutePath)
        resultMap.put("pdfPageCount", pdfPageCount)
        resultMap.put("imgPdfPgCountSame", (imageFiles.length - pdfPageCount) == 0)
        return resultMap
    }

    static void exec(List<Path> allFolders, String imgType = ImgToPdfUtil.IMG_TYPE_ANY) {
        log.info("""Img2Pdf for : ${allFolders.size()} in ${allFolders}
                with ImgType: ${imgType} shall start now.""")
        for (Path folder : allFolders) {
            File _file = folder.toFile()
            Map<String, Object> _row = [:]
            File[] imageFiles = ImgToPdfUtil.getImageFiles(_file.absolutePath, imgType)
            _row.put("imgCount", imageFiles.length as Integer)
            if (imageFiles.length == 0) {
                _row.put("0Images", true)
                VERIFY_IMG_TO_PDF_RESULTS << _row
                continue;
            }

            File outputPdfPath = PdfUtil.outputPdfFilePerFormat(_file.absolutePath)
            if (!outputPdfPath) {
                log.error("Couldnt find pdf in ${_file.absolutePath}")
                Map<String, Object> _row2 = [:]
                _row2.put("NoPDFInFolder", "Cannot create ${_file.absolutePath}")
                VERIFY_IMG_TO_PDF_RESULTS << _row
                continue;
            }
            log.info("""Processing: _file: ${_file.absolutePath}
                outputPdfPath: ${outputPdfPath}""")
            VerifyImgToPdf.VERIFY_IMG_TO_PDF_RESULTS << verifyImgToPdfSuccess(_file, outputPdfPath, imgType)
            GenericUtil.garbageCollectAndPrintMemUsageInfo()
        }

    }

}




