package com.relic.relic.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileUploadService {
    public String uploadImage(MultipartFile file) throws IOException;
}
