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
    void execute(JobExecutionContext context)
            throws JobExecutionException {

        log.info("Cron Job Started");
        //we have to clear it each time so that no instuction is repeated
        File fileWithInstructions = new File(REMOTE_INSTRUCTIONS_FILE)
        SimpleDateFormat dateFormat = new SimpleDateFormat(UploadUtils.DATE_TIME_PATTERN)

        if(!fileWithInstructions.exists()){
            fileWithInstructions.createNewFile()
        }
        String instructions = fileWithInstructions.getText('UTF-8')
        if(!instructions){
            instructions = DEFAULT_INSTRUCTION + dateFormat.format(new Date())
        }
        fileWithInstructions.write(DEFAULT_INSTRUCTION + dateFormat.format(new Date()))
        log.info "cmd /c ${instructions}".execute().text
        //To reboot use
        //shutdown /r
        // TO Close all Chrome Browsers use
        // TASKKILL /IM chrome.exe /F
    }

}