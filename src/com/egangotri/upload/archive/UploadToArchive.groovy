package com.egangotri.upload.archive

import com.egangotri.util.FileUtil
import org.openqa.selenium.support.ui.WebDriverWait
import com.egangotri.upload.util.UploadUtils
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions

/**
 * Created by user on 1/18/2016.
 * Make sure
 * The chromedriver binary is in the system path, or
 * use VM Argument -Dwebdriver.chrome.driver=c:/chromedriver.exe

 */
class UploadToArchive {

    static final List ARCHIVE_PROFILES = [/*ArchiveHandler.PROFILE_ENUMS.dt ,ArchiveHandler.PROFILE_ENUMS.rk,*/ArchiveHandler.PROFILE_ENUMS.ib, ArchiveHandler.PROFILE_ENUMS.jg]
    static main(args) {
        Map metaDataMap = UploadUtils.loadProperties("${UploadUtils.HOME}/archiveProj/UserIdsMetadata.properties")
        execute(ARCHIVE_PROFILES, metaDataMap)
    }

    public static void execute(List profiles, Map metaDataMap){
        println "Start uploading to Archive"
        profiles*.toString().each { String archiveProfile ->
            println "Uploading for Profile $archiveProfile"
            if(UploadUtils.hasAtleastOnePdf(ArchiveHandler.pickFolderBasedOnArchiveProfile(archiveProfile))){
                ArchiveHandler.uploadToArchive(metaDataMap, ArchiveHandler.ARCHIVE_URL, archiveProfile)
            }else{
                println "No Files uploadable for Profile $archiveProfile"

            }
        }
        println "***Browser for Archive Upload Launches Done"
    }


}


