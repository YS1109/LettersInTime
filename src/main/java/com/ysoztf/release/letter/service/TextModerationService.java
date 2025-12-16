package com.ysoztf.release.letter.service;

import com.aliyun.green20220302.models.TextModerationPlusResponseBody;

/**
 * 文本内容审核接口。
 *
 * 后续如果接入不同厂商（阿里云、腾讯云等），
 * 可以各自实现该接口，方便通过 Spring 容器进行替换或扩展。
 */
public interface TextModerationService {

    /**
     * 按业务规则判断一段文本内容是否通过审核。
     *
     * @param content 待审核文本
     * @return true 表示通过；false 表示未通过或调用失败
     */
    boolean isContentPass(String content);
}


