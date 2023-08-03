package com.egangotri.upload.archive

import com.egangotri.rest.UploadRestApiCalls
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.util.EGangotriUtil

class UploadToArchiveForMissed {
    void execute() {
        if(SettingsUtil.WRITE_TO_MONGO_DB){
            try{
                UploadRestApiCalls.addToUploadCycle(EGangotriUtil.UPLOAD_CYCLE_ID,profiles);
            }
            catch(Exception e){
                log.info("Exception calling addToUshered",e)
            }
        }

    }
}
