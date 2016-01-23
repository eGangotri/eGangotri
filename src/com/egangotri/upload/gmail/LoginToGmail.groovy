package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils

/**
 * Created by user on 1/19/2016.
 * Only Logs In
 */
class LoginToGmail {
    static final String LOGIN_PROFILE = "sr" // "bm", "mm", "jm" , "lk", "sr", "srCP" , "ij"

    static main(args) {
        println "start"
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
        GmailHandler.login(metaDataMap,LOGIN_PROFILE)
    }

}
