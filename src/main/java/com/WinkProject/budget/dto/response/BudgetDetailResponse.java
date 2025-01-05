package com.WinkProject.budget.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class BudgetDetailResponse {
    private String category;
    private Long amount;
    private String description;
}
