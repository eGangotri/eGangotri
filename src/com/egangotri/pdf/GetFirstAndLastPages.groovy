package com.egangotri.pdf

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter

/**
 * Created by user on 4/7/2016.
 */
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
                println("procesing File ${file.name}")
                manipulateWithCopy(file);
            }

        }
    }

    private static void manipulateWithCopy(File file)
            throws IOException, DocumentException {
        println("manipulateWithCopy pages")

        OutputStream os = new FileOutputStream("${DESTINATION_FOLDER}${file.name}");
        FileInputStream inStr = new FileInputStream(file)
        PdfReader reader = new PdfReader(inStr);
        int numOfPages = reader.getNumberOfPages()

        println("Orig: $numOfPages")
        if(numOfPages > (NUM_PAGES_TO_EXTRACT*2)){
            String selectPageExpression = "1-$NUM_PAGES_TO_EXTRACT,${numOfPages-(NUM_PAGES_TO_EXTRACT-1)}-${numOfPages}"
            println(selectPageExpression)
            reader.selectPages(selectPageExpression)
        }
        int selectNumOfPages = reader.getNumberOfPages()
        println("selectNumOfPages: $selectNumOfPages")
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, os);
        document.open();
        for (int i = 0; i < selectNumOfPages;) {
            copy.addPage(copy.getImportedPage(reader, ++i));
        }
        os.flush();
        document.close();
        os.close();
        reader.close();

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
