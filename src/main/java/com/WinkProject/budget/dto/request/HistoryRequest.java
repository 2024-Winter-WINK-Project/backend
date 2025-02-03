package com.WinkProject.budget.dto.request;

import lombok.Data;

@Data
public class HistoryRequest {
    private String category;
    private Long amount;
    private String description;
}
