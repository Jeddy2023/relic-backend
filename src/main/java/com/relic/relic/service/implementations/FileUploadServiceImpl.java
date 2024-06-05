package com.relic.relic.service.implementations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.relic.relic.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        String folder = "relic";
        Map<?, ?> uploadResult = cloudinary.uploader()
                .upload(multipartFile.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder,
                                "use_filename", true,
                                "unique_filename", true,
                                "invalidate", true,
                                "public_id", "unique_public_id"
                        ));

        return (String) uploadResult.get("url");
    }
}
