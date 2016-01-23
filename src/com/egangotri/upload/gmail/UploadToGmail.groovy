package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils

/**
 * Created by user on 1/22/2016.
 */

class UploadToGmail {
    static String MANUSCRIPT_MASTER_FOLDER_NAME = "C:\\hw\\avn\\AvnManuscripts\\GopinathKavirajTantricSahityaList"
    static String BOOK_MASTER_FOLDER_NAME = "C:\\hw\\avn\\AvnManuscripts\\GopinathKavirajTantricSahityaList"

    static final String LOGIN_PROFILE = UPLOAD_PROFILE_ENUMS.mm.toString()
    static enum UPLOAD_PROFILE_ENUMS {
        bm, mm
    }
    static main(args) {
        println "start"
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
        GmailHandler.loginAndUpload(metaDataMap,LOGIN_PROFILE, true, (LOGIN_PROFILE == UPLOAD_PROFILE_ENUMS.mm.toString() ? MANUSCRIPT_MASTER_FOLDER_NAME : BOOK_MASTER_FOLDER_NAME))
    }

}

