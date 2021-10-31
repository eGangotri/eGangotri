package com.egangotri.mover

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class Codes {
    static Map<String, String> CODE_TO_FOLDER_MAP = setCodeToFolderMap()
    static List<String> EXCLUDABLE_CODES = []
    static String DEFAULT_LOCAL_FOLDER_CODE = "ANON"
    static Map<String, String> setCodeToFolderMap() {
        Map<String, String> c2FMap = [:]
        c2FMap.put("JB", "JNGM")
        c2FMap.put("JNGM", "JNGM")
        c2FMap.put("JM", "JNGM_MANU")
        c2FMap.put("VN", "VN2")
        c2FMap.put("MB", "MB")
        c2FMap.put("ANON", "ANON")
        c2FMap.put("LBSB", "ANON")
        c2FMap.put("JI", "ANON")
        c2FMap.put("MUTHU", "MUTHU")
        c2FMap.put("BMB", "MUTHU")
        c2FMap.put("MORI", "MORI")
        c2FMap.put("PN", "PUNEET")
        c2FMap.put("SN", "SANJEEVANI")
        c2FMap.put("VM", "VED_MANDIR")
        c2FMap.put("KS", "PSTK_DVTA")

        c2FMap.put("LBS", "LBS")
        c2FMap.put("LB", "BV")
        c2FMap.put("ORIM", "ORIM")
        c2FMap.put("VK", "VK")
        c2FMap.put("SR", "SR")
        c2FMap.put("VM", "VED_MANDIR")
        c2FMap.put("ABSP", "ABSP")
        c2FMap.put("VK", "VK")
        c2FMap.put("ST", "SARVESH")
        c2FMap.put("AA", "AA")
        c2FMap.put("KRI", "KRI")
        c2FMap.put("BV", "BV")
        return removeExcludables(c2FMap)
    }

    static Boolean isValidCode(String fileName) {
        boolean valid = false
        log.info("CODE_TO_FOLDER_MAP.keySet() ${CODE_TO_FOLDER_MAP.keySet()}")
        CODE_TO_FOLDER_MAP.keySet().forEach { code ->
            {
                if (fileName.toUpperCase().startsWith("${code}")) {
                    valid = true
                }
            }
        }
        return valid
    }


    static String getCodeToFolderMap(String code) {
        if (CODE_TO_FOLDER_MAP.containsKey(code)) {
            return CODE_TO_FOLDER_MAP[code]
        } else return DEFAULT_LOCAL_FOLDER_CODE
    }


    static removeExcludables(Map<String,String> c2FMap){
        EXCLUDABLE_CODES?.forEach { String code ->
            if(c2FMap.containsKey(code)){
                c2FMap.remove(code);
            }
        }
        println("${c2FMap}")
        return c2FMap;
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
        log.info("Found ${titles.size()} titles in Report")
        return titles
    }

    static getDestDirByCode(String code){
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        return metaDataMap[getCodeToFolderMap(code)]
    }

    static getDestDirByFileName(File file){
        String code = file.name.split(/\s*-/)?.first()?.toUpperCase()
        return getDestDirByCode(code)
    }

}
