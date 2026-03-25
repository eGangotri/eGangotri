package com.egangotri.upload.archive.uploaders

import com.egangotri.rest.RestUtil
import com.egangotri.rest.UploadRestApiCalls
import com.egangotri.upload.archive.PreUploadReview
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class UploadersUtil {
    static boolean previewSuccess = true
    static Set<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES as Set
    static Hashtable<String, String> archiveLoginsMetaDataMap;
    static String PERCENT_SIGN_AS_FILE_SEPARATOR = "%"

    static setUploadCycleId(String uploadCycleId = "") {
        if (uploadCycleId != "") {
            EGangotriUtil.UPLOAD_CYCLE_ID = uploadCycleId
        }
        if (EGangotriUtil.UPLOAD_CYCLE_ID?.trim()?.size() == 0) {
            EGangotriUtil.UPLOAD_CYCLE_ID = UUID.randomUUID()
        };
        log.info("EGangotriUtil.UPLOAD_RUN_ID:" + EGangotriUtil.UPLOAD_CYCLE_ID)
        return uploadCycleId
    }
    static boolean checkIfMongoOn() {
        if (SettingsUtil.WRITE_TO_MONGO_DB) {
            RestUtil.startDBServerIfOff()
            boolean isOn = RestUtil.checkIfDBServerIsOn()
            if (!isOn) {
                log.info("This Upload Run is configured to write to DB but the DB Server is not on.\nHence cannot proceed")
                return
            } else {
                log.info("Mongo is running")
            }
            return isOn
        }
        return true
    }

    static void  setProfileForUpload(String profile) {
            UploadersUtil.archiveProfiles = [profile]
    }
    static void prelims() {
        UploadersUtil.archiveLoginsMetaDataMap = UploadUtils.getAllArchiveLogins() as Hashtable<String, String>
        SettingsUtil.applySettings()
        if(!checkIfMongoOn()){
            System.exit(0)
        }

    }
    static void checkReview(Collection profiles) {
        if (SettingsUtil.PREVIEW_FILES) {
            UploadersUtil.previewSuccess = PreUploadReview.preview(profiles as Set)
        }
        if (!UploadersUtil.previewSuccess) {
            log.info("Preview failed");
            System.exit(0);
        }
    }

    static Set<String> orderProfiles(List<String> argsList) {
        String subjectDescArg = argsList.find { String param -> param.toLowerCase().startsWith('subjectdesc=') }
        if (subjectDescArg) {
            UploadUtils.DEFAULT_SUBJECT_DESC = subjectDescArg.split('=')[1]
            log.info("Found subjectDesc: ${UploadUtils.DEFAULT_SUBJECT_DESC}")
            argsList.remove(subjectDescArg) // Remove the subjectdesc entry from argsList
            log.info("Removed subjectDesc from args, remaining: ${argsList}")
        }
        else {
            UploadUtils.DEFAULT_SUBJECT_DESC = '';
            log.info('No subjectDesc found in args, setting DEFAULT_SUBJECT_DESC to empty string')
        }
        Set<String> profiles = ArchiveUtil.filterInvalidProfiles(argsList, UploadersUtil.archiveLoginsMetaDataMap) as Set
        log.info("DEFAULT_SUBJECT_DESC: ${UploadUtils.DEFAULT_SUBJECT_DESC} argsList ${argsList}")
        return profiles
    }
    static void prelimsWithVOs(String profile, Set<QueuedVO> vos, String uploadCycleId = "") {
        UploadersUtil.archiveProfiles = Collections.singleton(profile);
        UploadersUtil.archiveLoginsMetaDataMap = UploadUtils.getAllArchiveLogins()
        SettingsUtil.applySettings()
        UploadersUtil.archiveProfiles = ArchiveUtil.filterInvalidProfiles(UploadersUtil.archiveProfiles, UploadersUtil.archiveLoginsMetaDataMap) as Set
        UploadersUtil.setUploadCycleId(uploadCycleId)
        checkIfMongoOn()
        if (SettingsUtil.PREVIEW_FILES) {
            UploadersUtil.previewSuccess = PreUploadReview.previewUsingVos(profile, vos)
        }
        if (!UploadersUtil.previewSuccess) {
            log.info("Preview failed");
            System.exit(0);
        }
    }

    static void addToUploadCycleWithMode(Collection profiles, String mode = "") {
        if (SettingsUtil.WRITE_TO_MONGO_DB) {
            try {
                Map<String, Object> result = UploadRestApiCalls.addToUploadCycle(profiles, mode);
                if (!result?.success) {
                    log.info("${result}. mongo call to UploadRestApiCalls.addToUploadCycle failed. quitting")
                    System.exit(0)
                }
            }
            catch (Exception e) {
                log.info("Exception calling addToUploadCycle", e.message)
                System.exit(0)
            }
        }
    }

    static void addToUploadCycleWithMode(String profile, String mode = "") {
        addToUploadCycleWithMode([profile],mode)
    }

    static void addToUploadCycleWithModeV2(String profile, List<String> uplodables, String mode = "") {
        if (SettingsUtil.WRITE_TO_MONGO_DB) {
            try {
                Map<String, Object> result = UploadRestApiCalls.addToUploadCycleV2(profile, uplodables, mode);
                if (!result?.success) {
                    log.info("${result}. mongo call to addToUploadCycleV2 failed. quitting")
                    System.exit(0)
                }
            }
            catch (Exception e) {
                log.info("Exception calling addToUploadCycle", e.message)
                System.exit(0)
            }
        }
    }
}

