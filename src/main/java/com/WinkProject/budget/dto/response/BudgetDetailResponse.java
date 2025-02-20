package com.WinkProject.budget.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class BudgetDetailResponse {
    private Long id;
    private String category;
    private boolean plus;
    private Long amount;
    private String description;
}
