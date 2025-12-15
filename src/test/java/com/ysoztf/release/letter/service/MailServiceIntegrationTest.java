package com.ysoztf.release.letter.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 基于 SpringBoot 应用上下文的集成测试（通测）
 * 通过 @SpringBootTest 启动应用，注入 MailService，
 * 使用真实的 JavaMailSender 和配置（会真的发邮件）。
 */
@SpringBootTest
class MailServiceIntegrationTest {

    @Autowired
    private MailService mailService;

    @Test
    void sendSimpleMail_shouldSendRealMailWithProjectConfig() {
        // 这里会使用 mail-config.yml 中配置的发件人账号和授权码
        // 请把收件人改成你自己的测试邮箱，避免打扰其他人
        String to = "ysoztf@gmail.com";
        String subject = "Integration Test - Real Mail";
        String content = "这是通过 SpringBoot 集成测试发出的真实测试邮件。";

        mailService.sendSimpleMail(to, subject, content);

        // 如果执行过程中没有抛异常，说明调用成功并已交给 SMTP 服务器发送
    }
}


