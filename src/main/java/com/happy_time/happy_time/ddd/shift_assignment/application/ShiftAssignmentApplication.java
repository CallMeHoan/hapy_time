package com.happy_time.happy_time.ddd.shift_assignment.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_assignment.command.CommandShiftAssignment;
import com.happy_time.happy_time.ddd.shift_assignment.repository.IShiftAssignmentRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ShiftAssignmentApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IShiftAssignmentRepository iShiftAssignmentRepository;

    public Page<ShiftAssignment> search(CommandShiftAssignment command, Integer page, Integer size) throws Exception {
        List<ShiftAssignment> ShiftAssignmentList = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getKeyword())) {
            query.addCriteria(Criteria.where("name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        ShiftAssignmentList = mongoTemplate.find(query, ShiftAssignment.class);
        return PageableExecutionUtils.getPage(
                ShiftAssignmentList,
                pageRequest,
                () -> mongoTemplate.count(query, ShiftAssignment.class));
    }

    public ShiftAssignment create(ShiftAssignment shift) throws Exception {
        if (StringUtils.isBlank(shift.getName())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(shift.getName()).toLowerCase(Locale.ROOT);
        shift.setName_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        shift.setCreated_at(current);
        shift.setLast_updated_at(current);
        iShiftAssignmentRepository.insert(shift);
        return shift;
    }

    public ShiftAssignment update(CommandShiftAssignment command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        ShiftAssignment config = mongoTemplate.findOne(query, ShiftAssignment.class);
        if(config != null) {
            if (StringUtils.isBlank(command.getName())) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getName()).toLowerCase(Locale.ROOT);
            config.setLast_updated_at(current_time);
            config.setName_unsigned(name_unsigned);
            config.setName(command.getName());
            config.setLast_update_by(command.getLast_update_by());
            return mongoTemplate.save(config, "shift_assignment");
        }
        else return null;
    }

    public ShiftAssignment getById(ObjectId id) {
        ShiftAssignment shift = mongoTemplate.findById(id, ShiftAssignment.class);
        if(shift != null) {
            if (shift.getIs_deleted()) return null;
            return shift;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        ShiftAssignment shift = mongoTemplate.findById(id, ShiftAssignment.class);
        if(shift != null) {
            shift.setIs_deleted(true);
            shift.setLast_updated_at(current_time);
            shift.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            shift.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(shift, "shift_assignment");
            return true;
        } else return false;
    }
}
