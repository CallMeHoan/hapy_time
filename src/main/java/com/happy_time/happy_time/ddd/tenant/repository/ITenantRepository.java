package com.happy_time.happy_time.ddd.tenant.repository;

import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITenantRepository extends MongoRepository<Tenant, String> {
}
