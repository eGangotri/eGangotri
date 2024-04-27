package com.egangotri.upload.archive.uploaders

class UploadItemFromExcel {
    String absolutePath;
    String subject;
    String description;
    String creator;

    UploadItemFromExcel( String absolutePath,String subject, String description, String creator) {
        this.absolutePath = absolutePath;
        this.subject = subject;
        this.description = description;
        this.creator = creator;
    }
}
