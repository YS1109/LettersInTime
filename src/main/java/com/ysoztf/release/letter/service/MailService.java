package com.ysoztf.release.letter.service;

public interface MailService {

    /**
     * 发送纯文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（纯文本）
     */
    void sendSimpleMail(String to, String subject, String content);
}


