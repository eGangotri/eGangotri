package com.egangotri.upload.vo

import com.egangotri.upload.util.ArchiveUtil
import groovy.transform.EqualsAndHashCode

class UsheredVO extends UploadVO{
    String archiveLink

    UsheredVO(List<String> fields){
        super(fields[0],fields[2])
        archiveProfile = fields[0]
        uploadLink = fields[1]?.replaceAll("\"", "'")
        path = fields[2]
        title = fields[3]
        archiveLink = ArchiveUtil.ARCHIVE_DOCUMENT_DETAIL_URL + "/" + fields[4]
    }
    @Override
    public String toString(){
        return super.toString() + " \n" + archiveLink
    }
}
