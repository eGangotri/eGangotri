package com.egangotri.scheduler

import com.egangotri.util.EGangotriUtil
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

class HelloJob implements Job {
    static final String UPLOAD_PROFILES_PROPERTIES_FILE = EGangotriUtil.EGANGOTRI_BASE_DIR + File.separator + "google_drive" + File.separator + "cronJobInstructions.txt"

    void execute(JobExecutionContext context)
            throws JobExecutionException {

        println("Hello Quartz!");
        println "cmd /c echo 'Hi!'".execute().text
        File fileWithInstructions = new File(UPLOAD_PROFILES_PROPERTIES_FILE)
        text = fh1.getText('UTF-8')
    }

}