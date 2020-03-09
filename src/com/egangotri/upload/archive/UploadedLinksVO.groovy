package com.egangotri.upload.archive

class UploadedLinksVO {
    String uploadLink
    String fullFilePath
    String fileTitle
    String archiveLink
    String archiveProfile

    UploadedLinksVO(List<String> fields){
        archiveProfile = fields[0]
        uploadLink = fields[1]
        fullFilePath = fields[2]
        fileTitle = fields[3]
        archiveLink = "https://archive.org/details/" + fields[4]
    }
}
