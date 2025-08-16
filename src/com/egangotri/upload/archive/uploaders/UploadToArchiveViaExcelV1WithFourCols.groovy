package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.util.EGangotriUtil
import com.google.gson.Gson
import groovy.util.logging.Slf4j

import org.apache.poi.ss.usermodel.*
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.nio.file.Paths

/**
 * works.
 *
 * expects path to a excel file with no header  with 4 columns:
 * C:\tmp\_data\tmp\Veda Mata Gayatri.pdf    Sub-1    Desc-1    Creator-1
 C:\tmp\_data\tmp\testFolder\Veda Mata Gayatri.pdf    Sub-2    Desc-2    Creator-2
 C:\tmp\_data\tmp\Veda Mata Gayatri - Copy.pdf    Sub-3    Desc-3    Creator-3
 */
@Slf4j
class UploadToArchiveViaExcelV1WithFourCols {

    static final int MAX_ROWS = 10000
    static final int MAX_BYTES = 200000000
    static final String REGEX_FOR_INVALID_CHARS = /[#!&]/
    /*
    ["profile": "value1", "excelPath": "value2", "range"="", "uploadCycleId"=""]
     */
    static void main(String[] args) {
        // Set a higher override value for POI operations
        IOUtils.setByteArrayMaxOverride(MAX_BYTES) // 200 million bytes
        String archiveProfile = ''
        String excelFileName = ''
        String[] range = []
        String uploadCycleId = ''
        log.info "args $args"

        if (args && args.length == 1) {
            String jsonString = args[0]
            //            def jsonSlurper = new JsonSlurper()
            //            def map = jsonSlurper.parseText(jsonString)
            //            log.info(map)
            log.info("jsonString : ${jsonString}")
            // Create a Gson instance
            Gson gson = new Gson()
            // Parse the JSON string to a Map
            Map<String, Object> map = gson.fromJson(jsonString, Map)
            archiveProfile = map.profile
            excelFileName = map.excelPath
            //System.getProperty("user.home") + "//Downloads//" + map.excelPath
            if (map?.range?.toString()?.contains('-')) {
                range = map??.range?.split('-')*.trim()
            }
            if (map?.uploadCycleId) {
                uploadCycleId = map.uploadCycleId
            }

            log.info("archiveProfile:${archiveProfile}" +
                    " excelFileName:${excelFileName} " +
                    "range:${range} uploadCycleId:${uploadCycleId}")
        } else {
            log.info """Must have exactly One JSON string as an Argument. Exiting.
             args = ['{"profile":"value1", "excelPath":"value2" , "range":"value2" , "uploadCycleId":"value2"}']"""
            System.exit(0)
        }

        UploadersUtil.prelims(new String[] {archiveProfile}, uploadCycleId)
        Map excelData = readExcelFile(excelFileName, range)
        if (excelData.success == false) {
            log.info("Errors in reading excel file ${excelData.errors}")
            System.exit(0)
        }
        List<UploadItemFromExcelVO> uploadItems = excelData.uploadItems
        log.info("uploadItems(${uploadItems.size()}) ${uploadItems[0]?.subject} ${uploadItems[0]?.description} ${uploadItems[0]?.creator} ${uploadItems[0]?.absolutePath}")
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        UploadersUtil.addToUploadCycleWithModeV2(archiveProfile, uploadItems*.absolutePath, "Excel-;${excelFileName}-;${range}")

        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromSuppliedData(archiveProfile, uploadItems)
        if (uploadItems) {
            log.info("uploadItems ${uploadItems.size()}")
            log.info("uploadItems ${uploadItems[0]?.absolutePath}")
            log.info("uploadItems ${uploadItems[-1]?.absolutePath}")
            log.info("vos ${vos.size()}")
            log.info("vos ${vos[0]}")
            log.info("vos ${vos[-1]}")
            List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(UploadersUtil.archiveLoginsMetaDataMap, vos)

            log.info("uploadStats ${uploadStats}")
            String report = UploadUtils.generateStats(uploadStats, archiveProfile, vos.size())
            uploadSuccessCheckingMatrix.put(1, report)
        } else {
            log.info('No Items for upload.')
        }
        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, vos.size())
        System.exit(0)
    }

