package com.egangotri.pdf

import groovy.util.logging.Log4j

@Log4j
class PdfUtil {
    static String NMM_PATH = "D:\\NMM\\"
    static String extractTiffFolderName(File pdfFolder){
        String[] _splitBy =  pdfFolder.name.split("_")
        String extractedDate = (_splitBy?.size()> 1 && _splitBy[1].size() > 10) ? _splitBy[1].substring(0,10) : ""
        String tiffFolderPath = "${NMM_PATH}${pdfFolder.getParentFile().name}\\${extractedDate}"
        if(extractedDate && new File(tiffFolderPath).exists()){
            return tiffFolderPath;
        }
        return null
    }
}
