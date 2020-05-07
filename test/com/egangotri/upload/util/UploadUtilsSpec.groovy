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
        log.info "cleanup"
    }

    void "test encodeString"(){
        given:""
        def val = UploadUtils.encodeString("description=Manuscripts of Dharmartha Trust ( धर्मार्थ ट्रस्ट )  at Raghunath Temple, Jammu,J&K")

        ["description=Hindi%20Books",
                "description=Sanskrit%20and%20Other%20Books",
                "description=Hindi%20Literature%20and%20Other%20Books",
                "description=Sharada%20Manuscripts%20and%20Other%20Dharma%20Books%20in%20KECSS%20Reference%20Library%20Pamposh%20Enclave%20New%20Delhi",
                "description=Sanskrit%20Vyakarana%20Books%20(व्याकरण%20ग्रंथाः)",
                "description=Complete%20Collection%20of%20Vraj%20Vallabh%20Dwivedijis%20Works"
        ].each{
            log.info java.net.URLDecoder.decode(it, "UTF-8")
        }

        def val2 = java.net.URLDecoder.decode("description=Manuscripts%20of%20Dharmartha%20Trust%20(%20धर्मार्थ%20ट्रस्ट%20)%20%20at%20Raghunath%20Temple,%20Jammu,J%26K", "UTF-8") //== "dehydrogenase+%28NADP%2B%29"//("2 3 4")
        expect: ""
        assert val != null
    }


    void "test getPdfsInPreCutOffFolder for a folder containing a pdf in a directory which doesnt have the word pre57 "() {
        given: "pdf in a non-pre57 file system"
        def folder = new File("target/test")
        def file = File.createTempFile("abc", ".pdf", folder)
        List list = UploadUtils.getFilesInPreCutOffFolder(folder)

        expect: "list is empty"
        log.info "folder path is " +  folder.absolutePath
        log.info "list $list"
        list.isEmpty()
    }

    void "test getPdfsInPreCutOffFolder for a folder containing a pdf in pre57 folder"() {
        given: "a pdf in a pre57 folder"
        def folder = File.createTempDir()
        def file = File.createTempFile("abc", ".pdf", folder)
        List list = UploadUtils.getFilesInPreCutOffFolder(folder)

        expect: "list should have been populated"
        log.info "folder path is " +  folder.absolutePath
        log.info "list $list"
        list.size() > 0
    }


}