package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import groovy.util.logging.Slf4j

@Slf4j
class TestFileNameLengths {
    static int MINIMUM_LENGTH = 20

    static boolean testLengths(List<String> profiles) {
        Map<String, List<String>> profileAndInvalidNames = [:]
        Map<String, List<String>> profileAndNames = [:]
        Set<String> setOfEndings = [] as Set
        profiles.eachWithIndex { archiveProfile, index ->
            List<String> uploadablesForProfile = UploadUtils.getUploadablesForProfile(archiveProfile)
            if (uploadablesForProfile) {
                List<String> shortNames = []
                List<String> names = []
                uploadablesForProfile.each { String entry ->
                    setOfEndings << UploadUtils.getFileEnding(entry)
                    String stripPath = UploadUtils.stripFilePath(entry)
                    names << "${stripPath} [\t ${entry} ]"
                    if (stripPath.length() < MINIMUM_LENGTH) {
                        shortNames << "${stripPath} [\t ${entry} ]"
                    }
                }
                if (shortNames) {
                    profileAndInvalidNames.put(archiveProfile, shortNames)
                }
                profileAndNames.put(archiveProfile, shortNames)
            }
        }
        log.info("This upload has folowing Unique Path Endings ${setOfEndings}")
        if (profileAndNames) {
            log.info("The Following are the files that will be uploaded")
            profileAndNames.eachWithIndex { Map.Entry<String, List<String>> entry, int index ->
                log.info "${index + 1}). ${entry.key}"
                log.info("${entry.value.join("\n")}")
            }

            if (profileAndInvalidNames) {
                log.info("The Following files need name-changes")
                profileAndInvalidNames.eachWithIndex { Map.Entry<String, List<String>> entry, int index ->
                    log.info "${index + 1}). ${entry.key}"
                    log.info("${entry.value.join("\n")}")
                }
                log.info("Cannot proceed because there are file names shorter than the Minimum")
            }

            return profileAndInvalidNames.size() == 0
        }
    }
}
