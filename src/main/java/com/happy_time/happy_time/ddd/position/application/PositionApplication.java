package com.happy_time.happy_time.ddd.position.application;

import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.position.Position;
import com.happy_time.happy_time.ddd.position.PositionView;
import com.happy_time.happy_time.ddd.position.repository.IPositionRepository;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class PositionApplication {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IPositionRepository iPositionRepository;

    public List<Position> addMany(List<Position> positions) throws Exception {
        //validate đầu vào
        if (CollectionUtils.isEmpty(positions)) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        for (Position pos : positions) {
            if (StringUtils.isEmpty(pos.getDepartment_id())) {
                throw new Exception(ExceptionMessage.MISSING_DEPARTMENT_ID);
            }
            if (StringUtils.isEmpty(pos.getPosition_name())){
                throw new Exception(ExceptionMessage.MISSING_NAME);
            }
        }
        return iPositionRepository.saveAll(positions);
    }

    public Boolean deleteMany(List<String> ids, String tenant_id, ReferenceData ref) throws Exception {
        if(CollectionUtils.isEmpty(ids)){
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(tenant_id)) {
            query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        }
        query.addCriteria(Criteria.where("_id").in(ids));
        List<Position> positions = mongoTemplate.find(query, Position.class);
        if (!CollectionUtils.isEmpty(positions)) {
            for (Position position : positions) {
                position.setIs_deleted(true);
                position.setLast_updated_date(System.currentTimeMillis());
                position.setLast_update_by(ref);
            }
            iPositionRepository.saveAll(positions);
        }
        return false;
    }

    public void checkExist(List<String> position_names, String tenant_id) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("position_name").in(position_names));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (mongoTemplate.exists(query, Position.class)) {
            throw new Exception(ExceptionMessage.POSITION_NAME_EXISTS);
        }
    }

    public List<Position> getByIds(List<String> ids) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(ids));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        return mongoTemplate.find(query, Position.class);
    }

    public PositionView setView(Position position) {
        return PositionView.builder()
                .id(position.get_id().toHexString())
                .position_name(position.getPosition_name())
                .is_manager(position.getIs_manager())
                .department_id(position.getDepartment_id())
                .position_code(position.getPosition_code())
                .build();
    }

}
