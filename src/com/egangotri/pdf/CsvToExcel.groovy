package com.egangotri.pdf

import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*

@Slf4j
class CsvToExcel {
    static void csvtoXls(File csvFile) {
        if (csvFile.exists()) {
            // Create an Excel workbook and sheet
            Workbook workbook = new XSSFWorkbook()
            Sheet sheet = workbook.createSheet("Data")
            def contents = csvFile.getText()

            // Parse the CSV data and write it to the sheet
            String[] rows = contents.split("\n")
            int maxColumns = 0
            rows?.tail()?.eachWithIndex { String rowStr, int rowIndex ->
                //we dont need sep=; line
                Row row = sheet.createRow(rowIndex)
                String[] columns = rowStr.split(BookTitles.CSV_SEPARATOR)
                columns.eachWithIndex { colStr, colIndex ->
                    Cell cell = row.createCell(colIndex)
                    cell.setCellValue(colStr)
                }
                if (columns.length > maxColumns) {
                    maxColumns = columns.length
                }
            }
            // Auto-size columns to fit content (at least header width)
            for (int c = 0; c < maxColumns; c++) {
                sheet.autoSizeColumn(c)
            }

            // Save the workbook to an Excel file
            String excelName = csvFile.absolutePath.replace(".csv", "") + ".xlsx"
            FileOutputStream fileOut = new FileOutputStream(excelName)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()
            log.info("CSV to Excel : ${excelName} ")
        }
    }
}
