package com.egangotri.pdf

import com.egangotri.util.FolderUtil
import com.egangotri.util.TimeUtil
import groovy.util.logging.Slf4j

import java.nio.file.Path

@Slf4j
class ImgToPdf {
    static void main(String[] args) {
        String folderName = args[0]
        String imgType = "ANY"
        if (args.length > 1) {
            imgType = args[1].trim();
        }
        long startTime = System.currentTimeMillis()
        log.info("Recieved folderName: ${folderName}")
        List<Path> allFolders = FolderUtil.listAllSubfolders(folderName)
        List<Map> allResults = []
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

            def results = [:];
            ImgToPdfUtil.convertImagesToPdf(_file, outputPdfPath, imgType)
            results.forEach {
                log.info("${it.key}: ${it.value}")
            }
            allResults << results
        }
        long endTime = System.currentTimeMillis()
        allResults.each { Map result ->
            result.forEach {
                log.info("${it.key}: ${it.value}")
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




