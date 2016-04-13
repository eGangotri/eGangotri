package com.egangotri.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by user on 2/6/2016.
 */


class FileUtil {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static final String HOME_DEFAULT = "C:\\hw"
    private static final Map<String,String> ALL_FOLDER_DIRNAMES = [JG: "amit", DT:"avn\\AvnManuscripts", RK: "megha", NK: "nk", DD: "ddp" ]

    static final Map<String,String> ALL_FOLDERS = [:]
    static{
        ALL_FOLDER_DIRNAMES.each{k,v ->  ALL_FOLDERS[k] = HOME_DEFAULT + File.separator + v }
    }

    static final String PRE_CUTOFF = "pre57"
    static String PDF_REGEX = /.*.pdf/

    public static moveDir(String srcDir, String destDir) {
        // create an ant-builder
        def ant = new AntBuilder()
        Log.info("Src $srcDir " + "dst: $destDir")

        ant.move(todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true') {
            fileset(dir: srcDir) {
                include(name: "**/*.*")
            }
        }
    }
}


