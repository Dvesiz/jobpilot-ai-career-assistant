package com.resumeor.resume;

import com.resumeor.auth.JwtService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @Test
    void uploadsAndParsesTextPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", samplePdf());
        mockMvc.perform(multipart("/api/resume/upload").file(file).header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileName").value("resume.pdf"))
                .andExpect(jsonPath("$.data.content").value(org.hamcrest.Matchers.containsString("Jane Doe")));
    }

    @Test
    void rejectsNonPdfFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "not a pdf".getBytes());
        mockMvc.perform(multipart("/api/resume/upload").file(file).header("Authorization", token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private byte[] samplePdf() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(72, 720);
                stream.showText("Jane Doe");
                stream.newLineAtOffset(0, -18);
                stream.showText("Experience");
                stream.newLineAtOffset(0, -18);
                stream.showText("Product Designer");
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private String token() {
        return "Bearer " + jwtService.createToken(1L, "demo");
    }
}
