package com.osci.contractmanagement.infrastructure.contract.file;

import com.osci.contractmanagement.application.provider.FileStorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalFileStorageProvider implements FileStorageProvider {
 
    private final Path storageDirectory;
 
    public LocalFileStorageProvider(@Value("${file.storage.local-path:./uploads}") String storagePath) {
        this.storageDirectory = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 디렉토리를 생성할 수 없습니다: " + this.storageDirectory, e);
        }
    }
 
    @Override
    public String store(byte[] fileBytes, String originalFilename) {
        String extension = extractExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = storageDirectory.resolve(storedFilename);
 
        try {
            Files.write(targetPath, fileBytes);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다: " + storedFilename, e);
        }
 
        return targetPath.toString();
    }
 
    @Override
    public byte[] load(String fileUrl) {
        try {
            return Files.readAllBytes(Paths.get(fileUrl));
        } catch (IOException e) {
            throw new IllegalStateException("파일을 읽을 수 없습니다: " + fileUrl, e);
        }
    }
 
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}