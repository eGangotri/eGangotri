package com.egangotri

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool

/**
 * Created by user on 7/11/2015.
 */
@Slf4j
class FolderUnzipper {
    static String FOLDER_NAME = "C:\\hw\\amit\\zipp"
    static List<String> ZIP = [".zip", ".rar"]
    static List ignoreList = []
    static boolean inSeparateFolder = false
    static boolean multiThreaded = false

    static main(String[] args) {
        File directory = new File(FOLDER_NAME)
        if (multiThreaded) {
            List<File> files = directory.listFiles().toList()
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

    static void unZip(File file) {
        String destDir = FOLDER_NAME
        if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && (file.name.endsWith(ZIP[0]) || (file.name.endsWith(ZIP[1])))) {
            if (inSeparateFolder) {
                destDir = FOLDER_NAME + File.separator + (file.name - ZIP)
                new File(destDir).mkdir()
            }
            def ant = new groovy.ant.AntBuilder()   // create an antbuilder
            log.info "${file.absolutePath}"

            ant.with {
                echo 'begin unzipping'
                unzip(src: file.absolutePath,
                        dest: destDir,
                        overwrite: "false")
                echo 'done unzipping'
            }
        }
    }
}


