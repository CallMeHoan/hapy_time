package com.happy_time.happy_time.ddd.news.reply.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.news.news.New;
import com.happy_time.happy_time.ddd.news.reply.Reply;
import com.happy_time.happy_time.ddd.news.reply.application.ReplyApplication;
import com.happy_time.happy_time.ddd.news.reply.command.CommandReply;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path ="/api/news/reply")
public class ReplyController {
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private ReplyApplication replyApplication;

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody Reply reply){
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.CREATE_ACTION)
                    .build();
            reply.setTenant_id(tenant_id);
            reply.setLast_update_by(ref);
            reply.setCreate_by(ref);
            reply.setAgent_id(agent_id);
            Reply created = replyApplication.create(reply);
            ResponseObject res;
            if(created != null) {
                res = ResponseObject.builder().status(9999).message("success").payload(created).build();
            } else {
                res = ResponseObject.builder().status(-9999).message("failed").payload(null).build();
            }
            return Optional.of(res);
        }catch(Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/search")
    public Optional<ResponseObject> search(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandReply command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id) || command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            command.setTenant_id(tenant_id);
            Page<Reply> news = replyApplication.search(command, page, size);
            List<Reply> list = news.getContent();
            if (news.getTotalElements() > 0L) {
                Paginated<Reply> total_configs = new Paginated<>(list, news.getTotalPages(), news.getSize(), news.getTotalElements());
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            } else {
                Paginated<Reply> total_configs = new Paginated<>(new ArrayList<>(), 0, 0, 0);
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            }
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
