package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import org.slf4j.*

/**
 * Created by user on 1/22/2016.
 */

class UploadToGoogleDrive {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static final List UPLOAD_PROFILES = [UPLOAD_PROFILE.BM, UPLOAD_PROFILE.MM]
    static enum UPLOAD_PROFILE {
        BM, MM
    }

    static main(args) {
        Log.info("start")
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.GOOGLE_DRIVE_PROPERTIES_FILE)
        List uploadProfiles = UPLOAD_PROFILES
        if (args) {
            Log.info("args $args")
            uploadProfiles = args.toList()
        }
        execute(uploadProfiles, metaDataMap)
    }

    public static void execute(List profiles, Hashtable<String, String> metaDataMap) {
        Log.info "Start uploading to Google Drive"
        profiles*.toString().eachWithIndex { uploadProfile, index ->
            List<String> folders = resolveFolderBasedOnGoogleDriveProfile(uploadProfile)
            Log.info("folders: $folders")
            Log.info "${index + 1}). Uploading to Google Drive for Profile $uploadProfile"
            folders.eachWithIndex { String folder, idx ->
                File directory = new File(folder)
                if (UploadUtils.hasAtleastOnePdf(directory)) {
                    Log.info "UploadToGoogleDrive: Processing directory for Upload: $directory"
                    GoogleDriveHandler.loginAndUpload(metaDataMap, uploadProfile, folder)
                } else {
                    Log.info "No Files uploadable to Google Drive for folder $folder in Profile $uploadProfile"
                }
            }
        }
        Log.info "***UploadToGoogleDrive Browser Launches Done"
    }

    static List<String> resolveFolderBasedOnGoogleDriveProfile(String uploadProfile) {
        if(uploadProfile == UPLOAD_PROFILE.MM.toString()){
            return EGangotriUtil.manuscriptFolders()
        } else {
            return EGangotriUtil.nonManuscriptFolders()
        }
    }
}

