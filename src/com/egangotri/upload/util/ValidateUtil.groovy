package com.egangotri.upload.util

import com.egangotri.upload.vo.QueuedVO
import com.egangotri.upload.vo.UsheredVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.nio.file.Files

@Slf4j
class ValidateUtil {
    static Set<UsheredVO> csvToUsheredItemsVO(File csvFile) {
        Set<UsheredVO> items = []
        csvFile.splitEachLine("\"\\s*,") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            items.add(new UsheredVO(_fields.toList()))
        }
        return items
    }

    static Set<QueuedVO> csvToQueuedVO(File csvFile) {
        Set<QueuedVO> items = []
        csvFile.splitEachLine("\"\\s*,") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            items.add(new QueuedVO(_fields.toList()))
        }
        return items
    }

    static Tuple statsForItemsVO(String csvFile){
        Set<QueuedVO> vos = csvToQueuedVO(new File(csvFile))
        return statsForVOs(vos)
    }

    static Tuple statsForUsheredItemsVO(String csvFile){
        Set<UsheredVO> vos = csvToUsheredItemsVO(new File(csvFile))
        return statsForVOs(vos)
    }

    static Tuple statsForVOs(Set<? extends UploadVO> vos){
        if(!vos) return new Tuple(0,"0")
        String desc = vos?.first()?.getClass()?.simpleName == UsheredVO.simpleName ? "item(s) were ushered for upload" : "item(s) were queued for upload"
        def vosGrouped = vos.groupBy { item -> item.archiveProfile}

        int totalItems = 0
        List sumString = []
        vosGrouped.eachWithIndex { def entry, int i ->
            totalItems += entry.value.size()
            sumString << entry.value.size()
            log.info( " ${i+1}). ${entry.value.size()} $desc  for profile ${entry.key}")
        }
        return new Tuple(totalItems, sumString.join("+"))
    }
    static String stripDoubleQuotes(String field) {
        return field.replaceAll("\"", "")
    }

    static File getLastModifiedFile(String folderPath, String pattern = "") {
        FileFilter fileFilter = { File file ->
            file.name.contains(pattern)
        } as FileFilter
        return new File(folderPath).listFiles(fileFilter)?.sort { -it.lastModified() }?.head()
    }

    static validateMaxUploadableLimit(){
        if(ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION > EGangotriUtil.MAX_UPLODABLES){
            log.info("Uploadable Count ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION} exceeds ${EGangotriUtil.MAX_UPLODABLES}. Cannot proceed. Quitting")
            System.exit(1)
        }
        log.info("Total Uploadable Count for Current Execution ${ArchiveUtil.GRAND_TOTAL_OF_ALL_UPLODABLES_IN_CURRENT_EXECUTION}")
    }

    static void logPerProfile(String msg, Set<? extends UploadVO> vos, String propertyAsString){
        log.info(msg)
        if(vos.size()){
            log.info("Affected Profile(s)" +  (vos*.archiveProfile as Set).toString())
            Map<String,Set<? extends UploadVO>> groupedByProfile = vos.groupBy{ def vo -> vo.archiveProfile} as Map<String, Set<? extends UploadVO>>

            groupedByProfile.keySet().each{ _prfName ->
                log.info("${_prfName}:")
                groupedByProfile[_prfName].eachWithIndex{ def vo, int counter ->
                    log.info("\t${counter+1}). '" + vo."$propertyAsString" + "'")
                }
            }
        }
    }

    static void moveFile(UsheredVO movableItems, String destFolder, String counter = ""){
        try {
            File movableFile = new File(movableItems?.path?:"")
            if(movableFile.exists()){
                File dest = new File(destFolder +  File.separator + movableItems.title)
                if(dest.exists()){
                    log.info("\t$dest pre=exists. Will alter title")
                    dest  = new File(destFolder +  File.separator + movableItems.title + "_1")
                }
                Files.move(movableFile.toPath(), dest.toPath())
                log.info("\t${counter}Moving ${movableItems.title} to ${destFolder}")
            }
            else{
                log.info("\t${counter}File ${movableItems?.title} not found.")
            }
        }
        catch(Exception e){
            log.error("Error moving ${movableItems.path} ${e.message}")
            e.printStackTrace()
        }
    }
}
