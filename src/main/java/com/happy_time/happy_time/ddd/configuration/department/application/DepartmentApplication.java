package com.happy_time.happy_time.ddd.configuration.department.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.configuration.department.Department;
import com.happy_time.happy_time.ddd.configuration.department.command.CommandDepartment;
import com.happy_time.happy_time.ddd.configuration.department.repository.IDepartmentRepository;
import com.happy_time.happy_time.ddd.configuration.position.Position;
import com.happy_time.happy_time.ddd.configuration.position.application.PositionApplication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class DepartmentApplication {
    @Autowired
    private IDepartmentRepository iDepartmentRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private PositionApplication positionApplication;

    public Department create(CommandDepartment command) throws Exception {
        if (StringUtils.isEmpty(command.getTenant_id())) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        if (StringUtils.isBlank(command.getName())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String department_name_unsigned = HAPStringUtils.stripAccents(command.getName()).toLowerCase(Locale.ROOT);
        Long current_time = System.currentTimeMillis();
        Department department = Department.builder()
                .tenant_id(command.getTenant_id())
                .department_name(command.getName())
                .create_by(command.getCreated_by())
                .last_update_by(command.getLast_updated_by())
                .created_date(current_time)
                .last_updated_date(current_time)
                .department_parent_id(command.getDepartment_parent_id())
                .department_name_unsigned(department_name_unsigned)
                .build();
        Department res = iDepartmentRepository.insert(department);

        if (!CollectionUtils.isEmpty(command.getPositions())) {
            for (Position position : command.getPositions()) {
                position.setCreate_by(command.getCreated_by());
                position.setLast_update_by(command.getLast_updated_by());
                position.setLast_updated_date(current_time);
                position.setCreated_date(current_time);
                position.setDepartment_id(res.get_id().toHexString());
            }
            List<Position> list_positions = positionApplication.addMany(command.getPositions());
            if (!CollectionUtils.isEmpty(list_positions)) {
                //update lại Department
                List<String> children_ids = list_positions.stream().map(i -> i.get_id().toHexString()).collect(Collectors.toList());
                res.setDepartment_children_ids(children_ids);
                return iDepartmentRepository.save(res);
            }
        }
        return null;
    }

    public Department delete(String id, ReferenceData last_updated_by) throws Exception {
        if (StringUtils.isBlank(id)) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Department department = this.getById(id);
        if (department != null) {
            if (StringUtils.isBlank(department.getDepartment_parent_id())) {
                throw new Exception(ExceptionMessage.CAN_NOT_DELETE_DEFAULT_DEPARTMENT);
            } else {
                if (!CollectionUtils.isEmpty(department.getDepartment_children_ids())){
                    throw new Exception(ExceptionMessage.CAN_NOT_DELETE_DEPARTMENT_HAS_CHILDREN);
                } else {
                    department.setIs_deleted(true);
                    department.setLast_update_by(last_updated_by);
                    department.setLast_updated_date(System.currentTimeMillis());
                    Department res = iDepartmentRepository.save(department);
                    List<String> children_ids = res.getPosition_ids();
                    if (!CollectionUtils.isEmpty(children_ids)) {
                        Boolean deleted = positionApplication.deleteMany(children_ids, department.getTenant_id(), last_updated_by);
                        if (deleted) {
                            return res;
                        }
                    }

                }
            }
        }
        return null;
    }

    public Department getById(String id){
        return iDepartmentRepository.findById(id).orElse(null);
    }
}
