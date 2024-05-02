package com.egangotri.rest

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.upload.vo.UsheredVO
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

        paramsMap.put("datetimeUploadStarted", "${dateFormat.format(new Date())}")
        //log.info("paramsMap ${paramsMap}")
        return paramsMap
    }

    static Map<String,Object> addToMongo(String restApiRoute,
                             String archiveProfile, String uploadLink, String localPath, String title,
                             String uploadCycleId, String csvName, String archiveItemId = null) {
        Map<String,Object> result = [:]

        try {
            Map body = createPostParamsAsMap(archiveProfile, uploadLink, localPath,
                    UploadUtils.stripFilePath(title),
                    uploadCycleId, csvName, archiveItemId)
            result = RestUtil.makePostCall(restApiRoute, body)
        }
        catch (Exception e) {
            log.info("addToQueue Error while calling ${restApiRoute}", e)
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

    static Map<String, Object> addToUploadCycle(Collection<String> profiles) {
        Map<String,Object>  result = [:]
        String restApiRoute = "/${RestUtil.UPLOAD_CYCLE_ROUTE}/add"
        log.info("ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION  ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}");
        log.info("ArchiveUtil.getGrandTotalOfAllUploadables  ${ArchiveUtil.getGrandTotalOfAllUploadables(profiles)}");

        def profilesAndCount = profiles.collect { String profile ->
            Integer countOfUploadableItems = FileRetrieverUtil.getCountOfUploadableItemsForProfile(profile)
            log.info("archiveProfile: ${profile} countOfUploadableItems ${countOfUploadableItems}")
            List<String> uploadables = FileRetrieverUtil.getUploadablesForProfile(profile).collect {
                (new File(it)).name
            }
            def json = new JsonBuilder()
            def root = json {
                archiveProfile profile
                count countOfUploadableItems
                titles uploadables
            }
            return new JsonSlurper().parseText(json.toString())
        }
        log.info("profilesAndCount: ${profilesAndCount.toString()} ")
        try {
            Map paramsMap = [:];
            if(!EGangotriUtil.UPLOAD_CYCLE_ID ) {
                EGangotriUtil.UPLOAD_CYCLE_ID = UUID.randomUUID().toString()
            }
            //some issue with json parsing if you dont explicitly do this
            String _formattedData = dateFormat.format(new Date())
            paramsMap.put("uploadCycleId", EGangotriUtil.UPLOAD_CYCLE_ID);
            paramsMap.put("uploadCount", ArchiveUtil.getGrandTotalOfAllUploadables(profiles));
            paramsMap.put("archiveProfiles", profilesAndCount);
            paramsMap.put("datetimeUploadStarted", _formattedData)
            result = RestUtil.makePostCall(restApiRoute, paramsMap) as Map<String, Object>

        }
        catch (Exception e) {
            log.info("addToQueue Error while calling ${restApiRoute}", e)
        }
        return result;
    }
}
