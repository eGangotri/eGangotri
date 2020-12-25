package com.egangotri.util


import groovy.util.logging.Slf4j

@Slf4j
class FileUtil {

    static Map<String, String> getFoldersCorrespondingToProfile() {
        Properties properties = new Properties()
        File propertiesFile = new File(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        propertiesFile.withInputStream {
            properties.load(it)
        }

        Map<String, String> profileAndFolder = [:]

        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement()
            String val = new String(properties.get(key).toString().getBytes("ISO-8859-1"), "UTF-8")?.trim()
            if (!key.contains(".")) {
                profileAndFolder.put(key, val)
            }
        }
        return profileAndFolder
    }

    static final Map<String, String> ALL_FOLDERS = getFoldersCorrespondingToProfile()

    static String ALLOWED_EXTENSIONS_REGEX = /.*/
    static String PDF_ONLY_REGEX = /.*\.pdf/

    static moveDir(String srcDir, String destDir, customInclusionFilter = "") {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $srcDir " + "dst: $destDir")
        try {
            ant.with {
                echo 'started moving'
                // notice nested Ant task
                move(todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true') {
                    fileset(dir: srcDir) {
                        include(name: customInclusionFilter ? customInclusionFilter : "**/*.*")
                    }
                }
                echo 'done moving'
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Folder ${srcDir}", e)
        }

    }

    static movePdfsInDir(String srcDir, String destDir) {
        moveDir(srcDir, destDir, "**/*.pdf")
    }

    static moveZip(String zipFile, String destDir) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $zipFile " + "dst: $destDir")
        try {
            ant.with {
                echo 'Started moving'
                move(file: zipFile, todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true')
                echo 'Done moving'
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Zip File ${zipFile}", e)
            throw e
        }
    }


    static moveAndUnzip(File zipFile, String destDir) {
        try {
            moveZip(zipFile.absolutePath, destDir)
            unzipFile(zipFile.name, destDir)
        }
        catch (Exception e) {
            log.error("Error in Moving/UnZip File ${zipFile}", e)
        }
    }

    static unzipFile(String zipFileName, String destDir) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $zipFileName " + "dst: $destDir")
        try {
            ant.with {
                echo 'Start unzipping'
                unzip(src: destDir + File.separator + zipFileName, dest: destDir, overwrite: 'false')
                echo 'Done unzipping'
            }
        }
        catch (Exception e) {
            log.error("Error in unzipping Zip File ${zipFileName}", e)
            throw e
        }
    }
}

