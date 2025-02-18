package com.egangotri.upload.archive.uploaders


import groovy.util.logging.Slf4j
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
class UploadToArchiveViaExcelV3WithOneCol {
    static void main(String[] args) {
        log.info("UploadToArchiveViaExcelV3WithOneCol.main(${args})")
        if (args.length >= 2) {
            String archiveProfile = args[0]
            String excelFileName = args[1]
            String uploadCycleId = args.length >=3 ? args[2] : "";
            String range = args.length == 4 ? args[3] : "";
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
                int absPathsSizeBeforeSubListing = absPaths.size()
                absPaths = absPaths.subList(start-1,end)
                log.info("Trimmed ${absPathsSizeBeforeSubListing} items in excel to (${absPaths.size()}) using Range:(${start}-${end}) ")
            }
            else{
                log.info("No Range provided. Will upload all items in excel")
                range = "1-${absPaths.size()}"
            }

            String listOfAbsPathWithPercentSignAsFileSeparator = absPaths.join(UploadersUtil.PERCENT_SIGN_AS_FILE_SEPARATOR)
            UploadToArchiveSelective.main(new String[]{archiveProfile, listOfAbsPathWithPercentSignAsFileSeparator, uploadCycleId, "Excel-V3-${range}"})
        } else {
            log.info "Must have 2-4 arg.s Profile name and fileName(s) of pdf as PERCENT Sign Separated. Optional Range"
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

