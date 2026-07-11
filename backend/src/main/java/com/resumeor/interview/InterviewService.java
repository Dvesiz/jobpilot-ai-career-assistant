package com.resumeor.interview;

import com.resumeor.ai.AiGateway;
import com.resumeor.ai.AiRateLimiter;
import com.resumeor.resume.ResumeDetail;
import com.resumeor.resume.ResumeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final ResumeService resumeService;
    private final AiGateway aiGateway;
    private final AiRateLimiter aiRateLimiter;

    public InterviewService(InterviewRepository interviewRepository, ResumeService resumeService, AiGateway aiGateway, AiRateLimiter aiRateLimiter) {
        this.interviewRepository = interviewRepository;
        this.resumeService = resumeService;
        this.aiGateway = aiGateway;
        this.aiRateLimiter = aiRateLimiter;
    }

    public InterviewStartResult start(long userId, InterviewStartRequest request) {
        aiRateLimiter.check(userId);
        ResumeDetail resume = resumeService.findOwned(request.resumeId(), userId);
        String fallback = request.focusOnResume()
                ? "请结合你简历中最贴近该岗位的项目，说明你负责的核心模块、遇到的技术难点，以及你如何用数据验证最终效果。"
                : "请介绍一个与你应聘岗位最相关的项目，说明你的具体职责、技术决策和最终结果。";
        String question = aiGateway.generate(userId,
                "你是一位严格但友善的技术面试官。只输出一个自然、具体的中文问题，不要解释。"
                        + "当要求基于简历时，先从简历中找与 JD 最相关的项目或技能，只问简历中明确出现的事实，禁止假设候选人有未写出的经验。"
                        + "问题必须同时贴合目标岗位的关键技术或业务要求。",
                "目标岗位：" + request.jobName() + "\n岗位 JD：" + request.jobJd()
                        + "\n是否基于简历出题：" + request.focusOnResume()
                        + (request.focusOnResume() ? "\n候选人简历：\n" + resume.originalContent() : ""),
                fallback
        );
        return new InterviewStartResult(interviewRepository.create(userId, request.resumeId(), request.jobName(), question), question);
    }

    public String answer(long userId, InterviewAnswerRequest request) {
        aiRateLimiter.check(userId);
        InterviewRecord record = findOwned(userId, request.recordId());
        String standardAnswer = "建议采用 STAR 结构：先说明项目背景和目标，再讲你的具体决策与协作方式，最后用可验证的业务或用户指标收束结果。";
        String fallback = "【回答点评】\n你的回答已经覆盖了项目背景，但可以更明确说明自己的决策依据与个人贡献。\n\n【改进方向】\n1. 补充一个量化指标。\n2. 说明你如何处理分歧或约束。\n3. 用一句话总结结果对业务或用户的价值。\n\n【参考回答】\n" + standardAnswer;
        String comment = aiGateway.generate(userId,
                "你是一位面试教练，请点评候选人的回答，给出具体改进方向和简短参考答案。",
                "岗位：" + record.jobName() + "\n问题：" + record.question() + "\n候选人回答：" + request.answer(),
                fallback
        );
        interviewRepository.updateAnswer(record.id(), userId, request.answer(), comment, standardAnswer);
        return comment;
    }

    public InterviewFollowUpResult followUp(long userId, long recordId) {
        aiRateLimiter.check(userId);
        InterviewRecord previous = findOwned(userId, recordId);
        if (previous.userAnswer() == null || previous.userAnswer().isBlank()) {
            throw new IllegalArgumentException("请先完成当前问题的回答");
        }
        String fallback = "你刚才提到“" + trim(previous.userAnswer(), 42) + "”。请具体说明你如何验证这个方案有效，并在出现分歧时如何推进团队达成共识？";
        String question = aiGateway.generate(userId,
                "你是一位面试官。请基于上一题回答，生成一个自然、具体的中文追问，只输出问题本身。",
                "岗位：" + previous.jobName() + "\n上一题：" + previous.question() + "\n候选人回答：" + previous.userAnswer() + "\n点评：" + previous.aiComment(),
                fallback
        );
        long nextId = interviewRepository.create(userId, previous.resumeId(), previous.jobName(), question);
        return new InterviewFollowUpResult(nextId, question);
    }

    public InterviewRecord findOwned(long userId, long recordId) {
        return interviewRepository.findOwned(recordId, userId).orElseThrow(() -> new IllegalArgumentException("面试记录不存在或无权访问"));
    }

    public List<InterviewRecord> list(long userId) {
        return interviewRepository.findAll(userId);
    }

    private String trim(String content, int maxLength) {
        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "…";
    }
}
