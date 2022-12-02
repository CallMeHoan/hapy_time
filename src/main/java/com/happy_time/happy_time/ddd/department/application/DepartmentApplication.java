package com.happy_time.happy_time.ddd.department.application;

import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.ddd.configs.ip_config.model.IPConfig;
import com.happy_time.happy_time.ddd.department.model.Department;
import com.happy_time.happy_time.ddd.department.repository.IDepartmentRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartmentApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IDepartmentRepository iDepartmentRepository;

    public List<Department> getAll(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (StringUtils.isNotBlank(tenant_id)) {
            query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        }
        return mongoTemplate.find(query, Department.class);
    }

    public Department create(Department department) {
        Long current = System.currentTimeMillis();
        department.setCreated_date(current);
        department.setLast_updated_date(current);
        iDepartmentRepository.save(department);
        return department;
    }

    public Department update(Department department, String id) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(department.getTenant_id()));
        Boolean is_exists = mongoTemplate.exists(query, IPConfig.class);
        if(is_exists) {
            department.setLast_updated_date(current_time);
            return mongoTemplate.save(department, "department");
        }
        else return null;
    }

    public Department getById(ObjectId id) {
        Department department = mongoTemplate.findById(id, Department.class);
        if(department != null) {
            if (department.getIs_deleted()) return null;
            return department;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        Department department = mongoTemplate.findById(id, Department.class);
        if(department != null) {
            department.setIs_deleted(true);
            department.setLast_updated_date(current_time);
            department.getLast_updated_by().setAction(AppConstant.DELETE_ACTION);
            department.getLast_updated_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(department, "department");
            return true;
        } else return false;
    }
}
