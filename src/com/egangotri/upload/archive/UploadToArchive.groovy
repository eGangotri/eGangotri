package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import org.slf4j.*

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
class UploadToArchive {
    final static Logger Log = LoggerFactory.getLogger(this.simpleName)

    static
    final List ARCHIVE_PROFILES = [ArchiveHandler.ARCHIVE_PROFILE.DT, ArchiveHandler.ARCHIVE_PROFILE.RK, ArchiveHandler.ARCHIVE_PROFILE.IB, ArchiveHandler.ARCHIVE_PROFILE.JG]

    static main(args) {
        List archiveProfiles = ARCHIVE_PROFILES
        if (args) {
            Log.info "args $args"
            archiveProfiles = args.toList()
        }

        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        execute(archiveProfiles, metaDataMap)
    }

    public static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer,String> uploadSuccessCheckingMatrix = [:]
        Log.info "Start uploading to Archive"
        profiles*.toString().eachWithIndex { archiveProfile,  index ->
            Log.info "${index+1}). Test Uploadables in archive.org Profile $archiveProfile"
            int countOfUploadablePdfs = UploadUtils.getCountOfUploadablePdfsForProfile(archiveProfile)
            int countOfUploadedItems = 0
            Log.info ("CountOfUploadablePdfs: $countOfUploadablePdfs")
            if (countOfUploadablePdfs) {
                countOfUploadedItems = ArchiveHandler.uploadToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
                Log.info("Uploaded $countOfUploadedItems docs for archiveProfile")
            } else {
                Log.info "No Files uploadable for Profile $archiveProfile"
            }

            String rep = "$archiveProfile, \t $countOfUploadablePdfs,\t $countOfUploadedItems,\t" + (countOfUploadablePdfs == countOfUploadedItems?'Success':'Failure!!!!')
            uploadSuccessCheckingMatrix.put((index+1),rep )
        }

        Log.info "Upload Report:\n"

        uploadSuccessCheckingMatrix.each { k,v ->
            Log.info "$k) $v"
        }

        Log.info "***Browser for Archive Upload Launches Done"
        return true
    }
}


