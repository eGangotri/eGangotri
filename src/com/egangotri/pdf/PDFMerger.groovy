package com.egangotri.pdf

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import groovy.util.logging.Slf4j

/**
 * Created by user on 4/7/2016.
 */
@Slf4j
class PDFMerger {
    static String ROOT_FOLDER = "E:\\ramtek_4_05-08-2019"
    // class ItextMerge {
    static void main(String[] args) {
        try {
            if(args){
                ROOT_FOLDER = args[0]
            }
            File rootDir = new File(ROOT_FOLDER)
            File[] foldersWithPdf = rootDir.listFiles({ d, f -> d.isDirectory()} as FilenameFilter);
            int counter = 0
            for (File subFolder in foldersWithPdf) {
                counter++
                log.info "${counter})subFolder $subFolder"
                try{
                    processMerge(subFolder)
                }
                catch(Exception e){
                    log.info("Error in Process Merge",e)
                }
                System.gc()
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace()
        } catch (DocumentException e) {
            e.printStackTrace()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    static void processMerge(File subFolder){
        List list = new ArrayList()
        def pdfFiles = new File(subFolder, "pdfs").listFiles({ d, f -> f ==~ /(?i).*.pdf/ } as FilenameFilter)
        for( File pdfFile in pdfFiles){
            log.info "pdfFile $pdfFile"
            list.add(pdfFile)
        }
        // Resulting pdf
        if(list){
            String finalPdf = subFolder.getParentFile().getAbsolutePath() + "//" + subFolder.name + ".pdf"
            log.info "Finl PdfName ${finalPdf}"
            doMerge(list, finalPdf)
        }
    }
    /**
     * Merge multiple pdf into one pdf
     *
     * @param list
     *            of pdf input stream
     * @param outputStream
     *            output file output stream
     */
    static void doMerge(List files, String finalPdf)
            throws DocumentException, IOException {
        log.info("doMerge for ${finalPdf}")
        Document document = new Document()
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(finalPdf));
        document.open();

        for (def file : files){
            log.info("merging ${file} into ${finalPdf}")
            PdfReader reader = new PdfReader(new FileInputStream(file));
            copy.addDocument(reader);
            copy.freeReader(reader);
            reader.close();
        }
        document.close();
    }
    // }
}
