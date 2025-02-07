package com.egangotri.mover

import com.egangotri.batch.SnapToHtml
import com.egangotri.upload.util.FileRetrieverUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import com.egangotri.util.FileSizeUtil
import com.egangotri.util.FileUtil
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.BooleanUtils

import java.text.SimpleDateFormat

@Slf4j
class FileMover {
    static String SUCCESS_STRING = 'Success'
    static String POSSIBLE_OVERWRITE_ISSUES_STRING = 'Possible OverWrite Issues'
    static String FAILURE_STRING = 'Failure!!!!'
    static String NOTHING_TO_MOVE = 'Nothing To Move'
    static String FILE_IN_USE = 'File(s) in Use. Cannot Move'

    static Map<String, List<String>> srcDestMap
    static List<String> profiles = []
    static List<Integer> totalFilesMoved = []
    static List<Integer> preMoveCountSrc = []
    static List<Integer> preMoveCountDest = []
    static List<Integer> postMoveCountSrc = []
    static List<Integer> postMoveCountDest = []
    static List<String> successStatuses = []
    static Boolean OVERWRITE_FLAG = false
    static List<String> excludeList = []


    static void main(String[] args) {
        if (args) {
            log.info "args $args"
            profiles = args.toList()
            if (profiles.last().equalsIgnoreCase("false") || profiles.last().equalsIgnoreCase("true")) {
                OVERWRITE_FLAG = BooleanUtils.toBoolean(profiles.last().toLowerCase())
                profiles.remove(profiles.last())
            }
            List removables = []
            profiles.each { String profile ->
                {
                    if (profile.startsWith("exclude=")) {
                        excludeList = profile.replace("exclude=", "").split(/.pdf\s*,\s*/) as List
                        excludeList = excludeList.collect {String excludable -> {
                            if(!excludable.endsWith(".pdf")) {
                                return  excludable + '.pdf'
                            }
                            else return excludable
                        } }
                        log.info("excludeList ${excludeList}")
                        removables.add(profile)
                    }
                }
            }

            removables.each { String removable ->
                {
                    profiles.remove(removable)
                    log.info("removed profile ${profiles}")
                }
            }
        }
        log.info("FileMover started for ${profiles.size()} Profiles on ${UploadUtils.getFormattedDateString()}")
        log.info "profiles $profiles overWriteFlag $OVERWRITE_FLAG"
        move()
    }

    static void move() {
        Map<String, String> srcMetaDataMap = FileUtil.getSrcFoldersCorrespondingToProfile();
        Map<String, String> destMetaDataMap = FileUtil.getDestFoldersCorrespondingToProfile();
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]

