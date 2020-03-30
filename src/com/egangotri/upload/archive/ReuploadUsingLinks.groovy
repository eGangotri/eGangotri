package com.egangotri.upload.archive

import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.vo.LinksVO
import groovy.util.logging.Slf4j

@Slf4j
class ReuploadUsingLinks {
    //This static variable can only be used with generateFailedLinksFromStaticList()
    static  List<String> STATIC_LIST_OF_BAD_LINKS =['https://archive.org/details/businessinternettherichjerkmakingmoneyontheinternetiseasytherichjerk.com2005_611_z']

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
    static void generateFailedLinksFromStaticList(){
        log.info("generating vos from static list of Links with size: " + STATIC_LIST_OF_BAD_LINKS.size())
        SettingsUtil.IGNORE_QUEUED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=true
        SettingsUtil.IGNORE_USHERED_ITEMS_IN_REUPLOAD_FAILED_ITEMS=false
        SettingsUtil.ONLY_GENERATE_STATS_IN_REUPLOAD_FAILED_ITEMS=false

        ValidateUploadsAndReUploadFailedItems.USHERED_LINKS_FOR_TESTING.eachWithIndex{ LinksVO entry, int i ->
            if(STATIC_LIST_OF_BAD_LINKS*.trim().contains(entry.archiveLink)){
                log.info("entry.uploadLink: " + entry.uploadLink)
                ValidateUploadsAndReUploadFailedItems.MISSED_OUT_USHERED_ITEMS << entry
            }
        }
    }
}
