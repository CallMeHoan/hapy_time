package com.happy_time.happy_time.ddd.face_tracking.repository;

import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFaceTrackingRepository extends MongoRepository<FaceTracking, String> {
}
