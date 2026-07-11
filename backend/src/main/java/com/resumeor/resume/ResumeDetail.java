package com.resumeor.resume;

public record ResumeDetail(long id, String originalContent, String optimizedContent, String jobJd, Integer matchScore, String matchReport) {
}
