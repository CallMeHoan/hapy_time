package com.happy_time.happy_time.ddd.news.news.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.news.category.Category;
import com.happy_time.happy_time.ddd.news.category.application.CategoryApplication;
import com.happy_time.happy_time.ddd.news.category.command.CommandCategory;
import com.happy_time.happy_time.ddd.news.category.repository.ICategoryRepository;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.news.repository.INewsRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

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
}
