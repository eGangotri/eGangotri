package com.egangotri.upload.archive

import com.egangotri.upload.util.ValidateUtil
import com.egangotri.upload.vo.UsheredVO
import groovy.util.logging.Slf4j

@Slf4j
class QuickTesting {
    static void main(String[] args) {
        Set<UsheredVO> ushLinks =
                ValidateUtil.csvToUsheredItemsVO(
                        "C:\\Users\\manee\\eGangotri\\items_ushered\\ushered_items_25-Dec-2021_11-15-AM.csv")
        def archiveProfiles = ushLinks*.archiveProfile as Set
        log.info("Converted " + ushLinks.size() +
                " links of upload-ushered Item(s) from CSV in Profiles" +
                " ${archiveProfiles.toString()}")

    }
}
