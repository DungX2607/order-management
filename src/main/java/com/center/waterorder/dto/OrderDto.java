package com.center.waterorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private String username;
    private String memberName;
    private Long menuItemId;
    private String menuItemName;
    private String categoryName;
    private String note;
    private Boolean pickedUp;
}
