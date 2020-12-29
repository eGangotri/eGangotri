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

    static moveDir(String srcDir, String destDir, customInclusionFilter = "", boolean overWriteFlag = false) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $srcDir dst: $destDir customInclusionFilter:${customInclusionFilter} overWriteFlag:${overWriteFlag}")
        try {
            ant.with {
                log.info('started moving')
                // notice nested Ant task
                move(todir: destDir, verbose: 'true', overwrite: overWriteFlag, preservelastmodified: 'true') {
                    fileset(dir: srcDir) {
                        include(name: customInclusionFilter ? customInclusionFilter : "**/*.*")
                    }
                }
                log.info ('done moving')
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Folder ${srcDir}", e)
        }

    }

    static movePdfsInDir(String srcDir, String destDir, boolean overWriteFlag = false) {
        moveDir(srcDir, destDir, "**/*.pdf", overWriteFlag)
    }

    static moveAndUnzip(File zipFile, String destDir) {
        try {
            moveZip(zipFile.absolutePath, destDir)
            unzipFile(zipFile, destDir)
        }
        catch (Exception e) {
            log.error("Error in Moving/UnZip File ${zipFile}", e)
        }
    }

    static moveZip(String zipFile, String destDir) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $zipFile " + "dst: $destDir")
        try {
            ant.with {
                log.info ('Started moving')
                move(file: zipFile, todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true')
                log.info('Done moving')
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Zip File ${zipFile}", e)
            throw e
        }
    }
    
    static unzipFile(File zipFile, String destDir) {
        String zipFileName = zipFile.name
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $zipFileName " + "dst: $destDir")

        try {
            ant.with {
                log.info ('Start unzipping')
                unzip(src: destDir + File.separator + zipFileName, dest: destDir, overwrite: 'false')
                log.info('Done unzipping')
            }
        }
        catch (Exception e) {
            log.error("Error in unzipping Zip File ${zipFileName}", e)
            throw e
        }
        String newFolderAfterZip = zipFileName.replaceAll("\\.zip|\\.rar", "")

        File[] unzippedFolders = new File(destDir).listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                log.info("dir: $dir $name")
                File f = new File(dir, name)
                log.info("is Dir ${f.isDirectory()} name starts with  ${name.startsWith(newFolderAfterZip.substring(0,3))}}")
                return ( f.isDirectory()  && name.startsWith(newFolderAfterZip.substring(0,3)))
            }
        })

        log.info("unzippedFolder: ${unzippedFolders}")
        unzippedFolders.each {File unzippedFolder ->
            try {
                ant.with {
                    log.info('Start moving zipped files to main folder')
                    move(todir: destDir, overwrite: 'false') {
                        fileset(dir: unzippedFolder.absolutePath) {
                            include(name: "**/*.pdf")
                        }
                    }
                    echo "Done moving zipped files to main folder for ${unzippedFolder}"
                }
                log.info("Deleting unzipped Folder: ${unzippedFolders}")
                unzippedFolder.delete();
            }
            catch (Exception e) {
                log.error("Error moving zipped files to ${destDir}", e)
                throw e
            }

        }
        log.info("Deleting Original Zip File: ${zipFile}" + (zipFile.delete()? '': 'Un') + "Successful")

    }
}

