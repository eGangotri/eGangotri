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
            if(args.length == 3){
                range = args[2].split("-")*.trim()
            }

        } else {
            log.info "Must have 2-3 arg.s Profile-Name/fileName of pdf/range"
            System.exit(0)
        }
        UploadToArchive.prelims(args)
        List<UploadItemFromExcel> uploadItems = readExcelFile(excelFileName)
        log.info("uploadItems(${uploadItems.size()}) ${uploadItems[0].subject} ${uploadItems[0].description} ${uploadItems[0].creator} ${uploadItems[0].absolutePath}")
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]

        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromSuppliedData(archiveProfile,uploadItems, range)
        log.info("vos ${vos}")
        if (uploadItems) {
            log.info("uploadItems ${uploadItems}")
            log.info("vos ${vos}")
            List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(UploadToArchive.metaDataMap, vos)

         //  List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(UploadToArchive.metaDataMap, vos as Set<QueuedVO>)
          //  log.info("uploadStats ${uploadStats}")
            String report = UploadUtils.generateStats(uploadStats, archiveProfile, vos.size())
            uploadSuccessCheckingMatrix.put(1, report)
        } else {
            log.info("No Items for upload.")
        }
        EGangotriUtil.recordProgramEnd()
        ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, vos.size())
        System.exit(0)
    }

    static def readExcelFile(filePath) {
        // Opening the Excel file
        FileInputStream file = new FileInputStream(new File(filePath))
        Workbook workbook = new XSSFWorkbook(file)
        log.info("readExcelFile ${filePath}")
        // Get the first sheet
        Sheet sheet = workbook.getSheetAt(0)

        // Iterate through each row of the first sheet
        List<UploadItemFromExcel> uploadItems = []
        sheet.each { row ->
            UploadItemFromExcel uploadItem = new UploadItemFromExcel(row.getCell(0).getStringCellValue() ,
                    row.getCell(1).getStringCellValue() ,
                    row.getCell(2).getStringCellValue() ,
                    row.getCell(3).getStringCellValue())
            Boolean uploadedFlag = row.getCell(4)?.getBooleanCellValue()
            log.info("uplFlg: ${uploadedFlag}")
            log.info("row.getCell(4)?.getBooleanCellValue(): ${row.getCell(4)?.getBooleanCellValue()}")
            if(!uploadedFlag){
                //uploadItems.add(uploadItem)
                log.info("adding ..")
            }
        }
        // Close resources
        workbook.close()
        file.close()
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

