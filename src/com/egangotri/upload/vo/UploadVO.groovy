package com.egangotri.upload.vo

import groovy.transform.EqualsAndHashCode

import static com.egangotri.upload.util.UploadUtils.generateUploadUrl
import static com.egangotri.upload.util.UploadUtils.removeFileEnding
import static com.egangotri.upload.util.UploadUtils.stripFilePath

@EqualsAndHashCode(includes="archiveProfile, path")
class UploadVO {
    String archiveProfile
    String uploadLink
    String path
    String title

    UploadVO(String _archiveProfile, String _path) {
        archiveProfile = _archiveProfile
        uploadLink = generateUploadUrl(archiveProfile, _path)
        path = _path
        title = stripFilePath(removeFileEnding(path))
    }

    UploadVO(List<String> fields) {
        archiveProfile = fields[0]
        uploadLink = fields[1]?.replaceAll("\"", "'")
        path = fields[2]
        title = fields[3]
    }

    @Override
    String toString() {
        return archiveProfile + " \n" + uploadLink + " \n" + path + " \n" + title
    }
}
