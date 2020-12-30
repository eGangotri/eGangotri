package com.egangotri.util


import groovy.util.logging.Slf4j
import org.apache.commons.io.FileDeleteStrategy

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
                log.info('done moving')
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Folder ${srcDir}", e)
        }

    }

    static movePdfsInDir(String srcDir, String destDir, boolean overWriteFlag = false) {
        moveDir(srcDir, destDir, "**/*.pdf", overWriteFlag)
    }

    static moveAndUnzip(File srcZipFile, String destDir) {
        try {
            moveZip(srcZipFile.absolutePath, destDir)
            unzipFile(destDir,srcZipFile.name)
        }
        catch (Exception e) {
            log.error("Error in Moving/UnZip File ${srcZipFile}", e)
        }
    }

    static moveZip(String srcZipFile, String destDir) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $srcZipFile " + "dst: $destDir")
        try {
            ant.with {
                log.info('Started moving')
                move(file: srcZipFile, todir: destDir, verbose: 'true', overwrite: 'false', preservelastmodified: 'true')
                log.info('Done moving')
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Zip File ${srcZipFile}", e)
            throw e
        }
    }

    static unzipFile(String destDir, String zipFileName) {
        unzipFile(new File(destDir,zipFileName))
    }

    static unzipFile(File zipFile) {
        log.info("unzipping  ${zipFile.getAbsolutePath()}")
        Set<File> deletables = [] as Set

        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        try {
            ant.with {
                log.info('Start unzipping')
                unzip(src: zipFile.getAbsolutePath(), dest: zipFile.parent, overwrite: 'false')
                log.info('Done unzipping')
            }
        }
        catch (Exception e) {
            log.error("Error in unzipping Zip File ${zipFile}", e)
            throw e
        }
        File[] unzippedFolders = getUnzippedFolderName(zipFile)

        log.info("unzippedFolder: ${unzippedFolders}")
        unzippedFolders.each { File unzippedFolder ->
            try {
                ant.with {
                    log.info('Start moving zipped files to main folder')
                    move(todir: zipFile.parent, overwrite: 'false') {
                        fileset(dir: unzippedFolder.absolutePath) {
                            include(name: "**/*.pdf")
                        }
                    }
                    echo "Done moving zipped files to main folder for ${unzippedFolder}"
                }
                deletables << unzippedFolder
            }
            catch (Exception e) {
                log.error("Error moving zipped files to ${zipFile.parent}", e)
                throw e
            }
        }
        deletables << zipFile
        deletables.each { deletableFile ->
            log.info("Deleting File: ${deletableFile.name} (${deletableFile.delete() ? '': 'Un'}Successful)")
        }
    }

    static File[] getUnzippedFolderName(File zipFile) {
        return zipFile.parentFile.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                File f = new File(dir, name)
                return (f.isDirectory() && name.startsWith(zipFile.name.substring(0, 3)))
            }
        })
    }
}


