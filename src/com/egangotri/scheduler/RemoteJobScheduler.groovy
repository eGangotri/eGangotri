package com.egangotri.scheduler

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory

class RemoteJobScheduler {
    static int CRON_JOB_FREQUENCY_IN_MINUTES = 5
    static void main(def args) throws Exception {
        JobDetail job = JobBuilder.newJob(ExecuteBatchJob.class)
                .withIdentity("executeRemotelyJob", "team-viewer-server-1").build();
        // Trigger the job to run on the next round minute
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("executeRemotelyTrigger", "team-viewer-server-1")
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMinutes(CRON_JOB_FREQUENCY_IN_MINUTES).repeatForever())
                .build();
        // schedule it
        Scheduler _scheduler = new StdSchedulerFactory().getScheduler();
        _scheduler.start();
        _scheduler.scheduleJob(job, trigger);
    }
}
