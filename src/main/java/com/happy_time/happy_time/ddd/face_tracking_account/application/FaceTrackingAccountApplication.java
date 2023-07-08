package com.happy_time.happy_time.ddd.face_tracking_account.application;

import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.device.Device;
import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.application.FaceTrackingApplication;
import com.happy_time.happy_time.ddd.face_tracking.command.CommandFaceTracking;
import com.happy_time.happy_time.ddd.face_tracking_account.FaceTrackingAccount;
import com.happy_time.happy_time.ddd.face_tracking_account.command.CommandFaceTrackingAccount;
import com.happy_time.happy_time.ddd.face_tracking_account.repository.IFaceTrackingAccountRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class FaceTrackingAccountApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IFaceTrackingAccountRepository iFaceTrackingAccountRepository;

    public FaceTrackingAccount searchOne(CommandFaceTrackingAccount command) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if (StringUtils.isNotBlank(command.getUser_name())) {
            query.addCriteria(Criteria.where("user_name").is(command.getUser_name()));
        }
        if (StringUtils.isNotBlank(command.getPassword())) {
            query.addCriteria(Criteria.where("password").is(command.getPassword()));
        }
        return mongoTemplate.findOne(query, FaceTrackingAccount.class);
    }

    public FaceTrackingAccount upsert(CommandFaceTrackingAccount command) throws Exception {
        if (StringUtils.isBlank(command.getTenant_id()) || StringUtils.isBlank(command.getUser_name()) || StringUtils.isBlank(command.getPassword())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Long current = System.currentTimeMillis();
        CommandFaceTrackingAccount commandFaceTrackingAccount = CommandFaceTrackingAccount.builder()
                .tenant_id(command.getTenant_id())
                .build();
        //check tài khoản đã tồn tại hay chưa
        FaceTrackingAccount account = this.searchOne(commandFaceTrackingAccount);
        if (account == null) {
            FaceTrackingAccount create = FaceTrackingAccount.builder()
                    .create_by(command.getRef())
                    .last_update_by(command.getRef())
                    .user_name(command.getUser_name())
                    .password(command.getPassword())
                    .created_date(current)
                    .last_updated_date(current)
                    .build();
            return mongoTemplate.insert(create);
        }

        if (command.getUser_name().equals(account.getUser_name()) && !command.getPassword().equals(account.getPassword())) {
            account.setPassword(command.getPassword());
            account.setLast_update_by(command.getRef());
            account.setLast_updated_date(current);
            return mongoTemplate.save(account);
        }
        return null;
    }

    public FaceTrackingAccount getById(ObjectId id) {
        FaceTrackingAccount faceTracking = mongoTemplate.findById(id, FaceTrackingAccount.class);
        if(faceTracking != null) {
            if (faceTracking.getIs_deleted()) return null;
            return faceTracking;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        FaceTrackingAccount faceTracking = mongoTemplate.findById(id, FaceTrackingAccount.class);
        if(faceTracking != null) {
            faceTracking.setIs_deleted(true);
            faceTracking.setLast_updated_date(current_time);
            faceTracking.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            faceTracking.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(faceTracking, "face_tracking_account");
            return true;
        } else return false;
    }
}
