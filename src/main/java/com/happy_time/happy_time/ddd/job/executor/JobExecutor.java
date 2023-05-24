package com.happy_time.happy_time.ddd.job.executor;

import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobExecutor {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private JobApplication jobApplication;

    @Autowired
    private ShiftResultApplication shiftResultApplication;

    public void executeJob(JobModel job) {
        switch (job.getAction()) {
            case JobAction.set_shift_result:
                shiftResultApplication.executeJob(job);
                break;
            default:
                logger.error("Unknown job action:" + job.get_id().toHexString());
                break;
        }
        //sau khi thực thi xong sẽ update lại biến excute của job
        jobApplication.cancelJob(job.get_id().toHexString());
    }
}
