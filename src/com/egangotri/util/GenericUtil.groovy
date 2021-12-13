package com.egangotri.util

import groovy.util.logging.Slf4j

@Slf4j
class GenericUtil {
    static List<String> REPORT = [];
    static int ELLIPSIS_LIMIT= 10
    static String reverseEllipsis(String text){
        return  (text.length() > ELLIPSIS_LIMIT ?
                "..."+ text.substring(text.length() - ELLIPSIS_LIMIT, text.length()):text)
    }

    static String ellipsis(String text){
        return  text.length() > ELLIPSIS_LIMIT ?
                text.substring(0, ELLIPSIS_LIMIT) + "..." :text
    }

    static String reverseEllipsis(File file){
        return reverseEllipsis(file.absolutePath)
    }

    static String ellipsis(File file){
        return ellipsis(file.absolutePath)
    }

    static String addReport(String report) {
        REPORT.push(report)
        log.info(report)
    }

    static void printReport() {
        REPORT.eachWithIndex { x, i -> {
            log.info("${i + 1}). ${x}")
        }
        }
    }

    static File[] getPdfs(File dir){
        return getFilesOfGivenType(dir, "pdf")
    }

    static File[] getTifs(File dir){
        return getFilesOfGivenType(dir, "tif")
    }
    static File[] getPngs(File dir){
        return getFilesOfGivenType(dir, "png")
    }
    static File[] getDirectories(File dir){
        return dir.listFiles({ File d-> d.isDirectory() } as FileFilter)?.sort{ File f -> f.lastModified()}
    }
    static File[] getFilesOfGivenType(File dir, String ext){
        return dir.listFiles({ File d, String f -> f ==~ /(?i).*.${ext}/ } as FilenameFilter)?.sort{ File f -> f.lastModified()}
    }

}
