package com.happy_time.happy_time.entities.agent.repository;
import com.happy_time.happy_time.entities.agent.model.Agent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAgentRepository extends MongoRepository<Agent, String> {

}
