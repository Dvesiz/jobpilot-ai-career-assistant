package com.resumeor.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterviewAnswerRequest(@NotNull Long recordId, @NotBlank String answer) {
}
