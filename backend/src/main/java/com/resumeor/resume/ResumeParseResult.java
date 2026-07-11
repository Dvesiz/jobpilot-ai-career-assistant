package com.resumeor.resume;

import java.util.List;

public record ResumeParseResult(long resumeId, String fileName, int pageCount, String content, List<ResumeSection> sections) {
}