    static def readExcelFile(filePath, String[] range = []) {
        // Setting maximum byte array size for POI operations
        IOUtils.setByteArrayMaxOverride(MAX_BYTES) // 200 million bytes
        // Opening the Excel file
        FileInputStream file = new FileInputStream(new File(filePath))
        Workbook workbook = new XSSFWorkbook(file)
        log.info("readExcelFile ${filePath} (limiting to max ${MAX_ROWS} rows)")
        // Get the first sheet
        Sheet sheet = workbook.getSheetAt(0)
        int counter = 0
        int start = 1
        int end = Math.min(sheet.size(), MAX_ROWS) // Limit to first ${MAX_ROWS} rows max
        List<UploadItemFromExcelVO> uploadItems = []
        List errors = []
        if (range?.size() == 2 && sheet.size() > 1) {
            start = range[0].toInteger()
            if (start < 1 || start > sheet.size()) {
                errors << "Invalid start range:${start} against Sheet Size(${sheet.size()})"
            }
            end = Math.min(range[1].toInteger(), MAX_ROWS) // Enforce max ${MAX_ROWS} rows even with specified range
            if (end < 1 || end > sheet.size()) {
                errors << "Invalid end range:${end} against Sheet Size(${sheet.size()})"
            }
        }
        if (errors?.size() > 0) {
            log.info("returning due to errors in reading excel file ${errors}")
            return [success:false, errors:errors]
        }

        for (int i = start; i <= end; i++) {
            Row row = sheet.getRow(i)
            if (row) {
                String absPath = row.getCell(0)?.stringCellValue?.trim() ?: ''
                String fileName = ''
                if (absPath) {
                    try {
                        fileName = Paths.get(absPath).fileName.toString()
                    }catch (Exception e) {
                        errors << "Error processing path: ${absPath}, row ${i}: ${e.message}"
                        continue
                    }
                } else {
                    errors << "Empty path at row ${i}"
                    continue
                }
                String subject = row.getCell(1)?.stringCellValue?.trim()?.replaceAll(REGEX_FOR_INVALID_CHARS, '') ?: fileName
                String description = (row.getCell(2)?.stringCellValue?.trim()?.replaceAll(REGEX_FOR_INVALID_CHARS, '')  ?: fileName) + " "
                String creator = (row.getCell(3)?.stringCellValue?.trim()?.replaceAll(REGEX_FOR_INVALID_CHARS, '') ?: fileName) + " "
                Boolean uploadedFlag = false
                if (absPath?.length() == 0) {
                    errors << "Invalid absolute path:${absPath} against Sheet Size(${sheet.size()}), skipping row ${i}"
                    continue
                }
                if (row.size() > 4) {
                    if (row.getCell(4).getCellType() == CellType.BOOLEAN) {
                        uploadedFlag = row.getCell(4).getBooleanCellValue()
                        log.info("readExcelFile uploadedFlag:${uploadedFlag}")
                    } else if (row.getCell(4).getCellType() == CellType.STRING) {
                        uploadedFlag = row.getCell(4).getStringCellValue()?.equalsIgnoreCase('true')
                    }
                }
                UploadItemFromExcelVO uploadItem = new UploadItemFromExcelVO(absPath, subject, description, creator, uploadedFlag)
                if (!uploadedFlag && absPath.contains(File.separator)) {
                    uploadItems.add(uploadItem)
                    counter++
                }
            }
        }
        // Close resources
        workbook.close()
        file.close()
        log.info("readExcelFile items added:${counter} size:${uploadItems.size()}")
        return [success:true, uploadItems:uploadItems]
    }

}

