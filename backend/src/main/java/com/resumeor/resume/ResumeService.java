package com.resumeor.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.resumeor.ai.AiGateway;
import com.resumeor.ai.AiRateLimiter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ResumeService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final JdbcTemplate jdbcTemplate;
    private final ResumeRepository resumeRepository;
    private final AiGateway aiGateway;
    private final AiRateLimiter aiRateLimiter;

    public ResumeService(JdbcTemplate jdbcTemplate, ResumeRepository resumeRepository, AiGateway aiGateway, AiRateLimiter aiRateLimiter) {
        this.jdbcTemplate = jdbcTemplate;
        this.resumeRepository = resumeRepository;
        this.aiGateway = aiGateway;
        this.aiRateLimiter = aiRateLimiter;
    }

    public ResumeOptimizeResult optimize(long userId, long resumeId) {
        aiRateLimiter.check(userId);
        ResumeDetail resume = findOwned(resumeId, userId);
        String fallback = "【优化后的简历表达】\n\n" + resume.originalContent()
                + "\n\n【优化建议】\n1. 为项目成果补充明确的业务指标。\n2. 用“负责 + 方法 + 结果”重写核心经历。\n3. 将目标岗位关键词自然融入技能与项目描述。";
        String optimized = aiGateway.generate(userId,
                "你是一位严谨的中国校招简历编辑。你的输出必须是一份可直接复制到 Word 的干净中文简历。"
                        + "绝不虚构经历、奖项、数据、技术栈或职责；原文没有的数据不要补写。"
                        + "禁止输出分析过程、修改建议、免责声明、Markdown 表格、连续星号、残缺链接或孤立的竖线。"
                        + "严格使用独占一行的二级标题：个人信息、求职意向、教育经历、项目经历、竞赛与成果、技能、校园经历；标题前后各保留一个空行。"
                        + "每个字段独占一行，项目条目严格按“项目名称｜时间”“技术栈：”“职责与成果：”分行输出；职责与成果每条以“• ”开头并独占一行。"
                        + "不得将邮箱、地点、岗位、项目名称、技术栈、链接或多条经历拼接在同一行；每一条成果保持原有量化数据，语言简洁准确。"
                        + "清理 PDF 解析造成的乱码、重复字符、断行和不完整的 Markdown 标记，但不得删除有价值的信息。",
                "请将以下 PDF 解析文本整理并重写为一份完整、排版干净的简历正文。只返回最终简历正文，不要返回其他内容：\n\n" + resume.originalContent(),
                fallback
        );
        resumeRepository.updateOptimizedContent(resumeId, userId, optimized);
        return new ResumeOptimizeResult(resumeId, optimized);
    }

    public ResumeMatchResult match(long userId, ResumeMatchRequest request) {
        aiRateLimiter.check(userId);
        ResumeDetail resume = findOwned(request.resumeId(), userId);
        List<String> keywords = List.of("产品", "设计", "用户研究", "Figma", "AI", "数据分析", "Java", "React", "沟通", "项目管理");
        String source = (resume.originalContent() + " " + request.jobJd()).toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String keyword : keywords) {
            boolean inResume = resume.originalContent().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
            boolean inJd = request.jobJd().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
            if (inResume && inJd) {
                matched.add(keyword);
            } else if (inJd) {
                missing.add(keyword);
            }
        }
        int score = Math.min(95, Math.max(35, 55 + matched.size() * 9 - missing.size() * 4));
        String fallback = "与“" + request.jobName() + "”的匹配度为 " + score + " 分。"
                + "已匹配能力：" + (matched.isEmpty() ? "待进一步确认" : String.join("、", matched)) + "。"
                + "建议补强：" + (missing.isEmpty() ? "突出量化成果与业务影响" : String.join("、", missing)) + "。";
        String report = aiGateway.generate(userId,
                "你是一位专业招聘顾问。输出中文岗位匹配报告，只使用纯文本，禁止 Markdown 标题、星号、表格、横线和英文小标题。"
                        + "严格按三段输出：核心优势：；关注点：；面试准备：。每段最多 3 条，使用中文分号分隔。"
                        + "只引用简历中真实出现的项目、技能和数据，不得虚构经验。建议必须贴合目标岗位。",
                "简历：\n" + resume.originalContent() + "\n\n目标岗位：" + request.jobName() + "\n岗位 JD：\n" + request.jobJd(),
                fallback
        );
        resumeRepository.updateMatch(resume.id(), userId, request.jobJd(), score, report);
        return new ResumeMatchResult(score, matched, missing, report);
    }

    public ResumeDetail findOwned(long resumeId, long userId) {
        return resumeRepository.findOwned(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("简历不存在或无权访问"));
    }

    public List<ResumeSummary> list(long userId) {
        return resumeRepository.findAll(userId);
    }

    public void delete(long userId, long resumeId) {
        if (!resumeRepository.deleteOwned(resumeId, userId)) {
            throw new IllegalArgumentException("简历不存在或无权访问");
        }
    }

    public ResumeParseResult parseAndSave(long userId, MultipartFile file) throws IOException {
        validate(file);
        byte[] bytes = file.getBytes();
        String content;
        int pageCount;
        try (PDDocument document = Loader.loadPDF(bytes)) {
            pageCount = document.getNumberOfPages();
            content = normalize(new PDFTextStripper().getText(document));
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("未能从该 PDF 提取文本，请上传文字版简历");
        }

        long resumeId = save(userId, file.getOriginalFilename(), content);
        return new ResumeParseResult(resumeId, file.getOriginalFilename(), pageCount, content, splitSections(content));
    }

    private void validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择需要上传的 PDF 简历");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("简历文件不能超过 10MB");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("仅支持 PDF 格式的简历");
        }
        byte[] header = file.getInputStream().readNBytes(4);
        if (header.length < 4 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F') {
            throw new IllegalArgumentException("文件内容不是有效的 PDF");
        }
    }

    private long save(long userId, String fileName, String content) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO user_resume (user_id, file_name, original_content) VALUES (?, ?, ?)",
                    new String[]{"id"}
            );
            statement.setLong(1, userId);
            statement.setString(2, fileName);
            statement.setString(3, content);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("简历保存失败");
        }
        return key.longValue();
    }

    private String normalize(String content) {
        return content.replace('\u0000', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\\n\\n")
                .trim();
    }

    private List<ResumeSection> splitSections(String content) {
        String[] headings = {"个人信息", "教育经历", "工作经历", "项目经历", "专业技能", "技能证书", "实习经历", "Profile", "Education", "Experience", "Projects", "Skills"};
        List<ResumeSection> sections = new ArrayList<>();
        String currentTitle = "简历内容";
        StringBuilder currentContent = new StringBuilder();
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (isHeading(trimmed, headings)) {
                addSection(sections, currentTitle, currentContent);
                currentTitle = trimmed;
                currentContent = new StringBuilder();
            } else if (!trimmed.isBlank()) {
                if (!currentContent.isEmpty()) {
                    currentContent.append('\n');
                }
                currentContent.append(trimmed);
            }
        }
        addSection(sections, currentTitle, currentContent);
        return sections;
    }

    private boolean isHeading(String line, String[] headings) {
        for (String heading : headings) {
            if (line.equalsIgnoreCase(heading)) {
                return true;
            }
        }
        return false;
    }

    private void addSection(List<ResumeSection> sections, String title, StringBuilder content) {
        if (!content.isEmpty()) {
            sections.add(new ResumeSection(title, content.toString()));
        }
    }
}
