package com.egangotri.util

import com.egangotri.mover.ZipMover
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileDeleteStrategy
import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

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
        log.info("Src $srcDir dst: \n$destDir " +
                "\ncustomInclusionFilter:${customInclusionFilter}" +
                " \noverWriteFlag:${overWriteFlag}")
        try {
            def pdfs = new File(srcDir).list({d, f-> f ==~ /.*.pdf/ } as FilenameFilter)
            log.info("Started moving \n\t${pdfs.join(",\n\t")}")
            List duplicates = duplicateFileNamesInSrcAndDest(srcDir,destDir)
            if(overWriteFlag || !duplicates){
                ant.with {
                    // notice nested Ant task
                    move(todir: destDir, verbose: 'true', overwrite: overWriteFlag, preservelastmodified: 'true') {
                        fileset(dir: srcDir) {
                            include(name: customInclusionFilter ? customInclusionFilter : "**/*.*")
                        }
                    }
                    log.info("done moving")
                }
            }
            else{
             log.info("Found overlapping ${duplicates.size()} files in both Source and Dest." +
                     "\n\t${duplicates.join(",\n\t")} \nWill not move anything")
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Folder ${srcDir}", e)
        }

    }

    static List duplicateFileNamesInSrcAndDest(String srcDir, String destDir){
        File[] srcPdfs = new File(srcDir).listFiles({d, f-> f ==~ /.*.pdf/ } as FilenameFilter)
        File[] destPdfs = new File(destDir).listFiles({d, f-> f ==~ /.*.pdf/ } as FilenameFilter)
        return (srcPdfs && destPdfs) ? srcPdfs*.getName().intersect((destPdfs*.getName())): []
    }

    static movePdfsInDir(String srcDir, String destDir, boolean overWriteFlag = false) {
        moveDir(srcDir, destDir, "**/*.pdf", overWriteFlag)
    }

    static moveAndUnzip(File srcZipFile, String destDir) {
        try {
            moveZip(srcZipFile.absolutePath, destDir)
            if(!ZipMover.ONLY_MOVE_DONT_UNZIP){
                unzipFile(destDir, srcZipFile.name)
            }
        }
        catch (Exception e) {
            log.error("Error in Moving/UnZip File ${srcZipFile}", e)
        }
    }

    static moveZip(String srcZipFile, String destDir) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Moving:\nSrc $srcZipFile to " + "\nDestDir: $destDir")
        try {
            FileUtils.moveFileToDirectory(new File(srcZipFile), new File(destDir), true)
        }
        catch (Exception e) {
            log.error("Error in Moving Zip File ${srcZipFile}", e)
            throw e
        }
    }

    static unzipFile(String destDir, String zipFileName) {
        unzipFile(new File(destDir, zipFileName))
    }

    static unzipFile(File zipFile) {
        log.info("Unzipping  ${zipFile.getAbsolutePath()} having ${countEntriesInZipFile(zipFile)} entries")
        Set<File> deletables = [] as Set

        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        try {
            ant.with {
                log.info("Start unzipping " + zipFile.name)
                unzip(src: zipFile.getAbsolutePath(), dest: zipFile.parent, overwrite: 'false')
                log.info("Done unzipping")
            }
        }
        catch (Exception e) {
            log.error("Error in unzipping Zip File ${zipFile}", e)
            throw e
        }
        File[] unzippedFolders = getUnzippedFolderName(zipFile)
        log.info("Moving ${unzippedFolders}")

        unzippedFolders.each { File unzippedFolder ->
            log.info("Moving ${unzippedFolder.list().size()} pdfs in ${unzippedFolder.name} to ${zipFile.parent}")
            listPdfs(unzippedFolder).each { _pdf ->
                {
                    ZipMover.ALL_ZIP_FILES_PROCESSED << _pdf.name
                    try {
                        FileUtils.moveFileToDirectory(_pdf, new File(zipFile.parent), false)
                    }
                    catch (Exception e) {
                        log.error("Error moving zipped file (${_pdf.name})to ${zipFile.parent}", e)
                    }
                }
            }
            deletables << unzippedFolder

        }
        deletables << zipFile
        deletables.each { deletableFile ->
            if ((deletableFile.isDirectory() && deletableFile.list().size() === 0)) {
                log.info("Deleting File: ${deletableFile.absolutePath} (${deletableFile.delete() ? '' : 'Un'}Successful)")
            }
            //File.delete() doesnt work so have to use this
            else if (deletableFile.name.endsWith(".zip")) {
                log.info("Deleting File: ${deletableFile.absolutePath}")
                ant.delete(file: "${deletableFile.getAbsolutePath()}")
            } else {
                log.info("${deletableFile.name} not deleted as it is a Non-Empty Directory")
            }
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

    static File[] listPdfs(File folder) {
        return folder.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                return name.endsWith(".pdf")
            }
        })
    }

    static int countEntriesInZipFile(File zipFile) {
        def zip = new ZipFile(zipFile)
        return zip.size()
    }
}


