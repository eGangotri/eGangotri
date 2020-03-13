package com.egangotri.upload.vo

import static com.egangotri.upload.util.UploadUtils.*

class ItemsVO extends UploadVO {
    ItemsVO(String _archiveProfile, String _fullFilePath){
        super()
        archiveProfile = _archiveProfile
        path = _fullFilePath
        title = stripFilePath(removeFileEnding(path))
        uploadLink = generateUploadUrl(archiveProfile, path)
    }

    ItemsVO(List<String> fields){
        super()
        archiveProfile = fields[0]
        uploadLink = fields[1]
        path = fields[2]
        title = fields[3]
    }
}
