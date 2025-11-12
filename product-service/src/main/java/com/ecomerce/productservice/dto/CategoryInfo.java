package com.ecomerce.productservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryInfo {
    private Long id;
    private String name;
    private String slug;
}