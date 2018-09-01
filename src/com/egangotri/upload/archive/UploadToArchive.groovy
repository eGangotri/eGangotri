package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=${System.getProperty('user.home')}${File.separator}chromedriver${File.separator}chromedriver.exe
 * chromedriver.exe
 C:\ws\eGangotri>java -Dwebdriver.chrome.driver=${System.getProperty('user.home')}${File.separator}chromedriver${File.separator}chromedriver.exe -jar ./build/libs/eGangotri.jar "DT"
 java -Dwebdriver.chrome.driver=/Users/user/chromedriver\chromedriver.exe -jar ./build/libs/eGangotri.jar "DT"
 ** Dont use \ followeb by a U

 */
@Slf4j
class UploadToArchive {

    static main(args) {
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            println "args $args"
            archiveProfiles = args.toList()
        }

       // System.setProperty("webdriver.chrome.driver", getClass().getResource("chromedriver.exe").toURI().toString())
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Hashtable<String, String> settingsMetaDataMap = UploadUtils.loadProperties(EGangotriUtil.SETTINGS_PROPERTIES_FILE)
        if(settingsMetaDataMap){
            println "settingsMetaDataMap.PARTITION_SIZE ${settingsMetaDataMap.PARTITION_SIZE}"
            if(settingsMetaDataMap.PARTITION_SIZE>0){
                EGangotriUtil.PARTITION_SIZE = settingsMetaDataMap.PARTITION_SIZE.toInteger()
                EGangotriUtil.PARTITIONING_ENABLED = true
            }
        }

        execute(archiveProfiles, metaDataMap)
    }

    public static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive"
        profiles*.toString().eachWithIndex { archiveProfile, index ->
            log.info "${index + 1}). Test Uploadables in archive.org Profile $archiveProfile"
            Integer countOfUploadablePdfs = UploadUtils.getCountOfUploadablePdfsForProfile(archiveProfile)
            Integer countOfUploadedItems = 0

            log.info("CountOfUploadablePdfs: $countOfUploadablePdfs")
            if (countOfUploadablePdfs) {
                if(EGangotriUtil.GENERATE_ONLY_URLS){
                    List<String> uploadables = UploadUtils.getUploadablePdfsForProfile(archiveProfile)
                    ArchiveHandler.generateAllUrls(archiveProfile,uploadables)
                }
                else{
                    countOfUploadedItems = ArchiveHandler.uploadToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
                    log.info("Uploaded $countOfUploadedItems docs for Profile: $archiveProfile")

                    String rep = "$archiveProfile, \t Total $countOfUploadablePdfs,\t Successful Upload Count $countOfUploadedItems,\t" + (countOfUploadablePdfs == countOfUploadedItems ? 'Success.All Uploaded' : 'Some Failed!')
                    uploadSuccessCheckingMatrix.put((index + 1), rep)
                }
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


