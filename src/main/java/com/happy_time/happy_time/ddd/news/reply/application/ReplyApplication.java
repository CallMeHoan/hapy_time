package com.happy_time.happy_time.ddd.news.reply.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.news.application.NewsApplication;
import com.happy_time.happy_time.ddd.news.news.command.CommandNews;
import com.happy_time.happy_time.ddd.news.reply.Reply;
import com.happy_time.happy_time.ddd.news.reply.command.CommandReply;
import com.happy_time.happy_time.ddd.news.reply.repository.IReplyRepository;
import org.apache.commons.lang3.StringUtils;
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

@Component
public class ReplyApplication {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private NewsApplication newsApplication;
    @Autowired
    private IReplyRepository iReplyRepository;


    public Reply create(Reply reply) throws Exception {
        if (StringUtils.isEmpty(reply.getAgent_id())
                || StringUtils.isBlank(reply.getType())
                || StringUtils.isBlank(reply.getReply_content())
                || StringUtils.isBlank(reply.getTenant_id())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        // sau khi update sẽ update số lượng tương tác trong new
        New news = newsApplication.getById(reply.getNew_id());
        if (news != null) {
            Reply res = iReplyRepository.insert(reply);
            switch (res.getType()) {
                case "comment" -> {
                    Integer total_reply = news.getTotal_replies();
                    news.setTotal_replies(total_reply++);
                }
                case "like" -> {
                    Integer total_like = news.getTotal_likes();
                    news.setTotal_replies(total_like++);
                }
                default -> {
                }
            }
            CommandNews command = CommandNews.builder()
                    .tenant_id(news.getTenant_id())
                    .total_replies(news.getTotal_replies())
                    .total_likes(news.getTotal_likes())
                    .build();
            newsApplication.update(command, news.get_id().toHexString());
            return res;
        }
        return null;
    }
    public Page<Reply> search(CommandReply command, Integer page, Integer size) throws Exception {
        List<Reply> list = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }

        Long total = mongoTemplate.count(query, Reply.class);
        list = mongoTemplate.find(query.with(pageRequest), Reply.class);
        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                () -> total);
    }

}
