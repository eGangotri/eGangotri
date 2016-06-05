package com.egangotri.upload

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.gmail.UploadToGoogleDrive
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.slf4j.*

/**
 * Created by user on 2/10/2016.
 */
@Slf4j
class UploadToArchiveAndGoogleDrive {
    static
    final List ARCHIVE_PROFILES = [/*ArchiveHandler.PROFILE_ENUMS.DT ,ArchiveHandler.PROFILE_ENUMS.RK,*/ ArchiveHandler.ARCHIVE_PROFILE.IB, ArchiveHandler.ARCHIVE_PROFILE.JG]
    static final List UPLOAD_PROFILES = [UploadToGoogleDrive.UPLOAD_PROFILE.BM, UploadToGoogleDrive.UPLOAD_PROFILE.MM]

    static main(args) {
        List archiveProfiles = ARCHIVE_PROFILES
        List uploadProfiles = UPLOAD_PROFILES

        if (args) {
           log.info "args $args"
            args.toList().each { String arg ->
                String[] argVals = arg.split("=")
                if (argVals[0] == "-u") {
                    uploadProfiles = argVals[1].split(",").collect{it.trim()}
                }
                else if (argVals[0] == "-a") {
                    archiveProfiles = argVals[1].split(",").collect{it.trim()}
                }
            }

           log.info "uploadProfiles ${uploadProfiles*.toString()}"
           log.info "archiveProfiles ${archiveProfiles*.toString()}"
        }

       log.info "UploadToArchiveAndGmail"
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        UploadToArchive.execute(archiveProfiles, metaDataMap)
        UploadToGoogleDrive.execute(uploadProfiles, metaDataMap)
    }
}
