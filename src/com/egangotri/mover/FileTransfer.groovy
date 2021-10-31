package com.egangotri.mover

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Slf4j
class FileTransfer {
    static String SRC_FOLDER = System.getProperty("user.home") + File.separator + "Downloads"

    static void main(String[] args) {
        if (args) {
            log.info "args $args"
            SRC_FOLDER = args[0]
        }
        File doneFolder = new File(SRC_FOLDER)
        log.info("Read Done-Folder ${doneFolder.name} on ${UploadUtils.getFormattedDateString()}")
        File[] validFolders = doneFolder?.listFiles(validFiles())
        log.info("folders ${validFolders}");
        if (validFolders) {
            log.info("FileTransfer started for \n${validFolders*.name.join(",\n")}")
            transferFolderContents(validFolders)
        } else {
            log.info("No Zips")
        }
    }

    static void transferFolderContents(File[] foldersToTransport) {
        String destDir = ""
        foldersToTransport.each { File srcFolder ->
            destDir = Codes.getDestDirByFileName(srcFolder)
            log.info("destDir ${destDir}")
            FileUtil.moveDir(srcFolder.absolutePath, destDir, "",false)
        }
        if(destDir){
            Runtime.getRuntime().exec("explorer.exe /select," + destDir)
        }
    }


    static FileFilter validFiles() {
        FileFilter fileFilter = { File file ->
            log.info("file name ${file.name}")
            return file.isDirectory() && Codes.isValidCode(file.name)
        } as FileFilter
        return fileFilter
    }

}


