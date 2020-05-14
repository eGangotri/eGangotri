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
        if (this == vo) return true;
        if (!vo) return false;
        if (this.getClass() != vo.getClass()) return false;
        def _vo = (QueuedVO) vo;
        return this.path == _vo.path && this.archiveProfile == _vo.archiveProfile
    }

    @Override
    int hashCode(){
        return super.hashCode()
    }
}
