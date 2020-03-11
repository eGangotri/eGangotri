package com.egangotri.upload.util

import com.egangotri.upload.vo.ItemsVO
import com.egangotri.upload.vo.LinksVO
import com.egangotri.upload.vo.UploadVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class ValidateLinksUtil {
    static List<LinksVO> csvToLinksVO(File csvFile) {
        List<LinksVO> items = []
        csvFile.splitEachLine("\",") { fields ->
            def _fields = fields.collect { stripDoubleQuotes(it.trim()) }
            items.add(new LinksVO(_fields.toList()))
        }
        items.each{
            println(it.toString())
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

    static int statsForItemsVO(String csvFile){
        List<ItemsVO> vos = csvToItemsVO(new File(csvFile))
        return statsForVOs(vos)
    }

    static int statsForLinksVO(String csvFile){
        List<LinksVO> vos = csvToLinksVO(new File(csvFile))
        return statsForVOs(vos)
    }

    static int statsForVOs(List<? extends UploadVO> vos){
        if(!vos) return 0
        String desc = vos?.first()?.getClass()?.simpleName == LinksVO.simpleName ? "item(s) had Identifiers generated from the uploaded ones" : "item(s) were queued for upload"
        def vosGrouped = vos.groupBy { item -> item.archiveProfile}

        int totalItems = 0
        vosGrouped.eachWithIndex { def entry, int i ->
            totalItems += entry.value.size()
            log.info( " ${i+1}). ${entry.value.size()} $desc  for profile ${entry.key}")
        }
        return totalItems
    }
    static String stripDoubleQuotes(String field) {
        return field.replaceAll("\"", "")
    }
}
