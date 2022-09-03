package com.happy_time.happy_time.entities.agent.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.entities.agent.application.AgentApplication;
import com.happy_time.happy_time.entities.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.entities.agent.model.Agent;
import com.happy_time.happy_time.common.Paginated;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/agent")
public class AgentController {
    
    @Autowired
    private AgentApplication agentApplication;

    @GetMapping("/search")
    public Optional<ResponseObject> search(@RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandSearchAgent command) {
        try {
            if (command == null) {
                throw new IllegalArgumentException("request_body_cant_be_null");
            }
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
            throw new IllegalStateException(e);
        }

    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(@RequestBody Agent agent) {
        Boolean created = agentApplication.create(agent);
        if(created) {
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("create_agent_successfully").build();
            return Optional.of(res);
        } else {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("create_agent_failed").build();
            return Optional.of(res);
        }
    }

    @PutMapping("/edit")
    public Optional<ResponseObject> edit(@RequestBody Agent agent) {
        Boolean edited = agentApplication.edit(agent);
        if(edited) {
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("edit_agent_successfully").build();
            return Optional.of(res);
        } else {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("edit_agent_failed").build();
            return Optional.of(res);
        }
    }

    @DeleteMapping("/remove/{id}")
    public Optional<ResponseObject> delete(@PathVariable ObjectId id) {
        Boolean is_deleted = agentApplication.delete(id);
        if(is_deleted) {
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("delete_agent_successfully").build();
            return Optional.of(res);
        } else {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("delete_agent_failed").build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get/{id}")
    public Optional<ResponseObject> getById(@PathVariable ObjectId id) {
        Agent agent = agentApplication.getById(id);
        if(agent != null) {
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(agent).build();
            return Optional.of(res);
        } else {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("cant_find_this_agent").build();
            return Optional.of(res);
        }
    }
}
