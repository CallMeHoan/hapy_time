package com.happy_time.happy_time.agent.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.agent.application.AgentApplication;
import com.happy_time.happy_time.agent.model.Agent;
import com.happy_time.happy_time.common.Paginated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/agent")
public class AgentController {
    
    @Autowired
    private AgentApplication agentApplication;

    @GetMapping("/search")
    public Optional<ResponseObject> search() {
        List<Agent> agents = agentApplication.search();
        Paginated<Agent> page = new Paginated<>(agents, 0, 0, 100);
        ResponseObject res = ResponseObject.builder().status("9999").message("success").payload(page).build();
        return Optional.of(res);
    }
}
