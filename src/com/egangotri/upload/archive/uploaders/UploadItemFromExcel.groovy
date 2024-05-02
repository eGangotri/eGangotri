package com.egangotri.upload.archive.uploaders

class UploadItemFromExcel {
    String absolutePath;
    String subject;
    String description;
    String creator;
    Boolean uploadFlag

    UploadItemFromExcel( String absolutePath,String subject, String description, String creator, Boolean _uploadFlag) {
        this.absolutePath = absolutePath;
        this.subject = subject;
        this.description = description;
        this.creator = creator;
        this.uploadFlag = _uploadFlag
    }
}
