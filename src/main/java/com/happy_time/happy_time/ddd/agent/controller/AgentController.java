package com.happy_time.happy_time.ddd.agent.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.jwt.JWTUtility;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/agent")
public class AgentController {
    
    @Autowired
    private AgentApplication agentApplication;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping("/search")
    public Optional<ResponseObject> search(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandSearchAgent command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            if (command == null) {
                throw new IllegalArgumentException("request_body_cant_be_null");
            }
            command.setTenant_id(tenant_id);
            Page<Agent> agents = agentApplication.search(command, page, size);
            List<Agent> list_agents = agents.getContent();
            if (agents.getTotalElements() > 0L) {
                Paginated<Agent> total_agents = new Paginated<>(list_agents, agents.getTotalPages(), agents.getSize(), agents.getTotalElements());
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_agents).build();
                return Optional.of(res);
            } else {
                Paginated<Agent> total_agents = new Paginated<>(new ArrayList<>(), 0, 0, 0);
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_agents).build();
                return Optional.of(res);
            }
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }

    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody Agent agent, @RequestHeader String token) throws Exception {
        String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
        String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
        if(StringUtils.isBlank(tenant_id)) {
            throw new IllegalArgumentException("missing_params");
        }
        ReferenceData ref = ReferenceData.builder()
                .agent_id(agent.get_id().toHexString())
                .updated_at(System.currentTimeMillis())
                .name(name)
                .action(AppConstant.CREATE_ACTION)
                .build();
        agent.setTenant_id(tenant_id);
        agent.setLast_update_by(ref);
        agent.setCreate_by(ref);
        Agent created = agentApplication.create(agent);
        if(created != null) {
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("create_agent_successfully").build();
            return Optional.of(res);
        } else {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("create_agent_failed").build();
            return Optional.of(res);
        }
    }

    @PutMapping("/update")
    public Optional<ResponseObject> edit(HttpServletRequest httpServletRequest, @RequestBody Agent agent) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent.get_id().toHexString())
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.UPDATE_ACTION)
                    .build();
            agent.setLast_update_by(ref);
            Agent edited = agentApplication.update(agent);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(edited).build();
            return Optional.of(res);
        }
        catch (Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @DeleteMapping("/remove/{id}")
    public Optional<ResponseObject> delete(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            Boolean is_deleted = agentApplication.delete(id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(is_deleted).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("delete_agent_failed").build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get/{id}")
    public Optional<ResponseObject> getById(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            Agent agent = agentApplication.getById(id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(agent).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
