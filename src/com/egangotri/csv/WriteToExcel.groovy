package com.egangotri.csv

import com.egangotri.util.EGangotriUtil
import org.apache.poi.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet

/**
 * Created by user on 2/21/2016.
 */
class WriteToExcel {
    static String ARCHIVE_IDENTIFIER = "archiveIdentifer.xls"

    static void toCSV(Map mapOfArchiveIdAndFileName) {
        String filePath = EGangotriUtil.EGANGOTRI_BASE_DIR + File.separator + ARCHIVE_IDENTIFIER
        int lastRowNum = getLastRowNum(filePath)
        write(filePath, lastRowNum)
    }

    static int getLastRowNum(String filePath) {
        FileInputStream fis = null;
        HSSFWorkbook workbook
        int lastRowNum = 0
        try {
            File file = new File(filePath)
            fis = new FileInputStream(file);
            workbook = new HSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            lastRowNum = sheet.getLastRowNum()
            //Row row = sheet.createRow((short) ( + 1));

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            fis.close()
        }
        return lastRowNum
    }

    static int write(String filePath, int lastRowNum){
        FileOutputStream fileOut = new FileOutputStream(filePath)

    }


}
