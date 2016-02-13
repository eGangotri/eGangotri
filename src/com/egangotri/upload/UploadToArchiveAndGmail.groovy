package com.egangotri.upload

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.gmail.UploadToGmail
import com.egangotri.upload.util.UploadUtils
import org.slf4j.LoggerFactory

/**
 * Created by user on 2/10/2016.
 */
class UploadToArchiveAndGmail {
    final static org.slf4j.Logger Log = LoggerFactory.getLogger(this.class);

    static final List LOGIN_PROFILES = [UploadToGmail.UPLOAD_PROFILE_ENUMS.bm/*, UPLOAD_PROFILE_ENUMS.mm*/]
    static final List ARCHIVE_PROFILES = [/*ArchiveHandler.PROFILE_ENUMS.dt ,ArchiveHandler.PROFILE_ENUMS.rk,*/ArchiveHandler.PROFILE_ENUMS.ib, ArchiveHandler.PROFILE_ENUMS.jg]

    static main(args) {
        println "UploadToArchiveAndGmail"
        Map metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/UserIdsMetadata.properties")

        UploadToArchive.execute(ARCHIVE_PROFILES,metaDataMap)
        UploadToGmail.execute(LOGIN_PROFILES,metaDataMap)

    }
}
