package com.happy_time.happy_time.ddd.news.category.service;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.news.category.Category;
import com.happy_time.happy_time.ddd.news.category.command.CommandCategory;
import com.happy_time.happy_time.ddd.news.category.repository.ICategoryRepository;
import com.happy_time.happy_time.ddd.news.news.repository.INewsRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CategoryService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ICategoryRepository iCategoryRepository;

    public Category getById(ObjectId id) {
        Category category = mongoTemplate.findById(id, Category.class);
        if(category != null) {
            if (category.getIs_deleted()) return null;
            return category;
        } else return null;
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
}
