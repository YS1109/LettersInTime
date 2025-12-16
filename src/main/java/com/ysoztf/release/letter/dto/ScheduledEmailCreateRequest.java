package com.ysoztf.release.letter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 创建计划邮件请求体
 */
@Data
public class ScheduledEmailCreateRequest {

    /**
     * 收件人邮箱
     */
    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "收件人邮箱格式不正确")
    private String to;

    /**
     * 邮件主题
     */
    @NotBlank(message = "邮件主题不能为空")
    @Size(max = 4096, message = "邮件主题长度不能超过4096字符")
    private String subject;

    /**
     * 邮件内容
     */
    @NotBlank(message = "邮件内容不能为空")
    private String content;

    /**
     * 计划发送时间
     */
    @NotNull(message = "计划发送时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;
}


