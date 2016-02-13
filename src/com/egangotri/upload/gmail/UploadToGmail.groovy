package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import org.apache.commons.logging.LogFactory
import org.slf4j.LoggerFactory

import java.util.logging.Logger

/**
 * Created by user on 1/22/2016.
 */

class UploadToGmail {
    final static org.slf4j.Logger Log = LoggerFactory.getLogger(this.class);
    static List MANUSCRIPT_MASTER_FOLDER_NAME = [FileUtil.DT_DEFAULT]
    static List BOOK_MASTER_FOLDER_NAME = [FileUtil.JG_DEFAULT, FileUtil.RK_DEFAULT]

    static final List UPLOAD_PROFILES = [UPLOAD_PROFILE_ENUMS.bm/*, UPLOAD_PROFILE_ENUMS.mm*/]
    static enum UPLOAD_PROFILE_ENUMS {
        bm, mm
    }

    static main(args) {
        Log.info ("start")
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
        List uploadProfiles = UPLOAD_PROFILES
        if (args) {
            Log.info ("args $args")
            uploadProfiles = args.toList()
        }
        execute(uploadProfiles, metaDataMap)
    }

    public static void execute(List profiles, Map metaDataMap) {
        Log.info "Start uploading to Gmail"
        profiles*.toString().each { String profile ->
            def folders = (profile == UPLOAD_PROFILE_ENUMS.mm.toString() ? MANUSCRIPT_MASTER_FOLDER_NAME : BOOK_MASTER_FOLDER_NAME)
            folders.each { String folder ->
                File directory = new File(folder)
                if (UploadUtils.hasAtleastOnePdf(directory,false)) {
                    Log.info "UploadToGmail: Processing directory for Upload: $directory"
                    //GmailHandler.loginAndUpload(metaDataMap, profile, folder)
                } else {
                    Log.info "No Files uploadable to Google Drive for Profile $profile"
                }
            }
        }
        Log.info "***Gmail Upload Browser Launches Done"
    }
}

