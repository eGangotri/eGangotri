package com.egangotri.upload.archive.uploaders

class UploadItemFromExcelVO {
    String absolutePath;
    String subject;
    String description;
    String creator;
    Boolean uploadFlag

    UploadItemFromExcelVO(String absolutePath, String subject, String description, String creator, Boolean _uploadFlag) {
        this.absolutePath = absolutePath;
        this.subject = subject;
        this.description = description;
        this.creator = creator;
        this.uploadFlag = _uploadFlag
    }
}
