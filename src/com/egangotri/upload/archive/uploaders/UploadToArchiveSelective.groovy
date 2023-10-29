package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class UploadToArchiveSelective {
    static void main(String[] args) {
        List<String> archiveProfiles = []
        if (args && args.length == 2) {
            log.info "args $args"
            archiveProfiles = args[0]
        } else {
            log.info "Must have 2 arg.s Profile name and title of pdf"
            System.exit(0)
        }
        UploadToArchive.prelims(args)
    }
}
