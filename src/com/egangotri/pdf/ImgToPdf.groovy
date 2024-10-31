package com.egangotri.pdf

import com.egangotri.util.FolderUtil
import com.egangotri.util.GenericUtil
import com.egangotri.util.TimeUtil
import groovy.util.logging.Slf4j

import java.nio.file.Path

@Slf4j
class ImgToPdf {
    static List<Map<String,Object>> IMG_TO_PDF_RESULTS = []
    static void main(String[] args) {
        String folderName = args[0]
        String imgType = "ANY"
        if (args.length > 1) {
            imgType = args[1].trim();
        }
        long startTime = System.currentTimeMillis()
        log.info("Recieved folderName: ${folderName}")
        List<Path> allFolders = FolderUtil.listAllSubfolders(folderName)
        log.info("""Img2Pdf for : ${allFolders.size()} in ${allFolders}
                with ImgType: ${imgType} shall start now.""")
        Map<String, Object> firstRow = [:]
        firstRow.put("Img2PdfRoot", "${folderName}")
        firstRow.put("SubFolderCount", allFolders.size())
        firstRow.put("imgType", imgType)
        IMG_TO_PDF_RESULTS << firstRow;

        // Print the results
        for (Path folder : allFolders) {
            File _file = folder.toFile()
            String outputPdfPath = createOutputPdfName(_file.absolutePath)
            if (!outputPdfPath) {
                log.error("Unable to create a unique PDF file name while processing Folder ${_file.absolutePath}")
                continue;
            }

            log.info("""Processing: _file: ${_file.absolutePath}
                outputPdfPath: ${outputPdfPath}""")

            Map<String,Object> resultMap = ImgToPdfUtil.convertImagesToPdf(_file, outputPdfPath, imgType)
            GenericUtil.garbageCollectAndPrintMemUsageInfo()
            ImgToPdf.IMG_TO_PDF_RESULTS << resultMap
        }
        long endTime = System.currentTimeMillis()
        IMG_TO_PDF_RESULTS.each { Map<String, Object> result ->
            result.each { key, value ->
                println "${key}: ${value}"
            }
        }
        log.info("Time taken to convert images to pdf: ${TimeUtil.formatTime(endTime - startTime)}")
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




