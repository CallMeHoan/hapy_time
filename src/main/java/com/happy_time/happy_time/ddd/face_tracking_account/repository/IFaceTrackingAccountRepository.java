package com.happy_time.happy_time.ddd.face_tracking_account.repository;

import com.happy_time.happy_time.ddd.face_tracking_account.FaceTrackingAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFaceTrackingAccountRepository extends MongoRepository<FaceTrackingAccount, String> {
}
