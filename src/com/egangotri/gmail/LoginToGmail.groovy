package com.egangotri.gmail

import com.egangotri.util.PDFUtil
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by user on 1/19/2016.
 * Only Logs In
 */
class LoginToGmail {
    static final String LOGIN_PROFILE = "jm" // "bm", "mm", "jm" , "lk", "sr", "srCP" , "ij"

    static main(args) {
        println "start"
        def metaDataMap = PDFUtil.loadProperties("${PDFUtil.HOME}/archiveProj/GmailData.properties")
        GmailHandler.login(metaDataMap,LOGIN_PROFILE)
    }

}
