package com.resumeor.interview;

import com.resumeor.ai.AiStreamService;
import com.resumeor.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {
    private final InterviewService interviewService;
    private final AiStreamService aiStreamService;

    public InterviewController(InterviewService interviewService, AiStreamService aiStreamService) {
        this.interviewService = interviewService;
        this.aiStreamService = aiStreamService;
    }

    @PostMapping("/start")
    public ApiResponse<InterviewStartResult> start(@Valid @RequestBody InterviewStartRequest request, @RequestAttribute long userId) {
        return ApiResponse.success(interviewService.start(userId, request));
    }

    @PostMapping(value = "/answer/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter answer(@Valid @RequestBody InterviewAnswerRequest request, @RequestAttribute long userId) {
        return aiStreamService.stream(interviewService.answer(userId, request));
    }

    @PostMapping("/follow-up/{recordId}")
    public ApiResponse<InterviewFollowUpResult> followUp(@PathVariable long recordId, @RequestAttribute long userId) {
        return ApiResponse.success(interviewService.followUp(userId, recordId));
    }

    @GetMapping("/list")
    public ApiResponse<List<InterviewRecord>> list(@RequestAttribute long userId) {
        return ApiResponse.success(interviewService.list(userId));
    }

    @GetMapping("/{recordId}")
    public ApiResponse<InterviewRecord> detail(@PathVariable long recordId, @RequestAttribute long userId) {
        return ApiResponse.success(interviewService.findOwned(userId, recordId));
    }

    @GetMapping("/{recordId}/export")
    public ResponseEntity<byte[]> export(@PathVariable long recordId, @RequestAttribute long userId) {
        InterviewRecord record = interviewService.findOwned(userId, recordId);
        String content = "岗位：" + record.jobName() + "\n\n问题：\n" + record.question()
                + "\n\n我的回答：\n" + nullToEmpty(record.userAnswer())
                + "\n\nAI 点评：\n" + nullToEmpty(record.aiComment())
                + "\n\n参考回答：\n" + nullToEmpty(record.standardAnswer());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=interview-review-" + recordId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
