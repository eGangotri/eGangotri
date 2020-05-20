package com.egangotri.upload.archive

import com.egangotri.util.EGangotriUtil

class StartFreshLogFile {
    static void main(String[] args) {
        def GOOGLE_DRIVE_PATH = EGangotriUtil.EGANGOTRI_BASE_DIR + File.separator + "google_drive/archive_uploader/server_logs"
        def filePath = "${GOOGLE_DRIVE_PATH}/egangotri_focus.log"

        def file = new File(filePath)
        if(file.exists()){
            println("file ${file} exists. now erasing");
            file.write("")
        }
        else{
            println("no ${file} file yet")
        }
    }
}
