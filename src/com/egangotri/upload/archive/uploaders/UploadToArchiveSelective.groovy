package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

//Works
@Slf4j
class UploadToArchiveSelective {
    static void main(String[] args) {
        String archiveProfile = ""
        String fileName = ""
        if (args && args.length >= 2/* && args.length % 2*/) {
            log.info "args $args"
            archiveProfile = args[0]
            fileName = args[1].endsWith(EGangotriUtil.PDF) ? args[1] : args[1] + EGangotriUtil.PDF;
        } else {
            log.info "Must have 2 arg.s Profile name and fileName of pdf"
            System.exit(0)
        }
        UploadToArchive.prelims(args)
        String localPath = getLocalPath(archiveProfile, fileName)
        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromFileNames(args[0], [localPath])
        if(localPath){
            log.info("localPath ${localPath}")
            log.info("vos ${vos}")
            List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(UploadToArchive.metaDataMap, vos as Set<QueuedVO>)
        }
        else {
            log.info("File ${fileName} not found")
        }
        System.exit(0)
    }

    static String getLocalPath(String archiveProfile, String fileName) {
        String _folder = FileRetrieverUtil.pickFolderBasedOnArchiveProfile(archiveProfile)
        String filePath = ""
        File folder = new File(_folder)
        if (folder.exists() && folder.isDirectory()) {
            // Create a File object for the file inside the folder
            def file = new File(folder, fileName)
            // Check if the file exists
            if (file.exists() && file.isFile()) {
                // Get the absolute path of the file
                filePath = file.getCanonicalPath()
                println "Absolute path of $fileName: $filePath"
            } else {
                println "File $fileName ${file.absolutePath} does not exist in the folder."
            }
        } else {
            println "Folder $folder does not exist."
        }
        return filePath;
    }
}
