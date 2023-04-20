package com.egangotri.util

import groovy.io.FileType

class FileSizeUtil {

    static String formatFileSize(BigDecimal sizeInKB, String delimiter = " "){
        BigDecimal sizeInMB = sizeInKB/1024
        BigDecimal sizeInGB = sizeInMB/1024
        return sizeInKB >= 1024 ? (sizeInMB >= 1024 ? "${sizeInGB.round(2)}${delimiter}GB" : "${sizeInMB.round(2)}${delimiter}MB") : "${sizeInKB.round(2)}${delimiter}KB"
    }

    static BigDecimal fileSizeInKBForPath(String absPath) {
        return fileSizeInKB(new File(absPath))
    }

    static BigDecimal fileSizeInKB(File file, String delimiter = " ") {
        if(file && file.exists()){
            return ((file?.size()/ 1024) as BigDecimal)
        }
        return "Empty or Non-Existent File"
    }

    static String getFileSizeFormatted(String absPath, String delimiter = " "){
        return formatFileSize(fileSizeInKBForPath(absPath),delimiter)
    }

    static String getFileSizeFormatted(File file, String delimiter = " "){
        return formatFileSize(fileSizeInKB(file),delimiter)
    }

    /*
Cannot use FileNameFilter or FileFilter as we are dealing with sub-directories
 */
    static File[] allPdfsInDirAsFileList(String srcDir) {
        File srcDirAsFile = new File(srcDir)
        def files = []
        if (srcDirAsFile) {
            srcDirAsFile.eachFileRecurse (FileType.FILES) { File file ->
                if(file.name.endsWithIgnoreCase(".pdf")){
                    files << file
                }
            }
        }
        return files
    }

    static String[] allPdfsInDirAsFilenameList(String srcDir) {
        return allPdfsInDirAsFileList(srcDir)
    }

}
