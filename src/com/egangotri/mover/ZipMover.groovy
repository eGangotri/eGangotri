package com.egangotri.mover

import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class ZipMover {
    static String srcFolder = System.getProperty("user.home") + File.separator + "Downloads"
    static Map<String, String> CODE_TO_FOLDER_MAP = setCodeToFolderMap()
    static DEFAULT_LOCAL_FOLDER_CODE = "ANON6"

    static void main(String[] args) {
        if (args) {
            log.info "args $args"
            srcFolder = args[0]
        }
        File downloadFolder = new File(srcFolder)
        log.info("Read Download Folder ${downloadFolder} on ${UploadUtils.getFormattedDateString()}")
        File[] zips = downloadFolder.listFiles(validFiles())
        if(zips){
            log.info("ZipMover started for \n${zips*.name.join(",\n")}")
            new ZipMover().move(zips)
        }
        else{
            log.info("No Zips")
        }
    }
    void move(File[] zips) {
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        zips.each { zipFile ->
            String code = zipFile.name.split("-")?.first()
            String destDir = metaDataMap[getCodeToFolderMap(code)]
            FileUtil.moveAndUnzip(zipFile, destDir)
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
        CODE_TO_FOLDER_MAP.keySet().forEach { code ->
            {
                if (fileName.startsWith("${code}-")) {
                    valid = true
                }
            }
        }
        return valid
    }

    static Map setCodeToFolderMap() {
        Map c2FMap = [:]
        c2FMap.put("JB", "JNGM_BOOKS")
        c2FMap.put("JN", "JNGM_BOOKS")
        c2FMap.put("JNGM", "JNGM_BOOKS")
        c2FMap.put("JM", "JNGM")
        c2FMap.put("VN", "VN2")
        c2FMap.put("MB", "MUM")
        c2FMap.put("ANON6", "ANON6")
        c2FMap.put("GNJ", "GNJ")


        c2FMap.put("SN", "SANJEEVANI")
        c2FMap.put("VM", "VED_MANDIR")
        c2FMap.put("KS", "PSTK_DVTA")

        c2FMap.put("RORI", "RORI")

        c2FMap.put("VK", "VK")
        c2FMap.put("SR", "SR")
        c2FMap.put("VM", "VED_MANDIR")
        c2FMap.put("RORI", "RORI")
        c2FMap.put("ABSP", "ABSP")
        c2FMap.put("VK", "VK")
        c2FMap.put("ST", "SARVESH")
        return c2FMap
    }

    static String getCodeToFolderMap(String code){
        if(CODE_TO_FOLDER_MAP.containsKey(code)){
            return CODE_TO_FOLDER_MAP[code]
        }
        else return DEFAULT_LOCAL_FOLDER_CODE
    }
}


