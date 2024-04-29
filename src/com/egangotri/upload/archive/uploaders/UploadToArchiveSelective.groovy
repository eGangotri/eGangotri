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
        List<String> fileNames = []
        if (args && args.length >= 2) {
            log.info "args $args"
            archiveProfile = args[0]
            fileNames.addAll(args[1].split(","))
        } else {
            log.info "Must have 2 arg.s Profile name and fileName of pdf"
            System.exit(0)
        }

        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        EGangotriUtil.recordProgramStart("eGangotri Archiver-Thru-AbsPaths")
        UploadToArchive.prelims(args)
        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromFileNames(archiveProfile, fileNames)
        if(vos){
            log.info("localPath ${fileNames}")
            log.info("vos ${vos}")
            List<Integer> uploadStats = ArchiveHandler.uploadAllItemsToArchiveByProfile(UploadToArchive.metaDataMap, vos as Set<QueuedVO>)
            uploadSuccessCheckingMatrix.put(1, uploadStats)


            EGangotriUtil.recordProgramEnd()
            ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, vos.size())
        }
        else {
            log.info("No uploadables found. Exiting.")
        }
      //  System.exit(0)
    }

}
