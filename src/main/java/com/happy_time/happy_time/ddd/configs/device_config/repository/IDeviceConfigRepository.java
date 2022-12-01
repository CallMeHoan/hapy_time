package com.happy_time.happy_time.ddd.configs.device_config.repository;

import com.happy_time.happy_time.ddd.configs.device_config.model.DeviceConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDeviceConfigRepository extends MongoRepository<DeviceConfig, String> {
}
