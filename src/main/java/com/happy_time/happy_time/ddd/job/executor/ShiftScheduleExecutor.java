package com.happy_time.happy_time.ddd.job.executor;

import com.happy_time.happy_time.ddd.job.application.JobApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ShiftScheduleExecutor {

    @Autowired
    private JobApplication jobApplication;

    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void execute() {
//        Long current = System.currentTimeMillis();
//        System.out.println("Logging job execution at" + DateTimeUtils.convertLongToDate(DateTimeUtils.DEFAULT_FORMAT, current));
//        String date = DateTimeUtils.convertLongToDate(DateTimeUtils.JOB_DATE_FORMAT, current);
//        List<JobModel> jobs = jobApplication.searchJobs(date);
//        if (!CollectionUtils.isEmpty(jobs)) {
//            for (JobModel job : jobs) {
//                jobApplication.executeJob(job);
//            }
//        }
    }
}
