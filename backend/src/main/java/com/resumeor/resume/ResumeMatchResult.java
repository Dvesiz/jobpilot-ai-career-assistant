package com.resumeor.resume;

import java.util.List;

public record ResumeMatchResult(int score, List<String> matchedSkills, List<String> missingSkills, String report) {
}
