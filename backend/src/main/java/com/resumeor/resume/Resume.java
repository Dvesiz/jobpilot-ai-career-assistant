package com.resumeor.resume;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user_resume")
public class Resume {
    private Long id;
    private Long userId;
    private String originalContent;
    private String optimizedContent;
    private String jobJd;
    private Integer matchScore;
    private String matchReport;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    public String getOptimizedContent() { return optimizedContent; }
    public void setOptimizedContent(String optimizedContent) { this.optimizedContent = optimizedContent; }
    public String getJobJd() { return jobJd; }
    public void setJobJd(String jobJd) { this.jobJd = jobJd; }
    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }
    public String getMatchReport() { return matchReport; }
    public void setMatchReport(String matchReport) { this.matchReport = matchReport; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
