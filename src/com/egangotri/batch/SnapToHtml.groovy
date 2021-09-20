package com.egangotri.batch

import com.egangotri.mail.Mailer
import com.egangotri.upload.util.SettingsUtil
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

@Slf4j
class SnapToHtml {
    static String SNAP2HTML_INSTALLATION_PATH = EGangotriUtil.EGANGOTRI_BASE_DIR + File.separator + "Snap2HTML\\Snap2HTML.exe"
    static String SNAP2HTML_REPO = EGangotriUtil.EGANGOTRI_BASE_DIR + File.separator + "Snap2HTML\\listings"
    static String FOLDER_TO_GENERATE_SNAP2HTML_OF = ""
    static String DATE_TIME_AM_PATTERN = "dd-MMM-yyyy hh.mm aa"
    static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_AM_PATTERN)
    //Will be initialized in SettingsUtil because its dependencies on other static variable will make it outdated
    static String SNAP2HTML_EXEC_CMD = ""

    static void main(String[] args) {
        try {
            if (args) {
                FOLDER_TO_GENERATE_SNAP2HTML_OF = args[0]
            }
            execute(args)
        }
        catch (Exception e) {
            log.error("Snap2Html", e)
        }
    }

    static void execute(String[] args) {
        SettingsUtil.applySnap2HtmlSettings()
        if (!new File(SnapToHtml.SNAP2HTML_REPO).exists()) {
            return
        }
        log.info "cmd /c echo Make sure Snap2HTML.exe is installed and is on the Path specified(${SNAP2HTML_INSTALLATION_PATH})".execute().text
        String fileTitle = "snap2html @ " + dateFormat.format(new Date())
        SNAP2HTML_EXEC_CMD = SNAP2HTML_EXEC_CMD.replaceAll('FILE_TITLE', fileTitle)
        log.info("SnapToHtml.execCmd: " + SnapToHtml.SNAP2HTML_EXEC_CMD)
        log.info("cmd /c ${SNAP2HTML_EXEC_CMD}".execute().text)
        File filePath = new File("${SNAP2HTML_REPO}${File.separator}${fileTitle}.html")
        if (filePath.exists()) {
            Mailer.notify(filePath)
        }
        System.exit(0)
    }
}
