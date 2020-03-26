package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class PreUploadReview {
    static int MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME = 6
    static void main(String[] args) {
        List<String> archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        SettingsUtil.applySettings(false)
        Set<String> purgedProfiles = ArchiveUtil.filterInvalidProfiles(archiveProfiles, metaDataMap)
        preview(purgedProfiles)
        System.exit(0)
    }

    static boolean preview(Set<String> profiles) {
        Map<String, List<String>> profileAndInvalidNames = [:]
        Map<String, List<String>> profileAndNames = [:]
        Set<String> setOfEndings = [] as Set
        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = ArchiveUtil.getGrandTotalOfAllUploadables(profiles)
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} items")
        profiles.eachWithIndex { archiveProfile, index ->
            List<String> uploadablesForProfile = UploadUtils.getUploadablesForProfile(archiveProfile)
            if (uploadablesForProfile) {
                List<String> shortNames = []
                List<String> names = []
                uploadablesForProfile.each { String entry ->
                    setOfEndings << UploadUtils.getFileEnding(entry)
                    String stripPath = UploadUtils.stripFilePath(entry)
                    names << "${stripPath} [\t ${entry} ]"
                    if (stripPath.length() < SettingsUtil.MINIMUM_FILE_NAME_LENGTH
                    ) {
                        shortNames << "${stripPath} [\t ${entry} ]"
                    }
                }
                if (shortNames) {
                    profileAndInvalidNames.put(archiveProfile, shortNames)
                }
                profileAndNames.put(archiveProfile, names)
            }
        }
        log.info("This upload has following Unique Path Endings ${setOfEndings}")
        if (profileAndNames) {
            log.info("The Following are the files that will be uploaded")
            profileAndNames.eachWithIndex { Map.Entry<String, List<String>> entry, int index ->
                log.info "${index + 1}). ${entry.key}"
                log.info("${entry.value.join("\n")}")
            }

            if (profileAndInvalidNames) {
                log.info("The Following files have names less than ${SettingsUtil.MINIMUM_FILE_NAME_LENGTH} characters")
                profileAndInvalidNames.eachWithIndex { Map.Entry<String, List<String>> entry, int index ->
                    log.info "${index + 1}). ${entry.key}"
                    entry.value.eachWithIndex{ String val, int i ->
                        log.info(" ${i+1}). $val}")
                    }
                }
                log.info("Cannot proceed because there are file names shorter than the Minimum Requirement Or Have More than ${MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME} Digits in file name.")
            }
            else{
                log.info("All texts are above minimum file length requirement")
            }

            return profileAndInvalidNames.size() == 0
        }
    }

    static boolean fileNameHasOverAllowedDigits(String fileName) {
        return fileName.findAll( /\d+/ ).join("").size() > MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME
    }
}
