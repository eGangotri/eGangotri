package com.egangotri.upload.util

import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
class UploadUtilsSpec extends Specification {
    String tmpDir = "target/test/pre57"

    def setup() {
        System.properties['java.io.tmpdir'] = tmpDir
        new File(tmpDir).mkdirs()
    }

    def cleanup(){
        println "cleanup"
    }

    void "test getPdfsInPreCutOffFolder for a folder containing a pdf in a directory which doesnt have the word pre57 "() {
        given: "pdf in a non-pre57 file system"
        def folder = new File("target/test")
        def file = File.createTempFile("abc", ".pdf", folder)
        List list = UploadUtils.getPdfsInPreCutOffFolder(folder)

        expect: "list is empty"
        println "folder path is " +  folder.absolutePath
        println "list $list"
        list.isEmpty()
    }

    void "test getPdfsInPreCutOffFolder for a folder containing a pdf in pre57 folder"() {
        given: "a pdf in a pre57 folder"
        def folder = File.createTempDir()
        def file = File.createTempFile("abc", ".pdf", folder)
        List list = UploadUtils.getPdfsInPreCutOffFolder(folder)

        expect: "list should have been populated"
        println "folder path is " +  folder.absolutePath
        println "list $list"
        list.size() > 0
    }


}