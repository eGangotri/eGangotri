package com.egangotri.upload.vo

class LinksVO extends UploadVO{
    String archiveLink

    LinksVO(List<String> fields){
        super()
        archiveProfile = fields[0]
        uploadLink = fields[1]
        path = fields[2]
        title = fields[3]
        archiveLink = "https://archive.org/details/" + fields[4]
    }
    @Override
    public String toString(){
        return super.toString() + " \n" + archiveLink
    }
}
