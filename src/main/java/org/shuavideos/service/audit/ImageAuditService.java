package org.shuavideos.service.audit;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import org.shuavideos.config.QiNiuConfig;
import org.shuavideos.constant.AuditStatus;
import org.shuavideos.entity.Setting;
import org.shuavideos.entity.json.*;
import org.shuavideos.entity.response.AuditResponse;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ImageAuditService extends AbstractAuditService<String, AuditResponse>{
    static String imageUrl = "http://ai.qiniuapi.com/v3/image/censor";
    static String imageBody = "{\n" +
            "    \"data\": {\n" +
            "        \"uri\": \"${url}\"\n" +
            "    },\n" +
            "    \"params\": {\n" +
            "        \"scenes\": [\n" +
            "            \"pulp\",\n" +
            "            \"terror\",\n" +
            "            \"politician\"\n" +
            "        ]\n" +
            "    }\n" +
            "}";;

    @Override
    public AuditResponse audit(String url) {
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setAuditStatus(AuditStatus.SUCCESS);
        if (!isNeedAudit()) {
            return auditResponse;
        }
        try {
            if(!url.contains(QiNiuConfig.CNAME)) {
                String encodedFileName = URLEncoder.encode(url, "utf-8").replace("+", "%20");
                url = String.format("%s/%s", QiNiuConfig.CNAME, encodedFileName);
            }
            url = appendUUID(url);
            String body = imageBody.replace("${url}", url);
            String method = "POST";
            // 获取token
            final String token = qiNiuConfig.getToken(imageUrl, method, body, contentType);
            StringMap header = new StringMap();
            header.put("Host", "ai.qiniuapi.com");
            header.put("Authorization", token);
            header.put("Content-Type", contentType);
            Configuration cfg = new Configuration(Region.region2());
            final Client client = new Client(cfg);
            Response response = client.post(imageUrl, body.getBytes(), header, contentType);
            final Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            final ResultChildJson result = objectMapper.convertValue(map.get("result"), ResultChildJson.class);
            final BodyJson bodyJson = new BodyJson();
            final ResultJson resultJson = new ResultJson();
            resultJson.setResult(result);
            bodyJson.setResult(resultJson);

            final Setting setting = settingService.getById(1);
            final SettingScoreJson settingScoreRule = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);

            final List<ScoreJson> auditRule = Arrays.asList(settingScoreRule.getManualScore(), settingScoreRule.getPassScore(), settingScoreRule.getSuccessScore());
            // 审核
            auditResponse = audit(auditRule, bodyJson);

        } catch (Exception e) {
            auditResponse.setAuditStatus(AuditStatus.SUCCESS);
            e.printStackTrace();
        }

        return auditResponse;

    }
}
