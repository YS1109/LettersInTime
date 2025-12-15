package com.ysoztf.release.letter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ysoztf.release.letter.entity.ScheduledEmail;
import com.ysoztf.release.letter.mapper.ScheduledEmailMapper;
import com.ysoztf.release.letter.service.MailService;
import com.ysoztf.release.letter.service.ScheduledEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.List;

@Service
public class ScheduledEmailServiceImpl implements ScheduledEmailService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledEmailServiceImpl.class);

    private final ScheduledEmailMapper scheduledEmailMapper;
    private final MailService mailService;
    private final Executor emailTaskExecutor;

    public ScheduledEmailServiceImpl(ScheduledEmailMapper scheduledEmailMapper,
                                     MailService mailService,
                                     @Qualifier("emailTaskExecutor") Executor emailTaskExecutor) {
        this.scheduledEmailMapper = scheduledEmailMapper;
        this.mailService = mailService;
        this.emailTaskExecutor = emailTaskExecutor;
    }

    @Override
    @Transactional
    public void scheduleEmail(String to, String subject, String content, LocalDateTime scheduledTime) {
        ScheduledEmail email = new ScheduledEmail(to, subject, content, scheduledTime);
        scheduledEmailMapper.insert(email);
    }

    /**
     * 定时任务：每分钟扫描一次数据库，发送到期的邮件。
     * cron 表达式可以根据需要调整。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void sendDueEmails() {
        LocalDateTime now = LocalDateTime.now();

        List<ScheduledEmail> pendingEmails = scheduledEmailMapper.selectList(
                new LambdaQueryWrapper<ScheduledEmail>()
                        .eq(ScheduledEmail::getStatus, "PENDING")
                        .le(ScheduledEmail::getScheduledTime, now)
        );

        for (ScheduledEmail email : pendingEmails) {
            // 先抢占任务：把状态从 PENDING 改成 SENDING，避免并发或下一轮定时任务重复发送
            int updated = scheduledEmailMapper.update(
                    null,
                    new LambdaUpdateWrapper<ScheduledEmail>()
                            .eq(ScheduledEmail::getId, email.getId())
                            .eq(ScheduledEmail::getStatus, "PENDING")
                            .set(ScheduledEmail::getStatus, "SENDING")
            );
            if (updated == 0) {
                // 说明这条记录已经被其他线程/实例处理了，跳过
                continue;
            }

            emailTaskExecutor.execute(() -> {
                try {
                    mailService.sendSimpleMail(email.getRecipient(), email.getSubject(), email.getContent());
                    email.setStatus("SENT");
                    email.setSentTime(LocalDateTime.now());
                } catch (Exception ex) {
                    log.error("发送计划邮件失败, id={}", email.getId(), ex);
                    email.setStatus("FAILED");
                }
                // 不论成功或失败都更新状态
                scheduledEmailMapper.updateById(email);
            });
        }
    }
}

