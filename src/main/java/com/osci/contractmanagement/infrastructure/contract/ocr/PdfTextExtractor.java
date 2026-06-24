package com.osci.contractmanagement.infrastructure.contract.ocr;

import com.osci.contractmanagement.application.provider.TextExtractor;
import org.apache.pdfbox.Loader;
import org.springframework.stereotype.Component;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

@Component
public class PdfTextExtractor implements TextExtractor {

    @Override
    public boolean supports(String contentType) {
        return "application/pdf".equalsIgnoreCase(contentType);
    }

    @Override
    public String extractText(byte[] fileBytes) {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            return ContractTextCleaner.clean(rawText);
        } catch (IOException e) {
            throw new IllegalStateException("PDF 텍스트 추출에 실패했습니다.", e);
        }
    }
}