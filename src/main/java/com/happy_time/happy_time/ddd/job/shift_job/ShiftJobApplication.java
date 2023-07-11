package com.happy_time.happy_time.ddd.job.shift_job;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_assignment.service.ShiftAssignmentService;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.ShiftResultJobData;
import com.happy_time.happy_time.ddd.shift_result.service.ShiftResultService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShiftJobApplication {
    @Autowired
    private ShiftAssignmentService shiftAssignmentService;
    @Autowired
    private JobApplication jobApplication;
    @Autowired
    private ShiftResultService shiftResultService;
    @Autowired
    private AgentApplication agentApplication;
    protected final Log logger = LogFactory.getLog(this.getClass());
    public void executeJob(JobModel jobModel) {
        if (jobModel == null) {
            return;
        }
        if (BooleanUtils.isTrue(jobModel.getExecuted())) {
            logger.error("ShiftResultApplication job executed:" + jobModel.get_id().toHexString());
            return;
        }
        if (StringUtils.isBlank(jobModel.getTenant_id())) {
            logger.error("ShiftResultApplication missing tenant_id:" + jobModel.get_id().toHexString());
            return;
        }
        String current_date = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis());

        if (!current_date.equals(jobModel.getExecuted_time())) {
            logger.error("ShiftResultApplication not in executed time:" + jobModel.get_id().toHexString());
            return;
        }

        if (StringUtils.isBlank(jobModel.getJob_data())) {
            logger.error("ShiftResultApplication job data null:" + jobModel.get_id().toHexString());
            return;
        }

        ShiftResultJobData data = JsonUtils.jsonToObject(jobModel.getJob_data(), ShiftResultJobData.class);
        if (data == null) {
            logger.error("ShiftResultApplication job data null:" + jobModel.get_id().toHexString());
            return;
        }

        if (CollectionUtils.isEmpty(data.getAgent_ids())) {
            logger.error("ShiftResultApplication no agents found:" + jobModel.get_id().toHexString());
            return;
        }

        if (StringUtils.isBlank(data.getShift_assigned_id())) {
            logger.error("ShiftResultApplication missing shift assignment:" + jobModel.get_id().toHexString());
            return;
        }

        //xử lý sau khi bypass all
        ShiftAssignment config = shiftAssignmentService.getById(data.getShift_assigned_id());
        if (config == null) {
            logger.error("ShiftResultApplication no config found:" + jobModel.get_id().toHexString());
            return;
        }

        if (config.getDay_range() != null) {
            if (CollectionUtils.isEmpty(config.getDay_range().getDays())) {
                logger.error("ShiftResultApplication no days in config:" + jobModel.get_id().toHexString());
                return;
            }
            //set ca + set job cho ngày tíếp theo
            //check xem ngày hiện tại có nằm trong config không nếu có thì tạo không thì bỏ qua
            Long current = System.currentTimeMillis();
            Integer current_day = DateTimeUtils.getDayOfWeek(current);
            if (!config.getDay_range().getDays().contains(current_day)) {
                logger.info("ShiftResultApplication current day do not have in config:" + jobModel.get_id().toHexString());
                return;
            }

            String date_execute = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, current);
            if (!CollectionUtils.isEmpty(config.getDay_range().getShift_ids())) {
                ShiftResult.Shift s = ShiftResult.Shift.builder()
                        .shift_schedule_ids(config.getDay_range().getShift_ids())
                        .date(date_execute)
                        .build();
                List<ShiftResult> results = new ArrayList<>();
                //check thêm để xem có nhân viên mới không và tạo job
                List<Agent> agents = switch (config.getApply_for()) {
                    case "company" -> agentApplication.getByTenant(config.getTenant_id());
                    case "agent" -> agentApplication.getByIds(config.getAgents());
                    case "department" -> agentApplication.getByDepartmentIds(config.getDepartments());
                    case "position" -> agentApplication.getByPositionIds(config.getPositions());
                    default -> new ArrayList<>();
                };
                List<String> agent_ids = agents.stream().map(i -> i.get_id().toHexString()).toList();
                for (String id : agent_ids) {
                    ShiftResult res = ShiftResult.builder()
                            .tenant_id(config.getTenant_id())
                            .shift_assigned_id(config.get_id().toHexString())
                            .agent_id(id)
                            .create_by(config.getCreate_by())
                            .last_update_by(config.getLast_update_by())
                            .created_at(current)
                            .last_updated_at(current)
                            .shift(s)
                            .build();
                    results.add(res);
                }
                shiftResultService.create(results);
            }

            //check tới ngày ngừng thực hiện hay chưa
            if (config.getDay_range().getTo() != null && config.getDay_range().getTo() != 0 && config.getDay_range().getTo() < current) {
                logger.info("ShiftResultApplication end of execution" + jobModel.get_id().toHexString());
                return;
            }

            //tính ngày thực hiện tiếp theo rồi thêm job

            String next_day = this.calculateNextExecuteTime(config.getDay_range());
            if (StringUtils.isNotBlank(next_day)) {
                //nếu thỏa rồi thì tạo job
                ShiftResultJobData new_job_data = ShiftResultJobData.builder()
                        .agent_ids(data.getAgent_ids())
                        .tenant_id(config.getTenant_id())
                        .ref_data(config.getCreate_by())
                        .shift_assigned_id(config.get_id().toHexString())
                        .build();
                String job_data = JsonUtils.toJSON(new_job_data);
                //build job data để add
                JobModel job = JobModel.builder()
                        .action(JobAction.set_shift_result)
                        .executed_time(next_day)
                        .tenant_id(config.getTenant_id())
                        .created_at(System.currentTimeMillis())
                        .last_updated_at(System.currentTimeMillis())
                        .job_data(job_data)
                        .build();
                jobApplication.setJob(job);
            } else {
                logger.info("ShiftResultApplication end of execution");
            }
        }
    }

    public String calculateNextExecuteTime(ShiftAssignment.DayRange config) {
        Long current = System.currentTimeMillis();
        String res = "";
        //vô thời hạn
        if (config.getFrom() < current && (config.getTo() == null || config.getTo() == 0)) {
            Long time_stamp = current;
            int retry = 0;
            while (true) {
                if (retry == 6) {
                    logger.error("calculateNextExecuteTime retry 6 time => failed");
                    break;
                }
                time_stamp += 3600 * 24 * 1000;
                Integer day_of_week = DateTimeUtils.getDayOfWeek(time_stamp);
                if (config.getDays().contains(day_of_week) && config.getFrom() < time_stamp) {
                    res = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, time_stamp);
                    return res;
                }

                retry += 1;
            }
        } else if (config.getFrom() < current && current < config.getTo()) {
            Long time_stamp = current;
            int retry = 0;
            while (true) {
                if (retry == 6) {
                    logger.error("calculateNextExecuteTime retry 6 time => failed");
                    break;
                }
                time_stamp += 3600 * 24 * 1000;
                Integer day_of_week = DateTimeUtils.getDayOfWeek(time_stamp);
                if (config.getDays().contains(day_of_week) && config.getFrom() < time_stamp && time_stamp < config.getTo()) {
                    res = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, time_stamp);
                    return res;
                }

                retry += 1;
            }
        }

        return res;
    }
}
