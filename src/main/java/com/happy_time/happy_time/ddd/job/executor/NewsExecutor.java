package com.happy_time.happy_time.ddd.job.executor;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import com.happy_time.happy_time.ddd.job.command.CommandSearchJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class NewsExecutor {
    @Autowired
    private JobApplication jobApplication;

    @Autowired
    private JobExecutor jobExecutor;

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Scheduled(cron = "*/60 * * * * *")
    private void execute() {
        String date = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis());
        CommandSearchJob command = CommandSearchJob.builder()
                .executed_date(date)
                .executed_time(System.currentTimeMillis())
                .action(JobAction.schedule_new)
                .build();
        List<JobModel> jobs = jobApplication.searchJobs(command);

        if (!CollectionUtils.isEmpty(jobs)) {
            for (JobModel job : jobs) {
                jobExecutor.executeJob(job);
            }
        }
        logger.info("ShiftScheduleExecutor executed " + jobs.size());
    }
}
