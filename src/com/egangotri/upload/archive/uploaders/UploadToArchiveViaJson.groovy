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
 * works.
 *
 * expects path to a excel file with no header  with 4 columns:
 * C:\tmp\_data\tmp\Veda Mata Gayatri.pdf	Sub-1	Desc-1	Creator-1
 C:\tmp\_data\tmp\testFolder\Veda Mata Gayatri.pdf	Sub-2	Desc-2	Creator-2
 C:\tmp\_data\tmp\Veda Mata Gayatri - Copy.pdf	Sub-3	Desc-3	Creator-3
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
