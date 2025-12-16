package com.ysoztf.release.letter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ysoztf.release.letter.common.BizException;
import com.ysoztf.release.letter.common.ErrorCode;
import com.ysoztf.release.letter.entity.ScheduledEmail;
import com.ysoztf.release.letter.entity.ScheduledEmailStatus;
import com.ysoztf.release.letter.mapper.ScheduledEmailMapper;
import com.ysoztf.release.letter.service.MailService;
import com.ysoztf.release.letter.service.TextModerationService;
import com.ysoztf.release.letter.service.ScheduledEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.List;

@Service
@Slf4j
public class ScheduledEmailServiceImpl implements ScheduledEmailService {

    private final ScheduledEmailMapper scheduledEmailMapper;
    private final MailService mailService;
    private final TextModerationService textModerationService;
    private final Executor emailTaskExecutor;

    public ScheduledEmailServiceImpl(ScheduledEmailMapper scheduledEmailMapper,
                                     MailService mailService,
                                     TextModerationService textModerationService,
                                     @Qualifier("emailTaskExecutor") Executor emailTaskExecutor) {
        this.scheduledEmailMapper = scheduledEmailMapper;
        this.mailService = mailService;
        this.textModerationService = textModerationService;
        this.emailTaskExecutor = emailTaskExecutor;
    }

    @Override
    @Transactional
    public void scheduleEmail(String to, String subject, String content, LocalDateTime scheduledTime) {
        // 业务校验：不接受 5 分钟内的任务
        //LocalDateTime now = LocalDateTime.now();
        //if (scheduledTime.isBefore(now.plusMinutes(5))) {
        //    throw new BizException(ErrorCode.PARAM_ERROR, "计划发送时间必须晚于当前时间至少5分钟");
        //}

        ScheduledEmail email = new ScheduledEmail(to, subject, content, scheduledTime);
        // 自动审核通过后直接置为 APPROVED，进入待发送队列
        email.setStatus(ScheduledEmailStatus.APPROVED);
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
                        .eq(ScheduledEmail::getStatus, ScheduledEmailStatus.APPROVED)
                        .le(ScheduledEmail::getScheduledTime, now)
        );

        for (ScheduledEmail email : pendingEmails) {
            // 先抢占任务：把状态从 APPROVED 改成 SENDING，避免并发或下一轮定时任务重复发送
            int updated = scheduledEmailMapper.update(
                    null,
                    new LambdaUpdateWrapper<ScheduledEmail>()
                            .eq(ScheduledEmail::getId, email.getId())
                            .eq(ScheduledEmail::getStatus, ScheduledEmailStatus.APPROVED)
                            .set(ScheduledEmail::getStatus, ScheduledEmailStatus.SENDING)
            );
            if (updated == 0) {
                // 说明这条记录已经被其他线程/实例处理了，跳过
                continue;
            }

            emailTaskExecutor.execute(() -> {
                try {
                    // 发送前内容审核：标题 + 内容
                    String reviewText = email.getSubject() + "\n" + email.getContent();
                    boolean pass = textModerationService.isContentPass(reviewText);
                    if (!pass) {
                        log.warn("计划邮件内容未通过审核, id={}", email.getId());
                        email.setStatus(ScheduledEmailStatus.REJECTED);
                    } else {
                        mailService.sendSimpleMail(email.getRecipient(), email.getSubject(), email.getContent());
                        email.setStatus(ScheduledEmailStatus.SENT);
                        email.setSentTime(LocalDateTime.now());
                    }
                } catch (Exception ex) {
                    log.error("发送计划邮件失败, id={}", email.getId(), ex);
                    email.setStatus(ScheduledEmailStatus.FAILED);
                }
                // 不论成功或失败都更新状态
                scheduledEmailMapper.updateById(email);
            });
        }
    }
}

