package com.egangotri.upload.archive

import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.ValidateUtil
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.nio.file.Files

@Slf4j
class CopyPostValidationFoldersToQueuedAndUsheredFolders {

    static void main(String[] args) {
        execute(args)
        System.exit(0)
    }

    def static execute(String[] args){
        log.info("Getting latest Files from ${EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER}")
        copyToUshered(ValidateUtil.getLastModifiedFile(EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER,"ushered"))
        copyToAllUplodables(ValidateUtil.getLastModifiedFile(EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER,"all"))

        ArchiveUtil.createValidationAllUplodableFiles()
        ArchiveUtil.createValidationUsheredFiles()
        ValidateUploadsAndReUploadFailedItems.execute(new String[0], "ValidateUsingPostValidationFolder")
    }

    def static copy(File src, String folderName) {
        File destFileName = new File(folderName + File.separator + src.name)
        log.info("copying \n\t${src.name} \n\tto \n\t${destFileName.parent} ")
        if(!destFileName.exists())
        {
            Files.copy(src.toPath(), destFileName.toPath())
        }
        else{
            log.info("File already exists: \t${destFileName.parent}.Skipping... ")
        }
    }

    def static copyToUshered(File src) {
        copy(src, EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER)
    }

    def static copyToAllUplodables(File src) {
        copy(src, EGangotriUtil.ARCHIVE_ITEMS_ALL_UPLOADABLES_FOLDER)
    }
}
