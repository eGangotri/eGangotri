package com.egangotri.pdf

import com.egangotri.util.TimeUtil
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extracts the first and last N pages of PDFs in a folder and subfolders.
 */
 class GetFirstAndLastNPagesFromPdfForMultiple {
    private static final Logger log = LoggerFactory.getLogger(GetFirstAndLastNPagesFromPdfForMultiple.class);

     static void main(String[] args) {
        execute(args);
    }

     static void execute(String[] args) {
         List logReports = []
        List folders = [];
        if (args.length >= 2) {
            String mainFoldersAsCSV = args[0];
            folders.addAll(mainFoldersAsCSV.split(",").collect(path->path.trim()));
            log.info("folders : ${folders.join(",")}")
            GetFirstAndLastNPagesFromPdf.OUTPUT_FOLDER = new File(args[1]);
        }
         GetFirstAndLastNPagesFromPdf.PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_TOTAL_FOLDER_COUNT = folders.size();
         long startTime = System.currentTimeMillis()

         folders.eachWithIndex { folder, index ->
             log.info("GetFirstAndLastNPagesFromPdfForMultiple (${index+1}) of ${GetFirstAndLastNPagesFromPdf.PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_TOTAL_FOLDER_COUNT}:Processing folder: {}", folder);
             GetFirstAndLastNPagesFromPdf.PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_NAME = folder;
             String _report = GetFirstAndLastNPagesFromPdf.execute(
                    [folder, args[1], args.length>=3 ? args[2] : null, args.length>=4 ? args[3] : null] as String[]
            )
             logReports.add(_report);
        }
         long endTime = System.currentTimeMillis()
         log.info("Time taken to extract pdf: ${TimeUtil.formatTime(endTime - startTime)}")
         log.info("GetFirstAndLastNPagesFromPdfForMultiple: All folders processed. Reports:\n{}", logReports.join("\n"));
    }
}
