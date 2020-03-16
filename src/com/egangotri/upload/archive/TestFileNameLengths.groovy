package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import groovy.util.logging.Slf4j

@Slf4j
class TestFileNameLengths {
    static int MINIMUM_LENGTH = 20
    static boolean testLengths(List<String> profiles){
        Map<String, List<String>> profileAndShortNames = [:]
        Set<String> setOfEndings = [] as Set
        profiles.eachWithIndex { archiveProfile, index ->
            List<String> uploadablesForProfile = UploadUtils.getUploadablesForProfile(archiveProfile)
            if (uploadablesForProfile) {
                List<String> shortNames = []
                uploadablesForProfile.each{ String entry ->
                    setOfEndings << UploadUtils.getFileEnding(entry)
                    String stripPath = UploadUtils.stripFilePath(entry)
                    if(stripPath.length() < MINIMUM_LENGTH){
                        shortNames << "${stripPath} [\t ${entry} ]"
                    }
                }
                if(shortNames){
                    profileAndShortNames.put(archiveProfile, shortNames)
                }
            }
        }
        log.info("This upload has folowing Unique Path Endings ${setOfEndings}")
        if(profileAndShortNames){
            log.info("The Following files need name-changes")
            profileAndShortNames.eachWithIndex { Map.Entry<String, List<String>> entry, int index ->
            log.info "${index + 1}). ${entry.key}"
            log.info("${entry.value.join("\n")}")
        }
            log.info("Cannot proceed because there are file names shorter than the Minimum")
        }

        return profileAndShortNames.size() == 0
    }
}
