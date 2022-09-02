package com.happy_time.happy_time.agent.application;

import com.happy_time.happy_time.agent.model.Agent;
import com.happy_time.happy_time.agent.repository.IAgentRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentApplication {
    @Autowired
    private IAgentRepository iAgentRepository;
    public List<Agent> search() {
        Document query = new Document();
        query.put("name", "hahaha");
        String queryString = query.toString();
        List<Agent> agents = iAgentRepository.search(queryString);
        return agents;

    }
}
