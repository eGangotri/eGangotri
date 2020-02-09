package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
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
            println "settingsMetaDataMap.PDF_ONLY ${settingsMetaDataMap.PDF_ONLY}"
            def generateRandomCreator = settingsMetaDataMap.GENERATE_RANDOM_CREATOR
            if(settingsMetaDataMap.PARTITION_SIZE.toInteger() >0){
                EGangotriUtil.PARTITION_SIZE = settingsMetaDataMap.PARTITION_SIZE.toInteger()
                EGangotriUtil.PARTITIONING_ENABLED = true
            }
            if(settingsMetaDataMap.PDF_ONLY && settingsMetaDataMap.PDF_ONLY == "true"){
                FileUtil.PDF_ONLY = settingsMetaDataMap.PDF_ONLY
                FileUtil.PDF_REGEX =  FileUtil.PDF_ONLY ? /.*.pdf/ : /.*/
                println("EGangotriUtil.PDF_REGEX: " + settingsMetaDataMap.PDF_ONLY.toBoolean() + " " + FileUtil.PDF_ONLY + " " + FileUtil.PDF_REGEX)
            }
            if(generateRandomCreator){
                if(generateRandomCreator.toLowerCase() != "false")
                EGangotriUtil.GENERATE_RANDOM_CREATOR = true
                if(generateRandomCreator.toLowerCase() != "true"){
                    EGangotriUtil.ACCOUNTS_WITH_RANDOMIZABLE_CREATORS = generateRandomCreator.split(",").collect{ -> it.trim()}
                }
                println("EGangotriUtil.GENERATE_RANDOM_CREATOR: " + generateRandomCreator)
            }
        }

        execute(archiveProfiles, metaDataMap)
    }

    static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive"
        //ArchiveHandler.generateSupplementaryUrlsForProfiles(profiles)
        profiles*.toString().eachWithIndex { archiveProfile, index ->
            log.info "${index + 1}). Test Uploadables in archive.org Profile $archiveProfile"
            Integer countOfUploadablePdfs = UploadUtils.getCountOfUploadablePdfsForProfile(archiveProfile)

            log.info("CountOfUploadablePdfs: $countOfUploadablePdfs")
            if (countOfUploadablePdfs) {
                if(EGangotriUtil.GENERATE_ONLY_URLS){
                    List<String> uploadables = UploadUtils.getUploadablePdfsForProfile(archiveProfile)
                    ArchiveHandler.generateAllUrls(archiveProfile,uploadables)
                }
                else{
                    List<Integer> uploadStats = ArchiveHandler.uploadToArchive(metaDataMap, archiveProfile)
                    Integer countOfUploadedItems = uploadStats[0]
                    log.info("Uploaded $countOfUploadedItems docs with ${uploadStats[1]} Exceptions for Profile: $archiveProfile")

                    String rep = "$archiveProfile, \t Total $countOfUploadablePdfs,\t Attempted Upload Count $countOfUploadedItems,\t with  ${uploadStats[1]} Exceptions \t" + (countOfUploadablePdfs == countOfUploadedItems ? 'Success. All items were put for upload.' : 'Some Failed!')
                    rep += "\n ***All Items put for upload implies all were attempted successfully for upload. But there can be errors still after attempted upload. best to check manually."
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


