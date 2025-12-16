package com.ysoztf.release.letter.entity;

/**
 * 计划邮件状态
 */
public enum ScheduledEmailStatus {

    /**
     * 已创建，待审核
     */
    CREATED,

    /**
     * 审核通过，待发送
     */
    APPROVED,

    /**
     * 审核拒绝，不发送
     */
    REJECTED,

    /**
     * 发送中（已被某个任务抢占）
     */
    SENDING,

    /**
     * 已发送成功
     */
    SENT,

    /**
     * 发送失败
     */
    FAILED
}


