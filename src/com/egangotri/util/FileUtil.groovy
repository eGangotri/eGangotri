package com.egangotri.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by user on 2/6/2016.
 */


class FileUtil {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static final String HOME_DEFAULT = "C:\\hw"

    static final Map ALL_FOLDERS = [JG_DEFAULT: HOME_DEFAULT + File.separator + "amit", DT_DEFAULT : HOME_DEFAULT + File.separator + "avn\\AvnManuscripts", RK_DEFAULT: HOME_DEFAULT + File.separator + "megha"]

    static final String JG_DEFAULT = ALL_FOLDERS.JG_DEFAULT
    static final String DT_DEFAULT = ALL_FOLDERS.DT_DEFAULT
    static final String RK_DEFAULT = ALL_FOLDERS.RK_DEFAULT

    static final String PRE_57 = "pre57"

    static final List ELIGIBLE_FOLDERS_FOR_PRE57_FILTERING = [RK_DEFAULT, JG_DEFAULT]
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


