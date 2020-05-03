package com.egangotri.upload.vo

import static com.egangotri.upload.util.UploadUtils.removeFileEnding
import static com.egangotri.upload.util.UploadUtils.stripFilePath

class UploadVO {
    String archiveProfile
    String uploadLink
    String path
    String title

    UploadVO(String _archiveProfile, String _path) {
        archiveProfile = _archiveProfile
        uploadLink = ""
        path = _path
        title = stripFilePath(removeFileEnding(path))
    }

    String toString() {
        return archiveProfile + " \n" + uploadLink + " \n" + path + " \n" + title
    }
}
