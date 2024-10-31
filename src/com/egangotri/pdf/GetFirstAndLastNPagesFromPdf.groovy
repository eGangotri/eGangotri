package com.egangotri.pdf;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.utils.PdfSplitter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Extracts the first and last N pages of PDFs in a folder and subfolders.
 */
public class GetFirstAndLastNPagesFromPdf {
    private static final Logger log = LoggerFactory.getLogger(GetFirstAndLastNPagesFromPdf.class);

    private static File MAIN_FOLDER = new File("C:\\Users\\chetan\\Documents\\_testPDF");
    private static final String OUTPUT_FOLDER_NAME = "_output";
    private static final String PDF_EXTENSION = ".pdf";
    private static final String TRUNCATION_SUFFIX_FOLDER = "_truncated";

    private static int PAGE_LIMIT_FIRST_PART = 10;
    private static int PAGE_LIMIT_LAST_PART = 10;
    private static boolean ONLY_PDFS = true;

    public static void main(String[] args) {
        execute(args);
    }

    public static void execute(String[] args) {
        if (args.length >= 1) {
            MAIN_FOLDER = new File(args[0]);
        }
        if (args.length >= 2) {
            PAGE_LIMIT_FIRST_PART = Integer.parseInt(args[1]);
        }
        if (args.length == 3) {
            PAGE_LIMIT_LAST_PART = Integer.parseInt(args[2]);
        }
        if(MAIN_FOLDER.isFile()){
            log.error("Please provide a folder path, not a file path.(${MAIN_FOLDER.getAbsolutePath()})");
            return;
        }
        String outputFolderPath = MAIN_FOLDER.getAbsolutePath() + TRUNCATION_SUFFIX_FOLDER;
        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        log.info("Created output folder: {}", outputFolder.getAbsolutePath());

        processDirectory(MAIN_FOLDER, outputFolder);
    }

    private static void processDirectory(File directory, File outputFolder) {
        log.info("Processing directory: {}", directory.getAbsolutePath());

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                // Recursively process subdirectories
                processDirectory(file, new File(outputFolder, file.getName()));
            } else if (file.getName().endsWith(PDF_EXTENSION) && (!ONLY_PDFS || file.getName().endsWith(PDF_EXTENSION))) {
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
        log.info("Extracting first {} and last {} pages from file: {}", PAGE_LIMIT_FIRST_PART, PAGE_LIMIT_LAST_PART, inputFile.getAbsolutePath());
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
