package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import org.apache.commons.logging.LogFactory

/**
 * Created by user on 1/19/2016.
 * Only Logs In
 */
class LoginToGoogleDrive {
    static org.apache.commons.logging.Log Log = LogFactory.getLog(this.class)

    static final List LOGIN_PROFILES = ["jm", "lk", "sr"] // "bm", "mm", "jm" , "lk", "sr", "srCP" , "ij", "srb1", gb11

    static main(args) {
        Log.info "start$args"
        List loginProfiles = LOGIN_PROFILES
        if (args) {
            Log.info "args $args"
            loginProfiles = args.toList()
        }

        loginProfiles.each { profile ->
            Log.info "profile: $profile"
            def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
            GoogleDriveHandler.login(metaDataMap, profile)
        }
    }
}
