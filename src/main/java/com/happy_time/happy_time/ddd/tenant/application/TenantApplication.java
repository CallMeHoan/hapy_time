package com.happy_time.happy_time.ddd.tenant.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import com.happy_time.happy_time.ddd.tenant.repository.ITenantRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantApplication {

    @Autowired
    private ITenantRepository iTenantRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<Tenant> create(CommandCreateTenant command) throws Exception {
        Long current_time = System.currentTimeMillis();
        if (StringUtils.isBlank(command.getCompany_name())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Tenant tenant = Tenant.builder()
                .company_name(command.getCompany_name())
                .company_shorthand(command.getCompany_shorthand())
                .scale(command.getScale())
                .is_deleted(false)
                .created_date(current_time)
                .last_updated_date(current_time)
                .website(command.getWebsite())
                .company_mail(command.getCompany_mail())
                .tax_number(command.getTax_number())
                .fanpage(command.getFanpage())
                .hotline(command.getHotline())
                .avatar(command.getAvatar())
                .build();

        Tenant created = iTenantRepository.save(tenant);
        return Optional.of(created);
    }

    public Tenant getById(ObjectId id) {
        Tenant tenant = mongoTemplate.findById(id, Tenant.class);
        if(tenant != null) {
            if (tenant.getIs_deleted()) return null;
            return tenant;
        } else return null;
    }

    public Tenant edit(Tenant tenant) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(tenant.get_id()));
        Tenant old_tenant = this.getById(new ObjectId(tenant.get_id().toHexString()));
        if(old_tenant != null) {
            Tenant updated = Tenant
                    .builder()
                    ._id(tenant.get_id())
                    .company_name(StringUtils.isNotBlank(tenant.getCompany_name()) ? tenant.getCompany_name() : old_tenant.getCompany_name())
                    .company_shorthand(StringUtils.isNotBlank(tenant.getCompany_shorthand()) ? tenant.getCompany_shorthand() : old_tenant.getCompany_shorthand())
                    .avatar(StringUtils.isNotBlank(tenant.getAvatar()) ? tenant.getAvatar() : old_tenant.getAvatar())
                    .scale(tenant.getScale() != null ? tenant.getScale() : old_tenant.getScale())
                    .status(tenant.getStatus() != null ? tenant.getStatus() : old_tenant.getStatus())
                    .company_mail(tenant.getCompany_mail() != null ? tenant.getCompany_mail() : old_tenant.getCompany_mail())
                    .website(tenant.getWebsite() != null ? tenant.getWebsite() :old_tenant.getWebsite())
                    .tax_number(tenant.getTax_number() != null ? tenant.getTax_number() :old_tenant.getTax_number())
                    .fanpage(tenant.getFanpage() != null ? tenant.getFanpage() :old_tenant.getFanpage())
                    .hotline(tenant.getHotline() != null ? tenant.getHotline() : old_tenant.getHotline())
                    .build();
            tenant.setLast_updated_date(current_time);
            return mongoTemplate.save(updated, "tenants");
        }
        else return null;
    }
}
