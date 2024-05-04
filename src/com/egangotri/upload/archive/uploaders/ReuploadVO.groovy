package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.vo.UploadVO

class ReuploadVO extends UploadVO{
    String archiveItemId;
    Boolean uploadFlag
    String archiveLink;

    ReuploadVO(String _path, String _uploadLink, String _archiveItemId, String _archiveProfile, Boolean _uploadFlag
                          ) {
        super(_path,_uploadLink, _archiveProfile);
        this.archiveItemId = _archiveItemId;
        this.uploadFlag = _uploadFlag;
        this.archiveLink = ArchiveUtil.ARCHIVE_DOCUMENT_DETAIL_URL + "/" + _archiveItemId
    }
}
