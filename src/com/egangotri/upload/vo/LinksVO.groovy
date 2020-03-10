package com.egangotri.upload.vo

class LinksVO extends UploadVO{
    String archiveLink

    LinksVO(List<String> fields){
        super()
        archiveProfile = fields[0]
        uploadLink = fields[1]
        fullFilePath = fields[2]
        fileTitle = fields[3]
        archiveLink = "https://archive.org/details/" + fields[4]
    }
}
