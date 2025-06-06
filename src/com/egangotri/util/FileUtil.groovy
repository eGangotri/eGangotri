package com.egangotri.util

import com.egangotri.mover.ZipMover
import com.egangotri.upload.util.UploadUtils
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import javax.swing.JOptionPane
import java.util.zip.ZipFile

@Slf4j
class FileUtil {

    static String SRC_ROOT = "SRC_ROOT"
    static String DEST_ROOT = "DEST_ROOT"
    static String DEST_OTRO_ROOT = "DEST_OTRO_ROOT"

    static private Map<String, String> getFoldersCorrespondingToProfile(String root) {
        Map<String, String> profileAndFolder = UploadUtils.readPropsFromListOfPropFiles(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILES)
        String rootPath = profileAndFolder.get(root)

        profileAndFolder.each { Map.Entry<String, String> entry ->
            if (!entry.key.contains('.') && entry.key != SRC_ROOT && entry.key != DEST_ROOT && entry.key != DEST_OTRO_ROOT) {
                String path = profileAndFolder.get(entry.key)?.toString()?.trim()
                if (path) {
                    profileAndFolder[entry.key] = path.contains(':') || path.startsWith(File.separator) ?
                            path :
                            new File(rootPath, path).path
                }
            }
        }

        return profileAndFolder
    }
    static private Map<String, String> getFoldersCorrespondingToProfile2(String root) {
        Properties properties = new Properties()
        for(String propertiesFileName : EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILES) {
            File propertiesFile = new File(propertiesFileName)
            if (propertiesFile.exists()) {
                log.info("Loading Local Folder Properties from ${propertiesFileName}")
                propertiesFile.withInputStream {
                    properties.load(it)
                }
            } else {
                log.info("No Local Folder Properties file found at ${propertiesFileName}")
            }
        }

        Map<String, String> profileAndFolder = [:]
        String rootPath = properties.getProperty(root)
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement()
            String path = new String(properties.get(key).toString().getBytes("ISO-8859-1"), "UTF-8")?.trim()
            if (!key.contains(".") && key != SRC_ROOT && key != DEST_ROOT && key != DEST_OTRO_ROOT) {
                //If the path provided is a Full path such as
                // C:// in Windows or /home/dir then don't make it relative to the SRC_ROOT
                if(path.contains(":") || path.startsWith(File.separator)){
                    profileAndFolder.put(key, path)
                }
                else {
                    String fullPath = "${rootPath}${File.separator}" + path
                    profileAndFolder.put(key, fullPath)
                }
            }
        }
        return profileAndFolder
    }


    static Map<String, String> getSrcFoldersCorrespondingToProfile() {
        return getFoldersCorrespondingToProfile(SRC_ROOT)
    }

    static Map<String, String> getDestFoldersCorrespondingToProfile() {
        return getFoldersCorrespondingToProfile(DEST_ROOT)
    }

    static final Map<String, String> ALL_FOLDERS = getSrcFoldersCorrespondingToProfile()

    static String ALLOWED_EXTENSIONS_REGEX = /.*/
    static String PDF_ONLY_REGEX = /.*\.pdf/

   static FileFilter pdfFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.name.endsWith(".pdf")
        }
    };

    static moveDir(String srcDir, String destDir, customInclusionFilter = "", List excludeList = [], boolean overWriteFlag = false) {
        // create an ant-builder
        def ant = new groovy.ant.AntBuilder()
        log.info("Src $srcDir dst: \n$destDir " +
                "\ncustomInclusionFilter:${customInclusionFilter}" +
                " \noverWriteFlag:${overWriteFlag}")
        try {
            def pdfs = new File(srcDir).list({ d, f -> f ==~ /.*.pdf/ } as FilenameFilter)
            log.info("Started moving (${pdfs?.length}) pdf(s) \n\t${pdfs.join(",\n\t")} ")
            List duplicates = duplicateFileNamesInSrcAndDestV2(srcDir, destDir)
            if (overWriteFlag || !duplicates) {
                if (duplicates) {
                    if (!handleDuplicationUserChoice(duplicates)) {
                        return;
                    }
                }
                ant.with {
                    // notice nested Ant task
                    move(todir: destDir, verbose: 'true', overwrite: overWriteFlag, preservelastmodified: 'true') {
                        fileset(dir: srcDir) {
                            include(name: customInclusionFilter ? customInclusionFilter : "**/*.*")
                            excludeList.each { excludeName ->
                                println("excludeName ${excludeName}")
                                exclude(name: "**/${excludeName}")
                            }

                        }
                    }
                    log.info("Done moving")
                }
            } else {
                log.info("Found overlapping ${duplicates.size()} files in both Source and Dest." +
                        "\n\t${duplicates.join(",\n\t")} \nWill not move anything")
            }
        }
        catch (Exception e) {
            log.error("Error in Moving Folder ${srcDir}", e)
        }
    }

    static boolean handleDuplicationUserChoice(List duplicates) {
        String seekPermissionText = "Found overlapping ${duplicates.size()} files in both Source and Dest. Enter 'Y' to continue"
        Closure<Object> readln = JOptionPane.&showInputDialog
        String seekPermissionToContinue = readln(seekPermissionText)
        println "Your response was $seekPermissionToContinue."
        if (!seekPermissionToContinue.toString().equalsIgnoreCase("Y")) {
            println "Shall return without moving"
            return false
        } else {
            println "Shall move."
        }
        return true
    }

    static List duplicateFileNamesInSrcAndDest(String srcDir, String destDir) {
        File[] srcPdfs = new File(srcDir)?.listFiles({ d, f -> f ==~ /.*.pdf/ } as FilenameFilter)
        File[] destPdfs = new File(destDir)?.listFiles({ d, f -> f ==~ /.*.pdf/ } as FilenameFilter)
        List duplicates = (srcPdfs && destPdfs) ? srcPdfs*.getName().intersect((destPdfs*.getName())) : []
        log.info("Found overlapping ${duplicates.size()} files in both Source and Dest." +
                "\n\t${duplicates.join(",\n\t")} \n")
        return duplicates
    }

    static List duplicateFileNamesInSrcAndDestV2(String srcDir, String destDir) {
        def srcPdfs = new File(srcDir).listFiles().collect { it.name.toLowerCase() }
        def destPdfs = new File(destDir).listFiles().collect { it.name.toLowerCase() }

        List duplicates = srcPdfs.intersect(destPdfs)

        duplicates.each {
            println it
        }
        log.info("Found overlapping ${duplicates.size()} files in both Source and Dest." +
                "\n\t${duplicates.join(",\n\t")} \n")
        return duplicates
    }
    static movePdfsInDir(String srcDir, String destDir, List excludeList = [], boolean overWriteFlag = false) {
        moveDir(srcDir, destDir, "**/*.pdf", excludeList, overWriteFlag)
    }

    static moveAndUnzip(File srcZipFile, String destDir) {
        try {
            moveZip(srcZipFile.absolutePath, destDir)
            if (!ZipMover.ONLY_MOVE_DONT_UNZIP) {
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
            if ((deletableFile.isDirectory() && deletableFile.list().size() == 0)) {
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

    static void main(String[] args) {
        Map<String, String> srcMetaDataMap = getSrcFoldersCorrespondingToProfile();
        Map<String, String> destMetaDataMap = getDestFoldersCorrespondingToProfile();
        log.info("" + srcMetaDataMap)
        log.info("" + destMetaDataMap)
    }
}





