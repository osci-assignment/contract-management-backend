package com.osci.contractmanagement.application.provider;

public interface FileStorageProvider {
    String store(byte[] fileBytes, String originalFilename);
    byte[] load(String fileUrl);
    void delete(String fileUrl);
}