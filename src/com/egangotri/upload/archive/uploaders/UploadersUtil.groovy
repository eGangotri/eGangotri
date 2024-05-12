package com.egangotri.upload.archive.uploaders

import com.egangotri.rest.RestUtil
import com.egangotri.rest.UploadRestApiCalls
import com.egangotri.upload.archive.PreUploadReview
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class UploadersUtil {
    static boolean previewSuccess = true
    static Set<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES as Set
    static Hashtable<String, String> metaDataMap;
    static String PERCENT_SIGN_AS_FILE_SEPARATOR = "%"

    static checkIfMongoOn(String uploadCycleId = "") {
        if (SettingsUtil.WRITE_TO_MONGO_DB) {
            RestUtil.startDBServerIfOff()
            boolean isOn = RestUtil.checkIfDBServerIsOn()
            if (!isOn) {
                log.info("This Upload Run is configured to write to DB but the DB Server is not on.\nHence cannot proceed")
                return
            } else {
                log.info("Mongo is running")
                if(uploadCycleId != ""){
                    EGangotriUtil.UPLOAD_CYCLE_ID = uploadCycleId
                }
                if (EGangotriUtil.UPLOAD_CYCLE_ID?.trim()?.size() == 0) {
                    EGangotriUtil.UPLOAD_CYCLE_ID = UUID.randomUUID()
                };
                log.info("EGangotriUtil.UPLOAD_RUN_ID:" + EGangotriUtil.UPLOAD_CYCLE_ID)
            }
        }
    }

    static void prelims(String[] args, String uploadCycleId = "") {
        if (args) {
            log.info "args $args"
            UploadersUtil.archiveProfiles = args.toList()
        }
        UploadersUtil.metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        SettingsUtil.applySettings()
        UploadersUtil.archiveProfiles = ArchiveUtil.filterInvalidProfiles(UploadersUtil.archiveProfiles, UploadersUtil.metaDataMap) as Set
        checkIfMongoOn(uploadCycleId)
        if (SettingsUtil.PREVIEW_FILES) {
            UploadersUtil.previewSuccess = PreUploadReview.preview(UploadersUtil.archiveProfiles)
        }
        if (!UploadersUtil.previewSuccess) {
            log.info("Preview failed");
            System.exit(0);
        }
    }


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
//static void prelims(String[] args) {
//    if (args) {
//        log.info "args $args"
//        archiveProfiles = args.toList()
//    }
//    metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
//    SettingsUtil.applySettings()
//    archiveProfiles = ArchiveUtil.filterInvalidProfiles(archiveProfiles, metaDataMap) as Set
//    checkIfMongoOn()
//    if (SettingsUtil.PREVIEW_FILES) {
//        previewSuccess = PreUploadReview.preview(archiveProfiles)
//    }
//    if (!previewSuccess) {
//        log.info("Preview failed");
//        System.exit(0);
//    }
//}

