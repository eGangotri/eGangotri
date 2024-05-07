package com.egangotri.upload.archive.uploaders

import com.egangotri.rest.UploadRestApiCalls
import com.egangotri.upload.util.SettingsUtil
import groovy.util.logging.Slf4j

@Slf4j
class Util {
    static void addToUploadCycleWithMode(Collection profiles, String mode ="") {
        if(SettingsUtil.WRITE_TO_MONGO_DB){
            try{
                Map<String, Object> result = UploadRestApiCalls.addToUploadCycle(profiles,mode);
                if(!result?.success){
                    log.info("${result}. mongo call to addToUploadCycle failed. quitting")
                    System.exit(0)
                }
            }
            catch(Exception e){
                log.info("Exception calling addToUploadCycle",e.message)
                System.exit(0)
            }
        }
    }

    static void addToUploadCycleWithModeV2(String profiles, List<UploadItemFromExcelVO> uplodables, String mode ="") {
        if(SettingsUtil.WRITE_TO_MONGO_DB){
            try{
                Map<String, Object> result = UploadRestApiCalls.addToUploadCycleV2(profiles,uplodables,mode);
                if(!result?.success){
                    log.info("${result}. mongo call to addToUploadCycle failed. quitting")
                    System.exit(0)
                }
            }
            catch(Exception e){
                log.info("Exception calling addToUploadCycle",e.message)
                System.exit(0)
            }
        }
    }
}
