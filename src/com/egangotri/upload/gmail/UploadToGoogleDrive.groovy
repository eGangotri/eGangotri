package com.egangotri.upload.gmail

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import groovy.io.FileType
import org.slf4j.*

import java.nio.file.FileVisitResult

/**
 * Created by user on 1/22/2016.
 */

class UploadToGoogleDrive {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)
    static List<String> MANUSCRIPT_MASTER_FOLDER_NAME = [FileUtil.DT_DEFAULT]
    static List<String> BOOK_MASTER_FOLDER_NAME = [FileUtil.JG_DEFAULT, FileUtil.RK_DEFAULT]

    static final List UPLOAD_PROFILES = [UPLOAD_PROFILE_ENUMS.bm, UPLOAD_PROFILE_ENUMS.mm]
    static enum UPLOAD_PROFILE_ENUMS {
        bm, mm
    }

    static main(args) {
        Log.info("start")
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
        List uploadProfiles = UPLOAD_PROFILES
        if (args) {
            Log.info("args $args")
            uploadProfiles = args.toList()
        }
        execute(uploadProfiles, metaDataMap)
    }

    public static void execute(List profiles, Map metaDataMap) {
        Log.info "Start uploading to Google Drive"
        profiles*.toString().eachWithIndex { uploadProfile, index ->
            List<String> folders = resolveFolderBasedOnProfile(uploadProfile)
            Log.info("folders: $folders")
            Log.info "${index + 1}). Uploading to Google Drive for Profile $uploadProfile"
            folders.eachWithIndex { String folder, idx ->
                File directory = new File(folder)
                if (UploadUtils.hasAtleastOnePdf(directory)) {
                    Log.info "UploadToGoogleDrive: Processing directory for Upload: $directory"
                    //GoogleDriveHandler.loginAndUpload(metaDataMap, uploadProfile, folder)
                } else {
                    Log.info "No Files uploadable to Google Drive for folder $folder in Profile $uploadProfile"
                }
            }
        }
        Log.info "***UploadToGoogleDrive Browser Launches Done"
    }

    static List<String> resolveFolderBasedOnProfile(String uploadProfile) {
        if (uploadProfile.toString().equals(UPLOAD_PROFILE_ENUMS.mm.toString())) {
            return MANUSCRIPT_MASTER_FOLDER_NAME
        } else {
            return BOOK_MASTER_FOLDER_NAME
        }
    }
}

