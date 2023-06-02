package com.happy_time.happy_time.ddd.news.reply.service;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.news.reply.Reply;
import com.happy_time.happy_time.ddd.news.reply.repository.IReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class ReplyService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IReplyRepository iReplyRepository;

    public void deleteMany(String new_id, String tenant_id) {

        List<Reply> replies = this.findByNewId(new_id, tenant_id);
        if (!CollectionUtils.isEmpty(replies)) {
            for (Reply reply : replies) {
                reply.setIs_deleted(true);
            }
            iReplyRepository.saveAll(replies);
        }
    }

    public List<Reply> findByNewId(String new_id, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("new_id").is(new_id));
        return mongoTemplate.find(query, Reply.class);
    }
}
