package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
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
@Slf4j
class UploadToArchiveViaExcel {
    static void main(String[] args) {
        String archiveProfile = ""
        String excelFileName = ""
        String[] range = []
        if (args && args.length >= 2) {
            log.info "args $args"
            archiveProfile = args[0]
            excelFileName = args[1]
            if (args.length == 3) {
                range = args[2].split("-")*.trim()
            }

        } else {
            log.info "Must have 2-3 arg.s Profile-Name/fileName of excel/range"
            System.exit(0)
        }
        UploadToArchive.prelims(args)
        Map excelData = readExcelFile(excelFileName, range)
        if(excelData.success == false){
            log.info("Errors in reading excel file ${excelData.errors}")
            System.exit(0)
        }
        List<UploadItemFromExcelVO> uploadItems = excelData.uploadItems
        log.info("uploadItems(${uploadItems.size()}) ${uploadItems[0].subject} ${uploadItems[0].description} ${uploadItems[0].creator} ${uploadItems[0].absolutePath}")
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        Util.addToUploadCycleWithModeV2(archiveProfile, uploadItems,"Excel-;${excelFileName}-;${range}");

        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromSuppliedData(archiveProfile, uploadItems)
        if (uploadItems) {
            log.info("uploadItems ${uploadItems.size()}")
            log.info("uploadItems ${uploadItems[0].absolutePath}")
            log.info("uploadItems ${uploadItems[-1].absolutePath}")
            log.info("vos ${vos.size()}")
            log.info("vos ${vos[0]}")
            log.info("vos ${vos[-1]}")
            List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(UploadToArchive.metaDataMap, vos)

            log.info("uploadStats ${uploadStats}")
            String report = UploadUtils.generateStats(uploadStats, archiveProfile, vos.size())
            uploadSuccessCheckingMatrix.put(1, report)
        } else {
            log.info("No Items for upload.")
        }
        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, vos.size())
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
        List<UploadItemFromExcelVO> uploadItems = []
        List errors = []
        if (range?.size() == 2 && sheet.size() > 1) {
            start = range[0].toInteger()
            if (start < 1 || start > sheet.size()) {
                errors << "Invalid start range:${start} against Sheet Size(${sheet.size()})"
            }
            end = range[1].toInteger()
            if (end < 1 || end > sheet.size()) {
                errors << "Invalid end range:${end} against Sheet Size(${sheet.size()})"
            }
        }
        if(errors?.size()>0) {
            return [success:false,errors:errors]
        };

        for (int i = start; i <= end; i++) {
            Row row = sheet.getRow(i)
            if (row) {
                String absPath = row.getCell(0).getStringCellValue();
                String subject = row.getCell(1).getStringCellValue()?.replaceAll(/[#!&]/,"")
                String description = row.getCell(2).getStringCellValue()?.replaceAll(/[#!&]/,"")
                String creator = row.getCell(3).getStringCellValue()?.replaceAll(/[#!&]/,"")
                Boolean uploadedFlag = false
                if (row.getCell(4).getCellType() == CellType.BOOLEAN) {
                    uploadedFlag = row.getCell(4).getBooleanCellValue();
                    log.info("readExcelFile uploadedFlag:${uploadedFlag}")
                } else if (row.getCell(4).getCellType() == CellType.STRING) {
                    uploadedFlag = row.getCell(4).getStringCellValue()?.equalsIgnoreCase("true")
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

