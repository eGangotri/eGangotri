package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class ValidateLinksInArchive {
    static Set archiveProfiles = []
    static File latestIdentifierFile = null
    static List<UploadedLinksVO> links = []

    static main(args) {
        latestIdentifierFile = new File( EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER ).listFiles()?.sort { -it.lastModified() }?.head()

        if(!latestIdentifierFile){
            log.error("No Files in ${EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER}.Cannot proceed. Quitting")
            System.exit(0)
        }
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            println "args $args"
            if(args?.size() != 1){
                log.error("Only 1 File Name can be accepted.Cannot proceed. Quitting")
                System.exit(0)
            }
            latestIdentifierFile = new File(EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER + File.separator + args.first())
            if(!latestIdentifierFile){
                log.error("No such File ${latestIdentifierFile} in ${EGangotriUtil.ARCHIVE_IDENTIFIER_FOLDER}.Cannot proceed. Quitting")
                System.exit(0)
            }
        }

        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        execute(archiveProfiles, metaDataMap)
    }

    static boolean execute(List profiles, Map metaDataMap) {
        println("latestIdentifierFile ${latestIdentifierFile.name}")
        processCSV()
        return true
    }

    static boolean processCSV() {
        latestIdentifierFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect {appendDoubleQuotes(it)}
            println (_fields.class)
            println (_fields)
            links.add(new UploadedLinksVO(_fields.toList()))
        }
        archiveProfiles = links*.archiveProfile as Set
        println(archiveProfiles)
    }

    static String appendDoubleQuotes(String field)
    {
        if(!field.endsWith("\"")) {
            return field.concat("\"")
        }
        else {
            return field
        }
    }
}
