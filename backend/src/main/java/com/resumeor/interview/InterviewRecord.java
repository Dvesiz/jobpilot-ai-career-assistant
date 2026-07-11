package com.resumeor.interview;

import java.time.LocalDateTime;

public record InterviewRecord(long id, long resumeId, String jobName, String question, String userAnswer,
                              String aiComment, String standardAnswer, LocalDateTime createTime) {
}
