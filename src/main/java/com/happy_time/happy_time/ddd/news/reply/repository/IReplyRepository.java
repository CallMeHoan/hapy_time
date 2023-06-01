package com.happy_time.happy_time.ddd.news.reply.repository;

import com.happy_time.happy_time.ddd.news.reply.Reply;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IReplyRepository extends MongoRepository<Reply, String> {
}
