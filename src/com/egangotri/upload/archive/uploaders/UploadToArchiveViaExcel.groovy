package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.vo.QueuedVO
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

        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromSuppliedData(archiveProfile,uploadItems, range)
        log.info("vos ${vos}")
        if (uploadItems) {
            log.info("uploadItems ${uploadItems}")
            log.info("vos ${vos}")
           List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(UploadToArchive.metaDataMap, vos as Set<QueuedVO>)
          //  log.info("uploadStats ${uploadStats}")
        } else {
            log.info("File ${excelFileName} not found")
        }
        System.exit(0)
    }

    static String getLocalPath(String archiveProfile, String fileName) {
        String _folder = FileRetrieverUtil.pickFolderBasedOnArchiveProfile(archiveProfile)
        String filePath = ""
        File folder = new File(_folder)
        if (folder.exists() && folder.isDirectory()) {
            // Create a File object for the file inside the folder
            def file = new File(folder, fileName)
            // Check if the file exists
            if (file.exists() && file.isFile()) {
                // Get the absolute path of the file
                filePath = file.getCanonicalPath()
                println "Absolute path of $fileName: $filePath"
            } else {
                println "File $fileName ${file.absolutePath} does not exist in the folder."
            }
        } else {
            println "Folder $folder does not exist."
        }
        return filePath;
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
            uploadItems.add(uploadItem)
            row.each { cell ->
                // Print out the cell's contents
                //print(cell.getStringCellValue() + "XX\t")
              //  printCellValue(cell)
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

