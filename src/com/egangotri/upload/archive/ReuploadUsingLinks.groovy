package com.egangotri.upload.archive

import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.vo.UsheredVO
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class ReuploadUsingLinks {
    //This static variable can only be used with generateFailedLinksFromStaticList()
    static  List<String> STATIC_LIST_OF_BAD_LINKS =
            []

    /** This method is used in unique cases.
     *  Where u have a list of failed Archive Urls and you want to use them to reupload them only
     * So u take the links copy paste to _staticListOfBadLinks ,
     * have following settings:
     * IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=true
     * IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=false
     * ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS=false
     *generating vos
     * comment out call to filterFailedUsheredItems()
     * uncomment call to generateFailedLinksFromStaticList() and execute the program
     *
     * .
     * Then upload the VOS
     */

    static main(args) {
        EGangotriUtil.recordProgramStart("ReuploadUsingLinks")
        SettingsUtil.applySettingsWithReuploaderFlags([true,false,false])
        ValidateUploadsAndReUploadFailedItems.execute(args,false)
        System.exit(0)
    }

    static void generateFailedLinksFromStaticList(){
        log.info("generating vos from static list of Links with size: " + STATIC_LIST_OF_BAD_LINKS.size())
        ValidateUploadsAndReUploadFailedItems.USHERED_LINKS_FOR_TESTING.eachWithIndex{ UsheredVO entry, int i ->
            if(STATIC_LIST_OF_BAD_LINKS*.trim().contains(entry.archiveLink)){
                log.info("entry.uploadLink: " + entry.uploadLink)
                ValidateUploadsAndReUploadFailedItems.MISSED_OUT_USHERED_ITEMS << entry
            }
        }
    }
}
