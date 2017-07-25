package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
@Slf4j
class UploadToArchive {

    static main(args) {
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            println "args $args"
            archiveProfiles = args.toList()
        }

        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        execute(archiveProfiles, metaDataMap)
    }

    public static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive"
        profiles*.toString().eachWithIndex { archiveProfile, index ->
            log.info "${index + 1}). Test Uploadables in archive.org Profile $archiveProfile"
            int countOfUploadablePdfs = UploadUtils.getCountOfUploadablePdfsForProfile(archiveProfile)
            int countOfUploadedItems = 0
            log.info("CountOfUploadablePdfs: $countOfUploadablePdfs")
            if (countOfUploadablePdfs) {
                countOfUploadedItems = ArchiveHandler.uploadToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
                log.info("Uploaded $countOfUploadedItems docs for $archiveProfile")

                String rep = "$archiveProfile, \t $countOfUploadablePdfs,\t $countOfUploadedItems,\t" + (countOfUploadablePdfs == countOfUploadedItems ? 'Success' : 'Failure!!!!')
                uploadSuccessCheckingMatrix.put((index + 1), rep)
            } else {
                log.info "No Files uploadable for Profile $archiveProfile"
            }
        }

        log.info "Upload Report:\n"

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }

        log.info "***Browser for Archive Upload Launches Done"
        return true
    }
}


