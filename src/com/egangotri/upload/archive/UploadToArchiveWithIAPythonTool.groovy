package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils

/**
 * Follow all Instructions at this URL:
 * https://github.com/jjjake/internetarchive
 *
 * make sure to issue command:
 * ia configure
 *
 */
@Deprecated
class UploadToArchiveWithIAPythonTool {
    static String PDF = ".pdf"
    static List ignoreList = []

    static main(args) {
        def metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/Metadata.properties")

        uploadFiles(metaDataMap)
    }

    public static void uploadFiles(def metaDataMap) {
        File directory = new File(metaDataMap.uploadable_files_dir)
        println "processFolder $directory"
        def files = directory.listFiles()
        files.each { File file ->
            if (!file.isDirectory() && !ignoreList.contains(file.name.toString()) && file.name.endsWith(PDF)) {
                println "****"
                String identifier = generateIdentifier(file.name)
                metaDataMap.file = file.name
                metaDataMap.title = file.name

                metaDataMap.item = identifier
                String keyColonValueString = makeKeyColonValueString(metaDataMap)
                String cmd = "ia upload $identifier \"${file.name}\" --metadata=\"${keyColonValueString}\""
                println cmd
                //println cmd.execute(null, new File(metaDataMap.archive_python_upload_tools_dir)).text
            }
        }
    }

    public static String makeKeyColonValueString(def metaDataMap) {
        String keyColonValueString = ""
        List requiredKeys = ["creator", "file", "mediatype", "texts", "collection", "title", "description", "subject", "language"]
        metaDataMap.each { key, value ->
            if(key in requiredKeys){
                keyColonValueString += "$key:\"$value\"" + " "
            }
        }
        return keyColonValueString
    }

    /**
     * Rules:
     *  An identifier is composed of any unique combination of alphanumeric characters, underscore (_) and dash (-).
     *  While the official limit is 100 characters, it is strongly suggested that they be between 5 and 80 characters in length
     *  Identifiers must be unique across the entirety of Internet Archive, not simply unique within a single collection
     */
    public static String generateIdentifier(String fileName) {
        def filteredString = (fileName - PDF).findAll {
            it =~ /[0-9a-zA-Z_-]/
        }

        String identifier = "eGangotri-" + filteredString.join()
        if (identifier.length() > 80) {
            identifier = identifier.substring(0, 79)
        }

        println identifier
        return identifier
    }



}
