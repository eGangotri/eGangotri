package com.egangotri.upload.util

import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class SettingsUtil {
    def static void applySettings(){
        Hashtable<String, String> settingsMetaDataMap = UploadUtils.loadProperties(EGangotriUtil.SETTINGS_PROPERTIES_FILE)
        if(settingsMetaDataMap){
            log.info "settingsMetaDataMap.PARTITION_SIZE ${settingsMetaDataMap.PARTITION_SIZE}"
            log.info "settingsMetaDataMap.PDF_ONLY ${settingsMetaDataMap.PDF_ONLY}"
            if(settingsMetaDataMap.PARTITION_SIZE && settingsMetaDataMap.PARTITION_SIZE.isInteger() && settingsMetaDataMap.PARTITION_SIZE.toInteger() >0){
                try{
                    Integer partitionSize = Integer.parseInt(settingsMetaDataMap.PARTITION_SIZE)
                    if(partitionSize > 0){
                        EGangotriUtil.PARTITION_SIZE = partitionSize
                        EGangotriUtil.PARTITIONING_ENABLED = true
                    }
                }
                catch(Exception e){
                    log.info("PARTITION_SIZE : ${settingsMetaDataMap.PARTITION_SIZE} is not a valid mumber. will not be considered")
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
                    log.info("CALIBRATE_TIMES is not a valid decimal mumber. will not be considered")
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

            def generateRandomCreatorFlag = settingsMetaDataMap.GENERATE_RANDOM_CREATOR
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

            if(settingsMetaDataMap.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS){
                EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS = settingsMetaDataMap.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS.split(",")*.trim().toList()
                log.info("IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS: " + EGangotriUtil.IGNORE_CREATOR_SETTINGS_FOR_ACCOUNTS)
            }
        }

    }
}