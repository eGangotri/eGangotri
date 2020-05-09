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
    final Set<String> ARCHIVE_PROFILES = [] as Set
    static final Set<String> UPLOAD_PROFILES = [] as Set

    static main(String[] args) {
        Set<String> archiveProfiles = ARCHIVE_PROFILES
        Set<String> uploadProfiles = UPLOAD_PROFILES

        if (args) {
           log.info "args $args"
            args.toList().each { String arg ->
                String[] argVals = arg.split("=")
                if (argVals[0] == "-u") {
                    uploadProfiles = argVals[1].split(",").collect{it.trim()} as Set
                }
                else if (argVals[0] == "-a") {
                    archiveProfiles = argVals[1].split(",").collect{it.trim()} as Set
                }
            }

           log.info "uploadProfiles $uploadProfiles"
           log.info "archiveProfiles $archiveProfiles"
        }

       log.info "UploadToArchiveAndGmail"
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        UploadToArchive.execute(archiveProfiles, metaDataMap)
        UploadToGoogleDrive.execute(uploadProfiles, metaDataMap)
    }
}
