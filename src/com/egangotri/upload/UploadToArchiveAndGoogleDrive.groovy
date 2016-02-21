package com.egangotri.upload

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.gmail.UploadToGoogleDrive
import com.egangotri.upload.util.UploadUtils
import org.slf4j.*

/**
 * Created by user on 2/10/2016.
 */
class UploadToArchiveAndGoogleDrive {
    final static Logger Log = LoggerFactory.getLogger(UploadToArchiveAndGoogleDrive.class);

    static
    final List ARCHIVE_PROFILES = [/*ArchiveHandler.PROFILE_ENUMS.dt ,ArchiveHandler.PROFILE_ENUMS.rk,*/ ArchiveHandler.PROFILE_ENUMS.ib, ArchiveHandler.PROFILE_ENUMS.jg]
    static final List UPLOAD_PROFILES = [UploadToGoogleDrive.UPLOAD_PROFILE_ENUMS.bm, UploadToGoogleDrive.UPLOAD_PROFILE_ENUMS.mm]

    static main(args) {
        List archiveProfiles = ARCHIVE_PROFILES
        List uploadProfiles = UPLOAD_PROFILES

        if (args) {
            Log.info "args $args"
            args.toList().each { String arg ->
                String[] argVals = arg.split("=")
                if (argVals[0] == "-u") {
                    uploadProfiles = argVals[1].split(",").toList()
                }
                else if (argVals[0] == "-a") {
                    archiveProfiles = argVals[1].split(",").toList()
                }
            }

            Log.info "uploadProfiles ${uploadProfiles*.toString()}"
            Log.info "archiveProfiles ${archiveProfiles*.toString()}"
        }

        Log.info "UploadToArchiveAndGmail"
        Map metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/UserIdsMetadata.properties")

        UploadToArchive.execute(archiveProfiles, metaDataMap)
        UploadToGoogleDrive.execute(uploadProfiles, metaDataMap)
    }
}
