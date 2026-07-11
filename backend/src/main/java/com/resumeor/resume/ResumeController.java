package com.resumeor.resume;

import com.resumeor.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.resumeor.ai.AiStreamService;
import jakarta.validation.Valid;

import java.io.IOException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private final ResumeService resumeService;
    private final AiStreamService aiStreamService;

    public ResumeController(ResumeService resumeService, AiStreamService aiStreamService) {
        this.resumeService = resumeService;
        this.aiStreamService = aiStreamService;
    }

    @PostMapping("/upload")
    public ApiResponse<ResumeParseResult> upload(@RequestParam("file") MultipartFile file, @RequestAttribute long userId) throws IOException {
        return ApiResponse.success(resumeService.parseAndSave(userId, file));
    }

    @GetMapping("/{resumeId}")
    public ApiResponse<ResumeDetail> detail(@RequestAttribute long userId, @org.springframework.web.bind.annotation.PathVariable long resumeId) {
        return ApiResponse.success(resumeService.findOwned(resumeId, userId));
    }

    @GetMapping("/list")
    public ApiResponse<java.util.List<ResumeSummary>> list(@RequestAttribute long userId) {
        return ApiResponse.success(resumeService.list(userId));
    }

    @DeleteMapping("/{resumeId}")
    public ApiResponse<Void> delete(@RequestAttribute long userId, @org.springframework.web.bind.annotation.PathVariable long resumeId) {
        resumeService.delete(userId, resumeId);
        return ApiResponse.success(null);
    }

    @PostMapping(value = "/optimize/stream", produces = "text/event-stream")
    public SseEmitter optimize(@RequestParam long resumeId, @RequestAttribute long userId) {
        return aiStreamService.stream(resumeService.optimize(userId, resumeId).optimizedContent());
    }

    @PostMapping("/match")
    public ApiResponse<ResumeMatchResult> match(@Valid @RequestBody ResumeMatchRequest request, @RequestAttribute long userId) {
        return ApiResponse.success(resumeService.match(userId, request));
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Void> handleUnreadablePdf(IOException exception) {
        return ApiResponse.failure(422, "PDF 解析失败，请检查文件是否完整");
    }
}
