package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * works.
 *
 * expects path a excel file with no header and just the Abs Path:
 * C:\tmp\_data\tmp\Veda Mata Gayatri.pdf
 C:\tmp\_data\tmp\testFolder\Veda Mata Gayatri.pdf
 C:\tmp\_data\tmp\Veda Mata Gayatri - Copy.pdf

 Other fields will be created by using the Profile Metadata
 */

@Slf4j
class UploadToArchiveViaExcelV3 {
    static void main(String[] args) {
        log.info("UploadToArchiveViaExcelV3.main(${args})")
        if (args.length >= 3) {
            String archiveProfile = args[0]
            String excelFileName = args[1]
            String uploadCycleId = args[2]
            String range = args.length == 4 ? args[3] : ""
            List<String> absPaths = readExcelFile(excelFileName)
            if(range.contains("-")) {
                String[] rangeArr = range.split("-")
                if(rangeArr.size() != 2){
                    log.info("Range is not Valid. Exiting")
                    return
                }
                int start = rangeArr[0].toInteger()
                int end = rangeArr[1].toInteger()
                if(start < 1 || end > absPaths.size()){
                    log.info("Range(${start}-${end}) is invalid with actual Uploadable Size(${absPaths.size()}). Exiting")
                    return
                }
                absPaths = absPaths.subList(start-1,end)
            }
            String listOfAbsPathWithPercentSignAsFileSeparator = absPaths.join(UploadersUtil.PERCENT_SIGN_AS_FILE_SEPARATOR)
            UploadToArchiveSelective.main(new String[]{archiveProfile, listOfAbsPathWithPercentSignAsFileSeparator, uploadCycleId})
        } else {
            log.info "Must have 2/3 arg.s Profile name and fileName(s) of pdf as PERCENT Sign Separated. Optional Range"
            return
        }
    }
    static def readExcelFile(filePath) {
        // Opening the Excel file
        FileInputStream file = new FileInputStream(new File(filePath))
        Workbook workbook = new XSSFWorkbook(file)
        log.info("readExcelFile ${filePath}")
        // Get the first sheet
        Sheet sheet = workbook.getSheetAt(0)
        int counter = 0
        int start = 1
        int end = sheet.size()
        List<String> uploadItems = []

        for (int i = start; i <= end; i++) {
            Row row = sheet.getRow(i)
            if (row) {
                String path = row.getCell(0).getStringCellValue();
                if (path.contains(File.separator)) {
                    uploadItems.add(path)
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

