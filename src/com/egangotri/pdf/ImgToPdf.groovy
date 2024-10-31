package com.egangotri.pdf

import com.egangotri.util.TimeUtil
import groovy.util.logging.Slf4j

@Slf4j
class ImgToPdf {
    static void main(String[] args) {
        String imageFolder = args[0]
        String imgType = "ANY"
        if(args.length > 1) {
            imgType = args[1].trim();
        }
        String folderName = new File(imageFolder).getName()
        String outputPdfPath = createOutputPdfName(imageFolder)

        if (!outputPdfPath) {
            log.error("Unable to create a unique PDF file name.")
            return
        }

        log.info("""Processing: folderName: ${folderName}
                outputPdfPath: ${outputPdfPath}""")
        long startTime = System.currentTimeMillis()
        ImgToPdfUtil.convertImagesToPdf(imageFolder, outputPdfPath,imgType)
        long endTime = System.currentTimeMillis()
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




