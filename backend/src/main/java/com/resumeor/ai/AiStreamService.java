package com.resumeor.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class AiStreamService {
    public SseEmitter stream(String content) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                for (int index = 0; index < content.length(); index += 14) {
                    int end = Math.min(content.length(), index + 14);
                    emitter.send(SseEmitter.event().name("chunk").data(content.substring(index, end)));
                }
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (IOException exception) {
                emitter.completeWithError(exception);
            }
        });
        return emitter;
    }
}
