package com.egangotri.upload.gmail

import com.egangotri.upload.util.UploadUtils

/**
 * Created by user on 1/19/2016.
 * Only Logs In
 */
class LoginToGmail {
    static final def LOGIN_PROFILES = ["sw22"] // "bm", "mm", "jm" , "lk", "sr", "srCP" , "ij", "srb1", gb11

    static main(args) {
        println "start"
        LOGIN_PROFILES.each { profile ->
            println "profile: $profile"
            def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/GmailData.properties")
            GmailHandler.login(metaDataMap, profile)
        }
    }
}
