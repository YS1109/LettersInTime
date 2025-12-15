package com.ysoztf.release.letter.service;

public interface MailService {

    /**
     * 發送純文本郵件
     *
     * @param to      收件人郵箱
     * @param subject 郵件主題
     * @param content 郵件內容（純文本）
     */
    void sendSimpleMail(String to, String subject, String content);
}


