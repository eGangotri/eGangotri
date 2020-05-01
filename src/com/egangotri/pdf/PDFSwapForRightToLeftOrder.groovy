package com.egangotri.pdf

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfSmartCopy
import groovy.util.logging.Slf4j

import java.util.regex.Matcher
@Slf4j
class PDFSwapForRightToLeftOrder {

    static String path = "C:\\q\\q\\q"
    static String fileName = "Haqayat-e-Rumi I and II 1945 - Anjuman-e- Taraqqi.pdf"
    static String tmpDir = path + File.separator + "tmp"
    /**
     * @param args
     */
    static main(def args) {
        convertToOnePagePdfs()
        swapPages()
    }

    static void swapPages() {
        List<File> odds = []
        List<File> evens = []
        new File(tmpDir).list().each { String fileName ->
            String fullPath = "$tmpDir${File.separator}${fileName}"
            log.info "${fullPath}"

            Matcher matcher = (fileName =~ /-\d+\.pdf/)
            int digits = stripToDigit(matcher ? matcher[0]?.toString() : "")

            if (digits > 0) {
                if ((digits) % 2 == 1) {
                    odds << new File(fullPath)
                    log.info "odds: $fileName"
                } else {
                    evens << new File(fullPath)
                    log.info "evens: $fileName"
                }
            }
        }

        int count = 0
        String swappedFile = "${getTmpFileNameBase()}-Swapped.pdf"
        concatenateOddEvenPdfs(odds, evens, new File(swappedFile))
    }

    static int stripToDigit(String digits) {
        log.info "$fileName: ${digits}"
        if (digits) {
            digits = digits.replace("-", "")
            digits = digits.replace(".pdf", "")

            if (digits.startsWith("00")) {
                digits = digits.replaceFirst("-00", "")
            }
            if (digits.startsWith("0")) {
                digits = digits.replaceFirst("-0", "")
            }
            log.info "${digits} "
            return digits as int
        }

        return 0

    }

    static void concatenatePdfs(List<File> listOfPdfFiles, File outputFile) throws DocumentException, IOException {
        log.info("concatenatePdfs for $outputFile")
        Document document = new Document()
        FileOutputStream outputStream = new FileOutputStream(outputFile)
        PdfCopy copy = new PdfSmartCopy(document, outputStream)
        document.open()
        for (File inFile : listOfPdfFiles) {
            PdfReader reader = new PdfReader(inFile.getAbsolutePath())
            copy.addDocument(reader)
            reader.close()
        }
        document.close()
    }

    static void concatenateOddEvenPdfs(List<File> listOfOddPdfFiles, List<File> listOfEvenPdfFiles, File outputFile) throws DocumentException, IOException {
        log.info("concatenatePdfs for $outputFile")
        Document document = new Document()
        FileOutputStream outputStream = new FileOutputStream(outputFile)
        PdfCopy copy = new PdfSmartCopy(document, outputStream)
        document.open()
        int count = listOfOddPdfFiles.size() // odd will alys be higher or equal

        for (int i = 0; i < count; i++) {
            File inFile = listOfOddPdfFiles[i]
            if (inFile) {
                PdfReader reader = new PdfReader(inFile.getAbsolutePath())
                copy.addDocument(reader)
                reader.close()
            }

            File inFile2 = listOfEvenPdfFiles[i]
            if (inFile2) {
                PdfReader reader = new PdfReader(inFile2.getAbsolutePath())
                copy.addDocument(reader)
                reader.close()
            }

        }
        if (document) {
            document.close()
        }
    }

    static String getFileName() {
        return path + File.separator + fileName
    }

    static PdfReader getReader(String inFile) {
        System.out.log.info("Reading " + inFile)
        PdfReader reader = new PdfReader(inFile)
        return reader

    }

    static String getTmpFileNameBase() {
        return "$tmpDir${File.separator}${fileName.substring(0, fileName.indexOf(".pdf"))}"
    }

    static String getTmpFileName(int i) {
        "${getTmpFileNameBase()}-${String.format("%03d", i + 1)}.pdf"
    }

    static void convertToOnePagePdfs() {
        try {
            PdfReader reader = getReader(getFileName())
            int n = reader.getNumberOfPages()
            System.out.log.info("Number of pages : " + n)
            int i = 0
            while (i < n) {
                String outFile = getTmpFileName(i)
                System.out.log.info("Writing " + outFile)
                Document document = new Document(reader.getPageSizeWithRotation(1))
                PdfCopy writer = new PdfCopy(document, new FileOutputStream(outFile))
                document.open()
                PdfImportedPage page = writer.getImportedPage(reader, ++i)
                writer.addPage(page)
                document.close()
                writer.close()
            }
        }
        catch (Exception e) {
            e.printStackTrace()
        }
    }
}

