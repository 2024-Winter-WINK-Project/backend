package com.WinkProject.budget.dto.response;

import lombok.Builder;
import lombok.Data;


import java.util.List;

@Data
@Builder
public class AdjustmentResponse {
    private String kakaoUrl;
    private String tossUrl;
    private String accountNumber;
    private String bankName;
    private List<Long> memberID;
}
