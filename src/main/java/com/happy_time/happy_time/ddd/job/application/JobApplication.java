package com.happy_time.happy_time.ddd.job.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.command.CommandSearchJob;
import com.happy_time.happy_time.ddd.job.repository.IJobRepository;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IJobRepository iJobRepository;

    public JobModel setJob(JobModel job) {
        Long current = System.currentTimeMillis();
        job.setCreated_at(current);
        job.setLast_updated_at(current);
        return iJobRepository.insert(job);
    }

    public void cancelJob(String id) {
        JobModel job = mongoTemplate.findById(id, JobModel.class);
        if (job != null) {
            job.setExecuted(true);
            job.setLast_updated_at(System.currentTimeMillis());
            iJobRepository.save(job);
        }
    }

    public List<JobModel> searchJobs(CommandSearchJob command) {
        Query query = new Query();
        query.addCriteria(Criteria.where("executed").is(false));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (StringUtils.isNotBlank(command.getExecuted_date())) {
            query.addCriteria(Criteria.where("executed_time").is(command.getExecuted_date()));
        }
        if (command.getExecuted_time() != null) {
            query.addCriteria(Criteria.where("executed_time").lte(command.getExecuted_time() - 60).gte(command.getExecuted_time() + 60));
        }
        if (StringUtils.isNotBlank(command.getAction())) {
            query.addCriteria(Criteria.where("action").is(command.getAction()));
        }

        List<JobModel> res = mongoTemplate.find(query, JobModel.class);
        if (res.size() > 0) {
            logger.info("Date execute " + command.getExecuted_date());
            logger.info("Executing " + res.size());
            return res;
        } else {
            logger.info("Date execute " + command.getExecuted_date());
            logger.info("No Job found!");
        }
        return new ArrayList<>();
    }
}
