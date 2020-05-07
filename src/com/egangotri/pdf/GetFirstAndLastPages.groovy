package com.egangotri.pdf

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import groovy.util.logging.Slf4j

/**
 * Created by user on 4/7/2016.
 */
@Slf4j
class GetFirstAndLastPages {
    static String FOLDER_NAME = "C:\\src_upss_manu\\noTitles"
    static String DESTINATION_FOLDER = "$FOLDER_NAME\\dest1\\"
    static int NUM_PAGES_TO_EXTRACT = 5
    static List ignoreList = []
    static String PDF = "pdf"


    static void main(String[] args) {
        procAdInfinitum(FOLDER_NAME)
    }

    static void extractFirstAndLastPages(File directory)
            throws DocumentException, IOException {

        for (File file : directory.listFiles()) {
            if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && file.name.endsWith(PDF)) {
                log.info("procesing File ${file.name}")
                manipulateWithCopy(file)
            }

        }
    }

    private static void manipulateWithCopy(File file)
            throws IOException, DocumentException {
        log.info("manipulateWithCopy pages")

        OutputStream os = new FileOutputStream("${DESTINATION_FOLDER}${file.name}")
        FileInputStream inStr = new FileInputStream(file)
        PdfReader reader = new PdfReader(inStr)
        int numOfPages = reader.getNumberOfPages()

        log.info("Orig: $numOfPages")
        if(numOfPages > (NUM_PAGES_TO_EXTRACT*2)){
            String selectPageExpression = "1-$NUM_PAGES_TO_EXTRACT,${numOfPages-(NUM_PAGES_TO_EXTRACT-1)}-${numOfPages}"
            log.info(selectPageExpression)
            reader.selectPages(selectPageExpression)
        }
        int selectNumOfPages = reader.getNumberOfPages()
        log.info("selectNumOfPages: $selectNumOfPages")
        Document document = new Document()
        PdfCopy copy = new PdfCopy(document, os)
        document.open()
        for (int i = 0; i < selectNumOfPages;) {
            copy.addPage(copy.getImportedPage(reader, ++i))
        }
        os.flush()
        document.close()
        os.close()
        reader.close()

    }

    static void procAdInfinitum(String folderAbsolutePath) {
        File directory = new File(folderAbsolutePath)

        extractFirstAndLastPages(directory)
        //Then get in Sub-directories and process them
        /*   for (File subDirectory : directory.listFiles()) {
               if (subDirectory.isDirectory() && !ignoreList.contains(subDirectory.name.toString())) {
                   procAdInfinitum(subDirectory.absolutePath)
               }
           }*/
    }
}
