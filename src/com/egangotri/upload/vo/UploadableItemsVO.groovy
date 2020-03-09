package com.egangotri.upload.vo

import static com.egangotri.upload.util.UploadUtils.*

class UploadableItemsVO extends UploadVO {
    UploadableItemsVO(String _archiveProfile, String _fullFilePath){
        super()
        archiveProfile = _archiveProfile
        fullFilePath = _fullFilePath
        fileTitle = getFileTitleOnly(removeFileEnding(fullFilePath))
        uploadLink = generateURL(archiveProfile, fullFilePath)
    }
}
