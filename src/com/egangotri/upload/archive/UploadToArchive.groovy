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
            log.info "args $args"
            archiveProfiles = args.toList()
        }

       // System.setProperty("webdriver.chrome.driver", getClass().getResource("chromedriver.exe").toURI().toString())
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        Hashtable<String, String> settingsMetaDataMap = UploadUtils.loadProperties(EGangotriUtil.SETTINGS_PROPERTIES_FILE)
        if(settingsMetaDataMap){
            log.info "settingsMetaDataMap.PARTITION_SIZE ${settingsMetaDataMap.PARTITION_SIZE}"
            log.info "settingsMetaDataMap.PDF_ONLY ${settingsMetaDataMap.PDF_ONLY}"
            def generateRandomCreatorFlag = settingsMetaDataMap.GENERATE_RANDOM_CREATOR
            if(settingsMetaDataMap.PARTITION_SIZE && settingsMetaDataMap.PARTITION_SIZE.isInteger() && settingsMetaDataMap.PARTITION_SIZE.toInteger() >0){
                try{
                    Integer partitionSize = Integer.parseInt(settingsMetaDataMap.PARTITION_SIZE)
                    if(partitionSize > 0){
                        EGangotriUtil.PARTITION_SIZE = partitionSize
                        EGangotriUtil.PARTITIONING_ENABLED = true
                    }
                }
                catch(Exception e){
                    log.info("PARTITION_SIZE : ${settingsMetaDataMap.PARTITION_SIZE} is not a valid mumber. Will not be considered")
                }
            }
            //CALIBRATE_TIMES
            if(settingsMetaDataMap.CALIBRATE_TIMES){
                try{
                    Float calibrateTimes = Float.parseFloat(settingsMetaDataMap.CALIBRATE_TIMES)
                    if(calibrateTimes >= 0.5 && calibrateTimes <= 5){
                        EGangotriUtil.ARCHIVE_WAITING_PERIOD_ONE_SEC *= calibrateTimes
                    }
                }
                catch(Exception e){
                    log.info("CALIBRATE_TIMES is not a valid decimal mumber. WIll not be considered")
                }

            }
            if(settingsMetaDataMap.PDF_ONLY && settingsMetaDataMap.PDF_ONLY == "true"){
                FileUtil.PDF_ONLY = settingsMetaDataMap.PDF_ONLY
                FileUtil.PDF_REGEX =  FileUtil.PDF_ONLY ? /.*.pdf/ : /.*/
                log.info("PDF_REGEX: " + settingsMetaDataMap.PDF_ONLY.toBoolean() + " " + FileUtil.PDF_ONLY + " " + FileUtil.PDF_REGEX)
            }
            if(settingsMetaDataMap.CREATOR_FROM_DASH_SEPARATED_STRING && settingsMetaDataMap.CREATOR_FROM_DASH_SEPARATED_STRING == "true"){
                EGangotriUtil.CREATOR_FROM_DASH_SEPARATED_STRING = settingsMetaDataMap.CREATOR_FROM_DASH_SEPARATED_STRING.toBoolean()
                log.info("CREATOR_FROM_DASH_SEPARATED_STRING: " + settingsMetaDataMap.CREATOR_FROM_DASH_SEPARATED_STRING.toBoolean())
            }

            if(settingsMetaDataMap.ADD_RANDOM_INTEGER_TO_PAGE_URL && settingsMetaDataMap.ADD_RANDOM_INTEGER_TO_PAGE_URL == "true"){
                EGangotriUtil.ADD_RANDOM_INTEGER_TO_PAGE_URL = settingsMetaDataMap.ADD_RANDOM_INTEGER_TO_PAGE_URL.toBoolean()
                log.info("ADD_RANDOM_INTEGER_TO_PAGE_URL: " + settingsMetaDataMap.ADD_RANDOM_INTEGER_TO_PAGE_URL.toBoolean())
            }

            if(settingsMetaDataMap.GENERATE_ONLY_URLS && settingsMetaDataMap.GENERATE_ONLY_URLS == "true"){
                EGangotriUtil.GENERATE_ONLY_URLS = settingsMetaDataMap.GENERATE_ONLY_URLS.toBoolean()
                log.info("GENERATE_ONLY_URLS: " + settingsMetaDataMap.GENERATE_ONLY_URLS.toBoolean())
            }


            if(generateRandomCreatorFlag){
                if(generateRandomCreatorFlag.toLowerCase() != "false")
                EGangotriUtil.GENERATE_RANDOM_CREATOR = true
                //if the value is not a Boolean but a CSV
                if(generateRandomCreatorFlag.toLowerCase() != "true"){
                    EGangotriUtil.ACCOUNTS_WITH_RANDOMIZABLE_CREATORS = generateRandomCreatorFlag.split(",").collect{ -> it.trim()}
                }
                log.info("GENERATE_RANDOM_CREATOR: " + generateRandomCreatorFlag)
            }

            if(settingsMetaDataMap.RANDOM_CREATOR_MAX_LIMIT && settingsMetaDataMap.RANDOM_CREATOR_MAX_LIMIT.isInteger()){
                int randomCreatorMaxLimit = settingsMetaDataMap.RANDOM_CREATOR_MAX_LIMIT.toInteger()
                if(randomCreatorMaxLimit >= 20 &&  randomCreatorMaxLimit <= 1000){
                    UploadUtils.RANDOM_CREATOR_MAX_LIMIT = randomCreatorMaxLimit
                }
                log.info("RANDOM_CREATOR_MAX_LIMIT: " + UploadUtils.RANDOM_CREATOR_MAX_LIMIT )
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
                    String statsAsPlusSeparatedValues = uploadStats.collect{ elem -> elem.first()}.join(" + ")
                    String countOfUploadedItems = uploadStats.size() > 1 ? "($statsAsPlusSeparatedValues) = $uplddSum": uploadStats.first().first()

                    int excSum = uploadStats.collect{ elem  -> elem.last()}.sum()
                    String excpsAsPlusSeparatedValues = uploadStats.collect{elem -> elem .last()}.join(" + ")
                    String exceptionCount = uploadStats.size() > 1 ? "($excpsAsPlusSeparatedValues) = $excSum" : uploadStats.first().last()
                    log.info("Uploaded $countOfUploadedItems items with (${exceptionCount}) Exceptions for Profile: $archiveProfile")

                    String rep = "$archiveProfile, \t Total $countOfUploadablePdfs,\t Attempted Upload Count $countOfUploadedItems,\t with  ${exceptionCount} Exceptions \t" + (countOfUploadablePdfs == uplddSum ? 'Success. All items were put for upload.' : 'Some Failed!')
                    uploadSuccessCheckingMatrix.put((index + 1), rep)
                }
            } else {
                log.info "No Files uploadable for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }
        if(uploadSuccessCheckingMatrix){
            log.info "Upload Report:\n"
            uploadSuccessCheckingMatrix.each { k, v ->
                log.info "$k) $v"
            }
            log.info "\n ***All Items put for upload implies all were attempted successfully for upload. But there can be errors still after attempted upload. best to check manually."
        }

        log.info "***End of Upload to Archive Program"
        return true
    }
}


