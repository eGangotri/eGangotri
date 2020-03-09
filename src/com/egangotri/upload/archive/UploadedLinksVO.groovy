package com.egangotri.upload.archive

class UploadedLinksVO {
    String uploadLink
    String fullFilePath
    String fileTitle
    String archiveLink
    String archiveProfile

   /* UploadedLinksVO(String[] fields){
        UploadedLinksVO(fields.toList())
    }*/

    UploadedLinksVO(List fields){
        archiveProfile = fields[0]
        uploadLink = fields[1]
        fullFilePath = fields[2]
        fileTitle = fields[3]
        archiveLink = fields[4]
    }
}
