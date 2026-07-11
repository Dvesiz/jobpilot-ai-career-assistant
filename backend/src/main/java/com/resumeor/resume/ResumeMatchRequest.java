package com.resumeor.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResumeMatchRequest(
        @NotNull Long resumeId,
        @NotBlank String jobName,
        @NotBlank String jobJd
) {
}
