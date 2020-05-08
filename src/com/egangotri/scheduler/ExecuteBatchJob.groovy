package com.egangotri.scheduler

import com.egangotri.upload.util.UploadUtils
import com.egangotri.util.EGangotriUtil
import groovy.util.logging.Slf4j
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

import java.text.SimpleDateFormat

@Slf4j
class ExecuteBatchJob implements Job {
    static final String  CRON_FILE_PATH = File.separator + "google_drive" + File.separator + "archive_uploader" + File.separator + "cron.txt"
    static final String REMOTE_INSTRUCTIONS_FILE = EGangotriUtil.EGANGOTRI_BASE_DIR + CRON_FILE_PATH
    static final String DEFAULT_INSTRUCTION = "echo Hi @"

    static final String RESTART_TEAMVIEWER = 'TASKKILL /IM TEAMVIEWER.exe /F && "C:\\Program Files (x86)\\TeamViewer\\TeamViewer.exe"'
    static final String RESTART_CHROME = 'TASKKILL /IM chrome.exe /F && "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"'
    static final String RESTART_SYSTEM = 'shutdown /r'

    static final Map<String, String> COMMON_INSTRUCTIONS_MAP =
            ["D": DEFAULT_INSTRUCTION + "<---", "T":RESTART_TEAMVIEWER, "C":RESTART_CHROME, "R": RESTART_SYSTEM]

    void execute(JobExecutionContext context)
            throws JobExecutionException {

        log.info("Cron Job Started");
        //we have to clear it each time so that no instuction is repeated
        File fileWithInstructions = new File(REMOTE_INSTRUCTIONS_FILE)
        SimpleDateFormat dateFormat = new SimpleDateFormat(UploadUtils.DATE_TIME_PATTERN)

        if(!fileWithInstructions.exists()){
            fileWithInstructions.createNewFile()
        }
        String instructions = fileWithInstructions.getText('UTF-8').trim()

        if(!instructions){
            instructions = COMMON_INSTRUCTIONS_MAP.D + dateFormat.format(new Date())
        }
        if(instructions?.size() == 1 && COMMON_INSTRUCTIONS_MAP.keySet().contains(instructions.toUpperCase())){
            instructions = COMMON_INSTRUCTIONS_MAP.get(instructions)
        }
        log.info "Instruction for execution will be: cmd /c ${instructions}"

        //reset instruction
        fileWithInstructions.write(DEFAULT_INSTRUCTION + dateFormat.format(new Date()))
        log.info "cmd /c ${instructions}".execute().text
        //To reboot use
        //shutdown /r
        // TO Close all Chrome Browsers use
        // TASKKILL /IM chrome.exe /F
    }

}