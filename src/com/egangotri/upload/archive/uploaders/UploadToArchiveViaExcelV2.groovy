package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * works.
 *
 * expects path to a excel file with no header  with 4 columns:
 * C:\tmp\_data\tmp\Veda Mata Gayatri.pdf	Sub-1	Desc-1	Creator-1
 C:\tmp\_data\tmp\testFolder\Veda Mata Gayatri.pdf	Sub-2	Desc-2	Creator-2
 C:\tmp\_data\tmp\Veda Mata Gayatri - Copy.pdf	Sub-3	Desc-3	Creator-3
 */

/**
 * Sample Excel :
 * localPath	uploadLink	archiveItemId	archiveProfile
 C:\tmp\_data\tmp2\Veda Mata Gayatri- Sri Aurobindo.pdf	https://archive.org/upload?description=Maha%20Pandit%20Rahul%20Sanskrityayan%20Books, 'Veda Mata Gayatri- Sri Aurobindo'&subject=Books of Maha Pandit Rahul Sanskrityayan&creator=Rahul SankrityayanMAX_	JhYQ_veda-mata-gayatri-sri-aurobindo	TMP2

 */
@Slf4j
class UploadToArchiveViaExcelV2 {
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
        SettingsUtil.applySettings()

        List<ReuploadVO> uploadablesFromExcel = readExcelFile(excelFileName, range)
        log.info("uploadItems(${uploadablesFromExcel.size()}) " +
                "${uploadablesFromExcel[0].path}")
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Map<String, List<ReuploadVO>> vosGrouped = uploadablesFromExcel.groupBy { ReuploadVO item -> item.archiveProfile }

        Util.preUpload(vosGrouped.entrySet()*.key);

        for(voGroup in vosGrouped.entrySet()){
            String archiveProfile = voGroup.key;
            Set<ReuploadVO> vos = voGroup.value as Set
            if (vos) {
                log.info("uploadItems ${vos[0].path}")
                log.info("uploadItems ${vos[-1].path}")
                log.info("vos ${vos.size()}")
                List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(UploadToArchive.metaDataMap, vos, true)

                log.info("uploadStats ${uploadStats}")
                String report = UploadUtils.generateStats(uploadStats, archiveProfile, vos.size())
                uploadSuccessCheckingMatrix.put(1, report)
            } else {
                log.info("No Items for upload.")
            }
        }

        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, vosGrouped.size(), true)
        System.exit(0)
    }

    static def readExcelFile(filePath, String[] range = []) {
        // Opening the Excel file
        FileInputStream file = new FileInputStream(new File(filePath))
        Workbook workbook = new XSSFWorkbook(file)
        log.info("readExcelFile ${filePath}")
        // Get the first sheet
        Sheet sheet = workbook.getSheetAt(0)
        int counter = 0
        int start = 1
        int end = sheet.size()
        List<ReuploadVO> uploadItems = []
        if (range?.size() == 2 && sheet.size() > 1) {
            start = range[0].toInteger()
            if (start < 1 || start > sheet.size()) {
                start = 1
            }
            end = range[1].toInteger()
            if (end < 1 || end > sheet.size()) {
                end = sheet.size()
            }
        }

        for (int i = start; i <= end; i++) {
            Row row = sheet.getRow(i)
            if (row) {
                String path = row.getCell(0).getStringCellValue();
                String uploadLink = row.getCell(1).getStringCellValue()
                String archiveItemId = row.getCell(2).getStringCellValue()
                String archiveProfile = row.getCell(3).getStringCellValue()
                Boolean uploadedFlag = false
                if (row?.getCell(4)?.getCellType() == CellType.BOOLEAN) {
                    uploadedFlag = row.getCell(4).getBooleanCellValue();
                    log.info("readExcelFile uploadedFlag:${uploadedFlag}")
                } else if (row?.getCell(4)?.getCellType() == CellType.STRING) {
                    uploadedFlag = row.getCell(4).getStringCellValue()?.equalsIgnoreCase("true")
                }

                if (!uploadedFlag && path.contains(File.separator)) {
                    ReuploadVO uploadItem = new ReuploadVO(path,
                            uploadLink, archiveItemId, archiveProfile, uploadedFlag)
                    uploadItems.add(uploadItem)
                    counter++
                }
            }
        }
        // Close resources
        workbook.close()
        file.close()
        log.info("readExcelFile items added:${counter} size:${uploadItems.size()}")
        return uploadItems;
    }

}

