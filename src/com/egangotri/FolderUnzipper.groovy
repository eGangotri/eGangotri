package com.egangotri

import groovyx.gpars.GParsPool

/**
 * Created by user on 7/11/2015.
 */
class FolderUnzipper {
    static String FOLDER_NAME = "KashmirUniHindiBooks - Copy"
    static List ZIP = [".zip", ".rar"]
    static List ignoreList = []
    static boolean inSeparateFolder = true
    static boolean multiThreaded = false

    static main(args) {
        File directory = new File(FOLDER_NAME)
        if (multiThreaded) {
            List files = directory.listFiles()
            GParsPool.withPool {
                files.eachParallel { File file ->
                    unZip(file)
                }
            }
        } else {
            for (File file : directory.listFiles()) {
                unZip(file)
            }
        }


    }

    public static unZip(File file) {
        String destDir = FOLDER_NAME
        if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) &&  ZIP.find{"ram.zip".endsWith(it.toString())}) {
            if (inSeparateFolder) {
                destDir = FOLDER_NAME + File.separator + (file.name - ZIP)
                new File(destDir).mkdir()
            }
            def ant = new AntBuilder()   // create an antbuilder
            println "${file.absolutePath}"
            ant.unzip(src: file.absolutePath,
                    dest: destDir,
                    overwrite: "false")
        }
    }
}


