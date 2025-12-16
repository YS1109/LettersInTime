package com.ysoztf.release.letter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("scheduled_email")
public class ScheduledEmail {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收件人邮箱
     */
    private String recipient;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件正文
     */
    private String content;

    /**
     * 计划发送时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 实际发送时间
     */
    private LocalDateTime sentTime;

    /**
     * 状态：CREATED / APPROVED / REJECTED / SENDING / SENT / FAILED
     */
    private ScheduledEmailStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    public ScheduledEmail() {
    }

    public ScheduledEmail(String recipient, String subject, String content, LocalDateTime scheduledTime) {
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.status = ScheduledEmailStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }

    public ScheduledEmailStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduledEmailStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


