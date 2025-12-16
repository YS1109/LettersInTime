package com.ysoztf.release.letter.config;

import com.aliyun.green20220302.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云内容安全（Green）账号与接入配置 & Client Bean 定义
 *
 * 配置示例（application-*.yml）：
 *
 * aliyun:
 *   green:
 *     access-key-id: your_ak
 *     access-key-secret: your_sk
 *     region-id: cn-shanghai
 *     endpoint: green-cip.cn-shanghai.aliyuncs.com
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.green")
@Data
public class AliyunGreenProperties {

    /**
     * RAM 用户 AccessKey ID
     */
    private String accessKeyId;

    /**
     * RAM 用户 AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 接入地域 ID，默认 cn-shanghai
     */
    private String regionId = "cn-shanghai";

    /**
     * 接入域名，默认 green-cip.cn-shanghai.aliyuncs.com
     */
    private String endpoint = "green-cip.cn-shanghai.aliyuncs.com";

    /**
     * 阿里云 Green Client Bean
     */
    @Bean
    public Client aliyunGreenClient() throws Exception {
        String ak = this.accessKeyId;
        String sk = this.accessKeySecret;

        if (ak == null || ak.isEmpty()) {
            ak = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        }
        if (sk == null || sk.isEmpty()) {
            sk = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        }

        Config config = new Config();
        config.setAccessKeyId(ak);
        config.setAccessKeySecret(sk);
        config.setRegionId(this.regionId);
        config.setEndpoint(this.endpoint);
        config.setReadTimeout(6000);
        config.setConnectTimeout(3000);

        return new Client(config);
    }
}

