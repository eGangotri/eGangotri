package com.egangotri.pdf

class TallyPojo {
    List NON_MATCHING = []
    List MATCHING
    List UNCHECKABLE
    List NOT_CREATED
    List EXCEPTION_ENCOUNTERED
    String TIF_FOLDER
    String PDF_FOLDER
    List TIF_DIR_FILES
    List PDF_FILES
    int ERROR_MARGIN

    public TallyPojo(){
        NON_MATCHING = []
        MATCHING = []
        UNCHECKABLE = []
        NOT_CREATED = []
        EXCEPTION_ENCOUNTERED = []
        TIF_DIR_FILES = []
        PDF_FILES = []
    }
    String genFinalReport(String tifFolder, String pdfFolder, List tifDirFiles, List pdfFiles) {
        TIF_FOLDER = tifFolder
        PDF_FOLDER = pdfFolder
        TIF_DIR_FILES = tifDirFiles
        PDF_FILES = pdfFiles
        ERROR_MARGIN = TIF_DIR_FILES.size() - MATCHING.size()
        return """
 Pdf Folder: ${PDF_FOLDER}
                    Tif Folder: ${TIF_FOLDER}                    
                    NON_MATCHING_COUNT: ${NON_MATCHING.size()}
                    MATCHING_COUNT: ${MATCHING.size()}
                    UNCHECKABLE_COUNT: ${UNCHECKABLE.size()}
                    NOT_CREATED_COUNT: ${NOT_CREATED.size()}
                    EXCEPTION_ENCOUNTERED_COUNT: ${EXCEPTION_ENCOUNTERED.size()}
                    Total Tiff Folders expected for Conversion: ${TIF_DIR_FILES?.size()}
                    Total PDFs in Folder: ${PDF_FILES?.size()}
                    Ready For Upload: ${MATCHING.join("\n")}
                    Manually check ${UNCHECKABLE}
                    Reconvert (Uncreated) ${NOT_CREATED.join("\n")}
                    Reconvert (Erroneous Page Count) ${NON_MATCHING}
                    Reconvert (Exception Encountered) ${EXCEPTION_ENCOUNTERED}
                    Error Margin:  ${ERROR_MARGIN} [${TIF_DIR_FILES.size()} - ${MATCHING.size()}]
"""
    }

}