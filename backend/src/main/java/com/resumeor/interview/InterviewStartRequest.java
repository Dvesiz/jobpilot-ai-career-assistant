package com.resumeor.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterviewStartRequest(@NotNull Long resumeId, @NotBlank String jobName, @NotBlank String jobJd, boolean focusOnResume) {
}
