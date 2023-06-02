package com.happy_time.happy_time.ddd.job.news_job;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.news.NewsJobModel;
import com.happy_time.happy_time.ddd.news.news.NewsStatus;
import com.happy_time.happy_time.ddd.news.news.application.NewsApplication;
import com.happy_time.happy_time.ddd.news.news.command.CommandNews;
import com.happy_time.happy_time.ddd.shift_result.ShiftResultJobData;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsJobApplication {
    protected final Log logger = LogFactory.getLog(this.getClass());
    @Autowired
    private NewsApplication newsApplication;
    public void execute(JobModel jobModel) {
        if (jobModel == null) {
            return;
        }
        if (BooleanUtils.isTrue(jobModel.getExecuted())) {
            logger.error("NewsJobApplication job executed:" + jobModel.get_id().toHexString());
            return;
        }
        if (StringUtils.isBlank(jobModel.getTenant_id())) {
            logger.error("NewsJobApplication missing tenant_id:" + jobModel.get_id().toHexString());
            return;
        }
        String current_date = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis());

        if (!current_date.equals(jobModel.getExecuted_time())) {
            logger.error("NewsJobApplication not in executed time:" + jobModel.get_id().toHexString());
            return;
        }

        if (StringUtils.isBlank(jobModel.getJob_data())) {
            logger.error("NewsJobApplication job data null:" + jobModel.get_id().toHexString());
            return;
        }

        NewsJobModel data = JsonUtils.jsonToObject(jobModel.getJob_data(), NewsJobModel.class);
        if (data == null) {
            logger.error("NewsJobApplication job data null:" + jobModel.get_id().toHexString());
            return;
        }

        if (StringUtils.isBlank(data.getId())) {
            logger.error("NewsJobApplication id null:" + jobModel.get_id().toHexString());
            return;
        }
        New item = newsApplication.getById(data.getId());
        if (item == null) {
            logger.error("NewsJobApplication new not found:" + jobModel.get_id().toHexString());
            return;
        }
        CommandNews commandNews = CommandNews.builder()
                .status(NewsStatus.POSTED)
                .ref(item.getCreate_by())
                .build();
        try {
            newsApplication.update(commandNews, item.get_id().toHexString());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
