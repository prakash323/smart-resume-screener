package com.unthinkable.resumescreener.parsing;

import com.unthinkable.resumescreener.exception.ResumeParsingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeParsingServiceTest {

    private final ResumeParsingService service = new ResumeParsingService();

    @Test
    void extractsTextFromPlainTextFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain",
                "Jane Doe - Java Engineer".getBytes(StandardCharsets.UTF_8));

        String text = service.extractText(file);

        assertThat(text).isEqualTo("Jane Doe - Java Engineer");
    }

    @Test
    void extractsTextFromPdfFile() throws IOException {
        byte[] pdfBytes = generateSimplePdf("Jane Doe - Java Engineer with Spring Boot experience");
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        String text = service.extractText(file);

        assertThat(text).contains("Jane Doe - Java Engineer with Spring Boot experience");
    }

    @Test
    void throwsOnEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> service.extractText(file))
                .isInstanceOf(ResumeParsingException.class);
    }

    @Test
    void throwsOnCorruptPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "not a real pdf".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.extractText(file))
                .isInstanceOf(ResumeParsingException.class);
    }

    private byte[] generateSimplePdf(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
