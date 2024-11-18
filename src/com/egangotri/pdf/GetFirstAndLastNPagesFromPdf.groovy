package com.egangotri.pdf

import com.egangotri.util.PdfUtil;
import com.itextpdf.kernel.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the first and last N pages of PDFs in a folder and subfolders.
 */
class GetFirstAndLastNPagesFromPdf {
    private static final Logger log = LoggerFactory.getLogger(GetFirstAndLastNPagesFromPdf.class);

    private static File MAIN_FOLDER = new File("C:\\Users\\chetan\\Documents\\_testPDF");
    static File OUTPUT_FOLDER = new File("C:\\Users\\chetan\\Documents\\_output")
    private static final String PDF_EXTENSION = ".pdf";

    static int PAGE_LIMIT_FIRST_PART = 10;
    static int PAGE_LIMIT_LAST_PART = 10;
    private static boolean ONLY_PDFS = true;
    static PDF_EXTRACTOR_STATS = [
            "PDF_PAGE_EXTRACT_FOLDER_INDEX": 0,
            "PDF_PAGE_EXTRACT_TOTAL_FOLDER_COUNT": 1,
        "PDF_PAGE_EXTRACT_FOLDER_TOTAL_PDF_COUNT": 0,
        "PDF_PAGE_EXTRACT_COUNTER": 0
    ]
    static String PDF_EXTRACTOR_REPORT = ""
    static void main(String[] args) {
        execute(args);
    }

    static String execute(String[] args) {
        if (args.length >= 2) {
            MAIN_FOLDER = new File(args[0]);
            OUTPUT_FOLDER = new File(args[1]);
        }
        if (args.length >= 3) {
            PAGE_LIMIT_FIRST_PART = Integer.parseInt(args[2]);
        }
        if (args.length == 4) {
            PAGE_LIMIT_LAST_PART = Integer.parseInt(args[3]);
        }
        if(MAIN_FOLDER.isFile()){
            log.error("Please provide a folder path for Src, not a file path.(${MAIN_FOLDER.getAbsolutePath()})");
            return;
        }
        if(OUTPUT_FOLDER.isFile()){
            log.error("Please provide a folder path for Dest, not a file path.(${OUTPUT_FOLDER.getAbsolutePath()})");
            return;
        }

        PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_INDEX++
        PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_TOTAL_PDF_COUNT = PdfUtil.calculateTotalFileCount(true, [MAIN_FOLDER.absolutePath])
        PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_COUNTER=0;

        String outputFolderPath = "${MAIN_FOLDER.name} (${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_TOTAL_PDF_COUNT})";
        File outputFolder = new File(OUTPUT_FOLDER.absolutePath, outputFolderPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        processDirectory(MAIN_FOLDER, outputFolder);

        PDF_EXTRACTOR_REPORT = "FINAL_REPORT(extractPages):\n" +
                "Folder # (${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_INDEX}/${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_TOTAL_FOLDER_COUNT}).\n" +
                "PDF Count ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_COUNTER} == PDF_PROCESSING_COUNTER ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_TOTAL_PDF_COUNT} Match ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_COUNTER == PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_TOTAL_PDF_COUNT}"
        log.info(PDF_EXTRACTOR_REPORT)
        return PDF_EXTRACTOR_REPORT
    }

    static void processDirectory(File directory, File outputFolder) {
        log.info("Processing directory: ${directory.getAbsolutePath()} --> ${outputFolder.getAbsolutePath()}");
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                // Recursively process subdirectories
                processDirectory(file, new File(outputFolder, file.getName()));
            } else if (file.getName().toLowerCase().endsWith(PDF_EXTENSION) && (!ONLY_PDFS || file.getName().toLowerCase().endsWith(PDF_EXTENSION))) {
                try {
                    File outputFile = new File(outputFolder, file.getName());
                    outputFolder.mkdirs();
                    log.info("Processing file: {}", file.getAbsolutePath());
                    extractFirstAndLastPages(file, outputFile);
                } catch (Exception e) {
                    log.error("Error processing file: {}", file.getAbsolutePath(), e);
                }
            }
        }
    }

    private static void extractFirstAndLastPages(File inputFile, File outputFile) throws IOException {
        PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_COUNTER++

        String folderStats = "Folder ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_INDEX} of ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_TOTAL_FOLDER_COUNT}"
        String fileStats = "( pdf # ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_COUNTER} of ${PDF_EXTRACTOR_STATS.PDF_PAGE_EXTRACT_FOLDER_TOTAL_PDF_COUNT})"
        log.info("(${folderStats} of ${fileStats}). Extracting first {} and last {} pages from file: {}", PAGE_LIMIT_FIRST_PART, PAGE_LIMIT_LAST_PART, inputFile.getAbsolutePath());
        try (PdfReader reader = new PdfReader(inputFile.getAbsolutePath());
             PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile));
             PdfDocument sourcePdf = new PdfDocument(reader);
             PdfDocument outputPdf = new PdfDocument(writer)) {

            int totalPages = sourcePdf.getNumberOfPages();
            int firstPartEnd = Math.min(PAGE_LIMIT_FIRST_PART, totalPages);
            int lastPartStart = Math.max(totalPages - PAGE_LIMIT_LAST_PART + 1, firstPartEnd + 1);

            addPages(sourcePdf, outputPdf, 1, firstPartEnd);
            if (lastPartStart <= totalPages) {
                addPages(sourcePdf, outputPdf, lastPartStart, totalPages);
            }
            log.info("Created truncated file: {}", outputFile.getAbsolutePath());
        }
        catch(Exception e) {
            log.error("Error extracting first and last pages from file: {}", inputFile.getAbsolutePath(), e);
        }
    }

    private static void addPages(PdfDocument sourcePdf, PdfDocument outputPdf, int startPage, int endPage) throws IOException {
        for (int i = startPage; i <= endPage; i++) {
            PdfPage page = sourcePdf.getPage(i).copyTo(outputPdf);
            outputPdf.addPage(page);
        }
    }
}
