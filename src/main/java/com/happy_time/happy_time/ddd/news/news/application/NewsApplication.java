package com.happy_time.happy_time.ddd.news.news.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.command.CommandIPConfig;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.news.category.Category;
import com.happy_time.happy_time.ddd.news.category.application.CategoryApplication;
import com.happy_time.happy_time.ddd.news.category.command.CommandCategory;
import com.happy_time.happy_time.ddd.news.category.repository.ICategoryRepository;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.news.NewsStatus;
import com.happy_time.happy_time.ddd.news.news.command.CommandNews;
import com.happy_time.happy_time.ddd.news.news.repository.INewsRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class NewsApplication {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private INewsRepository iNewsRepository;
    @Autowired
    private CategoryApplication categoryApplication;

    public New create(New item) throws Exception {
        if (StringUtils.isBlank(item.getTitle())
                || StringUtils.isBlank(item.getContent())
                || StringUtils.isBlank(item.getCategory_id())
                || StringUtils.isBlank(item.getBanner())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        //update số lượng bài viết của category
        Category category = categoryApplication.getById(new ObjectId(item.getCategory_id()));
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
            categoryApplication.update(command, item.getCategory_id());
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

    public Boolean delete(String id) {
        Long current_time = System.currentTimeMillis();
        New item = this.getById(id);
        if (item != null)  {
            item.setIs_deleted(true);
            item.setLast_updated_date(current_time);
            item.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            mongoTemplate.save(item, "new");
            return true;
        }
        return false;
    }

    public New update(CommandNews command, String id) {
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
            item.setLast_updated_date(current_time);
            item.setLast_update_by(command.getRef());
            if (item.getStatus().equals(NewsStatus.ON_SCHEDULED) && item.getPost_date() != null && item.getPost_date() > current_time) {
                //set job để đăng tin + hủy job cũ
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

        list = mongoTemplate.find(query, New.class);
        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                () -> mongoTemplate.count(query, New.class));
    }



}
