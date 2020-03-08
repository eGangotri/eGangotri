package com.egangotri.upload.archive

import com.egangotri.upload.util.SettingsUtil
import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=${System.getProperty('user.home')}${File.separator}chromedriver${File.separator}chromedriver.exe
 * chromedriver.exe
 C:\ws\eGangotri>java -Dwebdriver.chrome.driver=${System.getProperty('user.home')}${File.separator}chromedriver${File.separator}chromedriver.exe -jar ./build/libs/eGangotri.jar "DT"
 java -Dwebdriver.chrome.driver=/Users/user/chromedriver\chromedriver.exe -jar ./build/libs/eGangotri.jar "DT"
 ** Dont use \ followeb by a U

 */
@Slf4j
class UploadToArchive {

    static main(args) {
        List archiveProfiles = EGangotriUtil.ARCHIVE_PROFILES
        if (args) {
            log.info "args $args"
            archiveProfiles = args.toList()
        }

        // System.setProperty("webdriver.chrome.driver", getClass().getResource("chromedriver.exe").toURI().toString())
        Hashtable<String, String> metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        SettingsUtil.applySettings()
        UploadUtils.createIdentifierFileForCurrentExecution()
        execute(archiveProfiles, metaDataMap)
        System.exit(0)
    }

    static boolean execute(List profiles, Map metaDataMap) {
        Map<Integer, String> uploadSuccessCheckingMatrix = [:]
        log.info "Start uploading to Archive @ " + UploadUtils.getFormattedDateString()

        profiles*.toString().eachWithIndex { archiveProfile, index ->
            if (!UploadUtils.checkIfArchiveProfileHasValidUserName(metaDataMap, archiveProfile)) {
                return
            }
            log.info "${index + 1}). Starting upload in archive.org for Profile $archiveProfile"
            Integer countOfUploadablePdfs = UploadUtils.getCountOfUploadablePdfsForProfile(archiveProfile)
            if (countOfUploadablePdfs) {
                log.info "getUploadablesForProfile: $archiveProfile: ${countOfUploadablePdfs}"
                if (EGangotriUtil.GENERATE_ONLY_URLS) {
                    List<String> uploadables = UploadUtils.getUploadablesForProfile(archiveProfile)
                    ArchiveHandler.generateAllUrls(archiveProfile, uploadables)
                } else {
                    List<List<Integer>> uploadStats = ArchiveHandler.performPartitioningAndUploadToArchive(metaDataMap, archiveProfile)
                    int uplddSum = uploadStats.collect { elem -> elem.first() }.sum()
                    String statsAsPlusSeparatedValues = uploadStats.collect { elem -> elem.first() }.join(" + ")
                    String countOfUploadedItems = uploadStats.size() > 1 ? "($statsAsPlusSeparatedValues) = $uplddSum" : uploadStats.first().first()

                    int excSum = uploadStats.collect { elem -> elem.last() }.sum()
                    String excpsAsPlusSeparatedValues = uploadStats.collect { elem -> elem.last() }.join(" + ")
                    String exceptionCount = uploadStats.size() > 1 ? "($excpsAsPlusSeparatedValues) = $excSum" : uploadStats.first().last()
                    log.info("Uploaded $countOfUploadedItems items with (${exceptionCount}) Exceptions for Profile: $archiveProfile")

                    String statusMsg = countOfUploadablePdfs == uplddSum ? 'Success. All items were put for upload.' : "${(uplddSum == 0) ? 'All' : 'Some'} Failed!"
                    String rep = "$archiveProfile, \t Total $countOfUploadablePdfs,\t " +
                            "Attempted Upload Count $countOfUploadedItems,\t with  ${exceptionCount} Exceptions \t $statusMsg"
                    println(rep)
                    uploadSuccessCheckingMatrix.put((index + 1), rep)
                }
            } else {
                log.info "No uploadable files for Profile $archiveProfile"
            }
            EGangotriUtil.sleepTimeInSeconds(5)
        }
        if (uploadSuccessCheckingMatrix) {
            log.info "Upload Report:\n"
            uploadSuccessCheckingMatrix.each { k, v ->
                log.info "$k) $v"
            }
            log.info "\n ***All Items put for upload implies all were attempted successfully for upload. But there can be errors still after attempted upload. best to check manually."
        }

        log.info "***End of Upload to Archive Program"
        return true
    }
}


