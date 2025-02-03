package com.WinkProject.budget.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BudgetResponse {
    private Long totalAmount;
    private List<BudgetDetailResponse> details = new ArrayList<>();
}
