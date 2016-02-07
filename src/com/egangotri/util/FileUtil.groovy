package com.egangotri.util

/**
 * Created by user on 2/6/2016.
 */


class FileUtil {
    static final String HOME_DEFAULT = "C:\\hw"


    static final String JG_DEFAULT = HOME_DEFAULT + File.separator + "amit"
    static final String DT_DEFAULT = HOME_DEFAULT + File.separator + "avn\\AvnManuscripts"
    static final String RK_DEFAULT = HOME_DEFAULT + File.separator + "megha"
    static final String PRE_57 = "pre57"

    public static moveDir(String srcDir, String destDir) {
        // create an ant-builder
        def ant = new AntBuilder()
        println("Src $srcDir " + "dst: $destDir")

        ant.move(todir: destDir, verbose:'true', overwrite: 'false', preservelastmodified:'true') {
            fileset(dir: srcDir) {
                include(name: "**/*.*")
            }
        }
    }

}
