package com.ysoztf.release.letter.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailServiceImpl mailService;

    @BeforeEach
    void setUp() throws Exception {
        // 直接设置 from 字段，模拟配置注入
        Field fromField = MailServiceImpl.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(mailService, "791265812@qq.com");
    }

    @Test
    void sendSimpleMail_shouldUseJavaMailSenderWithCorrectMessage() {
        String to = "2353882323@qq.com";
        String subject = "Test Subject";
        String content = "Test Content";

        mailService.sendSimpleMail(to, subject, content);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getFrom()).isEqualTo("791265812@qq.com");
        assertThat(sentMessage.getTo()).containsExactly(to);
        assertThat(sentMessage.getSubject()).isEqualTo(subject);
        assertThat(sentMessage.getText()).isEqualTo(content);
    }
}


