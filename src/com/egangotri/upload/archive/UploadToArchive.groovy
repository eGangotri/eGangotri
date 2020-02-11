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
            if(settingsMetaDataMap.PARTITION_SIZE && settingsMetaDataMap.PARTITION_SIZE.toInteger() >0){
                try{
                    Integer partitionSize = Integer.parseInt(settingsMetaDataMap.PARTITION_SIZE)
                    if(partitionSize > 0){
                        EGangotriUtil.PARTITION_SIZE = partitionSize
                        EGangotriUtil.PARTITIONING_ENABLED = true
                    }
                }
                catch(Exception e){
                    println("PARTITION_SIZE : ${settingsMetaDataMap.PARTITION_SIZE} is not a valid mumber. Will not be considered")
                }
            }
            //TAB_LAUNCH_TIME
            if(settingsMetaDataMap.TAB_LAUNCH_TIME){
                try{
                    Float tabLaunchTime = Float.parseFloat(settingsMetaDataMap.TAB_LAUNCH_TIME)
                    if(tabLaunchTime >= 0.1 && tabLaunchTime <= 5){
                        EGangotriUtil.ARCHIVE_WAITING_PERIOD_ONE_SEC *= tabLaunchTime
                    }
                }
                catch(Exception e){
                    println("TAB_LAUNCH_TIME is not a valid decimal mumber. WIll not be considered")
                }

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
        System.exit(0)
    }

    static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive"
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
                    List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(metaDataMap, archiveProfile)
                    int uplddSum = uploadStats.collect{ elem -> elem.first()}.sum()
                    String countOfUploadedItems = "(" + uploadStats.collect{ elem -> elem.first()}.join(" + ") + ") = " + uplddSum

                    String exceptionCount = "(" + uploadStats.collect{elem -> elem .last()}.join(" + ") + ") = " +
                            uploadStats.collect{ elem  -> elem.last()}.sum()
                    log.info("Uploaded $countOfUploadedItems items with (${exceptionCount}) Exceptions for Profile: $archiveProfile")

                    String rep = "$archiveProfile, \t Total $countOfUploadablePdfs,\t Attempted Upload Count $countOfUploadedItems,\t with  ${exceptionCount} Exceptions \t" + (countOfUploadablePdfs == uplddSum ? 'Success. All items were put for upload.' : 'Some Failed!')
                    rep += "\n ***All Items put for upload implies all were attempted successfully for upload. But there can be errors still after attempted upload. best to check manually."
                    uploadSuccessCheckingMatrix.put((index + 1), rep)
                }
            } else {
                log.info "No Files uploadable for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }

        log.info "Upload Report:\n"

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }

        log.info "***Browser for Archive Upload Launches Done"
        return true
    }
}


