package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import org.slf4j.*

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
class UploadToArchive {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static
    final List ARCHIVE_PROFILES = [ArchiveHandler.PROFILE_ENUMS.dt, ArchiveHandler.PROFILE_ENUMS.rk, ArchiveHandler.PROFILE_ENUMS.ib, ArchiveHandler.PROFILE_ENUMS.jg]

    static main(args) {
        List archiveProfiles = ARCHIVE_PROFILES
        if (args) {
            Log.info "args $args"
            archiveProfiles = args.toList()
        }

        Map metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/UserIdsMetadata.properties")
        execute(archiveProfiles, metaDataMap)
    }

    public static boolean execute(List profiles, Map metaDataMap) {
        Log.info "Start uploading to Archive"
        profiles*.toString().eachWithIndex { archiveProfile,  index ->
            Log.info "${index+1}). Uploading to archive.org for Profile $archiveProfile"
            if (UploadUtils.hasAtleastOneUploadablePdfForProfile(archiveProfile)) {
                //ArchiveHandler.uploadToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
            } else {
                Log.info "No Files uploadable for Profile $archiveProfile"
            }
        }
        Log.info "***Browser for Archive Upload Launches Done"
        return true
    }
}


