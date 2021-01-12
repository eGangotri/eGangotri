package com.egangotri.mover


import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class ZipMover {
    static String SRC_FOLDER = System.getProperty("user.home") + File.separator + "Downloads"
    static String UPLOAD_REPORT_FILE = ""
    static Map<String, String> CODE_TO_FOLDER_MAP = setCodeToFolderMap()
    static String DEFAULT_LOCAL_FOLDER_CODE = "ANON6"
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
        if (zips) {
            log.info("ZipMover started for \n${zips*.name.join(",\n")}")
            moveZips(zips)
            compareZippedFilesToReportedFilesList()

        } else {
            log.info("No Zips")
        }

    }

    static void moveZips(File[] zips) {
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

    static Map<String, String> setCodeToFolderMap() {
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

    static String getCodeToFolderMap(String code) {
        if (CODE_TO_FOLDER_MAP.containsKey(code)) {
            return CODE_TO_FOLDER_MAP[code]
        } else return DEFAULT_LOCAL_FOLDER_CODE
    }

    static List<String> compareZippedFilesToReportedFilesList() {
        File uploadReportFile = new File(SRC_FOLDER, UPLOAD_REPORT_FILE)
        if(uploadReportFile.exists()){
            List<String> ALL_FILES_IN_LIST = processUploadReportFile(uploadReportFile)
            log.info("ALL_ZIP_FILES_PROCESSED: $ALL_ZIP_FILES_PROCESSED")
            def intersection = ALL_ZIP_FILES_PROCESSED.intersect(ALL_FILES_IN_LIST)
            def subtraction = ALL_FILES_IN_LIST - intersection
            log.info("Following pdf(s) \n${subtraction.join("\n")} \n[Count: ${subtraction.size()} ] not found")
        }
    }

    static List<String> processUploadReportFile(File reportFile) {
        String line = ""
        List<String> titles = []
        reportFile.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                //This will not work always because some file have errors
                //def pattern = ~/pdf\s\d.*\s\d.*\.\d.*\sMB/
                def pattern = ~/\(\d.*\)\.\s.*.pdf/
                def matcher = (line =~ pattern).findAll()
                if (matcher) {
                    //(1). Bhagvad Gita Vira Shaiva Bhashyam by Dr. T.G.Sidhhapparadhya Part 2 - Jangamwadi Math Collection.pdf 394 147.19 MB
                    //(2).	****Brihadaranya Satika Bhashyam Sanskrit Printed (incomplete) - Mumukshu Bhawan Collection-009.pdf	ERROR-READING	  	2.75 GB

                    String[] token = matcher[0].toString().split(/\s/, 2)
                    titles << token[1].trim()
                }
            }
        }
        log.info("Found ${titles.size()} titles in report")
        return titles
    }
}


