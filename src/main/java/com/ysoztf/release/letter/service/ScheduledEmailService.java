package com.ysoztf.release.letter.service;

import java.time.LocalDateTime;

public interface ScheduledEmailService {

    /**
     * 创建一条计划发送的邮件记录
     *
     * @param to            收件人邮箱
     * @param subject       邮件主题
     * @param content       邮件内容
     * @param scheduledTime 计划发送时间
     */
    void scheduleEmail(String to, String subject, String content, LocalDateTime scheduledTime);
}


