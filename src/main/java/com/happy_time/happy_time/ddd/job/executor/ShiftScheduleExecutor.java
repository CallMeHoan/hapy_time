package com.happy_time.happy_time.ddd.job.executor;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class ShiftScheduleExecutor {

    @Autowired
    private JobApplication jobApplication;
    @Autowired
    private JobExecutor jobExecutor;

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Scheduled(cron = "0 0 0 * * ?")
    public void execute() {
        Long current = System.currentTimeMillis();
        logger.info("Logging job execution at" + DateTimeUtils.convertLongToDate(DateTimeUtils.DEFAULT_FORMAT, current));
        String date = DateTimeUtils.convertLongToDate(DateTimeUtils.DEFAULT_FORMAT, current);
        List<JobModel> jobs = jobApplication.searchJobs(date);
        if (!CollectionUtils.isEmpty(jobs)) {
            for (JobModel job : jobs) {
                jobExecutor.executeJob(job);
            }
        }
        logger.info("ShiftScheduleExecutor executed " + jobs.size());
    }
}
