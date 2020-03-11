package com.egangotri.upload.vo

class UploadVO {
    String archiveProfile
    String uploadLink
    String path
    String title

    public String toString(){
        return archiveProfile + " \n" + uploadLink  + " \n" + path  + " \n" + title
    }
}
