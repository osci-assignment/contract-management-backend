package com.osci.contractmanagement.application.provider;

public interface TextExtractor {
 
    boolean supports(String contentType);
 
    String extractText(byte[] fileBytes);
}