package com.egangotri.mover


import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j

@Slf4j
class FileMover {
    static Map<String, List<String>> srcDestMap
    static List profiles = []

    static main(args) {
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
            String srcDir = metaDataMap["${profile}.src"]
            String destDir = metaDataMap["${profile}.dest"]

            Integer srcFilesCount = noOfFiles(srcDir)
            Integer dirFlesCountBeforeMove = noOfFiles(destDir)
            FileUtil.moveDir(srcDir, destDir)
            Integer dirFlesCountAfterMove = noOfFiles(destDir)

            Integer diff = Math.subtractExact(dirFlesCountAfterMove, dirFlesCountBeforeMove)
            String rep = "$srcDir"
            if(!srcFilesCount){
                rep += ":\tNothing to Move"
            }
            else {
                rep +=", \t $srcFilesCount,\t $destDir[bef:$dirFlesCountBeforeMove after:$dirFlesCountAfterMove],\t ${diff} \t"
                rep += (srcFilesCount == diff ? 'Success' : 'Failure!!!!')
            }


            uploadSuccessCheckingMatrix.put((index++), rep)
        }

        uploadSuccessCheckingMatrix.each { k, v ->
            log.info "$k) $v"
        }
    }

    static Integer noOfFiles(String dirName) {
        return UploadUtils.getAllPdfs(new File(dirName))?.size()
    }
}
