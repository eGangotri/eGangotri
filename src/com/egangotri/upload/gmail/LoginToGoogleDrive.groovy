package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import org.apache.commons.logging.LogFactory

/**
 * Created by user on 1/19/2016.
 * Only Logs In
 */
class LoginToGoogleDrive {
    static org.apache.commons.logging.Log Log = LogFactory.getLog(this.class)

    static final List<String> LOGIN_PROFILES = ["jm", "lk", "sr"] // "BM", "MM", "jm" , "lk", "sr", "srCP" , "ij", "srb1", gb11

    static main(args) {
        Log.info "start$args"
        List<String> loginProfiles = LOGIN_PROFILES
        if (args) {
            Log.info "args $args"
            loginProfiles = args.toList()
        }

        loginProfiles.each { String profile ->
            Log.info "profile: $profile"
            Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.GOOGLE_DRIVE_PROPERTIES_FILE)
            GoogleDriveHandler.login(metaDataMap, profile)
        }
    }
}
