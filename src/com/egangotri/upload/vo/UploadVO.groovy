package com.egangotri.upload.vo

import static com.egangotri.upload.util.UploadUtils.generateUploadUrl
import static com.egangotri.upload.util.UploadUtils.removeFileEnding
import static com.egangotri.upload.util.UploadUtils.stripFilePath

class UploadVO {
    String archiveProfile
    String uploadLink
    String path
    String title

    UploadVO(String _archiveProfile, String _path) {
        archiveProfile = _archiveProfile
        uploadLink = generateUploadUrl(archiveProfile, _path)
        path = _path
        title = stripFilePath(removeFileEnding(path))
    }

    UploadVO(List<String> fields) {
        archiveProfile = fields[0]
        uploadLink = fields[1]?.replaceAll("\"", "'")
        path = fields[2]
        title = fields[3]
    }

    @Override
    String toString() {
        return archiveProfile + " \n" + uploadLink + " \n" + path + " \n" + title
    }

    @Override
    boolean equals(Object vo) {
        if (this == vo) return true;
        if (!vo) return false;
        if (this.getClass() != vo.getClass()) return false;
        def _vo = (UploadVO) vo;
        return this.path == _vo.path && this.archiveProfile == _vo.archiveProfile
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.path == null) ? 0 : this.path.hashCode());
        result = prime * result + ((this.archiveProfile == null) ? 0 : this.archiveProfile.hashCode());
        return result;
    }
}
