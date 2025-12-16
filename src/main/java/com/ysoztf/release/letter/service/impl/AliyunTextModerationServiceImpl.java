package com.ysoztf.release.letter.service.impl;

import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.TextModerationPlusRequest;
import com.aliyun.green20220302.models.TextModerationPlusResponse;
import com.aliyun.green20220302.models.TextModerationPlusResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysoztf.release.letter.service.TextModerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基于阿里云内容安全（Green）的文本审核 Service
 *
 * 优先从配置文件读取 AK/SK，其次从环境变量读取：
 * - 配置属性：aliyun.green.access-key-id / access-key-secret / region-id / endpoint
 * - 环境变量：ALIBABA_CLOUD_ACCESS_KEY_ID / ALIBABA_CLOUD_ACCESS_KEY_SECRET
 */
@Service
@Slf4j
public class AliyunTextModerationServiceImpl implements TextModerationService {

    private final Client client;
    private final ObjectMapper objectMapper;

    public AliyunTextModerationServiceImpl(Client client,
                                           ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * 审核一段较长文本，按 550 个字符一段切分，多次调用审核接口。
     * 只要有任一分段未通过（或调用失败），就视为审核不通过。
     *
     * @param content 待审核内容
     * @return true 表示全部分段通过审核；false 表示有分段未通过或调用失败
     */
    @Override
    public boolean isContentPass(String content) {
        if (content == null || content.isEmpty()) {
            return true;
        }

        int chunkSize = 550;
        int length = content.length();
        int offset = 0;

        while (offset < length) {
            int end = Math.min(offset + chunkSize, length);
            String chunk = content.substring(offset, end);

            try {
                // 构造 serviceParameters
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("content", chunk);

                String serviceParametersJson = objectMapper.writeValueAsString(params);

                TextModerationPlusRequest request = new TextModerationPlusRequest();
                // 检测类型，可根据实际业务调整
                request.setService("ugc_moderation_byllm");
                request.setServiceParameters(serviceParametersJson);

                TextModerationPlusResponse response = client.textModerationPlus(request);
                if (response.getStatusCode() == 200) {
                    TextModerationPlusResponseBody result = response.getBody();
                    Integer code = result.getCode();
                    if (Integer.valueOf(200).equals(code)) {
                        TextModerationPlusResponseBody.TextModerationPlusResponseBodyData data = result.getData();
                        String level = data.getRiskLevel();
                        if (!"low".equals(level) && !"none".equals(level)) {
                            return false;
                        }
                    } else {
                        log.warn("Aliyun text moderation not success. code={}, msg={}", code, result.getMessage());
                        return false;
                    }
                } else {
                    log.warn("Aliyun text moderation response not success. httpStatus={}", response.getStatusCode());
                    return false;
                }
            } catch (Exception e) {
                log.error("Aliyun text moderation request error", e);
                return false;
            }

            offset = end;
        }
        return true;
    }
}


