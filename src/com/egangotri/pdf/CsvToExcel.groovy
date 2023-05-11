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
            rows?.tail()?.eachWithIndex { String rowStr, int rowIndex ->
                //we dont need sep=; line
                Row row = sheet.createRow(rowIndex)
                String[] columns = rowStr.split(BookTitles.CSV_SEPARATOR)
                columns.eachWithIndex { colStr, colIndex ->
                    Cell cell = row.createCell(colIndex)
                    cell.setCellValue(colStr)
                }
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

