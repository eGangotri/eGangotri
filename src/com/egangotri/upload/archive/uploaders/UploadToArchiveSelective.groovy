package com.egangotri.upload.archive.uploaders

import com.egangotri.upload.archive.ArchiveHandler
import com.egangotri.upload.archive.UploadToArchive
import com.egangotri.upload.util.ArchiveUtil
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.upload.vo.QueuedVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

//Works
@Slf4j
// filename separator is '%'
class UploadToArchiveSelective {
    static void main(String[] args) {
        String UPLOAD_TYPE = "Selective"
        String archiveProfile = ""
        List<String> fileNames = []
        List<String> validation = []
        if (args && args.length >= 2) {
            log.info "args $args"
            archiveProfile = args[0]
            fileNames = args[1].split(UploadersUtil.PERCENT_SIGN_AS_FILE_SEPARATOR)*.trim();
            fileNames.forEach { fileName ->
                {
                    if (!(fileName ==~ /.*\.\w+$/)) {
                        validation << fileName
                    }
                }
            }
            if (validation.size() > 0) {
                log.info("Some Filenames dont have an extension ${validation}")
                return
            }
            if (args.length == 3) {
                EGangotriUtil.UPLOAD_CYCLE_ID = args[2]
            }
            if (args.length == 4) {
                UPLOAD_TYPE = args[3]
            }

        } else {
            log.info "Must have 2/3 arg.s Profile name and fileName(s) of pdf as CSV"
            return
        }

        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        EGangotriUtil.recordProgramStart("eGangotri Archiver-Thru-AbsPaths")
        Set<QueuedVO> vos = ArchiveUtil.generateVOsFromFileNames(archiveProfile, fileNames)
        UploadersUtil.prelimsWithVOs(archiveProfile,vos)
        UploadersUtil.addToUploadCycleWithMode([archiveProfile], "${UPLOAD_TYPE}-(${fileNames.size()}");
        if (vos) {
            log.info("fileNames in args ${fileNames}")
            log.info("vos ${vos}")
            List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(UploadersUtil.metaDataMap, vos)
            String report = UploadUtils.generateStats(uploadStats, archiveProfile, vos.size())
            uploadSuccessCheckingMatrix.put(1, report)
            EGangotriUtil.recordProgramEnd()
            ArchiveUtil.printFinalReport(uploadSuccessCheckingMatrix, vos.size())
        } else {
            log.info("No uploadables found. Exiting.")
        }
        //  System.exit(0)
    }

}
