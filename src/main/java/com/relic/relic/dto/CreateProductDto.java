package com.relic.relic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDto {
    private String username;
    private String name;

    private String shortDescription;

    private String description;

    private Long categoryId;

    private String condition;

    private Long minimumBid;

    private String biddingTimeLimit;

    private MultipartFile productImage;
}

