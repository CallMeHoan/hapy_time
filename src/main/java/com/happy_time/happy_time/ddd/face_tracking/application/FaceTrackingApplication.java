package com.happy_time.happy_time.ddd.face_tracking.application;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.device.Device;
import com.happy_time.happy_time.ddd.device.command.CommandDevice;
import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.command.CommandFaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.repository.IFaceTrackingRepository;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FaceTrackingApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IFaceTrackingRepository iFaceTrackingRepository;

    @Autowired
    private AgentApplication agentApplication;

    public static String URL = "https://facerecognitionapi-production-5a10.up.railway.app";

    public Page<FaceTracking> search(CommandFaceTracking command, Integer page, Integer size) throws Exception {
        List<FaceTracking> faceTrackings = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getAgent_id())) {
            query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        }
        Long total = mongoTemplate.count(query, Device.class);
        if (total >= 0) {
            query.with(Sort.by(Sort.Direction.DESC, "_id"));
            faceTrackings = mongoTemplate.find(query.with(pageRequest), FaceTracking.class);
        }
        return PageableExecutionUtils.getPage(
                faceTrackings,
                pageRequest,
                () -> total);
    }

    private Query queryBuilder(CommandFaceTracking command) throws Exception {
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if (StringUtils.isNotBlank(command.getAgent_id())) {
            query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        }
        return query;
    }

    public FaceTracking create(FaceTracking faceTracking) throws Exception {
        if(CollectionUtils.isEmpty(faceTracking.getFace_tracking_images()) || StringUtils.isBlank(faceTracking.getAgent_id())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Agent agent = agentApplication.getById(new ObjectId(faceTracking.getAgent_id()));
        if (agent == null) {
            throw new Exception(ExceptionMessage.AGENT_NOT_EXIST);
        }

        //check xem nhân viên đó đã có face tracking hay chưa
        CommandFaceTracking command = CommandFaceTracking.builder()
                .tenant_id(faceTracking.getTenant_id())
                .agent_id(faceTracking.getAgent_id())
                .build();
        Query query = this.queryBuilder(command);
        long total = mongoTemplate.count(query, FaceTracking.class);
        if (total > 0) {
            throw new Exception("Nhân viên đã tồn tại Face Tracking");
        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("image_urls", faceTracking.getFace_tracking_images());

        String json_body = JsonUtils.toJSON(map);
        String url = URL + "/check/images";
        String res = this.callApi(url, json_body);
        if (StringUtils.isBlank(res)) {
            throw new Exception("Có lỗi xảy ra");
        }
        ResponseObject responseObject = JsonUtils.toObject(res, ResponseObject.class);

        return null;

    }

    private String callApi(String url, String json_body) throws IOException {
        //Gọi api sang bên app python để check dữ liệu khuôn mặt
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json_body);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();

        String str = "";
        ResponseBody res = response.body();
        if (res != null) {
            try {
                if (response.isSuccessful()) {
                    str = res.string();
                } else {
                    System.out.println(response);
                }
            } finally {
                res.close();
            }
        }
        response.close();
        return str;
    }
}

