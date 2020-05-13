package com.egangotri.upload.vo

import static com.egangotri.upload.util.UploadUtils.*

/**
 * represents an uploadable Doc that gets queued for Uploading in a given execution.
 * different from UsheredVO which represents a QueuedVO that has been ushered for uploading
 */
class QueuedVO extends UploadVO {
    QueuedVO(String _archiveProfile, String _fullFilePath){
        super(_archiveProfile,_fullFilePath)
    }

    QueuedVO(List<String> fields){
        super(fields)
    }
    @Override
    boolean equals(Object vo){
        return super.equals(vo)
    }

    @Override
    int hashCode(){
        return super.hashCode()
    }
}
