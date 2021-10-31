package com.egangotri.mover


import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class ZipMover {
    static String SRC_FOLDER = System.getProperty("user.home") + File.separator + "Downloads"
    static String UPLOAD_REPORT_FILE = ""
    static Boolean ONLY_MOVE_DONT_UNZIP = false
    static List ALL_ZIP_FILES_PROCESSED = []

    static void main(String[] args) {
        if (args) {
            log.info "args $args"
            UPLOAD_REPORT_FILE = args[0]
            if(args.size()> 1){
                SRC_FOLDER = args[1]
            }
        }
        File downloadFolder = new File(SRC_FOLDER)
        log.info("Read Download Folder ${downloadFolder} on ${UploadUtils.getFormattedDateString()}")
        File[] zips = downloadFolder.listFiles(validFiles())
        log.info("zips ${zips}");
        if (zips) {
            log.info("ZipMover started for \n${zips*.name.join(",\n")}")
            moveZipsAndUnzip(zips)
            compareZippedFilesToReportedFilesList()

        } else {
            log.info("No Zips")
        }

    }

    static void moveZipsAndUnzip(File[] zips) {
        String destDir = ""
        zips.each { zipFile ->
            destDir = Codes.getDestDirByFileName(zipFile)
            FileUtil.moveAndUnzip(zipFile, destDir)
        }
        if(destDir){
            Runtime.getRuntime().exec("explorer.exe /select," + destDir)
        }
    }
    static FileFilter validFiles() {
        FileFilter fileFilter = { File file ->
            (file.name.endsWith(".zip") ||
                    file.name.endsWith(".rar")) && Codes.isValidCode(file.name)
        } as FileFilter
        return fileFilter
    }

    static List<String> compareZippedFilesToReportedFilesList() {
        File uploadReportFile = new File(SRC_FOLDER, UPLOAD_REPORT_FILE)
        if(uploadReportFile.exists()){
            List<String> ALL_FILES_IN_LIST = Codes.processUploadReportFile(uploadReportFile)
            log.info("ALL_ZIP_FILES_PROCESSED: $ALL_ZIP_FILES_PROCESSED")
            def intersection = ALL_ZIP_FILES_PROCESSED.intersect(ALL_FILES_IN_LIST)
            def subtraction = ALL_FILES_IN_LIST - intersection
            log.info("Dumped ${ALL_ZIP_FILES_PROCESSED.size()} titles in Zips")
            //log.info("Following pdf(s) \n${subtraction.join("\n")} \n[Count: ${subtraction.size()} ] from ${ALL_FILES_IN_LIST.size()} not found")
        }
    }
}


