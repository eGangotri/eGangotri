package com.egangotri.util

import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Slf4j
class FileUtil {
    static final String HOME_DEFAULT = "C:\\hw"
    private static final Map<String,String> ALL_FOLDER_DIRNAMES =
            [JG: "amit", DT:"avn\\AvnManuscripts", RK: "megha", NK: "nk", UR: "dayal\\Prof-Shahid", SR: "dayal\\Sarai" ]

    static final Map<String,String> ALL_FOLDERS = [:]
    static{
        ALL_FOLDER_DIRNAMES.each{k,v ->  ALL_FOLDERS[k] = HOME_DEFAULT + File.separator + v }
    }

    static final String PRE_CUTOFF = "pre57"
    static String PDF_REGEX = /.*.pdf/

    public static moveDir(String srcDir, String destDir) {
        // create an ant-builder
        def ant = new AntBuilder()
       log.info("Src $srcDir " + "dst: $destDir")

        ant.move(todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true') {
            fileset(dir: srcDir) {
                include(name: "**/*.*")
            }
        }
    }
}


