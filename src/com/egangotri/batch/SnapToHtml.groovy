package com.egangotri.batch

import com.egangotri.mail.Mailer
import com.egangotri.upload.util.SettingsUtil
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

@Slf4j
class SnapToHtml {
    static String SNAP2HTML_INSTALLATION_PATH="Snap2HTML.exe"
    static String FOLDER_FOR_SNAP2HTML_LISTING_GENERATION=""
    static def dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa")
    //Will be initialized in SettingsUtil because its dependencies on other static variable will make it outdated
    static String execCmd = ""

    static void main(String[] args) {
        if (args && args.size() == 2) {
            SNAP2HTML_INSTALLATION_PATH = args[0]
            FOLDER_FOR_SNAP2HTML_LISTING_GENERATION = args[1]
        }
        try{
            execute(args)
        }
        catch(Exception e){
            log.error("Snap2Html",e)
        }
    }

    static void execute(String[] args){
        SettingsUtil.applySnap2HtmlSettings()
        if(!FOLDER_FOR_SNAP2HTML_LISTING_GENERATION){
            return
        }
        log.info "cmd /c echo Make sure Snap2HTML.exe is installed and is on the Path".execute().text
        String fileTitle = "snap2html @ " + dateFormat.format(new Date())
        execCmd = execCmd.replaceAll('FILE_TITLE', fileTitle)
        log.info("SnapToHtml.execCmd: " + SnapToHtml.execCmd)
        log.info( "cmd /c ${execCmd}".execute().text)
        File filePath = new File("${FOLDER_FOR_SNAP2HTML_LISTING_GENERATION}${File.separator}${fileTitle}.html")
        if(filePath.exists()){
            Mailer.notify(filePath)
        }
    }
}
