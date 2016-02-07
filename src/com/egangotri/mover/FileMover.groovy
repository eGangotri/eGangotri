package com.egangotri.mover

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import groovyx.gpars.GParsPool

/**
 * Created by user on 10/31/2015.
 */
class FileMover {
    static String DEST_ROOT_DIR = "C:\\Treasures6"

    //"C:\\Treasures6\\megha\\Alm-2"
    static Map<String, List<String>> srcDestMap
    static List profiles = [/*"jg", "dt", */"sr"]

    static main(args) {
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/SrcDestData.properties")
        profiles.each {  profile ->
        //String srcDir, String destDir ->
            FileUtil.moveDir(metaDataMap["${profile}.src"], metaDataMap["${profile}.dest"]);
        }
    }


}
