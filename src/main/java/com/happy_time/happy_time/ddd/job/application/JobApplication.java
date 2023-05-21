package com.happy_time.happy_time.ddd.job.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.repository.IJobRepository;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class JobApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IJobRepository iJobRepository;

    @Autowired
    private ShiftResultApplication shiftResultApplication;

    public void setJob(JobModel job) {
        Long current = System.currentTimeMillis();
        job.setCreated_at(current);
        job.setLast_updated_at(current);
        iJobRepository.insert(job);
    }

    public void cancelJob(String id) {
        JobModel job = mongoTemplate.findById(id, JobModel.class);
        if (job != null) {
            job.setExecuted(true);
            job.setLast_updated_at(System.currentTimeMillis());
            iJobRepository.save(job);
        }
    }

    public void executeJob(JobModel job) {
        switch (job.getAction()) {
            case JobAction.set_shift_result:
                shiftResultApplication.executeJob(job);
                break;
            default:
                System.out.println("Unknown job action:" + job.get_id().toHexString());
                break;
        }
        //sau khi thực thi xong sẽ update lại biến excute của job
        this.cancelJob(job.get_id().toHexString());
    }

    public List<JobModel> searchJobs(String executed_date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("executed_time").is(executed_date));
        query.addCriteria(Criteria.where("executed").is(false));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        List<JobModel> res = mongoTemplate.find(query, JobModel.class);
        if (res.size() > 0) {
            System.out.println("Date execute " + executed_date);
            System.out.println("Executing " + res.size());
            return res;
        } else {
            System.out.println("Date execute " + executed_date);
            System.out.println("No Job found!");
        }
        return new ArrayList<>();
    }
}
