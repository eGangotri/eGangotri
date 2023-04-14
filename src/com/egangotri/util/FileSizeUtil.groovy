package com.egangotri.util

class FileSizeUtil {

    static String formatFileSize(BigDecimal sizeInKB){
        BigDecimal sizeInMB = sizeInKB/1024
        BigDecimal sizeInGB = sizeInMB/1024
        return sizeInKB >= 1024 ? (sizeInMB >= 1024 ? "${sizeInGB.round(2)} GB" : "${sizeInMB.round(2)} MB") : "${sizeInKB.round(2)} KB"
    }

    static BigDecimal fileSizeInKBForPath(String absPath) {
        return fileSizeInKB(new File(absPath))
    }

    static BigDecimal fileSizeInKB(File file) {
        if(file && file.exists()){
            return ((file?.size()/ 1024) as BigDecimal)
        }
        return "Empty or Non-Existent File"
    }

    static String getFileSizeFormatted(String absPath){
        return formatFileSize(fileSizeInKBForPath(absPath))
    }

    static String getFileSizeFormatted(File file){
        return formatFileSize(fileSizeInKB(file))
    }


}
