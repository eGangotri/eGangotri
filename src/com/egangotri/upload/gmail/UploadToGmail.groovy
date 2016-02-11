package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil

/**
 * Created by user on 1/22/2016.
 */

class UploadToGmail {
    static List MANUSCRIPT_MASTER_FOLDER_NAME = [FileUtil.DT_DEFAULT]
    static List BOOK_MASTER_FOLDER_NAME = [FileUtil.JG_DEFAULT, FileUtil.RK_DEFAULT]
    //C:\hw\ib

    static final List LOGIN_PROFILES = [UPLOAD_PROFILE_ENUMS.bm/*, UPLOAD_PROFILE_ENUMS.mm*/]
    static enum UPLOAD_PROFILE_ENUMS {
        bm, mm
    }

    static main(args) {
        println "start"
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
        execute(LOGIN_PROFILES, metaDataMap)
    }

    public static void execute(List profiles, Map metaDataMap) {
        println "Start uploading to Gmail"
        profiles*.toString().each { String profile ->
            def folders = (profile == UPLOAD_PROFILE_ENUMS.mm.toString() ? MANUSCRIPT_MASTER_FOLDER_NAME : BOOK_MASTER_FOLDER_NAME)
            folders.each { String folder ->
                File directory = new File(folder)
                if (UploadUtils.hasAtleastOnePdf(directory)) {
                    println "Processing directory: $directory"
                    GmailHandler.loginAndUpload(metaDataMap, profile, folder)
                } else {
                    println "No Files uploadable to Google Drive for Profile $profile"
                }
            }
        }
        println "***Gmail Upload Browser Launches Done"
    }
}

