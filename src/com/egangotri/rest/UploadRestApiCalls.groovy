package com.egangotri.rest

import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.upload.vo.UsheredVO
import com.egangotri.util.EGangotriUtil
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
        log.info("paramsMap ${paramsMap}")
        return paramsMap
    }

    static String addToMongo(String restApiRoute,
                             String archiveProfile, String uploadLink, String localPath, String title,
                             String uploadCycleId, String csvName, String archiveItemId = null) {
        def result = ""

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

    static<T extends UploadVO> String addToUshered(
            T usheredVO,
            String uploadCycleId, String csvName, String archiveItemId) {
        String restApiRoute = '/itemsUshered/add'
        return addToMongo(restApiRoute,usheredVO.archiveProfile, usheredVO.uploadLink,
                usheredVO.path, usheredVO.title,
                uploadCycleId, csvName, archiveItemId)
    }
    static<T extends UploadVO> String addToQueue(T queuedVO,
                             uploadCycleId, csvName) {
        String restApiRoute = '/itemsQueued/add'
        return addToMongo(restApiRoute,queuedVO.archiveProfile, queuedVO.uploadLink,
                queuedVO.path, queuedVO.title,
                uploadCycleId, csvName)
    }

}
