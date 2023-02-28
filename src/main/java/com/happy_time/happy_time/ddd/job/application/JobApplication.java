package com.happy_time.happy_time.ddd.job.application;

import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.repository.IJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IJobRepository iJobRepository;

    public void setJob(JobModel job) {
        Long current = System.currentTimeMillis();
        job.setCreated_at(current);
        job.setLast_updated_at(current);
        iJobRepository.insert(job);
    }

    public void cancelJob(String id) {
        JobModel job = mongoTemplate.findById(id, JobModel.class);
        if (job != null) {
            job.setIs_deleted(true);
            job.setLast_updated_at(System.currentTimeMillis());
            iJobRepository.save(job);
        }
    }

    public void executeJob(String id) {
        JobModel job = mongoTemplate.findById(id, JobModel.class);
        if (job != null) {
            switch (job.getAction()) {
                case JobAction.set_shift_result:


            }
        }

    }
}
