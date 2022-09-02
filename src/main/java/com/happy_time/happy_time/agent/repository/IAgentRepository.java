package com.happy_time.happy_time.agent.repository;
import com.happy_time.happy_time.agent.model.Agent;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAgentRepository extends MongoRepository<Agent, String> {

    @Query(value = "{?0}")
    List<Agent> search(String query);

}
