package com.center.waterorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    private Long id;
    private Long categoryId;
    private String name;
    private Integer displayOrder;
    private Boolean active;
}
