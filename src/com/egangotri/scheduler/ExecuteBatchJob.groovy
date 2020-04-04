package com.egangotri.scheduler

import com.egangotri.util.EGangotriUtil
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

class ExecuteBatchJob implements Job {
    static final String  CRON_FILE_PATH = File.separator + "google_drive" + File.separator + "archive_uploader" + File.separator + "cron.txt"
    static final String REMOTE_INSTRUCTIONS_FILE = EGangotriUtil.EGANGOTRI_BASE_DIR + CRON_FILE_PATH
    static final String DEFAULT_INSTRUCTION = "echo Hi @"
    void execute(JobExecutionContext context)
            throws JobExecutionException {

        println("Cron Job Started");
        //we have to clear it each time so that no instuction is repeated
        File fileWithInstructions = new File(REMOTE_INSTRUCTIONS_FILE)
        if(!fileWithInstructions.exists()){
            fileWithInstructions.createNewFile()
        }
        String instructions = fileWithInstructions.getText('UTF-8')
        if(!instructions){
            instructions = DEFAULT_INSTRUCTION + new Date().format('YYYY-MM-dd HH:mm')
        }
        println "cmd /c ${instructions}".execute().text
        fileWithInstructions.write(DEFAULT_INSTRUCTION + new Date().format('YYYY-MM-dd HH:mm'))
        //To reboot use
        //"shutdown /r
    }

}