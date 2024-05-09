package com.egangotri.rest

import com.egangotri.upload.archive.uploaders.UploadItemFromExcelVO
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

@Slf4j
class UploadRestApiCalls {

    static SimpleDateFormat dateFormat = new SimpleDateFormat(EGangotriUtil.DATE_TIME_PATTERN)

    static Map createPostParamsAsMap(String archiveProfile, String uploadLink,
                                     String localPath, String title,
                                     String uploadCycleId, String csvName,
                                     String archiveItemId) {
        Map paramsMap = [:]
        paramsMap.put("archiveProfile", archiveProfile)
        paramsMap.put("uploadLink", uploadLink)
        paramsMap.put("localPath", localPath)
        paramsMap.put("title", title)
        paramsMap.put("uploadCycleId", uploadCycleId)
        if (archiveItemId) {
            paramsMap.put("archiveItemId", archiveItemId)
        }
        paramsMap.put("csvName", csvName)
        // issue with google json lib. but not adding is fine
        //  paramsMap.put("datetimeUploadStarted", "${dateFormat.format(new Date())}")
        //log.info("paramsMap ${paramsMap}")
        return paramsMap
    }

    static Map<String, Object> addToMongo(String restApiRoute,
                                          String archiveProfile, String uploadLink, String localPath, String title,
                                          String uploadCycleId, String csvName, String archiveItemId = null) {
        Map<String, Object> result = [:]

        try {
            Map body = createPostParamsAsMap(archiveProfile, uploadLink, localPath,
                    UploadUtils.stripFilePath(title),
                    uploadCycleId, csvName, archiveItemId)
            result = RestUtil.makePostCall(restApiRoute, body)
        }
        catch (Exception e) {
            log.info("addToMongo Error while calling ${restApiRoute}", e)
        }
        return result;
    }

    static <T extends UploadVO> Object addToUshered(
            T usheredVO,
            String uploadCycleId, String csvName, String archiveItemId) {
        String restApiRoute = "/${RestUtil.ITEMS_USHERED_PATH}/add"
        def result = addToMongo(restApiRoute, usheredVO.archiveProfile, usheredVO.uploadLink,
                usheredVO.path, usheredVO.title,
                uploadCycleId, csvName, archiveItemId)
        return result
    }

    static <T extends UploadVO> Object addToQueue(T queuedVO,
                                                  uploadCycleId, csvName) {
        String restApiRoute = "/${RestUtil.ITEMS_QUEUED_PATH}/add"
        def result = addToMongo(restApiRoute, queuedVO.archiveProfile, queuedVO.uploadLink,
                queuedVO.path, queuedVO.title,
                uploadCycleId, csvName)
        return result
    }

    static Map<String, Object> addToUploadCycle(Collection<String> profiles,

                                                String mode = "") {
        Map<String, Object> result = [:]
        String restApiRoute = "/${RestUtil.UPLOAD_CYCLE_ROUTE}/add"
        log.info("ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION  ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}");
        log.info("ArchiveUtil.getGrandTotalOfAllUploadables  ${ArchiveUtil.getGrandTotalOfAllUploadables(profiles)}");

        def profilesAndCount = profiles.collect { String profile ->
            List<File> uploadables = FileRetrieverUtil.getUploadablesForProfile(profile).collect {
                (new File(it))
            }
            def json = new JsonBuilder()
            def root = json {
                archiveProfile profile
                count uploadables?.size()
               absolutePaths uploadables*.absolutePath
            }
            return new JsonSlurper().parseText(json.toString())
        }
        log.info("profilesAndCount: ${profilesAndCount.toString()} ")
        try {
            Map paramsMap = [:];
            if (EGangotriUtil.UPLOAD_CYCLE_ID?.trim()?.size() == 0) {
                EGangotriUtil.UPLOAD_CYCLE_ID = UUID.randomUUID().toString()
            }
            paramsMap.put("uploadCycleId", EGangotriUtil.UPLOAD_CYCLE_ID);
            paramsMap.put("uploadCount", ArchiveUtil.getGrandTotalOfAllUploadables(profiles));
            paramsMap.put("archiveProfiles", profilesAndCount);
            if (mode.length() > 0) {
                paramsMap.put("mode", mode);
            }
            //date parsing in google json lib causing an issue so still works when mnot explicitly set
//            paramsMap.put("datetimeUploadStarted", new Date().toString())
            log.info("paramsMap ${paramsMap}")
            result = RestUtil.makePostCall(restApiRoute, paramsMap) as Map<String, Object>

        }
        catch (Exception e) {
            log.error("addToUploadCycle Error while calling ${restApiRoute}", e)
            return null
        }
        return result;
    }

    static Map<String, Object> addToUploadCycleV2(String profile,
                                                  List<UploadItemFromExcelVO> uploadables,
                                                  String mode = "") {
        Map<String, Object> result = [:]
        String restApiRoute = "/${RestUtil.UPLOAD_CYCLE_ROUTE}/add"
        int countOfUploadableItems = uploadables.size();
        log.info("ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION  ${countOfUploadableItems}");
        def json = new JsonBuilder()
        def root = json {
            archiveProfile profile
            count countOfUploadableItems
            absolutePaths uploadables*.absolutePath
        }
        def profilesAndCount = new JsonSlurper().parseText(json.toString())
        try {
            Map paramsMap = [:];
            if (EGangotriUtil.UPLOAD_CYCLE_ID?.trim()?.size() == 0) {
                EGangotriUtil.UPLOAD_CYCLE_ID = UUID.randomUUID().toString()
            }
            paramsMap.put("uploadCycleId", EGangotriUtil.UPLOAD_CYCLE_ID);
            paramsMap.put("uploadCount", countOfUploadableItems);
            paramsMap.put("archiveProfiles", profilesAndCount);
            if (mode.length() > 0) {
                paramsMap.put("mode", mode);
            }
            //date parsing in google json lib causing an issue so still works when mnot explicitly set
//            paramsMap.put("datetimeUploadStarted", new Date().toString())
            log.info("paramsMap ${paramsMap}")
            result = RestUtil.makePostCall(restApiRoute, paramsMap) as Map<String, Object>

        }
        catch (Exception e) {
            log.error("addToUploadCycle Error while calling ${restApiRoute}", e)
            return null
        }
        return result;
    }

}
