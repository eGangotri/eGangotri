package com.egangotri.mover

import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.BooleanUtils

@Slf4j
class FileMover {
    static Map<String, List<String>> srcDestMap
    static List profiles = []
    static List<Integer> totalFilesMoved = []
    static List<Integer> preMoveCountSrc = []
    static List<Integer> preMoveCountDest = []
    static List<Integer> postMoveCountSrc = []
    static List<Integer> postMoveCountDest = []
    static List<String> successStatuses = []
    static Boolean OVERWRITE_FLAG = false

    static void main(String[] args) {
        if (args) {
            log.info "args $args"
            profiles = args.toList()
            if (profiles.last().equalsIgnoreCase("false") || profiles.last().equalsIgnoreCase("true")) {
                OVERWRITE_FLAG = BooleanUtils.toBoolean(profiles.last().toLowerCase())
                profiles.remove(profiles.last())
            }

        }
        log.info("FileMover started for ${profiles.size()} Profiles on ${UploadUtils.getFormattedDateString()}")
        log.info "profiles $profiles overWriteFlag $OVERWRITE_FLAG"
        new FileMover().move()
    }

    static String SUCCESS_STRING = 'Success'
    static String POSSIBLE_OVERWRITE_ISSUES_STRING = 'Possible OverWrite Issues'
    static String FAILURE_STRING = 'Failure!!!!'
    static String NOTHING_TO_MOVE = 'Nothing To Move'

    void move() {
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.LOCAL_FOLDERS_PROPERTIES_FILE)
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]

        int index = 1
        profiles.each { profile ->
            String report = ""
            String srcDir = metaDataMap["${profile}"]
            String[] srcDirArr = srcDir?.split("\\\\")
            Integer srcFilesCountBeforeMove = 0
            Integer destFilesCountBeforeMove = 0
            Integer srcFilesCountAfterMove = 0
            Integer destFlesCountAfterMove = 0
            Integer destFolderDiff = 0

            if (srcDirArr) {
                srcDirArr[1] += "${File.separator}_freeze"
                String destDir = srcDirArr.join(File.separator)

                srcFilesCountBeforeMove = noOfFiles(srcDir)
                preMoveCountSrc.push(srcFilesCountBeforeMove)
                destFilesCountBeforeMove = noOfFiles(destDir)
                preMoveCountDest.push(destFilesCountBeforeMove)
                if (srcFilesCountBeforeMove) {
                    log.info("Moving $srcFilesCountBeforeMove files from \n${srcDir} to \n${destDir}")
                    FileUtil.movePdfsInDir(srcDir, destDir, OVERWRITE_FLAG)
                }
                srcFilesCountAfterMove = noOfFiles(srcDir)
                postMoveCountSrc.push(srcFilesCountAfterMove)

                destFlesCountAfterMove = noOfFiles(destDir)
                postMoveCountDest.push(destFlesCountAfterMove)

                destFolderDiff = Math.subtractExact(destFlesCountAfterMove, destFilesCountBeforeMove)
                totalFilesMoved.add(destFolderDiff)
                if (srcFilesCountBeforeMove) {
                    report += """${profile}: 
                                     ${dirStats(srcDir, srcFilesCountBeforeMove, srcFilesCountAfterMove)},
                                     ${dirStats(destDir, destFilesCountBeforeMove, destFlesCountAfterMove)}
                                     Moved ${destFolderDiff} files.\n"""
                }
                String success = getSuccessString(srcFilesCountBeforeMove, srcFilesCountAfterMove, destFolderDiff)
                successStatuses.add(success)
                report += "${profile}:\n${success}\n"

            } else {
                report += "${profile}:\tNo Such Profile"
            }
            uploadSuccessCheckingMatrix.put((index++), report)
        }

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }
        if (totalFilesMoved) {
            log.info """Total Files:
                      PreMove Src  ${statsRow(preMoveCountSrc)}    PreMove Dest ${statsRow(preMoveCountDest)}
                      PostMove Src ${statsRow(postMoveCountSrc)}   PostMove Dest ${statsRow(postMoveCountDest)} 

                      Total Files Moved [${totalFilesMoved.join("+")}=${totalFilesMoved.sum()}] 
                      [ ${successStatuses.join("+")} ]
                      [ ${successCount(SUCCESS_STRING)}(S)+${successCount(FAILURE_STRING)}(F)+
                        ${successCount(POSSIBLE_OVERWRITE_ISSUES_STRING)}(OverWriteError)+${successCount(NOTHING_TO_MOVE)}(N2M)]=(${successStatuses.size()}) ]"""
        }
    }

    static String statsRow(List moveCountList) {
        return "[${moveCountList.reverse().join("+")}=${moveCountList.sum()}]"

    }

    static String successCount(String statusString) {
        return successStatuses.count { it == statusString }
    }

    static String dirStats(String dir, int countBefore, int countAfter) {
        return "$dir[bef:$countBefore after:$countAfter]"
    }

    static Integer noOfFiles(String dirName) {
        return FileRetrieverUtil.getAllPdfFilesIncludingInIgnoredExtensions(new File(dirName))?.size()
    }

    static String getSuccessString(int srcFilesCountBeforeMove, int srcFilesCountAfterMove, int destFolderDiff) {
        if (!srcFilesCountBeforeMove) {
            return NOTHING_TO_MOVE
        }
        else {
            boolean grossMovementDiff = (srcFilesCountBeforeMove - srcFilesCountAfterMove) == destFolderDiff
            log.info("${grossMovementDiff} = (${srcFilesCountBeforeMove} - $srcFilesCountAfterMove) == ${destFolderDiff}\n")
            String success = grossMovementDiff ? SUCCESS_STRING : FAILURE_STRING
            if (success === SUCCESS_STRING && srcFilesCountAfterMove != 0) {
                success = POSSIBLE_OVERWRITE_ISSUES_STRING
            }
            return success;
        }
    }
}
