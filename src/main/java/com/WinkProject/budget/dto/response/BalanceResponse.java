package com.WinkProject.budget.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceResponse {
    private Long balance;
}
