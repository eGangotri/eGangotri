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
    static void main(String[] args) throws Exception {
        if(args && args?.first()?.toString()?.isInteger()){
            CRON_JOB_FREQUENCY_IN_MINUTES = args[0].toInteger()
        }
        execute("team-viewer-server-1", ExecuteBatchJob.class)
    }

    static void execute(String groupName, Class aClass ){
        JobDetail job = JobBuilder.newJob(aClass)
                .withIdentity("executeRemotelyJob", groupName).build();
        // Trigger the job to run on the next round minute
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("executeRemotelyTrigger", groupName)
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
