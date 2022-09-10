package com.happy_time.happy_time.ddd.agent.repository;

import com.happy_time.happy_time.ddd.agent.model.Agent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAgentRepository extends MongoRepository<Agent, String> {

}
