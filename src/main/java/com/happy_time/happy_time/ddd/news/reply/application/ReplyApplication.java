package com.happy_time.happy_time.ddd.news.reply.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.AgentView;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.news.application.NewsApplication;
import com.happy_time.happy_time.ddd.news.news.command.CommandNews;
import com.happy_time.happy_time.ddd.news.reply.Reply;
import com.happy_time.happy_time.ddd.news.reply.command.CommandReply;
import com.happy_time.happy_time.ddd.news.reply.repository.IReplyRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Autowired
    private AgentApplication agentApplication;


    public Reply create(Reply reply) throws Exception {
        if (StringUtils.isEmpty(reply.getAgent_id())
                || StringUtils.isBlank(reply.getType())
                || StringUtils.isBlank(reply.getTenant_id())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        if (reply.getType().equals("comment") && StringUtils.isBlank(reply.getReply_content())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        // sau khi update sẽ update số lượng tương tác trong new
        New news = newsApplication.getById(reply.getNew_id());
        if (news != null) {
            Long current = System.currentTimeMillis();
            reply.setLast_updated_date(current);
            reply.setCreated_date(current);
            if (reply.getType().equals("like")) {
                // check xem đã có lượt like từ thằng agent này chưa
                Boolean check = this.checkExists(news.get_id().toHexString(), reply.getAgent_id(), reply.getTenant_id());
                if (BooleanUtils.isTrue(check)) {
                    //nếu có rồi thì xóa lượt like này đồng thời update lại số lượt like
                    Reply res = this.delete(news.get_id().toHexString(), reply.getAgent_id(), reply.getTenant_id());
                    Integer total_like = news.getTotal_likes();
                    news.setTotal_likes(total_like - 1);
                    CommandNews command = CommandNews.builder()
                            .tenant_id(news.getTenant_id())
                            .total_replies(news.getTotal_replies())
                            .total_likes(news.getTotal_likes())
                            .build();
                    newsApplication.update(command, news.get_id().toHexString());
                    return res;
                }
            }
            Reply res = iReplyRepository.insert(reply);
            switch (res.getType()) {
                case "comment" -> {
                    Integer total_reply = news.getTotal_replies();
                    news.setTotal_replies(total_reply + 1);
                }
                case "like" -> {
                    Integer total_like = news.getTotal_likes();
                    news.setTotal_likes(total_like + 1);
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
        if(StringUtils.isNotBlank(command.getType())) {
            query.addCriteria(Criteria.where("type").is(command.getType()));
        }
        if(StringUtils.isNotBlank(command.getNew_id())) {
            query.addCriteria(Criteria.where("new_id").is(command.getNew_id()));
        }

        Long total = mongoTemplate.count(query, Reply.class);
        if (total > 0) {
            query.with(Sort.by(Sort.Direction.DESC, "_id"));
            list = mongoTemplate.find(query.with(pageRequest), Reply.class);
            //set  view + get news name
            for (Reply item : list) {
                if (StringUtils.isNotBlank(item.getAgent_id())) {
                    AgentView view = agentApplication.setView(item.getAgent_id(), item.getTenant_id());
                    item.setAgent_view(view);
                }
                if (StringUtils.isNotBlank(item.getNew_id())) {
                    New news = newsApplication.getById(item.getNew_id());
                    if (news != null) {
                        item.setNew_title(news.getTitle());
                    }
                }
            }
        }

        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                () -> total);
    }

    public Boolean checkExists(String new_id, String agent_id, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("new_id").is(new_id));
        query.addCriteria(Criteria.where("agent_id").is(agent_id));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("type").is("like"));
        return mongoTemplate.exists(query, Reply.class);
    }

    private Reply delete(String new_id, String agent_id, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("new_id").is(new_id));
        query.addCriteria(Criteria.where("agent_id").is(agent_id));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("type").is("like"));
        Reply rep =  mongoTemplate.findOne(query, Reply.class);
        if (rep != null) {
            rep.setIs_deleted(true);
            rep.setLast_updated_date(System.currentTimeMillis());
            return iReplyRepository.save(rep);
        }
        return null;
    }

}
