package com.egangotri.util

import com.egangotri.pdf.EGangotriPDFMerger
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

    static String dualEllipsis(String text){
        return  text.length() > ELLIPSIS_LIMIT*2 ?
                ellipsis(text) + reverseEllipsis(text) :ellipsis(text)
    }

    static String dualEllipsis(File file){
        return  dualEllipsis(file.absolutePath)
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
        REPORT.reverse().eachWithIndex { x, i -> {
            log.info("${i + 1}). ${x}")
        }
        }
    }

    static File[] getPdfs(File dir, String filter = EGangotriPDFMerger.OLD_LABEL){
        File[] pdfs = getFilesOfGivenType(dir, "pdf")
        if(filter){
            pdfs.findAll{!it.name.contains(filter)}
        }
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
    static File[] getDirectoriesSortedByName(File dir){
        return dir.listFiles({ File d-> d.isDirectory() } as FileFilter)?.sort{ File f -> f.name}
    }

    static File[] getDirectoriesSortedByName(String dir){
        return getDirectoriesSortedByName(new File(dir))
    }

    static File[] getFilesOfGivenType(File dir, String ext){
        return dir.listFiles({ File d, String f -> f ==~ /(?i).*.${ext}/ } as FilenameFilter)?.sort{ File f -> f.lastModified()}
    }
    static void garbageCollectAndPrintMemUsageInfo() {
        double memUse = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
        log.info("Garbage Collecting.\nMemory being used: ${Math.round(memUse)} mb.")
        System.gc()
        memUse = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
        log.info("Memory use after Garbage Collection: ${Math.round(memUse)} mb")
    }
}
