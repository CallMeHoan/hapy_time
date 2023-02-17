package com.happy_time.happy_time.ddd.department.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.department.Department;
import com.happy_time.happy_time.ddd.department.DepartmentView;
import com.happy_time.happy_time.ddd.department.command.CommandDepartment;
import com.happy_time.happy_time.ddd.department.repository.IDepartmentRepository;
import com.happy_time.happy_time.ddd.position.Position;
import com.happy_time.happy_time.ddd.position.PositionView;
import com.happy_time.happy_time.ddd.position.application.PositionApplication;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
        if (CollectionUtils.isEmpty(command.getPositions())) {
            throw new Exception(ExceptionMessage.MISSING_POSITION);
        }
        Department parent = null;
        if (StringUtils.isNotBlank(command.getDepartment_parent_id())) {
            parent = this.getById(command.getDepartment_parent_id());
        }
        Position contain_manager = command.getPositions().stream().filter(i -> BooleanUtils.isTrue(i.getIs_manager())).findFirst().orElse(null);
        if (contain_manager == null) {
            throw new Exception(ExceptionMessage.NEED_AT_LEAST_ONE_MANAGER);
        }
        this.checkExist(command.getName(), command.getTenant_id());
        positionApplication.checkExist(
                command.getPositions().stream().map(Position::getPosition_name).collect(Collectors.toList()),
                command.getTenant_id());

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
                position.setTenant_id(command.getTenant_id());
            }
            List<Position> list_positions = positionApplication.addMany(command.getPositions());
            if (!CollectionUtils.isEmpty(list_positions)) {
                //update lại Department
                List<String> children_ids = list_positions.stream().map(i -> i.get_id().toHexString()).collect(Collectors.toList());
                res.setPosition_ids(children_ids);
                iDepartmentRepository.save(res);
                //thêm parent id vào department cha
                if (parent != null) {
                    List<String> child_department_ids = parent.getDepartment_children_ids();
                    if (CollectionUtils.isEmpty(child_department_ids)) {
                        parent.setDepartment_children_ids(List.of(res.get_id().toHexString()));
                    } else {
                        child_department_ids.add(res.get_id().toHexString());
                        parent.setDepartment_children_ids(child_department_ids);
                    }
                    iDepartmentRepository.save(parent);
                }
                return res;
            }
        }
        return null;
    }

    public Boolean delete(String id, ReferenceData last_updated_by) throws Exception {
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
                    Department parent = this.getById(res.getDepartment_parent_id());
                    List<String> children_ids = res.getPosition_ids();
                    if (parent != null) {
                        parent.getDepartment_children_ids().remove(res.get_id().toHexString());
                        iDepartmentRepository.save(parent);
                    }
                    if (!CollectionUtils.isEmpty(children_ids)) {
                        return positionApplication.deleteMany(children_ids, department.getTenant_id(), last_updated_by);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Department getById(String id){
        return iDepartmentRepository.findById(id).orElse(null);
    }

    private void checkExist(String name, String tenant_id) throws Exception{
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("department_name").regex(name,"i"));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (mongoTemplate.exists(query, Department.class)) {
            throw new Exception(ExceptionMessage.DEPARTMENT_NAME_EXISTS);
        }
    }

    public DepartmentView getDepartmentOfTenant(String tenant_id) throws Exception {
        if (StringUtils.isBlank(tenant_id)) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        Query queryDepartment = new Query();
        queryDepartment.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        queryDepartment.addCriteria(Criteria.where("is_deleted").is(false));
        List<DepartmentView.Item> res = new ArrayList<>();
        List<Department> departments = mongoTemplate.find(queryDepartment, Department.class);
        Integer total_position = 0;
        Integer total_department = departments.size();
        if (!CollectionUtils.isEmpty(departments)) {
            for (Department department: departments) {
                if (!CollectionUtils.isEmpty(department.getPosition_ids())) {
                    List<Position> child_position = positionApplication.getByIds(department.getPosition_ids());
                    total_position += child_position.size();
                    List<PositionView> position_views = new ArrayList<>();
                    for (Position child : child_position) {
                        position_views.add(positionApplication.setView(child));
                    }
                    DepartmentView.Item item = DepartmentView.Item.builder()
                            .id(department.get_id().toHexString())
                            .department_name(department.getDepartment_name())
                            .position_ids(department.getPosition_ids())
                            .department_children_ids(department.getDepartment_children_ids())
                            .department_parent_id(department.getDepartment_parent_id())
                            .children_position(position_views)
                            .build();
                    res.add(item);
                }
            }
        }

        return DepartmentView.builder()
                .total_department(total_department)
                .total_position(total_position)
                .items(res)
                .build();
    }
}
