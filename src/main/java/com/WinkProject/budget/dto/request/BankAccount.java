package com.WinkProject.budget.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccount {
    private String bankAccountNumber;
}
