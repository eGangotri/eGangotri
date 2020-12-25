package com.egangotri.mover

import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class ZipMover {
    static String srcFolder = ''
    static List CODES = ["JB", "JM", "VN", "MB", "SN", "KS", "VM", "RORI", "ABSP", "VK"]
    static Map CODE_TO_FOLDER_MAP = [:]

    static void main(String[] args) {
        if (args) {
            log.info "args $args"
            srcFolder = args[0]
        }
        File downloadFolder = new File(srcFolder)
        downloadFolder.listFiles(validFiles())
        log.info("ZipMover started for ${downloadFolder}")

        File[] zips = downloadFolder.listFiles(validFiles())
        log.info("ZipMover started for ${zips}  on ${UploadUtils.getFormattedDateString()}")
        setCodeToFolderMap()
        new ZipMover().move(zips)
    }
    void move(File[] zips) {
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        zips.each { zipFile ->
            String destDir = metaDataMap[CODE_TO_FOLDER_MAP[zipFile.name.split("-").first()]]

            log.info("destDir; ${destDir}")
            //FileUtil.moveZip(zipFile.absolutePath, destDir)
        }
    }



    static FileFilter validFiles() {
        FileFilter fileFilter = { File file ->
            (file.name.endsWith(".zip") ||
                    file.name.endsWith(".rar")) && isValidCode(file.name)
        } as FileFilter
        return fileFilter
    }

    static Boolean isValidCode(String fileName) {
        boolean valid = false
        CODES.forEach { code ->
            {
                if (fileName.startsWith("${code}-")) {
                    valid = true
                }
            }
        }
        return valid
    }

    static void setCodeToFolderMap() {
        CODE_TO_FOLDER_MAP.put("JB", "JNGM")
        CODE_TO_FOLDER_MAP.put("JM", "JNGM_BOOKS")
        CODE_TO_FOLDER_MAP.put("VN", "VN2")
        CODE_TO_FOLDER_MAP.put("MB", "MUM")
        CODE_TO_FOLDER_MAP.put("ANON6", "ANON6")

        CODE_TO_FOLDER_MAP.put("SN", "SANJEEVANI")
        CODE_TO_FOLDER_MAP.put("VM", "VED_MANDIR")
        CODE_TO_FOLDER_MAP.put("KS", "PSTK_DVTA")

        CODE_TO_FOLDER_MAP.put("RORI", "RORI")

        CODE_TO_FOLDER_MAP.put("VK", "VK")
        CODE_TO_FOLDER_MAP.put("SR", "SR")

    }

}


