package com.egangotri.upload.archive

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

@Slf4j
class LoginToArchive {
    static final List ARCHIVE_PROFILES = []


    static main(args) {

/*        String acct = "indicjournals@gmail.com"
        ARCHIVE_PROFILES.add("$acct")
        ARCHIVE_PROFILES.add("jammumanuscripts@gmail.com")
        ARCHIVE_PROFILES.add("swamianantabodha@gmail.com")
        ARCHIVE_PROFILES.add("indologicalbooks@gmail.com")
        ARCHIVE_PROFILES.add("srinagarbooks@gmail.com")
        ARCHIVE_PROFILES.add("vintageindic@gmail.com")

        (2..25).each { digit ->
            ARCHIVE_PROFILES.add("$acct${digit.toString()}")
        }

        (27..30).each { digit ->
            ARCHIVE_PROFILES.add("$acct${digit.toString()}")
        }*/
        ARCHIVE_PROFILES.addAll("DT", "JG", "IB", "RK", "NK", "UR", "SR", "MS", "NR", "ANON", "KECSS", "UPSS", "VRVDW", "SBLK", "SBLK2", "KALLA", "KRI",
                "LCKN_PRE57", "LCKN_POST57", "OJHA", "VGBV", "TMKN", "RAMS", "UPSS_MANU", "RSS_MANU", "DT2", "EGANGOTRI_MANU", "EGANGOTRI_EBOOKS",
                "RETRO_UPLOAD", "SCL_HYD", "ISHWAR", "MAHA_MUNI", "DATTA", "VIJAY", "VISIONARY_TUNES", "AJG")

        ARCHIVE_PROFILES.each { println("$it")}
        log.info "login to Archive"
        def metaDataMap = UploadUtils.loadProperties(EGangotriUtil.ARCHIVE_PROPERTIES_FILE)
        ARCHIVE_PROFILES*.toString().each { String archiveProfile ->
            println "Logging for Profile $archiveProfile"
            ArchiveHandler.loginToArchive(metaDataMap, ArchiveHandler.ARCHIVE_LOGIN_URL, archiveProfile)
        }
        println "***Browser Launches Done"
    }
}

