package com.egangotri.scheduler

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory

class ExecuteScheduledJob {
    static void main(String[] args) throws Exception {
        JobDetail job = JobBuilder.newJob(HelloJob.class)
                .withIdentity("executeRemotelyJob", "team-viewer-server-1").build();
        // Trigger the job to run on the next round minute
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("executeRemotelyTrigger", "team-viewer-server-1")
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMinutes(5).repeatForever())
                .build();
        // schedule it
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }
}
