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
            String srcDir = metaDataMap["${profile}"]
            String[] srcDirArr = srcDir.split("\\\\")
            srcDirArr[1] += "${File.separator}_freeze"
            String destDir = srcDirArr.join(File.separator)

            Integer srcFilesCountBeforeMove = noOfFiles(srcDir)
            Integer destFilesCountBeforeMove = noOfFiles(destDir)
            if(srcFilesCountBeforeMove){
                println("Moving $srcFilesCountBeforeMove files from ${srcDir} to ${destDir}")
                FileUtil.movePdfsInDir(srcDir, destDir)
            }
            Integer srcFilesCountAfterMove = noOfFiles(srcDir)
            Integer destFlesCountAfterMove = noOfFiles(destDir)

            Integer destFolderDiff = Math.subtractExact(destFlesCountAfterMove, destFilesCountBeforeMove)
            String rep = ""
            if(!srcFilesCountBeforeMove){
                rep += "${profile}:\tNothing to Move"
            }
            else {
                rep +="${profile}: \t ${dirStats(srcDir,srcFilesCountBeforeMove,srcFilesCountAfterMove)},\t ${dirStats(destDir,destFilesCountBeforeMove,destFlesCountAfterMove)},\t ${destFolderDiff} \t"
                if(destFolderDiff == 0){
                    rep += "${profile}:\tNothing was moved"
                }
                else{
                    rep += (srcFilesCountBeforeMove-srcFilesCountAfterMove == destFolderDiff ? 'Success' : 'Failure!!!!')
                }

            }
            uploadSuccessCheckingMatrix.put((index++), rep)
        }

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }
    }

    static String dirStats(String dir, int countBefore, int countAfter){
        return "$dir[bef:$countBefore after:$countAfter]"
    }

    static Integer noOfFiles(String dirName) {
        return FileRetrieverUtil.getAllPdfFilesIncludingInIgnoredExtensions(new File(dirName))?.size()
    }
}
