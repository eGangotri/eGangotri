package com.egangotri.mover

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import org.slf4j.*

/**
 * Created by user on 10/31/2015.
 */
class FileMover {
    static String DEST_ROOT_DIR = "C:\\Treasures6"
    static Map<String, List<String>> srcDestMap
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static List profiles = ["dt", "sr", "jg"] //"sr", "dt", "jg"

    static main(args) {
        if (args) {
            Log.info "args $args"
            profiles = args.toList()
        }
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/SrcDestData.properties")
        profiles.each {  profile ->
            FileUtil.moveDir(metaDataMap["${profile}.src"], metaDataMap["${profile}.dest"]);
        }
    }
}
