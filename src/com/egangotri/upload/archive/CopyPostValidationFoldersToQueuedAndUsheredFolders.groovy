package com.egangotri.upload.archive

import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.nio.file.Files
@Slf4j
class CopyPostValidationFoldersToQueuedAndUsheredFolders {

    static main(args) {
        log.info("Getting latest Files from ${EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER}")
        def latestTwoFiles = new File(EGangotriUtil.ARCHIVE_ITEMS_POST_VALIDATIONS_FOLDER).listFiles()?.sort { -it.lastModified() }?.take(2)

        if(latestTwoFiles.first().name.contains("queued")){
            copyToQueued(latestTwoFiles.first())
            copyToUshered(latestTwoFiles.last())
        }
        else {
            copyToQueued(latestTwoFiles.last())
            copyToUshered(latestTwoFiles.first())
        }
        ValidateUploadsAndReUploadFailedItems.main(new String[0])
        System.exit(0)
    }
    
    def static copy(File src, File destFileName){
        log.info("copying \n\t${src.name} \n\tto \n\t${destFileName.parent} ")
        Files.copy(src.toPath(), destFileName.toPath())
    }

    def static copyToUshered(File src){
        copy(src,new File(EGangotriUtil.ARCHIVE_ITEMS_USHERED_FOLDER
                + File.separator + src.name))
    }
    def static copyToQueued(File src){
        copy(src,new File(EGangotriUtil.ARCHIVE_ITEMS_QUEUED_FOLDER
                + File.separator + src.name))
    }
}
