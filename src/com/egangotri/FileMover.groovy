package com.egangotri

import groovyx.gpars.GParsPool

/**
 * Created by user on 10/31/2015.
 */
class FileMover {
    static String SOURCE_FOLDER = "C:\\hw\\amit\\folder-1"
    static String DEST_DIR = "I:\\KashmirUniHindiBooks\\zippedFiles"

    static List ZIP = [".zip", ".rar"]
    static List ignoreList = []
    static boolean inSeparateFolder = true
    static boolean multiThreaded = false

    static main(args) {
        File file = new File(SOURCE_FOLDER)
        moveFile(file);
    }

    public static moveFile(File file) {
        def ant = new AntBuilder()   // create an antbuilder
        println "${file.absolutePath}"


        ant.copydir(src: file.absolutePath,
                dest: DEST_DIR)


//
//        ant.copy(file: file.absolutePath,
//                toDir: DEST_DIR)
    }
}
