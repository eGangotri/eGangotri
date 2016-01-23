package com.egangotri.upload.archive

import java.awt.Desktop

/**
 * Created by user on 1/13/2016.
 * https://blog.archive.org/2013/02/08/presetting-metadata-with-the-new-beta-uploader/
 */

@Deprecated
class LaunchBrowserWithArchiveUrlWithMetadata {

   static final int numberOfTimes = 1

    static final launchBrowser = true
    //http://archive.org/upload/?subject=Manuscripts,Sanskrit&language=san&description=Manuscripts%20of%20Dharmartha%20Trust%20(%20धर्मार्थ%20ट्रस्ट%20)%20%20at%20Raghunath%20Temple,%20Jammu,J%26K&creator=eGangotri&license_picker=CC0

    static main(args) {
        String link = UploadToArchive.generateURL()
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



}

