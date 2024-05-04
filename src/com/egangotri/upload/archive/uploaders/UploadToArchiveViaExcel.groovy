package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
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
            log.info "Must have 2-3 arg.s Profile-Name/fileName of pdf/range"
            System.exit(0)
        }
        UploadToArchive.prelims(args)
        List<UploadItemFromExcel> uploadItems = readExcelFile(excelFileName, range)
        log.info("uploadItems(${uploadItems.size()}) ${uploadItems[0].subject} ${uploadItems[0].description} ${uploadItems[0].creator} ${uploadItems[0].absolutePath}")
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]

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
        List<UploadItemFromExcel> uploadItems = []
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
                String absPath = row.getCell(0).getStringCellValue();
                String subject = row.getCell(1).getStringCellValue()
                String description = row.getCell(2).getStringCellValue()
                String creator = row.getCell(3).getStringCellValue()
                Boolean uploadedFlag = false
                if (row.getCell(4).getCellType() == CellType.BOOLEAN) {
                    uploadedFlag = row.getCell(4).getBooleanCellValue();
                    log.info("readExcelFile uploadedFlag:${uploadedFlag}")
                } else if (row.getCell(4).getCellType() == CellType.STRING) {
                    uploadedFlag = row.getCell(4).getStringCellValue()?.equalsIgnoreCase("true")
                }

                UploadItemFromExcel uploadItem = new UploadItemFromExcel(absPath, subject, description, creator, uploadedFlag)
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
        return uploadItems
    }

    static def printCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case CellType.STRING:
                print(cell.getRichStringCellValue().getString() + "\t")
                break
            case CellType.NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    print(cell.getDateCellValue() + "\t")
                } else {
                    print(cell.getNumericCellValue() + "\t")
                }
                break
            case CellType.BOOLEAN:
                print(cell.getBooleanCellValue() + "\t")
                break
            case CellType.FORMULA:
                print(cell.getCellFormula() + "\t")
                break
            default:
                print(" ")
        }
    }

}

