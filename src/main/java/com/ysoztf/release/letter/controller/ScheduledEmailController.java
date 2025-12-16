package com.ysoztf.release.letter.controller;

import com.ysoztf.release.letter.common.ApiResponse;
import com.ysoztf.release.letter.dto.ScheduledEmailCreateRequest;
import com.ysoztf.release.letter.service.ScheduledEmailService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 计划邮件相关接口
 */
@RestController
@RequestMapping("/api/scheduledEmails")
public class ScheduledEmailController {

    private final ScheduledEmailService scheduledEmailService;

    public ScheduledEmailController(ScheduledEmailService scheduledEmailService) {
        this.scheduledEmailService = scheduledEmailService;
    }

    /**
     * 创建计划发送邮件任务
     *
     * 示例：
     * POST /api/scheduled-emails
     * Content-Type: application/json
     * {
     *   "to": "xxx@qq.com",
     *   "subject": "测试标题",
     *   "content": "测试内容",
     *   "scheduledTime": "2025-12-31 10:00:00"
     * }
     */
    @PostMapping("/create")
    public ApiResponse<Void> createScheduledEmail(@Valid @RequestBody ScheduledEmailCreateRequest request) {
        scheduledEmailService.scheduleEmail(
                request.getTo(),
                request.getSubject(),
                request.getContent(),
                request.getScheduledTime()
        );
        return ApiResponse.success(null);
    }
}


