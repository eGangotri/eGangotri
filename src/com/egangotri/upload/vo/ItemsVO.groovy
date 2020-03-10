package com.egangotri.upload.vo

import static com.egangotri.upload.util.UploadUtils.*

class ItemsVO extends UploadVO {
    ItemsVO(String _archiveProfile, String _fullFilePath){
        super()
        archiveProfile = _archiveProfile
        fullFilePath = _fullFilePath
        fileTitle = getFileTitleOnly(removeFileEnding(fullFilePath))
        uploadLink = generateURL(archiveProfile, fullFilePath)
    }

    ItemsVO(List<String> fields){
        super()
        archiveProfile = fields[0]
        uploadLink = fields[1]
        fullFilePath = fields[2]
        fileTitle = fields[3]
    }
}
