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
        Map<String, List<FileData>> profileAndInvalidNames = [:]
        Map<String, List<FileData>> profileAndNames = [:]
        Set<String> setOfEndings = [] as Set
        List<String> setOfOffendingPaths = []
        ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION = ArchiveUtil.getGrandTotalOfAllUploadables(profiles)
        log.info("This Execution will target ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} items")
        log.info("This Execution will target Files of Cumulative Size ${ArchiveUtil.GRAND_TOTAL_OF_FILE_SIZE_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}")
        profiles.eachWithIndex { archiveProfile, index ->
            List<String> uploadablesForProfile = UploadUtils.getUploadablesForProfile(archiveProfile)
            if (uploadablesForProfile) {
                List<FileData> shortNames = []
                List<FileData> names = []
                uploadablesForProfile.each { String entry ->
                    setOfEndings << UploadUtils.getFileEnding(entry)
                    String stripPath = UploadUtils.stripFilePath(entry)
                    names << new FileData(stripPath, entry)
                    if (stripPath.length() < SettingsUtil.MINIMUM_FILE_NAME_LENGTH) {
                        String stripTitle = UploadUtils.stripFileTitle(entry)
                        setOfOffendingPaths << stripTitle
                        shortNames << new FileData(stripPath, entry)
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
            if (profileAndInvalidNames) {
                log.info("The Following files have names less than ${SettingsUtil.MINIMUM_FILE_NAME_LENGTH} characters")
                getOffendingFiles(profileAndInvalidNames)
                logOffendingFolders(setOfOffendingPaths)
                log.info("Cannot proceed because there are file names shorter than the Minimum Requirement Or Have More than ${MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME} Digits in file name.")
            }
            else{
                log.info("All texts are above minimum file length requirement")
                log.info("The Following are the files that will be uploaded")
                profileAndNames.eachWithIndex { Map.Entry<String, List<FileData>> entry, int index ->
                    log.info "${index + 1}). ${entry.key}"
                    log.info("\t${entry.value.join("\n\t")}")
                }
            }
            return profileAndInvalidNames.size() == 0
        }
    }

    static void logOffendingFolders(List<String> setOfOffendingPaths){
        log.info("This upload has following offending paths")
        (setOfOffendingPaths.groupBy {it}.sort { a, b -> b.value.size() <=> a.value.size() }).forEach{k,v ->
            log.info("$k\n : ${v.size()}")
        }

    }
    static void getOffendingFiles(Map<String, List<FileData>> profileAndInvalidNames){
        profileAndInvalidNames.eachWithIndex { Map.Entry<String, List<FileData>> entry, int index ->
            log.info "${index + 1}). ${entry.key}"
            entry.value.eachWithIndex{ FileData item, int i ->
                log.info("\t${i+1}). ${item.toString()}")
            }
        }
    }
    static boolean fileNameHasOverAllowedDigits(String fileName) {
        return fileName.findAll( /\d+/ ).join("").size() > MAXIMUM_ALLOWED_DIGITS_IN_FILE_NAME
    }
}


class FileData {
    String path
    String title

    FileData(String _path, String _title){
        path = _path
        title = _title
    }
    String toString(){
        return "${path} [\t ${title} ]"
    }
}
