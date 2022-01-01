package com.egangotri.pdf

class TallyPojo {
    static List NON_MATCHING
    static List MATCHING
    static List UNCHECKABLE
    static List NOT_CREATED
    static List EXCEPTION_ENCOUNTERED

    static void resetTallyObj(){
        NON_MATCHING = []
        MATCHING = []
        UNCHECKABLE = []
        NOT_CREATED = []
        EXCEPTION_ENCOUNTERED = []
    }

   static String genFinalReport(String tifFolder, String pdfFolder, List tifDirFiles, List pdfFiles){
        return """
 Pdf Folder: ${pdfFolder}
                    Tif Folder: ${tifFolder}                    
                    NON_MATCHING_COUNT: ${NON_MATCHING.size()}
                    MATCHING_COUNT: ${MATCHING.size()}
                    UNCHECKABLE_COUNT: ${UNCHECKABLE.size()}
                    NOT_CREATED_COUNT: ${NOT_CREATED.size()}
                    EXCEPTION_ENCOUNTERED_COUNT: ${EXCEPTION_ENCOUNTERED.size()}
                    Total Tiff Folders expected for Conversion: ${tifDirFiles?.size()}
                    Total PDFs in Folder: ${pdfFiles?.size()}
                    Ready For Upload: ${MATCHING.join("\n")}
                    Manually check ${UNCHECKABLE}
                    Reconvert (Uncreated) ${NOT_CREATED.join("\n")}
                    Reconvert (Erroneous Page Count) ${NON_MATCHING}
                    Reconvert (Exception Encountered) ${EXCEPTION_ENCOUNTERED}
                    Error Margin: ${tifDirFiles.size()} - ${MATCHING.size()} = ${tifDirFiles.size() - MATCHING.size()}

"""
    }
}
