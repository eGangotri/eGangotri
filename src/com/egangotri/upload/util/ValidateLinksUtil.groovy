package com.egangotri.upload.util

import com.egangotri.upload.vo.ItemsVO
import com.egangotri.upload.vo.LinksVO
import com.egangotri.upload.vo.UploadVO
import groovy.util.logging.Slf4j

@Slf4j
class ValidateLinksUtil {
    static List<LinksVO> csvToUsheredItemsVO(File csvFile) {
        List<LinksVO> items = []
        csvFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            items.add(new LinksVO(_fields.toList()))
        }
        return items
    }

    static List<ItemsVO> csvToItemsVO(File csvFile) {
        List<ItemsVO> items = []
        csvFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            items.add(new ItemsVO(_fields.toList()))
        }
        return items
    }

    static Tuple statsForItemsVO(String csvFile){
        List<ItemsVO> vos = csvToItemsVO(new File(csvFile))
        return statsForVOs(vos)
    }

    static Tuple statsForUsheredItemsVO(String csvFile){
        List<LinksVO> vos = csvToUsheredItemsVO(new File(csvFile))
        return statsForVOs(vos)
    }

    static Tuple statsForVOs(List<? extends UploadVO> vos){
        if(!vos) return new Tuple(0,"0")
        String desc = vos?.first()?.getClass()?.simpleName == LinksVO.simpleName ? "item(s) were ushered for upload" : "item(s) were queued for upload"
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
}