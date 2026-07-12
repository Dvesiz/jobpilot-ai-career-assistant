package com.resumeor.dashboard;

public record DashboardSummary(
        int resumeCount,
        Integer latestMatchScore,
        int monthlyInterviewCount,
        String nextAction,
        String nextActionPath
) {
}
