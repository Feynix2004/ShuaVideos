package org.shuavideos.config;


import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qiniu.kodo")
public class QiNiuConfig {
    /**
     * 账号
     */
    private String accessKey;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * bucketName
     */
    private String bucketName;

    public static final String CNAME = "http://syb6a4s6k.hb-bkt.clouddn.com";
    public static final String VIDEO_URL = "http://ai.qiniuapi.com/v3/video/censor";
    public static final String IMAGE_URL = "http://ai.qiniuapi.com/v3/image/censor";

    public static final String fops = "avthumb/mp4";

    public Auth buildAuth() {
        String accessKey = this.getAccessKey();
        String secretKey = this.getSecretKey();
        return Auth.create(accessKey, secretKey);
    }

    public String uploadToken(String type){
        final Auth auth = buildAuth();
        return auth.uploadToken(bucketName, null, 300, new
                StringMap().put("mimeLimit", "video/*;image/*"));
    }


    public String getToken(String url, String method, String body, String contentType) {

        final Auth auth = buildAuth();
        String qiniuToken = "Qiniu " + auth.signQiniuAuthorization(url, method, body == null ? null : body.getBytes(), contentType);
        return qiniuToken;
    }
}
