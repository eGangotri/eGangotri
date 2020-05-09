package com.egangotri.upload.gmail

import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class UploadToGoogleDrive {
    static List<String> UPLOAD_PROFILES = EGangotriUtil.getAllUploadProfiles()

    static main(String[] args) {
        log.info("start")
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.GOOGLE_DRIVE_PROPERTIES_FILE)
        List uploadProfiles = UPLOAD_PROFILES
        if (args) {
            log.info("args $args")
            uploadProfiles = args.toList()
        }
        execute(uploadProfiles, metaDataMap)
    }

    static void execute(Collection<String> profiles, Hashtable<String, String> metaDataMap) {
        log.info "Start uploading to Google Drive"
        Map<String, String> uploadSuccessCheckingMatrix = [:]
        profiles.eachWithIndex { String uploadProfile, index ->

            List<String> folders = []
            log.info("folders: $folders")
            log.info "${index + 1}). Uploading to Google Drive for Profile $uploadProfile"

            folders.eachWithIndex { String folder, idx ->
                log.info("folder: $folder")
                File directory = new File(folder)
                int countOfUploadablePdfs = FileRetrieverUtil.getAllFiles(directory)?.size()
                if (countOfUploadablePdfs) {
                    log.info "UploadToGoogleDrive: Upload $countOfUploadablePdfs docs to $directory"
                    boolean success = GoogleDriveHandler.loginAndUpload(metaDataMap, uploadProfile, folder)
                    uploadSuccessCheckingMatrix.put("${index+1}.${idx+1}", "$uploadProfile[$folder] \t $countOfUploadablePdfs Docs uploaded with  Exception Reported ${success?'None':' YES !!!!'}")

                } else {
                    log.info "No Files uploadable to Google Drive for folder $folder in Profile $uploadProfile"
                    uploadSuccessCheckingMatrix.put("${index+1}.${idx+1}", "$uploadProfile[$folder] \t $countOfUploadablePdfs Docs. nothing to upload ")
                }
            }
        }

        log.info "Upload Report:\n"

        uploadSuccessCheckingMatrix.each { k,v ->
            log.info "$k) $v"
        }

        log.info "***UploadToGoogleDrive Browser Launches Done"
    }

}

