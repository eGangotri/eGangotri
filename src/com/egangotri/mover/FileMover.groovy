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
    static List<String> successes = []
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
    static String FAILURE_STRING = 'Failure!!!!'

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
                    log.info("Moving $srcFilesCountBeforeMove files from ${srcDir} to ${destDir}")
                    FileUtil.movePdfsInDir(srcDir, destDir, OVERWRITE_FLAG)
                }
                srcFilesCountAfterMove = noOfFiles(srcDir)
                postMoveCountSrc.push(srcFilesCountAfterMove)

                destFlesCountAfterMove = noOfFiles(destDir)
                postMoveCountDest.push(destFlesCountAfterMove)

                destFolderDiff = Math.subtractExact(destFlesCountAfterMove, destFilesCountBeforeMove)
                totalFilesMoved.add(destFolderDiff)
                if (!srcFilesCountBeforeMove) {
                    report += "${profile}:\tNothing to Move"
                } else {
                    report += """${profile}: 
                                     ${dirStats(srcDir, srcFilesCountBeforeMove, srcFilesCountAfterMove)},
                                     ${dirStats(destDir, destFilesCountBeforeMove, destFlesCountAfterMove)}
                                     Moved ${destFolderDiff} files.\n"""
                    if (destFolderDiff == 0) {
                        report += "${profile}:\nNothing was moved"
                    } else {
                        String success = (srcFilesCountBeforeMove - srcFilesCountAfterMove == destFolderDiff ? SUCCESS_STRING : FAILURE_STRING)
                        successes.add(success)
                        report += success
                    }

                }
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
                      PreMove Src  [${preMoveCountSrc.reverse().join("+")}=${preMoveCountSrc.sum()}]  
                      PreMove Dest  [${preMoveCountDest.reverse().join("+")}=${preMoveCountDest.sum()}]  
                      PostMove Src  [${postMoveCountSrc.reverse().join("+")}=${postMoveCountSrc.sum()}]  
                      PostMove Src  [${postMoveCountDest.reverse().join("+")}=${postMoveCountDest.sum()}]  

                      total Files Moved [${totalFilesMoved.join("+")}=${totalFilesMoved.sum()}] 
                      [ ${successes.join("+")} ]
                      [${successCount(successes, true)}(S)+${successCount(successes, false)}(F)]=(${successes.size()}) ]"""
        }
    }

    static String successCount(List successes, boolean forSuccess = true) {
        return successes.count { forSuccess ? it == SUCCESS_STRING : it != SUCCESS_STRING }
    }

    static String dirStats(String dir, int countBefore, int countAfter) {
        return "$dir[bef:$countBefore after:$countAfter]"
    }

    static Integer noOfFiles(String dirName) {
        return FileRetrieverUtil.getAllPdfFilesIncludingInIgnoredExtensions(new File(dirName))?.size()
    }
}
