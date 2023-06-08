package com.happy_time.happy_time.ddd.news.news.application;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.command.CommandIPConfig;
import com.happy_time.happy_time.ddd.jedis.JedisMaster;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import com.happy_time.happy_time.ddd.news.category.Category;
import com.happy_time.happy_time.ddd.news.category.application.CategoryApplication;
import com.happy_time.happy_time.ddd.news.category.command.CommandCategory;
import com.happy_time.happy_time.ddd.news.category.repository.ICategoryRepository;
import com.happy_time.happy_time.ddd.news.category.service.CategoryService;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.news.NewsJobModel;
import com.happy_time.happy_time.ddd.news.news.NewsStatus;
import com.happy_time.happy_time.ddd.news.news.command.CommandNews;
import com.happy_time.happy_time.ddd.news.news.repository.INewsRepository;
import com.happy_time.happy_time.ddd.news.reply.application.ReplyApplication;
import com.happy_time.happy_time.ddd.news.reply.service.ReplyService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.happy_time.happy_time.ddd.jedis.JedisMaster.COLON;

@Component
public class NewsApplication {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private INewsRepository iNewsRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private JobApplication jobApplication;
    public New create(New item) throws Exception {
        if (StringUtils.isBlank(item.getTitle())
                || StringUtils.isBlank(item.getContent())
                || StringUtils.isBlank(item.getCategory_id())
                || StringUtils.isBlank(item.getBanner())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        //update số lượng bài viết của category
        Category category = categoryService.getById(new ObjectId(item.getCategory_id()));
        if (category != null) {
            String name_unsigned = HAPStringUtils.stripAccents(item.getTitle()).toLowerCase(Locale.ROOT);
            Long current = System.currentTimeMillis();
            item.setCreated_date(current);
            item.setLast_updated_date(current);
            item.setTitle_unsigned(name_unsigned);
            iNewsRepository.insert(item);


            Integer total_news = category.getTotal_news();
            total_news = total_news + 1;
            category.setTotal_news(total_news);
            CommandCategory command = CommandCategory.builder()
                    .tenant_id(item.getTenant_id())
                    .total_news(total_news)
                    .name(category.getCategory_name())
                    .ref(item.getCreate_by())
                    .build();
            categoryService.update(command, item.getCategory_id());

            if (item.getStatus().equals(NewsStatus.ON_SCHEDULED) && item.getPost_date() != null && item.getPost_date() > System.currentTimeMillis()) {
                //set job để đăng tin
                NewsJobModel model = NewsJobModel.builder()
                        .id(item.get_id().toHexString())
                        .tenant_id(item.getTenant_id())
                        .build();
                String data = JsonUtils.toJSON(model);
                JobModel jobModel = JobModel.builder()
                        .tenant_id(item.getTenant_id())
                        .action(JobAction.schedule_new)
                        .executed_time_in_millis(item.getPost_date())
                        .job_data(data)
                        .build();
                JobModel job = jobApplication.setJob(jobModel);
                this.updateJobId(item, job.get_id().toHexString());

            }
            return item;
        }
        return null;
    }

    public New getById(String id) {
        New item = mongoTemplate.findById(new ObjectId(id), New.class);
        if (item != null && BooleanUtils.isFalse(item.getIs_deleted())) {
            return item;
        }
        return null;
    }

    public Boolean delete(String id) throws Exception {
        Long current_time = System.currentTimeMillis();
        New item = this.getById(id);
        if (item != null)  {
            item.setIs_deleted(true);
            item.setLast_updated_date(current_time);
            item.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            mongoTemplate.save(item, "new");

            //sau khi xóa thì sẽ giảm số lượng bài viết trong category
            Category category = categoryService.getById(new ObjectId(item.getCategory_id()));
            Integer total_news = category.getTotal_news();
            total_news = total_news - 1;
            category.setTotal_news(total_news);
            CommandCategory command = CommandCategory.builder()
                    .tenant_id(item.getTenant_id())
                    .total_news(total_news)
                    .name(category.getCategory_name())
                    .ref(item.getCreate_by())
                    .build();
            categoryService.update(command, item.getCategory_id());

            //xóa luôn những reply của thằng này
            replyService.deleteMany(item.get_id().toHexString(), item.getTenant_id());
            return true;
        }
        return false;
    }

    public New update(CommandNews command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        New item = mongoTemplate.findOne(query, New.class);
        if (item != null) {
            item.setTitle(StringUtils.isNotBlank(command.getTitle()) ? command.getTitle() : item.getTitle());
            item.setBanner(StringUtils.isNotBlank(command.getBanner()) ? command.getBanner() : item.getBanner());
            item.setContent(StringUtils.isNotBlank(command.getContent()) ? command.getContent() : item.getContent());
            item.setStatus(StringUtils.isNotBlank(command.getStatus()) ? command.getStatus() : item.getStatus());
            item.setCategory_id(StringUtils.isNotBlank(command.getCategory_id()) ? command.getCategory_id() : item.getCategory_id());
            item.setPost_date(command.getPost_date() != null ? command.getPost_date() : item.getPost_date());
            item.setTotal_replies(command.getTotal_replies() != null ? command.getTotal_replies() : item.getTotal_replies());
            item.setTotal_likes(command.getTotal_likes() != null ? command.getTotal_likes() : item.getTotal_likes());
            item.setLast_updated_date(current_time);
            item.setLast_update_by(command.getRef());
            item.setStatus(StringUtils.isNotBlank(command.getStatus()) ? command.getStatus() : item.getStatus());
            if (item.getStatus().equals(NewsStatus.ON_SCHEDULED) && item.getPost_date() != null && item.getPost_date() > current_time) {
                //xóa job cũ
                jobApplication.cancelJob(item.getJob_id());
                //set job để đăng tin
                NewsJobModel model = NewsJobModel.builder()
                        .id(item.get_id().toHexString())
                        .tenant_id(item.getTenant_id())
                        .build();
                String data = JsonUtils.toJSON(model);
                JobModel jobModel = JobModel.builder()
                        .tenant_id(item.getTenant_id())
                        .action(JobAction.schedule_new)
                        .executed_time_in_millis(item.getPost_date())
                        .job_data(data)
                        .build();
                JobModel res = jobApplication.setJob(jobModel);

                //update lại job id trong item
                item.setJob_id(res.get_id().toHexString());
            }
            return mongoTemplate.save(item, "new");
        }
        return null;
    }

    public Page<New> search(CommandNews command, Integer page, Integer size) throws Exception {
        List<New> list = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getTitle())) {
            query.addCriteria(Criteria.where("title_unsigned").regex(HAPStringUtils.stripAccents(command.getTitle().toLowerCase(Locale.ROOT)),"i"));
        }
        if(StringUtils.isNotBlank(command.getStatus())) {
            query.addCriteria(Criteria.where("status").is(command.getStatus()));
        }

        Long total = mongoTemplate.count(query, New.class);
        list = mongoTemplate.find(query.with(pageRequest), New.class);
        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                () -> total);
    }

     public Boolean updateTotalView(String id) {
        New item = this.getById(id);
        if (item != null) {
            Integer total_views = item.getTotal_views();
            total_views += 1;
            item.setTotal_views(total_views);
            mongoTemplate.save(item, "new");
            return true;
        }
        return false;
     }

     private New updateJobId(New news, String job_id) {
        news.setJob_id(job_id);
        return mongoTemplate.save(news, "new");
     }



}
