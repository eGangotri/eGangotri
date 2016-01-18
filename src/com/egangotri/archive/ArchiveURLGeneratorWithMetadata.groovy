package com.egangotri.archive

import com.egangotri.util.PDFUtil

import java.awt.Desktop

/**
 * Created by user on 1/13/2016.
 * https://blog.archive.org/2013/02/08/presetting-metadata-with-the-new-beta-uploader/
 */

class ArchiveURLGeneratorWithMetadata {

    static final String baseUrl = "http://archive.org/upload/?"
    static final String ampersand = "&"
    static final String license_picker = "license_picker=CC0"
    static final int numberOfTimes = 1

    static final launchBrowser = true
    //http://archive.org/upload/?subject=Manuscripts,Sanskrit&language=san&description=Manuscripts%20of%20Dharmartha%20Trust%20(%20धर्मार्थ%20ट्रस्ट%20)%20%20at%20Raghunath%20Temple,%20Jammu,J%26K&creator=eGangotri&license_picker=CC0

    static main(args) {
        String link = generateURL()
        if (launchBrowser) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop()
                try {
                    (1..numberOfTimes).each {
                        desktop.browse(new URI(link))
                    }
                } catch (IOException | URISyntaxException e) {
                    println "exception"
                    e.printStackTrace()
                }
            }
        }
    }

    public static String generateURL() {
        def metaDataMap = PDFUtil.loadProperties("${PDFUtil.HOME}/archiveProj/URLGeneratorMetadata.properties")

        String fullURL = baseUrl + metaDataMap."${PDFUtil.ARCHIVE_PROFILE}.subjects" + ampersand + metaDataMap."${PDFUtil.ARCHIVE_PROFILE}.language" + ampersand + metaDataMap."${PDFUtil.ARCHIVE_PROFILE}.description" + ampersand + metaDataMap."${PDFUtil.ARCHIVE_PROFILE}.creator" + ampersand + license_picker
        println fullURL
        return fullURL
    }

}

