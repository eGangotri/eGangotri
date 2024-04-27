package com.egangotri.upload.vo

import groovy.transform.EqualsAndHashCode

/**
 * represents an uploadable Doc that gets queued for Uploading in a given execution.
 * different from UsheredVO which represents a QueuedVO that has been ushered for uploading
 */
class QueuedVO extends UploadVO {
    QueuedVO(String _archiveProfile, String _fullFilePath){
        super(_archiveProfile,_fullFilePath)
    }

    QueuedVO(String _archiveProfile,
             String _fullFilePath,
             String _subjects,
             String _desc,
             String creator = ""){
        super(_archiveProfile,_fullFilePath,_subjects, _desc, creator)
    }

    QueuedVO(List<String> fields){
        super(fields)
    }
}
