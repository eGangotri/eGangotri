package com.egangotri.mover

import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class FileMover {
    static Map<String, List<String>> srcDestMap
    static List profiles = []
    static List<Integer> totalFilesMoved = []
    static List<Integer> preMoveCount = []
    static List<String> successes = []
    static void main(String [] args) {
        if (args) {
            log.info "args $args"
            profiles = args.toList()
        }
        new FileMover().move()
    }

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

            if(srcDirArr){
                srcDirArr[1] += "${File.separator}_freeze"
                String destDir = srcDirArr.join(File.separator)

                srcFilesCountBeforeMove = noOfFiles(srcDir)
                preMoveCount.push(srcFilesCountBeforeMove)
                destFilesCountBeforeMove = noOfFiles(destDir)
                if(srcFilesCountBeforeMove){
                    println("Moving $srcFilesCountBeforeMove files from ${srcDir} to ${destDir}")
                    FileUtil.movePdfsInDir(srcDir, destDir)
                }
                srcFilesCountAfterMove = noOfFiles(srcDir)
                destFlesCountAfterMove = noOfFiles(destDir)

                destFolderDiff = Math.subtractExact(destFlesCountAfterMove, destFilesCountBeforeMove)
                totalFilesMoved.add(destFolderDiff)
                if(!srcFilesCountBeforeMove){
                    report += "${profile}:\tNothing to Move"
                }
                else {
                    report +="${profile}: \t ${dirStats(srcDir,srcFilesCountBeforeMove,srcFilesCountAfterMove)},\t ${dirStats(destDir,destFilesCountBeforeMove,destFlesCountAfterMove)},\t Moved ${destFolderDiff} files\t"
                    if(destFolderDiff == 0){
                        report += "${profile}:\tNothing was moved"
                    }
                    else{
                        String success = (srcFilesCountBeforeMove-srcFilesCountAfterMove == destFolderDiff ? 'Success' : 'Failure!!!!')
                        successes.add(success)
                        report += success
                    }

                }
            }
            else {
                report += "${profile}:\tNo Such Profile"
            }
            uploadSuccessCheckingMatrix.put((index++), report)
        }

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }
        if(totalFilesMoved){
            log.info "Total Files \n" +
                     "(of ${preMoveCount.reverse().join("+")} =${preMoveCount.sum()}) moved \n" +
                    "${totalFilesMoved.join("+")}=${totalFilesMoved.sum()}" + " [ ${successes.join("+")} (${successes.size()}) ]"
        }
    }

    static String dirStats(String dir, int countBefore, int countAfter){
        return "$dir[bef:$countBefore after:$countAfter]"
    }

    static Integer noOfFiles(String dirName) {
        return FileRetrieverUtil.getAllPdfFilesIncludingInIgnoredExtensions(new File(dirName))?.size()
    }
}
