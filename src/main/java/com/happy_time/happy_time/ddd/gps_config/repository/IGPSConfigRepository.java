package com.happy_time.happy_time.ddd.gps_config.repository;

import com.happy_time.happy_time.ddd.gps_config.GPSConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGPSConfigRepository extends MongoRepository<GPSConfig, String> {
}