        int index = 1
        profiles.each { String profile ->
            String report = ""
            String srcDirAbsPath = srcMetaDataMap[profile]

            if (srcDirAbsPath) {
                int srcFilesCountBeforeMove = 0
                int destFilesCountBeforeMove = 0
                int srcFilesCountAfterMove = 0
                int destFlesCountAfterMove = 0
                int destFolderDiff = 0

                String destDir = destMetaDataMap[profile]
                //check no file is in use before moving
                String[] filesInUse = checkNoFileInUse(srcDirAbsPath)
                if (filesInUse.length > 0) {
                    report += """${profile}: 
                    Following Files in Use.
                    Cannot move anything
                    ${filesInUse.join("\n")}\n"""
                    successStatuses.add(FILE_IN_USE)
                    report += "${profile}:\n${FILE_IN_USE}\n"
                } else {
                    saveFreezeFileStatePreMove(destDir, profile)

                    srcFilesCountBeforeMove = noOfFiles(srcDirAbsPath)
                    preMoveCountSrc.push(srcFilesCountBeforeMove)
                    destFilesCountBeforeMove = noOfFiles(destDir)
                    preMoveCountDest.push(destFilesCountBeforeMove)
                    if (srcFilesCountBeforeMove) {
                        log.info("Moving $srcFilesCountBeforeMove files from \n${srcDirAbsPath} to \n${destDir}")
                        FileUtil.movePdfsInDir(srcDirAbsPath, destDir, excludeList, OVERWRITE_FLAG)
                    }
                    srcFilesCountAfterMove = noOfFiles(srcDirAbsPath)
                    postMoveCountSrc.push(srcFilesCountAfterMove)

                    destFlesCountAfterMove = noOfFiles(destDir)
                    postMoveCountDest.push(destFlesCountAfterMove)

                    destFolderDiff = Math.subtractExact(destFlesCountAfterMove, destFilesCountBeforeMove)
                    totalFilesMoved.add(destFolderDiff)
                    if (srcFilesCountBeforeMove) {
                        report += """${profile}: 
                                     ${dirStats(srcDirAbsPath, srcFilesCountBeforeMove, srcFilesCountAfterMove)},
                                     ${dirStats(destDir, destFilesCountBeforeMove, destFlesCountAfterMove)}
                                     Moved ${destFolderDiff} files.\n"""
                    }
                    String success = getSuccessString(srcFilesCountBeforeMove, srcFilesCountAfterMove, destFolderDiff)
                    successStatuses.add(success)
                    report += "${profile}:\n${success}\n"
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
                      PreMove Src  ${statsRow(preMoveCountSrc)}    PreMove Dest ${statsRow(preMoveCountDest)}
                      PostMove Src ${statsRow(postMoveCountSrc)}   PostMove Dest ${statsRow(postMoveCountDest)} 

                      Total Files Moved [${totalFilesMoved.join("+")}=${totalFilesMoved.sum()}] 
                      [ ${successStatuses.join("+")} ]
                      [ ${successCount(SUCCESS_STRING)}(S)+  ${successCount(FAILURE_STRING)}(F)+
                        ${successCount(POSSIBLE_OVERWRITE_ISSUES_STRING)}(OverWriteError)+ ${successCount(FILE_IN_USE)}(FILE_IN_USE)+${successCount(NOTHING_TO_MOVE)}(N2M)]=(${successStatuses.size()}) ]"""
        }
    }

    static void saveFreezeFileStatePreMove(String destDir, String profile) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(EGangotriUtil.DATE_TIME_AM_PATTERN)
        String timeOfMove = dateFormat.format(new Date())
        String fileTitle = "manualDestFreezeFolderSnapshot @ ${timeOfMove}.txt"

        File manualFilesRepo = new File(EGangotriUtil.MANUAL_SNAPSHOT_REPO)
        if (!manualFilesRepo.exists()) {
            manualFilesRepo.mkdir()
        }
        File storingContentsOfFreezePreMove = new File(manualFilesRepo, fileTitle)
        String _freezeDirBeforeMove = new File(destDir).list()?.join("\n") ?: "empty\n"
        log.info("Storing snapshot in ${storingContentsOfFreezePreMove.name} _freezeDirBeforeMove ${_freezeDirBeforeMove}")
        storingContentsOfFreezePreMove << "Contents of ${destDir} for profile ${profile} preMove on ${timeOfMove}\n"
        storingContentsOfFreezePreMove << _freezeDirBeforeMove
        println storingContentsOfFreezePreMove.text
    }

    static String statsRow(List moveCountList) {
        return "[${moveCountList.reverse().join("+")}=${moveCountList.sum()}]"

    }

    static boolean isFileOpen(File file) {
        boolean isOpen = file.renameTo(file.absolutePath)
        return !isOpen
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

    static String[] checkNoFileInUse(String srcDirAbsPath) {
        String[] filesInUse = []
        File[] files = FileSizeUtil.allPdfsInDirAsFileList(srcDirAbsPath)
        if (files) {
            files.each { File file ->
                {
                    if (isFileOpen(file)) {
                        filesInUse += file.name
                    }
                }
            }
        }
        return filesInUse

    }

    static String getSuccessString(int srcFilesCountBeforeMove, int srcFilesCountAfterMove, int destFolderDiff) {
        if (!srcFilesCountBeforeMove) {
            return NOTHING_TO_MOVE
        } else {
            boolean grossMovementDiff = (srcFilesCountBeforeMove - srcFilesCountAfterMove) == destFolderDiff
            log.info("Gross Movement Diff ${grossMovementDiff} = (${srcFilesCountBeforeMove} - $srcFilesCountAfterMove) == ${destFolderDiff}\n")
            String success = grossMovementDiff ? SUCCESS_STRING : FAILURE_STRING
            if (success == SUCCESS_STRING && srcFilesCountAfterMove != 0) {
                success = POSSIBLE_OVERWRITE_ISSUES_STRING
            }
            return success;
        }
    }
}
