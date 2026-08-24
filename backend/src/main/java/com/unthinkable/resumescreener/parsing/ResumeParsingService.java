package com.unthinkable.resumescreener.parsing;

import com.unthinkable.resumescreener.exception.ResumeParsingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Converts an uploaded resume file (PDF or plain text) into raw text for downstream
 * LLM-based extraction. See ADR 0004 for why PDFBox was chosen.
 */
@Service
public class ResumeParsingService {

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResumeParsingException("Uploaded resume file is empty");
        }

        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);

        try {
            if (fileName.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
            return extractFromPdf(file.getBytes());
        } catch (IOException e) {
            throw new ResumeParsingException("Failed to read uploaded file: " + file.getOriginalFilename(), e);
        }
    }

    private String extractFromPdf(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                throw new ResumeParsingException("Cannot parse an encrypted/password-protected PDF");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new ResumeParsingException("No extractable text found in PDF (it may be a scanned image)");
            }
            return text;
        } catch (IOException e) {
            throw new ResumeParsingException("Failed to parse PDF resume", e);
        }
    }
}
