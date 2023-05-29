package com.happy_time.happy_time.ddd.news.category.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.command.CommandIPConfig;
import com.happy_time.happy_time.ddd.news.category.Category;
import com.happy_time.happy_time.ddd.news.category.command.CommandCategory;
import com.happy_time.happy_time.ddd.news.category.repository.ICategoryRepository;
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
public class CategoryApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ICategoryRepository iCategoryRepository;

    public Page<Category> search(CommandCategory command, Integer page, Integer size) throws Exception {
        List<Category> categories = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getKeyword())) {
            query.addCriteria(Criteria.where("category_name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        categories = mongoTemplate.find(query, Category.class);
        return PageableExecutionUtils.getPage(
                categories,
                pageRequest,
                () -> mongoTemplate.count(query, Category.class));
    }

    public Category create(Category category) throws Exception {
        if (StringUtils.isBlank(category.getCategory_name())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(category.getCategory_name()).toLowerCase(Locale.ROOT);
        category.setCategory_name_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        category.setCreated_date(current);
        category.setLast_updated_date(current);
        iCategoryRepository.insert(category);
        return category;
    }

    public Category update(CommandCategory command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        Category category = mongoTemplate.findOne(query, Category.class);
        if(category != null) {
            if (StringUtils.isBlank(command.getName())) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getName()).toLowerCase(Locale.ROOT);
            category.setLast_updated_date(current_time);
            category.setCategory_name_unsigned(name_unsigned);
            category.setCategory_name(command.getName());
            category.setLast_update_by(command.getRef());
            category.setTotal_news(command.getTotal_news());
            return mongoTemplate.save(category, "category");
        }
        else return null;
    }

    public Category getById(ObjectId id) {
        Category category = mongoTemplate.findById(id, Category.class);
        if(category != null) {
            if (category.getIs_deleted()) return null;
            return category;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        Category category = mongoTemplate.findById(id, Category.class);
        if(category != null) {
            category.setIs_deleted(true);
            category.setLast_updated_date(current_time);
            category.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            category.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(category, "category");
            return true;
        } else return false;
    }

}
