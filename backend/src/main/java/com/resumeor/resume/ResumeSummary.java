package com.resumeor.resume;

import java.time.LocalDateTime;

public record ResumeSummary(long id, String fileName, Integer matchScore, LocalDateTime createTime) {
}
