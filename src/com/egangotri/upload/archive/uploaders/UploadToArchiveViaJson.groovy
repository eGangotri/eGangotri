package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper

/**
 * json FILE MUST BE AN Array
 * [{
 "_id": {
 "$oid": "66371f0258a9236e29542a92"
 },
 "archiveProfile": "FIP",
 "uploadLink": "https://archive.org/upload?description=Nityanushthanavidhih by aghorashiva with vyakhya RE30575  Nityanushthanavidhih by aghorashiva with vyakhya -  FIP EFEO Pondicherry.pdf Manuscript hand list – 1 61, Palm-leaf, Grantha Agama-kriya, FIP-EFEO, 'RE30575  Nityanushthanavidhih by aghorashiva with vyakhya -  FIP EFEO Pondicherry'&subject=Manuscript hand list – 1 61, Palm-leaf, Grantha Agama-kriya, FIP-EFEO-Pondicherry&creator=FIP-EFEO-Pondicherry",
 "localPath": "D:\\FIP\\_IFP Palmleaf Manuscripts PDF All\\RE30575  Nityanushthanavidhih by aghorashiva with vyakhya -  FIP EFEO Pondicherry.pdf",
 "title": "RE30575  Nityanushthanavidhih by aghorashiva with vyakhya -  FIP EFEO Pondicherry",
 "uploadCycleId": "35628b0c-8b2a-4359-ab61-6af6210a8272",
 "archiveItemId": "sdMg_re-30575-nityanushthanavidhih-by-aghorashiva-with-vyakhya-fip-efeo-pondicherry",
 "csvName": "X",
 "uploadFlag": false,
 "datetimeUploadStarted": {
 "$date": "2024-05-05T05:54:10.780Z"
 },
 "createdAt": {
 "$date": "2024-05-05T05:54:10.780Z"
 },
 "updatedAt": {
 "$date": "2024-05-05T16:08:49.180Z"
 },
 "__v": 0
 },
 ...
 */
/**
Go TO MongoDB ItemsUshered.
 enter uploadCycleId
 retireve results as JSON
 put JSON in the args and run this task.

 */
@Slf4j
class UploadToArchiveViaJson {
    static void main(String[] args) {
        String excelFileName = ""
        String[] range = []
        if (args && args.length >= 1) {
            log.info "args $args"
            excelFileName = args[0]
            if (args.length == 2) {
                range = args[1].split("-")*.trim()
            }

        } else {
            log.info "Must have 1-2 arg.s Excel Path/range"
            System.exit(0)
        }
        UploadToArchive.metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        List<ReuploadVO> uploadablesFromJson = readJsonFile(excelFileName, range)
        log.info("uploadItems(${uploadablesFromJson.size()}) " +
                "${uploadablesFromJson[0].path}")
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Map<String, List<ReuploadVO>> vosGrouped = uploadablesFromJson.groupBy { ReuploadVO item -> item.archiveProfile }
        int attemptedItemsTotal = 0;
       /// SettingsUtil.applySettings();
       // Util.addToUploadCycleWithMode(vosGrouped.entrySet()*.key, "Json-(${range})");

        vosGrouped.eachWithIndex { entry, index ->
            String archiveProfile = entry.key
            Set<ReuploadVO> vos = entry.value as Set
            log.info "${index + 1}). " +
                    "Starting upload in archive.org for Profile $archiveProfile. " +
                    "Total Uplodables: ${vos.size()}/${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}"
            if (vos) {
                log.info("uploadItems ${vos[0].path}")
                log.info("uploadItems ${vos[-1].path}")
                log.info("vos ${vos.size()}")
                List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(UploadToArchive.metaDataMap, vos, true)
                log.info("uploadStats ${uploadStats}")
                String report = UploadUtils.generateStats(uploadStats, archiveProfile, vos.size())
                uploadSuccessCheckingMatrix.put((index + 1), report)
                attemptedItemsTotal += vos.size()
            } else {
                log.info "No uploadable files for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5, true)
        }


        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, attemptedItemsTotal, true)
        System.exit(0)
    }

    static def readJsonFile(String filePath, String[] range = []) {
        def jsonSlurper = new JsonSlurper()
        List jsonList = jsonSlurper.parse(new File(filePath)) as List
        log.info("readExcelFile ${filePath}")
        // Get the first sheet
        int counter = 0
        int start = 0
        int end = jsonList.size()
        List<ReuploadVO> uploadItems = []
        if (range?.size() == 2 && jsonList.size() > 1) {
            start = range[0].toInteger()
            if (start < 1 || start > jsonList.size()) {
                start = 0
            }
            end = range[1].toInteger()
            if (end < 1 || end > jsonList.size()) {
                end = jsonList.size()
            }
        }

        for (int i = start; i < end; i++) {
            def row = jsonList.get(i) as Map
            if (row) {
                String path = row.localPath
                String uploadLink = row.uploadLink
                String archiveItemId = row.archiveItemId
                String archiveProfile = row.archiveProfile
                Boolean uploadFlag = false
                if (row?.uploadFlag) {
                    uploadFlag = true
                    log.info("readExcelFile uploadFlag:${uploadFlag}")
                }

                if (!uploadFlag && path.contains(File.separator)) {
                    ReuploadVO uploadItem = new ReuploadVO(path,
                            uploadLink, archiveItemId, archiveProfile, uploadFlag)
                    uploadItems.add(uploadItem)
                    counter++
                }
            }
        }
        log.info("readJsonFile items added:${counter} size:${uploadItems.size()}")
        return uploadItems;
    }

}

